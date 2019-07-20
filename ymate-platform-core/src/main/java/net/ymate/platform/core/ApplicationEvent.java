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
package net.ymate.platform.core;

import net.ymate.platform.core.event.AbstractEventContext;
import net.ymate.platform.core.event.IEvent;

/**
 * 应用容器事件对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/17 下午6:35
 */
public final class ApplicationEvent extends AbstractEventContext<IApplication, ApplicationEvent.EVENT> implements IEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 应用容器事件枚举
     */
    public enum EVENT {

        /**
         * 应用容器启动事件
         */
        APPLICATION_STARTUP,

        /**
         * 应用容器初始化事件
         */
        APPLICATION_INITIALIZED,

        /**
         * 应用容器销毁事件
         */
        APPLICATION_DESTROYED
    }

    public ApplicationEvent(IApplication owner, EVENT eventName) {
        super(owner, ApplicationEvent.class, eventName);
    }
}
