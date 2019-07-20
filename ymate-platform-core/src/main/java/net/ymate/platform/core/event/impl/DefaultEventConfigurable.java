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

import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.event.IEventConfig;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurable;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-13 02:14
 * @since 2.1.0
 */
public class DefaultEventConfigurable extends DefaultModuleConfigurable {

    public static Builder builder() {
        return new Builder();
    }

    private DefaultEventConfigurable() {
        super(Events.MODULE_NAME);
    }

    public static final class Builder {

        private final DefaultEventConfigurable configurable = new DefaultEventConfigurable();

        private Builder() {
        }

        public Builder eventProvider(String eventProvider) {
            configurable.addConfig(IEventConfig.PROVIDER_CLASS, eventProvider);
            return this;
        }

        public Builder defaultMode(String defaultMode) {
            configurable.addConfig(IEventConfig.DEFAULT_MODE, defaultMode);
            return this;
        }

        public Builder threadPoolSize(int threadPoolSize) {
            configurable.addConfig(IEventConfig.THREAD_POOL_SIZE, String.valueOf(threadPoolSize));
            return this;
        }

        public Builder threadMaxPoolSize(int threadMaxPoolSize) {
            configurable.addConfig(IEventConfig.THREAD_MAX_POOL_SIZE, String.valueOf(threadMaxPoolSize));
            return this;
        }

        public Builder threadQueueSize(int threadQueueSize) {
            configurable.addConfig(IEventConfig.THREAD_QUEUE_SIZE, String.valueOf(threadQueueSize));
            return this;
        }

        public IModuleConfigurer build() {
            return configurable.toModuleConfigurer();
        }
    }
}
