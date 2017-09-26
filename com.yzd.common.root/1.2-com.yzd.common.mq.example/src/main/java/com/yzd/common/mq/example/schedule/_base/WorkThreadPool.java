package com.yzd.common.mq.example.schedule._base;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by zd.yao on 2017/7/7.
 */
public class WorkThreadPool {
    //通过ArrayBlockingQueue阻塞队列来控制线程的数量，相当于令牌桶算法，但这里的线程的数量就是阻塞队列的大小，但并不是严格的
    //在一些极端的情况下会比阻塞队列的大小多出5到10的线程。大家可以试情况选择
    //相当于令牌桶
    private ArrayBlockingQueue<Integer> TokenBucket;
    //同步阻塞队列线程池
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    public WorkThreadPool(String key,int maxThreadSize){
        //this.TokenBucket= new ArrayBlockingQueue<Integer>(maxThreadSize);
        this.TokenBucket= TokenBucketMap.getInstance().getBlockingQueue(key,maxThreadSize);
    }

    public ArrayBlockingQueue<Integer> getTokenBucket() {
        return TokenBucket;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }
}