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
package net.ymate.platform.serv.support;

import net.ymate.platform.serv.IServ;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 19:00
 * @version 1.0
 * @since 2.0.6
 */
public class ServServerConfigurable {

    public static ServServerConfigurable create(String name) {
        return new ServServerConfigurable(name);
    }

    private Map<String, String> __configs = new HashMap<String, String>();

    private String name;

    public ServServerConfigurable(String name) {
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

    public ServServerConfigurable serverHost(String serverHost) {
        __putItem(IServ.Const.HOST, serverHost);
        return this;
    }

    public ServServerConfigurable port(int port) {
        __putItem(IServ.Const.PORT, String.valueOf(port));
        return this;
    }

    public ServServerConfigurable charset(String charset) {
        __putItem(IServ.Const.CHARSET, charset);
        return this;
    }

    public ServServerConfigurable bufferSize(int bufferSize) {
        __putItem(IServ.Const.BUFFER_SIZE, String.valueOf(bufferSize));
        return this;
    }

    public ServServerConfigurable executorCount(int executorCount) {
        __putItem(IServ.Const.EXECUTOR_COUNT, String.valueOf(executorCount));
        return this;
    }

    public ServServerConfigurable keepAliveTime(long keepAliveTime) {
        __putItem(IServ.Const.KEEP_ALIVE_TIME, String.valueOf(keepAliveTime));
        return this;
    }

    public ServServerConfigurable threadMaxPoolSize(int threadMaxPoolSize) {
        __putItem(IServ.Const.EXECUTOR_COUNT, String.valueOf(threadMaxPoolSize));
        return this;
    }

    public ServServerConfigurable threadQueueSize(int threadQueueSize) {
        __putItem(IServ.Const.THREAD_QUEUE_SIZE, String.valueOf(threadQueueSize));
        return this;
    }

    public ServServerConfigurable selectorCount(int selectorCount) {
        __putItem(IServ.Const.SELECTOR_COUNT, String.valueOf(selectorCount));
        return this;
    }

    public ServServerConfigurable params(String key, String value) {
        if (StringUtils.isNotBlank(key)) {
            __putItem(IServ.Const.PARAMS_PREFIX + "." + key, value);
        }
        return this;
    }

    public ServServerConfigurable params(Map<String, String> params) {
        for (Map.Entry<String, String> param : params.entrySet()) {
            params.put(IServ.Const.PARAMS_PREFIX + "." + param.getKey(), param.getValue());
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> toMap() {
        return __configs;
    }
}
