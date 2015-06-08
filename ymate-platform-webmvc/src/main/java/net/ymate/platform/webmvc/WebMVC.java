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
package net.ymate.platform.webmvc;

import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.webmvc.annotation.Controller;
import net.ymate.platform.webmvc.annotation.FileUpload;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.handle.ControllerHandler;
import net.ymate.platform.webmvc.impl.DefaultModuleCfg;
import net.ymate.platform.webmvc.support.MultipartRequestWrapper;
import net.ymate.platform.webmvc.support.RequestExecutor;
import net.ymate.platform.webmvc.support.RequestMappingParser;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.impl.FreemarkerView;
import net.ymate.platform.webmvc.view.impl.HtmlView;
import net.ymate.platform.webmvc.view.impl.HttpStatusView;
import net.ymate.platform.webmvc.view.impl.JspView;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.reflect.Method;

/**
 * MVC框架管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-7 下午10:23:39
 * @version 1.0
 */
@Module
public class WebMVC implements IModule, IWebMvc {

    private static final Log _LOG = LogFactory.getLog(WebMVC.class);

    public static final Version VERSION = new Version(2, 0, 0, WebMVC.class.getPackage().getImplementationVersion(), Version.VersionType.Alphal);

    private static IWebMvc __instance;

    private YMP __owner;

    private IWebMvcModuleCfg __moduleCfg;

    private boolean __inited;

    private RequestMappingParser __mappingParser;

    /**
     * @return 返回默认MVC框架管理器实例对象
     */
    public static IWebMvc get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = YMP.get().getModule(WebMVC.class);
                }
            }
        }
        return __instance;
    }

    /**
     * @param owner YMP框架管理器实例
     * @return 返回指定YMP框架管理器容器内的MVC框架管理器实例
     */
    public static IWebMvc get(YMP owner) {
        return owner.getModule(WebMVC.class);
    }

    public void init(YMP owner) throws Exception {
        if (!__inited) {
            //
            _LOG.info("Initializing ymate-platform-webmvc-" + VERSION);
            //
            __owner = owner;
            __moduleCfg = new DefaultModuleCfg(owner);
            __mappingParser = new RequestMappingParser();
            __owner.getEvents().registerEvent(WebEvent.class);
            __owner.registerHandler(Controller.class, new ControllerHandler(__owner, this));
            //
            __inited = true;
        }
    }

    public boolean isInited() {
        return __inited;
    }

    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            __owner = null;
        }
    }

    public IWebMvcModuleCfg getModuleCfg() {
        return __moduleCfg;
    }

    public YMP getOwner() {
        return __owner;
    }

    public boolean registerController(Class<? extends Controller> targetClass) throws Exception {
        boolean _isValid = false;
        for (Method _method : targetClass.getDeclaredMethods()) {
            if (_method.isAnnotationPresent(RequestMapping.class)) {
                RequestMeta _meta = new RequestMeta(targetClass, _method);
                __mappingParser.registerRequestMeta(_meta);
                //
                _isValid = true;
            }
        }
        return _isValid;
    }

    public void processRequest(IRequestContext context,
                               ServletContext servletContext,
                               HttpServletRequest request,
                               HttpServletResponse response) throws Exception {

        RequestMeta _meta = __mappingParser.doParse(context);
        if (_meta != null) {
            // 先判断当前请求方式是否允许
            if (_meta.allowHttpMethod(context.getHttpMethod())) {
                // 判断是否需要处理文件上传
                if (context.getHttpMethod().equals(Type.HttpMethod.POST) && _meta.getMethod().isAnnotationPresent(FileUpload.class)) {
                    request = new MultipartRequestWrapper(this, request);
                }
                WebContext.create(this, context, servletContext, request, response);
                try {
                    IView _view = RequestExecutor.bind(this, _meta).execute();
                    if (_view != null) {
                        _view.render();
                    } else {
                        HttpStatusView.NOT_FOUND.render();
                    }
                } finally {
                    WebContext.destroy();
                }
            } else {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            }
        } else if (__moduleCfg.isConventionMode()) {
            boolean _isAllowConvention = false;
            if (!__moduleCfg.getConventionViewPaths().isEmpty()) {
                for (String _vPath : __moduleCfg.getConventionViewPaths()) {
                    if (_vPath.charAt(0) != '/') {
                        _vPath = "/" + _vPath;
                    }
                    if (context.getRequestMapping().startsWith(_vPath)) {
                        _isAllowConvention = true;
                        break;
                    }
                }
            } else {
                _isAllowConvention = true;
            }
            if (_isAllowConvention) {
                WebContext.create(this, context, servletContext, request, response);
                try {
                    IView _view = null;
                    if (__moduleCfg.getErrorProcessor() != null) {
                        _view = __moduleCfg.getErrorProcessor().onConvention(this, context);
                    }
                    if (_view == null) {
                        // 采用系统默认方式处理约定优于配置的URL请求映射
                        String[] _fileTypes = {".html", ".jsp", ".ftl"};
                        for (String _fileType : _fileTypes) {
                            File _targetFile = new File(__moduleCfg.getAbstractBaseViewPath(), context.getRequestMapping() + _fileType);
                            if (_targetFile.exists()) {
                                if (".html".equals(_fileType)) {
                                    _view = HtmlView.bind(this, context.getRequestMapping().substring(1));
                                    break;
                                } else if (".jsp".equals(_fileType)) {
                                    _view = JspView.bind(this, context.getRequestMapping().substring(1));
                                    break;
                                } else if (".ftl".equals(_fileType)) {
                                    _view = FreemarkerView.bind(this, context.getRequestMapping().substring(1));
                                    break;
                                }
                            }
                        }
                    }
                    if (_view != null) {
                        _view.render();
                    } else {
                        HttpStatusView.NOT_FOUND.render();
                    }
                } finally {
                    WebContext.destroy();
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
