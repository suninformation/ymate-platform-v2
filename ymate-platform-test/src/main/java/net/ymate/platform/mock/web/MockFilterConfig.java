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

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.*;

public class MockFilterConfig implements FilterConfig {

    private final ServletContext servletContext;

    private final String filterName;

    private final Map<String, String> initParameters = new LinkedHashMap<String, String>();

    public MockFilterConfig() {
        this(null, "");
    }

    public MockFilterConfig(String filterName) {
        this(null, filterName);
    }

    public MockFilterConfig(ServletContext servletContext) {
        this(servletContext, "");
    }

    public MockFilterConfig(ServletContext servletContext, String filterName) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.filterName = filterName;
    }

    @Override
    public String getFilterName() {
        return filterName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    public void addInitParameter(String name, String value) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        this.initParameters.put(name, value);
    }

    @Override
    public String getInitParameter(String name) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        return this.initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }

    public static class Builder {
        private ServletContext servletContext;
        private String filterName;
        private Map<String, String> initParameters = new LinkedHashMap<>();

        public static Builder create() {
            return new Builder();
        }

        public Builder servletContext(ServletContext servletContext) {
            this.servletContext = servletContext;
            return this;
        }

        public Builder filterName(String filterName) {
            this.filterName = filterName;
            return this;
        }

        public Builder addInitParameter(String name, String value) {
            Objects.requireNonNull(name, "Parameter name must not be null");
            this.initParameters.put(name, value);
            return this;
        }

        public Builder addInitParameters(Map<String, String> initParameters) {
            if (initParameters != null) {
                for (Map.Entry<String, String> entry : initParameters.entrySet()) {
                    Objects.requireNonNull(entry.getKey(), "Parameter name must not be null");
                    this.initParameters.put(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public MockFilterConfig build() {
            MockFilterConfig mockFilterConfig = new MockFilterConfig(servletContext, filterName);
            initParameters.forEach(mockFilterConfig::addInitParameter);
            return mockFilterConfig;
        }
    }
}
