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
package net.ymate.platform.persistence.jdbc.annotation;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 声明一个类为数据库方言接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2019-11-14 11:05
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Dialect {

    /**
     * @return 数据库方言名称
     */
    String value();

    /**
     * @return 数据库默认驱动类名称
     */
    String driverClass() default StringUtils.EMPTY;
}