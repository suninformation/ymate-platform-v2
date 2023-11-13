/*
 * Copyright 2007-2023 the original author or authors.
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
 * 拦截器全局规则设置
 *
 * @author 刘镇 (suninformation@163.com) on 2023/11/9 21:56
 * @since 2.1.3
 */
@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InterceptSettings {

    /**
     * @return 设置拦截器状态为禁止执行
     */
    Class<? extends IInterceptor>[] globals() default {};

    /**
     * @return 设置包拦截器
     */
    PackageSet[] packages() default {};

    /**
     * @return 设置类或方法拦截器
     */
    InterceptSet[] value() default {};

    @interface PackageSet {

        /**
         * @return 包名称集合
         */
        String[] names();

        /**
         * @return 拦截器集合
         */
        Item[] value();

        ContextParamSet[] params() default {};
    }

    @interface InterceptSet {

        /**
         * @return 目标类
         */
        Class<?>[] targets();

        /**
         * @return 方法名称集合
         */
        String[] names() default {};

        /**
         * @return 拦截器集合
         */
        Item[] value();

        ContextParamSet[] params() default {};
    }

    @interface Item {

        IInterceptor.SettingType type() default IInterceptor.SettingType.ADD_BEFORE;

        Class<? extends IInterceptor>[] value() default {};
    }

    @interface ContextParamSet {

        String key();

        String value();
    }
}
