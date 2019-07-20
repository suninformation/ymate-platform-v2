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
package net.ymate.platform.core.event;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationConfigurer;
import net.ymate.platform.core.event.impl.DefaultEventConfig;

/**
 * 事件管理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/16 上午2:15
 */
public final class Events {

    public static final String MODULE_NAME = "event";

    /**
     * 事件触发模式枚举
     */
    public enum MODE {
        /**
         * NORMAL - 同步执行
         */
        NORMAL,

        /**
         * ASYNC  - 异步执行
         */
        ASYNC
    }

    private final IEventConfig eventConfig;

    public Events(IApplication owner) {
        IApplicationConfigurer configurer = owner.getConfigurer();
        this.eventConfig = configurer != null ? DefaultEventConfig.create(configurer.getModuleConfigurer(Events.MODULE_NAME)) : DefaultEventConfig.defaultConfig();
    }

    public void initialize() {
        if (!eventConfig.isInitialized()) {
            eventConfig.initialize();
        }
        if (!eventConfig.getEventProvider().isInitialized()) {
            this.eventConfig.getEventProvider().initialize(eventConfig);
        }
    }

    public boolean isInitialized() {
        return eventConfig.isInitialized();
    }

    public void destroy() {
        this.eventConfig.getEventProvider().destroy();
    }

    @SuppressWarnings("unchecked")
    public Events registerEvent(Class<? extends IEvent> eventClass) {
        this.eventConfig.getEventProvider().registerEvent(eventClass);
        return this;
    }

    @SuppressWarnings("unchecked")
    public boolean unregisterEvent(Class<? extends IEvent> eventClass) {
        return this.eventConfig.getEventProvider().unregisterEvent(eventClass);
    }

    @SuppressWarnings("unchecked")
    public <CONTEXT extends AbstractEventContext> Events registerListener(Class<? extends IEvent> eventClass, IEventListener<CONTEXT> eventListener) {
        this.eventConfig.getEventProvider().registerListener(eventClass, eventListener);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <CONTEXT extends AbstractEventContext> Events registerListener(MODE mode, Class<? extends IEvent> eventClass, IEventListener<CONTEXT> eventListener) {
        this.eventConfig.getEventProvider().registerListener(mode, eventClass, eventListener);
        return this;
    }

    @SuppressWarnings("unchecked")
    public boolean unregisterListener(Class<? extends IEvent> eventClass, Class<? extends IEventListener> listenerClass) {
        return this.eventConfig.getEventProvider().unregisterListener(eventClass, listenerClass);
    }

    @SuppressWarnings("unchecked")
    public <CONTEXT extends AbstractEventContext> Events fireEvent(CONTEXT context) {
        this.eventConfig.getEventProvider().fireEvent(context);
        return this;
    }
}
