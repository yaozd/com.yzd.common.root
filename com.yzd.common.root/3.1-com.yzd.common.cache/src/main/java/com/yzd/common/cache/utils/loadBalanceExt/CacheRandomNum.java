package com.yzd.common.cache.utils.loadBalanceExt;

/**
 * 获得某个公共缓存KEY的随机编号--主要用于公共缓存热点数据的水平扩展N倍
 * */
public class CacheRandomNum {
    /**
     * 获得某个公共缓存KEY的随机编号
     * @param keyNameForTimestamp 时间版本缓存数据key的名称
     * @param countForCopyData 水平扩展缓存副本的数量
     * @return
     */
    public static Long getRandomNum(String keyNameForTimestamp,int countForCopyData){
        if(countForCopyData==0){
            return 0L;
        }
        Long value=CacheRoundRobin.getInstance().getValue(keyNameForTimestamp);
        return value%countForCopyData;
    }
}
