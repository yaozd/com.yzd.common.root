package com.yzd.common.cache.utils.loadBalanceExt;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class CacheRoundRobin {
    private static CacheRoundRobin ourInstance = new CacheRoundRobin();

    public static CacheRoundRobin getInstance() {
        return ourInstance;
    }

    private CacheRoundRobin() {
    }
    private static Map<String, AtomicLong> configMap = new HashMap<String, AtomicLong>();
    //目前这种属于人为的水平扩展-简单有效
    //只对公共缓存资源使用，因为有公共的数据才会出现热点问题。
    //对于公共缓存资源可以采用计数轮询的方式进行水平扩展
    //key的选择是“P01.Other1SelectAll 主名”这样就可以控制KEY的数量
    //P01.Other1SelectAll 主名
    //P01.Other1SelectAll:1di41n95bd34:0848e79ab3873df627ab8e1d3e13a61a 主名+WHERE的MD5的值
    public Long getValue(String key) {
        if(!configMap.containsKey(key)) {
            initConfig(key);
        }
        Long number=configMap.get(key).incrementAndGet();
        //100000L 进可能的保持均匀分布各个缓存节点
        if(number>100000L){
            configMap.put(key,new AtomicLong(0));
        }
        return number;
    }
    private synchronized void initConfig(String key) {
        configMap.put(key, new AtomicLong(0));
        if(configMap.size()>500){
            //KEY=缓存的时间版本的KEY的名称，所以数据量是有限的，理论上不会超过500个缓存的时间版本KEY
            throw new IllegalStateException("CacheRoundRobin->configMap.当前的KEY的数量已经超过500！请不要滥用缓存");
        }
    }
}
