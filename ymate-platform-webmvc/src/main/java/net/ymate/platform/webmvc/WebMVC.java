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
package net.ymate.platform.webmvc;

import com.alibaba.fastjson.JSON;
import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.BeanMeta;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.validation.IValidation;
import net.ymate.platform.validation.Validations;
import net.ymate.platform.webmvc.annotation.Controller;
import net.ymate.platform.webmvc.annotation.FileUpload;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.annotation.ResponseCache;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.cors.CrossDomainAnnotationInterceptor;
import net.ymate.platform.webmvc.cors.annotation.CrossDomain;
import net.ymate.platform.webmvc.impl.DefaultInterceptorRuleProcessor;
import net.ymate.platform.webmvc.impl.DefaultWebMvcConfig;
import net.ymate.platform.webmvc.support.MultipartRequestWrapper;
import net.ymate.platform.webmvc.support.RequestExecutor;
import net.ymate.platform.webmvc.support.RequestParametersProxy;
import net.ymate.platform.webmvc.validate.*;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;
import net.ymate.platform.webmvc.view.impl.HttpStatusView;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * MVC框架管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-7 下午10:23:39
 */
public final class WebMVC implements IModule, IWebMvc {

    private static final Log LOG = LogFactory.getLog(WebMVC.class);

    private static volatile IWebMvc instance;

    private IApplication owner;

    private IWebMvcConfig config;

    private boolean initialized;

    private IInterceptorRuleProcessor interceptorRuleProcessor;

    /**
     * @return 返回默认MVC框架管理器实例对象
     */
    public static IWebMvc get() {
        IWebMvc inst = instance;
        if (inst == null) {
            synchronized (WebMVC.class) {
                inst = instance;
                if (instance == null) {
                    instance = inst = YMP.get().getModuleManager().getModule(WebMVC.class);
                }
            }
        }
        return inst;
    }

    public WebMVC() {
    }

