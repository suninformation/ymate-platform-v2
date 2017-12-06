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
package net.ymate.platform.cache;

import net.ymate.platform.core.event.EventContext;
import net.ymate.platform.core.event.IEvent;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/11/20 上午4:51
 * @version 1.0
 */
public class CacheEvent extends EventContext<ICaches, CacheEvent.EVENT> implements IEvent {

    /**
     * Cache事件枚举
     */
    public enum EVENT {
        ELEMENT_PUT,
        ELEMENT_UPDATED,
        ELEMENT_EXPIRED,
        ELEMENT_EVICTED,
        ELEMENT_REMOVED,
        ELEMENT_REMOVED_ALL
    }

    public CacheEvent(ICaches owner, EVENT eventName) {
        super(owner, CacheEvent.class, eventName);
    }
}
