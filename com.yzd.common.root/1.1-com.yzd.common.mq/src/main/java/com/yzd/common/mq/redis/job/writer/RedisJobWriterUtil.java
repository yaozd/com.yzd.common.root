package com.yzd.common.mq.redis.job.writer;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;

/**
 * Created by zd.yao on 2017/8/29.
 */
public class RedisJobWriterUtil {
    /**
     * 写入消息
     * @param keyEnum
     * @param val
     */
    public static void write(JobEnum keyEnum, String val) {
        ShardedRedisMqUtil redisUtil = ShardedRedisMqUtil.getInstance();
        //先插入到排重set消息队列中
        long countOfsadd = redisUtil.saddExt(keyEnum.getSetName(), val);
        //判断当前消息是否存在-如果当前消息不存在，则插入到list消息队列中
        if (countOfsadd == 1){
            //常规操作-从尾部插入
            Long result = redisUtil.rpushExt(keyEnum.getListName(), val);
            System.out.println(result);
        }
    }
}
