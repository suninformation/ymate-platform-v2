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
import org.apache.commons.io.IOUtils;

import java.io.OutputStream;

/**
 * 文本视图
 *
 * @author 刘镇 (suninformation@163.com) on 2011-10-23 上午11:15:43
 */
public class TextView extends AbstractView {

    /**
     * 文本内容
     */
    private final String content;

    public static TextView bind(String content) {
        return new TextView(content);
    }

    /**
     * 构造器
     *
     * @param content 输出文本
     */
    public TextView(String content) {
        this(content, Type.ContentType.TEXT.getContentType());
    }

    /**
     * 构造器
     *
     * @param content     输出文本
     * @param contentType 内容类型
     */
    public TextView(String content, String contentType) {
        this.content = content;
        this.contentType = contentType;
    }

    @Override
    protected void doRenderView() throws Exception {
        render(WebContext.getResponse().getOutputStream());
    }

    @Override
    public void render(OutputStream output) throws Exception {
        IOUtils.write(content, output, WebContext.getResponse().getCharacterEncoding());
    }
}
