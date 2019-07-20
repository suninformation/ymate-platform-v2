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
package net.ymate.platform.plugin;

import net.ymate.platform.core.event.AbstractEventContext;
import net.ymate.platform.core.event.IEvent;

/**
 * 插件生命周期事件
 *
 * @author 刘镇 (suninformation@163.com) on 15/6/15 上午3:36
 */
public class PluginEvent extends AbstractEventContext<IPlugin, PluginEvent.EVENT> implements IEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 插件事件枚举
     */
    public enum EVENT {

        /**
         * 插件初始化事件
         */
        PLUGIN_INITIALIZED,

        /**
         * 插件启动事件
         */
        PLUGIN_STARTED,

        /**
         * 插件停止事件
         */
        PLUGIN_SHUTDOWN,

        /**
         * 插件销毁事件
         */
        PLUGIN_DESTROYED
    }

    public PluginEvent(IPlugin owner, EVENT eventName) {
        super(owner, PluginEvent.class, eventName);
    }
}
