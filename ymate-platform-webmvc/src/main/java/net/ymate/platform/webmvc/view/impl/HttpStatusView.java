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
package net.ymate.platform.webmvc.view.impl;

import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.AbstractView;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP返回码视图
 *
 * @author 刘镇 (suninformation@163.com) on 2011-10-29 上午02:26:25
 */
public class HttpStatusView extends AbstractView {

    /**
     * HTTP返回码
     */
    private final int status;

    private final String msg;

    private final boolean error;

    private String body;

    /**
     * STATUS: 405
     */
    public static HttpStatusView METHOD_NOT_ALLOWED = new HttpStatusView(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

    /**
     * STATUS: 404
     */
    public static HttpStatusView NOT_FOUND = new HttpStatusView(HttpServletResponse.SC_NOT_FOUND);

    /**
     * STATUS: 400
     */
    public static HttpStatusView BAD_REQUEST = new HttpStatusView(HttpServletResponse.SC_BAD_REQUEST);

    public static HttpStatusView bind(int status) {
        return new HttpStatusView(status);
    }

    public static HttpStatusView bind(int status, String msg) {
        return new HttpStatusView(status, msg);
    }

    /**
     * 构造器
     *
     * @param status HTTP返回码
     */
    public HttpStatusView(int status) {
        this.status = status;
        msg = null;
        error = true;
    }

    /**
     * 构造器
     *
     * @param status   HTTP返回码
     * @param useError 是否使用sendError方法
     */
    public HttpStatusView(int status, boolean useError) {
        this.status = status;
        error = useError;
        msg = null;
    }

    /**
     * 构造器
     *
     * @param status HTTP返回码
     * @param msg    错误提示信息
     */
    public HttpStatusView(int status, String msg) {
        this.status = status;
        this.msg = msg;
        error = false;
    }

    /**
     * 将文本内容写入回应数据流(注:调用此方法需采用useError=false设置)
     *
     * @param bodyStr 写入的内容
     * @return 当前视图对象
     */
    public HttpStatusView writeBody(String bodyStr) {
        body = bodyStr;
        return this;
    }

    @Override
    protected void doRenderView() throws Exception {
        HttpServletResponse httpServletResponse = WebContext.getResponse();
        if (StringUtils.isNotBlank(body)) {
            IOUtils.write(body, httpServletResponse.getOutputStream(), httpServletResponse.getCharacterEncoding());
        }
        if (StringUtils.isNotBlank(msg)) {
            httpServletResponse.sendError(status, msg);
        } else {
            if (error) {
                httpServletResponse.sendError(status);
            } else {
                httpServletResponse.setStatus(status);
            }
        }
        if (LOG.isDebugEnabled()) {
            List<String> strings = new ArrayList<>();
            strings.add(String.format("Status: %s", status));
            if (StringUtils.isNotBlank(msg)) {
                strings.add(String.format("Msg: %s", msg));
            }
            if (StringUtils.isNotBlank(body)) {
                strings.add(String.format("Body: %s", body));
            }
            doWriteLog(HttpStatusView.class, strings.toString());
        }
    }
}
