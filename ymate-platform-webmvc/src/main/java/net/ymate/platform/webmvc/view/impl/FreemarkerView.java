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

import freemarker.template.Configuration;
import net.ymate.platform.commons.FreemarkerConfigBuilder;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.AbstractView;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

/**
 * Freemarker视图
 *
 * @author 刘镇 (suninformation@163.com) on 2011-10-31 下午08:45:22
 */
public class FreemarkerView extends AbstractView {

    public static final String FILE_SUFFIX = ".ftl";

    private static Configuration freemarkerConfig;

    private String path;

    public static FreemarkerView bind() {
        return new FreemarkerView();
    }

    public static FreemarkerView bind(String path) {
        return new FreemarkerView(path);
    }

    public static FreemarkerView bind(IWebMvc owner, String path) {
        return new FreemarkerView(owner, path);
    }

    /**
     * 构造器
     *
     * @param owner 所属MVC框架管理器
     * @param path  FTL文件路径
     */
    public FreemarkerView(IWebMvc owner, String path) {
        doViewInit(owner);
        this.path = path;
    }

    public FreemarkerView() {
        doViewInit(WebContext.getContext().getOwner());
    }

    public FreemarkerView(String path) {
        this(WebContext.getContext().getOwner(), path);
    }

    /**
     * @return 返回当前模板引擎配置对象
     */
    public Configuration getEngineConfig() {
        return freemarkerConfig;
    }

    @Override
    protected synchronized void doViewInit(IWebMvc owner) {
        super.doViewInit(owner);
        if (freemarkerConfig == null) {
            try {
                FreemarkerConfigBuilder configBuilder = FreemarkerConfigBuilder.create();
                if (baseViewPath.startsWith(Type.Const.WEB_INF)) {
                    freemarkerConfig = configBuilder.addTemplateFileDir(new File(RuntimeUtils.getRootPath(), StringUtils.substringAfter(baseViewPath, Type.Const.WEB_INF))).build();
                } else {
                    freemarkerConfig = configBuilder.addTemplateFileDir(new File(baseViewPath)).build();
                }
            } catch (IOException e) {
                throw new Error(RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    private void doProcessPath() {
        path = doProcessPath(path, FILE_SUFFIX, false);
    }

    @Override
    protected void doRenderView() throws Exception {
        doProcessPath();
        freemarkerConfig.getTemplate(path, WebContext.getContext().getLocale()).process(attributes, WebContext.getResponse().getWriter());
    }

    @Override
    public void render(OutputStream output) throws Exception {
        doProcessPath();
        freemarkerConfig.getTemplate(path, WebContext.getContext().getLocale()).process(attributes, new BufferedWriter(new OutputStreamWriter(output)));
    }
}
