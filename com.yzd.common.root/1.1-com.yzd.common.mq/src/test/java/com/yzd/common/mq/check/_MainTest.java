package com.yzd.common.mq.check;

import com.yzd.common.mq.enumExt.JobLockEnum;
import com.yzd.common.mq.redis.job.check.CheckInvalidJob;
import com.yzd.common.mq.redis.job.check.RedisJobCheckTask;
import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.job.lock.IMyJobExecutorInf;
import com.yzd.common.mq.redis.job.lock.RedisJobLockUtil;
import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;
import org.junit.Test;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by zd.yao on 2017/8/30.
 */
public class _MainTest {
    JobEnum keyEnum= JobLockEnum.HelloWorldJob;

    /**
     * 初始思路
     */
    @Test
    public void delSetKeyTaskExample(){
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        long total = redisUtil.scard(keyEnum.getSetName());
        // 每5分钟获取当前消息的10%最多大值为200，进行消息删除重复消息；
        //200>countOfSrandMember>20
        int countOfSrandMember = (int) ((total + 10) * 0.1) + 20;
        countOfSrandMember = countOfSrandMember > 200 ? 200 : countOfSrandMember;
        List<String> setList = redisUtil.srandMember(keyEnum.getSetName(), countOfSrandMember);
        //
        for (String e : setList) {
            // 判断当前消息队列中是否存在此消息
            // 当count为负数时，移除方向是从尾到头-删除
            long valOfLrem = redisUtil.lrem(keyEnum.getListName(), -1, e);
            if (valOfLrem == 1) {
                // 常规操作-从头部插入
                redisUtil.lpush(keyEnum.getListName(), e);
                continue;
            }
            // 验证是否当前消息正在运行
            boolean isExists = redisUtil.exists(keyEnum.getListName() + e);
            if (isExists) {
                continue;
            }
            // 当前消息中不存在此消息同时当前正在运行消息中也不存在，则进行删除set中的消息
            boolean valOfSrem = redisUtil.srem(keyEnum.getListName(), e);
            System.out.println(valOfSrem);
        }
    }

    /**
     * 对应==RedisJobCheckTask类代码
     */
    @Test
    public void delSetKeyTaskExample1(){
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        Collection<JedisShardInfo> jedisCollection = redisUtil.getAllJedisShardInfo();
        JedisShardInfo j=jedisCollection.iterator().next();
        ShardedJedisPool shardedJedisPool =redisUtil.getOneShardedJedisPool(j);
        long total = redisUtil.scardExt(shardedJedisPool, keyEnum.getSetName());
        // 每5分钟获取当前消息的10%最多大值为200，进行消息删除重复消息；
        //200>countOfSrandMember>20
        int countOfSrandMember = (int) ((total + 10) * 0.1) + 20;
        countOfSrandMember = countOfSrandMember > 200 ? 200 : countOfSrandMember;
        List<String> setList = redisUtil.srandMemberExt(shardedJedisPool,keyEnum.getSetName(), countOfSrandMember);
        //
        for (String e : setList) {
            // 判断当前消息队列中是否存在此消息
            // 当count为负数时，移除方向是从尾到头-删除
            long valOfLrem = redisUtil.lremExt(keyEnum.getListName(), -1, e);
            if (valOfLrem == 1) {
                // 常规操作-从头部插入
                redisUtil.lpushExt(keyEnum.getListName(), e);
                continue;
            }
            // 验证是否当前消息正在运行
            boolean isExists = redisUtil.exists(keyEnum.getMutesKeyName() + e);
            if (isExists) {
                continue;
            }
            // 当前消息中不存在此消息同时当前正在运行消息中也不存在，则进行删除set中的消息
            boolean valOfSrem = redisUtil.sremExt(keyEnum.getSetName(), e);
            System.out.println(valOfSrem);
        }
    }

    /**
     * 对应==CheckInvalidJob类代码
     * 等待所有线程执行完毕
     * @throws InterruptedException
     */
    @Test
    public void delSetKeyTaskExample2() throws InterruptedException {
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        Collection<JedisShardInfo> jedisCollection = redisUtil.getAllJedisShardInfo();
        ExecutorService executorService = Executors.newFixedThreadPool(jedisCollection.size());
        CountDownLatch latch = new CountDownLatch(jedisCollection.size());
        for (JedisShardInfo j : jedisCollection) {
            ShardedJedisPool shardedJedisPool =redisUtil.getOneShardedJedisPool(j);
            RedisJobCheckTask jedisExecutor=new RedisJobCheckTask(shardedJedisPool,keyEnum,latch);
            executorService.execute(jedisExecutor);
        }
        latch.await();
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);
    }

    /**
     *TODO  最终版-组合版
     */
    //通过对当前的方法增加一个线程锁-确保当前只有一个任务在执行
    @Test
    public void delSetKeyTaskExample_final(){
        long timeoutSecond = 10;
        IMyJobExecutorInf myJobExecutorInf=new CheckInvalidJob(keyEnum);
        //对当前执行的任务进行加锁--具体的实现可参考lock下例子
        RedisJobLockUtil.lockTask(keyEnum.getLockCheckName(), timeoutSecond, myJobExecutorInf);
        System.out.println("redis-delSetKeyTaskExample3");
    }
}

