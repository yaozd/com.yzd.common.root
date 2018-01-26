package com.yzd.common.cache.utils.wrapper;

/**
 * Created by Administrator on 2017/1/17.
 */
public class CachedWrapper<T>  {
    public CachedWrapper() {}
    public CachedWrapper(T data)
    {
        this.setData(data);
    }
    public CachedWrapper(T data,String timestamp)
    {
        this.setData(data);
        this.setTimestamp(timestamp);
    }

    /**
     * 缓存数据
     */
    private T data;
    /**
     * 缓存数据-数据对比时间戳
     */
    private String timestamp;

    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
