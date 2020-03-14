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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求响应接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/9/7 下午11:20
 */
public interface IHttpResponse {

    /**
     * 默认实现
     */
    class Default implements IHttpResponse {

        private final int statusCode;

        private final String content;

        private String contentType;

        private String contentEncoding;

        private final long contentLength;

        //
        private final Map<String, String> headers = new HashMap<>();

        public Default(HttpResponse response) throws IOException {
            this(response, HttpClientHelper.DEFAULT_CHARSET);
        }

        public Default(HttpResponse response, String defaultCharset) throws IOException {
            statusCode = response.getStatusLine().getStatusCode();
            content = EntityUtils.toString(response.getEntity(), defaultCharset);
            Header header = response.getEntity().getContentEncoding();
            if (header != null) {
                contentEncoding = header.getValue();
            }
            header = response.getEntity().getContentType();
            if (header != null) {
                contentType = header.getValue();
            }
            contentLength = response.getEntity().getContentLength();
            Header[] headersArr = response.getAllHeaders();
            if (headersArr != null) {
                Arrays.stream(headersArr).forEachOrdered(element -> headers.put(element.getName(), element.getValue()));
            }
        }

        @Override
        public int getStatusCode() {
            return statusCode;
        }

        @Override
        public String getContent() {
            return content;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public long getContentLength() {
            return contentLength;
        }

        @Override
        public String getContentEncoding() {
            return contentEncoding;
        }

        @Override
        public Map<String, String> getHeaders() {
            return Collections.unmodifiableMap(headers);
        }

        @Override
        public String toString() {
            return String.format("{statusCode=%d, content='%s', contentType='%s', contentEncoding='%s', contentLength=%d, headers=%s}", statusCode, content, contentType, contentEncoding, contentLength, headers);
        }
    }

    /**
     * 获取HTTP状态码
     *
     * @return 返回状态码
     */
    int getStatusCode();

    /**
     * 获取响应内容
     *
     * @return 返回响应内容
     */
    String getContent();

    /**
     * 获取内容类型
     *
     * @return 返回内容类型
     */
    String getContentType();

    /**
     * 获取内容长度
     *
     * @return 返回内容长度
     */
    long getContentLength();

    /**
     * 获取内容编码字符集
     *
     * @return 返回编码字符集
     */
    String getContentEncoding();

    /**
     * 获取响应头集合
     *
     * @return 返回响应头数组集合
     */
    Map<String, String> getHeaders();
}
