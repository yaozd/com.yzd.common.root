package com.yzd.common.mq.example.schedule._base;

import com.yzd.common.mq.redis.job.enumExt.JobEnum;

/**
 * 任务列表
 * Created by zd.yao on 2017/9/14.
 */
public enum JobListEnum implements JobEnum {
    HelloWorldJob(1,"HelloWorld","成员描述信息");
    //region 内部变量
    private Integer id;
    // 成员变量
    private String name;
    // 成员描述信息
    private String description;
    //endregion
    // 构造方法
    private JobListEnum(Integer id,String name,String description) {
        this.id=id;
        this.name = name;
        this.description=description;
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
