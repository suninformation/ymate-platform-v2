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
package net.ymate.platform.webmvc.impl;

import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认WebMVC请求上下文接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-22 上午2:17:10
 */
public class DefaultRequestContext implements IRequestContext {

    /**
     * 原始URL
     */
    private final String originalUrl;

    /**
     * 控制器请求映射
     */
    private String requestMapping;

    /**
     * 前缀
     */
    private String prefix;

    /**
     * 后缀名称
     */
    private final String suffix;

    private final Type.HttpMethod httpMethod;

    private final Map<String, Object> attributes = new HashMap<>(16);

    public DefaultRequestContext(HttpServletRequest request, String prefix) {
        httpMethod = Type.HttpMethod.valueOf(request.getMethod());
        requestMapping = originalUrl = StringUtils.defaultIfBlank(request.getPathInfo(), request.getServletPath());
        if (StringUtils.isNotBlank(prefix)) {
            requestMapping = StringUtils.substringAfter(requestMapping, prefix);
            this.prefix = prefix;
        }
        int position = 0;
        if (!requestMapping.endsWith(Type.Const.PATH_SEPARATOR)) {
            position = requestMapping.lastIndexOf('.');
            if (position < requestMapping.lastIndexOf(Type.Const.PATH_SEPARATOR_CHAR)) {
                position = -1;
            }
        }
        if (position > 0) {
            this.suffix = requestMapping.substring(position + 1);
            requestMapping = requestMapping.substring(0, position);
        } else {
            this.suffix = StringUtils.EMPTY;
        }
    }

    @Override
    public String getRequestMapping() {
        return requestMapping;
    }

    @Override
    public String getOriginalUrl() {
        return originalUrl;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public String getSuffix() {
        return suffix;
    }

    @Override
    public Type.HttpMethod getHttpMethod() {
        return httpMethod;
    }

    ////

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) attributes.get(name);
    }

    @Override
    public IRequestContext addAttribute(String name, Object value) {
        attributes.put(name, value);
        return this;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
