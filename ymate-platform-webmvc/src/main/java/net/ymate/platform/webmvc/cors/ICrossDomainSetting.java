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
package net.ymate.platform.webmvc.cors;

import net.ymate.platform.core.beans.annotation.Ignored;

import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-17 12:25
 * @since 2.1.0
 */
@Ignored
public interface ICrossDomainSetting {

    /**
     * 针对OPTIONS请求是否自动回复, 默认: true
     *
     * @return 返回true表示自动回复
     */
    boolean isOptionsAutoReply();

    /**
     * 是否允许跨域请求带有验证信息
     *
     * @return 返回true表示允许
     */
    boolean isAllowedCredentials();

    /**
     * 跨域请求响应的最大缓存时间(秒)，默认: 1800(即30分种)
     *
     * @return 返回时间秒值
     */
    long getMaxAge();

    /**
     * 允许跨域的原始主机
     *
     * @return 返回主机集合
     */
    Set<String> getAllowedOrigins();

    /**
     * 允许跨域请求的方法
     *
     * @return 返回方法名称集合
     */
    Set<String> getAllowedMethods();

    /**
     * 允许跨域请求携带的请求头
     *
     * @return 返回请求头集合
     */
    Set<String> getAllowedHeaders();

    /**
     * Exposed请求头
     *
     * @return 返回Exposed请求头
     */
    Set<String> getExposedHeaders();
}
