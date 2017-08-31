package com.yzd.common.mq.base;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.job.mutesKey.RedisJobMutesKeyUtil;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by zd.yao on 2017/7/7.
 */
public class AbstractTask  implements Runnable {
    ArrayBlockingQueue<Integer> tokenBucket;
    JobEnum keyEnum;
    String value;
    public AbstractTask(ArrayBlockingQueue<Integer> tokenBucket,JobEnum keyEnum,String value){
        this.tokenBucket=tokenBucket;
        this.keyEnum=keyEnum;
        this.value=value;
    }

    @Override
    public void run() {
        try{
            //设置:mutexKey互斥锁
            Boolean isOkSetMutesKey= RedisJobMutesKeyUtil.set(keyEnum, value);
            if(!isOkSetMutesKey){return;}
            doWork();
        }finally {
            try {
                //令牌减1；
                tokenBucket.take();
                 //删除:先删除set排除消息对列再删除mutexKey互斥锁
                RedisJobMutesKeyUtil.del(keyEnum,value);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    protected void doWork() {
        //具体的业务逻辑代码
        throw new IllegalStateException("【AbstractTask】没有具体的业务逻辑实现代码");
    }
}