/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.webmvc.view;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.context.WebContext;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * 抽象MVC视图接口
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-20 下午6:56:56
 * @version 1.0
 */
public abstract class AbstractView implements IView {

    protected static Configuration __freemarkerConfig;

    protected static String __baseViewPath;

    protected static String __pluginViewPath;

    private static final Object __LOCKER = new Object();

    ////

    protected Map<String, Object> __attributes;

    protected String __contentType;

    public AbstractView() {
        __attributes = new HashMap<String, Object>();
    }

    public IView addAttribute(String name, Object value) {
        __attributes.put(name, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name) {
        return (T) __attributes.get(name);
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(__attributes);
    }

    public String getContentType() {
        return __contentType;
    }

    public IView setContentType(String contentType) {
        __contentType = contentType;
        return this;
    }

    public IView addDateHeader(String name, long date) {
        WebContext.getResponse().addDateHeader(name, date);
        return this;
    }

    public IView addHeader(String name, String value) {
        WebContext.getResponse().addHeader(name, value);
        return this;
    }

    public IView addIntHeader(String name, int value) {
        WebContext.getResponse().addIntHeader(name, value);
        return this;
    }

    public void render() throws Exception {
        if (WebContext.getResponse().isCommitted()) {
            return;
        }
        if (StringUtils.isNotBlank(__contentType)) {
            WebContext.getResponse().setContentType(__contentType);
        }
        __doRenderView();
    }

    /**
     * 视图渲染具体操作
     *
     * @throws Exception 抛出任何可能异常
     */
    protected abstract void __doRenderView() throws Exception;

    public void render(OutputStream output) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * 将参数与URL地址进行绑定
     *
     * @throws UnsupportedEncodingException URL编码异常
     */
    protected String __doBuildURL(String url) throws UnsupportedEncodingException {
        if (__attributes.isEmpty()) {
            return url;
        }
        StringBuilder _paramSB = new StringBuilder(url);
        if (!url.contains("?")) {
            _paramSB.append("?");
        } else {
            _paramSB.append("&");
        }
        boolean _flag = true;
        for (Map.Entry<String, Object> _entry : __attributes.entrySet()) {
            if (_flag) {
                _flag = false;
            } else {
                _paramSB.append("&");
            }
            _paramSB.append(_entry.getKey()).append("=");
            if (_entry.getValue() != null && StringUtils.isNotEmpty(_entry.getValue().toString())) {
                _paramSB.append(URLEncoder.encode(_entry.getValue().toString(), WebContext.getRequest().getCharacterEncoding()));
            }
        }
        return _paramSB.toString();
    }

    /**
     * 初始化配置参数(全局唯一)
     *
     * @param owner 所属WebMVC框架管理器
     */
    protected void __doInitConfiguration(IWebMvc owner) {
        if (__freemarkerConfig == null) {
            synchronized (__LOCKER) {
                // 模板基准路径并以'/WEB-INF'开始，以'/'结束
                if (__baseViewPath == null) {
                    String _vPath = StringUtils.trimToNull(owner.getModuleCfg().getBaseViewPath());
                    if (!_vPath.endsWith("/")) {
                        _vPath += "/";
                    }
                    __baseViewPath = _vPath;
                }
                // 插件模板基准路径，以'/WEB-INF'开始，以'/'结束
                if (__pluginViewPath == null) {
                    String _pHome = StringUtils.trimToNull(owner.getModuleCfg().getPluginHome());
                    // 为了适应Web环境JSP文件的特殊性(即不能引用工程路径外的JSP文件), 建议采用默认"/WEB-INF/plugins/
                    if (!_pHome.endsWith("/")) {
                        _pHome += "/";
                    }
                    __pluginViewPath = _pHome;
                }
                // 初始化Freemarker模板引擎配置
                if (__freemarkerConfig == null) {
                    __freemarkerConfig = new Configuration(Configuration.VERSION_2_3_22);
                    __freemarkerConfig.setDefaultEncoding(owner.getModuleCfg().getDefaultCharsetEncoding());
                    __freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
                    //
                    List<TemplateLoader> _tmpLoaders = new ArrayList<TemplateLoader>();
                    try {
                        if (__baseViewPath.startsWith("/WEB-INF")) {
                            _tmpLoaders.add(new FileTemplateLoader(new File(RuntimeUtils.getRootPath(), StringUtils.substringAfter(__baseViewPath, "/WEB-INF/"))));
                        } else {
                            _tmpLoaders.add(new FileTemplateLoader(new File(__baseViewPath)));
                        }
                        //
                        if (__pluginViewPath.startsWith("/WEB-INF")) {
                            _tmpLoaders.add(new FileTemplateLoader(new File(RuntimeUtils.getRootPath(), StringUtils.substringAfter(__pluginViewPath, "/WEB-INF/"))));
                        } else {
                            _tmpLoaders.add(new FileTemplateLoader(new File(__pluginViewPath)));
                        }
                        //
                        __freemarkerConfig.setTemplateLoader(new MultiTemplateLoader(_tmpLoaders.toArray(new TemplateLoader[_tmpLoaders.size()])));
                    } catch (IOException e) {
                        throw new Error(RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
        }
    }
}
