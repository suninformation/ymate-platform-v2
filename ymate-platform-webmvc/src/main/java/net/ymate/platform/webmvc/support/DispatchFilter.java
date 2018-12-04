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
package net.ymate.platform.webmvc.support;

import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.IWebMvcModuleCfg;
import net.ymate.platform.webmvc.WebMVC;
import net.ymate.platform.webmvc.impl.DefaultRequestContext;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * 基于Filter实现的WebMVC请求分发器
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-23 下午11:19:39
 * @version 1.0
 */
public class DispatchFilter implements Filter {

    private FilterConfig __filterConfig;

    private Pattern __ignorePattern;

    private Dispatcher __dispatcher;

    private String __requestPrefix;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        __filterConfig = filterConfig;
        String _regex = WebMVC.get().getModuleCfg().getRequestIgnoreRegex();
        if (!"false".equalsIgnoreCase(_regex)) {
            __ignorePattern = Pattern.compile(_regex, Pattern.CASE_INSENSITIVE);
        }
        IWebMvcModuleCfg _moduleCfg = WebMVC.get().getModuleCfg();
        __dispatcher = new Dispatcher(_moduleCfg.getDefaultCharsetEncoding(), _moduleCfg.getDefaultContentType(), _moduleCfg.getRequestMethodParam());
        __requestPrefix = _moduleCfg.getRequestPrefix();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (StringUtils.equalsIgnoreCase(((HttpServletRequest) request).getHeader("Connection"), "Upgrade")
                && StringUtils.equalsIgnoreCase(((HttpServletRequest) request).getHeader("Upgrade"), "websocket")) {
            chain.doFilter(request, response);
        } else {
            IRequestContext _requestContext = new DefaultRequestContext((HttpServletRequest) request, __requestPrefix);
            if (null == __ignorePattern || !__ignorePattern.matcher(_requestContext.getOriginalUrl()).find()) {
                __dispatcher.dispatch(_requestContext, __filterConfig.getServletContext(), (HttpServletRequest) request, (HttpServletResponse) response);
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    @Override
    public void destroy() {
    }
}
