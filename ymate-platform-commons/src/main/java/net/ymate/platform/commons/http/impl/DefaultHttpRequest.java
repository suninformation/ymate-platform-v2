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

import net.ymate.platform.commons.http.AbstractHttpRequest;
import net.ymate.platform.commons.http.CloseableHttpClientHelper;
import net.ymate.platform.commons.http.HttpRequestBuilder;
import net.ymate.platform.commons.http.IHttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.RequestBuilder;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/04/12 17:48
 * @since 2.1.0
 */
@Deprecated
public class DefaultHttpRequest extends AbstractHttpRequest<HttpRequestBuilder> {

    public DefaultHttpRequest(HttpRequestBuilder httpRequestBuilder) {
        super(httpRequestBuilder);
    }

    protected IHttpResponse doExecute(RequestBuilder requestBuilder) throws Exception {
        try (CloseableHttpClientHelper httpClientHelper = CloseableHttpClientHelper.create(httpRequestBuilder.getConfigurable())
                .customSSL(httpRequestBuilder.getSocketFactory())
                .connectionTimeout(httpRequestBuilder.getConnectionTimeout())
                .requestTimeout(httpRequestBuilder.getRequestTimeout())
                .socketTimeout(httpRequestBuilder.getSocketTimeout())) {
            return httpClientHelper.execute(httpClient -> httpClient.execute(requestBuilder.build(), (ResponseHandler<IHttpResponse>) response ->
                    new DefaultHttpResponse(response, httpRequestBuilder.getResponseCharset(), httpRequestBuilder.isDownload(), httpRequestBuilder.getFileHandler())));
        }
    }
}
