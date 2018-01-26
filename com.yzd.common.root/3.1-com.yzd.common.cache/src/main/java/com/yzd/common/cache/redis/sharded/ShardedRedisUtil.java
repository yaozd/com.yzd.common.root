package com.yzd.common.cache.redis.sharded;

import com.yzd.common.cache.utils.fastjson.FastJsonUtil;
import com.yzd.common.cache.utils.setting.CachedKeyUtil;
import com.yzd.common.cache.utils.setting.CachedSetting;
import com.yzd.common.cache.utils.wrapper.CachedWrapper;
import com.yzd.common.cache.utils.wrapper.CachedWrapperExecutor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 分片redis
 *
 * @author zhengzhiyuan
 * @since May 20, 2016
 */
public class ShardedRedisUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShardedRedisUtil.class);

    private static final String DEFAULT_REDIS_SEPARATOR = ";";

    private static final String HOST_PORT_SEPARATOR = ":";
    private static final String WEIGHT_SEPARATOR = "\\*";
    private ShardedJedisPool shardedJedisPool = null;

    private static final ShardedRedisUtil INSTANCE = new ShardedRedisUtil();

    private ShardedRedisUtil() {
        initialShardedPool();
    }

    private void initialShardedPool() {
        // 操作超时时间,默认2秒
        int timeout = NumberUtils.toInt(SharedRedisConfig.getConfigProperty("redis.timeout"), 2000);
        // jedis池最大连接数总数，默认8
        int maxTotal = NumberUtils.toInt(SharedRedisConfig.getConfigProperty("redis.jedisPoolConfig.maxTotal"), 8);
        // jedis池最大空闲连接数，默认8
        int maxIdle = NumberUtils.toInt(SharedRedisConfig.getConfigProperty("redis.jedisPoolConfig.maxIdle"), 8);
        // jedis池最少空闲连接数
        int minIdle = NumberUtils.toInt(SharedRedisConfig.getConfigProperty("redis.jedisPoolConfig.minIdle"), 0);
        // jedis池没有对象返回时，最大等待时间单位为毫秒
        long maxWaitMillis = NumberUtils.toLong(SharedRedisConfig.getConfigProperty("redis.jedisPoolConfig.maxWaitTime"), -1);
        // 在borrow一个jedis实例时，是否提前进行validate操作
        boolean testOnBorrow = Boolean.parseBoolean(SharedRedisConfig.getConfigProperty("redis.jedisPoolConfig.testOnBorrow"));

        // 设置jedis连接池配置
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        poolConfig.setTestOnBorrow(testOnBorrow);

        // 取得redis的url
        String redisUrls = SharedRedisConfig.getConfigProperty("redis.jedisPoolConfig.urls");
        if (redisUrls == null || redisUrls.trim().isEmpty()) {
            throw new IllegalStateException("the urls of redis is not configured");
        }
        LOGGER.info("the urls of redis is {}", redisUrls);
        // 生成连接池
        List<JedisShardInfo> shardedPoolList = new ArrayList<JedisShardInfo>();
        for (String redisUrl : redisUrls.split(DEFAULT_REDIS_SEPARATOR)) {
            JedisShardInfo Jedisinfo = new JedisShardInfo(redisUrl);
            Jedisinfo.setConnectionTimeout(timeout);
            Jedisinfo.setSoTimeout(timeout);
            shardedPoolList.add(Jedisinfo);
        }

        // 构造池
        this.shardedJedisPool = new ShardedJedisPool(poolConfig, shardedPoolList, Hashing.MURMUR_HASH);
    }

    public static ShardedRedisUtil getInstance() {
        return INSTANCE;
    }

    /**
     * 实现jedis连接的获取和释放，具体的redis业务逻辑由executor实现
     *
     * @param executor RedisExecutor接口的实现类
     * @return
     */
    public <T> T execute(String key, ShardedRedisExecutor<T> executor) {
        ShardedJedis jedis = shardedJedisPool.getResource();
        T result = null;
        try {
            result = executor.execute(jedis);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    public String set(final String key, final String value) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.set(key, value);
            }
        });
    }

    public String set(final String key, final String value, final String nxxx, final String expx, final long time) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.set(key, value, nxxx, expx, time);
            }
        });
    }

    public String get(final String key) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.get(key);
            }
        });
    }

    public Boolean exists(final String key) {
        return execute(key, new ShardedRedisExecutor<Boolean>() {
            @Override
            public Boolean execute(ShardedJedis jedis) {
                return jedis.exists(key);
            }
        });
    }

    public Long setnx(final String key, final String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.setnx(key, value);
            }
        });
    }

    public String setex(final String key, final int seconds, final String value) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.setex(key, seconds, value);
            }
        });
    }

    public Long expire(final String key, final int seconds) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.expire(key, seconds);
            }
        });
    }

    public Long incr(final String key) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.incr(key);
            }
        });
    }

    public Long decr(final String key) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.decr(key);
            }
        });
    }

    public Long hset(final String key, final String field, final String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.hset(key, field, value);
            }
        });
    }

    public String hget(final String key, final String field) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.hget(key, field);
            }
        });
    }

    public String hmset(final String key, final Map<String, String> hash) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.hmset(key, hash);
            }
        });
    }

    public List<String> hmget(final String key, final String... fields) {
        return execute(key, new ShardedRedisExecutor<List<String>>() {
            @Override
            public List<String> execute(ShardedJedis jedis) {
                return jedis.hmget(key, fields);
            }
        });
    }

    public Long del(final String key) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.del(key);
            }
        });
    }

    public Map<String, String> hgetAll(final String key) {
        return execute(key, new ShardedRedisExecutor<Map<String, String>>() {
            @Override
            public Map<String, String> execute(ShardedJedis jedis) {
                return jedis.hgetAll(key);
            }
        });
    }

    public void destroy() {
        this.shardedJedisPool.close();
    }

    //---------------------------------------------------------------------------------
    //扩展方法
    public <T> String set(final String key, T object) {
        String value = FastJsonUtil.serializeFormDate(object);
        return set(key, value);
    }

    public <T> T get(final String key, Class<T> clz) {
        String value = get(key);
        return FastJsonUtil.deserialize(value, clz);
    }

    public <T> String setex(final String key, final int seconds, T object) {
        String value = FastJsonUtil.serializeFormDate(object);
        return setex(key, seconds, value);
    }

    public <T> List<T> getList(final String key, Class<T> clz) {
        String value = get(key);
        return FastJsonUtil.deserializeList(value, clz);
    }

    public <T> String setCachedWrapper(final String key, final int seconds, T object) {
        return setex(key, seconds, new CachedWrapper<T>(object));
    }

    public <T> String setCachedWrapper(final String key, final int seconds, T object, final String timestamp) {
        return setex(key, seconds, new CachedWrapper<T>(object, timestamp));
    }

    public <T> CachedWrapper<T> getCachedWrapper(final String key) {
        CachedWrapper<T> getObj = get(key, CachedWrapper.class);
        return getObj;
    }

    /**
     * 读取并设置数据缓存
     * 通过互斥的锁来减少对数据库的访问
     * 互斥的锁-使用的redis-setNX的方法
     * 目前考虑的使用场景-缓存公共访问数据-更新机制-设置可容忍的过期时间
     * @param key                key
     * @param keyExpireSec       key的过期时间
     * @param nullValueExpireSec 查询结果为NULL值时的过期时间
     * @param keyMutexExpireSec  互斥key的过期时间(最大值为10秒,参考值为5秒)-互斥key的值取决于查询接口的响应时间
     * @param sleepMilliseconds  循环请求中-休眠的具体时间必要根据实际的情况做调整-目前暂定300毫秒不会影响到客户体验
     * @param executor           获取需要缓存的数据-从数据库或其他的地方查询
     * @return
     */
    public <T> CachedWrapper<T> getCachedWrapperByMutexKey(final String key,
                                                           final int keyExpireSec,
                                                           final int nullValueExpireSec,
                                                           final int keyMutexExpireSec,
                                                           final int sleepMilliseconds,
                                                           CachedWrapperExecutor<T> executor)  {
        if (StringUtils.isBlank(key)) throw new IllegalStateException("key值不能为空。");
        if (keyExpireSec < nullValueExpireSec) throw new IllegalStateException("key的过期时间必须大于查询结果为NULL值时的过期时间。");
        if (keyExpireSec < keyMutexExpireSec) throw new IllegalStateException("key的过期时间必须大于互斥key的过期时间。");
        if (keyMutexExpireSec > 10) throw new IllegalStateException("互斥key的过期时间必须小于10秒。");
        if (sleepMilliseconds > 2000) throw new IllegalStateException("循环请求sleep休眠时间必须小于2000毫秒。");
        CachedWrapper<T> value;
        String key_mutex = "mutexKey_" + key;
        //不需要对数据进行缓存
        if (keyExpireSec == 0 && nullValueExpireSec == 0 && keyMutexExpireSec == 0) {
            //获取需要缓存的数据-从数据库或其他的地方查询
            T result = executor.execute();
            value = new CachedWrapper<T>(result);
            return value;
        }
        while (true) {
            value = getCachedWrapper(key);
            //System.out.println(1); //debug
            if (value != null) return value;
            if (set(key_mutex, "1", "NX", "EX", keyMutexExpireSec) == null) {
                //休眠的具体时间必要根据实际的情况做调整
                //目前暂定300毫秒不会影响到客户体验
                try {
                    Thread.sleep(sleepMilliseconds);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //System.out.println(2); //debug
                continue;
            }
            try
            {
                //获取需要缓存的数据-从数据库或其他的地方查询
                T result = executor.execute();
                if (result == null) {
                    setCachedWrapper(key, nullValueExpireSec, null);
                } else {
                    setCachedWrapper(key, keyExpireSec, result);
                }
                //System.out.println(3); //debug
                value = new CachedWrapper<T>(result);
                return value;
            }
            finally
            {
                del(key_mutex);
            }
        }
    }

    public <T> CachedWrapper<T> getCachedWrapperByMutexKey(final String key,
                                                           final int keyExpireSec,
                                                           final int nullValueExpireSec,
                                                           final int keyMutexExpireSec,
                                                           CachedWrapperExecutor<T> executor)  {
        //休眠的具体时间必要根据实际的情况做调整
        //目前暂定300毫秒不会影响到客户体验
        return getCachedWrapperByMutexKey(key, keyExpireSec, nullValueExpireSec, keyMutexExpireSec, 300, executor);
    }
    //公共数据缓存使用方法。
    public <T> CachedWrapper<T> getPublicCachedWrapperByMutexKey(final CachedSetting cachedSetting,
                                                           final String where,
                                                           CachedWrapperExecutor<T> executor){
        //目前没有增加缓存访问次数的统计
        //以后增加访问次数的统计用与实现自动扩展
        //String key=cachedSetting.getKeyFullName()+ CachedKeyUtil.KeyMd5(where);
        String whereFullVal=cachedSetting.getVersion()+"|"+where;
        String key=cachedSetting.getKeyFullName()+ CachedKeyUtil.KeyMd5(whereFullVal);
        //休眠的具体时间必要根据实际的情况做调整
        //目前暂定300毫秒不会影响到客户体验
        return getCachedWrapperByMutexKey(key, cachedSetting.getKeyExpireSec(),cachedSetting.getNullValueExpireSec(),cachedSetting.getKeyMutexExpireSec(),cachedSetting.getSleepMilliseconds(), executor);
    }

    /**
     * 读取并设置数据缓存
     * 通过缓存数据-数据对比时间戳来判断数据是否更新
     * 缓存数据-数据对比时间戳（时间戳可以使用自增时间节点或是UUID，主要是体现数据发生更改，也可以使用UUID+时间节点这样可读性会好一点）
     * "timestamp": "2017-01-18 02:44:41|212cb6a7-5eb7-4b2e-995b-405aa0dcf9ad"
     * 目前考虑的使用场景-缓存个人用户的全局信息-但需要设计合理的个人用户信息更新机制
     * 缓存数据周期长--例如一天
     * @param key                key
     * @param keyExpireSec       key的过期时间
     * @param nullValueExpireSec 查询结果为NULL值时的过期时间
     * @param timestamp          缓存数据-数据对比时间戳
     * @param executor           获取需要缓存的数据-从数据库或其他的地方查询
     * @return
     */
    public <T> CachedWrapper<T> getCachedWrapperByTimestamp(final String key,
                                                            final int keyExpireSec,
                                                            final int nullValueExpireSec,
                                                            final String timestamp,
                                                            CachedWrapperExecutor<T> executor){
        if (StringUtils.isBlank(key)) throw new IllegalStateException("key值不能为空。");
        if (StringUtils.isBlank(timestamp)) throw new IllegalStateException("缓存数据对比时间戳timestamp值不能为空。");
        if (keyExpireSec < nullValueExpireSec) throw new IllegalStateException("key的过期时间必须大于查询结果为NULL值时的过期时间。");
        CachedWrapper<T> value;
        //不需要对数据进行缓存
        if (keyExpireSec == 0 && nullValueExpireSec == 0) {
            //获取需要缓存的数据-从数据库或其他的地方查询
            T result = executor.execute();
            value = new CachedWrapper<T>(result);
            return value;
        }
        value = getCachedWrapper(key);
        //System.out.println(1); //debug
        if (value != null && timestamp.equals(value.getTimestamp())) return value;
        //获取需要缓存的数据-从数据库或其他的地方查询
        T result = executor.execute();
        if (result == null) {
            setCachedWrapper(key, nullValueExpireSec, null, timestamp);
        } else {
            setCachedWrapper(key, keyExpireSec, result, timestamp);
        }
        //System.out.println(3); //debug
        value = new CachedWrapper<T>(result);
        return value;
    }
}
