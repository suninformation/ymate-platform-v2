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

import net.ymate.platform.commons.http.impl.DefaultHttpResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * CloseableHttpClientHelper测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-15 23:15:00
 * @since 2.1.4
 */
public class CloseableHttpClientHelperTest extends AbstractHttpClientTest {

    @Test
    public void testCreate() {
        // 测试默认构造方法
        CloseableHttpClientHelper helper = CloseableHttpClientHelper.create();
        assertNotNull(helper);

        // 测试带配置的构造方法，使用默认实现
        helper = CloseableHttpClientHelper.create(new ICloseableHttpClientConfigurable.Default());
        assertNotNull(helper);
    }

    @Test
    public void testGet() throws Exception {
        try (CloseableHttpClientHelper helper = CloseableHttpClientHelper.create()) {
            // 测试简单GET请求
            IHttpResponse response = helper.get(serverUrl + "/get");
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertEquals(TEST_TEXT_CONTENT, response.getContent());

            // 测试带参数的GET请求
            response = helper.get(serverUrl + "/get?name=test");
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertEquals("Hello, test!", response.getContent());

            // 测试带Headers的GET请求
            response = helper.get(serverUrl + "/get", new Header[]{new org.apache.http.message.BasicHeader("Custom-Header", "test-value")});
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 测试带Headers和字符集的GET请求
            response = helper.get(serverUrl + "/get", new Header[0], StandardCharsets.UTF_8.name());
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void testGetWithParams() throws Exception {
        // 测试带Map参数的GET请求
        try (CloseableHttpClientHelper helper = CloseableHttpClientHelper.create()) {
            Map<String, String> params = new HashMap<>();
            params.put("name", "test");
            IHttpResponse response = helper.get(serverUrl + "/get", params);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertEquals("Hello, test!", response.getContent());

            // 测试带参数和Headers的GET请求
            response = helper.get(serverUrl + "/get", params, new Header[0]);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 测试带参数、Headers和字符集的GET请求
            response = helper.get(serverUrl + "/get", params, new Header[0], StandardCharsets.UTF_8.name());
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 测试带参数、字符集和Headers的GET请求
            response = helper.get(serverUrl + "/get", params, StandardCharsets.UTF_8, new Header[0], StandardCharsets.UTF_8.name());
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void testPost() throws Exception {
        try (CloseableHttpClientHelper helper = CloseableHttpClientHelper.create()) {
            // 测试文本POST请求
            IHttpResponse response = helper.post(serverUrl + "/post", TEST_TEXT_CONTENT);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertEquals(TEST_TEXT_CONTENT, response.getContent());

            // 测试字节数组POST请求
            byte[] contentBytes = TEST_TEXT_CONTENT.getBytes(StandardCharsets.UTF_8);
            response = helper.post(serverUrl + "/post", ContentType.TEXT_PLAIN, contentBytes, new Header[0]);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertEquals(TEST_TEXT_CONTENT, response.getContent());

            // 测试带Headers的POST请求
            response = helper.post(serverUrl + "/post", ContentType.TEXT_PLAIN, TEST_TEXT_CONTENT, new Header[]{new org.apache.http.message.BasicHeader("Custom-Header", "test-value")});
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 测试带Headers和字符集的POST请求
            response = helper.post(serverUrl + "/post", ContentType.TEXT_PLAIN, TEST_TEXT_CONTENT, new Header[0], StandardCharsets.UTF_8.name());
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 测试带参数的POST请求
            Map<String, String> params = new HashMap<>();
            params.put("name", "test");
            response = helper.post(serverUrl + "/post", params);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 测试带参数和Headers的POST请求
            response = helper.post(serverUrl + "/post", params, new Header[0]);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 测试带参数、Headers和字符集的POST请求
            response = helper.post(serverUrl + "/post", ContentType.APPLICATION_FORM_URLENCODED, params, new Header[0], StandardCharsets.UTF_8.name());
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void testPostWithInputStream() throws Exception {
        // 测试带InputStream参数的POST请求
        try (CloseableHttpClientHelper helper = CloseableHttpClientHelper.create()) {
            InputStream contentStream = new ByteArrayInputStream(TEST_TEXT_CONTENT.getBytes(StandardCharsets.UTF_8));
            IHttpResponse response = helper.post(serverUrl + "/post", ContentType.TEXT_PLAIN, contentStream, new Header[0]);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertEquals(TEST_TEXT_CONTENT, response.getContent());
        }
    }

    @Test
    public void testExecute() throws Exception {
        try (CloseableHttpClientHelper helper = CloseableHttpClientHelper.create()) {
            // 测试GET请求执行
            RequestBuilder requestBuilder = RequestBuilder.get().setUri(serverUrl + "/get");
            IHttpResponse response = helper.execute(requestBuilder, StandardCharsets.UTF_8);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 测试带字符串字符集的执行
            response = helper.execute(requestBuilder, StandardCharsets.UTF_8.name());
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 测试带IHttpRequestExecutor的执行
            response = helper.execute(httpClient -> httpClient.execute(requestBuilder.build(), DefaultHttpResponse::new));
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void testUpload() throws Exception {
        try (CloseableHttpClientHelper helper = CloseableHttpClientHelper.create()) {
            // 测试文件上传
            IHttpResponse response = helper.upload(serverUrl + "/upload", "file", testFile.toFile(), new Header[0]);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
            assertTrue(response.getContent().contains(TEST_UPLOAD_FILE_NAME));

            // 测试带默认参数的文件上传
            response = helper.upload(serverUrl + "/upload", "file", testFile.toFile());
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 测试带默认字段名的文件上传
            response = helper.upload(serverUrl + "/upload", testFile.toFile(), new Header[0]);
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());

            // 测试带默认字段名和默认Headers的文件上传
            response = helper.upload(serverUrl + "/upload", testFile.toFile());
            assertNotNull(response);
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void testDownload() throws Exception {
        try (CloseableHttpClientHelper helper = CloseableHttpClientHelper.create()) {
            // 测试下载功能
            helper.download(serverUrl + "/download", new IFileHandler() {
                @Override
                public void handle(HttpResponse response, IFileWrapper fileWrapper) throws IOException {
                    // 验证下载内容
                    assertNotNull(fileWrapper);
                    assertNotNull(fileWrapper.getInputStream());
                    // 读取输入流内容
                    String contentStr = IOUtils.toString(fileWrapper.getInputStream(), StandardCharsets.UTF_8);
                    assertEquals(TEST_TEXT_CONTENT, contentStr);
                }
            });

            // 测试带Headers的下载
            helper.download(serverUrl + "/download", new Header[]{new org.apache.http.message.BasicHeader("Custom-Header", "test-value")}, new IFileHandler() {
                @Override
                public void handle(HttpResponse response, IFileWrapper fileWrapper) throws IOException {
                    assertNotNull(fileWrapper);
                    assertNotNull(fileWrapper.getInputStream());
                }
            });
        }
    }

    @Test
    public void testNewRequestBuilder() throws Exception {
        try (CloseableHttpClientHelper helper = CloseableHttpClientHelper.create()) {
            // 测试创建请求构建器
            CloseableHttpRequestBuilder builder = helper.newRequestBuilder(serverUrl + "/get");
            assertNotNull(builder);
        }
    }

    @Test
    public void testClose() throws Exception {
        CloseableHttpClientHelper helper = CloseableHttpClientHelper.create();
        // 测试关闭方法
        try {
            helper.close();
        } catch (Exception e) {
            fail("关闭方法不应该抛出异常: " + e.getMessage());
        }
    }
}
