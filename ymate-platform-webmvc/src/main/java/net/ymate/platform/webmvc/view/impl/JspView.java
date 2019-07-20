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

import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.util.WebUtils;
import net.ymate.platform.webmvc.view.AbstractView;

import javax.servlet.http.HttpServletRequest;
import java.io.OutputStream;

/**
 * JSP视图
 *
 * @author 刘镇 (suninformation@163.com) on 2011-7-24 下午06:49:28
 */
public class JspView extends AbstractView {

    public static final String FILE_SUFFIX = ".jsp";

    private String path;

    public static JspView bind() {
        return new JspView();
    }

    public static JspView bind(String path) {
        return new JspView(path);
    }

    public static JspView bind(IWebMvc owner) {
        return new JspView(owner);
    }

    public static JspView bind(IWebMvc owner, String path) {
        return new JspView(owner, path);
    }

    public JspView(IWebMvc owner) {
        doViewInit(owner);
    }

    /**
     * 构造器
     *
     * @param owner 所属MVC框架管理器
     * @param path  JSP文件路径
     */
    public JspView(IWebMvc owner, String path) {
        doViewInit(owner);
        this.path = path;
    }

    public JspView() {
        doViewInit(WebContext.getContext().getOwner());
    }

    public JspView(String path) {
        this(WebContext.getContext().getOwner(), path);
    }

    private void doProcessPath() {
        attributes.forEach((key, value) -> WebContext.getRequest().setAttribute(key, value));
        path = doProcessPath(path, FILE_SUFFIX, true);
    }

    @Override
    protected void doRenderView() throws Exception {
        doProcessPath();
        HttpServletRequest httpServletRequest = WebContext.getRequest();
        httpServletRequest.getRequestDispatcher(path).forward(httpServletRequest, WebContext.getResponse());
    }

    @Override
    public void render(final OutputStream output) throws Exception {
        doProcessPath();
        WebUtils.includeJsp(WebContext.getRequest(), WebContext.getResponse(), path, WebContext.getResponse().getCharacterEncoding(), output);
    }
}
