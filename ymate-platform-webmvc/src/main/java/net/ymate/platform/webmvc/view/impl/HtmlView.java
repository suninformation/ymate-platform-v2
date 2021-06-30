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
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.AbstractView;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * HTML文件内容视图
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 下午5:49
 */
public class HtmlView extends AbstractView {

    public static final String FILE_SUFFIX = ".html";

    /**
     * HTML内容
     */
    private final String content;

    public static HtmlView bind(IWebMvc owner, String htmlFile) throws Exception {
        if (StringUtils.isNotBlank(htmlFile)) {
            if (htmlFile.charAt(0) != Type.Const.PATH_SEPARATOR_CHAR) {
                htmlFile = Type.Const.PATH_SEPARATOR_CHAR + htmlFile;
            }
            String viewPath = getBaseViewPath(owner);
            if (htmlFile.startsWith(viewPath)) {
                htmlFile = StringUtils.substringAfter(viewPath, viewPath);
            }
            if (!htmlFile.endsWith(FILE_SUFFIX)) {
                htmlFile += FILE_SUFFIX;
            }
            return bind(new File(owner.getConfig().getAbstractBaseViewPath(), htmlFile));
        }
        return null;
    }

    public static HtmlView bind(File htmlFile) throws Exception {
        if (htmlFile != null && htmlFile.canRead() && htmlFile.exists() && htmlFile.isFile()) {
            try (InputStream inputStream = new FileInputStream(htmlFile)) {
                return new HtmlView(IOUtils.toString(inputStream, WebContext.getResponse().getCharacterEncoding()));
            }
        }
        return null;
    }

    public static HtmlView bind(String content) {
        return new HtmlView(content);
    }

    /**
     * 构造器
     *
     * @param content 输出HTML内容
     */
    public HtmlView(String content) {
        this.content = content;
        contentType = Type.ContentType.HTML.getContentType();
    }

    @Override
    protected void doRenderView() throws Exception {
        HttpServletResponse httpServletResponse = WebContext.getResponse();
        IOUtils.write(content, httpServletResponse.getOutputStream(), httpServletResponse.getCharacterEncoding());
    }

    @Override
    public void render(OutputStream output) throws Exception {
        IOUtils.write(content, output, WebContext.getResponse().getCharacterEncoding());
    }
}
