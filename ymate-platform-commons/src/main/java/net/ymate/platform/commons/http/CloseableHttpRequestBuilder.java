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

import net.ymate.platform.commons.http.impl.DefaultHttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.RequestBuilder;

/**
 * @author 刘镇 (suninformation@163.com) on 2024/5/13 14:40
 * @since 2.1.3
 */
public class CloseableHttpRequestBuilder extends AbstractHttpRequestBuilder<CloseableHttpRequestBuilder> {

    public static CloseableHttpRequestBuilder create(CloseableHttpClientHelper httpClientHelper, String url) {
        return new CloseableHttpRequestBuilder(httpClientHelper, url);
    }

    public static CloseableHttpRequestBuilder create(String url, ICloseableHttpClientConfigurable configurable) {
        return new CloseableHttpRequestBuilder(url, configurable);
    }

    public static CloseableHttpRequestBuilder create(String url) {
        return new CloseableHttpRequestBuilder(url, null);
    }

    private final CloseableHttpClientHelper httpClientHelper;

    private final ICloseableHttpClientConfigurable configurable;

    private CloseableHttpRequestBuilder(CloseableHttpClientHelper httpClientHelper, String url) {
        super(url);
        this.httpClientHelper = httpClientHelper;
        this.configurable = null;
    }

    private CloseableHttpRequestBuilder(String url, ICloseableHttpClientConfigurable configurable) {
        super(url);
        this.httpClientHelper = null;
        this.configurable = configurable;
    }

    @Override
    public IHttpRequest build() {
        if (httpClientHelper == null) {
            RequestConfig.Builder builder = getRequestConfig();
            if (builder == null) {
                builder = requestConfig(RequestConfig.custom()).getRequestConfig();
            }
            if (getConnectionTimeout() != -1) {
                builder.setConnectTimeout(getConnectionTimeout());
            }
            if (getRequestTimeout() != -1) {
                builder.setConnectionRequestTimeout(getRequestTimeout());
            }
            if (getSocketTimeout() != -1) {
                builder.setSocketTimeout(getSocketTimeout());
            }
        }
        return new AbstractHttpRequest<CloseableHttpRequestBuilder>(this) {
            @Override
            protected IHttpResponse doExecute(RequestBuilder requestBuilder) throws Exception {
                if (httpClientHelper == null) {
                    try (CloseableHttpClientHelper clientHelper = CloseableHttpClientHelper.create(configurable)
                            .customSSL(httpRequestBuilder.getSocketFactory())
                            .connectionTimeout(httpRequestBuilder.getConnectionTimeout())
                            .requestTimeout(httpRequestBuilder.getRequestTimeout())
                            .socketTimeout(httpRequestBuilder.getSocketTimeout())) {
                        return clientHelper.execute(httpClient -> httpClient.execute(requestBuilder.build(), (ResponseHandler<IHttpResponse>) response ->
                                new DefaultHttpResponse(response, httpRequestBuilder.getResponseCharset(), httpRequestBuilder.isDownload(), httpRequestBuilder.getFileHandler())));
                    }
                } else {
                    return httpClientHelper.execute(httpClient -> httpClient.execute(requestBuilder.build(), (ResponseHandler<IHttpResponse>) response ->
                            new DefaultHttpResponse(response, httpRequestBuilder.getResponseCharset(), httpRequestBuilder.isDownload(), httpRequestBuilder.getFileHandler())));
                }
            }
        };
    }
}
