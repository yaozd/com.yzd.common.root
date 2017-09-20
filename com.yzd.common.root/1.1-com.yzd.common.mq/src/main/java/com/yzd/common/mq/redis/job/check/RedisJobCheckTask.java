package com.yzd.common.mq.redis.job.check;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.job.mutesKey.RedisJobMutesKeyUtil;
import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zd.yao on 2017/8/30.
 */
public class RedisJobCheckTask implements Runnable {
    private String redisUrl;
    private JobEnum keyEnum;
    private CountDownLatch latch;
    public RedisJobCheckTask(String redisUrl, JobEnum keyEnum,CountDownLatch latch) {
        this.redisUrl=redisUrl;
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
        long total = redisUtil.scardExt(redisUrl, keyEnum.getSetName());
        // 每5分钟获取当前消息的10%最多大值为200，进行消息删除重复消息；
        //200>countOfSrandMember>20
        int countOfSrandMember = (int) ((total + 10) * 0.1) + 20;
        countOfSrandMember = countOfSrandMember > 200 ? 200 : countOfSrandMember;
        List<String> setList = redisUtil.srandMemberExt(redisUrl,keyEnum.getSetName(), countOfSrandMember);
        //
        for (String e : setList) {
            // 判断当前消息队列中是否存在此消息
            // 当count为负数时，移除方向是从尾到头-删除
            long valOfLrem = redisUtil.lremExt(keyEnum.getListName(), -1, e);
            if (valOfLrem == 1) {
                //region 必须确定这个值依然存在set集合中，才知道没有被其他线程消费，才可以在添加到list集合中
                long countOfsadd = redisUtil.saddExt(keyEnum.getSetName(), e);
                //countOfsadd>0，说明已经不存在set集合中，已经被其他线程消费掉了。
                if(countOfsadd>0)continue;
                // 常规操作-从头部插入
                redisUtil.lpushExt(keyEnum.getListName(), e);
                //endregion
                continue;
            }
            // 验证是否当前消息正在运行
            boolean isExists = RedisJobMutesKeyUtil.exists(keyEnum, e);
            if (isExists) {
                continue;
            }
            //region 当前消息中不存在此消息同时当前正在运行消息中也不存在，则进行删除set中的消息
            //先删除list集合中的值，再删除set集合中的值，确保值一定被删除
            redisUtil.lremExt(keyEnum.getListName(), -1, e);
            boolean valOfSrem = redisUtil.sremExt(keyEnum.getSetName(), e);
            //endregion
            System.out.println("【RedisJobCheckTask】-删除set中的消息="+valOfSrem);
        }
    }
}
