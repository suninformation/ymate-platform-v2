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
package net.ymate.platform.webmvc.annotation;

import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * 声明一个请求/回应Header键值对
 *
 * @author 刘镇 (suninformation@163.com) on 15/10/29 下午8:19
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ResponseHeaders.class)
@Documented
public @interface ResponseHeader {

    /**
     * @return Header键名称
     */
    String name();

    /**
     * @return Header值
     */
    String value() default StringUtils.EMPTY;

    /**
     * @return Header类型
     */
    Type.HeaderType type() default Type.HeaderType.STRING;
}
