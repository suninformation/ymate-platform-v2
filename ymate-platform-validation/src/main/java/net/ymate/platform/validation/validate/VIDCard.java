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
package net.ymate.platform.validation.validate;

import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 身份证号码验证注解
 *
 * @author 刘镇 (suninformation@163.com) on 2020/02/15 19:19
 * @since 2.1.0
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface VIDCard {

    /**
     * @return 自定义验证消息
     */
    String msg() default StringUtils.EMPTY;
}
