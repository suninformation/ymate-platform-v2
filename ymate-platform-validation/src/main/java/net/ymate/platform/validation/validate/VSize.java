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
package net.ymate.platform.validation.validate;

import net.ymate.platform.validation.annotation.VCondition;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 集合元素数量验证注解
 *
 * @author 刘镇 (suninformation@163.com) on 2026/3/2 15:03
 * @since 2.1.4
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VSize {

    /**
     * @return 验证分组，默认为Default分组
     * @since 2.1.4
     */
    Class<?>[] groups() default {};

    /**
     * @return 条件验证配置
     * @since 2.1.4
     */
    VCondition condition() default @VCondition;

    /**
     * @return 设置最小元素数量，0为不限制
     */
    int min() default 0;

    /**
     * @return 设置最大元素数量，0为不限制
     */
    int max() default 0;

    /**
     * @return 设置固定元素数量值，0为不限制
     */
    int eq() default 0;

    /**
     * @return 自定义验证消息
     */
    String msg() default StringUtils.EMPTY;
}
