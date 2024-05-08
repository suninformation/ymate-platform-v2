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
import net.ymate.platform.webmvc.util.WebUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 基于Filter实现的WebMVC请求分发器
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-23 下午11:19:39
 */
public class DispatchFilter implements Filter {

    private FilterConfig filterConfig;

    private Pattern ignorePattern;

    private Dispatcher dispatcher;

    private String requestPrefix;

    private boolean strictMode;

    private final Set<String> requestIgnoreUrls = new HashSet<>();

    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        IWebMvcConfig config = ((IWebMvc) filterConfig.getServletContext().getAttribute(IWebMvc.class.getName())).getConfig();
        Set<String> ignoreSuffixes = config.getRequestIgnoreSuffixes();
        String ignoreRegex;
        if (ignoreSuffixes != null && !ignoreSuffixes.isEmpty()) {
            ignoreRegex = IWebMvcConfig.IGNORE_REGEX_PREFIX + StringUtils.join(ignoreSuffixes, "|") + IWebMvcConfig.IGNORE_REGEX_SUFFIX;
        } else {
            ignoreRegex = IWebMvcConfig.IGNORE_REGEX;
        }
        ignorePattern = Pattern.compile(ignoreRegex, Pattern.CASE_INSENSITIVE);
        dispatcher = new Dispatcher(config.getDefaultCharsetEncoding(), config.getDefaultContentType(), config.getRequestMethodParam());
        requestPrefix = config.getRequestPrefix();
        strictMode = config.isRequestStrictModeEnabled();
        //
        String[] requestIgnoreUrlArr = StringUtils.split(StringUtils.defaultIfBlank(filterConfig.getInitParameter("requestIgnoreUrls"), filterConfig.getInitParameter("request-ignore-urls")), "|");
        if (ArrayUtils.isNotEmpty(requestIgnoreUrlArr)) {
            Arrays.stream(requestIgnoreUrlArr).filter(StringUtils::isNotBlank).forEach(requestIgnoreUrls::add);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (WebUtils.isWebSocket((HttpServletRequest) request)) {
            chain.doFilter(request, response);
        } else {
            IRequestContext requestContext = new DefaultRequestContext((HttpServletRequest) request, requestPrefix, strictMode);
            if (!requestIgnoreUrls.isEmpty() && requestIgnoreUrls.stream().anyMatch(s -> StringUtils.startsWith(requestContext.getOriginalUrl(), s))) {
                chain.doFilter(request, response);
            } else if (!ignorePattern.matcher(requestContext.getOriginalUrl()).find()) {
                dispatcher.dispatch(requestContext, filterConfig.getServletContext(), (HttpServletRequest) request, (HttpServletResponse) response);
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    @Override
    public void destroy() {
    }
}
