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
package net.ymate.platform.webmvc.annotation;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/09 21:09
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableConventionMode {

    /**
     * @return 是否采用URL伪静态
     */
    boolean urlRewriteMode() default false;

    /**
     * @return 是否采用拦截器规则设置
     */
    boolean interceptorMode() default false;

    /**
     * @return 允许访问的视图文件路径集合
     */
    String[] viewAllowPaths() default {};

    /**
     * @return 禁止访问的视图文件路径集合
     */
    String[] viewNotAllowPaths() default {};
}
