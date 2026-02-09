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

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MockAsyncContext implements AsyncContext {

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final List<AsyncListener> listeners = new ArrayList<>();

    private String dispatchedPath;

    private long timeout = 10 * 1000L;

    private final List<Runnable> dispatchHandlers = new ArrayList<>();

    public MockAsyncContext(ServletRequest request, ServletResponse response) {
        this.request = (HttpServletRequest) request;
        this.response = (HttpServletResponse) response;
    }

    public void addDispatchHandler(Runnable handler) {
        Objects.requireNonNull(handler, "Dispatch handler must not be null");
        synchronized (this) {
            if (this.dispatchedPath == null) {
                this.dispatchHandlers.add(handler);
            } else {
                handler.run();
            }
        }
    }

    @Override
    public ServletRequest getRequest() {
        return this.request;
    }

    @Override
    public ServletResponse getResponse() {
        return this.response;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return (this.request instanceof MockHttpServletRequest && this.response instanceof MockHttpServletResponse);
    }

    @Override
    public void dispatch() {
        dispatch(this.request.getRequestURI());
    }

    @Override
    public void dispatch(String path) {
        dispatch(null, path);
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        synchronized (this) {
            this.dispatchedPath = path;
            for (Runnable r : this.dispatchHandlers) {
                r.run();
            }
        }
    }

    public String getDispatchedPath() {
        return this.dispatchedPath;
    }

    @SuppressWarnings("unchecked")
    private <T> T doGetNativeRequest(ServletRequest request, Class<T> requiredType) {
        if (requiredType != null) {
            if (requiredType.isInstance(request)) {
                return (T) request;
            }
            if (request instanceof ServletRequestWrapper) {
                return doGetNativeRequest(((ServletRequestWrapper) request).getRequest(), requiredType);
            }
        }
        return null;
    }

    @Override
    public void complete() {
        MockHttpServletRequest mockRequest = doGetNativeRequest(request, MockHttpServletRequest.class);
        if (mockRequest != null) {
            mockRequest.setAsyncStarted(false);
        }
        for (AsyncListener listener : this.listeners) {
            try {
                listener.onComplete(new AsyncEvent(this, this.request, this.response));
            } catch (IOException ex) {
                throw new IllegalStateException("AsyncListener failure", ex);
            }
        }
    }

    @Override
    public void start(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void addListener(AsyncListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest request, ServletResponse response) {
        this.listeners.add(listener);
    }

    public List<AsyncListener> getListeners() {
        return this.listeners;
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public long getTimeout() {
        return this.timeout;
    }

    public static class Builder {
        private HttpServletRequest request;
        private HttpServletResponse response;

        public static Builder create() {
            return new Builder();
        }

        public Builder request(HttpServletRequest request) {
            this.request = request;
            return this;
        }

        public Builder response(HttpServletResponse response) {
            this.response = response;
            return this;
        }

        public Builder mock() {
            this.request = new MockHttpServletRequest();
            this.response = new MockHttpServletResponse();
            return this;
        }

        public MockAsyncContext build() {
            Objects.requireNonNull(request, "Request must not be null");
            Objects.requireNonNull(response, "Response must not be null");
            return new MockAsyncContext(request, response);
        }
    }
}
