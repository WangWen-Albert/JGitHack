/**
 * JHack.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.jgithack.model;

/**
 * IndexEntry－.git目录下index文件的基本组成元素
 * 
 * @author Albert Wang
 * @version $Id: IndexEntry.java, V0.1 2016-10-29 20:01:00, jawangwen@qq.com $
 */
public class IndexEntry {
    private Integer   id;
    private Integer   secondsCreated;
    private Integer   nanosecondsCreated;
    private Integer   secondsModified;
    private Integer   nanosecondsModified;
    private Integer   dev;
    private Integer   ino;
    private String    mode;
    private Integer   uid;
    private Integer   gid;
    private Integer   size;
    private String    sha1;
    private Short     flags;
    private Boolean   assumeValid;
    private Boolean   extended;
    private Boolean[] stage;
    private Short     extraFlags;
    private Boolean   reserved;
    private Boolean   skipWorktree;
    private Boolean   intentToAdd;
    private String    name;

    /**
     * Getter method for property <tt>id</tt>.
     * 
     * @return property value of id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Setter method for property <tt>id</tt>.
     * 
     * @param id value to be assigned to property id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Getter method for property <tt>secondsCreated</tt>.
     * 
     * @return property value of secondsCreated
     */
    public Integer getSecondsCreated() {
        return secondsCreated;
    }

    /**
     * Setter method for property <tt>secondsCreated</tt>.
     * 
     * @param secondsCreated value to be assigned to property secondsCreated
     */
    public void setSecondsCreated(Integer secondsCreated) {
        this.secondsCreated = secondsCreated;
    }

    /**
     * Getter method for property <tt>nanosecondsCreated</tt>.
     * 
     * @return property value of nanosecondsCreated
     */
    public Integer getNanosecondsCreated() {
        return nanosecondsCreated;
    }

    /**
     * Setter method for property <tt>nanosecondsCreated</tt>.
     * 
     * @param nanosecondsCreated value to be assigned to property nanosecondsCreated
     */
    public void setNanosecondsCreated(Integer nanosecondsCreated) {
        this.nanosecondsCreated = nanosecondsCreated;
    }

    /**
     * Getter method for property <tt>secondsModified</tt>.
     * 
     * @return property value of secondsModified
     */
    public Integer getSecondsModified() {
        return secondsModified;
    }

    /**
     * Setter method for property <tt>secondsModified</tt>.
     * 
     * @param secondsModified value to be assigned to property secondsModified
     */
    public void setSecondsModified(Integer secondsModified) {
        this.secondsModified = secondsModified;
    }

    /**
     * Getter method for property <tt>nanosecondsModified</tt>.
     * 
     * @return property value of nanosecondsModified
     */
    public Integer getNanosecondsModified() {
        return nanosecondsModified;
    }

    /**
     * Setter method for property <tt>nanosecondsModified</tt>.
     * 
     * @param nanosecondsModified value to be assigned to property nanosecondsModified
     */
    public void setNanosecondsModified(Integer nanosecondsModified) {
        this.nanosecondsModified = nanosecondsModified;
    }

    /**
     * Getter method for property <tt>dev</tt>.
     * 
     * @return property value of dev
     */
    public Integer getDev() {
        return dev;
    }

    /**
     * Setter method for property <tt>dev</tt>.
     * 
     * @param dev value to be assigned to property dev
     */
    public void setDev(Integer dev) {
        this.dev = dev;
    }

    /**
     * Getter method for property <tt>ino</tt>.
     * 
     * @return property value of ino
     */
    public Integer getIno() {
        return ino;
    }

    /**
     * Setter method for property <tt>ino</tt>.
     * 
     * @param ino value to be assigned to property ino
     */
    public void setIno(Integer ino) {
        this.ino = ino;
    }

    /**
     * Getter method for property <tt>mode</tt>.
     * 
     * @return property value of mode
     */
    public String getMode() {
        return mode;
    }

    /**
     * Setter method for property <tt>mode</tt>.
     * 
     * @param mode value to be assigned to property mode
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Getter method for property <tt>uid</tt>.
     * 
     * @return property value of uid
     */
    public Integer getUid() {
        return uid;
    }

    /**
     * Setter method for property <tt>uid</tt>.
     * 
     * @param uid value to be assigned to property uid
     */
    public void setUid(Integer uid) {
        this.uid = uid;
    }

