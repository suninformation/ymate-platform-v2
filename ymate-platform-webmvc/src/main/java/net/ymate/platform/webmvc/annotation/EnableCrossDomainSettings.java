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

import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.validate.IHostNameChecker;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/09 21:09
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableCrossDomainSettings {

    /**
     * @return 针对OPTIONS请求是否自动回复
     */
    boolean optionsAutoReply() default false;

    /**
     * @return 是否允许跨域请求带有验证信息
     */
    boolean allowedCredentials() default false;

    /**
     * @return 跨域请求响应的最大缓存时间(秒)
     */
    long maxAge() default 0;

    /**
     * @return 允许跨域的原始主机
     */
    String[] allowedOrigins() default {};

    /**
     * @return 允许跨域的主机检测器
     */
    Class<? extends IHostNameChecker> allowedOriginsChecker() default IHostNameChecker.class;

    /**
     * @return 允许跨域请求的方法
     */
    Type.HttpMethod[] allowedMethods() default {};

    /**
     * @return 允许跨域请求携带的Header信息
     */
    String[] allowedHeaders() default {};

    /**
     * @return 允许跨域访问的Header信息
     */
    String[] exposedHeaders() default {};
}
