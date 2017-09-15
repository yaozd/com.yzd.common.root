package com.yzd.common.mq.writer;

import com.yzd.common.mq.enumExt.JobLockEnum;
import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.job.lock.RedisJobLockUtil;
import com.yzd.common.mq.redis.job.writer.RedisJobWriterUtil;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by zd.yao on 2017/8/29.
 */
public class _MainTest {

    JobEnum keyEnum=JobLockEnum.HelloWorldJob;

    /**
     * 消息队列-写入消息
     */
    @Test
    public void writerExample(){

        for (int i = 0; i < 200; i++) {
            String val="id=" + i;
            RedisJobWriterUtil.write(keyEnum,val);
        }
    }

    /**
     *TODO 最终版-写入任务
     */
    @Test
    public void writerExample_final(){
        long timeoutSecond = 10;
        //对当前执行的任务进行加锁--具体的实现可参考lock下例子
        RedisJobLockUtil.lockTask(keyEnum.getLockWriterName(), timeoutSecond, () -> doWorkForWriter());
        System.out.println("redis-writerExample2");
    }
    //将任务写入到消息队列中
    void doWorkForWriter(){
        //模拟业务执行时间
        try {
            TimeUnit.SECONDS.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 100; i++) {
            String val="id=" + i;
            RedisJobWriterUtil.write(keyEnum,val);
        }
    }


}
