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
package net.ymate.platform.validation.validate;

import net.ymate.platform.commons.DateTimeHelper;
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.validation.ValidateContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/06/14 19:03
 * @since 2.1.0
 */
public class DateTimeValue implements Serializable {

    public static final String TODAY = "today";

    public static final String YESTERDAY = "yesterday";

    public static final String WEEK = "week";

    public static final String MONTH = "month";

    public static final String YEAR = "year";

    public static final int DATETIME_PART_MAX_LENGTH = 2;

    private final ZonedDateTime startDateTime;

    private ZonedDateTime endDateTime;

    private boolean single;

    public static DateTimeValue get(String paramName) {
        return (DateTimeValue) ValidateContext.getLocalAttributes().get(paramName);
    }

    public static DateTimeValue get(String paramName, IValueProcessor valueProcessor) {
        DateTimeValue dateTimeValue = get(paramName);
        if (dateTimeValue != null) {
            valueProcessor.process(dateTimeValue);
        }
        return dateTimeValue;
    }

    /**
     * @since 2.1.3
     */
    public static Long getStartDateTimeMillisOrNull(String paramName) {
        DateTimeValue dateTimeValue = get(paramName);
        return getStartDateTimeMillisOrNull(dateTimeValue);
    }

    /**
     * @since 2.1.4
     */
    public static Long getStartDateTimeMillisOrNull(DateTimeValue dateTimeValue) {
        if (dateTimeValue != null) {
            return dateTimeValue.getStartDateTimeMillisOrNull();
        }
        return null;
    }

    public static DateTimeValue parse(String dateTimeStr, boolean single) {
        return parse(dateTimeStr, null, null, single);
    }

    public static DateTimeValue parse(String dateTimeStr, String pattern, boolean single) {
        return parse(dateTimeStr, pattern, null, single);
    }

    public static DateTimeValue parse(String dateTimeStr, String pattern, String separator, boolean single) {
        DateTimeValue dateTimeValue = null;
        if (single) {
            Date date = parseSingleDate(dateTimeStr, pattern);
            if (date != null) {
                dateTimeValue = new DateTimeValue(date);
            }
        } else {
            dateTimeValue = parseDateRange(dateTimeStr, pattern, separator);
        }
        return dateTimeValue;
    }

    /**
     * 解析单个日期字符串
     *
     * @param dateTimeStr 日期时间字符串
     * @param pattern     日期格式
     * @return 解析后的Date对象
     * @since 2.1.4
     */
    private static Date parseSingleDate(String dateTimeStr, String pattern) {
        if (Strings.CI.equals(dateTimeStr, TODAY)) {
            return DateTimeHelper.now().toDayStart().time();
        } else if (Strings.CI.equals(dateTimeStr, YESTERDAY)) {
            return DateTimeHelper.now().toDayStart().daysAdd(-1).time();
        } else if (Strings.CI.equals(dateTimeStr, WEEK)) {
            return DateTimeHelper.now().toDayStart().toWeekStart().time();
        } else if (Strings.CI.equals(dateTimeStr, MONTH)) {
            return DateTimeHelper.now().toDayStart().day(1).time();
        } else if (Strings.CI.equals(dateTimeStr, YEAR)) {
            return DateTimeHelper.now().toDayStart().month(1).day(1).time();
        } else {
            return DateTimeValidator.parseDate(dateTimeStr, pattern);
        }
    }

    /**
     * 解析日期范围字符串
     *
     * @param dateTimeStr 日期时间字符串
     * @param pattern     日期格式
     * @param separator   日期分隔符
     * @return 解析后的DateTimeValue对象
     * @since 2.1.4
     */
    private static DateTimeValue parseDateRange(String dateTimeStr, String pattern, String separator) {
        if (Strings.CI.equals(dateTimeStr, TODAY)) {
            return today();
        } else if (Strings.CI.equals(dateTimeStr, YESTERDAY)) {
            return yesterday();
        } else if (Strings.CI.equals(dateTimeStr, WEEK)) {
            return week();
        } else if (Strings.CI.equals(dateTimeStr, MONTH)) {
            return month();
        } else if (Strings.CI.equals(dateTimeStr, YEAR)) {
            return year();
        } else {
            String[] dateTimeArr = StringUtils.split(dateTimeStr, StringUtils.defaultIfBlank(separator, "/"));
            if (ArrayUtils.isNotEmpty(dateTimeArr) && dateTimeArr.length <= DATETIME_PART_MAX_LENGTH) {
                Date dateTimeBegin = DateTimeValidator.parseDate(dateTimeArr[0], pattern);
                if (dateTimeBegin != null) {
                    Date dateTimeEnd = null;
                    if (dateTimeArr.length > 1 && !Strings.CI.equals(StringUtils.trim(dateTimeArr[0]), StringUtils.trim(dateTimeArr[1]))) {
                        dateTimeEnd = DateTimeValidator.parseDate(dateTimeArr[1], pattern);
                    }
                    if (dateTimeEnd == null) {
                        dateTimeEnd = DateTimeHelper.bind(dateTimeBegin).toDayEnd().time();
                    }
                    return new DateTimeValue(dateTimeBegin, dateTimeEnd);
                }
            }
        }
        return null;
    }

