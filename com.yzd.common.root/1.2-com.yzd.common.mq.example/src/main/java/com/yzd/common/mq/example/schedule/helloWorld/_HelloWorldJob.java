package com.yzd.common.mq.example.schedule.helloWorld;

import com.yzd.common.mq.example.schedule._base.JobListEnum;
import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.job.lock.RedisJobLockUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zd.yao on 2017/9/14.
 */
@Component
public class _HelloWorldJob {
    JobEnum keyEnum= JobListEnum.HelloWorldJob;
    @Scheduled(initialDelay = 3000, fixedDelay = 1000 * 5)
    public void writeTask() throws InterruptedException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
        System.out.println("[writeTask]-Begin-currentTime= " + dateFormat.format(new Date()));
        //region 对当前执行的任务进行加锁--具体的实现可参考lock下例子
        long timeoutSecond = 10;
        RedisJobLockUtil.lockTask(keyEnum.getLockWriterName(), timeoutSecond,new WriteTask(keyEnum));
        //endregion
        System.out.println("[writeTask]-End-currentTime= " + dateFormat.format(new Date()));

    }
    @Scheduled(initialDelay = 3000, fixedDelay = 1000 * 5)
    public void readTask() throws InterruptedException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[readTask]每隔5秒钟执行一次 " + dateFormat.format(new Date()));
    }
    @Scheduled(initialDelay = 3000, fixedDelay = 1000 * 5)
    public void checkTask() throws InterruptedException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        System.out.println("[checkTask]每隔5秒钟执行一次 " + dateFormat.format(new Date()));
    }
}