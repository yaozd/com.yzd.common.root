package com.yzd.common.cache.test.zaddExt;

import com.yzd.common.cache.redis.sharded.ShardedRedisUtil;
import org.junit.Test;

import java.security.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

public class SortedSet_UnitTest {
    //场景一：排行榜
    //场景二：定义更义处理，基于时间戳。定时间删除或更新
    String keyName="SortedSet.T1";
    @Test
    public void zadd_Test(){
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        for (int i = 0; i <100000 ; i++) {
            redisUtil.zadd(keyName,i,"A"+i);
        }
    }
    @Test
    public void zrem_Test(){
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        redisUtil.zrem(keyName,"A1");
    }
    @Test
    public void zrangeByScore_Test(){
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        Set<String> keySet= redisUtil.zrangeByScore(keyName,0,100);
        System.out.println(keySet.size());
    }
    @Test
    public void zscore_Test(){
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        Double d1=redisUtil.zscore(keyName,"ATimestamp");
        Double d2= Optional.ofNullable(d1).orElseGet(()->0.0);
        System.out.println(d2);
    }
    @Test
    public void t1_Test(){
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        redisUtil.zadd(keyName,dateToTimestamp(),"ATimestamp");
        //
        Set<String> keySet= redisUtil.zrangeByScore(keyName,0,100);
        System.out.println(keySet.size());
        Long timestamp=dateToTimestamp(new Date());
        System.out.println(timestamp);
        Date date=timestampToDate(timestamp);
        System.out.println(dateToString(date));
    }
    //定义更义处理，基于时间戳。定时间删除或更新
    //根据时间定时操作
    @Test
    public void t2_Test(){
        String member="ATimestamp";
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        //读取当前缓存中KEY的时间戳
        Double valInRedis= redisUtil.zscore(keyName,member);
        Long timestamp=dateToTimestamp(new Date());
        if(valInRedis==null||valInRedis>timestamp)
        {
            redisUtil.zadd(keyName,timestamp,member);
        }
        //时间减5分钟后的时间戳
        Long after5Timestamp=dateToTimestamp()-5*60*1000;
        String str1=dateToString(timestampToDate(after5Timestamp));
        System.out.println(str1);
        Set<String> keySet= redisUtil.zrangeByScore(keyName,0,after5Timestamp);
        System.out.println(keySet.size());
        //读取5分钟前插入的KEY
        for(String key:keySet){
            redisUtil.zrem(keyName,key);
            System.out.println(key);
        }
    }
    private Long dateToTimestamp(){
        return dateToTimestamp(new Date());
    }
    private Long dateToTimestamp(Date date){
        return date.getTime();
    }
    private Date timestampToDate(Long timestamp){
        return new Date(timestamp);
    }
    private String dateToString(Date date){
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.format(date);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
