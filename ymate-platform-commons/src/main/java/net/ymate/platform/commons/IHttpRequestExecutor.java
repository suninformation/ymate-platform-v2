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
package net.ymate.platform.commons;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * HTTP请求执行器接口
 *
 * @param <T> 结果对象类型
 * @author 刘镇 (suninformation@163.com) on 2018/7/26 下午12:08
 */
public interface IHttpRequestExecutor<T> {

    /**
     * 执行
     *
     * @param httpClient HttpClient实例对象
     * @return 返回执行结果对象
     * @throws Exception 可能产生的任何异常
     */
    T execute(CloseableHttpClient httpClient) throws Exception;
}
