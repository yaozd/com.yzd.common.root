package com.yzd.common.mq.writer;

import com.yzd.common.mq.enumExt.JobLockEnum;
import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.job.lock.RedisJobLockUtil;
import com.yzd.common.mq.redis.job.writer.RedisJobWriterUtil;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by zd.yao on 2017/8/29.
 */
public class _MainTest {
    @Rule
    public ContiPerfRule i = new ContiPerfRule();
    JobEnum keyEnum=JobLockEnum.HelloWorldJob;

    /**
     * 并发写入测试
     * 消息队列-写入消息
     */
    @Test
    @PerfTest(threads = 100,invocations=10)
    public void writerExample(){

        for (int i = 0; i < 10000000; i++) {
            String val="id=" + i+UUID.randomUUID().toString();
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
