/**
 * JHack.net
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.jgithack;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jgithack.core.IndexParse;
import com.jgithack.core.ObjectParse;
import com.jgithack.core.PageDownload;
import com.jgithack.io.FileUtils;
import com.jgithack.lang.StringUtil;
import com.jgithack.log.LogUtils;
import com.jgithack.model.IndexEntry;

/**
 * GitHack
 * 
 * @author Albert Wang
 * @version $Id: GitHack.java, V0.1 2016-10-29 20:10:00, jawangwen@qq.com $
 */
public class GitHack {
    private final Pattern logPattern  = Pattern.compile("^([0-9a-zA-Z]+) ([0-9a-zA-Z]+)");

    private String        rootUrl     = "";
    private String        downloadDir = null;
    private int           tryTimes    = 3;
    private boolean       nativeFirst = false;
    private boolean       skipError   = true;

    private int           threadSize  = 20;

    public GitHack() {
    }

    public GitHack(String rootUrl) throws MalformedURLException {
        this.withRootUrl(rootUrl).withDownloadDir(this.rootUrl.replace("http://", ""));
    }

    public GitHack(String rootUrl, String downloadDir) {
        this.withRootUrl(rootUrl).withDownloadDir(downloadDir);
    }

    public GitHack withRootUrl(String rootUrl) {
        this.rootUrl = StringUtil.trimToEmpty(rootUrl).replaceFirst("\\.git$", "")
            .replaceAll("/$", "");
        return this;
    }

    public GitHack withDownloadDir(String downloadDir) {
        this.downloadDir = StringUtil.trimToEmpty(downloadDir).replaceAll("/$", "");
        return this;
    }

    public GitHack withTryTimes(int tryTimes) {
        this.tryTimes = tryTimes;
        return this;
    }

    public GitHack nativeFirst(boolean enable) {
        this.nativeFirst = enable;
        return this;
    }

    public GitHack skipError(boolean enable) {
        this.skipError = enable;
        return this;
    }

    public GitHack withThreadSize(int threadSize) {
        this.threadSize = threadSize;
        return this;
    }

    /**
     * Build hack, i.e. object directories
     * 
     * @return
     * @throws Exception
     */
    public GitHack build() throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            LogUtils.info("GitHack build start");

            assureParams();

            LogUtils.info("rootUrl:     %s", rootUrl);
            LogUtils.info("downloadDir: %s", downloadDir);

            downloadFile("/.git/index");
            downloadFile("/.git/logs/HEAD");
            downloadFile("/.git/ORIG_HEAD");
            downloadFile("/.git/config");
            downloadFile("/.git/COMMIT_EDITMSG");
            downloadFile("/.git/HEAD");
            downloadFile("/.git/refs/heads/master");

            downloadObjects();

