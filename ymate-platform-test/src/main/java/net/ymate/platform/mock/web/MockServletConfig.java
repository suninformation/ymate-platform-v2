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

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.*;

public class MockServletConfig implements ServletConfig {

    private final ServletContext servletContext;

    private final String servletName;

    private final Map<String, String> initParameters = new LinkedHashMap<>();

    public MockServletConfig() {
        this(null, "");
    }

    public MockServletConfig(String servletName) {
        this(null, servletName);
    }

    public MockServletConfig(ServletContext servletContext) {
        this(servletContext, "");
    }

    public MockServletConfig(ServletContext servletContext, String servletName) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.servletName = servletName;
    }

    @Override
    public String getServletName() {
        return this.servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
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
        private String servletName = "";
        private final Map<String, String> initParameters = new LinkedHashMap<>();

        public static Builder create() {
            return new Builder();
        }

        public Builder servletContext(ServletContext servletContext) {
            this.servletContext = servletContext;
            return this;
        }

        public Builder servletName(String servletName) {
            this.servletName = servletName;
            return this;
        }

        public Builder initParameter(String name, String value) {
            Objects.requireNonNull(name, "Parameter name must not be null");
            this.initParameters.put(name, value);
            return this;
        }

        public Builder initParameters(Map<String, String> initParameters) {
            if (initParameters != null) {
                this.initParameters.putAll(initParameters);
            }
            return this;
        }

        public MockServletConfig build() {
            MockServletConfig servletConfig = new MockServletConfig(servletContext, servletName);
            // 添加初始化参数
            for (Map.Entry<String, String> entry : this.initParameters.entrySet()) {
                servletConfig.addInitParameter(entry.getKey(), entry.getValue());
            }
            return servletConfig;
        }
    }
}
