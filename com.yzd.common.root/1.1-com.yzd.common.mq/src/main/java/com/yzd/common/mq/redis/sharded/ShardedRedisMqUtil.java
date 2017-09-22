package com.yzd.common.mq.redis.sharded;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2017/3/6.
 */
public class ShardedRedisMqUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShardedRedisMqUtil.class);
    private static class SingletonHolder {
        private static final ShardedRedisMqUtil INSTANCE = new ShardedRedisMqUtil();
    }
    public static final ShardedRedisMqUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }
    private ShardedRedisMqUtil (){
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
        String[] redisUrlList=redisUrls.split(DEFAULT_REDIS_SEPARATOR);
        for (String redisUrl : redisUrlList) {
            JedisShardInfo Jedisinfo = new JedisShardInfo(redisUrl);
            Jedisinfo.setConnectionTimeout(timeout);
            Jedisinfo.setSoTimeout(timeout);
            shardedPoolList.add(Jedisinfo);
        }
        this.shardedJedisPool = new ShardedJedisPool(poolConfig, shardedPoolList, Hashing.MURMUR_HASH);
        //
        // 设置jedis连接池配置
        JedisPoolConfig poolConfig_Map = new JedisPoolConfig();
        poolConfig.setMaxTotal(SharedRedisConfig.shardedJedisPoolMap_JedisPoolSize);
        poolConfig.setMaxIdle(SharedRedisConfig.shardedJedisPoolMap_JedisPoolSize);
        poolConfig.setMinIdle(3);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        poolConfig.setTestOnBorrow(testOnBorrow);
        shardedJedisPoolMap=new ConcurrentHashMap<String, ShardedJedisPool>();
        for (String redisUrl:redisUrlList){
            JedisShardInfo Jedisinfo = new JedisShardInfo(redisUrl);
            Jedisinfo.setConnectionTimeout(timeout);
            Jedisinfo.setSoTimeout(timeout);
            shardedJedisPoolMap.put(redisUrl,new ShardedJedisPool(poolConfig_Map, Arrays.asList(Jedisinfo), Hashing.MURMUR_HASH));
        }
        //
    }
    private String DEFAULT_REDIS_SEPARATOR = ";";
    private ShardedJedisPool shardedJedisPool = null;
    private  Map<String,ShardedJedisPool> shardedJedisPoolMap=null;
    public List<String> getAllRedisUrls(){
        return new ArrayList<String>(shardedJedisPoolMap.keySet());
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

    /**
     *  这是一种特殊情况-以Redis作为消息队列-并且队列内容特别的大
     *  这里会以List中的value的值做为分片的信息
     *  这样就可以实现水平扩展
     *  主要解决redis作为消息队列时出现数据倾斜的问题
     * @param key
     * @param executor
     * @param <T>
     * @return
     */
    public <T> T execute(String redisUrl,String key, ShardedJedisPoolExecutor<T> executor) {
        ShardedJedis jedis =shardedJedisPoolMap.get(redisUrl).getResource();
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

    /**************************** redis 列表List start***************************/
    /**
     * 将一个值插入到列表头部，value可以重复，返回列表的长度
     *
     * @param key
     * @param value String
     * @return 返回List的长度
     */
    public Long lpush(final String key, final String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long length = jedis.lpush(key, value);
                return length;
            }
        });
    }

    /**
     * 将多个值插入到列表头部，value可以重复
     *
     * @param key
     * @param values String[]
     * @return 返回List的数量size
     */
    public Long lpush(String key, String[] values) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long size = jedis.lpush(key, values);
                return size;
            }
        });
    }

    /**
     * 获取List列表
     *
     * @param key
     * @param start long，开始索引
     * @param end   long， 结束索引
     * @return List<String>
     */
    public List<String> lrange(String key, long start, long end) {
        return execute(key, new ShardedRedisExecutor<List<String>>() {
            @Override
            public List<String> execute(ShardedJedis jedis) {
                List<String> list = jedis.lrange(key, start, end);
                return list;
            }
        });
    }

    /**
     * 通过索引获取列表中的元素
     *
     * @param key
     * @param index，索引，0表示最新的一个元素
     * @return String
     */
    public String lindex(String key, long index) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                String str = jedis.lindex(key, index);
                return str;
            }
        });
    }

    /**
     * 获取列表长度，key为空时返回0
     *
     * @param key
     * @return Long
     */
    public Long llen(String key) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long length = jedis.llen(key);
                return length;
            }
        });
    }

    /**
     * 在列表的元素前或者后插入元素，返回List的长度
     * 返回值是【-1则pivot不存在】【0则当前list集合不存在】
     * @param key
     * @param where LIST_POSITION
     * @param pivot 以该元素作为参照物，是在它之前，还是之后（pivot：枢轴;中心点，中枢;[物]支点，支枢;[体]回转运动。）
     * @param value
     * @return Long
     */
    public Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot, String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long length = jedis.linsert(key, where, pivot, value);
                return length;
            }
        });
    }

    /**
     * 将一个或多个值插入到已存在的列表头部，当成功时，返回List的长度；当不成功（即key不存在时，返回0）
     *
     * @param key
     * @param value String
     * @return Long
     */
    public Long lpushx(String key, String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long length = jedis.lpushx(key, value);
                return length;
            }
        });
    }

    /**
     * 移除列表元素，返回移除的元素数量
     *
     * @param key
     * @param count，标识，表示动作或者查找方向 <li>当count=0时，移除所有匹配的元素；</li>
     *                            <li>当count为负数时，移除方向是从尾到头；</li>
     *                            <li>当count为正数时，移除方向是从头到尾；</li>
     * @param value               匹配的元素
     * @return Long
     */
    public Long lrem(String key, long count, String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long length = jedis.lrem(key, count, value);
                return length;
            }
        });
    }

    /**
     * 通过索引设置列表元素的值，当超出索引时会抛错。成功设置返回true
     *
     * @param key
     * @param index 索引
     * @param value
     * @return boolean
     */
    public boolean lset(String key, long index, String value) {
        return execute(key, jedis -> {
            String statusCode = jedis.lset(key, index, value);
            if ("ok".equalsIgnoreCase(statusCode)) {
                return true;
            }
            return false;
        });
    }

    /**
     * 对一个列表进行修剪(trim)，就是说，让列表只保留指定区间内的元素，不在指定区间之内的元素都将被删除。
     *
     * @param key
     * @param start <li>可以为负数（-1是列表的最后一个元素，-2是列表倒数第二的元素。）</li>
     *              <li>如果start大于end，则返回一个空的列表，即列表被清空</li>
     * @param end   <li>可以为负数（-1是列表的最后一个元素，-2是列表倒数第二的元素。）</li>
     *              <li>可以超出索引，不影响结果</li>
     * @return boolean
     */
    public boolean ltrim(String key, long start, long end) {
        return execute(key, jedis -> {
            String statusCode = jedis.ltrim(key, start, end);
            if ("ok".equalsIgnoreCase(statusCode)) {
                return true;
            }
            return false;
        });
    }

    /**
     * 移出并获取列表的第一个元素，当列表不存在或者为空时，返回Null
     *
     * @param key
     * @return String
     */
    public String lpop(String key) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                String value = jedis.lpop(key);
                return value;
            }
        });
    }

    /**
     * 移除并获取列表最后一个元素，当列表不存在或者为空时，返回Null
     *
     * @param key
     * @return String
     */
    public String rpop(String key) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                String value = jedis.rpop(key);
                return value;
            }
        });
    }

    /**
     * 在列表中的尾部添加一个个值，返回列表的长度
     *
     * @param key
     * @param value
     * @return Long
     */
    public Long rpush(String key, String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long length = jedis.rpush(key, value);
                return length;
            }
        });
    }

    /**
     * 在列表中的尾部添加多个值，返回列表的长度
     *
     * @param key
     * @param values
     * @return Long
     */
    public Long rpush(String key, String[] values) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long length = jedis.rpush(key, values);
                return length;
            }
        });
    }

    /**
     * 仅当列表存在时，才会向列表中的尾部添加一个值，返回列表的长度
     *
     * @param key
     * @param value
     * @return Long
     */
    public Long rpushx(String key, String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long length = jedis.rpushx(key, value);
                return length;
            }
        });
    }

    /**
     * 移出并获取列表的【第一个元素】， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     *
     * @param timeout 单位为秒
     * @param key     <li>当有多个key时，只要某个key值的列表有内容，即马上返回，不再阻塞。</li>
     *                <li>当所有key都没有内容或不存在时，则会阻塞，直到有值返回或者超时。</li>
     *                <li>当超期时间到达时，keys列表仍然没有内容，则返回Null</li>
     * @return List<String>
     */
    public String blpop(int timeout, String key) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                List<String> value = jedis.blpop(timeout, key);
                if (value == null || value.isEmpty() || value.size() < 2) return null;
                return value.get(1);
            }
        });
    }

    /**
     * 移出并获取列表的【最后一个元素】， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。
     *
     * @param timeout 单位为秒
     * @param key     <li>当有多个key时，只要某个key值的列表有内容，即马上返回，不再阻塞。</li>
     *                <li>当所有key都没有内容或不存在时，则会阻塞，直到有值返回或者超时。</li>
     *                <li>当超期时间到达时，keys列表仍然没有内容，则返回Null</li>
     * @return List<String>
     */
    public String brpop(int timeout, String key) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                List<String> value = jedis.brpop(timeout, key);
                if (value == null || value.isEmpty() || value.size() < 2) return null;
                return value.get(1);
            }
        });
    }
    /**************************** redis 列表List end***************************/
    /**************************** redis 集合Set start***************************/
    /**Redis的Set是string类型的无序集合。集合成员是唯一的，这就意味着集合中不能出现重复的数据。**/
    /**
     * 向集合添加一个或多个成员，返回添加成功的数量
     *
     * @param key
     * @param members
     * @return Long
     */
    public Long sadd(String key, String... members) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long values = jedis.sadd(key, members);
                return values;
            }
        });
    }

    /**
     * 获取集合的成员数
     *
     * @param key
     * @return
     */
    public Long scard(String key) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long values = jedis.scard(key);
                return values;
            }
        });
    }

    /**
     * 返回集合中的所有成员
     *
     * @param key
     * @return Set<String>
     */
    public Set<String> smembers(String key) {
        return execute(key, new ShardedRedisExecutor<Set<String>>() {
            @Override
            public Set<String> execute(ShardedJedis jedis) {
                Set<String> values = jedis.smembers(key);
                return values;
            }
        });
    }

    /**
     * 判断 member 元素是否是集合 key 的成员，在集合中返回True
     *
     * @param key
     * @param member
     * @return Boolean
     */
    public Boolean sIsMember(String key, String member) {
        return execute(key, new ShardedRedisExecutor<Boolean>() {
            @Override
            public Boolean execute(ShardedJedis jedis) {
                Boolean value = jedis.sismember(key, member);
                return value;
            }
        });
    }

    /**
     * 移除集合中一个或多个成员
     *
     * @param key
     * @param members
     * @return
     */
    public boolean srem(String key, String... members) {
        //Integer reply, specifically: 1 if the new element was removed
        //0 if the new element was not a member of the set
        return execute(key, new ShardedRedisExecutor<Boolean>() {
            @Override
            public Boolean execute(ShardedJedis jedis) {
                Long value = jedis.srem(key, members);
                if (value > 0) {
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 返回集合中一个或多个随机数
     * <li>当count大于set的长度时，set所有值返回，不会抛错。</li>
     * <li>当count等于0时，返回[]</li>
     * <li>当count小于0时，也能返回。如-1返回一个，-2返回两个</li>
     *
     * @param key
     * @param count
     * @return List<String>
     */
    public List<String> srandMember(String key, int count) {
        return execute(key, new ShardedRedisExecutor<List<String>>() {
            @Override
            public List<String> execute(ShardedJedis jedis) {
                List<String> values = jedis.srandmember(key, count);
                return values;
            }
        });
    }

    /**
     * 移除并返回集合中的一个随机元素
     * <li>当set为空或者不存在时，返回Null</li>
     *
     * @param key
     * @return String
     */
    public String spop(String key) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                String value = jedis.spop(key);
                return value;
            }
        });
    }
    /**************************** redis 集合Set end***************************/

    /**************************** redis String start***************************/
    /**
     * Redis操作字符串工具类封装*
     */
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

    public Long del(final String key) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                return jedis.del(key);
            }
        });
    }
    /**************************** redis String end***************************/
    /**************************** redis 列表List扩展 start***************************/
    /**
     *  这是一种特殊情况-以Redis作为消息队列-并且队列内容特别的大
     *  这里会以List中的value的值做为分片的信息
     *  这样就可以实现水平扩展
     *  主要解决redis作为消息队列时出现数据倾斜的问题
     */
    /**
     * 将一个值插入到列表头部，value可以重复，返回列表的长度
     *
     * @param key
     * @param value String
     * @return 返回List的长度
     */
    public Long lpushExt(final String key, final String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                if (StringUtils.isBlank(value)) {
                    throw new IllegalStateException("[lpushExt]的value参数不能为空");
                }
                String valueKey = value.trim();
                Jedis j = (Jedis) shardedJedis.getShard(valueKey);
                Long length = j.lpush(key, value);
                return length;
            }
        });
    }
    /**
     * 将一个值插入到列表尾部，value可以重复，返回列表的长度
     *
     * @param key
     * @param value String
     * @return 返回List的长度
     */
    public Long rpushExt(final String key, final String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                if (StringUtils.isBlank(value)) {
                    throw new IllegalStateException("[rpushExt]的value参数不能为空");
                }
                String valueKey = value.trim();
                Jedis j = (Jedis) shardedJedis.getShard(valueKey);
                Long length = j.rpush(key, value);
                return length;
            }
        });
    }
    /**
     * 获得所有的Jedis的实例
     * 暂时无用
     */
    public Collection<Jedis> getAllShards() {
        return execute("", new ShardedRedisExecutor<Collection<Jedis>>() {
            @Override
            public Collection<Jedis> execute(ShardedJedis shardedJedis) {
                return shardedJedis.getAllShards();
            }
        });
    }
    /**
     * 获得所有redis分片信息（一致hash中生成的所有虚拟redis数据库）
     * 暂时无用
     * @return
     */
    public Collection<JedisShardInfo> getAllShardInfo() {
        return execute("", new ShardedRedisExecutor<Collection<JedisShardInfo>>() {
            @Override
            public Collection<JedisShardInfo> execute(ShardedJedis shardedJedis) {
                return shardedJedis.getAllShardInfo();
            }
        });
    }
    /**
     * 移出并获取列表的【最后一个元素】
     * @param key
     * @param timeout 超时时间单位是秒
     * @return
     */
    public String brpopExt(String redisUrl, String key,int timeout){
        return execute(redisUrl,key, new ShardedJedisPoolExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                List<String> value = jedis.brpop(timeout, key);
                if (value == null || value.isEmpty() || value.size() < 2) return null;
                return value.get(1);
            }
        });
    }
    /**
     * 移出并获取列表的【最前面的第一个元素】
     * @param key
     * @param timeout 超时时间单位是秒
     * @return
     */
    public String blpopExt(String redisUrl, String key,int timeout){
        return execute(redisUrl, key, new ShardedJedisPoolExecutor<String>() {
            @Override
            public String execute(ShardedJedis jedis) {
                List<String> value = jedis.blpop(timeout, key);
                if (value == null || value.isEmpty() || value.size() < 2) return null;
                return value.get(1);
            }
        });
    }
    public Long saddExt(final String key, final String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                if (StringUtils.isBlank(value)) {
                    throw new IllegalStateException("[saddExt]的value参数不能为空");
                }
                String valueKey = value.trim();
                Jedis j = (Jedis) shardedJedis.getShard(valueKey);
                Long length = j.sadd(key, value);
                return length;
            }
        });
    }
    public Boolean sremExt(final String key, final String value) {
        return execute(key, new ShardedRedisExecutor<Boolean>() {
            @Override
            public Boolean execute(ShardedJedis shardedJedis) {
                if (StringUtils.isBlank(value)) {
                    throw new IllegalStateException("[sremExt]的value参数不能为空");
                }
                String valueKey = value.trim();
                Jedis j = (Jedis) shardedJedis.getShard(valueKey);
                Long length = j.srem(key, value);
                if (length > 0) {
                    return true;
                }
                return false;
            }
        });
    }
    public Long scardExt(String redisUrl, String key){
        return execute(redisUrl, key, new ShardedJedisPoolExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long value = jedis.scard(key);
                return value;
            }
        });
    }
    public List<String> srandMemberExt(String redisUrl, String key,int count){
        return execute(redisUrl, key, new ShardedJedisPoolExecutor<List<String>>() {
            @Override
            public List<String> execute(ShardedJedis jedis) {
                List<String> values = jedis.srandmember(key, count);
                return values;
            }
        });
    }
    public Long lremExt(final String key,final long count, final String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                if (StringUtils.isBlank(value)) {
                    throw new IllegalStateException("[lremExt]的value参数不能为空");
                }
                String valueKey = value.trim();
                Jedis j = (Jedis) shardedJedis.getShard(valueKey);
                Long length = j.lrem(key, count, value);
                return length;
            }
        });
    }
    public String setExt(String shardValue,final String key, final String value, final String nxxx, final String expx, final long time) {
        return execute(key, new ShardedRedisExecutor<String>() {
            @Override
            public String execute(ShardedJedis shardedJedis) {
                if (StringUtils.isBlank(shardValue)) {
                    throw new IllegalStateException("[setExt]的value参数不能为空");
                }
                String valueKey = shardValue.trim();
                Jedis j = (Jedis) shardedJedis.getShard(valueKey);
                return j.set(key, value, nxxx, expx, time);
            }
        });
    }
    public Long delExt(String shardValue,final String key) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                if (StringUtils.isBlank(shardValue)) {
                    throw new IllegalStateException("[delExt]的value参数不能为空");
                }
                String valueKey = shardValue.trim();
                Jedis j = (Jedis) shardedJedis.getShard(valueKey);
                return j.del(key);
            }
        });
    }
    public Boolean existsExt(String shardValue,final String key) {
        return execute(key, new ShardedRedisExecutor<Boolean>() {
            @Override
            public Boolean execute(ShardedJedis shardedJedis) {
                if (StringUtils.isBlank(shardValue)) {
                    throw new IllegalStateException("[existsExt]的value参数不能为空");
                }
                String valueKey = shardValue.trim();
                Jedis j = (Jedis) shardedJedis.getShard(valueKey);
                return j.exists(key);
            }
        });
    }
    /**************************** redis 列表List扩展 end***************************/
    public Boolean sIsMemberExtByRedisUrl(String redisUrl, String key, String member){
        return execute(redisUrl, key, new ShardedJedisPoolExecutor<Boolean>() {
            @Override
            public Boolean execute(ShardedJedis jedis) {
                Boolean value = jedis.sismember(key, member);
                return value;
            }
        });
    }
    public Long delExtByRedisUrl(String redisUrl, String key){
        return execute(redisUrl, key, new ShardedJedisPoolExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis jedis) {
                Long value = jedis.del(key);
                return value;
            }
        });
    }
    public Long saddExtByRedisUrl(String redisUrl,final String key, String...  value) {
        return execute(redisUrl,key, new ShardedJedisPoolExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                Long length = shardedJedis.sadd(key, value);
                return length;
            }
        });
    }
    public Long linsertExt(String key, BinaryClient.LIST_POSITION where, String pivot, String value) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                if (StringUtils.isBlank(pivot)) {
                    throw new IllegalStateException("[linsertExt]的value参数不能为空");
                }
                String valueKey = pivot.trim();
                Jedis j = (Jedis) shardedJedis.getShard(valueKey);
                Long length = j.linsert(key, where, pivot, value);
                return length;
            }
        });
    }
    public Long lremExt2(final String key,final long count, final String value,String check) {
        return execute(key, new ShardedRedisExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                if (StringUtils.isBlank(value)) {
                    throw new IllegalStateException("[lremExt]的value参数不能为空");
                }
                String valueKey = value.trim();
                Jedis j = (Jedis) shardedJedis.getShard(valueKey);
                Long length = j.lrem(key, count, check);
                return length;
            }
        });
    }
    public Long lremExtByRedisUrl(String redisUrl,final String key,final long count, final String value) {
        return execute(redisUrl,key, new ShardedJedisPoolExecutor<Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                Long length = shardedJedis.lrem(key, count, value);
                return length;
            }
        });
    }
}