    /**
     * @param days 天数
     * @return 返回与当前时间相差days天数的日期时间对象
     * @since 2.1.4
     */
    public static DateTimeValue days(int days) {
        return new DateTimeValue(DateTimeHelper.now().daysAdd(-days).toDayStart().zonedDateTime(), DateTimeHelper.now().zonedDateTime());
    }

    /**
     * @param months 月份数
     * @return 返回与当前时间相差months月份数的日期时间对象
     * @since 2.1.4
     */
    public static DateTimeValue months(int months) {
        return new DateTimeValue(DateTimeHelper.now().monthsAdd(-months).toDayStart().zonedDateTime(), DateTimeHelper.now().zonedDateTime());
    }

    /**
     * @param years 年份数
     * @return 返回与当前时间相差years年份数的日期时间对象
     * @since 2.1.4
     */
    public static DateTimeValue years(int years) {
        return new DateTimeValue(DateTimeHelper.now().yearsAdd(-years).toDayStart().zonedDateTime(), DateTimeHelper.now().zonedDateTime());
    }

    /**
     * @return 返回当前时刻的日期时间值对象
     * @since 2.1.2
     */
    public static DateTimeValue now() {
        return new DateTimeValue(DateTimeHelper.now().zonedDateTime());
    }

    /**
     * @return 返回从今天零点到当前时刻的日期时间值对象
     * @since 2.1.2
     */
    public static DateTimeValue today() {
        return new DateTimeValue(DateTimeHelper.now().toDayStart().zonedDateTime(), DateTimeHelper.now().zonedDateTime());
    }

    /**
     * @return 返回从昨天零点到昨天23点59分59秒的时间值对象
     * @since 2.1.3
     */
    public static DateTimeValue yesterday() {
        DateTimeHelper yesterdayHelper = DateTimeHelper.now().toDayStart().daysAdd(-1);
        return new DateTimeValue(yesterdayHelper.timeMillis(), yesterdayHelper.toDayEnd().timeMillis());
    }

    /**
     * @return 返回从本周一零点到今天当前时刻的日期时间值对象
     * @since 2.1.2
     */
    public static DateTimeValue week() {
        return new DateTimeValue(DateTimeHelper.now().toWeekStart().zonedDateTime(), DateTimeHelper.now().zonedDateTime());
    }

    /**
     * @return 返回本月一号零点到今天当前时刻的日期时间值对象
     * @since 2.1.2
     */
    public static DateTimeValue month() {
        return new DateTimeValue(DateTimeHelper.now().day(1).toDayStart().zonedDateTime(), DateTimeHelper.now().zonedDateTime());
    }

    /**
     * @return 返回本年一月一号零点到今天当前时刻的日期时间值对象
     * @since 2.1.3
     */
    public static DateTimeValue year() {
        return new DateTimeValue(DateTimeHelper.now().month(1).day(1).toDayStart().zonedDateTime(), DateTimeHelper.now().zonedDateTime());
    }

    /**
     * 构造方法，根据开始日期时间毫秒值创建单日期对象
     *
     * @param startDate 开始日期时间毫秒值
     * @since 2.1.3
     */
    public DateTimeValue(long startDate) {
        this(DateTimeHelper.bind(startDate).zonedDateTime());
    }

    /**
     * 构造方法，根据开始和结束日期时间毫秒值创建日期范围对象
     *
     * @param startDate 开始日期时间毫秒值
     * @param endDate   结束日期时间毫秒值
     * @since 2.1.3
     */
    public DateTimeValue(long startDate, long endDate) {
        this(DateTimeHelper.bind(startDate).zonedDateTime(), DateTimeHelper.bind(endDate).zonedDateTime());
    }

    /**
     * 构造方法，根据Date对象创建单日期对象
     *
     * @param startDate 开始日期Date对象
     */
    public DateTimeValue(Date startDate) {
        this.startDateTime = startDate != null ? DateTimeHelper.bind(startDate).zonedDateTime() : null;
        this.single = true;
    }

    /**
     * 构造方法，根据Date对象创建日期范围对象
     *
     * @param startDate 开始日期Date对象
     * @param endDate   结束日期Date对象
     */
    public DateTimeValue(Date startDate, Date endDate) {
        this.startDateTime = startDate != null ? DateTimeHelper.bind(startDate).zonedDateTime() : null;
        this.endDateTime = endDate != null ? DateTimeHelper.bind(endDate).zonedDateTime() : null;
    }

    /**
     * @since 2.1.4
     */
    public DateTimeValue(LocalDateTime startDateTime) {
        this(startDateTime != null ? startDateTime.atZone(ZoneId.systemDefault()) : null);
    }

    /**
     * @since 2.1.4
     */
    public DateTimeValue(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this(startDateTime != null ? startDateTime.atZone(ZoneId.systemDefault()) : null,
                endDateTime != null ? endDateTime.atZone(ZoneId.systemDefault()) : null);
    }

