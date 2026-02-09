/*
 * Copyright 2007-present the original author or authors.
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
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * DateTimeHelper测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-07 13:05:41
 * @since 2.1.4
 */
public class DateTimeHelperTest {

    @Test
    public void testBindMethods() {
        // 测试bind(Date)
        Date now = new Date();
        DateTimeHelper helper1 = DateTimeHelper.bind(now);
        Assert.assertNotNull(helper1);
        Assert.assertEquals(now.getTime(), helper1.time().getTime());

        // 测试bind(long) - 毫秒级时间戳
        long timestamp = System.currentTimeMillis();
        DateTimeHelper helper2 = DateTimeHelper.bind(timestamp);
        Assert.assertNotNull(helper2);
        Assert.assertTrue(Math.abs(timestamp - helper2.timeMillis()) < 1000);

        // 测试bind(long) - 秒级时间戳
        long seconds = timestamp / 1000;
        DateTimeHelper helper3 = DateTimeHelper.bind(seconds);
        Assert.assertNotNull(helper3);
        Assert.assertTrue(Math.abs(timestamp - helper3.timeMillis()) < 1000);

        // 测试bind(String, String)
        String dateStr = "2023-12-25 10:30:45";
        String format = DateTimeUtils.YYYY_MM_DD_HH_MM_SS;
        DateTimeHelper helper4 = DateTimeHelper.bind(dateStr, format);
        Assert.assertNotNull(helper4);
        Assert.assertEquals("2023-12-25 10:30:45", helper4.toString(format));

        // 测试bind(LocalDateTime)
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeHelper helper5 = DateTimeHelper.bind(localDateTime);
        Assert.assertNotNull(helper5);
        Assert.assertEquals(localDateTime.getYear(), helper5.year());
        Assert.assertEquals(localDateTime.getMonthValue(), helper5.month());
        Assert.assertEquals(localDateTime.getDayOfMonth(), helper5.day());

        // 测试bind(LocalDateTime, ZoneId)
        ZoneId zoneId = ZoneId.of("Asia/Shanghai");
        DateTimeHelper helper6 = DateTimeHelper.bind(localDateTime, zoneId);
        Assert.assertNotNull(helper6);
        Assert.assertEquals(zoneId, helper6.zoneId());

        // 测试bind(ZonedDateTime)
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        DateTimeHelper helper7 = DateTimeHelper.bind(zonedDateTime);
        Assert.assertNotNull(helper7);
        Assert.assertEquals(zonedDateTime.getYear(), helper7.year());
        Assert.assertEquals(zonedDateTime.getZone(), helper7.zoneId());

        // 测试bind(int, int, int)
        DateTimeHelper helper8 = DateTimeHelper.bind(2024, 1, 1);
        Assert.assertNotNull(helper8);
        Assert.assertEquals(2024, helper8.year());
        Assert.assertEquals(1, helper8.month());
        Assert.assertEquals(1, helper8.day());

        // 测试bind(int, int, int, int, int, int)
        DateTimeHelper helper9 = DateTimeHelper.bind(2024, 1, 1, 12, 0, 0);
        Assert.assertNotNull(helper9);
        Assert.assertEquals(2024, helper9.year());
        Assert.assertEquals(1, helper9.month());
        Assert.assertEquals(1, helper9.day());
        Assert.assertEquals(12, helper9.hour());
        Assert.assertEquals(0, helper9.minute());
        Assert.assertEquals(0, helper9.second());

        // 测试now()
        DateTimeHelper helper10 = DateTimeHelper.now();
        Assert.assertNotNull(helper10);
        Assert.assertTrue(Math.abs(System.currentTimeMillis() - helper10.timeMillis()) < 1000);
    }

    @Test
    public void testGetterMethods() {
        DateTimeHelper helper = DateTimeHelper.bind(2024, 6, 15, 14, 30, 45);

        // 测试基本时间字段获取
        Assert.assertEquals(2024, helper.year());
        Assert.assertEquals(6, helper.month());
        Assert.assertEquals(15, helper.day());
        Assert.assertEquals(14, helper.hour());
        Assert.assertEquals(30, helper.minute());
        Assert.assertEquals(45, helper.second());

        // 测试星期相关方法
        Assert.assertTrue(helper.dayOfWeek() >= 1 && helper.dayOfWeek() <= 7);
        Assert.assertTrue(helper.weekOfMonth() >= 1 && helper.weekOfMonth() <= 5);
        Assert.assertTrue(helper.weekOfYear() >= 1 && helper.weekOfYear() <= 53);
        Assert.assertTrue(helper.dayOfWeekInMonth() >= 1 && helper.dayOfWeekInMonth() <= 5);

        // 测试月份天数
        Assert.assertTrue(helper.daysOfMonth() >= 28 && helper.daysOfMonth() <= 31);

        // 测试时间戳相关方法
        Assert.assertTrue(helper.timeMillis() > 0);
        Assert.assertTrue(helper.timeUTC() > 0);

        // 测试对象转换方法
        Assert.assertNotNull(helper.time());
        Assert.assertNotNull(helper.localDateTime());
        Assert.assertNotNull(helper.localDate());
        Assert.assertNotNull(helper.localTime());
        Assert.assertNotNull(helper.zoneId());
        Assert.assertNotNull(helper.zonedDateTime());
        Assert.assertNotNull(helper.timeZone());

        // 测试Calendar获取（兼容旧API）
        Assert.assertNotNull(helper.getCalendar());
    }

