package com.yzd.common.mq.redis.job.writer;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.sharded.ShardedRedisMqUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by zd.yao on 2017/8/29.
 */
public class RedisJobWriterUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisJobWriterUtil.class);
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
            if(logger.isDebugEnabled()){
                logger.debug("将数据写入到reids消息队列中result="+result);
            }
        }
    }
}
