package com.yzd.common.cache.test.sharded;

import cn.hutool.core.util.RandomUtil;
import com.yzd.common.cache.redis.sharded.ShardedRedisUtil;
import com.yzd.common.cache.test.cache.CacheKeyList;
import com.yzd.common.cache.utils.lockExt.KeyLockUtil;
import com.yzd.common.cache.utils.lockExt.ReaderToken;
import com.yzd.common.cache.utils.wrapper.CachedWrapper;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CachedDataByMutexKeyAndKeyLock_UnitTest {
    @Test
    public void KeyLock_Test(){
        new Thread() {
            public void run() {
                ReaderToken t= KeyLockUtil.getInstance().getReaderToken("ke");
                System.out.println(t.getAccessCount().getAndIncrement());
            };
        }.start();
        new Thread() {
            public void run() {
                ReaderToken t= KeyLockUtil.getInstance().getReaderToken("ke");
                System.out.println(t.getAccessCount().getAndIncrement());
            };
        }.start();
        ReaderToken t= KeyLockUtil.getInstance().getReaderToken("ke");
        System.out.println(t.getAccessCount().getAndIncrement());
        System.out.println(t.getAccessCount().getAndIncrement());
    }
    @Test
    public void getCachedWrapperByMutexKeyAndKeyLock_Test(){
        String key="P01.KeyLock_Test:b0baee9d279d34fa1dfd71aadb908c3f";
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        CachedWrapper<String> result = redisUtil.getCachedWrapperByMutexKeyAndKeyLock(key, 60 * 60 * 24, 5, 3,300,()->{
            try {
                Thread.sleep(50000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "cache data=getCachedWrapperByMutexKeyAndKeyLock_Test";
        });
    }

    @Test
    public void getCachedWrapperByMutexKeyAndKeyLock_Thread() throws InterruptedException {
        String key="P01.KeyLock_Thread:b0baee9d279d34fa1dfd71aadb908c3f";
        new Thread() {
            public void run() {
                ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
                CachedWrapper<String> result = redisUtil.getCachedWrapperByMutexKeyAndKeyLock(key, 60 * 60 * 24, 5, 3,300,()->{
                    try {
                        Thread.sleep(50000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "cache data=getCachedWrapperByMutexKeyAndKeyLock_Thread";
                });
            };
        }.start();
        Thread.sleep(1000);
        for (Integer i = 0; i < 1000; i++) {
            new Thread() {
                public void run() {
                    ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
                    CachedWrapper<String> result = redisUtil.getCachedWrapperByMutexKeyAndKeyLock(key, 60 * 60 * 24, 5, 3,300,()->{
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return "cache data=getCachedWrapperByMutexKeyAndKeyLock_Thread";
                    });
                };
            }.start();
        }

        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        CachedWrapper<String> result = redisUtil.getCachedWrapperByMutexKeyAndKeyLock(key, 60 * 60 * 24, 5, 3,300,()->{
            try {
                Thread.sleep(50000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "cache data=getCachedWrapperByMutexKeyAndKeyLock_Thread";
        });
        System.out.println(result.getData());
        assertThat(result).isNotNull();
    }
    @Test
    public void getCachedWrapperByMutexKeyAndKeyLock_Thread_Mutil_Key() throws InterruptedException {
        String key="P01.KeyLock_Thread:b0baee9d279d34fa1dfd71aadb908c3f";
        for (Integer i = 0; i < 100000; i++) {
            Integer t=i;
            new Thread() {
                public void run() {
                    ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
                    CachedWrapper<String> result = redisUtil.getCachedWrapperByMutexKeyAndKeyLock(key+t, 60 * 60 * 24, 5, 3,300,()->{
//                        try {
//                            Thread.sleep(5000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        return "cache data=getCachedWrapperByMutexKeyAndKeyLock_Thread";
                    });
                };
            }.start();
        }
        Thread.sleep(15000);
    }
    @Test
    public void getCachedWrapperByMutexKeyAndKeyLock_Thread_Mutil_Key1() throws InterruptedException {
        String key="P01.KeyLock_Thread:b0baee9d279d34fa1dfd71aadb908c3f";
        for (Integer i = 0; i < 100000; i++) {
            Integer t=i;
            new Thread() {
                public void run() {

                };
            }.start();
        }
        Thread.sleep(15000);
    }
    @Test
    public void getCachedWrapperByMutexKeyAndKeyLock_Thread_Mutil_Key2() throws InterruptedException {
        String key="P01.KeyLock_Thread:b0baee9d279d34fa1dfd71aadb908c3f";
        for (Integer i = 0; i < 1000; i++) {

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
    @Test
    public void getCachedWrapperByMutexKeyAndKeyLock_Thread_Mutil_Key3() throws InterruptedException {
        String key="P01.KeyLock_Thread:b0baee9d279d34fa1dfd71aadb908c3f";
        for (Integer i = 0; i < 100000; i++) {
            Integer t=i;
            new Thread() {
                public void run() {
                    ReaderToken t= KeyLockUtil.getInstance().getReaderToken(key);
                };
            }.start();
        }
        Thread.sleep(15000);
    }
    @Test
    public void Thread_Mutil_Key() throws InterruptedException {
        String key="P01.KeyLock_Thread:b0baee9d279d34fa1dfd71aadb908c3f";
        for (Integer i = 0; i < 100000; i++) {
            Integer t=i;
            new Thread() {
                public void run() {
                    System.out.println(key+t);
                    ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
                    redisUtil.set(key+t,"11");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                };
            }.start();
        }
        Thread.sleep(15000);
    }
}
