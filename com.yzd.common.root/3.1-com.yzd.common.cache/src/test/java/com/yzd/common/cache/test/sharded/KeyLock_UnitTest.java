package com.yzd.common.cache.test.sharded;

import cn.hutool.core.util.RandomUtil;
import com.yzd.common.cache.redis.sharded.ShardedRedisUtil;
import com.yzd.common.cache.utils.wrapper.CachedWrapper;

public class KeyLock_UnitTest {
    /***
     * 用于VisualVM-GC解决OutOfMemoryError: unable to create new native thread
     * 原因是创建10万的线程就会出现，目前测试程序创建的线程个数为5000个，并没有出现此问题
     * Exception in thread "main" java.lang.OutOfMemoryError: unable to create new native thread
     * 参考：
     * unable to create new native thread
     * http://www.cnblogs.com/metoy/p/6955608.html
     * @param args
     * @throws InterruptedException
     */
    public static void main(String args[]) throws InterruptedException {
        String key="P01.KeyLock_Thread:b0baee9d279d34fa1dfd71aadb908c3f";
        for (Integer i = 0; i < 5000; i++) {

            new Thread() {
                public void run() {
                    for (int j = 0; j <10000000 ; j++) {
                        String newKey=key+j+ RandomUtil.randomNumbers(3);
                        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
                        CachedWrapper<String> result = redisUtil.getCachedWrapperByMutexKeyAndKeyLock(newKey, 60 * 60 * 24, 5, 3,300,()->{
                            return "cache data=getCachedWrapperByMutexKeyAndKeyLock_Thread";
                        });
                    }

                };
            }.start();
        }
        Thread.sleep(150000);
    }
}
