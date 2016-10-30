/**
 * JHack.net
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.jgithack.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.jgithack.io.FileUtils;
import com.jgithack.model.IndexEntry;

/**
 * IndexParse－".git/index"文件解析方法
 * 
 * @author Albert Wang
 * @version $Id: IndexParse.java, V0.1 2016-10-29 20:05:00, jawangwen@qq.com $
 */
public class IndexParse {
    /** ".git/index"文件路径 */
    private String           indexPath;

    /** ".git/index"文件里的信息 */
    private List<IndexEntry> indexEntries;

    /**
     * 加载".git/index"文件进行解析
     * 
     * @param indexPath
     * @return                  加载后的解析方法对象（该对象可获取解析结果）
     * @throws Exception        加载失败则抛出异常
     */
    public static IndexParse load(String indexPath) throws IOException {
        IndexParse indexParse = new IndexParse(indexPath);
        indexParse.parse();
        return indexParse;
    }

    /**
     * 获取".git/index"文件里的信息
     * 
     * @return
     */
    public List<IndexEntry> getIndexEntries() {
        return indexEntries != null ? indexEntries : new ArrayList<IndexEntry>();
    }

    /**
     * 构造器方法，请使用load接口来加载和解析".git/index"文件
     * 
     * @param indexPath         ".git/index"文件地址
     */
    private IndexParse(String indexPath) {
        this.indexPath = indexPath;
    }

    /**
     * 解析".git/index"文件
     * 
     * @throws IOException      解析失败则抛出异常
     */
    private void parse() throws IOException {
        List<IndexEntry> result = new ArrayList<IndexEntry>();

        try {
            IndexReader reader = new IndexReader(indexPath);

            String signature = reader.nextString(4);
            if (!"DIRC".equals(signature)) {
                throw new IOException("Not a GIT index file");
            }

            Integer version = reader.nextInteger();
            if (version < 2 || version > 3) {
                throw new IOException(String.format("Unsupported version: %s", version));
            }

            Integer entrySize = reader.nextInteger();
            for (int id = 1; id <= entrySize; id++) {
                IndexEntry entry = new IndexEntry();

                entry.setId(id);
                entry.setSecondsCreated(reader.nextInteger());
                entry.setNanosecondsCreated(reader.nextInteger());
                entry.setSecondsModified((reader.nextInteger()));
                entry.setNanosecondsModified(reader.nextInteger());
                entry.setDev(reader.nextInteger());
                entry.setIno(reader.nextInteger());
                entry.setMode(Integer.toOctalString(reader.nextInteger()));
                entry.setUid(reader.nextInteger());
                entry.setGid(reader.nextInteger());
                entry.setSize(reader.nextInteger());
                entry.setSha1(reader.nextHexString(20));
                entry.setFlags(reader.nextShort());
                entry.setAssumeValid(isMask(entry.getFlags(), 0x8000));
                entry.setExtended(isMask(entry.getFlags(), 0x4000));

                boolean stage1 = isMask(entry.getFlags(), 0x2000);
                boolean stage2 = isMask(entry.getFlags(), 0x1000);
                entry.setStage(new Boolean[] { stage1, stage2 });

                int entryLen = 62;

                if (entry.getExtended() && version == 3) {
                    entry.setExtraFlags(reader.nextShort());
                    entry.setReserved(isMask(entry.getExtraFlags(), 0x8000));
                    entry.setSkipWorktree(isMask(entry.getExtraFlags(), 0x4000));
                    entry.setIntentToAdd(isMask(entry.getExtraFlags(), 0x2000));
                    entryLen += 2;
                }

                int nameLen = entry.getFlags() & 0xFFF;
                entry.setName(nameLen == 0xFFF ? reader.nextString() : reader.nextString(nameLen));
                entryLen += entry.getName().length();
                int padlen = 8 - (entryLen % 8);
                reader.checkAndSkipPads((byte) 0, padlen > 0 ? padlen : 8);

                result.add(entry);
            }

            indexEntries = result;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * 掩码判断
     * 
     * @param value
     * @param code
     * @return
     */
    private Boolean isMask(int value, int code) {
        return (value & code) != 0;
    }

    private class IndexReader {
        private final byte[] data;
        private int          offset;

        public IndexReader(String indexPath) throws IOException {
            File file = new File(indexPath);
            data = FileUtils.readFileToByteArray(file);
            offset = 0;
        }

        public String nextString() {
            int start = offset;
            while (true) {
                if (offset + 1 > data.length) {
                    throw new RuntimeException("nextString failed, offset: " + offset);
                }
                if (data[offset++] == 0) {
                    break;
                }
            }
            return new String(data, start, offset - start);
        }

        public String nextString(int len) {
            if (offset + len > data.length) {
                throw new RuntimeException(String.format("nextString failed, offset: %d, len: %d",
                    offset, len));
            }

            String result = new String(data, offset, len);
            offset += len;
            return result;
        }

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

        public Integer nextInteger() {
            if (offset + 4 > data.length) {
                throw new RuntimeException("nextInteger failed, offset: " + offset);
            }

            int result = 0;

            result |= (data[offset + 0] & 0xFF) << 24;
            result |= (data[offset + 1] & 0xFF) << 16;
            result |= (data[offset + 2] & 0xFF) << 8;
            result |= (data[offset + 3] & 0xFF);

            offset += 4;

            return result;
        }

        public Short nextShort() {
            if (offset + 2 > data.length) {
                throw new RuntimeException("nextShort failed, offset: " + offset);
            }

            int result = 0;

            result |= (data[offset + 0] & 0xFF) << 8;
            result |= (data[offset + 1] & 0xFF);

            offset += 2;

            return (short) result;
        }

        public void checkAndSkipPads(byte expected, int len) {
            if (offset + len > data.length) {
                throw new RuntimeException(String.format(
                    "checkAndSkipPads failed, offset: %d, len: %d", offset, len));
            }

            for (int index = 0; index < len; index++) {
                if (data[offset] != expected) {
                    throw new RuntimeException(String.format(
                        "Invalid Pad, actual: %s, expected: %s", data[offset], expected));
                }
                offset++;
            }
        }
    }
}
