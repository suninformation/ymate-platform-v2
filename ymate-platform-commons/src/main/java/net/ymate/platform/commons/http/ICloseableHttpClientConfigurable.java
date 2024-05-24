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

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;

import java.io.Closeable;
import java.io.IOException;

/**
 * 自定义CloseableHttpClient操作接口
 *
 * @author 刘镇 (suninformation@163.com) on 2024/05/13 02:11
 * @since 2.1.3
 */
public interface ICloseableHttpClientConfigurable extends Closeable {

    /**
     * 默认实现类
     */
    class Default implements ICloseableHttpClientConfigurable {

        private volatile CloseableHttpClient httpClient;

        @Override
        public CloseableHttpClient createHttpClient(SSLConnectionSocketFactory socketFactory, int connectionTimeout, int requestTimeout, int socketTimeout) {
            if (httpClient == null) {
                synchronized (ICloseableHttpClientConfigurable.Default.class) {
                    if (httpClient == null) {
                        if (socketFactory == null) {
                            socketFactory = new SSLConnectionSocketFactory(SSLContexts.createSystemDefault(), NoopHostnameVerifier.INSTANCE);
                        }
                        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                                .setConnectTimeout(connectionTimeout)
                                .setSocketTimeout(socketTimeout)
                                .setConnectionRequestTimeout(requestTimeout);
                        doRequestConfig(requestConfigBuilder);
                        httpClient = HttpClientBuilder.create()
                                .setDefaultRequestConfig(requestConfigBuilder.build())
                                .setSSLSocketFactory(socketFactory).build();
                    }
                }
            }
            return httpClient;
        }

        protected void doRequestConfig(RequestConfig.Builder builder) {
        }

        @Override
        public void close() throws IOException {
            if (httpClient != null) {
                httpClient.close();
                httpClient = null;
            }
        }
    }

    /**
     * 创建CloseableHttpClient实例
     *
     * @param socketFactory     Socket工厂对象
     * @param connectionTimeout 连接超时时间
     * @param requestTimeout    请求超时时间
     * @param socketTimeout     Socket超时时间
     * @return 返回创建的HttpClient实例对象
     */
    CloseableHttpClient createHttpClient(SSLConnectionSocketFactory socketFactory, int connectionTimeout, int requestTimeout, int socketTimeout);
}
