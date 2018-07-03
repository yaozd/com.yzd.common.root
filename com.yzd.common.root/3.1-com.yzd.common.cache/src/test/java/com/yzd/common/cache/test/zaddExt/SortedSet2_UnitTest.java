package com.yzd.common.cache.test.zaddExt;

import com.yzd.common.cache.redis.sharded.ShardedRedisUtil;
import com.yzd.common.cache.utils.fastjson.FastJsonUtil;
import org.junit.Test;
import redis.clients.jedis.Tuple;

import java.util.Set;

public class SortedSet2_UnitTest {
    @Test
    public void autoCreateTestData() {
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        for (int i = 0; i < 10; i++) {
            redisUtil.zadd("sortedSet", Math.random() * 100, "value:" + i);
        }
    }

    @Test
    public void testZRangeAndZRevRange() {
        /** zrange
         * 返回有序集 key 中，指定区间内的成员。
         其中成员的位置按 score 值递增(从小到大)来排序。 rev则相反
         具有相同 score 值的成员按字典序(lexicographical order )来排列。
         */
        ShardedRedisUtil redisUtil = ShardedRedisUtil.getInstance();
        Set<String> set = redisUtil.zrange("sortedSet", 0, 12222); //递增
        System.out.println(FastJsonUtil.serialize(set));
        Set<String> revSet = redisUtil.zrevrange("sortedSet", 0, -1); //递减
        System.out.println(FastJsonUtil.serialize(revSet));
        ///
        ///
        Set<Tuple> setWithScores = redisUtil.zrangeWithScores("sortedSet", 0, 12222); //递增
        System.out.println(FastJsonUtil.serialize(setWithScores));
        for (Tuple item : setWithScores) {
            System.out.println("item.getElement()=" + item.getElement() + ";" + "item.getScore()" + item.getScore());
        }

    }

}
