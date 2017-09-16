package com.yzd.common.mq.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executor;

/**
 * Created by zd.yao on 2017/9/14.
 */
@Configuration
@EnableScheduling
public class ScheduleConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    @Bean(destroyMethod="shutdown")
    public Executor taskExecutor() {
        //可以根据调度任务的个数来设置线程池的大小
        //return Executors.newScheduledThreadPool(30);
        //Executors.newScheduledThreadPool(30);一样没有根本的区别只是自定义线程池的名字
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(30);
        scheduler.setThreadNamePrefix("TaskScheduler-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }
}
