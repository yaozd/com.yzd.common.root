package com.yzd.common.mq.redis.job.lock;

import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;
import org.apache.commons.lang.ObjectUtils;

import java.util.concurrent.CountDownLatch;

/**
 * Created by zd.yao on 2017/8/28.
 */
public class RedisJobLockUtil {
    //对当前执行的任务进行加锁
    public static void lockTask(String key, long timeoutSecond, IMyJobExecutorInf myJobExecutorInf) {
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        String timestamp = String.valueOf(System.currentTimeMillis());
        CountDownLatch latch = new CountDownLatch(1);
        //超时时间必须大于业务逻辑执行的时间-锁才会有效果
        String isLock = redisUtil.set(key, timestamp, "nx", "ex", timeoutSecond);
        if (ObjectUtils.equals(isLock, "OK")) {
            //有些情况下任务是一个循环
            //所以我们可以把锁的粒度定义在单次循环的时间内有锁的有效时间
            //这种情况下锁的有效时间就变为了相对有效时间
            Thread expireUpdateThread= new ExpireUpdateThread(key, (int) timeoutSecond, timestamp, latch);
            expireUpdateThread.start();
            //当且仅当这个lock不存在的时候，设置完成之后设置过期时间为10。
            //获取锁的机制是对了，但是删除锁的机制直接使用del是不对的。因为有可能导致误删别人的锁的情况。
            //
            //具休的业务逻辑
            try {
                //模拟业务执行时间
                //TimeUnit.SECONDS.sleep(50);
                myJobExecutorInf.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                //业务完成
                String timestampInRedis = redisUtil.get(key);
                if (ObjectUtils.equals(timestamp, timestampInRedis)) {
                    redisUtil.del(key);
                }
                latch.countDown();
                //此处是人为触发线程中断异常-快速关闭ExpireUpdateThread线程
                expireUpdateThread.interrupt();
            }

        }
    }
}