            LogUtils.info("GitHack build success");
        } finally {
            LogUtils.info("GitHack cost %ss", (System.currentTimeMillis() - startTime) / 1000);
        }

        return this;
    }

    public void close() {

    }

    public void checkout() throws Exception {
        LogUtils.info("GitHack checkout start");

        for (IndexEntry entry : IndexParse.load(downloadDir + "/.git/index").getIndexEntries()) {
            downloadObject(entry.getSha1());
            restoreObjectToFile(entry.getSha1(), entry.getName());
        }

        LogUtils.info("GitHack checkout success");
    }

    private String downloadFile(String pagePath) throws Exception {
        try {
            String requestUrl = rootUrl + pagePath;
            String filePath = downloadDir + pagePath;
            long fileSize = new PageDownload(requestUrl).withTryTimes(tryTimes)
                .nativeFirst(nativeFirst).saveAs(filePath);
            if (fileSize <= 0) {
                throw new RuntimeException("None read from page: " + requestUrl);
            }
            LogUtils.info("download file \"%s\", size: %s", pagePath, fileSize);
            return filePath;
        } catch (Exception exception) {
            if (!skipError) {
                throw exception;
            }
            LogUtils.error(exception, "download page failed, file: %s", pagePath);
            return null;
        }
    }

    private void downloadObjects() throws Exception {
        AtomicInteger taskCount = new AtomicInteger(0);
        BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<Runnable>();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(threadSize, threadSize, 30,
            TimeUnit.SECONDS, tasks);
        ConcurrentMap<String, Exception> result = new ConcurrentHashMap<String, Exception>();
        try {

            for (String sha1 : readCommitSha1(downloadDir + "/.git/logs/HEAD")) {
                startObjectDownload(sha1, taskCount, executor, result);
            }

            while (taskCount.get() > 0) {
                Thread.sleep(1000);
            }

            for (Exception exception : result.values()) {
                if (!(exception instanceof NotException)) {
                    throw exception;
                }
            }
        } catch (Exception exception) {
            if (!skipError) {
                throw new RuntimeException("download objects failed", exception);
            }
            LogUtils.error(exception, "download objects failed");
        } finally {
            executor.shutdown();
        }
    }

    private String downloadObject(String sha1) throws Exception {
        try {
            String folder = String.format("/.git/objects/%s/%s", sha1.substring(0, 2),
                sha1.substring(2));
            String requestUrl = rootUrl + folder;
            String objectPath = downloadDir + folder;
            long objectSize = new PageDownload(requestUrl).withTryTimes(tryTimes)
                .nativeFirst(nativeFirst).saveAs(objectPath);
            if (objectSize <= 0) {
                throw new RuntimeException("None read from requestUrl: " + requestUrl);
            }
            LogUtils.info("download object, sha1: %s, size: %s", sha1, objectSize);
            return objectPath;
        } catch (Exception exception) {
            if (!skipError) {
                throw exception;
            }
            LogUtils.error(exception, "download object failed, sha1: %s", sha1);
        }
        return null;
    }

    private void restoreObjectToFile(String sha1, String filePath) throws Exception {
        filePath = filePath.startsWith("/") ? filePath : "/" + filePath;
        LogUtils.info("restore file: %s, sha1: %s", filePath, sha1);
        try {
            // 下载文件
            String folder = String.format("/.git/objects/%s/%s", sha1.substring(0, 2),
                sha1.substring(2));

            // 解压文件
            String fileContent = ObjectParse.load(downloadDir + folder).catFile();

            // 存储文件
            File localFile = new File(downloadDir + filePath);
            FileUtils.writeStringToFile(localFile, fileContent.replaceFirst("^blob \\d+\\x00", ""));
        } catch (Exception exception) {
            if (!skipError) {
                throw exception;
            }
            LogUtils.error(exception, "download failed, filePath: %s, sha1: %s", filePath, sha1);
        }
    }

    /**
     * 解压对象为文本
     * 
     * @param filePath      解决路径
     * @return
     * @throws IOException
     */

    private void assureParams() {
        if (StringUtil.isBlank(rootUrl)) {
            throw new RuntimeException("Empty rootUrl");
        }
        if (!rootUrl.toLowerCase().startsWith("http://")) {
            throw new RuntimeException("Invalid rootUrl(not start with http://): " + rootUrl);
        }
        if (StringUtil.isBlank(downloadDir)) {
            this.withDownloadDir(this.rootUrl.replace("http://", ""));
        }
    }

    private Set<String> readCommitSha1(String logPath) throws Exception {
        Set<String> result = new HashSet<String>();

        File file = new File(logPath);
        for (String line : FileUtils.readLines(file)) {
            Matcher matcher = logPattern.matcher(line);
            if (matcher.find()) {
                result.add(matcher.group(1));
                result.add(matcher.group(2));
            }
        }

        return result;
    }

    private void startObjectDownload(String sha1, AtomicInteger taskCount,
                                     ThreadPoolExecutor executor,
                                     ConcurrentMap<String, Exception> result) {
        if (!result.containsKey(sha1)) {
            if (result.putIfAbsent(sha1, new NotException()) == null) {
                executor.execute(new ObjectDownload(sha1, taskCount, executor, result));
                taskCount.getAndIncrement();
            }
        }
    }

    /** 非异常，GIT对象下载时作为哨兵 */
    private class NotException extends Exception {
        /**  */
        private static final long serialVersionUID = 8780565940390318109L;
    }

    private class ObjectDownload implements Runnable {
        private String                           sha1;
        private ThreadPoolExecutor               executor;
        private AtomicInteger                    taskCount;
        private ConcurrentMap<String, Exception> result;

        public ObjectDownload(String sha1, AtomicInteger taskCount, ThreadPoolExecutor executor,
                              ConcurrentMap<String, Exception> result) {
            this.sha1 = sha1;
            this.executor = executor;
            this.taskCount = taskCount;
            this.result = result;
        }

        @Override
        public void run() {
            try {
                String objectPath = downloadObject(sha1);
                if (StringUtil.isNotBlank(objectPath)) {
                    for (String sha1 : ObjectParse.load(objectPath).findRelatedSha1()) {
                        startObjectDownload(sha1, taskCount, executor, result);
                    }
                }
                result.put(sha1, new NotException());
            } catch (Exception exception) {
                LogUtils.error(exception, "ObjectDownload detect error! sha1: %s", sha1);
                result.put(sha1, exception);
            } finally {
                taskCount.getAndDecrement();
            }
        }
    }
}
