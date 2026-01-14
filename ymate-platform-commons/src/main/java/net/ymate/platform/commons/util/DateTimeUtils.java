/*
 * Copyright 2007-2025 the original author or authors.
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
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
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
    public static final long MINUTE = 60_000L;

    /**
     * 1小时
     */
    public static final long HOUR = 3_600_000L;

    /**
     * 1天
     */
    public static final long DAY = 86_400_000L;

    /**
     * 1周
     */
    public static final long WEEK = 604_800_000L;

    /**
     * 1年（按365天计算）
     */
    public static final long YEAR = 31_536_000_000L;

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

    /**
     * 时间修正偏移量
     */
    public static String TIMEZONE_OFFSET;

    public static final Map<String, String[]> TIME_ZONES;

    public static final int UTC_LENGTH = 10;

    static {
        // 使用JDK内置的ZoneId来构建时区映射，保持原有接口兼容性
        Map<String, String[]> timeZonesMap = new LinkedHashMap<>(32);
        // 映射标准时区偏移到对应的JDK时区ID
        timeZonesMap.put("-12", new String[]{"UTC-12:00", "(UTC -12:00) Baker Island"});
        timeZonesMap.put("-11", new String[]{"UTC-11:00", "(UTC -11:00) Samoa"});
        timeZonesMap.put("-10", new String[]{"UTC-10:00", "(UTC -10:00) Hawaii"});
        timeZonesMap.put("-9", new String[]{"UTC-09:00", "(UTC -09:00) Alaska"});
        timeZonesMap.put("-8", new String[]{"UTC-08:00", "(UTC -08:00) Pacific Time (US & Canada), Tijuana"});
        timeZonesMap.put("-7", new String[]{"UTC-07:00", "(UTC -07:00) Mountain Time (US & Canada), Arizona"});
        timeZonesMap.put("-6", new String[]{"UTC-06:00", "(UTC -06:00) Central Time (US & Canada), Mexico City"});
        timeZonesMap.put("-5", new String[]{"UTC-05:00", "(UTC -05:00) Eastern Time (US & Canada), Bogota, Lima, Quito"});
        timeZonesMap.put("-4", new String[]{"UTC-04:00", "(UTC -04:00) Atlantic Time (Canada), Caracas, La Paz"});
        timeZonesMap.put("-3.5", new String[]{"UTC-03:30", "(UTC -03:30) Newfoundland"});
        timeZonesMap.put("-3", new String[]{"UTC-03:00", "(UTC -03:00) Brasília, Buenos Aires, Georgetown"});
        timeZonesMap.put("-2", new String[]{"UTC-02:00", "(UTC -02:00) South Georgia and the South Sandwich Islands"});
        timeZonesMap.put("-1", new String[]{"UTC-01:00", "(UTC -01:00) Azores, Cape Verde Islands"});
        timeZonesMap.put("0", new String[]{"UTC", "(UTC) Coordinated Universal Time"});
        timeZonesMap.put("1", new String[]{"UTC+01:00", "(UTC +01:00) Central European Time"});
        timeZonesMap.put("2", new String[]{"UTC+02:00", "(UTC +02:00) Eastern European Time"});
        timeZonesMap.put("3", new String[]{"UTC+03:00", "(UTC +03:00) Moscow Standard Time"});
        timeZonesMap.put("3.5", new String[]{"UTC+03:30", "(UTC +03:30) Tehran"});
        timeZonesMap.put("4", new String[]{"UTC+04:00", "(UTC +04:00) Gulf Standard Time"});
        timeZonesMap.put("4.5", new String[]{"UTC+04:30", "(UTC +04:30) Afghanistan"});
        timeZonesMap.put("5", new String[]{"UTC+05:00", "(UTC +05:00) Pakistan Standard Time"});
        timeZonesMap.put("5.5", new String[]{"UTC+05:30", "(UTC +05:30) India Standard Time"});
        timeZonesMap.put("5.75", new String[]{"UTC+05:45", "(UTC +05:45) Nepal"});
        timeZonesMap.put("6", new String[]{"UTC+06:00", "(UTC +06:00) Bangladesh Standard Time"});
        timeZonesMap.put("6.5", new String[]{"UTC+06:30", "(UTC +06:30) Myanmar"});
        timeZonesMap.put("7", new String[]{"UTC+07:00", "(UTC +07:00) Indochina Time"});
        timeZonesMap.put("8", new String[]{"UTC+08:00", "(UTC +08:00) China Standard Time, Singapore Time"});
        timeZonesMap.put("9", new String[]{"UTC+09:00", "(UTC +09:00) Japan Standard Time, Korea Standard Time"});
        timeZonesMap.put("9.5", new String[]{"UTC+09:30", "(UTC +09:30) Australian Central Standard Time"});
        timeZonesMap.put("10", new String[]{"UTC+10:00", "(UTC +10:00) Australian Eastern Standard Time"});
        timeZonesMap.put("11", new String[]{"UTC+11:00", "(UTC +11:00) Solomon Islands"});
        timeZonesMap.put("12", new String[]{"UTC+12:00", "(UTC +12:00) New Zealand Standard Time"});
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
     * @deprecated
     */
    @Deprecated
    public static FastDateFormat getFastDateFormat(String format, String timeOffset) {
        return FastDateFormat.getInstance(format, getTimeZone(timeOffset), Locale.ENGLISH);
    }

    public static TimeZone getTimeZone(String timeOffset) {
        timeOffset = StringUtils.defaultIfBlank(timeOffset, TIMEZONE_OFFSET);
        if (StringUtils.isNotBlank(timeOffset)) {
            try {
                // 优先使用JDK 8的ZoneId处理
                if (TIME_ZONES.containsKey(timeOffset)) {
                    String zoneIdStr = TIME_ZONES.get(timeOffset)[0];
                    // 将UTC偏移转换为ZoneId
                    if (zoneIdStr.startsWith("UTC")) {
                        // 对于UTC偏移格式，直接使用
                        return TimeZone.getTimeZone(zoneIdStr);
                    }
                    return TimeZone.getTimeZone(zoneIdStr);
                } else {
                    // 尝试直接解析时区ID或偏移
                    ZoneId zoneId = parseZoneId(timeOffset);
                    if (zoneId != null) {
                        return TimeZone.getTimeZone(zoneId);
                    }
                }
            } catch (Exception e) {
                // 解析失败时返回null
            }
        }
        return null;
    }

    /**
     * 使用JDK 8解析时区ID或偏移
     *
     * @param timeOffset 时区偏移字符串
     * @return 对应的ZoneId
     */
    private static ZoneId parseZoneId(String timeOffset) {
        try {
            // 尝试直接解析为时区ID
            return ZoneId.of(timeOffset);
        } catch (Exception e1) {
            try {
                // 尝试解析为小时偏移
                double hours = Double.parseDouble(timeOffset);
                int totalMinutes = (int) (hours * 60);
                return ZoneId.ofOffset("UTC", ZoneOffset.ofTotalSeconds(totalMinutes * 60));
            } catch (Exception e2) {
                // 如果都失败，返回null
                return null;
            }
        }
    }

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
        return Instant.now().getEpochSecond();
    }

    /**
     * @return 获得当前时间
     */
    public static Date currentTime() {
        return Date.from(Instant.now());
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
        String format = StringUtils.defaultIfBlank(pattern, YYYY_MM_DD_HH_MM_SS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ENGLISH);
        Instant instant = Instant.ofEpochMilli(time);
        if (StringUtils.isNotBlank(timeOffset)) {
            try {
                // 使用JDK 8的ZoneId处理时区
                ZoneId zoneId = parseZoneId(timeOffset);
                if (zoneId != null) {
                    // 优先使用ZoneId
                    ZonedDateTime zonedDateTime = instant.atZone(zoneId);
                    return formatter.format(zonedDateTime);
                } else if (TIME_ZONES.containsKey(timeOffset)) {
                    // 回退到映射表
                    String zoneIdStr = TIME_ZONES.get(timeOffset)[0];
                    ZoneOffset offset = ZoneOffset.of(zoneIdStr.replace("UTC", StringUtils.EMPTY));
                    OffsetDateTime offsetDateTime = instant.atOffset(offset);
                    return formatter.format(offsetDateTime);
                }
            } catch (Exception e) {
                // 处理异常，回退到系统默认时区
            }
        }
        // 默认使用系统时区
        return formatter.format(instant.atZone(ZoneId.systemDefault()));
    }

    public static Date parseDateTime(String dateTime, String pattern) throws ParseException {
        return parseDateTime(dateTime, pattern, null);
    }

    /**
     * 解析ISO 8601格式的日期时间字符串
     *
     * @param dateTime   日期时间字符串
     * @param timeOffset 时区偏移字符串
     * @return 解析后的Date对象，解析失败返回null
     * @since 2.1.4
     */
    private static Date doParseIsoDateTime(String dateTime, String timeOffset) {
        // 尝试解析为带时区的OffsetDateTime
        try {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTime);
            return Date.from(offsetDateTime.toInstant());
        } catch (DateTimeParseException e1) {
            try {
                // 尝试解析为其他形式的ISO格式
                DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_DATE_TIME;
                TemporalAccessor temporalAccessor = isoFormatter.parse(dateTime);
                // 处理带时间戳的格式
                if (temporalAccessor.isSupported(ChronoField.INSTANT_SECONDS)) {
                    Instant instant = Instant.from(temporalAccessor);
                    return Date.from(instant);
                }
                // 处理带时间的格式
                if (temporalAccessor.isSupported(ChronoField.HOUR_OF_DAY)) {
                    return doParseIsoDateTimeWithTime(temporalAccessor, timeOffset);
                }
                // 处理仅日期部分的格式
                return doParseIsoDateOnly(temporalAccessor, timeOffset);
            } catch (DateTimeParseException e2) {
                // ISO解析失败，返回null
                return null;
            }
        }
    }

    /**
     * 解析带时间的ISO日期时间
     *
     * @param temporalAccessor 时间访问器
     * @param timeOffset       时区偏移字符串
     * @return 解析后的Date对象
     * @since 2.1.4
     */
    private static Date doParseIsoDateTimeWithTime(TemporalAccessor temporalAccessor, String timeOffset) {
        // 如果有时区信息
        if (temporalAccessor.isSupported(ChronoField.OFFSET_SECONDS)) {
            OffsetDateTime offsetDateTime = OffsetDateTime.from(temporalAccessor);
            return Date.from(offsetDateTime.toInstant());
        }
        // 没有时区信息，使用LocalDateTime
        LocalDateTime localDateTime = LocalDateTime.from(temporalAccessor);
        ZoneId zoneId = doResolveZoneId(timeOffset);
        if (zoneId != null) {
            return Date.from(localDateTime.atZone(zoneId).toInstant());
        }
        // 处理时区映射表
        if (StringUtils.isNotBlank(timeOffset) && TIME_ZONES.containsKey(timeOffset)) {
            String zoneIdStr = TIME_ZONES.get(timeOffset)[0];
            ZoneOffset offset = ZoneOffset.of(zoneIdStr.replace("UTC", StringUtils.EMPTY));
            return Date.from(localDateTime.atOffset(offset).toInstant());
        }
        // 默认使用系统时区
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 解析仅日期部分的ISO日期
     *
     * @param temporalAccessor 时间访问器
     * @param timeOffset       时区偏移字符串
     * @return 解析后的Date对象
     * @since 2.1.4
     */
    private static Date doParseIsoDateOnly(TemporalAccessor temporalAccessor, String timeOffset) {
        ZoneId zoneId = doResolveZoneId(timeOffset);
        // 尝试解析为LocalDate
        try {
            LocalDate localDate = LocalDate.from(temporalAccessor);
            return doConvertLocalDateToDate(localDate, zoneId, timeOffset);
        } catch (DateTimeParseException e1) {
            // 尝试解析为YearMonth
            try {
                YearMonth yearMonth = YearMonth.from(temporalAccessor);
                LocalDate localDate = yearMonth.atDay(1);
                return doConvertLocalDateToDate(localDate, zoneId, timeOffset);
            } catch (DateTimeParseException e2) {
                // 尝试解析为MonthDay
                try {
                    MonthDay monthDay = MonthDay.from(temporalAccessor);
                    int year = Year.now().getValue();
                    LocalDate localDate = LocalDate.of(year, monthDay.getMonth(), monthDay.getDayOfMonth());
                    return doConvertLocalDateToDate(localDate, zoneId, timeOffset);
                } catch (DateTimeParseException e3) {
                    // 尝试解析为Year
                    Year year = Year.from(temporalAccessor);
                    LocalDate localDate = LocalDate.of(year.getValue(), 1, 1);
                    return doConvertLocalDateToDate(localDate, zoneId, timeOffset);
                }
            }
        }
    }

    /**
     * 将LocalDate转换为Date对象
     *
     * @param localDate  本地日期
     * @param zoneId     时区ID
     * @param timeOffset 时区偏移字符串
     * @return 转换后的Date对象
     * @since 2.1.4
     */
    private static Date doConvertLocalDateToDate(LocalDate localDate, ZoneId zoneId, String timeOffset) {
        if (zoneId != null) {
            return Date.from(localDate.atStartOfDay(zoneId).toInstant());
        }
        // 处理时区映射表
        if (StringUtils.isNotBlank(timeOffset) && TIME_ZONES.containsKey(timeOffset)) {
            String zoneIdStr = TIME_ZONES.get(timeOffset)[0];
            ZoneOffset offset = ZoneOffset.of(zoneIdStr.replace("UTC", StringUtils.EMPTY));
            return Date.from(localDate.atTime(LocalTime.MIN).atOffset(offset).toInstant());
        }
        // 默认使用系统时区
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 解析时区ID
     *
     * @param timeOffset 时区偏移字符串
     * @return 解析后的ZoneId，解析失败返回null
     * @since 2.1.4
     */
    private static ZoneId doResolveZoneId(String timeOffset) {
        if (StringUtils.isNotBlank(timeOffset)) {
            return parseZoneId(timeOffset);
        }
        return null;
    }

    /**
     * 解析日期时间字符串为Date对象，支持指定时区
     *
     * @param dateTime   日期时间字符串，可以是自定义格式或ISO 8601标准格式
     * @param pattern    日期时间格式，若为空则使用yyyy-MM-dd HH:mm:ss作为默认格式
     * @param timeOffset 时区偏移字符串，可以是UTC偏移格式（如"+8"）、时区ID（如"Asia/Shanghai"）或空字符串（使用系统默认时区）
     * @return 解析后的Date对象
     * @throws ParseException 解析失败时抛出，包含具体的错误信息和错误位置
     */
    public static Date parseDateTime(String dateTime, String pattern, String timeOffset) throws ParseException {
        // 首先尝试解析ISO 8601格式
        Date isoResult = doParseIsoDateTime(dateTime, timeOffset);
        if (isoResult != null) {
            return isoResult;
        }
        // ISO解析失败，回退到指定的pattern解析
        String format = StringUtils.defaultIfBlank(pattern, YYYY_MM_DD_HH_MM_SS);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

        try {
            ZoneId zoneId = doResolveZoneId(timeOffset);
            if (zoneId != null) {
                return doParseDateWithZoneId(dateTime, formatter, zoneId);
            } else if (StringUtils.isNotBlank(timeOffset) && TIME_ZONES.containsKey(timeOffset)) {
                // 回退到映射表
                String zoneIdStr = TIME_ZONES.get(timeOffset)[0];
                ZoneOffset offset = ZoneOffset.of(zoneIdStr.replace("UTC", StringUtils.EMPTY));
                return doParseDateWithZoneOffset(dateTime, formatter, offset);
            }
            // 默认使用系统时区
            return doParseDateUsingDefaultZone(dateTime, formatter);
        } catch (DateTimeParseException e) {
            throw new ParseException(e.getMessage(), e.getErrorIndex());
        }
    }

    /**
     * 通用日期时间解析方法，支持多种日期时间类型
     *
     * @param dateStr       日期时间字符串
     * @param formatter     日期时间格式
     * @param temporalQuery 时间查询器
     * @return 解析后的TemporalAccessor对象
     * @throws DateTimeParseException 解析失败时抛出
     * @since 2.1.4
     */
    private static <T extends TemporalAccessor> T doParseTemporal(String dateStr, DateTimeFormatter formatter, TemporalQuery<T> temporalQuery) {
        return formatter.parse(dateStr, temporalQuery);
    }

    /**
     * 使用ZoneId解析日期时间字符串
     *
     * @param dateStr   日期时间字符串
     * @param formatter 日期时间格式
     * @param zoneId    时区ID
     * @return 解析后的Date对象
     * @since 2.1.4
     */
    private static Date doParseDateWithZoneId(String dateStr, DateTimeFormatter formatter, ZoneId zoneId) {
        // 尝试按顺序解析为不同的日期时间类型
        try {
            // 尝试解析为LocalDateTime（完整日期时间格式）
            LocalDateTime localDateTime = doParseTemporal(dateStr, formatter, LocalDateTime::from);
            return Date.from(localDateTime.atZone(zoneId).toInstant());
        } catch (DateTimeParseException e1) {
            try {
                // 尝试解析为LocalDate（仅日期格式）
                LocalDate localDate = doParseTemporal(dateStr, formatter, LocalDate::from);
                return Date.from(localDate.atStartOfDay(zoneId).toInstant());
            } catch (DateTimeParseException e2) {
                try {
                    // 尝试解析为MonthDay（仅月日格式）
                    MonthDay monthDay = doParseTemporal(dateStr, formatter, MonthDay::from);
                    int year = Year.now().getValue();
                    return Date.from(LocalDate.of(year, monthDay.getMonth(), monthDay.getDayOfMonth()).atStartOfDay(zoneId).toInstant());
                } catch (DateTimeParseException e3) {
                    try {
                        // 尝试解析为YearMonth（仅年月格式）
                        YearMonth yearMonth = doParseTemporal(dateStr, formatter, YearMonth::from);
                        return Date.from(yearMonth.atDay(1).atStartOfDay(zoneId).toInstant());
                    } catch (DateTimeParseException e4) {
                        try {
                            // 尝试解析为Year（仅年份格式）
                            Year year = doParseTemporal(dateStr, formatter, Year::from);
                            return Date.from(LocalDate.of(year.getValue(), 1, 1).atStartOfDay(zoneId).toInstant());
                        } catch (DateTimeParseException e5) {
                            // 尝试解析为LocalTime（仅时间格式）
                            LocalTime localTime = doParseTemporal(dateStr, formatter, LocalTime::from);
                            LocalDate today = LocalDate.now(zoneId);
                            return Date.from(LocalDateTime.of(today, localTime).atZone(zoneId).toInstant());
                        }
                    }
                }
            }
        }
    }

    /**
     * 使用ZoneOffset解析日期时间字符串
     *
     * @param dateStr   日期时间字符串
     * @param formatter 日期时间格式
     * @param offset    时区偏移
     * @return 解析后的Date对象
     * @since 2.1.4
     */
    private static Date doParseDateWithZoneOffset(String dateStr, DateTimeFormatter formatter, ZoneOffset offset) {
        ZoneId zoneId = ZoneId.ofOffset("UTC", offset);
        return doParseDateWithZoneId(dateStr, formatter, zoneId);
    }

    /**
     * 使用默认时区解析日期时间字符串
     *
     * @param dateStr   日期时间字符串
     * @param formatter 日期时间格式
     * @return 解析后的Date对象
     * @since 2.1.4
     */
    private static Date doParseDateUsingDefaultZone(String dateStr, DateTimeFormatter formatter) {
        return doParseDateWithZoneId(dateStr, formatter, ZoneId.systemDefault());
    }

    /**
     * @param year 年份
     * @return 判断年份是否为闰年
     */
    public static boolean isLeapYear(int year) {
        return Year.isLeap(year);
    }

    /**
     * @param o 目标日期时间类对象
     * @return 尝试通过目标类对象提取时间毫秒值
     * @since 2.1.2
     */
    public static long timeMillis(Object o) {
        if (o == null) {
            return 0L;
        }
        if (o instanceof java.sql.Date) {
            return ((java.sql.Date) o).getTime();
        } else if (o instanceof LocalDate) {
            return java.sql.Date.valueOf((LocalDate) o).getTime();
        } else if (o instanceof LocalTime) {
            return Time.valueOf((LocalTime) o).getTime();
        } else if (o instanceof LocalDateTime) {
            return Date.from(((LocalDateTime) o).atZone(ZoneId.systemDefault()).toInstant()).getTime();
        } else if (o instanceof ZonedDateTime) {
            return Date.from(((ZonedDateTime) o).toInstant()).getTime();
        } else if (o instanceof Instant) {
            return ((Instant) o).toEpochMilli();
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
