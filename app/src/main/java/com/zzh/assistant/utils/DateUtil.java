package com.zzh.assistant.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtil {
    /**
     * 国际化时间格式 2017-11-7T17:02:07+08:00
     *
     * @Date:17:01 2017/11/7
     */
    public static String YMDHMSXXX = "yyyy-MM-dd'T'HH:mm:ssXXX";
    /**
     * yyyy-MM-dd HH:mm:ss
     */
    public static String YMDHMS = "yyyy-MM-dd HH:mm:ss";
    /**
     * dd-MM-yyyy
     */
    public static String YMD = "yyyy-MM-dd";
    /**
     * MM-dd-yyyy
     */
    public static String MDY = "MM-dd-yyyy";
    /**
     * dd-MM-yyyy HH:mm
     */
    public static String DMYHM = "dd-MM-yyyy HH:mm";

    public static String HH = "HH";

    /**
     * 时间格式化
     *
     * @param date
     * @param formatPattern
     * @return java.lang.String
     * @Date:17:10 2017/11/7
     */
    public static String format(Date date, String formatPattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat f = new SimpleDateFormat(formatPattern);
        return f.format(date);
    }

    public static String format(String s, String formatPattern) {
        if (s == null) {
            return "";
        }
        SimpleDateFormat f = new SimpleDateFormat(formatPattern);
        Date date = null;
        try {
            date = f.parse(s);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return f.format(date);
    }


    /**
     * 当前时间+day,指定hour，分秒置为零
     *
     * @param day
     * @param hour
     * @return
     */
    public static Date addDay(int day, int hour) {
        // 取时间
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, day);
        return calendar.getTime();
    }

    /**
     * 当前时间+day,时分秒置为零
     *
     * @param day
     * @return
     */
    public static Date addDay(int day) {
        // 取时间
        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, day);// 把日期往后增加一天.整数往后推,负数往前移动
        // 这个时间就是日期往后推一天的结果
        return calendar.getTime();
    }


    /**
     * 指定时间+day 时分秒置为零
     *
     * @param day
     * @param date
     * @return
     */
    public static Date addDay(int day, Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, day);// 把日期往后增加一天.整数往后推,负数往前移动
        // 这个时间就是日期往后推一天的结果
        return calendar.getTime();
    }

    /**
     * 指定时间+day 时分秒不变
     *
     * @param day
     * @param date
     * @return
     */
    public static Date addDay2(int day, Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, day);// 把日期往后增加一天.整数往后推,负数往前移动
        // 这个时间就是日期往后推一天的结果
        return calendar.getTime();
    }
}
