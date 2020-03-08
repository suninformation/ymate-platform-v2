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
package net.ymate.platform.persistence.redis.annotation;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/11 00:06
 * @since 2.1.0
 */
public @interface RedisServer {

    /**
     * @return 服务器名称
     */
    String name();

    /**
     * @return 主机地址
     */
    String host() default StringUtils.EMPTY;

    /**
     * @return 主机端口
     */
    int port() default 0;

    /**
     * @return 连接超时时间(毫秒)
     */
    int timeout() default 0;

    /**
     * @return 超时时间(毫秒)
     */
    int socketTimeout() default 0;

    /**
     * @return 最大尝试次数
     */
    int maxAttempts() default 0;

    /**
     * @return 连接权重
     */
    int weight() default 0;

    /**
     * @return 数据库索引
     */
    int database() default 0;

    /**
     * @return 客户端名称
     */
    String clientName() default StringUtils.EMPTY;

    /**
     * @return 身份认证密码
     */
    String password() default StringUtils.EMPTY;
}