    public WebMVC(IWebMvcConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return IWebMvc.MODULE_NAME;
    }

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized) {
            //
            YMP.showModuleVersion("ymate-platform-webmvc", this);
            //
            this.owner = owner;
            this.owner.getEvents().registerEvent(WebEvent.class);
            //
            if (config == null) {
                IModuleConfigurer moduleConfigurer = owner.getConfigurer().getModuleConfigurer(MODULE_NAME);
                config = moduleConfigurer == null ? DefaultWebMvcConfig.defaultConfig() : DefaultWebMvcConfig.create(moduleConfigurer);
            }
            //
            if (!config.isInitialized()) {
                config.initialize(this);
            }
            if (config.getErrorProcessor() instanceof IWebInitialization) {
                ((IWebInitialization) config.getErrorProcessor()).initialize(this);
            }
            if (config.isConventionInterceptorMode()) {
                interceptorRuleProcessor = new DefaultInterceptorRuleProcessor();
                interceptorRuleProcessor.initialize(this);
            }
            //
            if (config.getCrossDomainSettings().isEnabled()) {
                owner.getInterceptSettings().getInterceptAnnHelper().registerInterceptAnnotation(CrossDomain.class, CrossDomainAnnotationInterceptor.class);
            }
            //
            IProxyFactory proxyFactory = owner.getBeanFactory().getProxyFactory();
            if (proxyFactory != null) {
                proxyFactory.registerProxy(new RequestParametersProxy());
            }
            //
            IValidation validation = owner.getModuleManager().getModule(Validations.class);
            validation.registerValidator(VHostName.class, HostNameValidator.class);
            validation.registerValidator(VToken.class, TokenValidator.class);
            validation.registerValidator(VUploadFile.class, UploadFileValidator.class);
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            if (config.getErrorProcessor() instanceof IWebInitialization) {
                ((IWebInitialization) config.getErrorProcessor()).close();
            }
            //
            owner = null;
        }
    }

    @Override
    public IWebMvcConfig getConfig() {
        return config;
    }

    @Override
    public IApplication getOwner() {
        return owner;
    }

    @Override
    public boolean registerController(Class<?> targetClass) throws Exception {
        return registerController(null, targetClass);
    }

    @Override
    public boolean registerController(String requestMappingPrefix, Class<?> targetClass) throws Exception {
        boolean isValid = false;
        for (Method method : targetClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class) && method.getModifiers() == Modifier.PUBLIC) {
                RequestMeta requestMeta = new RequestMeta(requestMappingPrefix, targetClass, method);
                config.getRequestMappingParser().registerRequestMeta(requestMeta);
                //
                if (owner.isDevEnv() && LOG.isDebugEnabled()) {
                    LOG.debug(String.format("--> %s: %s : %s.%s", requestMeta.getAllowMethods(), requestMeta.getMapping(), requestMeta.getTargetClass().getName(), requestMeta.getMethod().getName()));
                }
                //
                isValid = true;
            }
        }
        //
        if (isValid) {
            Controller annotation = targetClass.getAnnotation(Controller.class);
            owner.getBeanFactory().registerBean(BeanMeta.create(targetClass, annotation == null || annotation.singleton()));
        }
        return isValid;
    }

    @Override
    public boolean registerInterceptorRule(Class<? extends IInterceptorRule> targetClass) throws Exception {
        if (interceptorRuleProcessor != null) {
            interceptorRuleProcessor.registerInterceptorRule(targetClass);
            return true;
        }
        return false;
    }

    private IWebCacheProcessor doGetWebCacheProcessor(ResponseCache responseCache) {
        IWebCacheProcessor cacheProcessor = null;
        if (responseCache != null) {
            if (!IWebCacheProcessor.class.equals(responseCache.processorClass())) {
                cacheProcessor = ClassUtils.impl(responseCache.processorClass(), IWebCacheProcessor.class);
            }
            if (cacheProcessor == null) {
                cacheProcessor = getConfig().getCacheProcessor();
            }
        }
        return cacheProcessor;
    }

    private void processRequestMeta(IRequestContext context, HttpServletRequest request, RequestMeta requestMeta, boolean devEnv) throws Exception {
        if (devEnv && LOG.isInfoEnabled()) {
            LOG.info("Request mode: controller");
        }
        // 判断是否需要处理文件上传
        if (context.getHttpMethod().equals(Type.HttpMethod.POST) && requestMeta.getMethod().isAnnotationPresent(FileUpload.class)) {
            if (!(request instanceof IMultipartRequestWrapper)) {
                // 避免重复处理
                request = new MultipartRequestWrapper(this, request);
            }
            //
            if (devEnv && LOG.isInfoEnabled()) {
                LOG.info("Include file upload: YES");
            }
        }
        WebContext.getContext().addAttribute(Type.Context.HTTP_REQUEST, request);
        //
        IWebCacheProcessor cacheProcessor = doGetWebCacheProcessor(requestMeta.getResponseCache());
        IView view = null;
        // 首先判断是否可以使用缓存
        if (cacheProcessor != null) {
            // 尝试从缓存中加载执行结果
            if (cacheProcessor.processResponseCache(this, requestMeta.getResponseCache(), context, null)) {
                // 加载成功, 则
                view = View.nullView();
                //
                if (devEnv && LOG.isInfoEnabled()) {
                    LOG.info("Load data from the cache: YES");
                }
            }
        }
        if (view == null) {
            view = RequestExecutor.bind(this, requestMeta).execute();
            if (view != null) {
                if (cacheProcessor != null) {
                    try {
                        // 生成缓存
                        if (cacheProcessor.processResponseCache(this, requestMeta.getResponseCache(), context, view)) {
                            view = View.nullView();
                            //
                            if (devEnv && LOG.isInfoEnabled()) {
                                LOG.info("Results data cached: YES");
                            }
                        }
                    } catch (Exception e) {
                        // 缓存处理过程中的任何异常都不能影响本交请求的正常响应, 仅输出异常日志
                        LOG.warn(e.getMessage(), RuntimeUtils.unwrapThrow(e));
                    }
                }
                view.render();
            } else {
                HttpStatusView.NOT_FOUND.render();
            }
        } else {
            view.render();
        }
    }

    private void processRequestConvention(IRequestContext context, boolean devEnv) throws Exception {
        if (devEnv && LOG.isInfoEnabled()) {
            LOG.info("Request mode: convention");
        }
        //
        IView view = null;
        ResponseCache responseCacheAnn = null;
        if (interceptorRuleProcessor != null) {
            // 尝试执行Convention拦截规则
            PairObject<IView, ResponseCache> processRequest = interceptorRuleProcessor.processRequest(this, context);
            view = processRequest.getKey();
            responseCacheAnn = processRequest.getValue();
        }
        // 判断是否可以使用缓存
        IWebCacheProcessor cacheProcessor = doGetWebCacheProcessor(responseCacheAnn);
        // 首先判断是否可以使用缓存
        if (cacheProcessor != null) {
            // 尝试从缓存中加载执行结果
            if (cacheProcessor.processResponseCache(this, responseCacheAnn, context, null)) {
                // 加载成功, 则
                view = View.nullView();
                //
                if (devEnv && LOG.isInfoEnabled()) {
                    LOG.info("Load data from the cache: YES");
                }
            }
        }
        if (view == null) {
            // 处理Convention模式下URL参数集合
            String requestMapping = context.getRequestMapping();
            String[] urlParamArr = this.config.isConventionUrlRewriteMode() ? StringUtils.split(requestMapping, '_') : new String[]{requestMapping};
            if (urlParamArr != null && urlParamArr.length > 1) {
                requestMapping = urlParamArr[0];
                List<String> urlParams = Arrays.asList(urlParamArr).subList(1, urlParamArr.length);
                WebContext.getRequest().setAttribute("UrlParams", urlParams);
                //
                if (devEnv && LOG.isInfoEnabled()) {
                    LOG.info("With parameters: " + urlParams);
                }
            }
            //
            if (config.getErrorProcessor() != null) {
                view = config.getErrorProcessor().onConvention(this, context);
            }
            if (view == null) {
                PairObject<IView, String> mappingView = View.mappingToView(this, requestMapping);
                view = mappingView.getKey();
                if (mappingView.getValue() != null && devEnv && LOG.isInfoEnabled()) {
                    LOG.info(String.format("Rendering template file: %s%s", requestMapping, mappingView.getValue()));
                }
            }
            //
            if (view != null && cacheProcessor != null) {
                try {
                    if (cacheProcessor.processResponseCache(this, responseCacheAnn, context, view)) {
                        view = View.nullView();
                        //
                        if (devEnv && LOG.isInfoEnabled()) {
                            LOG.info("Results data cached: YES");
                        }
                    }
                } catch (Exception e) {
                    // 缓存处理过程中的任何异常都不能影响本交请求的正常响应, 仅输出异常日志
                    LOG.warn(e.getMessage(), RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        if (view != null) {
            view.render();
        } else {
            HttpStatusView.NOT_FOUND.render();
        }
    }

    private boolean isAllowRequest(IRequestContext context, HttpServletResponse response, RequestMeta requestMeta, boolean devEnv) throws Exception {
        boolean flag = true;
        // 先判断当前请求方式是否允许
        if (requestMeta.allowHttpMethod(context.getHttpMethod())) {
            Map<String, String> allowMap = requestMeta.getAllowHeaders();
            for (Map.Entry<String, String> entry : allowMap.entrySet()) {
                String header = WebContext.getRequest().getHeader(entry.getKey());
                if (StringUtils.equals(entry.getValue(), "*")) {
                    if (StringUtils.isBlank(header)) {
                        if (devEnv && LOG.isInfoEnabled()) {
                            LOG.info("Check request allowed: NO");
                        }
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        flag = false;
                    }
                } else {
                    if (header == null || !header.equalsIgnoreCase(entry.getValue())) {
                        if (devEnv && LOG.isInfoEnabled()) {
                            LOG.info("Check request allowed: NO");
                        }
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        flag = false;
                    }
                }
            }
            // 判断允许的请求参数
            allowMap = requestMeta.getAllowParams();
            for (Map.Entry<String, String> entry : allowMap.entrySet()) {
                if (StringUtils.equals(entry.getValue(), "*")) {
                    if (!WebContext.getRequest().getParameterMap().containsKey(entry.getKey())) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        //
                        if (devEnv && LOG.isInfoEnabled()) {
                            LOG.info("Check request allowed: NO");
                        }
                        flag = false;
                    }
                } else {
                    String paramValue = WebContext.getRequest().getParameter(entry.getKey());
                    if (paramValue == null || !paramValue.equalsIgnoreCase(entry.getValue())) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                        //
                        if (devEnv && LOG.isInfoEnabled()) {
                            LOG.info("Check request allowed: NO");
                        }
                        flag = false;
                    }
                }
            }
        } else {
            flag = false;
        }
        return flag;
    }

    private boolean isAllowConvention(IRequestContext context) {
        boolean allowConvention = true;
        if (!config.getConventionViewNotAllowPaths().isEmpty()) {
            for (String path : config.getConventionViewNotAllowPaths()) {
                if (context.getRequestMapping().startsWith(path)) {
                    allowConvention = false;
                    break;
                }
            }
        }
        if (allowConvention && !config.getConventionViewAllowPaths().isEmpty()) {
            allowConvention = false;
            for (String path : config.getConventionViewAllowPaths()) {
                if (context.getRequestMapping().startsWith(path)) {
                    allowConvention = true;
                    break;
                }
            }
        }
        return allowConvention;
    }

    @Override
    public void processRequest(IRequestContext context,
                               ServletContext servletContext,
                               HttpServletRequest request,
                               HttpServletResponse response) throws Exception {

        StopWatch consumeTime = null;
        RequestMeta requestMeta = null;
        try {
            if (owner.isDevEnv() && LOG.isInfoEnabled()) {
                consumeTime = new StopWatch();
                consumeTime.start();
                //
                LOG.info(String.format("Process request start: %s:%s", context.getHttpMethod(), context.getRequestMapping()));
                LOG.info(String.format("Parameters: %s", JSON.toJSONString(request.getParameterMap())));
            }
            //
            requestMeta = config.getRequestMappingParser().parse(context);
            if (requestMeta != null) {
                if (isAllowRequest(context, response, requestMeta, owner.isDevEnv())) {
                    processRequestMeta(context, request, requestMeta, owner.isDevEnv());
                } else {
                    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                }
            } else if (config.isConventionMode() && isAllowConvention(context)) {
                processRequestConvention(context, owner.isDevEnv());
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            IView view = null;
            if (requestMeta != null && requestMeta.getErrorProcessor() != null) {
                IResponseErrorProcessor errorProcessor = ClassUtils.impl(requestMeta.getErrorProcessor(), IResponseErrorProcessor.class);
                if (errorProcessor != null) {
                    view = errorProcessor.processError(this, e);
                    if (owner.isDevEnv() && LOG.isInfoEnabled()) {
                        LOG.info(String.format("An exception processed with: %s", requestMeta.getErrorProcessor().getName()));
                    }
                }
            }
            if (view != null) {
                view.render();
            } else {
                throw e;
            }
        } finally {
            if (consumeTime != null && owner.isDevEnv() && LOG.isInfoEnabled()) {
                consumeTime.stop();
                LOG.info(String.format("Process request completed: %s:%s: %d, total execution time: %dms", context.getHttpMethod(), context.getRequestMapping(), response.getStatus(), consumeTime.getTime()));
            }
        }
    }
}
