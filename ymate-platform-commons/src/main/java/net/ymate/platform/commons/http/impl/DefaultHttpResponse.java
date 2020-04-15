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
/*
 * Copyright (c) 2007-2020, the original author or authors. All rights reserved.
 *
 * This program licensed under the terms of the GNU Lesser General Public License version 3.0
 * as published by the Free Software Foundation.
 */
package net.ymate.platform.commons.http.impl;

import net.ymate.platform.commons.http.HttpClientHelper;
import net.ymate.platform.commons.http.IFileWrapper;
import net.ymate.platform.commons.http.IHttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * HTTP请求响应接口默认实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/9/7 下午11:20
 */
public class DefaultHttpResponse implements IHttpResponse {

    private final int statusCode;

    private final String reasonPhrase;

    private final Locale locale;

    private String content;

    private IFileWrapper fileWrapper;

    private String contentType;

    private String contentEncoding;

    private final long contentLength;

    private final Map<String, String> headers = new HashMap<>();

    public DefaultHttpResponse(HttpResponse response) throws IOException {
        this(response, HttpClientHelper.DEFAULT_CHARSET);
    }

    public DefaultHttpResponse(HttpResponse response, Charset defaultCharset) throws IOException {
        this(response, defaultCharset != null ? defaultCharset.name() : null);
    }

    public DefaultHttpResponse(HttpResponse response, String defaultCharset) throws IOException {
        this(response, defaultCharset, false);
    }

    public DefaultHttpResponse(HttpResponse response, Charset defaultCharset, boolean download) throws IOException {
        this(response, defaultCharset != null ? defaultCharset.name() : null, download);
    }

    public DefaultHttpResponse(HttpResponse response, String defaultCharset, boolean download) throws IOException {
        statusCode = response.getStatusLine().getStatusCode();
        reasonPhrase = response.getStatusLine().getReasonPhrase();
        locale = response.getLocale();
        //
        if (download && response.getStatusLine().getStatusCode() == HttpClientHelper.HTTP_STATUS_CODE_SUCCESS) {
            String fileName = null;
            if (response.containsHeader(HttpClientHelper.HEADER_CONTENT_DISPOSITION)) {
                fileName = StringUtils.substringAfter(response.getFirstHeader(HttpClientHelper.HEADER_CONTENT_DISPOSITION).getValue(), "filename=");
            }
            fileWrapper = new DefaultFileWrapper(fileName, response.getEntity().getContentType().getValue(), response.getEntity().getContentLength(), new BufferedInputStream(response.getEntity().getContent()));
        } else {
            content = EntityUtils.toString(response.getEntity(), defaultCharset);
        }
        //
        Header header = response.getEntity().getContentEncoding();
        if (header != null) {
            contentEncoding = header.getValue();
        }
        if (contentEncoding == null) {
            contentEncoding = StringUtils.defaultIfBlank(defaultCharset, HttpClientHelper.DEFAULT_CHARSET);
        }
        header = response.getEntity().getContentType();
        if (header != null) {
            contentType = header.getValue();
        }
        contentLength = response.getEntity().getContentLength();
        Header[] headersArr = response.getAllHeaders();
        if (headersArr != null) {
            Arrays.stream(headersArr).forEachOrdered(element -> headers.put(element.getName(), element.getValue()));
        }
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public String getReasonPhrase() {
        return reasonPhrase;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public IFileWrapper getFileWrapper() {
        return fileWrapper;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public long getContentLength() {
        return contentLength;
    }

    @Override
    public String getContentEncoding() {
        return contentEncoding;
    }

    @Override
    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    @Override
    public String toString() {
        return String.format("{statusCode=%d, content='%s', contentType='%s', contentEncoding='%s', contentLength=%d, headers=%s}", statusCode, content, contentType, contentEncoding, contentLength, headers);
    }
}
