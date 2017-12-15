package com.yzd.common.pubsub.example;

import com.yzd.common.pubsub.redis.sharded.ShardedRedisPubSubUtil;
import org.junit.Test;

import java.util.Date;

/**
 * Created by zd.yao on 2017/12/15.
 */
public class PubClientTest {
    private static final String channel="P03.CLEAN.CACHE";
    //发送的命令数据结构可以是json数据这样可以方便我们解析
    @Test
    public void pubClient_send(){
        //
        String command="发送的命令数据结构可以是json数据这样可以方便我们解析="+new Date().getTime();
        ShardedRedisPubSubUtil redisPubSubUtil=ShardedRedisPubSubUtil.getInstance();
        redisPubSubUtil.pubClient(channel,command);
    }
}
