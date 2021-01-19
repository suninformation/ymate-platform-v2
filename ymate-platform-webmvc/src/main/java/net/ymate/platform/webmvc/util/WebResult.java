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
package net.ymate.platform.webmvc.util;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.support.ErrorCode;
import net.ymate.platform.webmvc.AbstractWebResult;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.IWebResult;
import net.ymate.platform.webmvc.IWebResultBuilder;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.impl.DefaultWebResultBuilder;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.impl.HttpStatusView;
import net.ymate.platform.webmvc.view.impl.JspView;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/8/18 下午2:18
 * @since 2.0.6
 */
public final class WebResult extends AbstractWebResult<Integer> {

    public static IWebResultBuilder builder() {
        IWebResultBuilder builder = null;
        try {
            builder = ClassUtils.getExtensionLoader(IWebResultBuilder.class).getExtensionClass().newInstance();
        } catch (Exception ignored) {
        }
        return builder != null ? builder : new DefaultWebResultBuilder();
    }

    public static IWebResultBuilder builder(ErrorCode errorCode) {
        return builder(WebContext.getContext().getOwner(), null, errorCode);
    }

    public static IWebResultBuilder builder(String resourceName, ErrorCode errorCode) {
        return builder(WebContext.getContext().getOwner(), resourceName, errorCode);
    }

    public static IWebResultBuilder builder(IWebMvc owner, ErrorCode errorCode) {
        return builder(owner, null, errorCode);
    }

    public static IWebResultBuilder builder(IWebMvc owner, String resourceName, ErrorCode errorCode) {
        IWebResultBuilder builder = builder();
        String msg = null;
        if (StringUtils.isNotBlank(errorCode.i18nKey())) {
            msg = WebUtils.i18nStr(owner, resourceName, errorCode.i18nKey(), null);
        }
        if (StringUtils.isBlank(msg)) {
            msg = WebUtils.errorCodeI18n(owner, resourceName, errorCode.code(), errorCode.message());
        }
        builder.code(errorCode.code()).msg(msg);
        if (!errorCode.attrs().isEmpty()) {
            builder.attrs(errorCode.attrs());
        }
        if (!errorCode.data().isEmpty()) {
            builder.data(errorCode.data());
        }
        return builder;
    }

    public static WebResult create() {
        return new WebResult();
    }

    public static WebResult create(int code) {
        return new WebResult(code);
    }

    public static WebResult create(ErrorCode errorCode) {
        return create(WebContext.getContext().getOwner(), null, errorCode);
    }

    public static WebResult create(IWebMvc owner, ErrorCode errorCode) {
        return create(owner, null, errorCode);
    }

    public static WebResult create(String resourceName, ErrorCode errorCode) {
        return create(WebContext.getContext().getOwner(), resourceName, errorCode);
    }

    public static WebResult create(IWebMvc owner, String resourceName, ErrorCode errorCode) {
        String msg = null;
        if (StringUtils.isNotBlank(errorCode.i18nKey())) {
            msg = WebUtils.i18nStr(owner, resourceName, errorCode.i18nKey(), null);
        }
        if (StringUtils.isBlank(msg)) {
            msg = WebUtils.errorCodeI18n(owner, resourceName, errorCode.code(), errorCode.message());
        }
        WebResult result = new WebResult(errorCode.code()).msg(msg);
        if (!errorCode.attrs().isEmpty()) {
            result.attrs(errorCode.attrs());
        }
        if (!errorCode.data().isEmpty()) {
            result.data(errorCode.data());
        }
        return result;
    }

    public static WebResult succeed() {
        return new WebResult(ErrorCode.SUCCEED);
    }

    public WebResult() {
        super();
    }

    public WebResult(int code) {
        super(code);
    }

    @Override
    public boolean isSuccess() {
        return code() != null && code().equals(ErrorCode.SUCCEED);
    }

    @Override
    public WebResult code(Integer code) {
        super.code(code);
        return this;
    }

