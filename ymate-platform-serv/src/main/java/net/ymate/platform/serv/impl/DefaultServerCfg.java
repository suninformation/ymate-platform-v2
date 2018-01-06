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
import net.ymate.platform.serv.IServModuleCfg;
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

    private final String __serverName;

    private final String __serverHost;

    private final int __port;

    private final String __charset;

    private final int __bufferSize;

    private int __executorCount;

    private final long __keepAliveTime;

    private int __threadMaxPoolSize;

    private int __threadQueueSize;

    private final Map<String, String> __params;

    public DefaultServerCfg(IServModuleCfg moduleCfg, String serverName) {
        __serverName = StringUtils.defaultIfBlank(serverName, "default");
        Map<String, String> _serverCfgs = moduleCfg.getServerCfg(__serverName);
        //
        __serverHost = StringUtils.defaultIfBlank(_serverCfgs.get("host"), "0.0.0.0");
        __port = BlurObject.bind(StringUtils.defaultIfBlank(_serverCfgs.get("port"), "8281")).toIntValue();
        __charset = StringUtils.defaultIfBlank(_serverCfgs.get("charset"), "UTF-8");
        __bufferSize = BlurObject.bind(StringUtils.defaultIfBlank(_serverCfgs.get("buffer_size"), "4096")).toIntValue();
        __executorCount = BlurObject.bind(_serverCfgs.get("executor_count")).toIntValue();
        if (__executorCount <= 0) {
            __executorCount = Runtime.getRuntime().availableProcessors();
        }
        //
        __keepAliveTime = BlurObject.bind(_serverCfgs.get("keep_alive_time")).toLongValue();
        //
        __threadMaxPoolSize = BlurObject.bind(_serverCfgs.get("thread_max_pool_size")).toIntValue();
        if (__threadMaxPoolSize <= 0) {
            __threadMaxPoolSize = 200;
        }
        //
        __threadQueueSize = BlurObject.bind(_serverCfgs.get("thread_queue_size")).toIntValue();
        if (__threadQueueSize <= 0) {
            __threadQueueSize = 1024;
        }
        __params = new HashMap<String, String>();
        for (Map.Entry<String, String> _entry : _serverCfgs.entrySet()) {
            if (_entry.getKey().startsWith("params.")) {
                String _paramKey = StringUtils.substring(_entry.getKey(), ("params.").length());
                __params.put(_paramKey, _entry.getValue());
            }
        }
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
    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(__params);
    }
}
