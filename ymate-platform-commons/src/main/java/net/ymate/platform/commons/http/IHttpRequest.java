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

/**
 * @author 刘镇 (suninformation@163.com) on 2020/04/12 16:17
 * @since 2.1.0
 */
public interface IHttpRequest {

    /**
     * 执行GET请求
     *
     * @return 返回响应接口对象
     * @throws Exception 可能产生的任何异常
     */
    IHttpResponse get() throws Exception;

    /**
     * 执行POST请求
     *
     * @return 返回响应接口对象
     * @throws Exception 可能产生的任何异常
     */
    IHttpResponse post() throws Exception;

    /**
     * 执行HEAD请求
     *
     * @return 返回响应接口对象
     * @throws Exception 可能产生的任何异常
     */
    IHttpResponse head() throws Exception;

    /**
     * 执行PUT请求
     *
     * @return 返回响应接口对象
     * @throws Exception 可能产生的任何异常
     */
    IHttpResponse put() throws Exception;

    /**
     * 执行DELETE请求
     *
     * @return 返回响应接口对象
     * @throws Exception 可能产生的任何异常
     */
    IHttpResponse delete() throws Exception;

    /**
     * 执行PATCH请求
     *
     * @return 返回响应接口对象
     * @throws Exception 可能产生的任何异常
     */
    IHttpResponse patch() throws Exception;

    /**
     * 执行OPTIONS请求
     *
     * @return 返回响应接口对象
     * @throws Exception 可能产生的任何异常
     */
    IHttpResponse options() throws Exception;

    /**
     * 执行TRACE请求
     *
     * @return 返回响应接口对象
     * @throws Exception 可能产生的任何异常
     */
    IHttpResponse trace() throws Exception;
}
