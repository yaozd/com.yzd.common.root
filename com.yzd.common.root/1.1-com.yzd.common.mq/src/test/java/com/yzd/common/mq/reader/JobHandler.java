package com.yzd.common.mq.reader;

import com.yzd.common.mq.base.AbstractTask;
import com.yzd.common.mq.redis.job.enumExt.JobEnum;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by zd.yao on 2017/8/30.
 */
public class JobHandler extends AbstractTask {
    //队列消息指令
    String value;
    public JobHandler(ArrayBlockingQueue<Integer> tokenBucket,JobEnum keyEnum,String value) {
        super(tokenBucket,keyEnum,value);
        this.value=value;
    }
    @Override
    protected void doWork(){
        try{
            //具体的业务处理逻辑
            //任务操作异常或数据库异常
            TimeUnit.SECONDS.sleep(30);
            System.out.println("brpopExtByShardedJedisPoolExample2:value="+value);
        }catch (Exception ex){
            //log ex
        }
    }
}
