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
package net.ymate.platform.commons.http.impl;

import net.ymate.platform.commons.http.HttpClientHelper;
import net.ymate.platform.commons.http.HttpRequestBuilder;
import net.ymate.platform.commons.http.IHttpRequest;
import net.ymate.platform.commons.http.IHttpResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.File;
import java.io.InputStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/04/12 17:48
 * @since 2.1.0
 */
public class DefaultHttpRequest implements IHttpRequest {

    private final HttpRequestBuilder httpRequestBuilder;

    public DefaultHttpRequest(HttpRequestBuilder httpRequestBuilder) {
        this.httpRequestBuilder = httpRequestBuilder;
    }

    private RequestBuilder doGetRequestBuilder(RequestBuilder requestBuilder) {
        httpRequestBuilder.getHeaders().forEach(requestBuilder::addHeader);
        httpRequestBuilder.getParams().forEach(requestBuilder::addParameter);
        requestBuilder.setUri(httpRequestBuilder.getUrl());
        if (!StringUtils.equalsIgnoreCase(requestBuilder.getMethod(), HttpGet.METHOD_NAME)) {
            if (!httpRequestBuilder.getContents().isEmpty()) {
                MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
                httpRequestBuilder.getContents().forEach((key, value) -> multipartEntityBuilder.addPart(FormBodyPartBuilder.create(key, value).build()));
                requestBuilder.setEntity(multipartEntityBuilder.build());
            } else if (httpRequestBuilder.getContent() != null) {
                Object content = httpRequestBuilder.getContent();
                ContentType contentType = httpRequestBuilder.getContentType();
                if (content instanceof String) {
                    if (contentType == null) {
                        contentType = ContentType.create(HttpClientHelper.CONTENT_TYPE_TEXT_PLAIN, HttpClientHelper.DEFAULT_CHARSET);
                    }
                    requestBuilder.setEntity(EntityBuilder.create()
                            .setContentEncoding(contentType.getCharset().name())
                            .setContentType(contentType)
                            .setText((String) content).build());
                } else {
                    if (contentType == null) {
                        contentType = ContentType.create(HttpClientHelper.CONTENT_TYPE_OCTET_STREAM, HttpClientHelper.DEFAULT_CHARSET);
                    }
                    if (content instanceof byte[]) {
                        requestBuilder.setEntity(EntityBuilder.create()
                                .setContentEncoding(contentType.getCharset().name())
                                .setContentType(contentType)
                                .setBinary((byte[]) content).build());
                    } else if (content instanceof InputStream) {
                        requestBuilder.setEntity(EntityBuilder.create()
                                .setContentEncoding(contentType.getCharset().name())
                                .setContentType(contentType)
                                .setStream((InputStream) content).build());
                    } else if (content instanceof File) {
                        requestBuilder.setEntity(EntityBuilder.create()
                                .setContentEncoding(contentType.getCharset().name())
                                .setContentType(contentType)
                                .setFile((File) content).build());
                    }
                }
            }
        }
        return requestBuilder;
    }

    private IHttpResponse doExecute(RequestBuilder requestBuilder) throws Exception {
        try (CloseableHttpClient httpClient = HttpClientHelper.create(httpRequestBuilder.getConfigurable())
                .customSSL(httpRequestBuilder.getSocketFactory())
                .connectionTimeout(httpRequestBuilder.getConnectionTimeout())
                .requestTimeout(httpRequestBuilder.getRequestTimeout())
                .socketTimeout(httpRequestBuilder.getSocketTimeout())
                .getHttpClient()) {
            return httpClient.execute(requestBuilder.build(), (ResponseHandler<IHttpResponse>) response -> new DefaultHttpResponse(response, httpRequestBuilder.getResponseCharset(), httpRequestBuilder.isDownload(), httpRequestBuilder.getFileHandler()));
        }
    }

    @Override
    public IHttpResponse get() throws Exception {
        return doExecute(doGetRequestBuilder(RequestBuilder.get()));
    }

    @Override
    public IHttpResponse post() throws Exception {
        return doExecute(doGetRequestBuilder(RequestBuilder.post()));
    }

    @Override
    public IHttpResponse head() throws Exception {
        return doExecute(doGetRequestBuilder(RequestBuilder.head()));
    }

    @Override
    public IHttpResponse put() throws Exception {
        return doExecute(doGetRequestBuilder(RequestBuilder.put()));
    }

    @Override
    public IHttpResponse delete() throws Exception {
        return doExecute(doGetRequestBuilder(RequestBuilder.delete()));
    }

    @Override
    public IHttpResponse patch() throws Exception {
        return doExecute(doGetRequestBuilder(RequestBuilder.patch()));
    }

    @Override
    public IHttpResponse options() throws Exception {
        return doExecute(doGetRequestBuilder(RequestBuilder.options()));
    }

    @Override
    public IHttpResponse trace() throws Exception {
        return doExecute(doGetRequestBuilder(RequestBuilder.trace()));
    }
}
