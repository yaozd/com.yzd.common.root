package com.yzd.common.cache.utils.lockExt;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.date.DateUnit;

public class KeyLockUtil {
    private static KeyLockUtil ourInstance = new KeyLockUtil();

    public static KeyLockUtil getInstance() {
        return ourInstance;
    }
    //lruCache的容量为1000，性能为最佳。-例如：1百万的循环访问，大约5秒。相当于20万/S；
    Cache<String, ReaderToken> lruCache;
    private KeyLockUtil() {
        lruCache = CacheUtil.newLRUCache(1000);
    }
    public ReaderToken getReaderToken(String key){
        if(key==null){
            throw new IllegalStateException("key is Null");
        }
        ReaderToken readerToken=lruCache.get(key);
        if(readerToken!=null){
            return readerToken;
        }
        return getNewReaderToken(key);
    }

    private synchronized ReaderToken getNewReaderToken(String key) {
        ReaderToken readerToken=lruCache.get(key);
        if(readerToken!=null){
            return readerToken;
        }
        //阻塞KEY的锁的过期时间为10秒。
        lruCache.put(key,new ReaderToken(), DateUnit.SECOND.getMillis() * 10);
        return lruCache.get(key);
    }
    public void remove(String key){
        lruCache.remove(key);
    }
}
