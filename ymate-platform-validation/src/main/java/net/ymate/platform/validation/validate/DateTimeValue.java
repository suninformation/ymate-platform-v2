/*
 * Copyright 2007-2020 the original author or authors.
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

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/06/14 19:03
 * @since 2.1.0
 */
public class DateTimeValue implements Serializable {

    private final Date startDate;

    private Date endDate;

    private boolean single;

    public static DateTimeValue get(String paramName) {
        return (DateTimeValue) ValidateContext.getLocalAttributes().get(paramName);
    }

    public static void get(String paramName, IValueProcessor valueProcessor) {
        DateTimeValue dateTimeValue = get(paramName);
        if (dateTimeValue != null) {
            valueProcessor.process(dateTimeValue);
        }
    }

    public static DateTimeValue parse(String dateTimeStr, boolean single) {
        return parse(dateTimeStr, null, null, single);
    }

    public static DateTimeValue parse(String dateTimeStr, String pattern, boolean single) {
        return parse(dateTimeStr, pattern, null, single);
    }

    public static DateTimeValue parse(String dateTimeStr, String pattern, String separator, boolean single) {
        DateTimeValue dateTimeValue = null;
        pattern = StringUtils.defaultIfBlank(pattern, DateTimeUtils.YYYY_MM_DD_HH_MM_SS);
        if (single) {
            Date date = DateTimeValidator.parseDate(dateTimeStr, pattern);
            if (date != null) {
                dateTimeValue = new DateTimeValue(date);
            }
        } else {
            String[] dateTimeArr = StringUtils.split(dateTimeStr, StringUtils.defaultIfBlank(separator, "/"));
            if (ArrayUtils.isNotEmpty(dateTimeArr)) {
                if (dateTimeArr.length <= DateTimeValidator.DATETIME_PART_MAX_LENGTH) {
                    Date dateTimeBegin = DateTimeValidator.parseDate(dateTimeArr[0], pattern);
                    Date dateTimeEnd = null;
                    if (dateTimeBegin != null) {
                        if (dateTimeArr.length > 1 && !StringUtils.equalsIgnoreCase(StringUtils.trim(dateTimeArr[0]), StringUtils.trim(dateTimeArr[1]))) {
                            dateTimeEnd = DateTimeValidator.parseDate(dateTimeArr[1], pattern);
                        }
                        if (dateTimeEnd == null) {
                            dateTimeEnd = DateTimeHelper.bind(dateTimeBegin).toDayEnd().time();
                        }
                        dateTimeValue = new DateTimeValue(dateTimeBegin, dateTimeEnd);
                    }
                }
            }
        }
        return dateTimeValue;
    }

    public DateTimeValue(Date startDate) {
        this.startDate = startDate;
        this.single = true;
    }

    public DateTimeValue(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public boolean isSingle() {
        return single;
    }

    public boolean isNullStartDate() {
        return startDate == null;
    }

    public boolean isNullEndDate() {
        return endDate == null;
    }

    /**
     * @return 获取开始日期毫秒值，若为空则返回0
     */
    public long getStartDateTimeMillis() {
        return isNullStartDate() ? 0 : startDate.getTime();
    }

    public Long getStartDateTimeMillisOrNull() {
        if (isNullStartDate()) {
            return null;
        }
        return startDate.getTime();
    }

    public Timestamp getStartDateTimestampOrNull() {
        if (isNullStartDate()) {
            return null;
        }
        return new Timestamp(startDate.getTime());
    }

    /**
     * @return 获取结束日期毫秒值，若为空则返回0
     */
    public long getEndDateTimeMillis() {
        return isNullEndDate() ? 0 : endDate.getTime();
    }

    public Long getEndDateTimeMillisOrNull() {
        if (isNullEndDate()) {
            return null;
        }
        return endDate.getTime();
    }

    public Timestamp getEndDateTimestampOrNull() {
        if (isNullEndDate()) {
            return null;
        }
        return new Timestamp(endDate.getTime());
    }

    public DateTimeHelper bindStartDate() {
        return DateTimeHelper.bind(startDate);
    }

    public DateTimeHelper bindEndDate() {
        return DateTimeHelper.bind(endDate);
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
