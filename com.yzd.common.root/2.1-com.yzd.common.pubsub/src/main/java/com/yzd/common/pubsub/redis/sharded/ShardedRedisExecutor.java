package com.yzd.common.pubsub.redis.sharded;

import redis.clients.jedis.ShardedJedis;

/**
 * Created by zd.yao on 2017/12/11.
 */
// redis具体逻辑接口
public interface ShardedRedisExecutor<T> {
    T execute(ShardedJedis jedis);
}