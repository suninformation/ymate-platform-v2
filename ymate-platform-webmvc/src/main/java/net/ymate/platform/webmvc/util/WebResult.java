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

import com.alibaba.fastjson.JSONObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.support.ErrorCode;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;
import net.ymate.platform.webmvc.view.impl.HttpStatusView;
import net.ymate.platform.webmvc.view.impl.JsonView;
import net.ymate.platform.webmvc.view.impl.JspView;
import net.ymate.platform.webmvc.view.impl.TextView;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/8/18 下午2:18
 * @since 2.0.6
 */
public final class WebResult implements Serializable {

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

    private Integer code;

    private String msg;

    private Map<String, Object> data = new LinkedHashMap<>();

    private Map<String, Object> attrs = new LinkedHashMap<>();

    private boolean withContentType;

    private boolean keepNullValue;

    private boolean quoteFieldNames;

    private boolean useSingleQuotes;

    public WebResult() {
    }

    public WebResult(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public WebResult code(Integer code) {
        this.code = code;
        return this;
    }

    public String msg() {
        return StringUtils.trimToEmpty(msg);
    }

    public WebResult msg(String msg) {
        this.msg = msg;
        return this;
    }

    public WebResult data(Object data) {
        attrs.put(Type.Const.PARAM_DATA, data);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T data() {
        return (T) attrs.get(Type.Const.PARAM_DATA);
    }

    public WebResult attrs(Map<String, Object> attrs) {
        this.attrs.putAll(attrs);
        return this;
    }

    public Map<String, Object> attrs() {
        return attrs;
    }

    @SuppressWarnings("unchecked")
    public <T> T dataAttr(String dataKey) {
        return (T) data.get(dataKey);
    }

    public WebResult dataAttr(String dataKey, Object dataValue) {
        data.put(dataKey, dataValue);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T attr(String attrKey) {
        return (T) attrs.get(attrKey);
    }

    public WebResult attr(String attrKey, Object attrValue) {
        attrs.put(attrKey, attrValue);
        return this;
    }

    public WebResult withContentType() {
        withContentType = true;
        return this;
    }

    public WebResult keepNullValue() {
        keepNullValue = true;
        return this;
    }

    public WebResult quoteFieldNames() {
        quoteFieldNames = true;
        return this;
    }

    public WebResult useSingleQuotes() {
        useSingleQuotes = true;
        return this;
    }

    private Map<String, Object> doFilter(IDateFilter dateFilter, boolean attr, Map<String, Object> targetMap) {
        if (dateFilter != null && targetMap != null && !targetMap.isEmpty()) {
            Map<String, Object> filtered = new LinkedHashMap<>(data.size());
            data.forEach((key, value) -> {
                Object item = dateFilter.filter(attr, key, value);
                if (item != null) {
                    filtered.put(key, value);
                }
            });
            return filtered;
        }
        return targetMap;
    }

    public WebResult dataFilter(IDateFilter dateFilter) {
        data = doFilter(dateFilter, true, data);
        attrs = doFilter(dateFilter, false, attrs);
        return this;
    }

    public JSONObject toJSONObject() {
        JSONObject jsonObj = new JSONObject(true);
        if (code != null) {
            jsonObj.put(Type.Const.PARAM_RET, code);
        }
        if (StringUtils.isNotBlank(msg)) {
            jsonObj.put(Type.Const.PARAM_MSG, msg);
        }
        if (data != null && !data.isEmpty()) {
            jsonObj.put(Type.Const.PARAM_DATA, data);
        }
        if (attrs != null && !attrs.isEmpty()) {
            jsonObj.putAll(attrs);
        }
        return jsonObj;
    }

    public JsonView toJsonView() {
        return toJsonView(null);
    }

    public JsonView toJsonView(String callback) {
        JsonView jsonView = new JsonView(toJSONObject()).withJsonCallback(callback);
        if (quoteFieldNames) {
            jsonView.quoteFieldNames();
            if (useSingleQuotes) {
                jsonView.useSingleQuotes();
            }
        }
        if (keepNullValue) {
            jsonView.keepNullValue();
        }
        if (withContentType) {
            jsonView.withContentType();
        }
        return jsonView;
    }

    public String toXml(boolean cdata) {
        StringBuilder content = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xml>")
                .append("<ret>").append(code).append("</ret>");
        if (StringUtils.isNotBlank(msg)) {
            if (cdata) {
                content.append("<msg><![CDATA[").append(msg).append("]]></msg>");
            } else {
                content.append("<msg>").append(msg).append("</msg>");
            }
        }
        if (data != null && !data.isEmpty()) {
            content.append("<data>");
            data.forEach((key, value) -> doContentAppend(content, cdata, key, value));
            content.append("</data>");
        }
        if (attrs != null && !attrs.isEmpty()) {
            attrs.forEach((key, value) -> doContentAppend(content, cdata, key, value));
        }
        content.append("</xml>");
        return content.toString();
    }

    public TextView toXmlView() {
        return toXmlView(true);
    }

    public TextView toXmlView(boolean cdata) {
        TextView textView = View.textView(toXml(cdata));
        if (withContentType) {
            textView.setContentType(Type.ContentType.XML.getContentType());
        }
        return textView;
    }

    @SuppressWarnings("unchecked")
    private void doContentAppend(StringBuilder content, boolean cdata, String key, Object value) {
        if (value != null) {
            content.append("<").append(key).append(">");
            if (value instanceof Number || int.class.isAssignableFrom(value.getClass()) || long.class.isAssignableFrom(value.getClass()) || float.class.isAssignableFrom(value.getClass()) || double.class.isAssignableFrom(value.getClass())) {
                content.append(value);
            } else if (value instanceof Map) {
                ((Map<String, Object>) value).forEach((key1, value1) -> doContentAppend(content, cdata, key1, value1));
            } else if (value instanceof Collection) {
                ((Collection<?>) value).forEach((item) -> doContentAppend(content, cdata, "item", item));
            } else if (value instanceof Boolean || value instanceof String || boolean.class.isAssignableFrom(value.getClass())) {
                if (cdata) {
                    content.append("<![CDATA[").append(value).append("]]>");
                } else {
                    content.append(value);
                }
            } else {
                ClassUtils.wrapper(value).toMap().forEach((key1, value1) -> doContentAppend(content, cdata, key1, value1));
            }
            content.append("</").append(key).append(">");
        }
    }

    public static IView formatView(WebResult result) {
        return formatView(null, Type.Const.PARAM_FORMAT, Type.Const.PARAM_CALLBACK, result);
    }

    public static IView formatView(String path, WebResult result) {
        return formatView(path, Type.Const.PARAM_FORMAT, Type.Const.PARAM_CALLBACK, result);
    }

    public static IView formatView(WebResult result, String defaultFormat) {
        return formatView(null, Type.Const.PARAM_FORMAT, defaultFormat, Type.Const.PARAM_CALLBACK, result);
    }

    /**
     * @param path          JSP模块路径
     * @param paramFormat   数据格式，可选值：json|jsonp|xml
     * @param paramCallback 当数据结式为jsonp时，指定回调方法参数名
     * @param result        回应的数据对象
     * @return 根据paramFormat等参数判断返回对应的视图对象
     */
    public static IView formatView(String path, String paramFormat, String paramCallback, WebResult result) {
        return formatView(path, paramFormat, null, paramCallback, result);
    }

    public static IView formatView(String path, String paramFormat, String defaultFormat, String paramCallback, WebResult result) {
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

    /**
     * 数据过滤器接口
     */
    public interface IDateFilter {

        /**
         * 执行数据过滤
         *
         * @param dataAttr  当前数据是否为data属性
         * @param itemName  属性名称
         * @param itemValue 属性值对象
         * @return 若返回null则该属性将被忽略
         */
        Object filter(boolean dataAttr, String itemName, Object itemValue);
    }
}
