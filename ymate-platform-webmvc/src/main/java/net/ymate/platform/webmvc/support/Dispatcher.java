/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.webmvc.support;

import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.WebMVC;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-04 17:27
 * @version 1.0
 */
public class Dispatcher {

    private String charsetEncoding;

    private String contentType;

    private String requestMethodParam;

    public Dispatcher(String charsetEncoding, String contentType, String requestMethodParam) {
        this.charsetEncoding = charsetEncoding;
        this.contentType = contentType;
        this.requestMethodParam = requestMethodParam;
    }

    public void dispatch(IRequestContext _requestContext, ServletContext servletContext, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding(charsetEncoding);
        response.setCharacterEncoding(charsetEncoding);
        response.setContentType(contentType.concat("; charset=").concat(charsetEncoding));
        //
        GenericDispatcher.create(WebMVC.get()).execute(_requestContext, servletContext, new RequestMethodWrapper(request, requestMethodParam), new GenericResponseWrapper(response));
    }
}
