package com.yzd.common.cache.test.sharded;


import com.yzd.common.cache.redis.sharded.ShardedRedisUtil;
import com.yzd.common.cache.test.cache.CacheKeyList;
import com.yzd.common.cache.utils.wrapper.CachedWrapper;
import com.yzd.common.cache.utils.wrapper.CachedWrapperExecutor;
import org.junit.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by zd.yao on 2017/3/17.
 */
public class ShardedRedisUtil_UnitTest {
    @Test
    public void setExample() {
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        String result = redisUtil.set("ShardedRedisUtilUnitTest", "2017031701");
        assertThat(result).isEqualTo("OK");
    }
    @Test
    public void setExampleWithCacheKey() {
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        //P01.HelloWorld:b0baee9d279d34fa1dfd71aadb908c3f
        CachedWrapper<String> result = redisUtil.getPublicCachedWrapperByMutexKey(CacheKeyList.HelloWorld, "1112121",()->{
            return "cache data";
        });
        System.out.println(result.getData());
        assertThat(result).isNotNull();
    }
    @Test
    public void getValueByTimestamp() throws Exception {
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        int loginId = 1000;
        String keyTimestampPerson = "keyTimestamp_person_" + String.valueOf(loginId);
        //keyTimestampPerson=keyTimestamp_person_1000
        //输出结果： "timestamp": "2017-01-18 02:44:41|212cb6a7-5eb7-4b2e-995b-405aa0dcf9ad"
        CachedWrapper<String> wrapperValue_keyTimestamp = redisUtil.getCachedWrapperByMutexKey(keyTimestampPerson, 60 * 60 * 24, 5, 3,
                new CachedWrapperExecutor<String>() {
                    @Override
                    public String execute() {
                        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss|");
                        String dateFormat = format1.format(new Date());
                        String uuid = UUID.randomUUID().toString();
                        String timestampSetVal = dateFormat + uuid;
                        return timestampSetVal;
                    }
                });
        String timestamp = wrapperValue_keyTimestamp.getData();
        CachedWrapper<String> wrapperValue_Timestamp = redisUtil.getCachedWrapperByTimestamp("value-timestamp", 1000, 500, timestamp,
                new CachedWrapperExecutor<String>() {
                    @Override
                    public String execute() {
                        return "目前考虑的使用场景-缓存个人用户的全局信息" +
                                "-但需要设计合理的个人用户信息更新机制" +
                                "-缓存数据周期长--例如一天";
                    }
                });
    }
    @Test
    public void addExpireForSet(){
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        redisUtil.sadd("set.test","1");
        redisUtil.expire("set.test",100);
    }
}
