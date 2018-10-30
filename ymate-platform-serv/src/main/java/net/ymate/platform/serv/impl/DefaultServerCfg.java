/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.serv.IServ;
import net.ymate.platform.serv.IServerCfg;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/6 下午6:44
 * @version 1.0
 */
public class DefaultServerCfg implements IServerCfg {

    public static Builder create() {
        return new Builder();
    }

    private String __serverName;

    private String __serverHost;

    private int __port;

    private String __charset;

    private int __bufferSize;

    private int __executorCount;

    private long __keepAliveTime;

    private int __threadMaxPoolSize;

    private int __threadQueueSize;

    private int __selectorCount;

    private Map<String, String> __params;

    public DefaultServerCfg(Map<String, String> serverCfg, String serverName) {
        __serverName = StringUtils.defaultIfBlank(serverName, IServ.Const.DEFAULT_NAME);
        __serverHost = StringUtils.defaultIfBlank(serverCfg.get(IServ.Const.HOST), IServ.Const.DEFAULT_HOST);
        __port = BlurObject.bind(StringUtils.defaultIfBlank(serverCfg.get(IServ.Const.PORT), IServ.Const.DEFAULT_PORT)).toIntValue();
        __charset = StringUtils.defaultIfBlank(serverCfg.get(IServ.Const.CHARSET), IServ.Const.DEFAULT_CHARSET);
        __bufferSize = BlurObject.bind(StringUtils.defaultIfBlank(serverCfg.get(IServ.Const.BUFFER_SIZE), IServ.Const.DEFAULT_BUFFER_SIZE)).toIntValue();
        __executorCount = BlurObject.bind(serverCfg.get(IServ.Const.EXECUTOR_COUNT)).toIntValue();
        if (__executorCount <= 0) {
            __executorCount = Runtime.getRuntime().availableProcessors();
        }
        //
        __keepAliveTime = BlurObject.bind(serverCfg.get(IServ.Const.KEEP_ALIVE_TIME)).toLongValue();
        //
        __threadMaxPoolSize = BlurObject.bind(serverCfg.get(IServ.Const.THREAD_MAX_POOL_SIZE)).toIntValue();
        if (__threadMaxPoolSize <= 0) {
            __threadMaxPoolSize = 200;
        }
        //
        __threadQueueSize = BlurObject.bind(serverCfg.get(IServ.Const.THREAD_QUEUE_SIZE)).toIntValue();
        if (__threadQueueSize <= 0) {
            __threadQueueSize = 1024;
        }
        __selectorCount = BlurObject.bind(StringUtils.defaultIfBlank(serverCfg.get(IServ.Const.SELECTOR_COUNT), "1")).toIntValue();
        //
        __params = RuntimeUtils.keyStartsWith(serverCfg, IServ.Const.PARAMS_PREFIX);
    }

    @Override
    public String getServerName() {
        return __serverName;
    }

    @Override
    public String getServerHost() {
        return __serverHost;
    }

    @Override
    public int getPort() {
        return __port;
    }

    @Override
    public String getCharset() {
        return __charset;
    }

    @Override
    public int getBufferSize() {
        return __bufferSize;
    }

    @Override
    public int getExecutorCount() {
        return __executorCount;
    }

    @Override
    public long getKeepAliveTime() {
        return __keepAliveTime;
    }

    @Override
    public int getThreadMaxPoolSize() {
        return __threadMaxPoolSize;
    }

    @Override
    public int getThreadQueueSize() {
        return __threadQueueSize;
    }

    @Override
    public int getSelectorCount() {
        return __selectorCount;
    }

    @Override
    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(__params);
    }

    public static class Builder {

        private String serverName;

        Map<String, String> params = new HashMap<String, String>();

        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder serverHost(String serverHost) {
            params.put(IServ.Const.HOST, serverHost);
            return this;
        }

        public Builder port(int port) {
            params.put(IServ.Const.PORT, port + "");
            return this;
        }

        public Builder charset(String charset) {
            params.put(IServ.Const.CHARSET, charset);
            return this;
        }

        public Builder bufferSize(int bufferSize) {
            params.put(IServ.Const.BUFFER_SIZE, bufferSize + "");
            return this;
        }

        public Builder executorCount(int executorCount) {
            params.put(IServ.Const.EXECUTOR_COUNT, executorCount + "");
            return this;
        }

        public Builder keepAliveTime(long keepAliveTime) {
            params.put(IServ.Const.KEEP_ALIVE_TIME, keepAliveTime + "");
            return this;
        }

        public Builder threadMaxPoolSize(int threadMaxPoolSize) {
            params.put(IServ.Const.EXECUTOR_COUNT, threadMaxPoolSize + "");
            return this;
        }

        public Builder threadQueueSize(int threadQueueSize) {
            params.put(IServ.Const.THREAD_QUEUE_SIZE, threadQueueSize + "");
            return this;
        }

        public Builder selectorCount(int selectorCount) {
            params.put(IServ.Const.SELECTOR_COUNT, selectorCount + "");
            return this;
        }

        public Builder params(String key, String value) {
            if (StringUtils.isNotBlank(key)) {
                params.put(IServ.Const.PARAMS_PREFIX + "." + key, value);
            }
            return this;
        }

        public Builder params(Map<String, String> params) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                params.put(IServ.Const.PARAMS_PREFIX + "." + param.getKey(), param.getValue());
            }
            return this;
        }

        public IServerCfg build() {
            return new DefaultServerCfg(params, serverName);
        }
    }
}
