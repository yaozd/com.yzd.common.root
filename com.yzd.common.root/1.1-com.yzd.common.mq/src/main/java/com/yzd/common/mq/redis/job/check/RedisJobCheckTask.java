package com.yzd.common.mq.redis.job.check;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;
import redis.clients.jedis.ShardedJedisPool;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zd.yao on 2017/8/30.
 */
public class RedisJobCheckTask implements Runnable {
    private ShardedJedisPool j;
    private JobEnum keyEnum;
    private CountDownLatch latch;
    public RedisJobCheckTask(ShardedJedisPool j, JobEnum keyEnum,CountDownLatch latch) {
        this.j=j;
        this.keyEnum=keyEnum;
        this.latch=latch;
    }
    @Override
    public void run() {
        try{
            doWork();
        }finally {
            latch.countDown();
        }

    }

    private void doWork() {
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        long total = redisUtil.scardExt(j, keyEnum.getSetName());
        // 每5分钟获取当前消息的10%最多大值为200，进行消息删除重复消息；
        //200>countOfSrandMember>20
        int countOfSrandMember = (int) ((total + 10) * 0.1) + 20;
        countOfSrandMember = countOfSrandMember > 200 ? 200 : countOfSrandMember;
        List<String> setList = redisUtil.srandMemberExt(j,keyEnum.getSetName(), countOfSrandMember);
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
}