    /**
     * Getter method for property <tt>gid</tt>.
     * 
     * @return property value of gid
     */
    public Integer getGid() {
        return gid;
    }

    /**
     * Setter method for property <tt>gid</tt>.
     * 
     * @param gid value to be assigned to property gid
     */
    public void setGid(Integer gid) {
        this.gid = gid;
    }

    /**
     * Getter method for property <tt>size</tt>.
     * 
     * @return property value of size
     */
    public Integer getSize() {
        return size;
    }

    /**
     * Setter method for property <tt>size</tt>.
     * 
     * @param size value to be assigned to property size
     */
    public void setSize(Integer size) {
        this.size = size;
    }

    /**
     * Getter method for property <tt>sha1</tt>.
     * 
     * @return property value of sha1
     */
    public String getSha1() {
        return sha1;
    }

    /**
     * Setter method for property <tt>sha1</tt>.
     * 
     * @param sha1 value to be assigned to property sha1
     */
    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    /**
     * Getter method for property <tt>flags</tt>.
     * 
     * @return property value of flags
     */
    public Short getFlags() {
        return flags;
    }

    /**
     * Setter method for property <tt>flags</tt>.
     * 
     * @param flags value to be assigned to property flags
     */
    public void setFlags(Short flags) {
        this.flags = flags;
    }

    /**
     * Getter method for property <tt>assumeValid</tt>.
     * 
     * @return property value of assumeValid
     */
    public Boolean getAssumeValid() {
        return assumeValid;
    }

    /**
     * Setter method for property <tt>assumeValid</tt>.
     * 
     * @param assumeValid value to be assigned to property assumeValid
     */
    public void setAssumeValid(Boolean assumeValid) {
        this.assumeValid = assumeValid;
    }

    /**
     * Getter method for property <tt>extended</tt>.
     * 
     * @return property value of extended
     */
    public Boolean getExtended() {
        return extended;
    }

    /**
     * Setter method for property <tt>extended</tt>.
     * 
     * @param extended value to be assigned to property extended
     */
    public void setExtended(Boolean extended) {
        this.extended = extended;
    }

    /**
     * Getter method for property <tt>stage</tt>.
     * 
     * @return property value of stage
     */
    public Boolean[] getStage() {
        return stage;
    }

    /**
     * Setter method for property <tt>stage</tt>.
     * 
     * @param stage value to be assigned to property stage
     */
    public void setStage(Boolean[] stage) {
        this.stage = stage;
    }

    /**
     * Getter method for property <tt>extraFlags</tt>.
     * 
     * @return property value of extraFlags
     */
    public Short getExtraFlags() {
        return extraFlags;
    }

    /**
     * Setter method for property <tt>extraFlags</tt>.
     * 
     * @param extraFlags value to be assigned to property extraFlags
     */
    public void setExtraFlags(Short extraFlags) {
        this.extraFlags = extraFlags;
    }

    /**
     * Getter method for property <tt>reserved</tt>.
     * 
     * @return property value of reserved
     */
    public Boolean getReserved() {
        return reserved;
    }

    /**
     * Setter method for property <tt>reserved</tt>.
     * 
     * @param reserved value to be assigned to property reserved
     */
    public void setReserved(Boolean reserved) {
        this.reserved = reserved;
    }

    /**
     * Getter method for property <tt>skipWorktree</tt>.
     * 
     * @return property value of skipWorktree
     */
    public Boolean getSkipWorktree() {
        return skipWorktree;
    }

    /**
     * Setter method for property <tt>skipWorktree</tt>.
     * 
     * @param skipWorktree value to be assigned to property skipWorktree
     */
    public void setSkipWorktree(Boolean skipWorktree) {
        this.skipWorktree = skipWorktree;
    }

    /**
     * Getter method for property <tt>intentToAdd</tt>.
     * 
     * @return property value of intentToAdd
     */
    public Boolean getIntentToAdd() {
        return intentToAdd;
    }

    /**
     * Setter method for property <tt>intentToAdd</tt>.
     * 
     * @param intentToAdd value to be assigned to property intentToAdd
     */
    public void setIntentToAdd(Boolean intentToAdd) {
        this.intentToAdd = intentToAdd;
    }

    /**
     * Getter method for property <tt>name</tt>.
     * 
     * @return property value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name</tt>.
     * 
     * @param name value to be assigned to property name
     */
    public void setName(String name) {
        this.name = name;
    }
}
