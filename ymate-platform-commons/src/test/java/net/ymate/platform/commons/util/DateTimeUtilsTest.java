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
package net.ymate.platform.commons.util;

import org.junit.Test;

import java.time.*;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.*;

/**
 * DateTimeUtils类的单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-05 15:30:00
 * @since 2.1.4
 */
public class DateTimeUtilsTest {

    /**
     * 测试常量定义
     * <p>
     * 测试内容：
     * <ul>
     *     <li>时间常量：SECOND、MINUTE、HOUR、DAY、WEEK、YEAR</li>
     *     <li>日期格式常量：YYYY_MM_DD_HH_MM_SS_SSS、YYYY_MM_DD_HH_MM_SS、YYYY_MM_DD、YYYY_MM、YYYY_MM_DD_HH_MM</li>
     *     <li>其他常量：UTC_LENGTH、TIME_ZONES</li>
     * </ul>
     * </p>
     */
    @Test
    public void testConstants() {
        // 测试时间常量
        assertEquals(1000L, DateTimeUtils.SECOND);
        assertEquals(60_000L, DateTimeUtils.MINUTE);
        assertEquals(3_600_000L, DateTimeUtils.HOUR);
        assertEquals(86_400_000L, DateTimeUtils.DAY);
        assertEquals(604_800_000L, DateTimeUtils.WEEK);
        assertEquals(31_536_000_000L, DateTimeUtils.YEAR);

        // 测试日期格式常量
        assertEquals("yyyy-MM-dd HH:mm:ss.SSS", DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS);
        assertEquals("yyyy-MM-dd HH:mm:ss", DateTimeUtils.YYYY_MM_DD_HH_MM_SS);
        assertEquals("yyyy-MM-dd", DateTimeUtils.YYYY_MM_DD);
        assertEquals("yyyy-MM", DateTimeUtils.YYYY_MM);
        assertEquals("yyyy-MM-dd HH:mm", DateTimeUtils.YYYY_MM_DD_HH_MM);

        // 测试其他常量
        assertEquals(10, DateTimeUtils.UTC_LENGTH);
        assertNotNull(DateTimeUtils.TIME_ZONES);
        assertFalse(DateTimeUtils.TIME_ZONES.isEmpty());
    }

    /**
     * 测试currentTimeMillis方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>验证currentTimeMillis()返回的值与System.currentTimeMillis()的值接近</li>
     * </ul>
     * </p>
     */
    @Test
    public void testCurrentTimeMillis() {
        long millis1 = System.currentTimeMillis();
        long millis2 = DateTimeUtils.currentTimeMillis();
        assertTrue(Math.abs(millis1 - millis2) < 100);
    }

    /**
     * 测试currentTimeUTC方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>验证currentTimeUTC()返回的值与Instant.now().getEpochSecond()的值接近</li>
     * </ul>
     * </p>
     */
    @Test
    public void testCurrentTimeUTC() {
        long utc1 = Instant.now().getEpochSecond();
        long utc2 = DateTimeUtils.currentTimeUTC();
        assertTrue(Math.abs(utc1 - utc2) < 2);
    }

    /**
     * 测试currentTime方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>验证currentTime()返回的值与new Date()的值接近</li>
     * </ul>
     * </p>
     */
    @Test
    public void testCurrentTime() {
        Date now1 = new Date();
        Date now2 = DateTimeUtils.currentTime();
        assertTrue(Math.abs(now1.getTime() - now2.getTime()) < 100);
    }

    /**
     * 测试systemTimeUTC方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>验证systemTimeUTC()返回的值与Instant.now().getEpochSecond()的值接近</li>
     * </ul>
     * </p>
     */
    @Test
    public void testSystemTimeUTC() {
        int systemUtc1 = (int) Instant.now().getEpochSecond();
        int systemUtc2 = DateTimeUtils.systemTimeUTC();
        assertTrue(Math.abs(systemUtc1 - systemUtc2) < 2);
    }

