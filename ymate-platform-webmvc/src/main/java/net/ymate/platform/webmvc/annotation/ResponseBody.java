/*
 * Copyright 2007-2018 the original author or authors.
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

import net.ymate.platform.webmvc.IResponseBodyProcessor;
import net.ymate.platform.webmvc.impl.DefaultResponseBodyProcessor;

import java.lang.annotation.*;

/**
 * 控制器方法返回结果对象自定义输出
 *
 * @author 刘镇 (suninformation@163.com) on 2018/1/10 上午12:38
 * @version 1.0
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {

    /**
     * @return 响应头是否携带Content-Type参数项
     */
    boolean contentType() default true;

    /**
     * @return 是否保留空值参数项
     */
    boolean keepNull() default true;

    /**
     * @return 参数键名是否使有引号标识符
     */
    boolean quoteField() default true;

    /**
     * @return 自定义对象输出处理器, 默认为JSON格式输出
     */
    Class<? extends IResponseBodyProcessor> value() default DefaultResponseBodyProcessor.class;
}
