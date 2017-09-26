package com.yzd.common.mq.lock;

import com.yzd.common.mq.enumExt.JobLockEnum;
import com.yzd.common.mq.redis.job.lock.IMyJobExecutorInf;
import com.yzd.common.mq.redis.job.lock.RedisJobLockUtil;
import org.junit.Test;

/**
 * Created by zd.yao on 2017/8/28.
 */
public class _MainTest {
    String KEY_TEST="KEY-BUSINESS_LOCK";
    /**
     * 对当前执行的任务进行加锁--具体的实现可参考lock下例子
     * ==
     * 有一大的前提：假设程序运行的服务没有发生网络抖动的情况
     * 有一些统计任务或者报表任务不是短时间可以运行完成
     * 就可以通过一个单独的线程来维护过期时间来解决长时间执行的任务
     * 使用情况：后台调度任务或补录程序
     */
    @Test
    public void lockForLongTimeExample() {
        //String key = KEY_TEST;
        String key = JobLockEnum.HelloWorldJob.getLockWriterName();
        long timeoutSecond = 10;
        IMyJobExecutorInf myJobExecutorInf=new HelloWorldJob();
        //对当前执行的任务进行加锁--具体的实现可参考lock下例子
        RedisJobLockUtil.lockTask(key, timeoutSecond, myJobExecutorInf);
        System.out.println("redis-lockForLongTimeExample");
    }
}
