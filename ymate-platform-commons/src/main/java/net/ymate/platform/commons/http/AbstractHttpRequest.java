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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.File;
import java.io.InputStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2024/5/13 03:03
 * @since 2.1.3
 */
public abstract class AbstractHttpRequest<T extends AbstractHttpRequestBuilder<?>> implements IHttpRequest {

    protected final T httpRequestBuilder;

    protected AbstractHttpRequest(T httpRequestBuilder) {
        this.httpRequestBuilder = httpRequestBuilder;
    }

    protected RequestBuilder doRequestBuilder(RequestBuilder requestBuilder) {
        requestBuilder.setUri(httpRequestBuilder.getUrl())
                .setCharset(httpRequestBuilder.getCharset());
        if (httpRequestBuilder.getRequestConfig() != null) {
            requestBuilder.setConfig(httpRequestBuilder.getRequestConfig().build());
        }
        httpRequestBuilder.getHeaders().forEach(requestBuilder::addHeader);
        httpRequestBuilder.getParams().forEach(requestBuilder::addParameter);
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
                        contentType = ContentType.create(AbstractHttpClientHelper.CONTENT_TYPE_TEXT_PLAIN, requestBuilder.getCharset() != null ? requestBuilder.getCharset().name() : AbstractHttpClientHelper.DEFAULT_CHARSET);
                    }
                    requestBuilder.setEntity(EntityBuilder.create()
                            .setContentEncoding(contentType.getCharset().name())
                            .setContentType(contentType)
                            .setText((String) content).build());
                } else {
                    if (contentType == null) {
                        contentType = ContentType.create(AbstractHttpClientHelper.CONTENT_TYPE_OCTET_STREAM, requestBuilder.getCharset() != null ? requestBuilder.getCharset().name() : AbstractHttpClientHelper.DEFAULT_CHARSET);
                    }
                    String contentEncoding = doGetContentEncoding(requestBuilder, contentType);
                    EntityBuilder entityBuilder = EntityBuilder.create()
                            .setContentEncoding(contentEncoding)
                            .setContentType(contentType);
                    if (content instanceof byte[]) {
                        requestBuilder.setEntity(entityBuilder.setBinary((byte[]) content).build());
                    } else if (content instanceof InputStream) {
                        requestBuilder.setEntity(entityBuilder.setStream((InputStream) content).build());
                    } else if (content instanceof File) {
                        requestBuilder.setEntity(entityBuilder.setFile((File) content).build());
                    }
                }
            }
        }
        return requestBuilder;
    }

    protected String doGetContentEncoding(RequestBuilder requestBuilder, ContentType contentType) {
        String contentEncoding;
        if (contentType.getCharset() == null) {
            if (requestBuilder.getCharset() != null) {
                contentEncoding = requestBuilder.getCharset().name();
            } else {
                contentEncoding = AbstractHttpClientHelper.DEFAULT_CHARSET;
            }
        } else {
            contentEncoding = contentType.getCharset().name();
        }
        return contentEncoding;
    }

    protected abstract IHttpResponse doExecute(RequestBuilder requestBuilder) throws Exception;

    @Override
    public IHttpResponse get() throws Exception {
        return doExecute(doRequestBuilder(RequestBuilder.get()));
    }

    @Override
    public IHttpResponse post() throws Exception {
        return doExecute(doRequestBuilder(RequestBuilder.post()));
    }

    @Override
    public IHttpResponse head() throws Exception {
        return doExecute(doRequestBuilder(RequestBuilder.head()));
    }

    @Override
    public IHttpResponse put() throws Exception {
        return doExecute(doRequestBuilder(RequestBuilder.put()));
    }

    @Override
    public IHttpResponse delete() throws Exception {
        return doExecute(doRequestBuilder(RequestBuilder.delete()));
    }

    @Override
    public IHttpResponse patch() throws Exception {
        return doExecute(doRequestBuilder(RequestBuilder.patch()));
    }

    @Override
    public IHttpResponse options() throws Exception {
        return doExecute(doRequestBuilder(RequestBuilder.options()));
    }

    @Override
    public IHttpResponse trace() throws Exception {
        return doExecute(doRequestBuilder(RequestBuilder.trace()));
    }
}
