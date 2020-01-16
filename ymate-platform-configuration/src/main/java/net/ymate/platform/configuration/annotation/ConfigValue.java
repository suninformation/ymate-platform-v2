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
package net.ymate.platform.configuration.annotation;

import net.ymate.platform.core.configuration.IConfigFileParser;
import net.ymate.platform.core.configuration.IConfiguration;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 配置注入 - 用于读取配置项参数值为类成员变量或方法参数赋值
 *
 * @author 刘镇 (suninformation@163.com) on 2019-04-25 13:38
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigValue {

    /**
     * @return 配置分类名称, 默认值为: default
     */
    String category() default IConfigFileParser.DEFAULT_CATEGORY_NAME;

    /**
     * @return 配置项名称, 若未提供则使用成员变量或方法参数名称
     */
    String value() default StringUtils.EMPTY;

    /**
     * @return 配置项默认值
     */
    String defaultValue() default StringUtils.EMPTY;

    /**
     * @return 配置类集合
     */
    Class<? extends IConfiguration>[] configs() default {};
}
