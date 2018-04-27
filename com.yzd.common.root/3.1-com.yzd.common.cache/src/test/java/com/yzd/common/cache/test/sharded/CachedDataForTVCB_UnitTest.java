package com.yzd.common.cache.test.sharded;

import com.yzd.common.cache.redis.sharded.ShardedRedisUtil;
import com.yzd.common.cache.utils.fastjson.FastJsonUtil;
import com.yzd.common.cache.utils.setting.CachedSetting;
import com.yzd.common.cache.utils.setting.CachedSettingForTVCB;
import com.yzd.common.cache.utils.wrapper.CachedWrapper;
import com.yzd.common.cache.utils.wrapper.CachedWrapperExecutor;
import org.junit.Test;

/**
 * 通过水平扩展的缓存幅本的用于解决公共缓存数据的热点问题
 * TVCB=timestamp+version+cached+data
 * 时间版本缓存数据-缓存配置信息
 */
public class CachedDataForTVCB_UnitTest {
    @Test
    public void getCachedDataForTVCB_Test() {
        //step-01   初始信息
        String keyNameForTimestamp = "P01.Timestamp:publicNormal";//时间戳版本key的名称
        String keyNameForExpireAllKeySet = "P01.ExpireAllKeySet";//保证所有的SaveAllKeySet都设置了过期时间
        String keyNameForSaveAllKeySet = "P01.SaveAllKeySet:";//保存资源时间戳版本对应的所有缓存
        int timeoutForPublicKey=1000;//过期时间
        String where = null;
        //step-02   获得当前缓存数据的时间戳版本
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        CachedWrapper<String> wrapperValue_keyTimestamp = redisUtil.getTimestampKey(keyNameForTimestamp,
                timeoutForPublicKey,
                5,
                3,
                300,
                keyNameForExpireAllKeySet,
                keyNameForSaveAllKeySet,
                timeoutForPublicKey,
                new CachedWrapperExecutor<String>() {
                    @Override
                    public String execute() {
                        //通过twitter的snowflake算法解决数据时间戳重复问题
                        return "123456" ;
                    }
                });
        String keyTimestamp=wrapperValue_keyTimestamp.getData();
        //step-03 通过时间戳版本+缓存数据的副本的数量->设置缓存信息
        CachedSettingForTVCB cachedSettingForData = new CachedSettingForTVCB();
        cachedSettingForData
                .setProjectNo("P01")
                .setKeyName("allUserInfos")
                .setKeyExpireSec(1000)
                .setKeyExpireSecForNullValue(3)
                .setKeyExpireSecForMutexKey(3)
                .setSleepMillisecondsForMutexKey(300)
                .setKeyNameForTimestamp("P01.Timestamp:publicNormal")
                .setCountForCopyData(10)
                .setVersion("1.0")
                .setDesc("所有用户的信息");
        String keyNameForSaveAllKeySetWithTimestamp=keyNameForSaveAllKeySet+keyTimestamp;
        String result = redisUtil.getCachedDataForTVCB(cachedSettingForData, where, keyNameForTimestamp, keyNameForSaveAllKeySetWithTimestamp
                , new CachedWrapperExecutor<String>() {
                    @Override
                    public String execute() {
                        return "缓存数据为json格式";
                    }
                });
        System.out.println(result);
        //step-04   json数据反序列化
        FastJsonUtil.deserialize(result,String.class);
    }
}
