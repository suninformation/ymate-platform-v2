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
package net.ymate.platform.serv.impl;

import net.ymate.platform.serv.IClientCfg;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/6 下午6:50
 */
public final class DefaultClientCfg implements IClientCfg {

    public static Builder builder() {
        return new Builder();
    }

    private String clientName;

    private String remoteHost;

    private int port;

    private String charset;

    private int executorCount;

    private int connectionTimeout;

    private int bufferSize;

    private int reconnectionInterval;

    private int heartbeatInterval;

    private final Map<String, String> params = new HashMap<>();

    private DefaultClientCfg() {
    }

    @Override
    public String getClientName() {
        return clientName;
    }

    @Override
    public String getRemoteHost() {
        return remoteHost;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getCharset() {
        return charset;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    @Override
    public int getExecutorCount() {
        return executorCount;
    }

    @Override
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public int getReconnectionInterval() {
        return reconnectionInterval;
    }

    @Override
    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    @Override
    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(params);
    }

    @Override
    public String getParam(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return params.get(key);
    }

    @Override
    public String getParam(String key, String defaultValue) {
        return StringUtils.defaultIfBlank(getParam(key), defaultValue);
    }

    public static final class Builder {

        private final DefaultClientCfg clientCfg = new DefaultClientCfg();

        private Builder() {
        }

        public Builder clientName(String clientName) {
            clientCfg.clientName = clientName;
            return this;
        }

        public Builder remoteHost(String remoteHost) {
            clientCfg.remoteHost = remoteHost;
            return this;
        }

        public Builder port(int port) {
            clientCfg.port = port;
            return this;
        }

        public Builder charset(String charset) {
            clientCfg.charset = charset;
            return this;
        }

        public Builder bufferSize(int bufferSize) {
            clientCfg.bufferSize = bufferSize;
            return this;
        }

        public Builder executorCount(int executorCount) {
            clientCfg.executorCount = executorCount;
            return this;
        }

        public Builder connectionTimeout(int connectionTimeout) {
            clientCfg.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder reconnectionInterval(int reconnectionInterval) {
            clientCfg.reconnectionInterval = reconnectionInterval;
            return this;
        }

        public Builder heartbeatInterval(int heartbeatInterval) {
            clientCfg.heartbeatInterval = heartbeatInterval;
            return this;
        }

        public Builder params(String key, String value) {
            clientCfg.params.put(key, value);
            return this;
        }

        public Builder params(Map<String, String> params) {
            clientCfg.params.putAll(params);
            return this;
        }

        public IClientCfg build() {
            return clientCfg;
        }
    }
}
