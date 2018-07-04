package com.yzd.common.mq.redis.job.check;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.job.mutesKey.RedisJobMutesKeyUtil;
import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;
import com.yzd.common.mq.redis.sharded.SharedRedisConfig;
import com.yzd.common.mq.redis.utils.TimestampUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Tuple;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Created by zd.yao on 2017/8/30.
 */
public class RedisJobCheckTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RedisJobCheckTask.class);
    private String redisUrl;
    private JobEnum keyEnum;
    private CountDownLatch latch;

    public RedisJobCheckTask(String redisUrl, JobEnum keyEnum, CountDownLatch latch) {
        this.redisUrl = redisUrl;
        this.keyEnum = keyEnum;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            doWork();
        } finally {
            latch.countDown();
        }

    }

    private void doWork() {
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        long total = redisUtil.scardExt(redisUrl, keyEnum.getSetName());
        // 每5分钟获取当前消息的10%最多大值为200，进行消息删除重复消息；
        //200>countOfSrandMember>100
        int countOfSrandMember = (int) ((total + 10) * 0.1) + 100;
        countOfSrandMember = countOfSrandMember > 200 ? 200 : countOfSrandMember;
        List<String> setList = redisUtil.srandMemberExt(redisUrl, keyEnum.getSetName(), countOfSrandMember);
        if (setList.size() == 0) {
            return;
        }
        //
        Long nowTimestamp = TimestampUtil.dateToTimestamp();
        for (String setVal : setList) {
            Double score = redisUtil.zscoreExtByRedisUrl(redisUrl, keyEnum.getCreateTimeName(), setVal);
            if (score == null) {
                redisUtil.zaddExtByRedisUrl(redisUrl, keyEnum.getCreateTimeName(), nowTimestamp, setVal);
            }
        }
        //时间减5分钟后的时间戳
        Long after5Timestamp = nowTimestamp - 5 * 60 * 1000;
        String taskCreateTime = TimestampUtil.dateToString(TimestampUtil.timestampToDate(after5Timestamp));
        System.out.println("当前检查的任务创建时间<=" + taskCreateTime);
        //
        Set<Tuple> checkValSet = redisUtil.zrangeByScoreWithScoresByRedisUrl(redisUrl, keyEnum.getCreateTimeName(), 0, after5Timestamp, 0, 200);
        if (checkValSet == null||checkValSet.isEmpty()) {
            return;
        }
        //第一次确认需要删除的元素
        Set<Tuple> setList_OneSure = new HashSet<>();
        for (Tuple item : checkValSet) {
            String e = item.getElement();
            if (isNotNeedRemove(redisUtil, e)) {
                continue;
            }
            setList_OneSure.add(item);
        }
        //第二次确认元素在比对过程中是否发生过改变
        Set<Tuple> setList_TwoSure = new HashSet<>();
        for (Tuple item : setList_OneSure) {
            String e = item.getElement();
            Double score = redisUtil.zscoreExtByRedisUrl(redisUrl, keyEnum.getCreateTimeName(), e);
            if (score == null) {
                continue;
            }
            if (score.longValue() == item.getScore()) {
                setList_TwoSure.add(item);
                //当前消息中不存在此消息同时当前正在运行消息中也不存在，则进行删除set中的消息
                //先删除set集合中的值，再删除sort set集合中的值，确保值一定被删除
                redisUtil.sremExt(keyEnum.getSetName(), e);
                redisUtil.zremExt(keyEnum.getCreateTimeName(), e);
            }
        }
        //主要用于测试时统计有哪些值被排除掉了。-在生产环境中这里是可以注释掉的。
        for (Tuple item : setList_TwoSure) {
            //主要用于测试时统计有哪些值被排除掉了。
            redisUtil.lpushExt("TEST_TMP_LIST", item.getElement());
        }
        //删除所有临时
        redisUtil.lremExtByRedisUrl(redisUrl, keyEnum.getListName(), 0, SharedRedisConfig.CHECK_IS_EXIST_TEMP_VAL);
        //任务余量超过阀值预警通知-获取五分钟前的任务余量
        Long countTask = redisUtil.zcountExtByRedisUrl(redisUrl, keyEnum.getCreateTimeName(), 0, after5Timestamp);
        long countMax = 200L;
        StringBuilder messageBuilder = new StringBuilder();
        if (countTask > countMax) {
            messageBuilder.append("任务余量超过阀值,发生任务堆积，触发预警通知");
            messageBuilder.append("; 当前检查的任务创建时间=" + taskCreateTime);
            messageBuilder.append("; 任务余量=" + countTask);
            messageBuilder.append("; 任务阀值=" + countMax);
            messageBuilder.append("; redis配置信息=" + redisUrl);
            System.out.println(messageBuilder.toString());
            //此处可以接钉钉。这样可以实时预警
        }
    }

    private boolean isNotNeedRemove(ShardedRedisMqUtil redisUtil, String e) {
        //返回值是【-1则pivot不存在】【0则当前list集合不存在】
        Long linsertLong = redisUtil.linsertExt(keyEnum.getListName(), BinaryClient.LIST_POSITION.AFTER, e, SharedRedisConfig.CHECK_IS_EXIST_TEMP_VAL);
        if (linsertLong > 0) {
            redisUtil.lremExt2(keyEnum.getListName(), 0, e, SharedRedisConfig.CHECK_IS_EXIST_TEMP_VAL);
            return true;
        }
        // 验证是否当前消息正在运行
        boolean isExistsMutesKey = RedisJobMutesKeyUtil.exists(keyEnum, e);
        if (isExistsMutesKey) {
            return true;
        }
        return false;
    }

}
