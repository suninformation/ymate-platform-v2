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
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.AbstractView;
import net.ymate.platform.webmvc.view.View;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Properties;

/**
 * Velocity视图
 *
 * @author 刘镇 (suninformation@163.com) on 15/10/28 下午8:20
 */
public class VelocityView extends AbstractView {

    public static final String FILE_SUFFIX = ".vm";

    private static Properties velocityConfig = new Properties();

    static {
        View.registerViewBuilder(FILE_SUFFIX, VelocityView::bind);
    }

    private VelocityContext velocityContext;

    private boolean initialized;

    private String path;

    public static VelocityView bind() {
        return new VelocityView();
    }

    public static VelocityView bind(String path) {
        return new VelocityView(WebContext.getContext().getOwner(), path);
    }

    public static VelocityView bind(IWebMvc owner, String path) {
        return new VelocityView(owner, path);
    }

    /**
     * 构造器
     *
     * @param owner 所属MVC框架管理器
     * @param path  VM文件路径
     */
    public VelocityView(IWebMvc owner, String path) {
        doViewInit(owner);
        this.path = path;
    }

    public VelocityView() {
        doViewInit(WebContext.getContext().getOwner());
    }

    @Override
    protected synchronized void doViewInit(IWebMvc owner) {
        super.doViewInit(owner);
        if (!initialized) {
            velocityConfig.setProperty(Velocity.INPUT_ENCODING, DEFAULT_CHARSET);
            velocityConfig.setProperty(Velocity.OUTPUT_ENCODING, DEFAULT_CHARSET);
            //
            if (baseViewPath.startsWith(Type.Const.WEB_INF)) {
                velocityConfig.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, new File(RuntimeUtils.getRootPath(), StringUtils.substringAfter(baseViewPath, Type.Const.WEB_INF)).getPath());
            } else {
                velocityConfig.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, baseViewPath);
            }
            Velocity.init(velocityConfig);
            //
            initialized = true;
        }
    }

    public static void properties(String key, String value) {
        velocityConfig.setProperty(key, value);
    }

    private void doProcessPath() {
        path = doProcessPath(path, FILE_SUFFIX, false);
        velocityContext = new VelocityContext();
        attributes.forEach((key, value) -> velocityContext.put(key, value));
    }

    @Override
    protected void doRenderView() throws Exception {
        doProcessPath();
        Velocity.getTemplate(path).merge(velocityContext, WebContext.getResponse().getWriter());
    }

    @Override
    public void render(OutputStream output) throws Exception {
        doProcessPath();
        Velocity.getTemplate(path).merge(velocityContext, new BufferedWriter(new OutputStreamWriter(output)));
    }

}
