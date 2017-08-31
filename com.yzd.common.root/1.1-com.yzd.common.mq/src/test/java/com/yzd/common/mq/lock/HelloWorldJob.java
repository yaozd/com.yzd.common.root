package com.yzd.common.mq.lock;

import com.yzd.common.mq.redis.job.lock.IMyJobExecutorInf;

import java.util.concurrent.TimeUnit;

/**
 * Created by zd.yao on 2017/8/28.
 */
public class HelloWorldJob implements IMyJobExecutorInf {
    @Override
    public void execute() {
        try {
            System.out.println("task begin");
            TimeUnit.SECONDS.sleep(100);
            System.out.println("task end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
