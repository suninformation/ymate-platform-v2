/*
 * Copyright 2007-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.ymate.platform.commons.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * 日期时间数据处理工具类
 *
 * @author 刘镇 (suninformation@163.com) on 2010-4-18 上午02:40:41
 */
public final class DateTimeUtils {

    /**
     * 1秒
     */
    public static final long SECOND = 1000L;

    /**
     * 1分钟
     */
    public static final long MINUTE = SECOND * 60;

    /**
     * 1小时
     */
    public static final long HOUR = MINUTE * 60;

    /**
     * 1天
     */
    public static final long DAY = HOUR * 24;

    /**
     * 1周
     */
    public static final long WEEK = DAY * 7;

    /**
     * 1年（or 366 ???）
     */
    public static final long YEAR = DAY * 365;

    /**
     * 日期格式化字符串：yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * 日期格式化字符串：yyyy-MM-dd HH:mm:ss
     */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * 日期格式化字符串：yyyy-MM-dd
     */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * 日期格式化字符串：yyyy-MM
     */
    public static final String YYYY_MM = "yyyy-MM";

    /**
     * 日期格式化字符串：yyyy-MM-dd HH:mm
     */
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";

    public static final Map<String, String[]> TIME_ZONES;

    public static final int UTC_LENGTH = 10;

    static {
        Map<String, String[]> timeZonesMap = new LinkedHashMap<>(32);
        //
        timeZonesMap.put("-12", new String[]{"GMT-12:00", "(GMT -12:00) Eniwetok, Kwajalein"});
        timeZonesMap.put("-11", new String[]{"GMT-11:00", "(GMT -11:00) Midway Island, Samoa"});
        timeZonesMap.put("-10", new String[]{"GMT-10:00", "(GMT -10:00) Hawaii"});
        timeZonesMap.put("-9", new String[]{"GMT-09:00", "(GMT -09:00) Alaska"});
        timeZonesMap.put("-8", new String[]{"GMT-08:00", "(GMT -08:00) Pacific Time (US &amp; Canada), Tijuana"});
        timeZonesMap.put("-7", new String[]{"GMT-07:00", "(GMT -07:00) Mountain Time (US &amp; Canada), Arizona"});
        timeZonesMap.put("-6", new String[]{"GMT-06:00", "(GMT -06:00) Central Time (US &amp; Canada), Mexico City"});
        timeZonesMap.put("-5", new String[]{"GMT-05:00", "(GMT -05:00) Eastern Time (US &amp; Canada), Bogota, Lima, Quito"});
        timeZonesMap.put("-4", new String[]{"GMT-04:00", "(GMT -04:00) Atlantic Time (Canada), Caracas, La Paz"});
        timeZonesMap.put("-3.5", new String[]{"GMT-03:30", "(GMT -03:30) Newfoundland"});
        timeZonesMap.put("-3", new String[]{"GMT-03:00", "(GMT -03:00) Brassila, Buenos Aires, Georgetown, Falkland Is"});
        timeZonesMap.put("-2", new String[]{"GMT-02:00", "(GMT -02:00) Mid-Atlantic, Ascension Is., St. Helena"});
        timeZonesMap.put("-1", new String[]{"GMT-01:00", "(GMT -01:00) Azores, Cape Verde Islands"});
        timeZonesMap.put("0", new String[]{"GMT", "(GMT) Casablanca, Dublin, Edinburgh, London, Lisbon, Monrovia"});
        timeZonesMap.put("1", new String[]{"GMT+01:00", "(GMT +01:00) Amsterdam, Berlin, Brussels, Madrid, Paris, Rome"});
        timeZonesMap.put("2", new String[]{"GMT+02:00", "(GMT +02:00) Cairo, Helsinki, Kaliningrad, South Africa"});
        timeZonesMap.put("3", new String[]{"GMT+03:00", "(GMT +03:00) Baghdad, Riyadh, Moscow, Nairobi"});
        timeZonesMap.put("3.5", new String[]{"GMT+03:30", "(GMT +03:30) Tehran"});
        timeZonesMap.put("4", new String[]{"GMT+04:00", "(GMT +04:00) Abu Dhabi, Baku, Muscat, Tbilisi"});
        timeZonesMap.put("4.5", new String[]{"GMT+04:30", "(GMT +04:30) Kabul"});
        timeZonesMap.put("5", new String[]{"GMT+05:00", "(GMT +05:00) Ekaterinburg, Islamabad, Karachi, Tashkent"});
        timeZonesMap.put("5.5", new String[]{"GMT+05:30", "(GMT +05:30) Bombay, Calcutta, Madras, New Delhi"});
        timeZonesMap.put("5.75", new String[]{"GMT+05:45", "(GMT +05:45) Katmandu"});
        timeZonesMap.put("6", new String[]{"GMT+06:00", "(GMT +06:00) Almaty, Colombo, Dhaka, Novosibirsk"});
        timeZonesMap.put("6.5", new String[]{"GMT+06:30", "(GMT +06:30) Rangoon"});
        timeZonesMap.put("7", new String[]{"GMT+07:00", "(GMT +07:00) Bangkok, Hanoi, Jakarta"});
        timeZonesMap.put("8", new String[]{"GMT+08:00", "(GMT +08:00) Beijing, Hong Kong, Perth, Singapore, Taipei"});
        timeZonesMap.put("9", new String[]{"GMT+09:00", "(GMT +09:00) Osaka, Sapporo, Seoul, Tokyo, Yakutsk"});
        timeZonesMap.put("9.5", new String[]{"GMT+09:30", "(GMT +09:30) Adelaide, Darwin"});
        timeZonesMap.put("10", new String[]{"GMT+10:00", "(GMT +10:00) Canberra, Guam, Melbourne, Sydney, Vladivostok"});
        timeZonesMap.put("11", new String[]{"GMT+11:00", "(GMT +11:00) Magadan, New Caledonia, Solomon Islands"});
        timeZonesMap.put("12", new String[]{"GMT+12:00", "(GMT +12:00) Auckland, Wellington, Fiji, Marshall Island"});
        //
        TIME_ZONES = Collections.unmodifiableMap(timeZonesMap);
    }

