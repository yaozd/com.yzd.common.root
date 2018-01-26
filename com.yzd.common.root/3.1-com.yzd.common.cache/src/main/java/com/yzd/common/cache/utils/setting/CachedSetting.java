package com.yzd.common.cache.utils.setting;


/**
 * Created by Administrator on 2017/1/17.
 */
public class CachedSetting {
    public CachedSetting(String projectNo,String key,int keyExpireSec,String desc){
        this.setProjectNo(projectNo);
        this.setKey(key);
        this.setKeyExpireSec(keyExpireSec);
        this.setDesc(desc);
    }
    public CachedSetting(String projectNo,String key,int keyExpireSec,int nullValueExpireSec,int keyMutexExpireSec,String desc){
        this.setProjectNo(projectNo);
        this.setKey(key);
        this.setKeyExpireSec(keyExpireSec);
        this.setNullValueExpireSec(nullValueExpireSec);
        this.setKeyMutexExpireSec(keyMutexExpireSec);
        this.setDesc(desc);
    }
    public CachedSetting(String projectNo,String key,int keyExpireSec,int nullValueExpireSec,int keyMutexExpireSec,int sleepMilliseconds,String desc){
        this.setProjectNo(projectNo);
        this.setKey(key);
        this.setKeyExpireSec(keyExpireSec);
        this.setNullValueExpireSec(nullValueExpireSec);
        this.setKeyMutexExpireSec(keyMutexExpireSec);
        this.setDesc(desc);
        this.setSleepMilliseconds(sleepMilliseconds);
    }
    public CachedSetting(String projectNo,String key,int keyExpireSec,int nullValueExpireSec,int keyMutexExpireSec,int sleepMilliseconds,String version,String desc){
        this.setProjectNo(projectNo);
        this.setKey(key);
        this.setKeyExpireSec(keyExpireSec);
        this.setNullValueExpireSec(nullValueExpireSec);
        this.setKeyMutexExpireSec(keyMutexExpireSec);
        this.setDesc(desc);
        this.setSleepMilliseconds(sleepMilliseconds);
        this.setVersion(version);
    }
    private String projectNo;
    private String key;
    private int keyExpireSec;
    private int nullValueExpireSec;
    private int keyMutexExpireSec;
    private int sleepMilliseconds;
    private String desc;
    private String version;
    private String keyFullName;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getKeyExpireSec() {
        return keyExpireSec;
    }

    public void setKeyExpireSec(int keyExpireSec) {
        this.keyExpireSec = keyExpireSec;
    }

    public int getNullValueExpireSec() {
        return nullValueExpireSec;
    }

    public void setNullValueExpireSec(int nullValueExpireSec) {
        this.nullValueExpireSec = nullValueExpireSec;
    }

    public int getKeyMutexExpireSec() {
        return keyMutexExpireSec;
    }

    public void setKeyMutexExpireSec(int keyMutexExpireSec) {
        this.keyMutexExpireSec = keyMutexExpireSec;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getSleepMilliseconds() {
        return sleepMilliseconds;
    }

    public void setSleepMilliseconds(int sleepMilliseconds) {
        this.sleepMilliseconds = sleepMilliseconds;
    }

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    public String getKeyFullName() {
        return this.projectNo+"."+this.key+":";
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
