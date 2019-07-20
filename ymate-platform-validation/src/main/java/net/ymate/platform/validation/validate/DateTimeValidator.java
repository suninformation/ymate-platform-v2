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
package net.ymate.platform.validation.validate;

import net.ymate.platform.commons.DateTimeHelper;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.commons.util.DateTimeUtils;
import net.ymate.platform.core.beans.annotation.CleanProxy;
import net.ymate.platform.validation.IValidator;
import net.ymate.platform.validation.ValidateContext;
import net.ymate.platform.validation.ValidateResult;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.util.Date;

/**
 * 日期类型参数验证
 *
 * @author 刘镇 (suninformation@163.com) on 2013-4-17 下午9:43:55
 */
@CleanProxy
public final class DateTimeValidator implements IValidator {

    private static final int DATETIME_PART_MAX_LENGTH = 2;

    private static final String I18N_MESSAGE_KEY = "ymp.validation.datetime";

    private static final String I18N_MESSAGE_DEFAULT_VALUE = "{0} not a valid datetime.";

    private static final String I18N_MESSAGE_MAX_DAYS_KEY = "ymp.validation.datetime_max_days";

    private static final String I18N_MESSAGE_MAX_DAYS_DEFAULT_VALUE = "{0} has exceeded the max days.";

    /**
     * 根据paramValue字符串创建时间日期对象
     *
     * @param paramValue 参数值
     * @return 返回日期对象，若非法则返回null
     * @since 2.1.0
     */
    public static Date parseDate(String paramValue) {
        return parseDate(paramValue, DateTimeUtils.YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 验证paramValue字符串是否为合法的时间戳
     *
     * @param paramValue 参数值
     * @param pattern    日期时间格式
     * @return 返回日期对象，若非法则返回null
     * @since 2.1.0
     */
    public static Date parseDate(String paramValue, String pattern) {
        try {
            return DateTimeUtils.parseDateTime(paramValue, StringUtils.defaultIfBlank(pattern, DateTimeUtils.YYYY_MM_DD_HH_MM_SS));
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * @param paramName  自定参数名称
     * @param paramValue 待验证参数值对象
     * @param pattern    日期格式字符串
     * @param separator  时间段字符串之间的分割符号
     * @param maxDays    时间段之间的天数最大差值，默认为0表示不限制
     * @param single     仅接收单日期(即所选日期的00点00分00秒0毫秒到所选日期的23点59分59秒0毫秒)
     * @return 返回结果为0表示合法，为1表示日期字符串格式无效，为2表示时间段差值超过限定天数，为3表示开始日期时间大于结束日期时间
     */
    private int validate(String paramName, String paramValue, String pattern, String separator, int maxDays, boolean single) {
        int result = 0;
        pattern = StringUtils.defaultIfBlank(pattern, DateTimeUtils.YYYY_MM_DD_HH_MM_SS);
        if (single) {
            Date date = parseDate(paramValue, pattern);
            if (date == null) {
                result = 1;
            } else {
                ValidateContext.getLocalAttributes().put(paramName, date);
            }
        } else {
            String[] dateTimeArr = StringUtils.split(paramValue, StringUtils.defaultIfBlank(separator, "/"));
            if (ArrayUtils.isNotEmpty(dateTimeArr)) {
                if (dateTimeArr.length <= DATETIME_PART_MAX_LENGTH) {
                    Date dateTimeBegin = parseDate(dateTimeArr[0], pattern);
                    Date dateTimeEnd = null;
                    if (dateTimeBegin != null) {
                        if (dateTimeArr.length > 1 && !StringUtils.equalsIgnoreCase(StringUtils.trim(dateTimeArr[0]), StringUtils.trim(dateTimeArr[1]))) {
                            dateTimeEnd = parseDate(dateTimeArr[1], pattern);
                            if (dateTimeEnd != null) {
                                if (dateTimeBegin.getTime() < dateTimeEnd.getTime()) {
                                    result = 3;
                                } else if (maxDays > 0) {
                                    long days = DateTimeHelper.bind(dateTimeBegin).subtract(dateTimeEnd) / DateTimeUtils.DAY;
                                    if (days < 0 || days > maxDays) {
                                        result = 2;
                                    }
                                }
                            }
                        }
                    } else {
                        result = 1;
                    }
                    if (result == 0) {
                        if (dateTimeEnd == null) {
                            dateTimeEnd = DateTimeHelper.bind(dateTimeBegin).hoursAdd(23).minutesAdd(59).secondsAdd(59).time();
                        }
                        ValidateContext.getLocalAttributes().put(paramName, PairObject.bind(dateTimeBegin, dateTimeEnd));
                    }
                } else {
                    result = 1;
                }
            }
        }
        return result;
    }

    @Override
    public ValidateResult validate(ValidateContext context) {
        Object paramValue = context.getParamValue();
        if (paramValue != null) {
            VDateTime vDateTime = (VDateTime) context.getAnnotation();
            int result = 0;
            String paramName = StringUtils.defaultIfBlank(vDateTime.value(), context.getParamName());
            if (context.getParamValue().getClass().isArray()) {
                Object[] values = (Object[]) paramValue;
                for (Object pValue : values) {
                    String pValueStr = BlurObject.bind(pValue).toStringValue();
                    result = validate(paramName, pValueStr, vDateTime.pattern(), vDateTime.separator(), vDateTime.maxDays(), vDateTime.single());
                    if (result > 0) {
                        break;
                    }
                }
            } else {
                String pValueStr = BlurObject.bind(paramValue).toStringValue();
                result = validate(paramName, pValueStr, vDateTime.pattern(), vDateTime.separator(), vDateTime.maxDays(), vDateTime.single());
            }
            if (result > 0) {
                ValidateResult.Builder builder = ValidateResult.builder(context).matched(true);
                if (StringUtils.isNotBlank(vDateTime.msg())) {
                    return builder.msg(vDateTime.msg()).build();
                }
                switch (result) {
                    case 2:
                    case 3:
                        builder.msg(I18N_MESSAGE_MAX_DAYS_KEY, I18N_MESSAGE_MAX_DAYS_DEFAULT_VALUE, builder.name());
                        break;
                    default:
                        builder.msg(I18N_MESSAGE_KEY, I18N_MESSAGE_DEFAULT_VALUE, builder.name());
                }
                return builder.build();
            }
        }
        return null;
    }
}
