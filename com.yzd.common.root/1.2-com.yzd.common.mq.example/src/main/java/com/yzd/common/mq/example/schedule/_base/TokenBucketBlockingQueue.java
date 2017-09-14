package com.yzd.common.mq.example.schedule._base;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by zd.yao on 2017/9/14.
 */
public class TokenBucketBlockingQueue {
    private static class SingletonHolder {
        private static final TokenBucketBlockingQueue INSTANCE = new TokenBucketBlockingQueue();
    }
    public static final TokenBucketBlockingQueue getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private TokenBucketBlockingQueue (){
        blockingQueueMap=new HashMap<>();
    }
    private Map<String,ArrayBlockingQueue> blockingQueueMap;
    public ArrayBlockingQueue getBlockingQueue(String key,int maxThreadSize){
        //先删除再创建，保证当前的为最新
        blockingQueueMap.remove(key);
        blockingQueueMap.put(key,new ArrayBlockingQueue<Integer>(maxThreadSize));
        return blockingQueueMap.get(key);
    }
    public int getBlockingQueueMapSize(){
        return blockingQueueMap.size();
    }
    public boolean isNoRunningTask(){
        for(ArrayBlockingQueue e:blockingQueueMap.values()){
            if(!e.isEmpty()){
                return false;
            }
        }
       return true;
    }
}