    /**
     * @see DateTimeUtils#getFastDateFormat(String, String)
     * @deprecated
     */
    @Deprecated
    public static SimpleDateFormat getSimpleDateFormat(String format, String timeOffset) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.ENGLISH);
        TimeZone timeZone = getTimeZone(timeOffset);
        if (timeZone != null) {
            dateFormat.setTimeZone(timeZone);
        }
        return dateFormat;
    }

    /**
     * @since 2.1.3
     */
    public static FastDateFormat getFastDateFormat(String format, String timeOffset) {
        return FastDateFormat.getInstance(format, getTimeZone(timeOffset), Locale.ENGLISH);
    }

    public static TimeZone getTimeZone(String timeOffset) {
        timeOffset = StringUtils.defaultIfBlank(timeOffset, TIMEZONE_OFFSET);
        if (StringUtils.isNotBlank(timeOffset) && TIME_ZONES.containsKey(timeOffset)) {
            return TimeZone.getTimeZone(TIME_ZONES.get(timeOffset)[0]);
        }
        return null;
    }

    /**
     * 时间修正偏移量
     */
    public static String TIMEZONE_OFFSET;

    /**
     * 私有构造器， 防止被实例化
     */
    private DateTimeUtils() {
    }

    /**
     * @return 获取当前时间
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * @return 获取当前UTC时间
     */
    public static long currentTimeUTC() {
        return currentTimeMillis() / SECOND;
    }

    /**
     * @return 获得当前时间
     */
    public static Date currentTime() {
        return new Date();
    }

    /**
     * @return 获取系统UTC时间
     */
    public static int systemTimeUTC() {
        return (int) currentTimeUTC();
    }

    /**
     * @param time    日期时间值(若为UTC时间，方法内将自动乘以1000)
     * @param pattern 日期时间输出模式，若为空则使用yyyy-MM-dd HH:mm:ss.SSS作为默认
     * @return 格式化日期时间输出字符串
     */
    public static String formatTime(long time, String pattern) {
        return formatTime(time, pattern, null);
    }

    public static String formatTime(long time, String pattern, String timeOffset) {
        if (String.valueOf(time).length() <= UTC_LENGTH) {
            time *= SECOND;
        }
        return getFastDateFormat(StringUtils.defaultIfBlank(pattern, YYYY_MM_DD_HH_MM_SS), timeOffset).format(new Date(time));
    }

    public static Date parseDateTime(String dateTime, String pattern) throws ParseException {
        return parseDateTime(dateTime, pattern, null);
    }

    public static Date parseDateTime(String dateTime, String pattern, String timeOffset) throws ParseException {
        return getFastDateFormat(StringUtils.defaultIfBlank(pattern, YYYY_MM_DD_HH_MM_SS), timeOffset).parse(dateTime);
    }

    /**
     * @param year 年份
     * @return 判断年份是否为闰年
     */
    public static boolean isLeapYear(int year) {
        return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    /**
     * @param o 目标日期时间类对象
     * @return 尝试通过目标类对象提取时间毫秒值
     * @since 2.1.2
     */
    public static long timeMillis(Object o) {
        if (o instanceof java.sql.Date) {
            return ((java.sql.Date) o).getTime();
        } else if (o instanceof LocalDate) {
            return java.sql.Date.valueOf(((LocalDate) o)).getTime();
        } else if (o instanceof LocalTime) {
            return Time.valueOf((LocalTime) o).getTime();
        } else if (o instanceof LocalDateTime) {
            return java.sql.Date.valueOf(((LocalDateTime) o).toLocalDate()).getTime();
        } else if (o instanceof Timestamp) {
            return ((Timestamp) o).getTime();
        } else if (o instanceof Time) {
            return ((Time) o).getTime();
        } else if (o instanceof Date) {
            return ((Date) o).getTime();
        }
        return 0L;
    }
}
