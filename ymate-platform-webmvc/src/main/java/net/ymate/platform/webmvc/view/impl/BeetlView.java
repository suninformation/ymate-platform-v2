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

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.support.RecycleHelper;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.AbstractView;
import org.apache.commons.lang3.StringUtils;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.FileResourceLoader;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/4/3 上午4:06
 */
public class BeetlView extends AbstractView {

    public static final String FILE_SUFFIX = ".btl";

    private static GroupTemplate groupTemplate;

    private String path;

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
        doViewInit(owner);
        this.path = path;
    }

    public BeetlView() {
        doViewInit(WebContext.getContext().getOwner());
    }

    public BeetlView(String path) {
        this(WebContext.getContext().getOwner(), path);
    }

    /**
     * @return 返回当前模板引擎配置对象
     */
    public Configuration getEngineConfig() {
        return groupTemplate.getConf();
    }

    @Override
    protected synchronized void doViewInit(IWebMvc owner) {
        super.doViewInit(owner);
        if (groupTemplate == null) {
            try {
                String viewRoot;
                if (baseViewPath.startsWith(Type.Const.WEB_INF)) {
                    viewRoot = new File(RuntimeUtils.getRootPath(), StringUtils.substringAfter(baseViewPath, Type.Const.WEB_INF)).getPath();
                } else {
                    viewRoot = new File(baseViewPath).getPath();
                }
                FileResourceLoader resourceLoader = new FileResourceLoader(viewRoot, DEFAULT_CHARSET);
                groupTemplate = new GroupTemplate(resourceLoader, Configuration.defaultConfiguration());
                //
                RecycleHelper.getInstance().register(() -> {
                    if (groupTemplate != null) {
                        groupTemplate.close();
                        groupTemplate = null;
                    }
                });
            } catch (IOException e) {
                throw new Error(RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    private void doProcessPath() {
        path = doProcessPath(path, FILE_SUFFIX, false);
    }

    private void doRender(OutputStream outputStream) {
        Template template = groupTemplate.getTemplate(path);
        template.binding(attributes);
        template.renderTo(outputStream);
    }

    @Override
    protected void doRenderView() throws Exception {
        doProcessPath();
        doRender(WebContext.getResponse().getOutputStream());
    }

    @Override
    public void render(OutputStream output) throws Exception {
        doProcessPath();
        doRender(output);
    }
}
