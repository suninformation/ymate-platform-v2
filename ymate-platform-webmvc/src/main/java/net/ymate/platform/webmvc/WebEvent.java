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
package net.ymate.platform.webmvc;

import net.ymate.platform.core.event.AbstractEventContext;
import net.ymate.platform.core.event.IEvent;

/**
 * WEB事件对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/19 下午11:45
 */
public class WebEvent extends AbstractEventContext<IWebMvc, WebEvent.EVENT> implements IEvent {

    private static final long serialVersionUID = 1L;

    /**
     * WEB事件枚举
     */
    public enum EVENT {

        /**
         * 容器初始化事件
         */
        SERVLET_CONTEXT_INITIALIZED,

        /**
         * 容器销毁事件
         */
        SERVLET_CONTEXT_DESTROYED,

        /**
         * 容器属性添加
         */
        SERVLET_CONTEXT_ATTR_ADDED,

        /**
         * 容器属性移除
         */
        SERVLET_CONTEXT_ATTR_REMOVED,

        /**
         * 容器属性替换
         */
        SERVLET_CONTEXT_ATTR_REPLACED,

        /**
         * 会话创建事件
         */
        SESSION_CREATED,

        /**
         * 会话销毁事件
         */
        SESSION_DESTROYED,

        /**
         * 会话属性添加
         */
        SESSION_ATTR_ADDED,

        /**
         * 会话属性移除
         */
        SESSION_ATTR_REMOVED,

        /**
         * 会话属性替换
         */
        SESSION_ATTR_REPLACED,

        /**
         * 请求初始化事件
         */
        REQUEST_INITIALIZED,

        /**
         * 请求销毁事件
         */
        REQUEST_DESTROYED,

        /**
         * 请求属性添加
         */
        REQUEST_ATTR_ADDED,

        /**
         * 请求属性移除
         */
        REQUEST_ATTR_REMOVED,

        /**
         * 请求属性替换
         */
        REQUEST_ATTR_REPLACED,

        /**
         * 接收控制器方法请求事件
         */
        REQUEST_RECEIVED,

        /**
         * 完成控制器方法请求事件
         */
        REQUEST_COMPLETED,

        /**
         * 控制器方法执行过程中发生异常错误
         */
        REQUEST_UNEXPECTED_ERROR
    }

    public WebEvent(IWebMvc owner, EVENT eventName) {
        super(owner, WebEvent.class, eventName);
    }
}
