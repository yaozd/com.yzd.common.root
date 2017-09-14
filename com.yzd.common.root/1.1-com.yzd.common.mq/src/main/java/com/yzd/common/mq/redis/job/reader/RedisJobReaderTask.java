package com.yzd.common.mq.redis.job.reader;

import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.ShardedJedisPool;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by zd.yao on 2017/8/28.
 */
public class RedisJobReaderTask implements Runnable {
    private ShardedJedisPool j;
    private String listKey;
    private SynchronousQueue<String> data;
    private ArrayBlockingQueue<Integer> TokenBucket;
    public RedisJobReaderTask(ShardedJedisPool j, String listKey, SynchronousQueue<String> data,ArrayBlockingQueue<Integer> TokenBucket) {
        this.j=j;
        this.listKey=listKey;
        this.data=data;
        this.TokenBucket=TokenBucket;
    }

    @Override
    public void run() {
        while (true){
            //当读取器获得退出命令后则不在执行读取任务
            if(RedisJobReader.isShutdown){break;}
            try {
                //相当于令牌桶-通过令牌来控制有效读取的任务数等于可运行的处理的线程数
                TokenBucket.put(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //从redis中读取消息
            String value = getValueWhile();
            try {
                //将数据放在同步队列中
                data.put(value);
            } catch (InterruptedException e) {
                //log 记录日志
                e.printStackTrace();
            }
        }
    }
    //循环取值-直到当前值不等null时才可以
    private String getValueWhile(){
        while (true){
            String value = getValue();
            if (value == null) continue;
            return value;
        }
    }

    //从redis 里面读取消息可放到一个单独的抽象类里AbstractJob中，可以使代码更加清楚
    //通过静态方法引用就可以
    private String getValue(){
        //
        String value=null;
        //redis-网络抖动等特殊情况下的异常处理
        try{
            //阻塞指令-读取reids的消息队列
            ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
            value=redisUtil.blpopExt(j, listKey, 5);
            System.out.println("RedisJobReaderTask-阻塞指令-读取reids的消息队列"+value);
        }catch (Exception e){
            //log 记录日志
            e.printStackTrace();
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            return null;
        }
        //redis 阻塞超时的情况下处理
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value;
    }
}

