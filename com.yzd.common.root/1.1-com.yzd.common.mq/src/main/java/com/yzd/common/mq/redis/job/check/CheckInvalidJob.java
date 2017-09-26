package com.yzd.common.mq.redis.job.check;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.job.lock.IMyJobExecutorInf;
import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 检查无效的任务-然后从排重队列中剔除
 * Created by zd.yao on 2017/8/30.
 */
public class CheckInvalidJob implements IMyJobExecutorInf {
    private JobEnum keyEnum;

    public CheckInvalidJob(JobEnum keyEnum) {
        this.keyEnum = keyEnum;
    }

    @Override
    public void execute() {
        try {
            doWork();
        } catch (Exception ex) {
            //log
        }
    }

    void doWork() throws InterruptedException {
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        List<String> redisUrlList = redisUtil.getAllRedisUrls();
        ExecutorService executorService = Executors.newFixedThreadPool(redisUrlList.size());
        CountDownLatch latch = new CountDownLatch(redisUrlList.size());
        for (String redisUrl : redisUrlList) {
            RedisJobCheckTask jedisExecutor = new RedisJobCheckTask(redisUrl, keyEnum, latch);
            executorService.execute(jedisExecutor);
        }
        latch.await();
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);
    }

}
