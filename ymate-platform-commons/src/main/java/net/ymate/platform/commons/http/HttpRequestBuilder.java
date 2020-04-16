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
package net.ymate.platform.commons.http;

import net.ymate.platform.commons.http.impl.DefaultHttpRequest;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/04/12 13:56
 * @since 2.1.0
 */
public final class HttpRequestBuilder {

    public static HttpRequestBuilder create(String url) {
        return new HttpRequestBuilder(url);
    }

    private final String url;

    private final List<Header> headers = new ArrayList<>();

    private final List<NameValuePair> params = new ArrayList<>();

    private final Map<String, ContentBody> contents = new HashMap<>();

    private Object content;

    private Charset charset;

    private Charset responseCharset;

    private ContentType contentType;

    private boolean download;

    private IFileHandler fileHandler;

    private int connectionTimeout = -1;

    private int requestTimeout = -1;

    private int socketTimeout = -1;

    private SSLConnectionSocketFactory socketFactory;

    private IHttpClientConfigurable configurable;

    public HttpRequestBuilder(String url) {
        if (StringUtils.isBlank(url)) {
            throw new NullArgumentException("url");
        }
        this.url = url;
    }

    public HttpRequestBuilder addHeader(String name, String value) {
        return addHeader(new BasicHeader(name, value));
    }

    public HttpRequestBuilder addHeader(Header header) {
        if (header != null) {
            headers.add(header);
        }
        return this;
    }

    public HttpRequestBuilder addHeaders(Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(this::addHeader);
        }
        return this;
    }

    public HttpRequestBuilder addParam(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
        return this;
    }

    public HttpRequestBuilder addParam(NameValuePair param) {
        if (param != null) {
            params.add(param);
        }
        return this;
    }

    public HttpRequestBuilder addParams(Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            params.forEach(this::addParam);
        }
        return this;
    }

    public HttpRequestBuilder addBody(String fieldName, ContentBody contentBody) {
        if (StringUtils.isBlank(fieldName)) {
            throw new NullArgumentException("fieldName");
        }
        if (contentBody != null) {
            contents.put(fieldName, contentBody);
        }
        return this;
    }

    public HttpRequestBuilder addContent(String fieldName, File file) {
        if (file != null) {
            addBody(fieldName, new FileBody(file));
        }
        return this;
    }

    public HttpRequestBuilder addContent(String fieldName, InputStream inputStream, String fileName) {
        if (inputStream != null) {
            addBody(fieldName, new InputStreamBody(inputStream, fileName));
        }
        return this;
    }

    public HttpRequestBuilder addContent(String fieldName, InputStream inputStream, ContentType contentType, String fileName) {
        if (inputStream != null) {
            addBody(fieldName, new InputStreamBody(inputStream, contentType, fileName));
        }
        return this;
    }

    public HttpRequestBuilder addContent(String fieldName, String body, ContentType contentType) {
        if (body != null && contentType != null) {
            addBody(fieldName, new StringBody(body, contentType));
        }
        return this;
    }

    public HttpRequestBuilder content(String content) {
        this.content = content;
        return this;
    }

    public HttpRequestBuilder content(byte[] content) {
        this.content = content;
        return this;
    }

    public HttpRequestBuilder content(InputStream content) {
        this.content = content;
        return this;
    }

    public HttpRequestBuilder content(File content) {
        this.content = content;
        return this;
    }

    public HttpRequestBuilder charset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public HttpRequestBuilder charset(String charset) {
        this.charset = Charset.forName(charset);
        return this;
    }

    public HttpRequestBuilder responseCharset(Charset responseCharset) {
        this.responseCharset = responseCharset;
        return this;
    }

    public HttpRequestBuilder responseCharset(String responseCharset) {
        this.responseCharset = Charset.forName(responseCharset);
        return this;
    }

    public HttpRequestBuilder contentType(String mimeType, String charset) {
        if (StringUtils.isNotBlank(mimeType)) {
            this.contentType = ContentType.create(mimeType, StringUtils.defaultIfBlank(charset, this.charset != null ? this.charset.name() : null));
        }
        return this;
    }

    public HttpRequestBuilder contentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public HttpRequestBuilder download(boolean download) {
        this.download = download;
        return this;
    }

    public HttpRequestBuilder download(IFileHandler fileHandler) {
        this.download = true;
        this.fileHandler = fileHandler;
        return this;
    }

    public HttpRequestBuilder connectionTimeout(int connectionTimeout) {
        if (connectionTimeout > -1) {
            this.connectionTimeout = connectionTimeout;
        }
        return this;
    }

    public HttpRequestBuilder requestTimeout(int requestTimeout) {
        if (requestTimeout > -1) {
            this.requestTimeout = requestTimeout;
        }
        return this;
    }

    public HttpRequestBuilder socketTimeout(int socketTimeout) {
        if (socketTimeout > -1) {
            this.socketTimeout = socketTimeout;
        }
        return this;
    }

    public HttpRequestBuilder socketFactory(SSLConnectionSocketFactory socketFactory) {
        this.socketFactory = socketFactory;
        return this;
    }

    public HttpRequestBuilder configurable(IHttpClientConfigurable httpClientConfigurable) {
        this.configurable = httpClientConfigurable;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public List<NameValuePair> getParams() {
        return params;
    }

    public Map<String, ContentBody> getContents() {
        return contents;
    }

    public Object getContent() {
        return content;
    }

    public Charset getCharset() {
        return charset;
    }

    public Charset getResponseCharset() {
        return responseCharset;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public boolean isDownload() {
        return download;
    }

    public IFileHandler getFileHandler() {
        return fileHandler;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public SSLConnectionSocketFactory getSocketFactory() {
        return socketFactory;
    }

    public IHttpClientConfigurable getConfigurable() {
        return configurable;
    }

    public IHttpRequest build() {
        return new DefaultHttpRequest(this);
    }
}
