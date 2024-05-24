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

import org.apache.http.entity.mime.content.ContentBody;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件包装器接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/8/29 上午9:37
 */
public interface IFileWrapper extends Closeable {

    /**
     * 是否存在错误
     *
     * @return 返回true表示存在错误
     */
    boolean hasError();

    /**
     * 获取错误描述信息
     *
     * @return 返回错误描述信息
     */
    String getErrorMsg();

    /**
     * 获取文件名称(含扩展名称)
     *
     * @return 返回文件名称
     */
    String getFileName();

    /**
     * 获取文件名称(不含扩展名称)
     *
     * @return 返回文件名称
     */
    String getName();

    /**
     * 获取文件扩展名称
     *
     * @return 返回文件扩展名称
     */
    String getSuffix();

    /**
     * 获取文件长度
     *
     * @return 返回文件长度
     */
    long getContentLength();

    /**
     * 获取文件类型
     *
     * @return 返回文件类型
     */
    String getContentType();

    /**
     * 获取文件输入流
     *
     * @return 返回文件输入流
     * @throws IOException 可能产生的异常
     */
    InputStream getInputStream() throws IOException;

    /**
     * 获取文件对象
     *
     * @return 返回文件对象
     */
    File getFile();

    /**
     * 转移文件到目标文件
     *
     * @param distFile 目标文件
     * @throws IOException 可能产生的异常
     * @since 2.1.3
     */
    void transferTo(File distFile) throws IOException;

    /**
     * 复制文件到目标文件
     *
     * @param distFile 目标文件
     * @throws IOException 可能产生的异常
     */
    void writeTo(File distFile) throws IOException;

    /**
     * 将文件转换为ContentBody对象
     *
     * @return 返回转换后的对象
     * @throws IOException 可能产生的异常
     */
    ContentBody toContentBody() throws IOException;
}
