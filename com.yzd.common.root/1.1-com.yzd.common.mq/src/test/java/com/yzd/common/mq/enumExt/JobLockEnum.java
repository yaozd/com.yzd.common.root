package com.yzd.common.mq.enumExt;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;

/**
 * Created by zd.yao on 2017/8/29.
 */
public enum  JobLockEnum implements JobEnum {
    HelloWorldJob("HelloWorld",1);
    // 成员变量
    private String name;
    private Integer id;
    // 构造方法
    private JobLockEnum(String name,Integer id) {
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
    public String getSetName(){
        return "jobSet-"+name;
    }
    public String getMutesKeyName(){
        return "jobMK_"+name+":";
    }
    public String getTokenBucketName(){
        return name;
    }
}