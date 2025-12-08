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
package net.ymate.platform.commons;

import net.ymate.platform.commons.util.DateTimeUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date（日期）类型数据处理相关的函数工具集合<br>
 * 使用JDK8日期时间API的实现版本，基于原DateTimeHelper类的功能进行重构<br>
 * 采用ZonedDateTime作为核心日期时间表示，支持时区操作和所有原类功能<br>
 *
 * @author 刘镇 (suninformation@163.com) on 2010-8-8 下午12:37:55
 */
public class DateTimeHelper {

    private static final int UTC_TIME_LENGTH = 10;

    /**
     * 核心日期时间对象，使用ZonedDateTime支持时区和完整的日期时间操作
     */
    private ZonedDateTime zonedDateTime;

    /**
     * 绑定Date对象创建DateTimeHelper实例
     *
     * @param date Date对象
     * @return DateTimeHelper实例
     */
    public static DateTimeHelper bind(Date date) {
        return new DateTimeHelper(date);
    }

    /**
     * 绑定时间戳创建DateTimeHelper实例<br>
     * 支持秒级和毫秒级时间戳自动识别
     *
     * @param date 时间戳（秒或毫秒）
     * @return DateTimeHelper实例
     */
    public static DateTimeHelper bind(long date) {
        return new DateTimeHelper(date);
    }

    /**
     * 绑定日期时间字符串创建DateTimeHelper实例
     *
     * @param dateStr    日期时间字符串
     * @param dateFormat 日期时间格式
     * @return DateTimeHelper实例
     */
    public static DateTimeHelper bind(String dateStr, String dateFormat) {
        return new DateTimeHelper(dateStr, dateFormat);
    }

    /**
     * 绑定LocalDateTime对象创建DateTimeHelper实例
     *
     * @param localDateTime LocalDateTime对象
     * @return DateTimeHelper实例
     * @since 2.1.4
     */
    public static DateTimeHelper bind(LocalDateTime localDateTime) {
        return new DateTimeHelper(localDateTime);
    }

    /**
     * 绑定LocalDateTime对象创建DateTimeHelper实例
     *
     * @param localDateTime LocalDateTime对象
     * @param zoneId        时区ID
     * @return DateTimeHelper实例
     * @since 2.1.4
     */
    public static DateTimeHelper bind(LocalDateTime localDateTime, ZoneId zoneId) {
        return new DateTimeHelper(localDateTime, zoneId);
    }

    /**
     * 绑定ZonedDateTime对象创建DateTimeHelper实例
     *
     * @param zonedDateTime ZonedDateTime对象
     * @return DateTimeHelper实例
     * @since 2.1.4
     */
    public static DateTimeHelper bind(ZonedDateTime zonedDateTime) {
        return new DateTimeHelper(zonedDateTime);
    }

    /**
     * 绑定年月日创建DateTimeHelper实例
     *
     * @param year  年
     * @param month 月（1-12）
     * @param day   日（1-31）
     * @return DateTimeHelper实例
     * @since 2.1.4
     */
    public static DateTimeHelper bind(int year, int month, int day) {
        return new DateTimeHelper(year, month, day);
    }

    /**
     * 绑定年月日时分秒创建DateTimeHelper实例
     *
     * @param year   年
     * @param month  月（1-12）
     * @param day    日（1-31）
     * @param hour   时（0-23）
     * @param minute 分（0-59）
     * @param second 秒（0-59）
     * @return DateTimeHelper实例
     * @since 2.1.4
     */
    public static DateTimeHelper bind(int year, int month, int day, int hour, int minute, int second) {
        return new DateTimeHelper(year, month, day, hour, minute, second);
    }

    /**
     * 获取当前日期时间的实例
     *
     * @return DateTimeHelper实例
     */
    public static DateTimeHelper now() {
        return new DateTimeHelper();
    }

    private DateTimeHelper() {
        this.zonedDateTime = ZonedDateTime.now();
    }

    private DateTimeHelper(Date date) {
        this.zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private DateTimeHelper(long date) {
        if (String.valueOf(date).length() <= UTC_TIME_LENGTH) {
            date *= DateTimeUtils.SECOND;
        }
        this.zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault());
    }

    private DateTimeHelper(String dateStr, String dateFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
        this.zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
    }

    /**
     * @since 2.1.4
     */
    private DateTimeHelper(LocalDateTime localDateTime) {
        this.zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
    }

