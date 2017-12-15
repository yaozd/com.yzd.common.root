package com.yzd.common.pubsub.redis.sharded;

import java.util.ResourceBundle;

/**
 * Created by zd.yao on 2017/12/11.
 */
public class SharedRedisConfig {
    private static final String DEFAULT_REDIS_PROPERTIES = "shardedRedisPubSub";
    private static ResourceBundle REDIS_CONFIG = ResourceBundle.getBundle(DEFAULT_REDIS_PROPERTIES);
    protected static String getConfigProperty(String key) {
        return REDIS_CONFIG.getString(key);
    }
}
