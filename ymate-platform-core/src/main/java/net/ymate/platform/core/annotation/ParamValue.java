/*
 * Copyright 2007-2022 the original author or authors.
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
package net.ymate.platform.core.annotation;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 自定义扩展参数值注入
 *
 * @author 刘镇 (suninformation@163.com) on 2022/7/5 09:24
 * @since 2.1.2
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ParamValue {

    /**
     * @return 自定义扩展参数名称, 若未提供则使用成员变量或方法参数名称
     */
    String value() default StringUtils.EMPTY;

    /**
     * @return 自定义扩展参数默认值
     */
    String defaultValue() default StringUtils.EMPTY;

    /**
     * @return 是否替换字符串中的环境变量
     */
    boolean replaceEnvVariable() default false;
}
