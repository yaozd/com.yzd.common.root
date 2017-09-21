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
            // 当count为负数时，移除方向是从尾到头-删除所有
            long valOfLrem = redisUtil.lremExt(keyEnum.getListName(), 0, e);
            if (valOfLrem >0 ) {
                //region 尽可能保证数据不重复添加
                boolean valOfSrem = redisUtil.sremExt(keyEnum.getSetName(), e);
                //set中删除失败则正明该元素已经不存在
                if(!valOfSrem)continue;
                //set添加失败则证明该元素已经存在
                long countOfsadd = redisUtil.saddExt(keyEnum.getSetName(), e);
                if(countOfsadd==0)continue;
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
            boolean valOfSrem = redisUtil.sremExt(keyEnum.getSetName(), e);
            //endregion
            System.out.println("【RedisJobCheckTask】-删除set中的消息="+valOfSrem);
        }
    }
}
