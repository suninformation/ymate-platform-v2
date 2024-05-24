/*
 * Copyright 2007-2024 the original author or authors.
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

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2024/5/13 03:05
 * @since 2.1.3
 */
public abstract class AbstractHttpRequestBuilder<T extends AbstractHttpRequestBuilder<?>> {

    private final String url;

    private final List<Header> headers = new ArrayList<>();

    private final List<NameValuePair> params = new ArrayList<>();

    private final Map<String, ContentBody> contents = new HashMap<>();

    private Object content;

    private Charset charset;

    private Charset responseCharset;

    private ContentType contentType;

    private RequestConfig.Builder requestConfig;

    private boolean download;

    private IFileHandler fileHandler;

    private int connectionTimeout = -1;

    private int requestTimeout = -1;

    private int socketTimeout = -1;

    private SSLConnectionSocketFactory socketFactory;

    protected AbstractHttpRequestBuilder(String url) {
        if (StringUtils.isBlank(url)) {
            throw new NullArgumentException("url");
        }
        this.url = url;
        this.charset = StandardCharsets.UTF_8;
    }

    public T addHeader(String name, String value) {
        return addHeader(new BasicHeader(name, value));
    }

    @SuppressWarnings("unchecked")
    public T addHeader(Header header) {
        if (header != null) {
            headers.add(header);
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addHeaders(Header[] headers) {
        if (headers != null) {
            Arrays.stream(headers).forEach(this::addHeader);
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addHeaders(Map<String, String> headers) {
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(this::addHeader);
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addParam(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addParam(NameValuePair param) {
        if (param != null) {
            params.add(param);
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addParams(Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            params.forEach(this::addParam);
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addBody(String fieldName, ContentBody contentBody) {
        if (StringUtils.isBlank(fieldName)) {
            throw new NullArgumentException("fieldName");
        }
        if (contentBody != null) {
            contents.put(fieldName, contentBody);
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addContent(String fieldName, File file) {
        if (file != null) {
            addBody(fieldName, new FileBody(file));
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addContent(String fieldName, InputStream inputStream, String fileName) {
        if (inputStream != null) {
            addBody(fieldName, new InputStreamBody(inputStream, fileName));
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addContent(String fieldName, InputStream inputStream, ContentType contentType, String fileName) {
        if (inputStream != null) {
            addBody(fieldName, new InputStreamBody(inputStream, contentType, fileName));
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addContent(String fieldName, String body, ContentType contentType) {
        if (body != null && contentType != null) {
            addBody(fieldName, new StringBody(body, contentType));
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T content(String content) {
        this.content = content;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T content(byte[] content) {
        this.content = content;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T content(InputStream content) {
        this.content = content;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T content(File content) {
        this.content = content;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T charset(Charset charset) {
        this.charset = charset;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T charset(String charset) {
        this.charset = Charset.forName(charset);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T responseCharset(Charset responseCharset) {
        this.responseCharset = responseCharset;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T responseCharset(String responseCharset) {
        this.responseCharset = Charset.forName(responseCharset);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T contentType(String mimeType, String charset) {
        if (StringUtils.isNotBlank(mimeType)) {
            this.contentType = ContentType.create(mimeType, StringUtils.defaultIfBlank(charset, this.charset != null ? this.charset.name() : null));
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T contentType(ContentType contentType) {
        this.contentType = contentType;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T requestConfig(RequestConfig.Builder requestConfig) {
        this.requestConfig = requestConfig;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T download(boolean download) {
        this.download = download;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T download(IFileHandler fileHandler) {
        this.download = true;
        this.fileHandler = fileHandler;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T connectionTimeout(int connectionTimeout) {
        if (connectionTimeout > -1) {
            this.connectionTimeout = connectionTimeout;
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T requestTimeout(int requestTimeout) {
        if (requestTimeout > -1) {
            this.requestTimeout = requestTimeout;
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T socketTimeout(int socketTimeout) {
        if (socketTimeout > -1) {
            this.socketTimeout = socketTimeout;
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T socketFactory(SSLConnectionSocketFactory socketFactory) {
        this.socketFactory = socketFactory;
        return (T) this;
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

    public RequestConfig.Builder getRequestConfig() {
        return requestConfig;
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

    public abstract IHttpRequest build();
}
