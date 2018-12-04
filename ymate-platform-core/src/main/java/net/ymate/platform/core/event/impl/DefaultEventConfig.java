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
package net.ymate.platform.core.event.impl;

import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.event.IEventConfig;
import net.ymate.platform.core.event.IEventProvider;
import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 默认事件配置
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/17 下午4:06
 * @version 1.0
 */
public final class DefaultEventConfig implements IEventConfig {

    private IEventProvider __eventProvider;

    private Events.MODE __defaultMode;

    private int __threadPoolSize;

    private int __threadMaxPoolSize;

    private int __threadQueueSize;

    public DefaultEventConfig() {
        this(null);
    }

    public DefaultEventConfig(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            __eventProvider = new DefaultEventProvider();
            __defaultMode = Events.MODE.ASYNC;
            __threadPoolSize = Runtime.getRuntime().availableProcessors();
            __threadMaxPoolSize = 200;
            __threadQueueSize = 1024;
        } else {
            __eventProvider = ClassUtils.impl(params.get(PROVIDER_CLASS), IEventProvider.class, this.getClass());
            if (__eventProvider == null) {
                __eventProvider = new DefaultEventProvider();
            }
            //
            __defaultMode = Events.MODE.valueOf(StringUtils.defaultIfBlank(params.get(DEFAULT_MODE), Events.MODE.ASYNC.name()).toUpperCase());
            //
            __threadPoolSize = BlurObject.bind(params.get(THREAD_POOL_SIZE)).toIntValue();
            if (__threadPoolSize <= 0) {
                __threadPoolSize = Runtime.getRuntime().availableProcessors();
            }
            //
            __threadMaxPoolSize = BlurObject.bind(params.get(THREAD_MAX_POOL_SIZE)).toIntValue();
            if (__threadMaxPoolSize <= 0) {
                __threadMaxPoolSize = 200;
            }
            //
            __threadQueueSize = BlurObject.bind(params.get(THREAD_QUEUE_SIZE)).toIntValue();
            if (__threadQueueSize <= 0) {
                __threadQueueSize = 1024;
            }
        }
    }

    @Override
    public IEventProvider getEventProvider() {
        return __eventProvider;
    }

    @Override
    public Events.MODE getDefaultMode() {
        return __defaultMode;
    }

    @Override
    public int getThreadPoolSize() {
        return __threadPoolSize;
    }

    @Override
    public int getThreadMaxPoolSize() {
        return __threadMaxPoolSize;
    }

    @Override
    public int getThreadQueueSize() {
        return __threadQueueSize;
    }
}
