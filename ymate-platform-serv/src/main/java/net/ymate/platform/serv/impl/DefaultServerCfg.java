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

import net.ymate.platform.serv.IServerCfg;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/6 下午6:44
 */
public final class DefaultServerCfg implements IServerCfg {

    public static Builder builder() {
        return new Builder();
    }

    private String serverName;

    private String serverHost;

    private int port;

    private String charset;

    private int bufferSize;

    private int executorCount;

    private long keepAliveTime;

    private int threadMaxPoolSize;

    private int threadQueueSize;

    private int selectorCount;

    private final Map<String, String> params = new HashMap<>();

    private DefaultServerCfg() {
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public String getServerHost() {
        return serverHost;
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
    public long getKeepAliveTime() {
        return keepAliveTime;
    }

    @Override
    public int getThreadMaxPoolSize() {
        return threadMaxPoolSize;
    }

    @Override
    public int getThreadQueueSize() {
        return threadQueueSize;
    }

    @Override
    public int getSelectorCount() {
        return selectorCount;
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

        private final DefaultServerCfg serverCfg = new DefaultServerCfg();

        private Builder() {
        }

        public Builder serverName(String serverName) {
            serverCfg.serverName = serverName;
            return this;
        }

        public Builder serverHost(String serverHost) {
            serverCfg.serverHost = serverHost;
            return this;
        }

        public Builder port(int port) {
            serverCfg.port = port;
            return this;
        }

        public Builder charset(String charset) {
            serverCfg.charset = charset;
            return this;
        }

        public Builder bufferSize(int bufferSize) {
            serverCfg.bufferSize = bufferSize;
            return this;
        }

        public Builder executorCount(int executorCount) {
            serverCfg.executorCount = executorCount;
            return this;
        }

        public Builder keepAliveTime(long keepAliveTime) {
            serverCfg.keepAliveTime = keepAliveTime;
            return this;
        }

        public Builder threadMaxPoolSize(int threadMaxPoolSize) {
            serverCfg.threadMaxPoolSize = threadMaxPoolSize;
            return this;
        }

        public Builder threadQueueSize(int threadQueueSize) {
            serverCfg.threadQueueSize = threadQueueSize;
            return this;
        }

        public Builder selectorCount(int selectorCount) {
            serverCfg.selectorCount = selectorCount;
            return this;
        }

        public Builder params(String key, String value) {
            serverCfg.params.put(key, value);
            return this;
        }

        public Builder params(Map<String, String> params) {
            serverCfg.params.putAll(params);
            return this;
        }

        public IServerCfg build() {
            return serverCfg;
        }
    }
}
