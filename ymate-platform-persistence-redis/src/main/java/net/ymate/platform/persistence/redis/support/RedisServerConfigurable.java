/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.persistence.redis.support;

import net.ymate.platform.core.support.IPasswordProcessor;
import net.ymate.platform.persistence.redis.IRedisModuleCfg;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 16:28
 * @version 1.0
 * @since 2.0.6
 */
public class RedisServerConfigurable {

    public static RedisServerConfigurable create(String name) {
        return new RedisServerConfigurable(name);
    }

    private Map<String, String> __configs = new HashMap<String, String>();

    private String name;

    public RedisServerConfigurable(String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.name = name;
    }

    private void __putItem(String key, String value) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
            return;
        }
        __configs.put("server." + name + "." + key, value);
    }

    public RedisServerConfigurable host(String host) {
        __putItem(IRedisModuleCfg.HOST, host);
        return this;
    }

    public RedisServerConfigurable port(int port) {
        __putItem(IRedisModuleCfg.PORT, String.valueOf(port));
        return this;
    }

    public RedisServerConfigurable clientName(String clientName) {
        __putItem(IRedisModuleCfg.CLIENT_NAME, clientName);
        return this;
    }

    public RedisServerConfigurable timeout(int timeout) {
        __putItem(IRedisModuleCfg.TIMEOUT, String.valueOf(timeout));
        return this;
    }

    public RedisServerConfigurable socketTimeout(int socketTimeout) {
        __putItem(IRedisModuleCfg.SOCKET_TIMEOUT, String.valueOf(socketTimeout));
        return this;
    }

    public RedisServerConfigurable maxAttempts(int maxAttempts) {
        __putItem(IRedisModuleCfg.MAX_ATTEMPTS, String.valueOf(maxAttempts));
        return this;
    }

    public RedisServerConfigurable weight(int weight) {
        __putItem(IRedisModuleCfg.WEIGHT, String.valueOf(weight));
        return this;
    }

    public RedisServerConfigurable password(String password) {
        __putItem(IRedisModuleCfg.PASSWORD, password);
        return this;
    }

    public RedisServerConfigurable passwordEncrypted(boolean passwordEncrypted) {
        __putItem(IRedisModuleCfg.PASSWORD_ENCRYPTED, String.valueOf(passwordEncrypted));
        return this;
    }

    public RedisServerConfigurable passwordClass(Class<? extends IPasswordProcessor> passwordClass) {
        __putItem(IRedisModuleCfg.PASSWORD_CLASS, passwordClass.getName());
        return this;
    }

    public RedisServerConfigurable database(int database) {
        __putItem(IRedisModuleCfg.DATABASE, String.valueOf(database));
        return this;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> toMap() {
        return __configs;
    }
}
