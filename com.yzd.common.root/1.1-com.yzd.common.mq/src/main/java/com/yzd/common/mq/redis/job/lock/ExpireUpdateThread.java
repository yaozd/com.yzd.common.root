package com.yzd.common.mq.redis.job.lock;

import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;
import org.apache.commons.lang.ObjectUtils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by zd.yao on 2017/7/12.
 */
public class ExpireUpdateThread extends Thread {
    String key;
    int timeoutSecond;
    String timestamp;
    CountDownLatch latch;
    public ExpireUpdateThread(String key, int timeoutSecond, String timestamp, CountDownLatch latch){
        this.key=key;
        this.timeoutSecond=timeoutSecond;
        this.timestamp=timestamp;
        this.latch=latch;
    }
    @Override
    public void run(){
        while (latch.getCount()>0){
            ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
            String timestampInRedis=redisUtil.get(key);
            //有些情况下任务是一个循环
            //所以我们可以把锁的粒度定义在单次循环的时间内有锁的有效时间
            //这种情况下锁的有效时间就变为了相对有效时间
            //当且仅当这个lock不存在的时候，设置完成之后设置过期时间为10。
            //获取锁的机制是对了，但是删除锁的机制直接使用del是不对的。因为有可能导致误删别人的锁的情况。
            if(ObjectUtils.equals(timestamp, timestampInRedis)){
                redisUtil.expire(key, timeoutSecond+5);
            }else {
                latch.countDown();
            }
            System.out.println("latch.getCount():"+latch.getCount());
            try {
                TimeUnit.SECONDS.sleep(timeoutSecond);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
