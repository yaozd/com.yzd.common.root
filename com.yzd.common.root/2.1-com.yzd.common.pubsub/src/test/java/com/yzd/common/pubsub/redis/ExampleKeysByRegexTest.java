package com.yzd.common.pubsub.redis;

import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by zd.yao on 2017/12/18.
 */
public class ExampleKeysByRegexTest {
    /**
     *java代码怎么正则删除redis的数据
     * 参考：https://zhidao.baidu.com/question/1672667543364415227.html
     * 使用场景：
     * 如果当前的缓存数据有时间版的概念的话
     * 在更新缓存数据的时间戳的先通过正则删除上一个时间戳的缓存的数据。
     * 注：因为缓存是分布时的所以查找时也要多个redis库进行查找后删除
     */
    @Test
    public void example_getKeysByRegex(){
        String redisUrl = "redis://127.0.0.1:6379/1";
        List<JedisShardInfo> shardedPoolList = new ArrayList<JedisShardInfo>();
        JedisShardInfo Jedisinfo = new JedisShardInfo(redisUrl);
        shardedPoolList.add(Jedisinfo);
        ShardedJedis shardedJedis = new ShardedJedis(shardedPoolList);
        String pre_str="P03.CLEAN.CACHE:";
        Jedis jedis = shardedJedis.getShard(pre_str);
        try {
            Set<String> set = jedis.keys(pre_str +"*");
            Iterator<String> it = set.iterator();
            while(it.hasNext()){
                String keyStr = it.next();
                System.out.println(keyStr);
                jedis.del(keyStr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null)
                jedis.close();
        }
    }
}
