package com.yzd.common.mq.shutdown;

import com.yzd.common.mq.base.TokenBucketBlockingQueue;
import com.yzd.common.mq.redis.job.reader.RedisJobReader;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zd.yao on 2017/9/14.
 */
public class _MainTest {
    //主要用于使用的消息队列的后台任务调试-能够优雅退出-监听退出信号
    @Test
    public void closeReaderTask(){
        RedisJobReader.shutdown();
        int blockingQueueMapSize= TokenBucketBlockingQueue.getInstance().getBlockingQueueMapSize();
        boolean isNoRunningTask= TokenBucketBlockingQueue.getInstance().isNoRunningTask();
        System.out.println("blockingQueueMapSize="+blockingQueueMapSize);
        System.out.println("isNoRunningTask="+isNoRunningTask);
    }
    /**
     *TODO 最终版-关闭消息队列的读取任务优雅退出程序
     */
    @Test
    public void closeReaderTask_final() throws InterruptedException {
        System.out.println("Begin currentTimeMillis=" + new Date());
        //关闭消息队列的读取任务
        RedisJobReader.shutdown();
        //
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    boolean isNoRunningTask= TokenBucketBlockingQueue.getInstance().isNoRunningTask();
                    if(isNoRunningTask)break;
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //region 通过shutdown+awaitTermination实现任务执行超时后终止
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
        //endregion
        System.out.println("End currentTimeMillis=" + new Date());
    }
}
