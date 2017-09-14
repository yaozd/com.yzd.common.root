package com.yzd.common.mq.reader;

import com.yzd.common.mq.base.WorkThreadPool;
import com.yzd.common.mq.enumExt.JobLockEnum;
import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.job.mutesKey.RedisJobMutesKeyUtil;
import com.yzd.common.mq.redis.job.reader.RedisJobReaderTask;
import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;
import org.junit.Test;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.util.Collection;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;

/**
 * Created by zd.yao on 2017/8/28.
 */
public class _MainTest {
    //String key = "RedisMq:SynchronousQueue";
    String key = JobLockEnum.HelloWorldJob.getListName();
    JobEnum keyEnum=JobLockEnum.HelloWorldJob;
    /**************************** redis 列表List扩展 start***************************/
    /**
     * 这是一种特殊情况-以Redis作为消息队列-并且队列内容特别的大
     * 这里会以List中的value的值做为分片的信息
     * 这样就可以实现水平扩展
     * 主要解决redis作为消息队列时出现数据倾斜的问题
     */
    @Test
    public void lpushExtExample() {
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        for (int i = 0; i < 100; i++) {
            Long result = redisUtil.rpushExt(keyEnum.getListName(), "2017-07-11:" + i);
            System.out.println(result);
        }
    }

    /**
     * 通过同步队列SynchronousQueue来合并多个redis数据源的读取结果
     * @throws InterruptedException
     */
    @Test
    public void brpopExtByShardedJedisPoolExample() throws InterruptedException {
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        Collection<JedisShardInfo> jedisCollection = redisUtil.getAllJedisShardInfo();
        ExecutorService executorService = Executors.newFixedThreadPool(jedisCollection.size());
        SynchronousQueue<String> data = new SynchronousQueue<String>();
        //相当于令牌桶-通过令牌来控制有效读取的任务数等于可运行的处理的线程数
        //ArrayBlockingQueue<Integer>(2)相当于线程数是2个
        ArrayBlockingQueue<Integer> TokenBucket=new ArrayBlockingQueue<Integer>(2);
        for (JedisShardInfo j : jedisCollection) {
            ShardedJedisPool shardedJedisPool =redisUtil.getOneShardedJedisPool(j);
            RedisJobReaderTask jedisExecutor = new RedisJobReaderTask(shardedJedisPool, keyEnum.getListName(),data,TokenBucket);
            executorService.execute(jedisExecutor);
        }
        while (true){
            String value=data.take();
            //设置:mutexKey互斥锁
            Boolean isOkSetMutesKey= RedisJobMutesKeyUtil.set(keyEnum, value);
            if(!isOkSetMutesKey){
                continue;
            }
            try{
                //具体的业务处理逻辑
                //任务操作异常或数据库异常
                System.out.println("brpopExtByShardedJedisPoolExample:value="+value);
            }finally {
                //删除:先删除set排除消息对列再删除mutexKey互斥锁
                RedisJobMutesKeyUtil.del(keyEnum,value);
            }
        }
    }
    /**
     *TODO 最终版-增加多线程任务处理程序
     */
    @Test
    public void brpopExtByShardedJedisPoolExample_final() throws InterruptedException {
        //相当于令牌桶-通过令牌来控制有效读取的任务数等于可运行的处理的线程数
        //ArrayBlockingQueue<Integer>(2)相当于线程数是2个
        //ArrayBlockingQueue<Integer> TokenBucket=new ArrayBlockingQueue<Integer>(2);
        WorkThreadPool task_readQueue_threadPool=new WorkThreadPool(keyEnum.getTokenBucketName(),20);
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        Collection<JedisShardInfo> jedisCollection = redisUtil.getAllJedisShardInfo();
        ExecutorService executorService = Executors.newFixedThreadPool(jedisCollection.size());
        SynchronousQueue<String> data = new SynchronousQueue<String>(true);
        for (JedisShardInfo j : jedisCollection) {
            ShardedJedisPool shardedJedisPool =redisUtil.getOneShardedJedisPool(j);
            RedisJobReaderTask jedisExecutor = new RedisJobReaderTask(shardedJedisPool, keyEnum.getListName(),data,task_readQueue_threadPool.getTokenBucket());
            executorService.execute(jedisExecutor);
        }
        int debug=1;
        while (true){
            String value=data.take();
            System.out.println(value);
            JobHandler task1=new JobHandler(task_readQueue_threadPool.getTokenBucket(),keyEnum,value);
            task_readQueue_threadPool.getExecutor().execute(task1);
        }
    }
}
