package com.yzd.common.mq.example;

import com.yzd.common.mq.example.config.EventListener;
import com.yzd.common.mq.example.schedule._base.JobListEnum;
import com.yzd.common.mq.redis.sharded.SharedRedisConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.util.concurrent.CountDownLatch;

/**
 * Created by zd.yao on 2017/9/14.
 */
@SpringBootApplication
@ComponentScan("com.yzd.common.mq.example")
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    @Bean
    public CountDownLatch closeLatch() {
        return new CountDownLatch(1);
    }

    public static void main(String[] args) throws InterruptedException {
        logger.info("项目启动--BEGIN");
        //设置独立jedis线程池的大小=任务列表大小+7；如果不设置初始值则默认值为8；
        //这样可以保证jedis线程池根据任务列表的数据自动增长
        SharedRedisConfig.setJedisPoolSizeOfShardedJedisPoolMap(JobListEnum.values().length);
        //
        SpringApplication app = new SpringApplication(Application.class);
        app.addListeners(new EventListener());
        ApplicationContext ctx =app.run(args);
        logger.info("项目启动--END");
        CountDownLatch closeLatch = ctx.getBean(CountDownLatch.class);
        closeLatch.await();
    }
}