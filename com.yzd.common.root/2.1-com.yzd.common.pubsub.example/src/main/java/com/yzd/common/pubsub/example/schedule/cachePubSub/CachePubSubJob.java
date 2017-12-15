package com.yzd.common.pubsub.example.schedule.cachePubSub;

import com.yzd.common.pubsub.redis.sharded.ShardedRedisPubSubUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zd.yao on 2017/12/15.
 */
@Component
public class CachePubSubJob {
    private static final String channel="P03.CLEAN.CACHE";

    //订阅者：心跳2分钟一次。
    @Scheduled(fixedDelay = 1000*60*2)
    public void subClient_Heart() throws InterruptedException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String beginLog="[订阅者：心跳2分钟一次]-Begin-currentTime= " + dateFormat.format(new Date());
        System.out.println(beginLog);
        //--
        ShardedRedisPubSubUtil redisPubSubUtil=ShardedRedisPubSubUtil.getInstance();
        redisPubSubUtil.subClient(channel);
    }
    //订阅者:接收监听出现网络闪断异常后，1分钟后重新打开。
    @Scheduled(fixedDelay = 1000*60*1)
    public void subClient_PubSubListener() throws InterruptedException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        String beginLog="[订阅者:接收监听启动时间]-Begin-currentTime= " + dateFormat.format(new Date());
        System.out.println(beginLog);
        //--
        while (true){
            ShardedRedisPubSubUtil redisPubSubUtil=ShardedRedisPubSubUtil.getInstance();
            String value=  redisPubSubUtil.pubSubListener(channel,5);
            if(StringUtils.isBlank(value)){
                continue;
            }
            System.out.println(value);
            //具体的业务操作逻辑--value
        }
    }
}