    /**
     * 测试formatTime方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>测试毫秒级时间戳格式化</li>
     *     <li>测试秒级时间戳格式化</li>
     *     <li>测试带时区的格式化</li>
     *     <li>测试默认格式格式化</li>
     * </ul>
     * </p>
     */
    @Test
    public void testFormatTime() {
        // 测试毫秒级时间戳
        long millisTime = 1672531200000L; // 2023-01-01 00:00:00 UTC
        // 使用UTC时区进行格式化，确保结果一致
        String millisFormatted = DateTimeUtils.formatTime(millisTime, DateTimeUtils.YYYY_MM_DD_HH_MM_SS, "0");
        assertEquals("2023-01-01 00:00:00", millisFormatted);

        // 测试秒级时间戳
        long secondsTime = 1672531200L; // 2023-01-01 00:00:00 UTC
        // 使用UTC时区进行格式化，确保结果一致
        String secondsFormatted = DateTimeUtils.formatTime(secondsTime, DateTimeUtils.YYYY_MM_DD_HH_MM_SS, "0");
        assertEquals("2023-01-01 00:00:00", secondsFormatted);

        // 测试带时区的格式化
        String utcFormatted = DateTimeUtils.formatTime(millisTime, DateTimeUtils.YYYY_MM_DD_HH_MM_SS, "0");
        assertEquals("2023-01-01 00:00:00", utcFormatted);

        // 测试默认格式
        // 默认格式会使用系统时区，所以不直接断言具体时间，而是检查是否包含日期部分
        String defaultFormatted = DateTimeUtils.formatTime(millisTime, null);
        assertTrue(defaultFormatted.contains("2023-01-01"));
        // 或者使用UTC时区进行测试，确保结果一致
        String defaultFormattedUtc = DateTimeUtils.formatTime(millisTime, DateTimeUtils.YYYY_MM_DD_HH_MM_SS_SSS, "0");
        assertTrue(defaultFormattedUtc.startsWith("2023-01-01 00:00:00"));
    }

    /**
     * 测试parseDateTime方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>测试标准格式解析</li>
     *     <li>测试带时区的解析</li>
     *     <li>测试ISO格式解析</li>
     *     <li>测试默认格式解析</li>
     * </ul>
     * </p>
     *
     * @throws Exception 解析异常
     */
    @Test
    public void testParseDateTime() throws Exception {
        // 测试标准格式解析
        String dateStr = "2023-01-01 12:00:00";
        Date parsedDate = DateTimeUtils.parseDateTime(dateStr, DateTimeUtils.YYYY_MM_DD_HH_MM_SS);
        assertNotNull(parsedDate);

        // 测试带时区的解析
        Date parsedDateWithZone = DateTimeUtils.parseDateTime(dateStr, DateTimeUtils.YYYY_MM_DD_HH_MM_SS, "8");
        assertNotNull(parsedDateWithZone);

        // 测试ISO格式解析
        String isoStr = "2023-01-01T12:00:00Z";
        Date parsedIsoDate = DateTimeUtils.parseDateTime(isoStr, null);
        assertNotNull(parsedIsoDate);

        // 测试默认格式解析
        Date parsedDefault = DateTimeUtils.parseDateTime("2023-01-01 12:00:00", null);
        assertNotNull(parsedDefault);
    }

    /**
     * 测试isLeapYear方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>测试闰年（如2024年、2000年）</li>
     *     <li>测试平年（如2023年、1900年）</li>
     * </ul>
     * </p>
     */
    @Test
    public void testIsLeapYear() {
        assertTrue(DateTimeUtils.isLeapYear(2024));
        assertFalse(DateTimeUtils.isLeapYear(2023));
        assertTrue(DateTimeUtils.isLeapYear(2000));
        assertFalse(DateTimeUtils.isLeapYear(1900));
    }

