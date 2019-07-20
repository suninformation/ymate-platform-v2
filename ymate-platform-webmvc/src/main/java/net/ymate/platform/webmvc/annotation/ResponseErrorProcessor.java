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

import net.ymate.platform.webmvc.IResponseErrorProcessor;

import java.lang.annotation.*;

/**
 * 声明控制器方法的默认异常处理器
 *
 * @author 刘镇 (suninformation@163.com) on 2017/12/11 下午1:08
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseErrorProcessor {

    /**
     * @return 指定控制器异常自定义处理过程
     */
    Class<? extends IResponseErrorProcessor> value();
}
