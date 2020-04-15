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

import org.apache.http.HttpResponse;

import java.io.IOException;

/**
 * 文件处理器接口
 *
 * @author 刘镇 (suninformation@163.com) on 16/7/13 下午1:35
 */
public interface IFileHandler {

    /**
     * 执行处理过程
     *
     * @param response    HTTP响应对象
     * @param fileWrapper 文件包装器
     * @throws IOException 可能产生的异常
     */
    void handle(HttpResponse response, IFileWrapper fileWrapper) throws IOException;
}
