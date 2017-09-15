package com.yzd.common.mq.example.config;

import com.yzd.common.mq.example.schedule._base.TokenBucketMap;
import com.yzd.common.mq.redis.job.reader.RedisJobReader;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 监听应用关闭的钩子
 * Created by zd.yao on 2017/9/15.
 */
public class EventListener  implements ApplicationListener {
    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        //应用关闭-kill PID 不要使用kill -9 PID
        if (applicationEvent instanceof ContextClosedEvent) {
            System.out.println("应用关闭-kill PID 不要使用kill -9 PID");
            //关闭消息队列的读取任务
            RedisJobReader.shutdown();
            //
            ExecutorService executor = Executors.newFixedThreadPool(1);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        boolean isNoRunningTask= TokenBucketMap.getInstance().isNoRunningTask();
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
            try {
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {e.printStackTrace();}
            //endregion
            return;
        }
    }
}
