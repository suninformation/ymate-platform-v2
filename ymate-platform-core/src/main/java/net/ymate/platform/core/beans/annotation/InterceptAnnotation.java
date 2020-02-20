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
package net.ymate.platform.core.beans.annotation;

import net.ymate.platform.core.beans.intercept.IInterceptor;

import java.lang.annotation.*;

/**
 * 声明一个注解类用于支持通过其替代原始拦截器配置特性(执行方式与@Around注解相同并优先于其它拦截器注解)
 *
 * @author 刘镇 (suninformation@163.com) on 2019-07-09 15:23
 * @since 2.1.0
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InterceptAnnotation {

    /**
     * @return 设置拦截器方向, 默认为空表示全部方向
     */
    IInterceptor.Direction[] value() default {};
}