    @Test
    public void testSetterMethods() {
        DateTimeHelper helper = DateTimeHelper.bind(2024, 1, 1, 0, 0, 0);

        // 测试年份设置
        helper.year(2025);
        Assert.assertEquals(2025, helper.year());

        // 测试月份设置
        helper.month(6);
        Assert.assertEquals(6, helper.month());

        // 测试日期设置
        helper.day(15);
        Assert.assertEquals(15, helper.day());

        // 测试小时设置
        helper.hour(14);
        Assert.assertEquals(14, helper.hour());

        // 测试分钟设置
        helper.minute(30);
        Assert.assertEquals(30, helper.minute());

        // 测试秒设置
        helper.second(45);
        Assert.assertEquals(45, helper.second());

        // 测试毫秒设置
        helper.millisecond(500);
        Assert.assertEquals(500, helper.millisecond());

        // 测试时间戳设置
        long newTimestamp = System.currentTimeMillis();
        helper.timeMillis(newTimestamp);
        Assert.assertEquals(newTimestamp, helper.timeMillis());

        // 测试UTC时间设置
        long newUtc = newTimestamp / 1000;
        helper.timeUTC(newUtc);
        Assert.assertEquals(newUtc, helper.timeUTC());

        // 测试Date设置
        Date newDate = new Date(newTimestamp);
        helper.time(newDate);
        Assert.assertEquals(newDate.getTime(), helper.time().getTime());

        // 测试LocalDateTime设置
        LocalDateTime newLocalDateTime = LocalDateTime.now();
        helper.time(newLocalDateTime);
        Assert.assertEquals(newLocalDateTime.getYear(), helper.year());
        Assert.assertEquals(newLocalDateTime.getMonthValue(), helper.month());
        Assert.assertEquals(newLocalDateTime.getDayOfMonth(), helper.day());

        // 测试LocalDateTime和时区设置
        ZoneId newZoneId = ZoneId.of("America/New_York");
        helper.time(newLocalDateTime, newZoneId);
        Assert.assertEquals(newLocalDateTime.getYear(), helper.year());
        Assert.assertEquals(newZoneId, helper.zoneId());
    }

    @Test
    public void testTimeZoneMethods() {
        DateTimeHelper helper = DateTimeHelper.now();
        ZoneId originalZone = helper.zoneId();

        // 测试设置时区（ZoneId）
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        helper.timeZone(shanghaiZone);
        Assert.assertEquals(shanghaiZone, helper.zoneId());

        // 测试设置时区（TimeZone）
        TimeZone newYorkTimeZone = TimeZone.getTimeZone("America/New_York");
        helper.timeZone(newYorkTimeZone);
        Assert.assertEquals(newYorkTimeZone.getID(), helper.zoneId().getId());

        // 测试获取时区
        Assert.assertNotNull(helper.timeZone());
    }

    @Test
    public void testDateAdjustmentMethods() {
        DateTimeHelper helper = DateTimeHelper.bind(2024, 6, 15, 14, 30, 45);

        // 测试到当天开始
        helper.toDayStart();
        Assert.assertEquals(0, helper.hour());
        Assert.assertEquals(0, helper.minute());
        Assert.assertEquals(0, helper.second());
        Assert.assertEquals(0, helper.millisecond());

        // 测试到当天结束
        helper.toDayEnd();
        Assert.assertEquals(23, helper.hour());
        Assert.assertEquals(59, helper.minute());
        Assert.assertEquals(59, helper.second());
        Assert.assertEquals(999, helper.millisecond());

        // 测试到当月开始
        helper = DateTimeHelper.bind(2024, 6, 15, 14, 30, 45);
        helper.toMonthStart();
        Assert.assertEquals(1, helper.day());
        Assert.assertEquals(0, helper.hour());
        Assert.assertEquals(0, helper.minute());
        Assert.assertEquals(0, helper.second());

        // 测试到当月结束
        helper.toMonthEnd();
        Assert.assertEquals(30, helper.day()); // 6月有30天
        Assert.assertEquals(23, helper.hour());
        Assert.assertEquals(59, helper.minute());
        Assert.assertEquals(59, helper.second());

        // 测试到当周开始（周一）
        helper = DateTimeHelper.bind(2024, 6, 15, 14, 30, 45); // 2024-06-15 是周六
        helper.toWeekStart();
        Assert.assertEquals(10, helper.day()); // 2024-06-10 是周一

        // 测试到当周结束（周日）
        helper.toWeekEnd();
        Assert.assertEquals(16, helper.day()); // 2024-06-16 是周日
    }

