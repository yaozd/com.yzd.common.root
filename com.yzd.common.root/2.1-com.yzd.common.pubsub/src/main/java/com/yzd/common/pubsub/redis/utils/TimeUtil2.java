package com.yzd.common.pubsub.redis.utils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Created by zd.yao on 2017/12/11.
 */
public class TimeUtil2 {
    public static Double getTimeOfSecond(){
        Date date = new Date();
        return Double.valueOf(date.getTime()/1000);
    }

    public static int getDiffToCurrentTime(Double oldTime){
        Double currentTime=getTimeOfSecond();
        BigDecimal currentBD = new BigDecimal(currentTime);
        BigDecimal oldBD = new BigDecimal(oldTime);
        BigInteger val= currentBD.subtract(oldBD).toBigInteger();
        return Integer.parseInt(val.toString());
    }
}
