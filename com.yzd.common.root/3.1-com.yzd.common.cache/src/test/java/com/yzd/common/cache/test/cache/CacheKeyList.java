package com.yzd.common.cache.test.cache;

import com.yzd.common.cache.utils.setting.CachedSetting;

/**
 * Created by zd.yao on 2017/11/27.
 */
public class CacheKeyList {
    //具体的项目缓存编号可以查看wiki
    //http://w.hb.com/docs/show/11
    //用户数据库的dubbo服务缓存编码
    private static String com_hb_insure_app_api="P01";
    public static CachedSetting HelloWorld=newCachedSetting("HelloWorld","缓存：描述信息","1");

    //key=项目缓存编号.KeyName_XXX
    //公共数据缓存规则：缓存一分钟，NULL缓存15秒，5秒互斥，300毫秒自旋。
    private static CachedSetting newCachedSetting(String key,String desc,String version){
       return new CachedSetting(com_hb_insure_app_api,key, 60, 15, 5,300,desc,version);
    }
}
