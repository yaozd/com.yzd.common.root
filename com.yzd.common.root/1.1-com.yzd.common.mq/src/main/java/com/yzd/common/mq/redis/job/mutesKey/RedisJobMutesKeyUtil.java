package com.yzd.common.mq.redis.job.mutesKey;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;
import org.apache.commons.lang.ObjectUtils;

/**
 * Created by zd.yao on 2017/8/29.
 */
public class RedisJobMutesKeyUtil {
    /**
     * 设置任务运行时的互斥锁-记录正在运行的任务-有效时间为5分钟-任务完成后主动删除MutesKey
     * @param keyEnum
     * @param val
     * @return
     */
    public static Boolean set(JobEnum keyEnum, String val){
        String mutexKey = getMutexKey(keyEnum, val);
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        //设置任务正在运行-有效时间为5分钟
        String isOk = redisUtil.setExt(val, mutexKey, "1", "NX", "EX", 60 * 5);
        if (ObjectUtils.notEqual("OK",isOk)) {
            return false;
        }
        return true;
    }

    /**
     * 先删除set排除消息对列再删除mutexKey互斥锁
     * @param keyEnum
     * @param val
     * @return
     */
    public static Boolean del(JobEnum keyEnum, String val){
        String mutexKey = getMutexKey(keyEnum, val);
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        //先删除set排除消息对列再删除mutexKey互斥锁
        Long delNum= redisUtil.delExt(val,mutexKey);
        //删除任务的创建时间集合中的对象
        redisUtil.zremExt(keyEnum.getCreateTimeName(),val);
        //最后删除SET
        redisUtil.sremExt(keyEnum.getSetName(), val);
        if(delNum==0){
            return false;
        }
        return true;
    }
    public  static Boolean exists(JobEnum keyEnum, String val) {
        String mutexKey = getMutexKey(keyEnum, val);
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        return redisUtil.existsExt(val,mutexKey);
    }

    private static String getMutexKey(JobEnum keyEnum, String val) {
        return keyEnum.getMutesKeyName()+val;
    }
}
