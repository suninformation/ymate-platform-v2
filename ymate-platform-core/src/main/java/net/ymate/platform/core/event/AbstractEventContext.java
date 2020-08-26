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

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件上下文接口
 *
 * @param <T> 事件源类型
 * @param <E> 事件枚举
 * @author 刘镇 (suninformation@163.com) on 15/5/16 上午2:58
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractEventContext<T, E extends Enum> extends EventObject {

    private final Class<? extends IEvent> eventClass;

    private final E eventName;

    private final Map<String, Object> params = new HashMap<>();

    private final long timestamp;

    protected AbstractEventContext(T owner, Class<? extends IEvent> eventClass, E eventName) {
        super(owner);
        //
        this.eventClass = eventClass;
        this.eventName = eventName;
        //
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getSource() {
        return (T) super.getSource();
    }

    public Class<? extends IEvent> getEventClass() {
        return eventClass;
    }

    public E getEventName() {
        return eventName;
    }

    @SuppressWarnings("unchecked")
    public <EVENT_SOURCE> EVENT_SOURCE getEventSource() {
        return (EVENT_SOURCE) params.get(IEvent.EVENT_SOURCE);
    }

    public AbstractEventContext<T, E> setEventSource(Object eventSource) {
        params.put(IEvent.EVENT_SOURCE, eventSource);
        return this;
    }

    public AbstractEventContext<T, E> addParamExtend(String paramName, Object paramObject) {
        params.put(paramName, paramObject);
        return this;
    }

    public Object getParamExtend(String paramName) {
        return params.get(paramName);
    }

    public long getTimestamp() {
        return timestamp;
    }
}
