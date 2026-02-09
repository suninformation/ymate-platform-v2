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
package net.ymate.platform.mock;

import net.ymate.platform.mock.web.*;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.support.DispatchFilter;
import net.ymate.platform.webmvc.support.DispatchServlet;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 2023/1/6 22:48
 * @since 1.0.1
 */
public class MockWebRequestHelper {

    private final IWebMvc owner;

    private final MockServletContext servletContext;

    private final MockHttpServletRequest servletRequest;

    public static MockWebRequestHelper create(IWebMvc owner) {
        return new MockWebRequestHelper(owner);
    }

    public static MockWebRequestHelper create(IWebMvc owner, MockServletContext servletContext) {
        return new MockWebRequestHelper(owner, servletContext);
    }

    private MockWebRequestHelper(IWebMvc owner) {
        this(owner, new MockServletContext());
    }

    private MockWebRequestHelper(IWebMvc owner, MockServletContext servletContext) {
        this.owner = owner;
        this.servletContext = servletContext != null ? servletContext : new MockServletContext();
        this.servletContext.setAttribute(IWebMvc.class.getName(), owner);
        servletRequest = new MockHttpServletRequest(servletContext);
        servletRequest.addHeader(Type.HttpHead.USER_AGENT, "MockWebRequestHelper/1.0.1");
    }

    public IWebMvc owner() {
        return owner;
    }

    public MockServletContext servletContext() {
        return servletContext;
    }

    public MockHttpServletRequest servletRequest() {
        return servletRequest;
    }

    public MockWebRequestHelper mapping(Type.HttpMethod httpMethod, String mapping) {
        servletRequest.setMethod(httpMethod.name());
        servletRequest.setServletPath(mapping);
        return this;
    }

    public MockWebRequestHelper get(String mapping) {
        return mapping(Type.HttpMethod.GET, mapping);
    }

    public MockWebRequestHelper post(String mapping) {
        return mapping(Type.HttpMethod.POST, mapping);
    }

    public MockWebRequestHelper header(String name, Object value) {
        servletRequest.addHeader(name, value);
        return this;
    }

    public MockWebRequestHelper formUrlencoded() {
        return header(Type.HttpHead.CONTENT_TYPE, "application/x-www-form-urlencoded");
    }

    public MockWebRequestHelper parameter(String name, String value) {
        servletRequest.addParameter(name, value);
        return this;
    }

    public MockWebRequestHelper parameter(String name, String... value) {
        servletRequest.addParameter(name, value);
        return this;
    }

    public MockHttpServletResponse doFilter() throws ServletException, IOException {
        return doFilter(null);
    }

    public MockHttpServletResponse doFilter(MockFilterChain filterChain) throws ServletException, IOException {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        servletResponse.setContentType("text/plain");
        DispatchFilter dispatchFilter = new DispatchFilter();
        dispatchFilter.init(new MockFilterConfig(servletContext, "DispatchFilter"));
        dispatchFilter.doFilter(servletRequest, servletResponse, filterChain != null ? filterChain : new MockFilterChain());
        return servletResponse;
    }

    public MockHttpServletResponse doService() throws ServletException, IOException {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        servletResponse.setContentType("text/plain");
        DispatchServlet servlet = new DispatchServlet();
        servlet.init(new MockServletConfig(servletContext, "DispatchServlet"));
        servlet.service(servletRequest, servletResponse);
        return servletResponse;
    }
}
