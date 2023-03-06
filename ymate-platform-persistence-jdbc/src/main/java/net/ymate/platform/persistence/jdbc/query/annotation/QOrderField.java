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
package net.ymate.platform.persistence.jdbc.query.annotation;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 定义一个参与排序的字段
 *
 * @author 刘镇 (suninformation@163.com) on 2020/04/18 11:39
 * @since 2.1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface QOrderField {

    /**
     * @return 自定义前缀
     */
    String prefix() default StringUtils.EMPTY;

    /**
     * @return 字段名称
     */
    String value();

    /**
     * @return 排序类型，默认值为：正序
     */
    Type type() default Type.ASC;

    /**
     * @return 是否包装标识符
     * @since 2.1.2
     */
    boolean wrapIdentifier() default true;

    /**
     * 排序类型枚举
     */
    enum Type {

        /**
         * 倒序
         */
        DESC,

        /**
         * 正序
         */
        ASC
    }
}
