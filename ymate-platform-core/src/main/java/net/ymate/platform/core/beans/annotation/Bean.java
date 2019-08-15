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

import net.ymate.platform.core.beans.IBeanHandler;

import java.lang.annotation.*;

/**
 * 声明一个类由IoC容器管理的注解
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-15 下午4:18:18
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Bean {

    /**
     * @return 是否为单例，默认为true
     */
    boolean singleton() default true;

    /**
     * @return 自定义对象处理器 (将取代原来的处理器)
     */
    Class<? extends IBeanHandler> handler() default IBeanHandler.class;
}
