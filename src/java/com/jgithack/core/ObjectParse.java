/**
 * JHack.net
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.jgithack.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.InflaterInputStream;

import com.jgithack.io.FileUtils;

/**
 * ObjectParse－.git/objects的解析方法
 * 
 * @author Albert Wang
 * @version $Id: GitHack.java, V0.1 2016-10-29 20:03:00, jawangwen@qq.com $
 */
public class ObjectParse {
    private final Pattern headPattern   = Pattern.compile("^(\\w+)\\s+(\\d+)");
    private final Pattern commitPattern = Pattern.compile("^tree.+?([0-9a-zA-Z]+)");

    private File          objectFile;
    private byte[]        objectData;
    private String        objectType;
    private String        objectText;
    private Set<String>   relatedSha1;

    /**
     * 加载object文件进行解析
     * 
     * @param objectPath    object文件地址
     * @return              加载后的解析方法对象（该对象可获取解析结果）
     * @throws Exception    加载失败则抛出异常
     */
    public static ObjectParse load(String objectPath) throws Exception {
        ObjectParse objectParse = new ObjectParse(objectPath);
        objectParse.parse();
        return objectParse;
    }

    /**
     * 获取GIT对象类型
     * 
     * @return              GIT对象类型
     */
    public String getType() {
        return objectType;
    }

    /**
     * 获取GIT对象文件内容
     * 
     * @return              GIT对象文件内容
     */
    public String catFile() {
        return objectText;
    }

    /**
     * 查找GIT对象关联的其他对象
     * 
     * @return              被关联对象的sha1集合
     */
    public Set<String> findRelatedSha1() {
        return relatedSha1 != null ? relatedSha1 : new HashSet<String>();
    }

    /**
     * 构造器方法，请使用load接口来加载和解析GIT对象文件
     * 
     * @param objectPath    GIT对象文件地址
     * @throws IOException  
     */
    private ObjectParse(String objectPath) throws IOException {
        objectFile = new File(objectPath);
        objectData = unzip(FileUtils.readFileToByteArray(objectFile));
        relatedSha1 = new HashSet<String>();
    }

    /**
     * 对对象文件的原始字节inflater解压缩
     * 
     * @param bytes                 对象文件的原始字节素组
     * @return                      解压缩后的字节素组
     * @throws IOException          解压缩失败则抛出异常
     */
    private byte[] unzip(byte[] bytes) throws IOException {
        if (bytes != null) {
            byte[] result = new byte[0];

            InflaterInputStream stream = new InflaterInputStream(new ByteArrayInputStream(bytes));
            while (true) {
                byte[] buffer = new byte[1024];
                int size = stream.read(buffer, 0, buffer.length);
                if (size < 0) {
                    break;
                }
                result = new byte[result.length + size];
                System.arraycopy(buffer, 0, result, 0, size);
            }

            return result;
        }
        return null;
    }

    /**
     * 解析对象文件
     * 
     * @throws IOException
     */
    private void parse() throws IOException {
        ObjectReader reader = new ObjectReader(objectData);

        objectType = reader.nextHead();
        if (objectType.equals("commit")) {
            objectText = reader.nextString();
            Matcher matcher = commitPattern.matcher(objectText);
            if (matcher.find()) {
                relatedSha1.add(matcher.group(1));
            }
        } else if (objectType.equals("tree")) {
            while (reader.isAvailable()) {
                reader.nextString(); //  blobInfo
                String blobSha1 = reader.nextHexString(20);
                relatedSha1.add(blobSha1);
            }
        } else if (objectType.equals("blob")) {
            objectText = reader.nextString();
        } else {
            throw new IOException(String.format("Unsupport object type \"%s\", file: %s",
                objectType, objectFile));
        }
    }

    /**
     * ObjectReader－.git/objects文件解压缩后的字节读取器
     * 
     * @author Albert Wang
     * @version $Id: ObjectParse.java, V0.1 Oct 30, 2016 3:47:32 PM jawangwen@qq.com $
     */
    private class ObjectReader {
        private final byte[] data;
        private int          offset = 0;
        private int          limit  = 0;

        /**
         * 读取器构造方法
         * 
         * @param objectData        待读取的字节数组
         */
        public ObjectReader(byte[] objectData) {
            this.data = objectData;
        }

        /**
         * 探测是否还有未读取的字节
         * 
         * @return
         */
        public boolean isAvailable() {
            return offset < limit;
        }

        /**
         * 读取头部，并返回对象类型
         * 
         * @return                  头部指明的对象文件类型
         * @throws IOException      读取失败则抛出异常
         */
        public String nextHead() throws IOException {
            String head = nextString();

            Matcher headMatcher = headPattern.matcher(head);
            if (!headMatcher.find()) {
                throw new IOException(String.format("Unkown object head \"%s\"", head));
            }

            objectType = headMatcher.group(1);
            limit += head.length() + 1 + Integer.valueOf(headMatcher.group(2));
            if (limit > data.length) {
                throw new IOException(String.format("Invalid head size, head: %s", head));
            }

            return objectType;
        }

        /**
         * 读取下一批字节作为字符串（截至到0x00或数据末尾）
         * 
         * @return                  已读取的字符串
         */
        public String nextString() {
            int start = offset;
            int length = 0;

            while (offset < data.length && data[offset++] != 0) {
                length++;
            }

            return new String(data, start, length);
        }

        /**
         * 读取下一批字节作为十六进制字符串
         * 
         * @param byteNum           需要读取的字节数
         * @return                  已读取的十六进制字符串
         */
        public String nextHexString(int byteNum) {
            if (offset + byteNum > data.length) {
                throw new RuntimeException(String.format(
                    "nextHexString failed, offset: %d, byteNum: %d", offset, byteNum));
            }

            StringBuffer sb = new StringBuffer(byteNum * 2);
            for (int index = 0; index < byteNum; index++) {
                String hex = Integer.toHexString(data[offset++] & 0xFF);
                sb.append(hex.length() < 2 ? "0" : "").append(hex);
            }

            return sb.toString();
        }
    }
}
