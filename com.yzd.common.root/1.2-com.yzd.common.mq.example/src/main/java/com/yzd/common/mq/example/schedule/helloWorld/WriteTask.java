package com.yzd.common.mq.example.schedule.helloWorld;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;
import com.yzd.common.mq.redis.job.lock.IMyJobExecutorInf;
import com.yzd.common.mq.redis.job.writer.RedisJobWriterUtil;

/**
 * Created by zd.yao on 2017/9/14.
 */
public class WriteTask implements IMyJobExecutorInf {
    private JobEnum keyEnum;
    public WriteTask(JobEnum keyEnum) {
        this.keyEnum = keyEnum;
    }
    @Override
    public void execute() {
        doWork();
    }
    //将任务写入到消息队列中
    void doWork() {
        for (int i = 0; i < 1000; i++) {
            String val = "id=" + i;
            RedisJobWriterUtil.write(keyEnum, val);
        }
    }
}
