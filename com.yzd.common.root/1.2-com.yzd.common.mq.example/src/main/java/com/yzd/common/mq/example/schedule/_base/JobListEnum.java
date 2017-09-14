package com.yzd.common.mq.example.schedule._base;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;

/**
 * 任务列表
 * Created by zd.yao on 2017/9/14.
 */
public enum JobListEnum implements JobEnum {
    HelloWorldJob("HelloWorld",1);
    // 成员变量
    private String name;
    private Integer id;
    // 构造方法
    private JobListEnum(String name,Integer id) {
        this.name = name;
        this.id=id;
    }
    @Override
    public String getLockWriterName(){
        return "jobLockWriter-"+name;
    }
    @Override
    public String getLockCheckName(){
        return "jobLockCheck-"+name;
    }
    @Override
    public String getListName(){
        return "jobList-"+name;
    }
    @Override
    public String getSetName(){
        return "jobSet-"+name;
    }
    @Override
    public String getMutesKeyName(){
        return "jobMK_"+name+":";
    }
    @Override
    public String getTokenBucketName(){
        return name;
    }
}
