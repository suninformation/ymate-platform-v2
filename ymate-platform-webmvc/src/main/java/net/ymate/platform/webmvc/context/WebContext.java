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
package net.ymate.platform.webmvc.context;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.webmvc.IMultipartRequestWrapper;
import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.IUploadFileWrapper;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.base.Type;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import java.util.*;

/**
 * Web环境上下文封装类，为了能够方便代码移植并脱离Web环境依赖进行开发测试(功能参考Struts2)
 *
 * @author 刘镇 (suninformation@163.com) on 2011-7-24 下午10:31:48
 */
public final class WebContext {

    private static final ThreadLocal<WebContext> LOCAL_CONTEXT = new ThreadLocal<>();

    private final Map<String, Object> attributes;

    public static WebContext getContext() {
        return LOCAL_CONTEXT.get();
    }

    public static void destroy() {
        LOCAL_CONTEXT.remove();
    }

    public static WebContext create(IWebMvc webMvc,
                                    IRequestContext requestContext,
                                    ServletContext servletContext,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {

        HashMap<String, Object> contextMap = new HashMap<>(16);
        //
        contextMap.put(Type.Context.WEB_REQUEST_CONTEXT, requestContext);
        contextMap.put(Type.Context.PARAMETERS, request.getParameterMap());
        contextMap.put(Type.Context.REQUEST, new RequestMap(request));
        contextMap.put(Type.Context.SESSION, new SessionMap(request));
        contextMap.put(Type.Context.APPLICATION, new ApplicationMap(servletContext));
        contextMap.put(Type.Context.LOCALE, webMvc.getOwner().getI18n().getDefaultLocale());
        contextMap.put(Type.Context.HTTP_REQUEST, request);
        contextMap.put(Type.Context.HTTP_RESPONSE, response);
        contextMap.put(Type.Context.SERVLET_CONTEXT, servletContext);
        contextMap.put(Type.Context.WEB_CONTEXT_OWNER, webMvc);
        //
        WebContext context = new WebContext(contextMap);
        LOCAL_CONTEXT.set(context);
        //
        return context;
    }

    public static ServletContext getServletContext() {
        return WebContext.getContext().getAttribute(Type.Context.SERVLET_CONTEXT);
    }

    public static PageContext getPageContext() {
        return WebContext.getContext().getAttribute(Type.Context.PAGE_CONTEXT);
    }

    public static IRequestContext getRequestContext() {
        return WebContext.getContext().getAttribute(Type.Context.WEB_REQUEST_CONTEXT);
    }

    public static HttpServletRequest getRequest() {
        return WebContext.getContext().getAttribute(Type.Context.HTTP_REQUEST);
    }

    public static HttpServletResponse getResponse() {
        return WebContext.getContext().getAttribute(Type.Context.HTTP_RESPONSE);
    }

    //

    private WebContext(Map<String, Object> contextMap) {
        attributes = contextMap;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
    }

    public WebContext addAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Locale getLocale() {
        Locale locale = getAttribute(Type.Context.LOCALE);
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }

    public Map<String, Object> getApplication() {
        return getAttribute(Type.Context.APPLICATION);
    }

    public Map<String, Object> getSession() {
        return getAttribute(Type.Context.SESSION);
    }

    public Map<String, Object> getParameters() {
        return getAttribute(Type.Context.PARAMETERS);
    }

    public IWebMvc getOwner() {
        return getAttribute(Type.Context.WEB_CONTEXT_OWNER);
    }

    // -----------------

    @SuppressWarnings("unchecked")
    public <T> T getApplicationAttributeToObject(String name) {
        return (T) getApplication().get(name);
    }

    public String getApplicationAttributeToString(String name) {
        return BlurObject.bind(getApplication().get(name)).toStringValue();
    }

    public int getApplicationAttributeToInt(String name) {
        return BlurObject.bind(getApplication().get(name)).toIntValue();
    }

    public long getApplicationAttributeToLong(String name) {
        return BlurObject.bind(getApplication().get(name)).toLongValue();
    }

    public boolean getApplicationAttributeToBoolean(String name) {
        return BlurObject.bind(getApplication().get(name)).toBooleanValue();
    }

    public WebContext addApplicationAttribute(String name, Object value) {
        getApplication().put(name, value);
        return this;
    }

    // -----------------

    @SuppressWarnings("unchecked")
    public <T> T getSessionAttributeToObject(String name) {
        return (T) getSession().get(name);
    }

    public String getSessionAttributeToString(String name) {
        return BlurObject.bind(getSession().get(name)).toStringValue();
    }

    public int getSessionAttributeToInt(String name) {
        return BlurObject.bind(getSession().get(name)).toIntValue();
    }

    public long getSessionAttributeToLong(String name) {
        return BlurObject.bind(getSession().get(name)).toLongValue();
    }

    public boolean getSessionAttributeToBoolean(String name) {
        return BlurObject.bind(getSession().get(name)).toBooleanValue();
    }

    public WebContext addSessionAttribute(String name, Object value) {
        getSession().put(name, value);
        return this;
    }

    // -----------------

    @SuppressWarnings("unchecked")
    public <T> T getRequestAttributeToObject(String name) {
        return (T) getRequest().getAttribute(name);
    }

    public String getRequestAttributeToString(String name) {
        return BlurObject.bind(getRequest().getAttribute(name)).toStringValue();
    }

    public int getRequestAttributeToInt(String name) {
        return BlurObject.bind(getRequest().getAttribute(name)).toIntValue();
    }

    public long getRequestAttributeToLong(String name) {
        return BlurObject.bind(getRequest().getAttribute(name)).toLongValue();
    }

    public boolean getRequestAttributeToBoolean(String name) {
        return BlurObject.bind(getRequest().getAttribute(name)).toBooleanValue();
    }

    public WebContext addRequestAttribute(String name, Object value) {
        getRequest().setAttribute(name, value);
        return this;
    }

    // -----------------

    public String getParameterToString(String name) {
        String[] values = (String[]) getParameters().get(name);
        if (values != null && values.length > 0) {
            return values[0];
        }
        return null;
    }

    public int getParameterToInt(String name) {
        return BlurObject.bind(getParameterToString(name)).toIntValue();
    }

    public long getParameterToLong(String name) {
        return BlurObject.bind(getParameterToString(name)).toLongValue();
    }

    public boolean getParameterToBoolean(String name) {
        return BlurObject.bind(getParameterToString(name)).toBooleanValue();
    }

    // MultipartRequestWrapper

    public IUploadFileWrapper getUploadFile(String name) {
        if (getRequest() instanceof IMultipartRequestWrapper) {
            return ((IMultipartRequestWrapper) getRequest()).getUploadFile(name);
        }
        return null;
    }

    public IUploadFileWrapper[] getUploadFiles(String name) {
        if (getRequest() instanceof IMultipartRequestWrapper) {
            return ((IMultipartRequestWrapper) getRequest()).getUploadFiles(name);
        }
        return null;
    }

    public Set<IUploadFileWrapper> getUploadFiles() {
        if (getRequest() instanceof IMultipartRequestWrapper) {
            return ((IMultipartRequestWrapper) getRequest()).getUploadFiles();
        }
        return Collections.emptySet();
    }

}
