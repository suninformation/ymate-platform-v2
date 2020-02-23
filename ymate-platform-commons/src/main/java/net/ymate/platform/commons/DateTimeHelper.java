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
package net.ymate.platform.commons;

import net.ymate.platform.commons.util.DateTimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date（日期）类型数据处理相关的函数工具集合<br>
 * 于 2015/8/14 下午2:58 由DateUtils类重构
 *
 * @author 刘镇 (suninformation@163.com) on 2010-8-8 下午12:37:55
 */
public class DateTimeHelper {

    private static final int UTC_TIME_LENGTH = 10;

    private final Calendar calendar;

    public static DateTimeHelper bind(Date date) {
        return new DateTimeHelper(date);
    }

    public static DateTimeHelper bind(long date) {
        return new DateTimeHelper(date);
    }

    public static DateTimeHelper bind(String dateStr, String dateFormat) throws ParseException {
        return new DateTimeHelper(dateStr, dateFormat);
    }

    public static DateTimeHelper now() {
        return new DateTimeHelper();
    }

    private DateTimeHelper() {
        calendar = Calendar.getInstance();
    }

    private DateTimeHelper(Date date) {
        calendar = Calendar.getInstance();
        calendar.setTime(date);
    }

    private DateTimeHelper(long date) {
        calendar = Calendar.getInstance();
        if (String.valueOf(date).length() <= UTC_TIME_LENGTH) {
            date *= DateTimeUtils.SECOND;
        }
        calendar.setTimeInMillis(date);
    }

    private DateTimeHelper(String dateStr, String dateFormat) throws ParseException {
        calendar = Calendar.getInstance();
        calendar.setTime(new SimpleDateFormat(dateFormat).parse(dateStr));
    }

    private DateTimeHelper(int year, int month, int day) {
        calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
    }

    private DateTimeHelper(int year, int month, int day, int hour, int minute, int second) {
        calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, minute, second);
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public DateTimeHelper timeZone(TimeZone timeZone) {
        calendar.setTimeZone(timeZone);
        return this;
    }

    public TimeZone timeZone() {
        return calendar.getTimeZone();
    }

    public Date time() {
        return calendar.getTime();
    }

    public DateTimeHelper time(Date date) {
        calendar.setTime(date);
        return this;
    }

    public int year() {
        return calendar.get(Calendar.YEAR);
    }

    public DateTimeHelper year(int year) {
        calendar.set(Calendar.YEAR, year);
        return this;
    }

    public int month() {
        return calendar.get(Calendar.MONTH) + 1;
    }

    public DateTimeHelper month(int month) {
        calendar.set(Calendar.MONTH, month - 1);
        return this;
    }

    public int day() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public DateTimeHelper day(int day) {
        calendar.set(Calendar.DATE, day);
        return this;
    }

    public int hour() {
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public DateTimeHelper hour(int hour) {
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        return this;
    }

    public int minute() {
        return calendar.get(Calendar.MINUTE);
    }

    public DateTimeHelper minute(int minute) {
        calendar.set(Calendar.MINUTE, minute);
        return this;
    }

    public int second() {
        return calendar.get(Calendar.SECOND);
    }

    public DateTimeHelper second(int second) {
        calendar.set(Calendar.SECOND, second);
        return this;
    }

    public int millisecond() {
        return calendar.get(Calendar.MILLISECOND);
    }

    public DateTimeHelper millisecond(int millisecond) {
        calendar.set(Calendar.MILLISECOND, millisecond);
        return this;
    }

    public int dayOfWeek() {
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        if (day == Calendar.SUNDAY) {
            return 7;
        }
        return day - 1;
    }

    public int weekOfMonth() {
        return calendar.get(Calendar.WEEK_OF_MONTH);
    }

    public int weekOfYear() {
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    public int dayOfWeekInMonth() {
        return calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
    }

    public long timeMillis() {
        return calendar.getTimeInMillis();
    }

    public DateTimeHelper timeMillis(long timeMillis) {
        calendar.setTimeInMillis(timeMillis);
        return this;
    }

    public int timeUTC() {
        return (int) (timeMillis() / DateTimeUtils.SECOND);
    }

    public DateTimeHelper timeUTC(long timeUTC) {
        if (String.valueOf(timeUTC).length() <= UTC_TIME_LENGTH) {
            timeUTC *= DateTimeUtils.SECOND;
        }
        calendar.setTimeInMillis(timeUTC);
        return this;
    }

    public int daysOfMonth() {
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public long subtract(Date date) {
        return calendar.getTimeInMillis() - date.getTime();
    }

    public long subtract(DateTimeHelper dateTimeHelper) {
        return subtract(dateTimeHelper.time());
    }

    public DateTimeHelper millisecondsAdd(int milliseconds) {
        calendar.add(Calendar.MILLISECOND, milliseconds);
        return this;
    }

    public DateTimeHelper secondsAdd(int seconds) {
        calendar.add(Calendar.SECOND, seconds);
        return this;
    }

    public DateTimeHelper minutesAdd(int minutes) {
        calendar.add(Calendar.MINUTE, minutes);
        return this;
    }

    public DateTimeHelper hoursAdd(int hours) {
        calendar.add(Calendar.HOUR, hours);
        return this;
    }

    public DateTimeHelper daysAdd(int days) {
        calendar.add(Calendar.DATE, days);
        return this;
    }

    public DateTimeHelper weeksAdd(int weeks) {
        calendar.add(Calendar.WEEK_OF_MONTH, weeks);
        return this;
    }

    public DateTimeHelper monthsAdd(int months) {
        calendar.add(Calendar.MONTH, months);
        return this;
    }

    public DateTimeHelper yearsAdd(int years) {
        calendar.add(Calendar.YEAR, years);
        return this;
    }

    @Override
    public String toString() {
        return toString(DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS);
    }

    public String toString(String dateFormat) {
        SimpleDateFormat fmt = new SimpleDateFormat(dateFormat);
        fmt.setTimeZone(calendar.getTimeZone());
        return fmt.format(calendar.getTime());
    }
}
