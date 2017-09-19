package com.yzd.common.mq.example.schedule._base;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 阻塞队列令牌桶的MAP--单例模式
 * Created by zd.yao on 2017/9/15.
 */
public class TokenBucketMap {
    private static class SingletonHolder {
        private static final TokenBucketMap INSTANCE = new TokenBucketMap();
    }
    public static final TokenBucketMap getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private TokenBucketMap (){
        blockingQueueMap=new ConcurrentHashMap<>();
    }
    private Map<String,ArrayBlockingQueue> blockingQueueMap;
    public ArrayBlockingQueue getBlockingQueue(String key,int maxThreadSize){
        //先删除再创建，保证当前的为最新
        blockingQueueMap.remove(key);
        blockingQueueMap.put(key,new ArrayBlockingQueue<Integer>(maxThreadSize));
        return blockingQueueMap.get(key);
    }
    public int getMapSize(){
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
