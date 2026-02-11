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
package net.ymate.platform.webmvc.exception;

import net.ymate.platform.webmvc.IRequestContext;

import javax.servlet.ServletException;

/**
 * 请求未匹配异常，用于指示请求未匹配到任何控制器方法
 * 此异常不应被视为错误，而是正常的请求处理流程的一部分
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-11 15:01:10
 * @since 2.1.4
 */
public class RequestNotMatchedException extends ServletException {

    private static final long serialVersionUID = 1L;

    private final IRequestContext requestContext;

    public RequestNotMatchedException(IRequestContext requestContext) {
        super("Request not matched: " + requestContext.getRequestMapping());
        this.requestContext = requestContext;
    }

    public IRequestContext getRequestContext() {
        return requestContext;
    }
}
