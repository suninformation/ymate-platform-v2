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

import org.apache.commons.lang3.ArrayUtils;

import javax.servlet.*;
import java.io.IOException;
import java.util.*;

public class MockFilterChain implements FilterChain {

    private ServletRequest request;

    private ServletResponse response;

    private final List<Filter> filters;

    private Iterator<Filter> iterator;

    /**
     * 无参构造方法，创建空的过滤器链
     */
    public MockFilterChain() {
        this.filters = Collections.emptyList();
    }

    /**
     * 使用指定的Servlet创建过滤器链
     *
     * @param servlet Servlet实例
     */
    public MockFilterChain(Servlet servlet) {
        this.filters = initFilterList(servlet);
    }

    /**
     * 使用指定的Servlet和过滤器创建过滤器链
     *
     * @param servlet Servlet实例
     * @param filters 过滤器数组
     * @throws NullPointerException 如果filters为null或包含null值
     */
    public MockFilterChain(Servlet servlet, Filter... filters) {
        Objects.requireNonNull(filters, "filters cannot be null");
        for (Filter filter : filters) {
            Objects.requireNonNull(filter, "filters cannot contain null values");
        }
        this.filters = initFilterList(servlet, filters);
    }

    /**
     * 初始化过滤器列表
     *
     * @param servlet Servlet实例
     * @param filters 过滤器数组
     * @return 过滤器列表
     */
    private static List<Filter> initFilterList(Servlet servlet, Filter... filters) {
        List<Filter> allFilters = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(filters)) {
            Collections.addAll(allFilters, filters);
        }
        allFilters.add(new ServletFilterProxy(servlet));
        return allFilters;
    }

    /**
     * 获取当前请求对象
     *
     * @return 请求对象
     */
    public ServletRequest getRequest() {
        return this.request;
    }

    /**
     * 获取当前响应对象
     *
     * @return 响应对象
     */
    public ServletResponse getResponse() {
        return this.response;
    }

    /**
     * 执行过滤器链
     *
     * @param request  请求对象
     * @param response 响应对象
     * @throws IOException           如果发生I/O异常
     * @throws ServletException      如果发生Servlet异常
     * @throws NullPointerException  如果request或response为null
     * @throws IllegalStateException 如果过滤器链已经被调用过
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        Objects.requireNonNull(request, "Request must not be null");
        Objects.requireNonNull(response, "Response must not be null");
        if (this.request != null) {
            throw new IllegalStateException("This FilterChain has already been called!");
        }
        if (this.iterator == null) {
            this.iterator = this.filters.iterator();
        }
        if (this.iterator.hasNext()) {
            Filter nextFilter = this.iterator.next();
            nextFilter.doFilter(request, response, this);
        }
        this.request = request;
        this.response = response;
    }

    /**
     * 重置过滤器链状态，使其可以再次被调用
     */
    public void reset() {
        this.request = null;
        this.response = null;
        this.iterator = null;
    }

    /**
     * Servlet过滤器代理，用于将Servlet包装为Filter
     */
    private static class ServletFilterProxy implements Filter {

        private final Servlet delegateServlet;

        /**
         * 构造方法
         *
         * @param servlet Servlet实例
         * @throws NullPointerException 如果servlet为null
         */
        private ServletFilterProxy(Servlet servlet) {
            Objects.requireNonNull(servlet, "servlet cannot be null");
            this.delegateServlet = servlet;
        }

        /**
         * 执行过滤操作，调用Servlet的service方法
         *
         * @param request  请求对象
         * @param response 响应对象
         * @param chain    过滤器链
         * @throws IOException      如果发生I/O异常
         * @throws ServletException 如果发生Servlet异常
         */
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            this.delegateServlet.service(request, response);
        }

        /**
         * 初始化过滤器
         *
         * @param filterConfig 过滤器配置
         * @throws ServletException 如果发生Servlet异常
         */
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
        }

        /**
         * 销毁过滤器
         */
        @Override
        public void destroy() {
        }

        /**
         * 返回字符串表示
         *
         * @return 字符串表示
         */
        @Override
        public String toString() {
            return this.delegateServlet.toString();
        }
    }

    public static class Builder {
        private Servlet servlet;
        private List<Filter> filters = new ArrayList<>();

        public static Builder create() {
            return new Builder();
        }

        public Builder servlet(Servlet servlet) {
            this.servlet = servlet;
            return this;
        }

        public Builder addFilter(Filter filter) {
            Objects.requireNonNull(filter, "Filter must not be null");
            this.filters.add(filter);
            return this;
        }

        public Builder addFilters(Filter... filters) {
            if (ArrayUtils.isNotEmpty(filters)) {
                for (Filter filter : filters) {
                    Objects.requireNonNull(filter, "Filter must not be null");
                    this.filters.add(filter);
                }
            }
            return this;
        }

        public Builder addFilters(List<Filter> filters) {
            if (filters != null) {
                for (Filter filter : filters) {
                    Objects.requireNonNull(filter, "Filter must not be null");
                    this.filters.add(filter);
                }
            }
            return this;
        }

        public MockFilterChain build() {
            if (servlet != null) {
                return new MockFilterChain(servlet, filters.toArray(new Filter[0]));
            } else {
                return new MockFilterChain();
            }
        }
    }
}
