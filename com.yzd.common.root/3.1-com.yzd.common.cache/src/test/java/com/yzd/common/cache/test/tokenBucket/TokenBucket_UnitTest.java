package com.yzd.common.cache.test.tokenBucket;

import com.yzd.common.cache.redis.sharded.ShardedRedisUtil;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author zd.yao
 * @description
 * @date 2019/8/20
 **/

public class TokenBucket_UnitTest {

    private final String key="orderid:1";

    /**
     * 从令牌桶中获取令牌
     */
    @Test
    public void acquire_token(){
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        boolean isOk=redisUtil.acquire4TokenBucket(key,"3","100");
        System.out.println(isOk);
    }

    /**
     * 从令牌桶中释放令牌
     */
    @Test
    public void release_token(){
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        boolean isOk=redisUtil.release4TokenBucket(key);
        System.out.println(isOk);
    }
    /**
     * 令牌桶使用测试
     * -产品控流--50处理等待请求计数|200访问等待请求计数
     * -简易令牌桶算法
     * -每个产品一个令牌桶
     */
    @Test
    public void use_token() throws InterruptedException {
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        //
        int getTimeoutLimiter = 20;
        int getTimeoutNum = 0;
        try {
            while (true) {
                if (getTimeoutNum > getTimeoutLimiter) {
                    throw new IllegalArgumentException("超过获取等待超时时间20秒");
                }
                if (!redisUtil.acquire4TokenBucket(key,"3","100")) {
                    getTimeoutNum=getTimeoutNum+1;
                    TimeUnit.SECONDS.sleep(1);
                    continue;
                }
                //调试使用：模拟业务处理的时间。
                TimeUnit.SECONDS.sleep(10);
                //逻辑操作处理程序
                System.out.println("逻辑操作处理程序");
                return; //中断循环
            }
        } catch (InterruptedException e) {
            throw e;
        } finally {
            redisUtil.release4TokenBucket(key);
        }
    }
}
