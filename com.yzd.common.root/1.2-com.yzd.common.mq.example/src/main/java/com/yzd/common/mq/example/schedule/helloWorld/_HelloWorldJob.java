package com.yzd.common.mq.example.schedule.helloWorld;

import com.yzd.common.mq.example.schedule._base.JobListEnum;
import com.yzd.common.mq.example.schedule._base.WorkThreadPool;
import com.yzd.common.mq.redis.job.check.CheckInvalidJob;
import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.job.lock.IMyJobExecutorInf;
import com.yzd.common.mq.redis.job.lock.RedisJobLockUtil;
import com.yzd.common.mq.redis.job.reader.RedisJobReaderTask;
import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by zd.yao on 2017/9/14.
 */
@Component
public class _HelloWorldJob {
    private static final Logger logger = LoggerFactory.getLogger(_HelloWorldJob.class);
    JobEnum keyEnum= JobListEnum.HelloWorldJob;
    //模拟程序仅执行一次的情况-真正开发时不需要isCloseWriter
    boolean isCloseWriter=false;
    //int myJobExecutorAfterSleepSecond=5; 单实例并且每5秒执行一次
    @Scheduled(initialDelay = 3000, fixedDelay = 1000 * 5)
    public void writeTask() throws InterruptedException {
        //模拟程序仅执行一次的情况
        //if(isCloseWriter){return;}isCloseWriter=true;
        //
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
        logger.info("[writeTask]-Begin-currentTime= " + dateFormat.format(new Date()));
        int myJobExecutorAfterSleepSecond=0;
        //region 对当前执行的任务进行加锁--具体的实现可参考lock下例子
        long timeoutSecond = 10;
        RedisJobLockUtil.lockTask(keyEnum.getLockWriterName(), timeoutSecond,new WriteTask(keyEnum),myJobExecutorAfterSleepSecond);
        //endregion
        logger.info("[writeTask]-End-currentTime= " + dateFormat.format(new Date()));

    }
    @Scheduled(initialDelay = 4000, fixedDelay = 10 * 5)
    public void readTask() throws InterruptedException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        logger.info("[readTask]-Begin-currentTime= " + dateFormat.format(new Date()));
        //region 实现任务读取
        int maxThreadSize=20;
        WorkThreadPool task_readQueue_threadPool=new WorkThreadPool(keyEnum.getTokenBucketName(),maxThreadSize);
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        List<String> redisUrlList = redisUtil.getAllRedisUrls();
        ExecutorService executorService = Executors.newFixedThreadPool(redisUrlList.size());
        SynchronousQueue<String> data = new SynchronousQueue<String>(true);
        for (String redisUrl : redisUrlList) {
            RedisJobReaderTask jedisExecutor = new RedisJobReaderTask(redisUrl, keyEnum.getListName(),data,task_readQueue_threadPool.getTokenBucket());
            executorService.execute(jedisExecutor);
        }
        int debug=1;
        while (true){
            String value=data.take();
            logger.info("当前值value="+value);
            ReadTask task1=new ReadTask(task_readQueue_threadPool.getTokenBucket(),keyEnum,value);
            task_readQueue_threadPool.getExecutor().execute(task1);
        }
        //endregion
    }
    //int myJobExecutorAfterSleepSecond=5; 单实例并且每5秒执行一次
    @Scheduled(initialDelay = 3000, fixedDelay = 10*5)
    public void checkTask() throws InterruptedException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        logger.info("[checkTask]-Begin-currentTime= " + dateFormat.format(new Date()));
        //region
        long timeoutSecond = 10;
        IMyJobExecutorInf myJobExecutorInf=new CheckInvalidJob(keyEnum);
        int myJobExecutorAfterSleepSecond=0;
        //对当前执行的任务进行加锁--具体的实现可参考lock下例子
        RedisJobLockUtil.lockTask(keyEnum.getLockCheckName(), timeoutSecond, myJobExecutorInf,myJobExecutorAfterSleepSecond);
        //endregion
        logger.info("[checkTask]-End-currentTime= " + dateFormat.format(new Date()));
    }
}