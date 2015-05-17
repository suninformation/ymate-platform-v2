/*
 * Copyright 2007-2107 the original author or authors.
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

import net.ymate.platform.core.event.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 默认事件管理提供者接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/16 上午2:38
 * @version 1.0
 */
public class DefaultEventProvider<T, E extends Enum<E>, EVENT extends IEvent<T, E>, CONTEXT extends EventContext<T, E>> implements IEventProvider<T, E, EVENT, CONTEXT> {

    private IEventConfig __eventConfig;

    private ExecutorService __eventExecPool;

    private Map<Class<? extends IEvent>, EVENT> __eventsMap;

    private Map<Class<? extends IEvent>, List<IEventListener<EVENT, CONTEXT>>> __listenersMap;

    public void init(IEventConfig eventConfig) {
        __eventConfig = eventConfig;
        //
        int _poolSize = eventConfig.getThreadPoolSize();
        if (_poolSize <= 0) {
            _poolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;
        }
        __eventExecPool = Executors.newFixedThreadPool(_poolSize);
        //
        __eventsMap = new ConcurrentHashMap<Class<? extends IEvent>, EVENT>();
        __listenersMap = new ConcurrentHashMap<Class<? extends IEvent>, List<IEventListener<EVENT, CONTEXT>>>();
    }

    public IEventConfig getEventConfig() {
        return __eventConfig;
    }

    public void destroy() {
        if (__eventExecPool != null) {
            __eventExecPool.shutdown();
            __eventExecPool = null;
        }
        __eventsMap = null;
        __listenersMap = null;
    }

    public void registerEvent(EVENT event) {
        __eventsMap.put(event.getClass(), event);
    }

    public void registerListener(Class<? extends IEvent> eventClass, IEventListener<EVENT, CONTEXT> eventListener) {
        if (__listenersMap.containsKey(eventClass)) {
            __listenersMap.get(eventClass).add(eventListener);
        } else {
            List<IEventListener<EVENT, CONTEXT>> _listeners = new ArrayList<IEventListener<EVENT, CONTEXT>>();
            _listeners.add(eventListener);
            __listenersMap.put(eventClass, _listeners);
        }
    }

    public void fireEvent(IEvent.MODE mode, final CONTEXT context) {
        final EVENT _event = __eventsMap.get(context.getEventClass());
        if (_event != null) {
            Collection<IEventListener<EVENT, CONTEXT>> _listeners = __listenersMap.get(context.getEventClass());
            switch (mode) {
                case ASYNC:
                    for (final IEventListener<EVENT, CONTEXT> _listener : _listeners) {
                        if (__eventExecPool != null) {
                            __eventExecPool.execute(new Runnable() {
                                public void run() {
                                    _listener.handle(_event, context);
                                }
                            });
                        }
                    }
                    break;
                default:
                    for (final IEventListener<EVENT, CONTEXT> _listener : _listeners) {
                        _listener.handle(_event, context);
                    }
            }
        }
    }
}