    /**
     * @since 2.1.4
     */
    private DateTimeHelper(LocalDateTime localDateTime, ZoneId zoneId) {
        this.zonedDateTime = localDateTime.atZone(zoneId);
    }

    /**
     * @since 2.1.4
     */
    private DateTimeHelper(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    private DateTimeHelper(int year, int month, int day) {
        this.zonedDateTime = ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZoneId.systemDefault());
    }

    private DateTimeHelper(int year, int month, int day, int hour, int minute, int second) {
        this.zonedDateTime = ZonedDateTime.of(year, month, day, hour, minute, second, 0, ZoneId.systemDefault());
    }

    /**
     * 获取Calendar对象（兼容旧API）
     *
     * @return Calendar实例
     * @deprecated
     */
    @Deprecated
    public Calendar getCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(zonedDateTime.getZone()));
        calendar.setTimeInMillis(zonedDateTime.toInstant().toEpochMilli());
        return calendar;
    }

    /**
     * 设置时区
     *
     * @param timeZone 时区对象
     * @return 当前实例，支持链式调用
     */
    public DateTimeHelper timeZone(TimeZone timeZone) {
        this.zonedDateTime = this.zonedDateTime.withZoneSameInstant(ZoneId.of(timeZone.getID()));
        return this;
    }

    /**
     * 设置时区
     *
     * @param zoneId 时区ID
     * @return 当前实例，支持链式调用
     * @since 2.1.4
     */
    public DateTimeHelper timeZone(ZoneId zoneId) {
        this.zonedDateTime = this.zonedDateTime.withZoneSameInstant(zoneId);
        return this;
    }

    /**
     * 获取当前时区
     *
     * @return 时区对象
     */
    public TimeZone timeZone() {
        return TimeZone.getTimeZone(zonedDateTime.getZone());
    }

    /**
     * 获取Date对象（兼容旧API）
     *
     * @return Date实例
     */
    public Date time() {
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * 获取LocalDateTime对象
     *
     * @return LocalDateTime实例
     * @since 2.1.4
     */
    public LocalDateTime localDateTime() {
        return zonedDateTime.toLocalDateTime();
    }

    /**
     * 获取LocalDate对象
     *
     * @return LocalDate实例
     * @since 2.1.4
     */
    public LocalDate localDate() {
        return zonedDateTime.toLocalDate();
    }

    /**
     * 获取LocalTime对象
     *
     * @return LocalTime实例
     * @since 2.1.4
     */
    public LocalTime localTime() {
        return zonedDateTime.toLocalTime();
    }

    /**
     * 获取ZoneId对象
     *
     * @return ZoneId实例
     * @since 2.1.4
     */
    public ZoneId zoneId() {
        return zonedDateTime.getZone();
    }

    /**
     * 获取ZonedDateTime对象
     *
     * @return ZonedDateTime实例
     * @since 2.1.4
     */
    public ZonedDateTime zonedDateTime() {
        return zonedDateTime;
    }

    /**
     * 设置时间（兼容旧API）
     *
     * @param date Date对象
     * @return 当前实例，支持链式调用
     */
    public DateTimeHelper time(Date date) {
        this.zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), zonedDateTime.getZone());
        return this;
    }

    /**
     * 设置时间
     *
     * @param localDateTime LocalDateTime对象
     * @return 当前实例，支持链式调用
     * @since 2.1.4
     */
    public DateTimeHelper time(LocalDateTime localDateTime) {
        this.zonedDateTime = localDateTime.atZone(zonedDateTime.getZone());
        return this;
    }

    /**
     * 设置时间
     *
     * @param localDateTime LocalDateTime对象
     * @param zoneId        时区ID
     * @return 当前实例，支持链式调用
     * @since 2.1.4
     */
    public DateTimeHelper time(LocalDateTime localDateTime, ZoneId zoneId) {
        this.zonedDateTime = localDateTime.atZone(zoneId);
        return this;
    }

    public int year() {
        return zonedDateTime.getYear();
    }

    public DateTimeHelper year(int year) {
        this.zonedDateTime = this.zonedDateTime.withYear(year);
        return this;
    }

    public boolean isLeapYear() {
        return Year.isLeap(zonedDateTime.getYear());
    }

    public int month() {
        return zonedDateTime.getMonthValue();
    }

    public DateTimeHelper month(int month) {
        this.zonedDateTime = this.zonedDateTime.withMonth(month);
        return this;
    }

    public int day() {
        return zonedDateTime.getDayOfMonth();
    }

    public DateTimeHelper day(int day) {
        this.zonedDateTime = this.zonedDateTime.withDayOfMonth(day);
        return this;
    }

    public DateTimeHelper toDayStart() {
        this.zonedDateTime = this.zonedDateTime.truncatedTo(ChronoUnit.DAYS);
        return this;
    }

    public DateTimeHelper toDayEnd() {
        this.zonedDateTime = this.zonedDateTime.truncatedTo(ChronoUnit.DAYS)
                .plusDays(1)
                .minusNanos(1);
        return this;
    }

    /**
     * 设置时间为当前月份的第一天的00:00:00.000
     *
     * @return 当前实例，支持链式调用
     * @since 2.1.4
     */
    public DateTimeHelper toMonthStart() {
        this.zonedDateTime = this.zonedDateTime.with(TemporalAdjusters.firstDayOfMonth())
                .truncatedTo(ChronoUnit.DAYS);
        return this;
    }

    /**
     * 设置时间为当前月份的最后一天的23:59:59.999
     *
     * @return 当前实例，支持链式调用
     * @since 2.1.4
     */
    public DateTimeHelper toMonthEnd() {
        this.zonedDateTime = this.zonedDateTime.with(TemporalAdjusters.lastDayOfMonth())
                .truncatedTo(ChronoUnit.DAYS)
                .plusDays(1)
                .minusNanos(1);
        return this;
    }

    public int hour() {
        return zonedDateTime.getHour();
    }

    public DateTimeHelper hour(int hour) {
        this.zonedDateTime = this.zonedDateTime.withHour(hour);
        return this;
    }

    public int minute() {
        return zonedDateTime.getMinute();
    }

    public DateTimeHelper minute(int minute) {
        this.zonedDateTime = this.zonedDateTime.withMinute(minute);
        return this;
    }

    public int second() {
        return zonedDateTime.getSecond();
    }

    public DateTimeHelper second(int second) {
        this.zonedDateTime = this.zonedDateTime.withSecond(second);
        return this;
    }

    public int millisecond() {
        return zonedDateTime.getNano() / 1_000_000;
    }

    public DateTimeHelper millisecond(int millisecond) {
        int nano = millisecond * 1_000_000 + (zonedDateTime.getNano() % 1_000_000);
        this.zonedDateTime = this.zonedDateTime.withNano(nano);
        return this;
    }

    public int dayOfWeek() {
        // 周日 = 7
        return zonedDateTime.getDayOfWeek().getValue();
    }

    public int weekOfMonth() {
        return zonedDateTime.get(WeekFields.ISO.weekOfMonth());
    }

    public int weekOfYear() {
        return zonedDateTime.get(WeekFields.ISO.weekOfYear());
    }

    public int dayOfWeekInMonth() {
        // 1. 获取当前日期是当月的第几天（1~31）
        int dayOfMonth = zonedDateTime.getDayOfMonth();
        // 2. 获取当前日期是周几（1=周一，7=周日，符合 ISO 标准）
        int dayOfWeek = zonedDateTime.getDayOfWeek().getValue();
        // 3. 获取当月1号是周几（同样 1=周一，7=周日）
        int firstDayOfMonth = zonedDateTime.withDayOfMonth(1).getDayOfWeek().getValue();
        // 4. 计算：当前日期 相对 1号 的周偏移量（0~6，代表“1号之后过了几天到当前周的同一天”）
        int offset = (dayOfWeek - firstDayOfMonth + 7) % 7;
        // 5. 核心公式：推导当前日期属于当月的第几周（从1开始）
        return (dayOfMonth - 1 - offset) / 7 + 1;
    }

    public DateTimeHelper toWeekStart() {
        this.zonedDateTime = this.zonedDateTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return this;
    }

    public DateTimeHelper toWeekEnd() {
        this.zonedDateTime = this.zonedDateTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return this;
    }

    public long timeMillis() {
        return zonedDateTime.toInstant().toEpochMilli();
    }

    public DateTimeHelper timeMillis(long timeMillis) {
        this.zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeMillis), zonedDateTime.getZone());
        return this;
    }

    public int timeUTC() {
        return (int) (timeMillis() / DateTimeUtils.SECOND);
    }

    public DateTimeHelper timeUTC(long timeUTC) {
        if (String.valueOf(timeUTC).length() <= UTC_TIME_LENGTH) {
            timeUTC *= DateTimeUtils.SECOND;
        }
        this.zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timeUTC), zonedDateTime.getZone());
        return this;
    }

    public int daysOfMonth() {
        return zonedDateTime.toLocalDate().lengthOfMonth();
    }

    public long subtract(Date date) {
        return zonedDateTime.toInstant().toEpochMilli() - date.getTime();
    }

    public long subtract(DateTimeHelper dateTimeHelper) {
        return subtract(dateTimeHelper.time());
    }

    /**
     * 计算与LocalDateTime的时间差（毫秒）
     *
     * @param localDateTime LocalDateTime对象
     * @return 时间差（毫秒）
     * @since 2.1.4
     */
    public long subtract(LocalDateTime localDateTime) {
        ZonedDateTime targetZonedDateTime = localDateTime.atZone(zonedDateTime.getZone());
        return zonedDateTime.toInstant().toEpochMilli() - targetZonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * 计算与LocalDateTime的时间差（毫秒）
     *
     * @param localDateTime LocalDateTime对象
     * @param zoneId        时区ID
     * @return 时间差（毫秒）
     * @since 2.1.4
     */
    public long subtract(LocalDateTime localDateTime, ZoneId zoneId) {
        ZonedDateTime targetZonedDateTime = localDateTime.atZone(zoneId);
        return zonedDateTime.toInstant().toEpochMilli() - targetZonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * 计算与Instant的时间差（毫秒）
     *
     * @param instant Instant对象
     * @return 时间差（毫秒）
     * @since 2.1.4
     */
    public long subtract(Instant instant) {
        return zonedDateTime.toInstant().toEpochMilli() - instant.toEpochMilli();
    }

    /**
     * 计算与LocalDate的时间差（毫秒）
     *
     * @param localDate LocalDate对象
     * @return 时间差（毫秒）
     * @since 2.1.4
     */
    public long subtract(LocalDate localDate) {
        ZonedDateTime targetZonedDateTime = localDate.atStartOfDay(zonedDateTime.getZone());
        return zonedDateTime.toInstant().toEpochMilli() - targetZonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * 计算与LocalDate的时间差（毫秒）
     *
     * @param localDate LocalDate对象
     * @param zoneId    时区ID
     * @return 时间差（毫秒）
     * @since 2.1.4
     */
    public long subtract(LocalDate localDate, ZoneId zoneId) {
        ZonedDateTime targetZonedDateTime = localDate.atStartOfDay(zoneId);
        return zonedDateTime.toInstant().toEpochMilli() - targetZonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * 计算与ZonedDateTime的时间差（毫秒）
     *
     * @param zonedDateTime ZonedDateTime对象
     * @return 时间差（毫秒）
     * @since 2.1.4
     */
    public long subtract(ZonedDateTime zonedDateTime) {
        return this.zonedDateTime.toInstant().toEpochMilli() - zonedDateTime.toInstant().toEpochMilli();
    }

    public DateTimeHelper millisecondsAdd(int milliseconds) {
        this.zonedDateTime = this.zonedDateTime.plus(milliseconds, ChronoUnit.MILLIS);
        return this;
    }

    public DateTimeHelper secondsAdd(int seconds) {
        this.zonedDateTime = this.zonedDateTime.plusSeconds(seconds);
        return this;
    }

    public DateTimeHelper minutesAdd(int minutes) {
        this.zonedDateTime = this.zonedDateTime.plusMinutes(minutes);
        return this;
    }

    public DateTimeHelper hoursAdd(int hours) {
        this.zonedDateTime = this.zonedDateTime.plusHours(hours);
        return this;
    }

    public DateTimeHelper daysAdd(int days) {
        this.zonedDateTime = this.zonedDateTime.plusDays(days);
        return this;
    }

    public DateTimeHelper weeksAdd(int weeks) {
        this.zonedDateTime = this.zonedDateTime.plusWeeks(weeks);
        return this;
    }

    public DateTimeHelper monthsAdd(int months) {
        this.zonedDateTime = this.zonedDateTime.plusMonths(months);
        return this;
    }

    public DateTimeHelper yearsAdd(int years) {
        this.zonedDateTime = this.zonedDateTime.plusYears(years);
        return this;
    }

    @Override
    public String toString() {
        // 默认使用带毫秒的标准日期时间格式
        return toString(DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS);
    }

    /**
     * 按照指定格式将日期时间转换为字符串
     *
     * @param dateFormat 日期时间格式模式
     * @return 格式化后的日期时间字符串
     */
    public String toString(String dateFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return this.zonedDateTime.format(formatter);
    }
}