    @Test
    public void testAddMethods() {
        DateTimeHelper helper = DateTimeHelper.bind(2024, 1, 1, 0, 0, 0);

        // 测试毫秒增加
        long originalMillis = helper.timeMillis();
        helper.millisecondsAdd(1000);
        Assert.assertEquals(originalMillis + 1000, helper.timeMillis());

        // 测试秒增加
        helper = DateTimeHelper.bind(2024, 1, 1, 0, 0, 0);
        helper.secondsAdd(30);
        Assert.assertEquals(30, helper.second());

        // 测试分钟增加
        helper = DateTimeHelper.bind(2024, 1, 1, 0, 0, 0);
        helper.minutesAdd(45);
        Assert.assertEquals(45, helper.minute());

        // 测试小时增加
        helper = DateTimeHelper.bind(2024, 1, 1, 0, 0, 0);
        helper.hoursAdd(5);
        Assert.assertEquals(5, helper.hour());

        // 测试天增加
        helper = DateTimeHelper.bind(2024, 1, 1, 0, 0, 0);
        helper.daysAdd(7);
        Assert.assertEquals(8, helper.day());

        // 测试周增加
        helper = DateTimeHelper.bind(2024, 1, 1, 0, 0, 0);
        helper.weeksAdd(1);
        Assert.assertEquals(8, helper.day());

        // 测试月增加
        helper = DateTimeHelper.bind(2024, 1, 1, 0, 0, 0);
        helper.monthsAdd(1);
        Assert.assertEquals(2, helper.month());

        // 测试年增加
        helper = DateTimeHelper.bind(2024, 1, 1, 0, 0, 0);
        helper.yearsAdd(1);
        Assert.assertEquals(2025, helper.year());
    }

    @Test
    public void testSubtractMethods() {
        DateTimeHelper helper1 = DateTimeHelper.bind(2024, 1, 2, 0, 0, 0);
        DateTimeHelper helper2 = DateTimeHelper.bind(2024, 1, 1, 0, 0, 0);

        // 测试与DateTimeHelper的时间差
        long diff1 = helper1.subtract(helper2);
        Assert.assertEquals(86400000, diff1); // 1天 = 86400000毫秒

        // 测试与Date的时间差
        long diff2 = helper1.subtract(helper2.time());
        Assert.assertEquals(86400000, diff2);

        // 测试与LocalDateTime的时间差
        long diff3 = helper1.subtract(helper2.localDateTime());
        Assert.assertEquals(86400000, diff3);

        // 测试与LocalDateTime和时区的时间差
        long diff4 = helper1.subtract(helper2.localDateTime(), helper2.zoneId());
        Assert.assertEquals(86400000, diff4);

        // 测试与LocalDate的时间差
        long diff5 = helper1.subtract(helper2.localDate());
        Assert.assertEquals(86400000, diff5);

        // 测试与LocalDate和时区的时间差
        long diff6 = helper1.subtract(helper2.localDate(), helper2.zoneId());
        Assert.assertEquals(86400000, diff6);

        // 测试与ZonedDateTime的时间差
        long diff7 = helper1.subtract(helper2.zonedDateTime());
        Assert.assertEquals(86400000, diff7);

        // 测试与Instant的时间差
        long diff8 = helper1.subtract(helper2.zonedDateTime().toInstant());
        Assert.assertEquals(86400000, diff8);
    }

    @Test
    public void testToStringMethods() {
        DateTimeHelper helper = DateTimeHelper.bind(2024, 12, 25, 10, 30, 45);

        // 测试默认toString
        String defaultStr = helper.toString();
        Assert.assertNotNull(defaultStr);
        Assert.assertTrue(defaultStr.contains("2024"));
        Assert.assertTrue(defaultStr.contains("12"));
        Assert.assertTrue(defaultStr.contains("25"));

        // 测试指定格式toString
        String format = DateTimeUtils.YYYY_MM_DD_HH_MM_SS;
        String formattedStr = helper.toString(format);
        Assert.assertEquals("2024-12-25 10:30:45", formattedStr);

        // 测试其他格式
        String shortFormat = "yyyy-MM-dd";
        String shortFormattedStr = helper.toString(shortFormat);
        Assert.assertEquals("2024-12-25", shortFormattedStr);

        String timeFormat = "HH:mm:ss";
        String timeFormattedStr = helper.toString(timeFormat);
        Assert.assertEquals("10:30:45", timeFormattedStr);
    }

    @Test
    public void testLeapYear() {
        // 测试闰年
        DateTimeHelper leapYearHelper = DateTimeHelper.bind(2024, 2, 1);
        Assert.assertTrue(leapYearHelper.isLeapYear());

        // 测试非闰年
        DateTimeHelper nonLeapYearHelper = DateTimeHelper.bind(2023, 2, 1);
        Assert.assertFalse(nonLeapYearHelper.isLeapYear());
    }
}
