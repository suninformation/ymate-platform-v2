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

/**
 * @author 刘镇 (suninformation@163.com) on 2020/04/12 13:56
 * @see CloseableHttpRequestBuilder
 * @since 2.1.0
 */
@Deprecated
public final class HttpRequestBuilder extends AbstractHttpRequestBuilder<HttpRequestBuilder> {

    public static HttpRequestBuilder create(String url) {
        return new HttpRequestBuilder(url);
    }

    private IHttpClientConfigurable configurable;

    public HttpRequestBuilder(String url) {
        super(url);
    }

    public HttpRequestBuilder configurable(IHttpClientConfigurable httpClientConfigurable) {
        this.configurable = httpClientConfigurable;
        return this;
    }

    public IHttpClientConfigurable getConfigurable() {
        return configurable;
    }

    @Override
    public IHttpRequest build() {
        return new DefaultHttpRequest(this);
    }
}
