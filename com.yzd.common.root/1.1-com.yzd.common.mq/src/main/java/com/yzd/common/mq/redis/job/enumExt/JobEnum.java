package com.yzd.common.mq.redis.job.enumExt;

/**
 * Created by zd.yao on 2017/8/29.
 */
public interface JobEnum {
    //任务锁-用于保证写入的任务当前只有一个程序在执行
    String getLockWriterName();
    //任务锁-用于保证检查无效的任务当前只有一个程序在执行
    String getLockCheckName();
    //任务消息队列
    String getListName();
    //排重消息队列
    String getSetName();
    //互斥锁-记录正在运行的任务-有效时间为5分钟-任务完成后主动删除MutesKey
    String getMutesKeyName();
    //阻塞队列的令牌名称
    String getTokenBucketName();
    //检查时临时存储
    String getCheckTmpName();
    //存储任务创建时间
    String getCreateTimeName();
}
