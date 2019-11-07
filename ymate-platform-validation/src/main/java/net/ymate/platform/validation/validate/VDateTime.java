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

import net.ymate.platform.commons.util.DateTimeUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 日期字符串参数验证注解
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/25 下午1:10
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VDateTime {

    /**
     * @return 自定参数名称
     */
    String value() default StringUtils.EMPTY;

    /**
     * @return 日期格式字符串
     */
    String pattern() default DateTimeUtils.YYYY_MM_DD_HH_MM_SS;

    /**
     * @return 仅接收单日期(即所选日期的00点00分00秒0毫秒到所选日期的23点59分59秒0毫秒)
     */
    boolean single() default true;

    /**
     * @return 时间段字符串之间的分割符号
     */
    String separator() default "/";

    /**
     * @return 时间段之间的天数最大差值，默认为0表示不限制
     */
    int maxDays() default 0;

    /**
     * @return 自定义验证消息
     */
    String msg() default StringUtils.EMPTY;
}
