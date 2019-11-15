/*
 * Copyright 2007-2016 the original author or authors.
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
import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.IWebMvcModuleCfg;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;
import net.ymate.platform.webmvc.view.impl.HttpStatusView;
import net.ymate.platform.webmvc.view.impl.JsonView;
import net.ymate.platform.webmvc.view.impl.JspView;
import net.ymate.platform.webmvc.view.impl.TextView;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/8/18 下午2:18
 * @version 1.0
 * @since 2.0.6
 */
public final class WebResult {

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
        String _msg = null;
        if (StringUtils.isNotBlank(errorCode.getI18nKey())) {
            _msg = WebUtils.i18nStr(owner, resourceName, errorCode.getI18nKey(), null);
        }
        if (StringUtils.isBlank(_msg)) {
            _msg = WebUtils.errorCodeI18n(owner, resourceName, errorCode.getCode(), errorCode.getMessage());
        }
        WebResult _result = new WebResult(errorCode.getCode()).msg(_msg);
        if (!errorCode.getAttributes().isEmpty()) {
            _result.attrs(errorCode.getAttributes());
        }
        return _result;
    }

    public static WebResult succeed() {
        return new WebResult(ErrorCode.SUCCEED);
    }

    private Integer __code;

    private String __msg;

    private Map<String, Object> __data = new LinkedHashMap<String, Object>();

    private Map<String, Object> __attrs = new LinkedHashMap<String, Object>();

    private boolean __withContentType;

    private boolean __keepNullValue;

    private boolean __quoteFieldNames;

    private boolean __useSingleQuotes;

    private IDateFilter __dataFilter;

    private WebResult() {
    }

    private WebResult(int code) {
        __code = code;
    }

    public int code() {
        return __code;
    }

    public WebResult code(Integer code) {
        __code = code;
        return this;
    }

    public String msg() {
        return StringUtils.trimToEmpty(__msg);
    }

    public WebResult msg(String msg) {
        __msg = msg;
        return this;
    }

    public WebResult data(Object data) {
        __attrs.put(Type.Const.PARAM_DATA, data);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T data() {
        return (T) __attrs.get(Type.Const.PARAM_DATA);
    }

    public WebResult attrs(Map<String, Object> attrs) {
        __attrs = attrs;
        return this;
    }

    public Map<String, Object> attrs() {
        return __attrs;
    }

    @SuppressWarnings("unchecked")
    public <T> T dataAttr(String dataKey) {
        return (T) __data.get(dataKey);
    }

    public WebResult dataAttr(String dataKey, Object dataValue) {
        __data.put(dataKey, dataValue);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T attr(String attrKey) {
        return (T) __attrs.get(attrKey);
    }

    public WebResult attr(String attrKey, Object attrValue) {
        __attrs.put(attrKey, attrValue);
        return this;
    }

    public WebResult dataFilter(IDateFilter dateFilter) {
        __dataFilter = dateFilter;
        return this;
    }

    public WebResult withContentType() {
        __withContentType = true;
        return this;
    }

    public WebResult keepNullValue() {
        __keepNullValue = true;
        return this;
    }

    public WebResult quoteFieldNames() {
        __quoteFieldNames = true;
        return this;
    }

    public WebResult useSingleQuotes() {
        __useSingleQuotes = true;
        return this;
    }

    private Map<String, Object> __doFilter(boolean attr, Map<String, Object> targetMap) {
        if (__dataFilter != null && targetMap != null && !targetMap.isEmpty()) {
            Map<String, Object> _filtered = new LinkedHashMap<String, Object>();
            for (Map.Entry<String, Object> _entry : __data.entrySet()) {
                Object _item = __dataFilter.filter(attr, _entry.getKey(), _entry.getValue());
                if (_item != null) {
                    _filtered.put(_entry.getKey(), _entry.getValue());
                }
            }
            return _filtered;
        }
        return targetMap;
    }

    public WebResult doFilter() {
        __data = __doFilter(true, __data);
        __attrs = __doFilter(false, __attrs);
        return this;
    }

    public IView toJSON() {
        return toJSON(null);
    }

    public IView toJSON(String callback) {
        JSONObject _jsonObj = new JSONObject(true);
        if (__code != null) {
            _jsonObj.put(Type.Const.PARAM_RET, __code);
        }
        if (StringUtils.isNotBlank(__msg)) {
            _jsonObj.put(Type.Const.PARAM_MSG, __msg);
        }
        if (__data != null && !__data.isEmpty()) {
            _jsonObj.put(Type.Const.PARAM_DATA, __data);
        }
        if (__attrs != null && !__attrs.isEmpty()) {
            _jsonObj.putAll(__attrs);
        }
        //
        JsonView _view = new JsonView(_jsonObj).withJsonCallback(callback);
        if (__quoteFieldNames) {
            _view.quoteFieldNames();
            if (__useSingleQuotes) {
                _view.useSingleQuotes();
            }
        }
        if (__keepNullValue) {
            _view.keepNullValue();
        }
        if (__withContentType) {
            _view.withContentType();
        }
        return _view;
    }

    public IView toXML(boolean cdata) {
        StringBuilder _content = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        _content.append("<xml><ret>").append(__code).append("</ret>");
        if (StringUtils.isNotBlank(__msg)) {
            if (cdata) {
                _content.append("<msg><![CDATA[").append(__msg).append("]]></msg>");
            } else {
                _content.append("<msg>").append(__msg).append("</msg>");
            }
        }
        if (__data != null && !__data.isEmpty()) {
            _content.append("<data>");
            for (Map.Entry<String, Object> _entry : __data.entrySet()) {
                __doAppendContent(_content, cdata, _entry.getKey(), _entry.getValue());
            }
            _content.append("</data>");
        }
        if (__attrs != null && !__attrs.isEmpty()) {
            for (Map.Entry<String, Object> _entry : __attrs.entrySet()) {
                __doAppendContent(_content, cdata, _entry.getKey(), _entry.getValue());
            }
        }
        _content.append("</xml>");
        TextView _view = View.textView(_content.toString());
        if (__withContentType) {
            _view.setContentType("application/xml");
        }
        return _view;
    }

    @SuppressWarnings("unchecked")
    private void __doAppendContent(StringBuilder content, boolean cdata, String key, Object value) {
        if (value != null) {
            content.append("<").append(key).append(">");
            if (value instanceof Number || int.class.isAssignableFrom(value.getClass()) || long.class.isAssignableFrom(value.getClass()) || float.class.isAssignableFrom(value.getClass()) || double.class.isAssignableFrom(value.getClass())) {
                content.append(value);
            } else if (value instanceof Map) {
                Map<String, Object> _map = (Map<String, Object>) value;
                if (!_map.isEmpty()) {
                    for (Map.Entry<String, Object> _entry : _map.entrySet()) {
                        __doAppendContent(content, cdata, _entry.getKey(), _entry.getValue());
                    }
                }
            } else if (value instanceof Collection) {
                Collection _list = (Collection) value;
                if (!_list.isEmpty()) {
                    for (Object _item : _list) {
                        __doAppendContent(content, cdata, "item", _item);
                    }
                }
            } else if (value instanceof Boolean || value instanceof String || boolean.class.isAssignableFrom(value.getClass())) {
                if (cdata) {
                    content.append("<![CDATA[").append(value).append("]]>");
                } else {
                    content.append(value);
                }
            } else {
                Map<String, Object> _map = ClassUtils.wrapper(value).toMap();
                if (!_map.isEmpty()) {
                    for (Map.Entry<String, Object> _entry : _map.entrySet()) {
                        __doAppendContent(content, cdata, _entry.getKey(), _entry.getValue());
                    }
                }
            }
            content.append("</").append(key).append(">");
        }
    }

    public IView toXML() {
        return toXML(false);
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
        IView _view = null;
        String _format = StringUtils.defaultIfBlank(WebContext.getRequest().getParameter(paramFormat), StringUtils.trimToNull(defaultFormat));
        if (_format != null && result != null) {
            if (BlurObject.bind(WebContext.getContext().getOwner().getOwner().getConfig().getParam(IWebMvcModuleCfg.PARAMS_ERROR_WITH_CONTENT_TYPE)).toBooleanValue()) {
                result.withContentType();
            }
            if (Type.Const.FORMAT_JSON.equalsIgnoreCase(_format)) {
                _view = result.toJSON(StringUtils.trimToNull(WebContext.getRequest().getParameter(paramCallback)));
            } else if (Type.Const.FORMAT_XML.equalsIgnoreCase(_format)) {
                _view = result.toXML(true);
            }
        }
        if (_view == null) {
            if (StringUtils.isNotBlank(path)) {
                _view = new JspView(path);
                if (result != null) {
                    _view.addAttribute(Type.Const.PARAM_RET, result.code());
                    //
                    if (StringUtils.isNotBlank(result.msg())) {
                        _view.addAttribute(Type.Const.PARAM_MSG, result.msg());
                    }
                    if (result.data() != null) {
                        _view.addAttribute(Type.Const.PARAM_DATA, result.data());
                    }
                    for (Map.Entry<String, Object> _entry : result.attrs().entrySet()) {
                        _view.addAttribute(_entry.getKey(), _entry.getValue());
                    }
                }
            } else {
                if (result != null && StringUtils.isNotBlank(result.msg())) {
                    _view = new HttpStatusView(HttpServletResponse.SC_BAD_REQUEST, result.msg());
                } else {
                    _view = new HttpStatusView(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }
        return _view;
    }

    /**
     * 数据过滤器接口
     */
    public interface IDateFilter {

        /**
         * @param dataAttr  当前数据是否为data属性
         * @param itemName  属性名称
         * @param itemValue 属性值对象
         * @return 若返回null则该属性将被忽略
         */
        Object filter(boolean dataAttr, String itemName, Object itemValue);
    }
}
