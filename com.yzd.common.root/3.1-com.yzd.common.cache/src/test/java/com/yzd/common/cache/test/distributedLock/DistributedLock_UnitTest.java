package com.yzd.common.cache.test.distributedLock;

import com.yzd.common.cache.redis.sharded.ShardedRedisUtil;
import org.junit.Test;

import java.util.UUID;

/**
 * @author zd.yao
 * @description
 * @date 2019/8/21
 **/

public class DistributedLock_UnitTest {

    private final String key="m:1";
    //private final String requestId=UUID.randomUUID().toString();
    private final String requestId="6a129fbf-0a64-4853-b7d5-51e76002154a";
    /**
     * 获得分布式锁
     */
    @Test
    public void acquire_lock(){
        int expireSec=100;
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        boolean isOk=redisUtil.acquire4DistributedLock(key,requestId,expireSec);
        System.out.println(isOk);
    }

    /**
     * 释放分布式锁
     */
    @Test
    public void release_lock(){
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        boolean isOk=redisUtil.release4DistributedLock(key,requestId);
        System.out.println(isOk);
    }
}