    @Override
    public WebResult msg(String msg) {
        super.msg(msg);
        return this;
    }

    @Override
    public WebResult data(Object data) {
        super.data(data);
        return this;
    }

    @Override
    public WebResult attrs(Map<String, Object> attrs) {
        super.attrs(attrs);
        return this;
    }

    @Override
    public WebResult dataAttr(String dataKey, Object dataValue) {
        super.dataAttr(dataKey, dataValue);
        return this;
    }

    @Override
    public WebResult attr(String attrKey, Object attrValue) {
        super.attr(attrKey, attrValue);
        return this;
    }

    @Override
    public WebResult withContentType() {
        super.withContentType();
        return this;
    }

    @Override
    public WebResult keepNullValue() {
        super.keepNullValue();
        return this;
    }

    @Override
    public WebResult snakeCase() {
        super.snakeCase();
        return this;
    }

    public static IView formatView(IWebResult<?> result) {
        return formatView(null, Type.Const.PARAM_FORMAT, Type.Const.PARAM_CALLBACK, result);
    }

    public static IView formatView(String path, IWebResult<?> result) {
        return formatView(path, Type.Const.PARAM_FORMAT, Type.Const.PARAM_CALLBACK, result);
    }

    public static IView formatView(IWebResult<?> result, String defaultFormat) {
        return formatView(null, Type.Const.PARAM_FORMAT, defaultFormat, Type.Const.PARAM_CALLBACK, result);
    }

    /**
     * @param path          JSP模块路径
     * @param paramFormat   数据格式，可选值：json|jsonp|xml
     * @param paramCallback 当数据结式为jsonp时，指定回调方法参数名
     * @param result        回应的数据对象
     * @return 根据paramFormat等参数判断返回对应的视图对象
     */
    public static IView formatView(String path, String paramFormat, String paramCallback, IWebResult<?> result) {
        return formatView(path, paramFormat, null, paramCallback, result);
    }

    public static IView formatView(String path, String paramFormat, String defaultFormat, String paramCallback, IWebResult<?> result) {
        IView returnView = null;
        if (result != null) {
            HttpServletRequest request = WebContext.getRequest();
            if (WebUtils.isJsonAccepted(request, paramFormat) || StringUtils.equalsIgnoreCase(defaultFormat, Type.Const.FORMAT_JSON)) {
                returnView = result.withContentType().toJsonView(StringUtils.trimToNull(WebContext.getRequest().getParameter(paramCallback)));
            } else if (WebUtils.isXmlAccepted(request, paramFormat) || StringUtils.equalsIgnoreCase(defaultFormat, Type.Const.FORMAT_XML)) {
                returnView = result.withContentType().toXmlView();
            } else if (WebUtils.isAjax(request)) {
                returnView = result.withContentType().toJsonView(StringUtils.trimToNull(WebContext.getRequest().getParameter(paramCallback)));
            }
        }
        if (returnView == null) {
            if (StringUtils.isNotBlank(path)) {
                returnView = new JspView(path);
                if (result != null) {
                    returnView.addAttribute(Type.Const.PARAM_RET, result.code());
                    //
                    if (StringUtils.isNotBlank(result.msg())) {
                        returnView.addAttribute(Type.Const.PARAM_MSG, result.msg());
                    }
                    if (result.data() != null) {
                        returnView.addAttribute(Type.Const.PARAM_DATA, result.data());
                    }
                    for (Map.Entry<String, Object> entry : result.attrs().entrySet()) {
                        returnView.addAttribute(entry.getKey(), entry.getValue());
                    }
                }
            } else if (result != null && StringUtils.isNotBlank(result.msg())) {
                returnView = new HttpStatusView(HttpServletResponse.SC_BAD_REQUEST, result.msg());
            } else {
                returnView = new HttpStatusView(HttpServletResponse.SC_BAD_REQUEST);
            }
        }
        return returnView;
    }
}
