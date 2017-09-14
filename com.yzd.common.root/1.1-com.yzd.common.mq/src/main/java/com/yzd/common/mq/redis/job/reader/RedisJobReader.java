package com.yzd.common.mq.redis.job.reader;

/**
 * Created by zd.yao on 2017/9/14.
 */
public class RedisJobReader {
    public static boolean isShutdown=false;
    //关闭消息队列的读取任务
    public static void shutdown(){
        isShutdown=true;
    }
}
