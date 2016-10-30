/**
 * JHack.net
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.jgithack.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import com.jgithack.io.FileUtils;
import com.jgithack.lang.StringUtil;

/**
 * PageDownload－页面下载方法
 * 
 * @author Albert Wang
 * @version $Id: PageDownload.java, V0.1 2016-10-29 20:00:00, jawangwen@qq.com $
 */
public class PageDownload {
    /** 页面地址 */
    private String              remoteUrl;

    /** 访问参数 */
    private Map<String, String> params;

    /** 重试次数 */
    private int                 tryTimes    = 3;

    /** 本地文件是否优先，如果本地优先并且本地文件存在，下载动作不会被执行 */
    private boolean             nativeFirst = false;

    /**
     * 页面下载方法
     * 
     * @param remoteUrl     页面地址
     */
    public PageDownload(String remoteUrl) {
        this.remoteUrl = assureRemoteUrl(remoteUrl);
    }

    /**
     * 页面下载方法
     * 
     * @param remoteUrl     页面地址
     * @param params        访问参数，可为null
     */
    public PageDownload(String remoteUrl, Map<String, String> params) {
        this.remoteUrl = assureRemoteUrl(remoteUrl);
        this.params = params;
    }

    /**
     * 设置访问参数（如果有参数的话）
     * 
     * @param params        访问参数，可为null
     * @return              页面下载方法对象
     */
    public PageDownload withParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    /**
     * 设置下载重试次数（如果需要重试的话）
     * 
     * @param tryTimes      下载重试次数
     * @return              页面下载方法对象
     */
    public PageDownload withTryTimes(int tryTimes) {
        this.tryTimes = tryTimes;
        return this;
    }

    /**
     * 设置本地文件优先（如果不希望重复下载的话）
     * 
     * @param enable        true表示本地文件优先，false表示远程文件优先
     * @return              页面下载方法对象
     */
    public PageDownload nativeFirst(boolean enable) {
        this.nativeFirst = enable;
        return this;
    }

    /**
     * 下载页面并保存到本地文件
     * 
     * 如果设置了本地优先并且本地文件存在，下载动作不会被执行
     * 
     * @param localPath     要保存到的本地文件地址
     * @return              文件的字节数
     * @throws IOException
     */
    public long saveAs(String localPath) throws IOException {
        long pageSize = 0;
        File localFile = new File(localPath);

        if (!nativeFirst || !localFile.exists()) {
            for (int tryTimes = 0; tryTimes < this.tryTimes; tryTimes++) {
                try {
                    pageSize = doSave(localFile);
                    if (pageSize > 0) {
                        break;
                    }
                } catch (Exception exception) {
                    if (tryTimes == this.tryTimes) {
                        throw new IOException("saveAs failed, localPath: " + localPath, exception);
                    }
                }
            }
        } else {
            pageSize = localFile.length();
        }

        return pageSize;
    }

    /**
     * 执行下载，并保存到本地文件
     * 
     * @param localFile     要保存到的本地文件地址
     * @return              文件的字节数
     * @throws IOException
     */
    private long doSave(File localFile) throws IOException {
        long pageSize = -1;

        HttpURLConnection httpConnection = newHttpConnection(encodeUrl(remoteUrl, params));
        int code = httpConnection.getResponseCode();
        InputStream inputStream = httpConnection.getInputStream();
        try {
            if (code != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException(String.format("Read Page failed, code: %s", code));
            }

            byte[] buffer = new byte[1024];
            while (true) {
                int result = inputStream.read(buffer, 0, buffer.length);
                if (result < 0) {
                    break;
                }
                pageSize = pageSize >= 0 ? pageSize : 0;
                byte[] output = Arrays.copyOf(buffer, result);
                FileUtils.writeByteArrayToFile(localFile, output, (pageSize > 0));
                pageSize += result;
            }
        } finally {
            inputStream.close();
            httpConnection.disconnect();
        }

        return pageSize;
    }

    /**
     * 确认页面地址是否合法
     * 
     * @param remoteUrl     页面地址
     * @return              若合法，则返回地址地址本身
     */
    private String assureRemoteUrl(String remoteUrl) {
        if (StringUtil.isBlank(remoteUrl)) {
            throw new RuntimeException("Empty remoteUrl");
        }
        if (!remoteUrl.toLowerCase().startsWith("http://")) {
            throw new RuntimeException("Invalid remoteUrl(not start with http://): " + remoteUrl);
        }
        return remoteUrl;
    }

    /**
     * 对页面地址及访问参数进行编码
     * 
     * @param remoteUrl     页面地址
     * @param params        访问参数（可以为null）
     * @return              编码后的地址
     */
    private String encodeUrl(String remoteUrl, Map<String, String> params) {
        StringBuffer sb = new StringBuffer(remoteUrl);

        if (params != null) {
            sb.append("?");
            for (Entry<String, String> param : params.entrySet()) {
                sb.append(param.getKey());
                sb.append("=");
                sb.append(String.valueOf(param.getValue()));
                sb.append("&");
            }
            if (sb.charAt(sb.length() - 1) == '&') {
                sb.deleteCharAt(sb.length() - 1);
            }
        }

        return sb.toString().replaceAll(" ", "%20");
    }

    /**
     * 创建HTTP连接
     * 
     * @param requestUrl        编码后的页面地址
     * @return                  打开后的HTTP连接
     * @throws IOException
     */
    private HttpURLConnection newHttpConnection(String requestUrl) throws IOException {
        try {
            URL url = new URL(requestUrl);

            HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setRequestMethod("GET");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setReadTimeout(3000);

            return httpConnection;
        } catch (Exception exception) {
            throw new IOException("New HTTP Connection failed", exception);
        }
    }
}
