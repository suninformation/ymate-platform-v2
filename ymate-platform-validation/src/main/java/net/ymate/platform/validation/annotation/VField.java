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
package net.ymate.platform.validation.annotation;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 指定待验证的成员或方法参数名称的注解
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/26 上午10:36
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VField {

    /**
     * @return 绑定的参数名称前缀
     * @since 2.1.3
     */
    String prefix() default StringUtils.EMPTY;

    /**
     * @return 参数名称(用于与集成端业务参数一致)
     * @since 2.1.3
     */
    String value() default StringUtils.EMPTY;

    /**
     * @return 自定义参数名称
     */
    String name() default StringUtils.EMPTY;

    /**
     * @return 自定义参数I18n标签名称
     */
    String label() default StringUtils.EMPTY;
}
