package com.yzd.common.mq.redis.job.check;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.job.mutesKey.RedisJobMutesKeyUtil;
import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;
import com.yzd.common.mq.redis.sharded.SharedRedisConfig;
import redis.clients.jedis.BinaryClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        List<String> setList = redisUtil.srandMemberExt(redisUrl, keyEnum.getSetName(), countOfSrandMember);
        if(setList.size()==0)return;
        String[] checkTempList = new String[setList.size()];
        setList.toArray(checkTempList);
        redisUtil.delExtByRedisUrl(redisUrl, keyEnum.getCheckTmpName());
        redisUtil.saddExtByRedisUrl(redisUrl, keyEnum.getCheckTmpName(), checkTempList);
        //关键点-数据从redis中读取到数据加锁之间有一个空白时间--是造成重复添加的原因
        //第一次确认需要删除的元素
        List<String> setList_OneSure=new ArrayList<>();
        for (String e : setList) {
            Boolean isNotExistSetMember=redisUtil.sIsMemberExtByRedisUrl(redisUrl, keyEnum.getSetName(),e)==false;
            if (isNotExistSetMember) continue;
            setList_OneSure.add(e);
        }
        if(setList_OneSure.size()==0)return;
        //第二次确认需要删除的元素
        List<String> setList_TwoSure=new ArrayList<>();
        for (String e : setList_OneSure) {
            if (isNotNeedRemove(redisUtil, e)) continue;
            setList_TwoSure.add(e);
        }
        if(setList_TwoSure.size()==0)return;
        //第二次与第三次中休眠3秒
        //解决数据从redis中读取到数据加锁之间有一个空白时间--是造成重复添加的问题
        sleepTime_SECONDS_3();
        //第三次确认需要删除的元素
        List<String> setList_ThreeSure=new ArrayList<>();
        for(String e:setList_TwoSure){
            if (isNotNeedRemove(redisUtil, e)) continue;
            setList_ThreeSure.add(e);
        }
        if(setList_ThreeSure.size()==0)return;
        for(String e:setList_ThreeSure){
            //region 当前消息中不存在此消息同时当前正在运行消息中也不存在，则进行删除set中的消息
            //先删除list集合中的值，再删除set集合中的值，确保值一定被删除
            redisUtil.sremExt(keyEnum.getSetName(), e);
            redisUtil.sremExt(keyEnum.getCheckTmpName(), e);
            //主要用于测试时统计有哪些值被排除掉了。
            redisUtil.lpushExt("TEST_TMP_LIST", e);
            //endregion
        }
        redisUtil.delExtByRedisUrl(redisUrl,keyEnum.getCheckTmpName());
        redisUtil.lremExtByRedisUrl(redisUrl,keyEnum.getListName(),0,SharedRedisConfig.CHECK_IS_EXIST_TEMP_VAL);
    }

    private void sleepTime_SECONDS_3() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isNotNeedRemove(ShardedRedisMqUtil redisUtil, String e) {
        //返回值是【-1则pivot不存在】【0则当前list集合不存在】
        Long linsertLong=  redisUtil.linsertExt(keyEnum.getListName(), BinaryClient.LIST_POSITION.AFTER, e, SharedRedisConfig.CHECK_IS_EXIST_TEMP_VAL);
        if(linsertLong>0){
            redisUtil.sremExt(keyEnum.getCheckTmpName(), e);
            redisUtil.lremExt2(keyEnum.getListName(), 0, e, SharedRedisConfig.CHECK_IS_EXIST_TEMP_VAL);
            return true;
        }
        // 验证是否当前消息正在运行
        boolean isExistsMutesKey = RedisJobMutesKeyUtil.exists(keyEnum, e);
        if (isExistsMutesKey) {
            redisUtil.sremExt(keyEnum.getCheckTmpName(), e);
            return true;
        }
        //
        boolean isExistCheckTmpMember=redisUtil.sIsMemberExtByRedisUrl(redisUrl,keyEnum.getCheckTmpName(), e);
        if(!isExistCheckTmpMember){
            return true;
        }
        return false;
    }
}
