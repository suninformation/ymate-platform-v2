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
package net.ymate.platform.cache;

import net.ymate.platform.core.event.AbstractEventContext;
import net.ymate.platform.core.event.IEvent;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/11/20 上午4:51
 */
public class CacheEvent extends AbstractEventContext<ICaches, CacheEvent.EVENT> implements IEvent {

    private static final long serialVersionUID = 1L;

    /**
     * Cache事件枚举
     */
    public enum EVENT {

        /**
         * 缓存元素添加
         */
        ELEMENT_PUT,

        /**
         * 缓存元素更新
         */
        ELEMENT_UPDATED,

        /**
         * 缓存元素过期
         */
        ELEMENT_EXPIRED,

        /**
         * 缓存元素被驱逐
         */
        ELEMENT_EVICTED,

        /**
         * 缓存元素移除
         */
        ELEMENT_REMOVED,

        /**
         * 缓存元素被清空
         */
        ELEMENT_REMOVED_ALL
    }

    public CacheEvent(ICaches owner, EVENT eventName) {
        super(owner, CacheEvent.class, eventName);
    }
}
