/*
 * Copyright 2007-present the original author or authors.
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
package net.ymate.platform.core.persistence;

import java.util.*;

/**
 * 会话事件监听器抽象实现
 *
 * @param <LISTENER_TYPE> 事件监听器类型
 * @param <EVENT_CONTEXT> 事件上下文类型
 * @author 刘镇 (suninformation@163.com) on 2026/3/24 10:34
 * @since 2.1.4
 */
public abstract class AbstractSessionEventListener<LISTENER_TYPE extends ISessionEventListener<EVENT_CONTEXT>, EVENT_CONTEXT extends SessionEventContext> implements ISessionEventListener<EVENT_CONTEXT> {

    private final List<LISTENER_TYPE> listeners = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public AbstractSessionEventListener(LISTENER_TYPE... listeners) {
        addListener(listeners);
    }

    public List<LISTENER_TYPE> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

    @SuppressWarnings("unchecked")
    public AbstractSessionEventListener<LISTENER_TYPE, EVENT_CONTEXT> addListener(LISTENER_TYPE... listeners) {
        Arrays.stream(listeners).filter(Objects::nonNull).forEachOrdered(this.listeners::add);
        return this;
    }

    public AbstractSessionEventListener<LISTENER_TYPE, EVENT_CONTEXT> addListener(Iterable<LISTENER_TYPE> listeners) {
        listeners.forEach(listener -> {
            if (listener != null) {
                this.listeners.add(listener);
            }
        });
        return this;
    }

    @Override
    public void onQueryBefore(EVENT_CONTEXT eventContext) throws Exception {
        for (LISTENER_TYPE listener : listeners) {
            listener.onQueryBefore(eventContext);
        }
    }

    @Override
    public void onQueryAfter(EVENT_CONTEXT eventContext) throws Exception {
        for (LISTENER_TYPE listener : listeners) {
            listener.onQueryAfter(eventContext);
        }
    }

    @Override
    public void onInsertBefore(EVENT_CONTEXT eventContext) throws Exception {
        for (LISTENER_TYPE listener : listeners) {
            listener.onInsertBefore(eventContext);
        }
    }

    @Override
    public void onInsertAfter(EVENT_CONTEXT eventContext) throws Exception {
        for (LISTENER_TYPE listener : listeners) {
            listener.onInsertAfter(eventContext);
        }
    }

    @Override
    public void onInsertIfNotExistBefore(EVENT_CONTEXT eventContext) throws Exception {
        for (LISTENER_TYPE listener : listeners) {
            listener.onInsertIfNotExistBefore(eventContext);
        }
    }

    @Override
    public void onInsertIfNotExistAfter(EVENT_CONTEXT eventContext) throws Exception {
        for (LISTENER_TYPE listener : listeners) {
            listener.onInsertIfNotExistAfter(eventContext);
        }
    }

    @Override
    public void onUpdateBefore(EVENT_CONTEXT eventContext) throws Exception {
        for (LISTENER_TYPE listener : listeners) {
            listener.onUpdateBefore(eventContext);
        }
    }

    @Override
    public void onUpdateAfter(EVENT_CONTEXT eventContext) throws Exception {
        for (LISTENER_TYPE listener : listeners) {
            listener.onUpdateAfter(eventContext);
        }
    }

    @Override
    public void onUpsertBefore(EVENT_CONTEXT eventContext) throws Exception {
        for (LISTENER_TYPE listener : listeners) {
            listener.onUpsertBefore(eventContext);
        }
    }

    @Override
    public void onUpsertAfter(EVENT_CONTEXT eventContext) throws Exception {
        for (LISTENER_TYPE listener : listeners) {
            listener.onUpsertAfter(eventContext);
        }
    }

    @Override
    public void onRemoveBefore(EVENT_CONTEXT eventContext) throws Exception {
        for (LISTENER_TYPE listener : listeners) {
            listener.onRemoveBefore(eventContext);
        }
    }

    @Override
    public void onRemoveAfter(EVENT_CONTEXT eventContext) throws Exception {
        for (LISTENER_TYPE listener : listeners) {
            listener.onRemoveAfter(eventContext);
        }
    }
}