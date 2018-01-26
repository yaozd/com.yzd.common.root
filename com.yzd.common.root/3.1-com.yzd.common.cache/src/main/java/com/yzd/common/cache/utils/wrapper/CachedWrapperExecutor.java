package com.yzd.common.cache.utils.wrapper;

/**
 * Created by Administrator on 2017/1/17.
 */
// 读取缓存数据在具体逻辑接口
public interface CachedWrapperExecutor<T> {
    T execute();
}
