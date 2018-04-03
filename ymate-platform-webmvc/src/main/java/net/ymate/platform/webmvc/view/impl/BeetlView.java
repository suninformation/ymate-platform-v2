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
package net.ymate.platform.webmvc.view.impl;

import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.AbstractView;
import org.apache.commons.lang.StringUtils;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.FileResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/4/3 上午4:06
 * @version 1.0
 */
public class BeetlView extends AbstractView {

    static GroupTemplate __groupTemplate;

    String __path;

    public static BeetlView bind() {
        return new BeetlView();
    }

    public static BeetlView bind(String path) {
        return new BeetlView(path);
    }

    public static BeetlView bind(IWebMvc owner, String path) {
        return new BeetlView(owner, path);
    }

    /**
     * 构造器
     *
     * @param owner 所属MVC框架管理器
     * @param path  模板文件路径
     */
    public BeetlView(IWebMvc owner, String path) {
        __doViewInit(owner);
        __path = path;
    }

    public BeetlView() {
        __doViewInit(WebContext.getContext().getOwner());
    }

    public BeetlView(String path) {
        this(WebContext.getContext().getOwner(), path);
    }

    /**
     * @return 返回当前模板引擎配置对象
     */
    public Configuration getEngineConfig() {
        return __groupTemplate.getConf();
    }

    @Override
    protected synchronized void __doViewInit(IWebMvc owner) {
        super.__doViewInit(owner);
        // 初始化Beelt模板引擎配置
        if (__groupTemplate == null) {
            try {
                String _viewRoot;
                if (__baseViewPath.startsWith("/WEB-INF")) {
                    _viewRoot = new File(RuntimeUtils.getRootPath(), StringUtils.substringAfter(__baseViewPath, "/WEB-INF/")).getPath();
                } else {
                    _viewRoot = new File(__baseViewPath).getPath();
                }
                FileResourceLoader resourceLoader = new FileResourceLoader(_viewRoot, DEFAULT_CHARSET);
                Configuration cfg = Configuration.defaultConfiguration();
                __groupTemplate = new GroupTemplate(resourceLoader, cfg);
            } catch (IOException e) {
                throw new Error(RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    protected void __doProcessPath() {
        if (StringUtils.isNotBlank(__contentType)) {
            WebContext.getResponse().setContentType(__contentType);
        }
        if (StringUtils.isBlank(__path)) {
            String _mapping = WebContext.getRequestContext().getRequestMapping();
            if (_mapping.endsWith("/")) {
                _mapping = _mapping.substring(0, _mapping.length() - 1);
            }
            __path = _mapping + ".btl";
        } else {
            if (__path.startsWith(__baseViewPath)) {
                __path = StringUtils.substringAfter(__path, __baseViewPath);
            }
            if (!__path.endsWith(".")) {
                __path += ".btl";
            }
        }
    }

    private void __doRender(OutputStream outputStream) {
        Template _tmpl = __groupTemplate.getTemplate(__path);
        _tmpl.binding(__attributes);
        _tmpl.renderTo(outputStream);
    }

    @Override
    protected void __doRenderView() throws Exception {
        __doProcessPath();
        __doRender(WebContext.getResponse().getOutputStream());
    }

    @Override
    public void render(OutputStream output) throws Exception {
        __doProcessPath();
        __doRender(output);
    }
}
