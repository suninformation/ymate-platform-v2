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
package net.ymate.platform.webmvc.cors.annotation;

import net.ymate.platform.core.beans.annotation.InterceptAnnotation;
import net.ymate.platform.core.beans.intercept.IInterceptor;

import java.lang.annotation.*;

/**
 * 声明开启跨域处理
 *
 * @author 刘镇 (suninformation@163.com) on 2019-08-17 14:19
 * @since 2.1.0
 * @deprecated 从 `2.1.3` 开始不再使用拦截器处理跨域请求
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InterceptAnnotation(IInterceptor.Direction.BEFORE)
@Deprecated
public @interface CrossDomain {
}
