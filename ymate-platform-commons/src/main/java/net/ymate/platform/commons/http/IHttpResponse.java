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

import java.io.Closeable;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * HTTP请求响应接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/9/7 下午11:20
 */
public interface IHttpResponse extends Closeable {

    /**
     * 获取HTTP状态码
     *
     * @return 返回状态码
     */
    int getStatusCode();

    /**
     * 获取原因说明
     *
     * @return 返回原因说明文字
     */
    String getReasonPhrase();

    /**
     * 获取Locale对象
     *
     * @return 返回Locale对象
     */
    Locale getLocale();

    /**
     * 获取响应内容
     *
     * @return 返回响应内容
     * @throws IOException 可能产生的IO异常
     */
    String getContent() throws IOException;

    /**
     * 获取文件包装器
     *
     * @return 返回文件包装器对象(若当前响应并非文件下载则返回null)
     */
    IFileWrapper getFileWrapper();

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
