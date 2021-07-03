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

import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.validate.IHostNameChecker;

import java.lang.annotation.*;

/**
 * 声明自定义跨域配置
 *
 * @author 刘镇 (suninformation@163.com) on 2019-08-16 14:37
 * @since 2.1.0
 */
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CrossDomainSetting {

    boolean optionsAutoReply() default false;

    boolean allowedCredentials() default false;

    long maxAge() default 0;

    String[] allowedOrigins() default {};

    Class<? extends IHostNameChecker> allowedOriginsChecker() default IHostNameChecker.class;

    Type.HttpMethod[] allowedMethods() default {};

    String[] allowedHeaders() default {};

    String[] exposedHeaders() default {};
}
