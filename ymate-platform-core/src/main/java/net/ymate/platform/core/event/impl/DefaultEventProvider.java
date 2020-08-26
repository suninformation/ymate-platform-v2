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

import net.ymate.platform.commons.impl.DefaultThreadFactory;
import net.ymate.platform.commons.util.ThreadUtils;
import net.ymate.platform.core.event.*;
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
 */
public final class DefaultEventProvider<T, E extends Enum<E>, EVENT extends Class<? extends IEvent>, CONTEXT extends AbstractEventContext<T, E>> implements IEventProvider<T, E, EVENT, CONTEXT> {

    private static final Log LOG = LogFactory.getLog(DefaultEventProvider.class);

    private IEventConfig eventConfig;

    private ExecutorService executorService;

    private List<EVENT> events = new CopyOnWriteArrayList<>();

    private Map<EVENT, List<IEventListener<CONTEXT>>> asyncListeners = new ConcurrentHashMap<>();

    private Map<EVENT, List<IEventListener<CONTEXT>>> normalListeners = new ConcurrentHashMap<>();

    private boolean initialized;

    @Override
    public void initialize(IEventConfig eventConfig) {
        if (!initialized) {
            this.eventConfig = eventConfig;
            //
            int corePoolSize = eventConfig.getThreadPoolSize() > 0 ? eventConfig.getThreadPoolSize() : Runtime.getRuntime().availableProcessors();
            int maxPoolSize = eventConfig.getThreadMaxPoolSize() > 0 ? eventConfig.getThreadMaxPoolSize() : 200;
            int queueSize = eventConfig.getThreadQueueSize() > 0 ? eventConfig.getThreadQueueSize() : 1024;
            //
            executorService = ThreadUtils.newThreadExecutor(corePoolSize, maxPoolSize, 0L, queueSize, DefaultThreadFactory.create("event-pool-"));
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public IEventConfig getEventConfig() {
        return eventConfig;
    }

    @Override
    public void destroy() {
        if (initialized) {
            initialized = false;
            //
            if (executorService != null) {
                executorService.shutdown();
                executorService = null;
            }
            events = null;
            asyncListeners = null;
            normalListeners = null;
        }
    }

    @Override
    public void registerEvent(EVENT eventClass) {
        if (!events.contains(eventClass)) {
            events.add(eventClass);
        } else if (LOG.isWarnEnabled()) {
            LOG.warn(String.format("Event class [%s] duplicate registration is not allowed.", eventClass));
        }
    }

    @Override
    public boolean unregisterEvent(EVENT eventClass) {
        return events.remove(eventClass);
    }

    private void registerEventListener(Map<EVENT, List<IEventListener<CONTEXT>>> listenersMap, EVENT eventClass, IEventListener<CONTEXT> eventListener) {
        if (listenersMap.containsKey(eventClass)) {
            List<IEventListener<CONTEXT>> listeners = listenersMap.get(eventClass);
            if (!listeners.contains(eventListener)) {
                listeners.add(eventListener);
            } else if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("EventListener object [%s] duplicate registration is not allowed.", eventListener.getClass()));
            }
        } else {
            List<IEventListener<CONTEXT>> listeners = new ArrayList<>();
            listeners.add(eventListener);
            listenersMap.put(eventClass, listeners);
        }
    }

    @SuppressWarnings("rawtypes")
    private boolean unregisterEventListener(Map<EVENT, List<IEventListener<CONTEXT>>> listenersMap, EVENT eventClass, Class<? extends IEventListener> listenerClass) {
        boolean flag = false;
        List<IEventListener<CONTEXT>> listeners = listenersMap.get(eventClass);
        if (listeners != null) {
            for (IEventListener<CONTEXT> item : listeners) {
                if (item.getClass().equals(listenerClass)) {
                    listeners.remove(item);
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    @Override
    public void registerListener(EVENT eventClass, IEventListener<CONTEXT> eventListener) {
        registerListener(eventConfig.getDefaultMode(), eventClass, eventListener);
    }

    @Override
    public void registerListener(Events.MODE mode, EVENT eventClass, IEventListener<CONTEXT> eventListener) {
        if (mode == Events.MODE.ASYNC) {
            registerEventListener(asyncListeners, eventClass, eventListener);
        } else {
            registerEventListener(normalListeners, eventClass, eventListener);
        }
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean unregisterListener(EVENT eventClass, Class<? extends IEventListener> listenerClass) {
        return unregisterEventListener(asyncListeners, eventClass, listenerClass) || unregisterEventListener(normalListeners, eventClass, listenerClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fireEvent(final CONTEXT context) {
        EVENT eventKey = (EVENT) context.getEventClass();
        if (events.contains(eventKey)) {
            // 先执行同步事件
            Collection<IEventListener<CONTEXT>> listeners = normalListeners.get(eventKey);
            if (listeners != null && !listeners.isEmpty()) {
                for (IEventListener<CONTEXT> listener : listeners) {
                    if (listener.handle(context)) {
                        // 返回值若为true则表示终止同步事件广播并结束执行
                        break;
                    }
                }
            }
            // 再触发异步事件
            listeners = asyncListeners.get(eventKey);
            if (listeners != null && !listeners.isEmpty()) {
                listeners.stream().filter(listener -> executorService != null).forEach(listener -> executorService.execute(() -> listener.handle(context)));
            }
        }
    }
}
