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

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 数值类型参数验证注解
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/25 下午1:09
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VNumeric {

    /**
     * @return 仅检查值是否为数字（当取值为true时生效，同时其它参数将失效）
     * @since 2.1.3
     */
    boolean digits() default false;

    /**
     * @return 设置最小值，0为不限制
     */
    double min() default 0;

    /**
     * @return 设置最大值，0为不限制
     */
    double max() default 0;

    /**
     * @return 设置值相等，0为不限制
     */
    double eq() default 0;

    /**
     * @return 设置小数位数，0不不限制
     */
    int decimals() default 0;

    /**
     * @return 自定义验证消息
     */
    String msg() default StringUtils.EMPTY;
}
