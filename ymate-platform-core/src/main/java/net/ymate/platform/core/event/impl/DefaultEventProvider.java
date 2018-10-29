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
package net.ymate.platform.core.event.impl;

import net.ymate.platform.core.event.*;
import net.ymate.platform.core.support.DefaultThreadFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * 默认事件管理提供者接口实现
 *
 * @param <T>       事件所有者类型
 * @param <E>       事件枚举
 * @param <EVENT>   事件对象类型
 * @param <CONTEXT> 事件监听器上下文对象类型
 * @author 刘镇 (suninformation@163.com) on 15/5/16 上午2:38
 * @version 1.0
 */
public final class DefaultEventProvider<T, E extends Enum<E>, EVENT extends Class<? extends IEvent>, CONTEXT extends EventContext<T, E>> implements IEventProvider<T, E, EVENT, CONTEXT> {

    private static final Log _LOG = LogFactory.getLog(DefaultEventProvider.class);

    private IEventConfig __eventConfig;

    private ExecutorService __eventExecutorService;

    private List<EVENT> __events = new CopyOnWriteArrayList<EVENT>();

    private Map<EVENT, List<IEventListener<CONTEXT>>> __asyncListeners = new ConcurrentHashMap<EVENT, List<IEventListener<CONTEXT>>>();

    private Map<EVENT, List<IEventListener<CONTEXT>>> __normalListeners = new ConcurrentHashMap<EVENT, List<IEventListener<CONTEXT>>>();

    @Override
    public void init(IEventConfig eventConfig) {
        __eventConfig = eventConfig;
        __eventExecutorService = DefaultThreadFactory.newThreadExecutor(eventConfig.getThreadPoolSize() > 0 ? eventConfig.getThreadPoolSize() : Runtime.getRuntime().availableProcessors(),
                eventConfig.getThreadMaxPoolSize() > 0 ? eventConfig.getThreadMaxPoolSize() : 200, 0L, eventConfig.getThreadQueueSize() > 0 ? eventConfig.getThreadQueueSize() : 1024, DefaultThreadFactory.create("event-pool-"));
    }

    @Override
    public IEventConfig getEventConfig() {
        return __eventConfig;
    }

    @Override
    public void destroy() {
        if (__eventExecutorService != null) {
            __eventExecutorService.shutdown();
            __eventExecutorService = null;
        }
        __events = null;
        __asyncListeners = null;
        __normalListeners = null;
    }

    @Override
    public void registerEvent(EVENT eventClass) {
        if (!__events.contains(eventClass)) {
            __events.add(eventClass);
        } else {
            _LOG.warn("Event class [" + eventClass + "] duplicate registration is not allowed");
        }
    }

    @Override
    public boolean unregisterEvent(EVENT eventClass) {
        return __events.remove(eventClass);
    }

    private void __doRegisterEventListener(Map<EVENT, List<IEventListener<CONTEXT>>> listenersMap, EVENT eventClass, IEventListener<CONTEXT> eventListener) {
        if (listenersMap.containsKey(eventClass)) {
            List<IEventListener<CONTEXT>> _listeners = listenersMap.get(eventClass);
            if (!_listeners.contains(eventListener)) {
                _listeners.add(eventListener);
            } else {
                _LOG.warn("EventListener object [" + eventListener.getClass() + "] duplicate registration is not allowed");
            }
        } else {
            List<IEventListener<CONTEXT>> _listeners = new ArrayList<IEventListener<CONTEXT>>();
            _listeners.add(eventListener);
            listenersMap.put(eventClass, _listeners);
        }
    }

    private boolean __doUnregisterEventListener(Map<EVENT, List<IEventListener<CONTEXT>>> listenersMap, EVENT eventClass, Class<? extends IEventListener> listenerClass) {
        boolean _flag = false;
        List<IEventListener<CONTEXT>> _listeners = listenersMap.get(eventClass);
        if (_listeners != null) {
            for (IEventListener<CONTEXT> _item : _listeners) {
                if (_item.getClass().equals(listenerClass)) {
                    _listeners.remove(_item);
                    _flag = true;
                    break;
                }
            }
        }
        return _flag;
    }

    @Override
    public void registerListener(EVENT eventClass, IEventListener<CONTEXT> eventListener) {
        registerListener(__eventConfig.getDefaultMode(), eventClass, eventListener);
    }

    @Override
    public void registerListener(Events.MODE mode, EVENT eventClass, IEventListener<CONTEXT> eventListener) {
        switch (mode) {
            case ASYNC:
                __doRegisterEventListener(__asyncListeners, eventClass, eventListener);
                break;
            default:
                __doRegisterEventListener(__normalListeners, eventClass, eventListener);
        }
    }

    @Override
    public boolean unregisterListener(EVENT eventClass, Class<? extends IEventListener> listenerClass) {
        return __doUnregisterEventListener(__asyncListeners, eventClass, listenerClass) || __doUnregisterEventListener(__normalListeners, eventClass, listenerClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fireEvent(final CONTEXT context) {
        EVENT _eventKey = (EVENT) context.getEventClass();
        if (__events.contains(_eventKey)) {
            // 先执行同步事件
            Collection<IEventListener<CONTEXT>> _listeners = __normalListeners.get(_eventKey);
            if (_listeners != null && !_listeners.isEmpty()) {
                for (IEventListener<CONTEXT> _listener : _listeners) {
                    if (_listener.handle(context)) {
                        // 返回值若为true则表示终止同步事件广播并结束执行
                        break;
                    }
                }
            }
            // 再触发异步事件
            _listeners = __asyncListeners.get(_eventKey);
            if (_listeners != null && !_listeners.isEmpty()) {
                for (final IEventListener<CONTEXT> _listener : _listeners) {
                    if (__eventExecutorService != null) {
                        __eventExecutorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                _listener.handle(context);
                            }
                        });
                    }
                }
            }
        }
    }
}
