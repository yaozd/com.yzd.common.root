package com.yzd.common.cache.utils.setting;

import com.yzd.common.cache.utils.encrypt.MD5Util;
import com.yzd.common.cache.utils.fastjson.FastJsonUtil;

/**
 * Created by zd.yao on 2017/3/21.
 */
public class CachedKeyUtil {
    public static <T> String KeyMd5(T object){
        String val= FastJsonUtil.serialize(object);
        return MD5Util.encode(val, "UTF-8");
    }
    public static String KeyMd5(String val){
        return MD5Util.encode(val,"UTF-8");
    }
    public static <T> String KeySerialize(T object){
        String val= FastJsonUtil.serialize(object);
        return val;
    }
}