    /**
     * 测试timeMillis方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>测试Date对象</li>
     *     <li>测试java.sql.Date对象</li>
     *     <li>测试LocalDate对象</li>
     *     <li>测试LocalTime对象</li>
     *     <li>测试LocalDateTime对象</li>
     *     <li>测试ZonedDateTime对象</li>
     *     <li>测试Instant对象</li>
     *     <li>测试null值</li>
     * </ul>
     * </p>
     */
    @Test
    public void testTimeMillis() {
        Date date = new Date();
        long expectedMillis = date.getTime();

        // 测试Date对象
        assertEquals(expectedMillis, DateTimeUtils.timeMillis(date));

        // 测试java.sql.Date对象
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        assertEquals(expectedMillis, DateTimeUtils.timeMillis(sqlDate));

        // 测试LocalDate对象
        LocalDate localDate = LocalDate.now();
        assertEquals(java.sql.Date.valueOf(localDate).getTime(), DateTimeUtils.timeMillis(localDate));

        // 测试LocalTime对象
        LocalTime localTime = LocalTime.now();
        assertEquals(java.sql.Time.valueOf(localTime).getTime(), DateTimeUtils.timeMillis(localTime));

        // 测试LocalDateTime对象
        LocalDateTime localDateTime = LocalDateTime.now();
        assertEquals(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant()).getTime(), DateTimeUtils.timeMillis(localDateTime));

        // 测试ZonedDateTime对象
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        assertEquals(Date.from(zonedDateTime.toInstant()).getTime(), DateTimeUtils.timeMillis(zonedDateTime));

        // 测试Instant对象
        Instant instant = Instant.now();
        assertEquals(Date.from(instant).getTime(), DateTimeUtils.timeMillis(instant));

        // 测试null值
        assertEquals(0L, DateTimeUtils.timeMillis(null));
    }

    /**
     * 测试getTimeZone方法
     * <p>
     * 测试内容：
     * <ul>
     *     <li>使用时区偏移获取时区（如"8"）</li>
     *     <li>使用时区ID获取时区（如"UTC"）</li>
     *     <li>使用无效时区偏移（返回null）</li>
     *     <li>使用空时区偏移（返回null）</li>
     * </ul>
     * </p>
     */
    @Test
    public void testGetTimeZone() {
        // 测试使用时区偏移获取时区
        TimeZone tz1 = DateTimeUtils.getTimeZone("8");
        assertEquals(TimeZone.getTimeZone("UTC+08:00"), tz1);

        // 测试使用时区ID获取时区
        TimeZone tz2 = DateTimeUtils.getTimeZone("UTC");
        assertEquals(TimeZone.getTimeZone("UTC"), tz2);

        // 测试使用无效时区偏移
        TimeZone tz3 = DateTimeUtils.getTimeZone("invalid");
        assertNull(tz3);

        // 测试使用空时区偏移
        TimeZone tz4 = DateTimeUtils.getTimeZone(null);
        assertNull(tz4);
    }

    /**
     * 测试TIME_ZONES映射表
     * <p>
     * 测试内容：
     * <ul>
     *     <li>测试时区映射表包含关键时区（如"0"、"8"、"-5"）</li>
     *     <li>测试时区映射表的结构（每个时区映射包含时区ID和描述）</li>
     * </ul>
     * </p>
     */
    @Test
    public void testTimeZones() {
        // 测试时区映射表包含关键时区
        assertTrue(DateTimeUtils.TIME_ZONES.containsKey("0"));
        assertTrue(DateTimeUtils.TIME_ZONES.containsKey("8"));
        assertTrue(DateTimeUtils.TIME_ZONES.containsKey("-5"));

        // 测试时区映射表的结构
        String[] utcZone = DateTimeUtils.TIME_ZONES.get("0");
        assertNotNull(utcZone);
        assertEquals(2, utcZone.length);
        assertEquals("UTC", utcZone[0]);
    }
}
