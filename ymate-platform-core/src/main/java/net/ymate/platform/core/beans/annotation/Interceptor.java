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

import java.lang.annotation.*;

/**
 * 指定一个类为自定义拦截器
 *
 * @author 刘镇 (suninformation@163.com) on 2018/7/30 下午9:36
 * @since 2.0.6
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Interceptor {

    /**
     * @return 是否为单例，默认为true
     */
    boolean singleton() default true;

    /**
     * @return 自定义注解类型(用于通过注解替代原始拦截器配置)
     * @since 2.1.0
     */
    Class<? extends Annotation> value() default Annotation.class;
}
