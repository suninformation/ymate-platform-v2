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
package net.ymate.platform.mock.web;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("deprecation")
public class MockHttpSession implements HttpSession {

    public static final String SESSION_COOKIE_NAME = "JSESSION";

    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

    private String id;

    private final long creationTime = System.currentTimeMillis();

    private int maxInactiveInterval;

    private long lastAccessedTime = System.currentTimeMillis();

    private final ServletContext servletContext;

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    private boolean invalid = false;

    private boolean isNew = true;

    public MockHttpSession() {
        this(null);
    }

    public MockHttpSession(ServletContext servletContext) {
        this(servletContext, null);
    }

    public MockHttpSession(ServletContext servletContext, String id) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.id = (id != null ? id : Integer.toString(NEXT_ID.getAndIncrement()));
    }

    @Override
    public long getCreationTime() {
        assertIsValid();
        return this.creationTime;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String changeSessionId() {
        this.id = Integer.toString(NEXT_ID.getAndIncrement());
        return this.id;
    }

    public void access() {
        this.lastAccessedTime = System.currentTimeMillis();
        this.isNew = false;
    }

    @Override
    public long getLastAccessedTime() {
        assertIsValid();
        return this.lastAccessedTime;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        this.maxInactiveInterval = interval;
    }

    @Override
    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }

    @Override
    public javax.servlet.http.HttpSessionContext getSessionContext() {
        throw new UnsupportedOperationException("getSessionContext");
    }

    @Override
    public Object getAttribute(String name) {
        if (isInvalid()) {
            return null;
        }
        Objects.requireNonNull(name, "Attribute name must not be null");
        return this.attributes.get(name);
    }

    @Override
    public Object getValue(String name) {
        return getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        assertIsValid();
        return Collections.enumeration(new LinkedHashSet<>(this.attributes.keySet()));
    }

    @Override
    public String[] getValueNames() {
        assertIsValid();
        return this.attributes.keySet().toArray(new String[0]);
    }

    @Override
    public void setAttribute(String name, Object value) {
        assertIsValid();
        Objects.requireNonNull(name, "Attribute name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
            if (value instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name, value));
            }
        } else {
            removeAttribute(name);
        }
    }

    @Override
    public void putValue(String name, Object value) {
        setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        assertIsValid();
        Objects.requireNonNull(name, "Attribute name must not be null");
        Object value = this.attributes.remove(name);
        if (value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
        }
    }

    @Override
    public void removeValue(String name) {
        removeAttribute(name);
    }

    public void clearAttributes() {
        for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            it.remove();
            if (value instanceof HttpSessionBindingListener) {
                ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
            }
        }
    }

    @Override
    public void invalidate() {
        if (!isInvalid()) {
            this.invalid = true;
            clearAttributes();
        }
    }

    public boolean isInvalid() {
        return this.invalid;
    }

    private void assertIsValid() {
        if (isInvalid()) {
            throw new IllegalStateException("The session has already been invalidated");
        }
    }

    public void setNew(boolean value) {
        this.isNew = value;
    }

    @Override
    public boolean isNew() {
        assertIsValid();
        return this.isNew;
    }

    public Serializable serializeState() {
        HashMap<String, Serializable> state = new HashMap<>();
        for (Iterator<Map.Entry<String, Object>> it = this.attributes.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            it.remove();
            if (value instanceof Serializable) {
                state.put(name, (Serializable) value);
            } else {
                if (value instanceof HttpSessionBindingListener) {
                    ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
                }
            }
        }
        return state;
    }

    @SuppressWarnings("unchecked")
    public void deserializeState(Serializable state) {
        if (!(state instanceof Map)) {
            throw new IllegalArgumentException("Serialized state needs to be of type [java.util.Map]");
        }
        this.attributes.putAll((Map<String, Object>) state);
    }

    public static class Builder {
        private String id;
        private int maxInactiveInterval;
        private ServletContext servletContext;
        private final Map<String, Object> attributes = new LinkedHashMap<>();
        private boolean invalid = false;
        private boolean isNew = true;

        public static Builder create() {
            return new Builder();
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder maxInactiveInterval(int maxInactiveInterval) {
            this.maxInactiveInterval = maxInactiveInterval;
            return this;
        }

        public Builder servletContext(ServletContext servletContext) {
            this.servletContext = servletContext;
            return this;
        }

        public Builder attribute(String name, Object value) {
            Objects.requireNonNull(name, "Attribute name must not be null");
            this.attributes.put(name, value);
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public Builder invalid(boolean invalid) {
            this.invalid = invalid;
            return this;
        }

        public Builder isNew(boolean isNew) {
            this.isNew = isNew;
            return this;
        }

        public MockHttpSession build() {
            MockHttpSession session = new MockHttpSession(servletContext, id);
            session.maxInactiveInterval = this.maxInactiveInterval;
            for (Map.Entry<String, Object> entry : this.attributes.entrySet()) {
                session.setAttribute(entry.getKey(), entry.getValue());
            }
            session.invalid = this.invalid;
            session.isNew = this.isNew;
            return session;
        }
    }
}
