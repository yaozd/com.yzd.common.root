package com.yzd.common.cache.utils.setting;

/**
 * TVCB=timestamp+version+cached+data
 * 时间版本缓存数据-缓存配置信息
 */

public class CachedSettingForTVCB {
    /**
     * 项目编号
     * */
    private String projectNo;
    /**
     * 缓存KEY
     * */
    private String keyName;
    /**
     * 过期时间->缓存KEY
     * */
    private int keyExpireSec;
    /**
     * 过期时间->空值
     * */
    private int keyExpireSecForNullValue;
    /**
     * 过期时间->互斥KEY
     * */
    private int keyExpireSecForMutexKey;
    /**
     * 休眠时间->互斥KEY
     * */
    private int sleepMillisecondsForMutexKey;
    /**
     * 时间版本缓存数据key的名称
     */
    private String keyNameForTimestamp;
    /**
     * 水平扩展缓存副本的数量->副本的数量默认原来0
     */
    private int countForCopyData=0;
    /**
     * 描述信息
     */
    private String desc;
    /**
     * 当前缓存数据的数据结构版本
     */
    private String version;

    public String getProjectNo() {
        return projectNo;
    }

    public CachedSettingForTVCB setProjectNo(String projectNo) {
        this.projectNo = projectNo;
        return this;
    }

    public String getKeyName() {
        return keyName;
    }

    public CachedSettingForTVCB setKeyName(String keyName) {
        this.keyName = keyName;
        return this;
    }

    public int getKeyExpireSec() {
        return keyExpireSec;
    }

    public CachedSettingForTVCB setKeyExpireSec(int keyExpireSec) {
        this.keyExpireSec = keyExpireSec;
        return this;
    }

    public int getKeyExpireSecForNullValue() {
        return keyExpireSecForNullValue;
    }

    public CachedSettingForTVCB setKeyExpireSecForNullValue(int keyExpireSecForNullValue) {
        this.keyExpireSecForNullValue = keyExpireSecForNullValue;
        return this;
    }

    public int getKeyExpireSecForMutexKey() {
        return keyExpireSecForMutexKey;
    }

    public CachedSettingForTVCB setKeyExpireSecForMutexKey(int keyExpireSecForMutexKey) {
        this.keyExpireSecForMutexKey = keyExpireSecForMutexKey;
        return this;
    }

    public int getSleepMillisecondsForMutexKey() {
        return sleepMillisecondsForMutexKey;
    }

    public CachedSettingForTVCB setSleepMillisecondsForMutexKey(int sleepMillisecondsForMutexKey) {
        this.sleepMillisecondsForMutexKey = sleepMillisecondsForMutexKey;
        return this;
    }

    public String getKeyNameForTimestamp() {
        return keyNameForTimestamp;
    }

    public CachedSettingForTVCB setKeyNameForTimestamp(String keyNameForTimestamp) {
        this.keyNameForTimestamp = keyNameForTimestamp;
        return this;
    }

    public int getCountForCopyData() {
        return countForCopyData;
    }

    public CachedSettingForTVCB setCountForCopyData(int countForCopyData) {
        this.countForCopyData = countForCopyData;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public CachedSettingForTVCB setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getVersion() {
        return version;
    }

    public CachedSettingForTVCB setVersion(String version) {
        this.version = version;
        return this;
    }
}
