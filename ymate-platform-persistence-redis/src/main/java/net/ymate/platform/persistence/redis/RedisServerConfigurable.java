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
package net.ymate.platform.persistence.redis;

import net.ymate.platform.commons.IPasswordProcessor;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 16:28
 * @since 2.0.6
 */
public final class RedisServerConfigurable {

    public static Builder builder(String name) {
        return new Builder(name);
    }

    private final Map<String, String> configs = new HashMap<>();

    private final String name;

    public RedisServerConfigurable(String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.name = name;
    }

    public void addConfig(String confKey, String confValue) {
        if (StringUtils.isNoneBlank(confKey) && StringUtils.isNotBlank(confValue)) {
            configs.put(String.format("server.%s.%s", name, confKey), confValue);
        }
    }

    public String getName() {
        return name;
    }

    public Map<String, String> toMap() {
        return configs;
    }

    public static final class Builder {

        private final RedisServerConfigurable configurable;

        private Builder(String name) {
            configurable = new RedisServerConfigurable(name);
        }

        public Builder host(String host) {
            configurable.addConfig(IRedisConfig.HOST, host);
            return this;
        }

        public Builder port(int port) {
            configurable.addConfig(IRedisConfig.PORT, String.valueOf(port));
            return this;
        }

        public Builder clientName(String clientName) {
            configurable.addConfig(IRedisConfig.CLIENT_NAME, clientName);
            return this;
        }

        public Builder timeout(int timeout) {
            configurable.addConfig(IRedisConfig.TIMEOUT, String.valueOf(timeout));
            return this;
        }

        public Builder socketTimeout(int socketTimeout) {
            configurable.addConfig(IRedisConfig.SOCKET_TIMEOUT, String.valueOf(socketTimeout));
            return this;
        }

        public Builder maxAttempts(int maxAttempts) {
            configurable.addConfig(IRedisConfig.MAX_ATTEMPTS, String.valueOf(maxAttempts));
            return this;
        }

        public Builder weight(int weight) {
            configurable.addConfig(IRedisConfig.WEIGHT, String.valueOf(weight));
            return this;
        }

        public Builder password(String password) {
            configurable.addConfig(IRedisConfig.PASSWORD, password);
            return this;
        }

        public Builder passwordEncrypted(boolean passwordEncrypted) {
            configurable.addConfig(IRedisConfig.PASSWORD_ENCRYPTED, String.valueOf(passwordEncrypted));
            return this;
        }

        public Builder passwordClass(Class<? extends IPasswordProcessor> passwordClass) {
            configurable.addConfig(IRedisConfig.PASSWORD_CLASS, passwordClass.getName());
            return this;
        }

        public Builder database(int database) {
            configurable.addConfig(IRedisConfig.DATABASE, String.valueOf(database));
            return this;
        }

        public RedisServerConfigurable build() {
            return configurable;
        }
    }
}
