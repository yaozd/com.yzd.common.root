package com.yzd.common.mq.redis.sharded;

import redis.clients.jedis.ShardedJedis;

/**
 *  这是一种特殊情况-以Redis作为消息队列-并且队列内容特别的大
 *  这里会以List中的value的值做为分片的信息
 *  这样就可以实现水平扩展
 *  主要解决redis作为消息队列时出现数据倾斜的问题
 * Created by zd.yao on 2017/7/7.
 */
public interface ShardedJedisPoolExecutor<T> {
    T execute(ShardedJedis jedis);
}