    /**
     * @since 2.1.4
     */
    public DateTimeValue(LocalDateTime startDateTime, ZoneId zoneId) {
        this(startDateTime != null ? startDateTime.atZone(zoneId) : null);
    }

    /**
     * @since 2.1.4
     */
    public DateTimeValue(LocalDateTime startDateTime, LocalDateTime endDateTime, ZoneId zoneId) {
        this(startDateTime != null ? startDateTime.atZone(zoneId) : null,
                endDateTime != null ? endDateTime.atZone(zoneId) : null);
    }

    /**
     * @since 2.1.4
     */
    public DateTimeValue(ZonedDateTime startDateTime) {
        this.startDateTime = startDateTime;
        this.single = true;
    }

    /**
     * @since 2.1.4
     */
    public DateTimeValue(ZonedDateTime startDateTime, ZonedDateTime endDateTime) {
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public Date getStartDate() {
        return startDateTime != null ? Date.from(startDateTime.toInstant()) : null;
    }

    public Date getEndDate() {
        return endDateTime != null ? Date.from(endDateTime.toInstant()) : null;
    }

    public boolean isSingle() {
        return single;
    }

    public boolean isNullStartDate() {
        return startDateTime == null;
    }

    public boolean isNullEndDate() {
        return endDateTime == null;
    }

    /**
     * @since 2.1.4
     */
    public ZonedDateTime getStartZonedDateTime() {
        return startDateTime;
    }

    /**
     * @since 2.1.4
     */
    public ZonedDateTime getEndZonedDateTime() {
        return endDateTime;
    }

    /**
     * @since 2.1.4
     */
    public LocalDateTime getStartLocalDateTime() {
        return startDateTime != null ? startDateTime.toLocalDateTime() : null;
    }

    /**
     * @since 2.1.4
     */
    public LocalDateTime getEndLocalDateTime() {
        return endDateTime != null ? endDateTime.toLocalDateTime() : null;
    }

    /**
     * @return 获取开始日期毫秒值，若为空则返回0
     */
    public long getStartDateTimeMillis() {
        return isNullStartDate() ? 0 : startDateTime.toInstant().toEpochMilli();
    }

    public Long getStartDateTimeMillisOrNull() {
        if (isNullStartDate()) {
            return null;
        }
        return startDateTime.toInstant().toEpochMilli();
    }

    public Timestamp getStartDateTimestampOrNull() {
        if (isNullStartDate()) {
            return null;
        }
        return Timestamp.from(startDateTime.toInstant());
    }

    /**
     * @return 获取结束日期毫秒值，若为空则返回0
     */
    public long getEndDateTimeMillis() {
        return isNullEndDate() ? 0 : endDateTime.toInstant().toEpochMilli();
    }

    public Long getEndDateTimeMillisOrNull() {
        if (isNullEndDate()) {
            return null;
        }
        return endDateTime.toInstant().toEpochMilli();
    }

    public Timestamp getEndDateTimestampOrNull() {
        if (isNullEndDate()) {
            return null;
        }
        return Timestamp.from(endDateTime.toInstant());
    }

    public DateTimeHelper bindStartDate() {
        if (isNullStartDate()) {
            return null;
        }
        return DateTimeHelper.bind(startDateTime);
    }

    public DateTimeHelper bindEndDate() {
        if (isNullEndDate()) {
            return null;
        }
        return DateTimeHelper.bind(endDateTime);
    }

    /**
     * @return 计算两日期之间相差天数（绝对值）
     * @since 2.1.2
     */
    public long getMaxDays() {
        return getMaxTimeMillis() / DateTimeUtils.DAY;
    }

    /**
     * @return 计算两日期之间相差毫秒值（绝对值）
     * @since 2.1.2
     */
    public long getMaxTimeMillis() {
        if (isNullStartDate() || isNullEndDate()) {
            return 0L;
        }
        return Math.abs(startDateTime.toInstant().toEpochMilli() - endDateTime.toInstant().toEpochMilli());
    }

    /**
     * @return 以字符串输出
     * @since 2.1.2
     */
    @Override
    public String toString() {
        return toString(DateTimeUtils.YYYY_MM_DD_HH_MM_SS, null);
    }

    /**
     * @param dateFormat 日期格式字符串
     * @param separator  时间段字符串之间的分割符号
     * @return 以字符串输出
     * @since 2.1.2
     */
    public String toString(String dateFormat, String separator) {
        StringBuilder stringBuilder = new StringBuilder();
        if (startDateTime != null) {
            stringBuilder.append(DateTimeHelper.bind(startDateTime).toString(dateFormat));
        }
        if (endDateTime != null) {
            stringBuilder.append(StringUtils.SPACE)
                    .append(StringUtils.defaultIfBlank(separator, "/"))
                    .append(StringUtils.SPACE)
                    .append(DateTimeHelper.bind(endDateTime).toString(dateFormat));
        }
        return stringBuilder.toString();
    }

    public interface IValueProcessor {

        /**
         * 处理日期时间值
         *
         * @param dateTimeValue 日期时间值对象
         */
        void process(DateTimeValue dateTimeValue);
    }
}
