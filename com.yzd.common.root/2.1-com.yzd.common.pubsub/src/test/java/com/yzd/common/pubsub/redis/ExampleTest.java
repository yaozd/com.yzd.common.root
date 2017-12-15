package com.yzd.common.pubsub.redis;

import com.yzd.common.pubsub.redis.sharded.ShardedRedisPubSubUtil;
import com.yzd.common.pubsub.redis.utils.LocalIpAddressUtil;
import com.yzd.common.pubsub.redis.utils.TimeUtil2;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by zd.yao on 2017/12/14.
 */
public class ExampleTest {
    /**
     *  //此操作可以使用spring boot 的调度任务作为多线程
     */
    @Test
    public void example_final() throws InterruptedException {
        //如果使用发面订阅功能同一个程序在同一个PC中只能部署一台，不可部署多台。
        //下面包含实现发布订阅所有的技术要点：
        String channel = "channel";
        String redisUrl = "redis://127.0.0.1:6379/1";
        List<JedisShardInfo> shardedPoolList = new ArrayList<JedisShardInfo>();
        JedisShardInfo Jedisinfo = new JedisShardInfo(redisUrl);
        shardedPoolList.add(Jedisinfo);
        ShardedJedis shardedJedis = new ShardedJedis(shardedPoolList);
        String member = LocalIpAddressUtil.resolveLocalAddress().getHostAddress();
        Double score = TimeUtil2.getTimeOfSecond();
        shardedJedis.zadd(channel, score, member);
        Set<String> memberSet = shardedJedis.zrange(channel, 0l, 2l);
        for (String item : memberSet) {
            Double val = shardedJedis.zscore(channel, item);
            BigDecimal b1 = new BigDecimal(val);
            System.out.println(b1);
            //
            TimeUnit.SECONDS.sleep(60);
            int diffVal= TimeUtil2.getDiffToCurrentTime(val);
            System.out.println(diffVal);
            //
            shardedJedis.zrem(channel, item);
            String listKey=getPubListName(channel, item);
            //在做为缓存清除通知的情况下，是可以增加KEY的过期时间的
            shardedJedis.rpush(listKey,"command");
            shardedJedis.expire(listKey,60*60);

        }
        while (true){
            List<String> valueList = shardedJedis.blpop(3, getPubListName(channel,member));
            if(valueList.size()==0){
                break;
            }
            //[channel:192.168.1.107, command]
            System.out.println(valueList.toString());
        }
        System.out.println("end");

    }
    private String getPubListName(String channel,String member){
        return  channel+":"+member;
    }
    //
    String channel="P03.CLEAN.CACHE";
    @Test
    public void subClientTest(){
        ShardedRedisPubSubUtil redisPubSubUtil=ShardedRedisPubSubUtil.getInstance();
        redisPubSubUtil.subClient(channel);
        redisPubSubUtil.subClient(channel);
        redisPubSubUtil.subClient(channel);
    }
    @Test
    public void pubClientTest(){
        String command="current time ="+new Date().getTime();
        ShardedRedisPubSubUtil redisPubSubUtil=ShardedRedisPubSubUtil.getInstance();
        redisPubSubUtil.pubClient(channel,command);
    }
    @Test
    public void pubSubListenerTest(){
        //理论上这里的while(true)也是要加try catch的，用于解决网络闪断的问题
        //这里也可以使用spring boot的调度任务来解决闪断后的再一次重新打开。
        while (true){
            ShardedRedisPubSubUtil redisPubSubUtil=ShardedRedisPubSubUtil.getInstance();
            String value=  redisPubSubUtil.pubSubListener(channel,5);
            if(StringUtils.isBlank(value)){
                continue;
            }
            System.out.println(value);
        }
    }
}
