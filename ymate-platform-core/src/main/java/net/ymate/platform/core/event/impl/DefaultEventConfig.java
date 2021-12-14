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
package net.ymate.platform.core.event.impl;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.event.IEventConfig;
import net.ymate.platform.core.event.IEventProvider;
import net.ymate.platform.core.module.IModuleConfigurer;
import org.apache.commons.lang3.StringUtils;

/**
 * 默认事件配置
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/17 下午4:06
 */
public final class DefaultEventConfig implements IEventConfig {

    @SuppressWarnings("rawtypes")
    private IEventProvider eventProvider;

    private Events.MODE defaultMode;

    private int threadPoolSize;

    private int threadMaxPoolSize;

    private int threadQueueSize;

    private boolean initialized;

    public static DefaultEventConfig defaultConfig() {
        return new DefaultEventConfig();
    }

    public static DefaultEventConfig create(IModuleConfigurer moduleConfigurer) {
        return new DefaultEventConfig(moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultEventConfig() {
    }

    private DefaultEventConfig(IModuleConfigurer moduleConfigurer) {
        if (moduleConfigurer != null) {
            IConfigReader configReader = moduleConfigurer.getConfigReader();
            if (configReader != null && !configReader.toMap().isEmpty()) {
                this.eventProvider = configReader.getClassImpl(PROVIDER_CLASS, IEventProvider.class);
                String defaultModeStr = configReader.getString(DEFAULT_MODE);
                if (StringUtils.isNotBlank(defaultModeStr)) {
                    this.defaultMode = Events.MODE.valueOf(defaultModeStr.toUpperCase());
                }
                this.threadPoolSize = configReader.getInt(THREAD_POOL_SIZE);
                this.threadMaxPoolSize = configReader.getInt(THREAD_MAX_POOL_SIZE);
                this.threadQueueSize = configReader.getInt(THREAD_QUEUE_SIZE);
            }
        }
    }

    @Override
    public void initialize() {
        if (!initialized) {
            if (this.eventProvider == null) {
                this.eventProvider = ClassUtils.loadClass(IEventProvider.class, DefaultEventProvider.class);
            }
            this.defaultMode = defaultMode != null ? defaultMode : Events.MODE.ASYNC;
            this.threadPoolSize = threadPoolSize > 0 ? threadPoolSize : Runtime.getRuntime().availableProcessors();
            this.threadMaxPoolSize = threadMaxPoolSize > 0 ? threadMaxPoolSize : 200;
            this.threadQueueSize = threadQueueSize > 0 ? threadQueueSize : 1024;
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public IEventProvider getEventProvider() {
        return eventProvider;
    }

    @SuppressWarnings("rawtypes")
    public void setEventProvider(IEventProvider eventProvider) {
        if (!initialized) {
            this.eventProvider = eventProvider;
        }
    }

    @Override
    public Events.MODE getDefaultMode() {
        return defaultMode;
    }

    public void setDefaultMode(Events.MODE defaultMode) {
        if (!initialized) {
            this.defaultMode = defaultMode;
        }
    }

    @Override
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        if (!initialized) {
            this.threadPoolSize = threadPoolSize;
        }
    }

    @Override
    public int getThreadMaxPoolSize() {
        return threadMaxPoolSize;
    }

    public void setThreadMaxPoolSize(int threadMaxPoolSize) {
        if (!initialized) {
            this.threadMaxPoolSize = threadMaxPoolSize;
        }
    }

    @Override
    public int getThreadQueueSize() {
        return threadQueueSize;
    }

    public void setThreadQueueSize(int threadQueueSize) {
        if (!initialized) {
            this.threadQueueSize = threadQueueSize;
        }
    }

    public static final class Builder {

        private final DefaultEventConfig config = new DefaultEventConfig();

        private Builder() {
        }

        @SuppressWarnings("rawtypes")
        public Builder eventProvider(IEventProvider eventProvider) {
            config.setEventProvider(eventProvider);
            return this;
        }

        public Builder defaultMode(Events.MODE defaultMode) {
            config.setDefaultMode(defaultMode);
            return this;
        }

        public Builder threadPoolSize(int threadPoolSize) {
            config.setThreadPoolSize(threadPoolSize);
            return this;
        }

        public Builder threadMaxPoolSize(int threadMaxPoolSize) {
            config.setThreadMaxPoolSize(threadMaxPoolSize);
            return this;
        }

        public Builder threadQueueSize(int threadQueueSize) {
            config.setThreadQueueSize(threadQueueSize);
            return this;
        }

        public DefaultEventConfig build() {
            return config;
        }
    }
}
