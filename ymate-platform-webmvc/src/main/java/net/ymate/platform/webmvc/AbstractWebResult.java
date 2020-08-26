/*
 * Copyright 2007-2020 the original author or authors.
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
package net.ymate.platform.webmvc;

import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.view.View;
import net.ymate.platform.webmvc.view.impl.JsonView;
import net.ymate.platform.webmvc.view.impl.TextView;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/08/26 09:59
 * @since 2.1.0
 */
public abstract class AbstractWebResult<CODE_TYPE extends Serializable> implements IWebResult<CODE_TYPE>, Serializable {

    private CODE_TYPE code;

    private String msg;

    private Map<String, Object> data = new LinkedHashMap<>();

    private Map<String, Object> attrs = new LinkedHashMap<>();

    private boolean withContentType;

    private boolean keepNullValue;

    public AbstractWebResult() {
    }

    public AbstractWebResult(CODE_TYPE code) {
        this.code = code;
    }

    @Override
    public CODE_TYPE code() {
        return code;
    }

    @Override
    public IWebResult<CODE_TYPE> code(CODE_TYPE code) {
        this.code = code;
        return this;
    }

    @Override
    public String msg() {
        return StringUtils.trimToEmpty(msg);
    }

    @Override
    public IWebResult<CODE_TYPE> msg(String msg) {
        this.msg = msg;
        return this;
    }

    @Override
    public IWebResult<CODE_TYPE> data(Object data) {
        if (data != null) {
            attrs.put(Type.Const.PARAM_DATA, data);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T data() {
        return (T) attrs.get(Type.Const.PARAM_DATA);
    }

    @Override
    public IWebResult<CODE_TYPE> attrs(Map<String, Object> attrs) {
        this.attrs.putAll(attrs);
        return this;
    }

    @Override
    public Map<String, Object> attrs() {
        return attrs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T dataAttr(String dataKey) {
        return (T) data.get(dataKey);
    }

    @Override
    public IWebResult<CODE_TYPE> dataAttr(String dataKey, Object dataValue) {
        data.put(dataKey, dataValue);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T attr(String attrKey) {
        return (T) attrs.get(attrKey);
    }

    @Override
    public IWebResult<CODE_TYPE> attr(String attrKey, Object attrValue) {
        attrs.put(attrKey, attrValue);
        return this;
    }

    @Override
    public IWebResult<CODE_TYPE> withContentType() {
        withContentType = true;
        return this;
    }

    @Override
    public IWebResult<CODE_TYPE> keepNullValue() {
        keepNullValue = true;
        return this;
    }

    protected Map<String, Object> doFilter(IDateFilter dateFilter, boolean attr, Map<String, Object> targetMap) {
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

    @Override
    public IWebResult<CODE_TYPE> dataFilter(IDateFilter dateFilter) {
        data = doFilter(dateFilter, true, data);
        attrs = doFilter(dateFilter, false, attrs);
        return this;
    }

    @Override
    public IJsonObjectWrapper toJsonObject() {
        IJsonObjectWrapper jsonObj = JsonWrapper.createJsonObject(true);
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
            attrs.forEach(jsonObj::put);
        }
        return jsonObj;
    }

    @Override
    public JsonView toJsonView() {
        return toJsonView(null);
    }

    @Override
    public JsonView toJsonView(String callback) {
        JsonView jsonView = new JsonView(toJsonObject()).withJsonCallback(callback);
        if (keepNullValue) {
            jsonView.keepNullValue();
        }
        if (withContentType) {
            jsonView.withContentType();
        }
        return jsonView;
    }

    @Override
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

    @Override
    public TextView toXmlView() {
        return toXmlView(true);
    }

    @Override
    public TextView toXmlView(boolean cdata) {
        TextView textView = View.textView(toXml(cdata));
        if (withContentType) {
            textView.setContentType(Type.ContentType.XML.getContentType());
        }
        return textView;
    }

    @SuppressWarnings("unchecked")
    protected void doContentAppend(StringBuilder content, boolean cdata, String key, Object value) {
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
}
