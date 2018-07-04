package com.yzd.common.mq.redis.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author YZD
 */
public class TimestampUtil {
    /***
     * 当前时间戳
     * @return
     */
    public static Long dateToTimestamp(){
        return System.currentTimeMillis();
    }

    public static Long  dateToTimestamp(Date date){
        return date.getTime();
    }
    public static Date timestampToDate(Long timestamp){
        return new Date(timestamp);
    }
    public static String dateToString(Date date){
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.format(date);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
