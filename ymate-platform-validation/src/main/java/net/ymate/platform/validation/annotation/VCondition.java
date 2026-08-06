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
package net.ymate.platform.validation.annotation;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 条件验证注解，标注在验证注解上，声明该验证规则需满足的前置条件
 *
 * @author 刘镇 (suninformation@163.com) on 2026/6/22 00:40
 * @since 2.1.4
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VCondition {

    /**
     * 条件类型枚举
     */
    enum Type {
        /**
         * 指定字段存在(非空)时才验证
         */
        FIELD_NOT_EMPTY,

        /**
         * 指定字段为空时才验证
         */
        FIELD_EMPTY,

        /**
         * 指定字段值等于预期值时才验证
         */
        FIELD_EQUALS,

        /**
         * 指定字段值不等于预期值时才验证
         */
        FIELD_NOT_EQUALS,

        /**
         * 指定字段值大于预期值时才验证
         */
        FIELD_GT,

        /**
         * 指定字段值大于或等于预期值时才验证
         */
        FIELD_GT_EQ,

        /**
         * 指定字段值小于预期值时才验证
         */
        FIELD_LT,

        /**
         * 指定字段值小于或等于预期值时才验证
         */
        FIELD_LT_EQ
    }

    /**
     * @return 条件类型，默认为FIELD_NOT_EMPTY
     */
    Type type() default Type.FIELD_NOT_EMPTY;

    /**
     * @return 依赖的字段名称
     */
    String field() default StringUtils.EMPTY;

    /**
     * @return 依赖字段的预期值（用于FIELD_EQUALS/FIELD_NOT_EQUALS/FIELD_GT/FIELD_GT_EQ/FIELD_LT/FIELD_LT_EQ）
     */
    String expectedValue() default StringUtils.EMPTY;
}
