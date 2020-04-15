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
package net.ymate.platform.commons.http;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * 自定义HttpClient操作接口
 *
 * @author 刘镇 (suninformation@163.com) on 2018/7/25 上午10:17
 */
public interface IHttpClientConfigurable {

    /**
     * 创建HttpClient实例
     *
     * @param socketFactory     Socket工厂对象
     * @param connectionTimeout 连接超时时间
     * @param requestTimeout    请求超时时间
     * @param socketTimeout     Socket超时时间
     * @return 返回创建的HttpClient实例对象
     */
    CloseableHttpClient createHttpClient(SSLConnectionSocketFactory socketFactory, int connectionTimeout, int requestTimeout, int socketTimeout);

    /**
     * 关闭(释放)HttpClient实例
     *
     * @param httpClient HttpClient实例对象
     */
    void closeHttpClient(CloseableHttpClient httpClient);
}
