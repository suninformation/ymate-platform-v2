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
package net.ymate.platform.webmvc.support;

import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.IWebMvcConfig;
import net.ymate.platform.webmvc.impl.DefaultRequestContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 基于HttpServlet实现的WebMVC请求分发器
 *
 * @author 刘镇 (suninformation@163.com) on 2013年8月18日 下午7:04:30
 */
public class DispatchServlet extends HttpServlet {

    private ServletContext servletContext;

    private Dispatcher dispatcher;

    private String requestPrefix;

    private boolean strictMode;

    @Override
    public void init(ServletConfig config) {
        servletContext = config.getServletContext();
        //
        IWebMvcConfig mvcConfig = ((IWebMvc) config.getServletContext().getAttribute(IWebMvc.class.getName())).getConfig();
        dispatcher = new Dispatcher(mvcConfig.getDefaultCharsetEncoding(), mvcConfig.getDefaultContentType(), mvcConfig.getRequestMethodParam());
        requestPrefix = mvcConfig.getRequestPrefix();
        strictMode = mvcConfig.isRequestStrictModeEnabled();
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        IRequestContext requestContext = new DefaultRequestContext(request, requestPrefix, strictMode);
        dispatcher.dispatch(requestContext, servletContext, request, response);
    }
}
