package com.yzd.common.mq.redis.sharded;

import java.util.ResourceBundle;

/**
 * 读取redis配置
 * 
 * @author zhengzhiyuan
 * @since May 20, 2016
 */
public class SharedRedisConfig {
    private static final String DEFAULT_REDIS_PROPERTIES = "shardedRedisMQ";
    private static ResourceBundle REDIS_CONFIG = ResourceBundle.getBundle(DEFAULT_REDIS_PROPERTIES);
    //CHECK_IS_EXIST_TEMP_VAL 是临时检查的值不实际意义,只用于测试一个值是否存在
    public static final String CHECK_IS_EXIST_TEMP_VAL="CHECK_IS_EXIST_TEMP_VAL";
    protected static String getConfigProperty(String key) {
        return REDIS_CONFIG.getString(key);
    }
    //region 设置独立jedis线程池的大小=任务列表大小+7；如果不设置初始值则默认值为8；
    //这样可以保证jedis线程池根据任务列表的数据自动增长
    protected static int shardedJedisPoolMap_JedisPoolSize=8;
    public static void setJedisPoolSizeOfShardedJedisPoolMap(int poolSize){
        shardedJedisPoolMap_JedisPoolSize= 7+poolSize;
    }
    //endregion
}
