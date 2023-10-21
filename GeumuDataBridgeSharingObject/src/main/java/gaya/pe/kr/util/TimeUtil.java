package gaya.pe.kr.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TimeUtil {

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    static SimpleDateFormat simpleDateFormatYearMD = new SimpleDateFormat("yyyy-MM-dd");
    public static long getTimeDiffTwoDay(Date before, Date after) {
        return ( ( ( ( after.getTime() - before.getTime()) / 1000 ) / 60 ) / 60 ) / 24 ;
    }

    public static long getTimeDiffDay(Date date) {
        Date now = new Date();
        return ( ( ( ( now.getTime() - date.getTime()) / 1000 ) / 60 ) / 60 ) / 24 ;
    }

    public static long getTimeDiffHour(Date date) {
        Date now = new Date();
        return ( ( ( now.getTime() - date.getTime()) / 1000 ) / 60 ) / 60 ;
    }

    public static long getTimeDiffMinute(Date date) {
        Date now = new Date();
        return ( ( ( now.getTime() - date.getTime()) / 1000 ) / 60 ) ;
    }

    /**
     *
     * @param date 정해진 시간
     * @return 현재 시간과 파라미터 값을 대조해서 시간 차를 알려줌
     * 만일 음수 값일 경우 parameter 값이 현재 시간보다 뒤에있음
     * ex) -101 @param date 까지 101초 남았다는 뜻
     * 양수 값이면 이미 date 날짜보다 지났다는 뜻
     *
     */
    public static long getTimeDiffSec(Date date) {
        Date now = new Date();
        return (  ( now.getTime() - date.getTime()) / 1000 ) ;
    }

    public static String getTimeMinSec(int time) {
        if ( time >= 60 ) {
            int min = time/60;
            int sec = time%60;
            if ( sec != 0 ) {
                return String.format("%d:%d", min, sec);
            } else {
                return String.format("%d:00", min);
            }
        } else {
            return String.format("00:%d", time);
        }
    }

    public static Date getAfterMinTime(int min) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, min);
        return calendar.getTime();
    }

    public static Date getAfterSecTime(int sec) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, sec);
        return calendar.getTime();
    }

    public static Date getAfterDayTime(int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, day);
        return calendar.getTime();
    }


    public static List<Date> getNextDates(int days) {
        List<Date> dateList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();  // 오늘 날짜를 기준으로 Calendar 객체 생성
        for (int i = 0; i < days; i++) {
            calendar.add(Calendar.DATE, 1);  // 다음 날로 이동
            dateList.add(calendar.getTime());  // List에 추가
        }

        return dateList;
    }

    public static List<Date> getNextDates(Date startDate, int days) {
        List<Date> dateList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();  // 오늘 날짜를 기준으로 Calendar 객체 생성
        calendar.setTime(startDate);
        for (int i = 0; i < days; i++) {
            calendar.add(Calendar.DATE, 1);  // 다음 날로 이동
            dateList.add(calendar.getTime());  // List에 추가
        }

        return dateList;
    }

    public static List<Date> getBeforeDates(Date startDate, int days) {
        List<Date> dateList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();  // 오늘 날짜를 기준으로 Calendar 객체 생성
        calendar.setTime(startDate);
        for (int i = 0; i < days; i++) {
            calendar.add(Calendar.DATE, -1);  // 다음 날로 이동
            dateList.add(calendar.getTime());  // List에 추가
        }

        return dateList;
    }

    public static boolean isSameDay(Date dateA, Date dateB) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(dateA);
        cal2.setTime(dateB);

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    public static int getField(Date date, int field) {

        Calendar calendar1 = Calendar.getInstance();

        calendar1.setTime(date);

        return calendar1.get(field);

    }
    public static SimpleDateFormat getSimpleDateFormat() {
        return simpleDateFormat;
    }

    public static SimpleDateFormat getSimpleDateFormatYearMD() {
        return simpleDateFormatYearMD;
    }
}
