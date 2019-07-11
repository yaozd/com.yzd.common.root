package com.yzd.common.cache.test.sharded;

import com.yzd.common.cache.redis.sharded.ShardedRedisUtil;
import com.yzd.common.cache.utils.fastjson.FastJsonUtil;
import com.yzd.common.cache.utils.wrapper.CachedWrapper;
import com.yzd.common.cache.utils.wrapper.CachedWrapperExecutor;
import org.junit.Test;

public class PublicCached_UnitTest
{
    //通过水平扩展的缓存幅本的用于解决公共缓存数据的热点问题--下面是模拟的操作逻辑
    //解决公共缓存数据的热点问题
    //STEP-01
    @Test
    public void publicCache_Test(){
        String key="P01.20180321:1deywjfe8jr41";
        Long number=1L;
        //
        String dataInRedis=getDataInRedis(key, number);
        System.out.println(dataInRedis);
        //
        String result=FastJsonUtil.serialize(dataInRedis);
        System.out.println(result);
        //
        FastJsonUtil.deserialize(result,String.class);
        //dataInRedis是非标准Json格式，所以会报异常，（!=0）转化为（"!=0"）
        FastJsonUtil.deserialize(dataInRedis,String.class);
    }
    //STEP-02
    private String getDataInRedis(String key, Long number) {
        // number==0
        String dataInRedis;
        if(number==0){
            String keyWithNumber=key+"."+number.toString();
            return "0";
        }
        // number!=0的处理情况[STEP03]
        return "!=0";
    }

    //STEP-03
    //下面是number!=0的处理情况[STEP03]
    @Test
    public void CacheRoundRobin_Test(){
        String key="P01.20180321:1deywjfe8jr41";
        Long number=1L;
        String keyWithNumber=key+"."+number.toString();
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        String dataInRedis=redisUtil.get(keyWithNumber);
        if(dataInRedis!=null){
            System.out.println("step-01:当前数据存在");
            return;
        }
        System.out.println("step-02:当前数据不存在缓存中");
        String keyWithZero=key+".0";
        //下面是轮训编号=Zero的节点的数据读取
        CachedWrapper<String> dataInRedisByZero=redisUtil.getCachedWrapperByMutexKey(keyWithZero,
                60, 3, 3, 300, new CachedWrapperExecutor<String>() {
                    @Override
                    public String execute() {
                        return "20180321-01";
                    }
                });
        //所有的子节的缓存过期时间要依赖于父节点（轮训编号=Zero的节点）的时间
        Long t1=redisUtil.Ttl(keyWithZero);
        t1=t1<1?1:t1;
        String result=dataInRedisByZero.getData();
        redisUtil.setex(keyWithNumber,t1.intValue(),result);
        //return; result;
    }
}
