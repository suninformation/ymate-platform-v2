/*
 * Copyright 2007-present the original author or authors.
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

import org.apache.http.entity.ContentType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * CloseableHttpRequestBuilder测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-15 23:30:00
 * @since 2.1.4
 */
public class CloseableHttpRequestBuilderTest extends AbstractHttpClientTest {

    @Test
    public void testCreate() {
        // 测试带HttpClientHelper的构造方法
        CloseableHttpClientHelper helper = CloseableHttpClientHelper.create();
        CloseableHttpRequestBuilder builder = CloseableHttpRequestBuilder.create(helper, serverUrl + "/get");
        assertNotNull(builder);

        // 测试带配置的构造方法，使用默认实现
        builder = CloseableHttpRequestBuilder.create(serverUrl + "/get", new ICloseableHttpClientConfigurable.Default());
        assertNotNull(builder);

        // 测试默认构造方法
        builder = CloseableHttpRequestBuilder.create(serverUrl + "/get");
        assertNotNull(builder);
    }

    @Test
    public void testBuildAndExecuteGet() throws Exception {
        // 测试GET请求构建和执行
        CloseableHttpRequestBuilder builder = CloseableHttpRequestBuilder.create(serverUrl + "/get");
        IHttpRequest request = builder.build();
        assertNotNull(request);

        IHttpResponse response = request.get();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(TEST_TEXT_CONTENT, response.getContent());
    }

    @Test
    public void testBuildAndExecutePost() throws Exception {
        // 测试POST请求构建和执行
        CloseableHttpRequestBuilder builder = CloseableHttpRequestBuilder.create(serverUrl + "/post");
        builder.content(TEST_TEXT_CONTENT)
                .contentType(ContentType.TEXT_PLAIN);
        IHttpRequest request = builder.build();
        assertNotNull(request);

        IHttpResponse response = request.post();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(TEST_TEXT_CONTENT, response.getContent());
    }

    @Test
    public void testBuildWithHeaders() throws Exception {
        // 测试带Headers的请求构建
        CloseableHttpRequestBuilder builder = CloseableHttpRequestBuilder.create(serverUrl + "/get");
        builder.addHeader("Custom-Header", "test-value")
                .addHeader("Another-Header", "another-value");

        IHttpRequest request = builder.build();
        assertNotNull(request);

        IHttpResponse response = request.get();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void testBuildWithParams() throws Exception {
        // 测试带参数的请求构建
        CloseableHttpRequestBuilder builder = CloseableHttpRequestBuilder.create(serverUrl + "/get");
        builder.addParam("name", "test")
                .addParam("value", "123");

        IHttpRequest request = builder.build();
        assertNotNull(request);

        IHttpResponse response = request.get();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello, test!", response.getContent());
    }

    @Test
    public void testBuildWithDownload() throws Exception {
        // 测试下载请求构建
        CloseableHttpRequestBuilder builder = CloseableHttpRequestBuilder.create(serverUrl + "/download")
                .download(true);
        IHttpRequest request = builder.build();
        assertNotNull(request);

        IHttpResponse response = request.get();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }
}
