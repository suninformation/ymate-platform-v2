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
package net.ymate.platform.serv;

import net.ymate.platform.core.util.UUIDUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/15 下午11:25
 * @version 1.0
 */
public abstract class AbstractSession implements ISession {

    private final String __id;

    private long __lastTouchTime;

    private final ConcurrentMap<String, Object> __attributes;

    public AbstractSession() {
        __id = UUIDUtils.UUID();
        __lastTouchTime = System.currentTimeMillis();
        __attributes = new ConcurrentHashMap<String, Object>();
    }

    @Override
    public String id() {
        return __id;
    }

    @Override
    public boolean isNew() {
        return status() == Status.NEW;
    }

    @Override
    public boolean isConnected() {
        return status() == Status.CONNECTED;
    }

    @Override
    public void touch() {
        __lastTouchTime = System.currentTimeMillis();
    }

    @Override
    public long lastTouchTime() {
        return __lastTouchTime;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T attr(String key) {
        return (T) __attributes.get(key);
    }

    @Override
    public void attr(String key, Object value) {
        __attributes.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractSession session = (AbstractSession) o;
        return __id.equals(session.__id);
    }

    @Override
    public int hashCode() {
        return __id.hashCode();
    }
}
