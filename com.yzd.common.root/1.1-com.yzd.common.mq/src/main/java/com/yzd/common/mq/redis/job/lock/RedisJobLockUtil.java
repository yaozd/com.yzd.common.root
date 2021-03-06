package com.yzd.common.mq.redis.job.lock;

import com.yzd.common.mq.redis.job.reader.RedisJobReader;
import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by zd.yao on 2017/8/28.
 */
public class RedisJobLockUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisJobLockUtil.class);
    //对当前执行的任务进行加锁
    public static void lockTask(String key, long timeoutSecond, IMyJobExecutorInf myJobExecutorInf){
        lockTask(key,timeoutSecond,myJobExecutorInf,0);
    }
    public static void lockTask(String key, long timeoutSecond, IMyJobExecutorInf myJobExecutorInf,int myJobExecutorAfterSleepSecond) {
        //当获得退出命令后则不在执行任何操作
        if(RedisJobReader.isShutdown){return;}
        //
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
                long  begin = System.currentTimeMillis();
                myJobExecutorInf.execute();
                long  end = System.currentTimeMillis();
                long executeTime=(end-begin)/1000;
                int restSleepTime= (int) (myJobExecutorAfterSleepSecond-executeTime);
                //主要是保证单实例程序在某一时间段内执行的次数
                myJobExecutorAfterSleepFun(restSleepTime);
            } catch (Exception e) {
                logger.error("[lockTask]", e);
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
    //主要是保证单实例程序在某一时间段内执行的次数
    private static void myJobExecutorAfterSleepFun(int myJobExecutorAfterSleepSecond) {
        if(myJobExecutorAfterSleepSecond<1)return;
        try {
            TimeUnit.SECONDS.sleep(myJobExecutorAfterSleepSecond);
        } catch (InterruptedException e) {
            //此处人为故意吃掉异常
        }
    }
}