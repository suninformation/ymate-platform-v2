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

import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.webmvc.base.Type;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author 刘镇 (suninformation@163.com) on 16/3/10 下午10:20
 */
public class GenericResponseWrapper extends HttpServletResponseWrapper {

    private int contentLength;

    private final Map<String, PairObject<Type.HeaderType, Object>> headersMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    public GenericResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void reset() {
        super.reset();
        contentLength = 0;
        headersMap.clear();
    }

    @Override
    public void setContentLength(final int length) {
        this.contentLength = length;
        super.setContentLength(length);
    }

    public int getContentLength() {
        return contentLength;
    }

    @Override
    public void addHeader(String name, String value) {
        this.headersMap.computeIfAbsent(name, k -> PairObject.bind(Type.HeaderType.STRING)).setValue(value);
        super.addHeader(name, value);
    }

    @Override
    public void setHeader(String name, String value) {
        this.headersMap.put(name, PairObject.bind(Type.HeaderType.STRING, value));
        super.setHeader(name, value);
    }

    @Override
    public void addDateHeader(String name, long date) {
        this.headersMap.computeIfAbsent(name, k -> PairObject.bind(Type.HeaderType.DATE)).setValue(date);
        super.addDateHeader(name, date);
    }

    @Override
    public void setDateHeader(String name, long date) {
        this.headersMap.put(name, PairObject.bind(Type.HeaderType.DATE, date));
        super.setDateHeader(name, date);
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.headersMap.computeIfAbsent(name, k -> PairObject.bind(Type.HeaderType.INT)).setValue(value);
        super.addIntHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.headersMap.put(name, PairObject.bind(Type.HeaderType.INT, value));
        super.setIntHeader(name, value);
    }

    public Map<String, PairObject<Type.HeaderType, Object>> getHeaders() {
        return headersMap;
    }
}
