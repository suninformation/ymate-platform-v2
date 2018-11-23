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
import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.IServ;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/6 下午6:50
 * @version 1.0
 */
public class DefaultClientCfg implements IClientCfg {

    public static Builder create() {
        return new Builder();
    }

    private String __clientName;

    private String __remoteHost;

    private int __port;

    private String __charset;

    private int __executorCount;

    private int __connectionTimeout;

    private int __bufferSize;

    private int __reconnectionInterval;

    private int __heartbeatInterval;

    private Map<String, String> __params;

    public DefaultClientCfg(Map<String, String> clientCfgs, String clientName) {
        __clientName = StringUtils.defaultIfBlank(clientName, IServ.Const.DEFAULT_NAME);
        __remoteHost = StringUtils.defaultIfBlank(clientCfgs.get(IServ.Const.HOST), IServ.Const.DEFAULT_HOST);
        __port = BlurObject.bind(StringUtils.defaultIfBlank(clientCfgs.get(IServ.Const.PORT), String.valueOf(IServ.Const.DEFAULT_PORT))).toIntValue();
        __charset = StringUtils.defaultIfBlank(clientCfgs.get(IServ.Const.CHARSET), IServ.Const.DEFAULT_CHARSET);
        __bufferSize = BlurObject.bind(StringUtils.defaultIfBlank(clientCfgs.get(IServ.Const.BUFFER_SIZE), String.valueOf(IServ.Const.DEFAULT_BUFFER_SIZE))).toIntValue();
        __executorCount = BlurObject.bind(clientCfgs.get(IServ.Const.EXECUTOR_COUNT)).toIntValue();
        if (__executorCount <= 0) {
            __executorCount = Runtime.getRuntime().availableProcessors();
        }
        __connectionTimeout = BlurObject.bind(StringUtils.defaultIfBlank(clientCfgs.get(IServ.Const.CONNECTION_TIMEOUT), String.valueOf(IServ.Const.DEFAULT_CONNECTION_TIMEOUT))).toIntValue();
        __reconnectionInterval = BlurObject.bind(StringUtils.defaultIfBlank(clientCfgs.get(IServ.Const.RECONNECTION_INTERVAL), String.valueOf(IServ.Const.DEFAULT_RECONNECTION_INTERVAL))).toIntValue();
        __heartbeatInterval = BlurObject.bind(StringUtils.defaultIfBlank(clientCfgs.get(IServ.Const.HEARTBEAT_INTERVAL), String.valueOf(IServ.Const.DEFAULT_HEARTBEAT_INTERVAL))).toIntValue();
        //
        __params = RuntimeUtils.keyStartsWith(clientCfgs, IServ.Const.PARAMS_PREFIX);
    }

    @Override
    public String getClientName() {
        return __clientName;
    }

    @Override
    public String getRemoteHost() {
        return __remoteHost;
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
    public int getConnectionTimeout() {
        return __connectionTimeout;
    }

    @Override
    public int getReconnectionInterval() {
        return __reconnectionInterval;
    }

    @Override
    public int getHeartbeatInterval() {
        return __heartbeatInterval;
    }

    @Override
    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(__params);
    }

    @Override
    public String getParam(String key) {
        return __params.get(key);
    }

    public static class Builder {

        private String clientName;

        Map<String, String> params = new HashMap<String, String>();

        public Builder clientName(String clientName) {
            this.clientName = clientName;
            return this;
        }

        public Builder remoteHost(String serverHost) {
            params.put(IServ.Const.HOST, serverHost);
            return this;
        }

        public Builder port(int port) {
            params.put(IServ.Const.PORT, String.valueOf(port));
            return this;
        }

        public Builder charset(String charset) {
            params.put(IServ.Const.CHARSET, charset);
            return this;
        }

        public Builder bufferSize(int bufferSize) {
            params.put(IServ.Const.BUFFER_SIZE, String.valueOf(bufferSize));
            return this;
        }

        public Builder executorCount(int executorCount) {
            params.put(IServ.Const.EXECUTOR_COUNT, String.valueOf(executorCount));
            return this;
        }

        public Builder connectionTimeout(int connectionTime) {
            params.put(IServ.Const.CONNECTION_TIMEOUT, String.valueOf(connectionTime));
            return this;
        }

        public Builder reconnectionInterval(int reconnectionInterval) {
            params.put(IServ.Const.RECONNECTION_INTERVAL, String.valueOf(reconnectionInterval));
            return this;
        }

        public Builder heartbeatInterval(int heartbeatInterval) {
            params.put(IServ.Const.HEARTBEAT_INTERVAL, String.valueOf(heartbeatInterval));
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

        public IClientCfg build() {
            return new DefaultClientCfg(params, clientName);
        }
    }
}
