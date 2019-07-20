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

import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.AbstractView;

import javax.servlet.http.HttpServletRequest;

/**
 * 内部重定向（请求转发）视图
 *
 * @author 刘镇 (suninformation@163.com) on 2011-10-28 下午05:15:10
 */
public class ForwardView extends AbstractView {

    /**
     * 重定向URL
     */
    private String path;

    public static ForwardView bind(String path) {
        return new ForwardView(path);
    }

    /**
     * 构造器
     *
     * @param path 转发路径
     */
    public ForwardView(String path) {
        this.path = path;
    }

    @Override
    protected void doRenderView() throws Exception {
        // 绝对路径 : 以 '/' 开头的路径不增加 '/WEB-INF'
        if (path.charAt(0) != Type.Const.PATH_SEPARATOR_CHAR) {
            // 包名形式的路径
            path = Type.Const.WEB_INF + path;
        }
        // 执行 Forward
        HttpServletRequest httpServletRequest = WebContext.getRequest();
        httpServletRequest.getRequestDispatcher(buildUrl(path)).forward(httpServletRequest, WebContext.getResponse());
    }
}
