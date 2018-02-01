package com.haitao55.spider.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SpiderDateTimeUtil {
    
    public static String FORMAT_LONG_DATE = "yyyy-MM-dd HH:mm:ss";
    public static String FORMAT_SHORT_DATE = "yyyy-MM-dd";
    public static String FORMAT_GMT_DATE = "yyyy-MM-dd'T'HH:mm:ss+08:00";

    public static Date getSpecifiedDate(Date d, int hour, int minute, int second){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        if(hour >= 0){
            calendar.set(Calendar.HOUR_OF_DAY, hour);    
        }
        if(minute >= 0){
            calendar.set(Calendar.MINUTE, minute);    
        }
        if(second >= 0){
            calendar.set(Calendar.SECOND, second);    
        }
        
        return calendar.getTime();
    }
    
    public static Date getSpecifiedDate(long timeInMillis){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMillis);
        return calendar.getTime();
    }
    
    public static String format(Date d, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(d);
    }
    
    public static String format(long ts, String format){
        Date d = getSpecifiedDate(ts);
        return format(d, format);
    }
    
    public static Date parse(String s, String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        Date d = null;
        try {
            d = sdf.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }
    
    public static int getHour(Date d){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }
    
    public static int getMinute(Date d){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        return calendar.get(Calendar.MINUTE);
    }
    
    public static int getSecond(Date d){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        return calendar.get(Calendar.SECOND);
    }
    
    public static int getDayOfWeek(Date d){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }
    
    public static Date add(Date d, int field, int amount){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(d);
        calendar.add(field, amount);
        return calendar.getTime();
    }
    
    public static long timeGapInSeconds(Date start, Date end){
        long s = start.getTime();
        long e = end.getTime();
        return (long)(e - s) / 1000;
    }
    
    public static long systemTimestamp(){
        return System.currentTimeMillis() / 1000;
    }

    public static String dateFormat(long ts, String format){
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(ts);
    }
    
    public static long dateFormat(int numberDay,String currentDate,
    		String format) throws ParseException{
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar cale = Calendar.getInstance();
        Date date = sdf.parse(currentDate);
        cale.setTime(date);
        cale.set(Calendar.DATE, cale.get(Calendar.DATE) - numberDay);
        return cale.getTime().getTime();
    	
    }
    
    public static long dateHourFormat(int hour,String currentDate,
    		String format) throws ParseException{
    	SimpleDateFormat sdf = new SimpleDateFormat(format);
        Calendar cale = Calendar.getInstance();
        Date date = sdf.parse(currentDate);
        cale.setTime(date);
        cale.set(Calendar.HOUR_OF_DAY,cale.get(Calendar.HOUR_OF_DAY) - hour);
        return cale.getTime().getTime();
    	
    }
    
}
