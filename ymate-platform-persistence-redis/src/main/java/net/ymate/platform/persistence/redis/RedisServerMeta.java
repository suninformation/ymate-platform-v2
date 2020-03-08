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

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * 服务配置描述
 *
 * @author 刘镇 (suninformation@163.com) on 2019-05-22 02:19
 */
public class RedisServerMeta implements Serializable {

    private static final long serialVersionUID = 1L;

    public static Builder builder(String name) {
        return new Builder(name);
    }

    private String name;

    private String host;

    private int port;

    private int timeout;

    private int socketTimeout;

    private int maxAttempts;

    private int weight;

    private int database;

    private String clientName;

    private String password;

    public RedisServerMeta(String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException("name");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RedisServerMeta that = (RedisServerMeta) o;
        return port == that.port && host.equals(that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return String.format("RedisServer [name='%s', host='%s', port=%d, weight=%d, database=%d, clientName='%s']", name, host, port, weight, database, clientName);
    }

    public static class Builder {

        private final RedisServerMeta serverMeta;

        public Builder(String name) {
            serverMeta = new RedisServerMeta(name);
        }

        public Builder host(String host) {
            serverMeta.setHost(host);
            return this;
        }

        public Builder port(int port) {
            serverMeta.setPort(port);
            return this;
        }

        public Builder timeout(int timeout) {
            serverMeta.setTimeout(timeout);
            return this;
        }

        public Builder socketTimeout(int socketTimeout) {
            serverMeta.setSocketTimeout(socketTimeout);
            return this;
        }

        public Builder maxAttempts(int maxAttempts) {
            serverMeta.setMaxAttempts(maxAttempts);
            return this;
        }

        public Builder weight(int weight) {
            serverMeta.setWeight(weight);
            return this;
        }

        public Builder database(int database) {
            serverMeta.setDatabase(database);
            return this;
        }

        public Builder clientName(String clientName) {
            serverMeta.setClientName(clientName);
            return this;
        }

        public Builder password(String password) {
            serverMeta.setPassword(password);
            return this;
        }

        public RedisServerMeta build() {
            return serverMeta;
        }
    }
}
