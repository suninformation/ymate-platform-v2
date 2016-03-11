/*
 * Copyright 2007-2016 the original author or authors.
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

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 16/3/10 下午10:20
 * @version 1.0
 */
public class GenericResponseWrapper extends HttpServletResponseWrapper {

    private int statusCode = SC_OK;

    public GenericResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    public void setStatus(final int code) {
        statusCode = code;
        super.setStatus(code);
    }

    public void setStatus(final int code, final String msg) {
        statusCode = code;
        super.setStatus(code);
    }

    public int getStatus() {
        return statusCode;
    }

    public void sendError(int i, String string) throws IOException {
        statusCode = i;
        super.sendError(i, string);
    }

    public void sendError(int i) throws IOException {
        statusCode = i;
        super.sendError(i);
    }

    public void sendRedirect(String string) throws IOException {
        statusCode = HttpServletResponse.SC_MOVED_TEMPORARILY;
        super.sendRedirect(string);
    }

    public void reset() {
        super.reset();
        statusCode = SC_OK;
    }
}
