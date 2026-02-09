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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.apache.http.entity.ContentType;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * HTTP客户端测试基类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-15 23:00:00
 * @since 2.1.4
 */
public abstract class AbstractHttpClientTest {

    protected static final String TEST_TEXT_CONTENT = "Hello, World! This is a test content.";
    protected static final String TEST_JSON_CONTENT = "{\"name\": \"test\", \"value\": 123}";
    protected static final String TEST_FILE_NAME = "test.txt";
    protected static final String TEST_UPLOAD_FILE_NAME = "upload.txt";

    protected static int serverPort = 0;
    protected static String serverUrl = "";
    protected static WireMockServer wireMockServer = null;
    protected static Path testDir = null;
    protected static Path testFile = null;

    @BeforeClass
    public static void setup() throws Exception {
        // 创建测试目录和文件
        testDir = Files.createTempDirectory("http-client-test");
        testFile = testDir.resolve(TEST_FILE_NAME);
        Files.write(testFile, TEST_TEXT_CONTENT.getBytes(StandardCharsets.UTF_8));

        // 启动WireMock服务器
        WireMockConfiguration config = WireMockConfiguration.wireMockConfig().dynamicPort();
        wireMockServer = new WireMockServer(config);
        wireMockServer.start();

        // 获取实际端口
        serverPort = wireMockServer.port();
        serverUrl = "http://localhost:" + serverPort;

        // 配置WireMock响应
        configureWireMockResponses();

        System.out.println("Embedded HTTP server started at " + serverUrl);
    }

    @AfterClass
    public static void teardown() {
        // 停止服务器
        if (wireMockServer != null) {
            try {
                wireMockServer.stop();
                System.out.println("Embedded HTTP server stopped");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 删除测试目录
        if (testDir != null) {
            try {
                Files.walk(testDir)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 配置WireMock响应
     */
    private static void configureWireMockResponses() {
        // 重置所有现有映射
        wireMockServer.resetAll();

        // 配置GET请求
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/get"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", ContentType.TEXT_PLAIN.getMimeType())
                        .withBody(TEST_TEXT_CONTENT)));

        // 配置带参数的GET请求
        wireMockServer.stubFor(WireMock.get(WireMock.urlPathEqualTo("/get"))
                .withQueryParam("name", WireMock.equalTo("test"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", ContentType.TEXT_PLAIN.getMimeType())
                        .withBody("Hello, test!")));

        // 配置POST请求 - 文本内容
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/post"))
                .withHeader("Content-Type", WireMock.notMatching("application/json"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", ContentType.TEXT_PLAIN.getMimeType())
                        .withBody("Hello, World! This is a test content.")));

        // 配置POST请求 - JSON内容
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/post"))
                .withHeader("Content-Type", WireMock.matching("application/json"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType())
                        .withBody(TEST_JSON_CONTENT)));

        // 配置文件下载
        wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/download"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", ContentType.TEXT_PLAIN.getMimeType())
                        .withHeader("Content-Disposition", "attachment; filename=" + TEST_FILE_NAME)
                        .withBody(TEST_TEXT_CONTENT)));

        // 配置文件上传
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/upload"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", ContentType.TEXT_PLAIN.getMimeType())
                        .withBody("File uploaded successfully: " + TEST_UPLOAD_FILE_NAME)));

        // 配置多部分表单
        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/multipart"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", ContentType.TEXT_PLAIN.getMimeType())
                        .withBody("Multipart form processed successfully")));

        // 配置方法测试
        wireMockServer.stubFor(WireMock.any(WireMock.urlEqualTo("/method"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", ContentType.TEXT_PLAIN.getMimeType())
                        .withBody("Method received: GET")));
    }

    /**
     * 创建临时测试文件
     */
    protected Path createTempFile(String content) throws IOException {
        Path tempFile = Files.createTempFile(testDir, "temp", ".txt");
        Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));
        return tempFile;
    }
}
