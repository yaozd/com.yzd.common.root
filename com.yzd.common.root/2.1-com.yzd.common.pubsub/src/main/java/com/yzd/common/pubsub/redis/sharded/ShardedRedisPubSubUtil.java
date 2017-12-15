package com.yzd.common.pubsub.redis.sharded;

import com.yzd.common.pubsub.redis.utils.LocalIpAddressUtil;
import com.yzd.common.pubsub.redis.utils.TimeUtil2;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.util.Hashing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by zd.yao on 2017/12/11.
 */
public class ShardedRedisPubSubUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardedRedisPubSubUtil.class);

    private static final String DEFAULT_REDIS_SEPARATOR = ";";

    private static final String HOST_PORT_SEPARATOR = ":";
    private static final String WEIGHT_SEPARATOR = "\\*";
    private ShardedJedisPool shardedJedisPool = null;

    private static final ShardedRedisPubSubUtil INSTANCE = new ShardedRedisPubSubUtil();

    private ShardedRedisPubSubUtil() {
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

    public static ShardedRedisPubSubUtil getInstance() {
        return INSTANCE;
    }
    //当前启动编号
    private static final String CurrentNum=getCurrentNum();
    private static String getCurrentNum(){
        Date date = new Date();
        return String.valueOf(date.getTime());
    }
    //subZSet订阅者集合过期时间1小时
    private static final Integer SubZSetExpireTime=60*60;
    //订阅者规则2如果15分钟之内没有心跳则从SubZSet集合中移除
    private static final Integer SubZSetItemDeletedTime=60*15;
    //接收的监听集合过期时间30分钟
    private static final Integer PubSubListExpireTime=60*30;
    //
    /**
     * 实现jedis连接的获取和释放，具体的redis业务逻辑由executor实现
     *
     * @param executor RedisExecutor接口的实现类
     * @return
     */
    public <T> T execute(ShardedRedisExecutor<T> executor) {
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
    //
    public String set(final String key, final String value) {
        return execute(new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                return jedis.set(key, value);
            }
        });
    }
    public Long zadd(String key, double score, String member) {
        return execute(new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.zadd(key, score, member);
            }
        });
    }
    public Set<String> zrange(String key, long start, long end) {
        return execute(new ShardedRedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(ShardedJedis jedis) {
                return jedis.zrange(key, start, end);
            }
        });
    }
    public Double zscore(String key, String member) {
        return execute(new ShardedRedisExecutor<Double>() {
            @Override
            public Double execute(ShardedJedis jedis) {
                return jedis.zscore(key, member);
            }
        });
    }
    public Long zrem(String key, String... members) {
        return execute(new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.zrem(key, members);
            }
        });
    }
    public Long rpush(String key, String... strings) {
        return execute(new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.rpush(key, strings);
            }
        });
    }
    public Long expire(String key, int seconds) {
        return execute(new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.expire(key, seconds);
            }
        });
    }
    public List<String> blpop(int timeout, String key) {
        return execute(new ShardedRedisExecutor<List<String>>() {
            @Override
            public List<String> execute(ShardedJedis jedis) {
                return jedis.blpop(timeout, key);
            }
        });
    }
    //订阅者
    public void subClient(String channel){
        String subZSetName=getSubZSetName(channel);
        String member = getMember();
        Double score = TimeUtil2.getTimeOfSecond();
        zadd(subZSetName, score, member);
        expire(subZSetName,SubZSetExpireTime);
    }
    //发送者
    public void pubClient(String channel,String command){
        String subZSetName=getSubZSetName(channel);
        Set<String> memberSet = zrange(subZSetName, 0l, Integer.MAX_VALUE);
        for (String item : memberSet) {
            Double val = zscore(subZSetName, item);
            //
            int diffVal= TimeUtil2.getDiffToCurrentTime(val);
            if(diffVal>SubZSetItemDeletedTime){
                zrem(subZSetName, item);
                continue;
            }
            //list集合名
            String listKey=getPubListName(channel, item);
            //在做为缓存清除通知的情况下，是可以增加KEY的过期时间的
            rpush(listKey, command);
            expire(listKey, PubSubListExpireTime);
        }
    }
    //订阅者接收监听
    public String pubSubListener(String channel,int timeoutSecond){
        String member=getMember();
        List<String> valueList = blpop(timeoutSecond, getPubListName(channel,member));
        if(valueList.size()==0){
            return null;
        }
        return valueList.get(1);
    }
    private String getMember(){
        //member=本机IP+本机起动的时间
        //可以解决相用的程序在同一PC打开多个的情况
        return LocalIpAddressUtil.resolveLocalAddress().getHostAddress()+"#"+CurrentNum;
    }
    //P03.CLEAN.CACHE:0SubZSet
    private String getSubZSetName(String channel){
        return channel+":"+"0SubZSet";
    }
    //P03.CLEAN.CACHE:192.168.1.81#1513305571792
    private String getPubListName(String channel,String member){
        return  channel+":"+member;
    }
}
