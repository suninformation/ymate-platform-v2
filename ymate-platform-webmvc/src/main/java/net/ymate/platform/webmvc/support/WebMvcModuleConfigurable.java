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
package net.ymate.platform.webmvc.support;

import net.ymate.platform.core.support.IModuleConfigurable;
import net.ymate.platform.webmvc.*;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 19:32
 * @version 1.0
 * @since 2.0.6
 */
public class WebMvcModuleConfigurable implements IModuleConfigurable {

    public static WebMvcModuleConfigurable create() {
        return new WebMvcModuleConfigurable();
    }

    private Map<String, String> __configs = new HashMap<String, String>();

    public WebMvcModuleConfigurable requestMappingParserClass(Class<? extends IRequestMappingParser> requestMappingParserClass) {
        __configs.put(IWebMvcModuleCfg.REQUEST_MAPPING_PARSER_CLASS, requestMappingParserClass.getName());
        return this;
    }

    public WebMvcModuleConfigurable requestProcessorClass(String requestProcessorClass) {
        __configs.put(IWebMvcModuleCfg.REQUEST_PROCESSOR_CLASS, StringUtils.trimToEmpty(requestProcessorClass));
        return this;
    }

    public WebMvcModuleConfigurable requestProcessorClass(Class<? extends IRequestProcessor> requestProcessorClass) {
        __configs.put(IWebMvcModuleCfg.REQUEST_PROCESSOR_CLASS, requestProcessorClass.getName());
        return this;
    }

    public WebMvcModuleConfigurable errorProcessorClass(Class<? extends IWebErrorProcessor> errorProcessorClass) {
        __configs.put(IWebMvcModuleCfg.ERROR_PROCESSOR_CLASS, errorProcessorClass.getName());
        return this;
    }

    public WebMvcModuleConfigurable cacheProcessorClass(Class<? extends IWebCacheProcessor> cacheProcessorClass) {
        __configs.put(IWebMvcModuleCfg.CACHE_PROCESSOR_CLASS, cacheProcessorClass.getName());
        return this;
    }

    public WebMvcModuleConfigurable i18nResourcesHome(String i18nResourcesHome) {
        __configs.put(IWebMvcModuleCfg.I18N_RESOURCES_HOME, StringUtils.trimToEmpty(i18nResourcesHome));
        return this;
    }

    public WebMvcModuleConfigurable i18nResourceName(String i18nResourceName) {
        __configs.put(IWebMvcModuleCfg.I18N_RESOURCE_NAME, StringUtils.trimToEmpty(i18nResourceName));
        return this;
    }

    public WebMvcModuleConfigurable i18nLanguageParamName(String i18nLanguageParamName) {
        __configs.put(IWebMvcModuleCfg.I18N_LANGUAGE_PARAM_NAME, StringUtils.trimToEmpty(i18nLanguageParamName));
        return this;
    }

    public WebMvcModuleConfigurable defaultCharsetEncoding(String defaultCharsetEncoding) {
        __configs.put(IWebMvcModuleCfg.DEFAULT_CHARSET_ENCODING, StringUtils.trimToEmpty(defaultCharsetEncoding));
        return this;
    }

    public WebMvcModuleConfigurable defaultContentType(String defaultContentType) {
        __configs.put(IWebMvcModuleCfg.DEFAULT_CONTENT_TYPE, StringUtils.trimToEmpty(defaultContentType));
        return this;
    }

    public WebMvcModuleConfigurable requestIgnoreRegex(String requestIgnoreRegex) {
        __configs.put(IWebMvcModuleCfg.REQUEST_IGNORE_REGEX, StringUtils.trimToEmpty(requestIgnoreRegex));
        return this;
    }

    public WebMvcModuleConfigurable requestMethodParam(String requestMethodParam) {
        __configs.put(IWebMvcModuleCfg.REQUEST_METHOD_PARAM, StringUtils.trimToEmpty(requestMethodParam));
        return this;
    }

    public WebMvcModuleConfigurable requestPrefix(String requestPrefix) {
        __configs.put(IWebMvcModuleCfg.REQUEST_PREFIX, StringUtils.trimToEmpty(requestPrefix));
        return this;
    }

    public WebMvcModuleConfigurable parameterEscapeMode(boolean parameterEscapeMode) {
        __configs.put(IWebMvcModuleCfg.PARAMETER_ESCAPE_MODE, String.valueOf(parameterEscapeMode));
        return this;
    }

    public WebMvcModuleConfigurable parameterEscapeOrder(Type.EscapeOrder parameterEscapeOrder) {
        __configs.put(IWebMvcModuleCfg.PARAMETER_ESCAPE_ORDER, parameterEscapeOrder.name());
        return this;
    }

    public WebMvcModuleConfigurable baseViewPath(String baseViewPath) {
        __configs.put(IWebMvcModuleCfg.BASE_VIEW_PATH, StringUtils.trimToEmpty(baseViewPath));
        return this;
    }

    public WebMvcModuleConfigurable cookiePrefix(String cookiePrefix) {
        __configs.put(IWebMvcModuleCfg.COOKIE_PREFIX, StringUtils.trimToEmpty(cookiePrefix));
        return this;
    }

    public WebMvcModuleConfigurable cookieDomain(String cookieDomain) {
        __configs.put(IWebMvcModuleCfg.COOKIE_DOMAIN, StringUtils.trimToEmpty(cookieDomain));
        return this;
    }

    public WebMvcModuleConfigurable cookiePath(String cookiePath) {
        __configs.put(IWebMvcModuleCfg.COOKIE_PATH, StringUtils.trimToEmpty(cookiePath));
        return this;
    }

    public WebMvcModuleConfigurable cookieAuthKey(String cookieAuthKey) {
        __configs.put(IWebMvcModuleCfg.COOKIE_AUTH_KEY, StringUtils.trimToEmpty(cookieAuthKey));
        return this;
    }

    public WebMvcModuleConfigurable defaultEnabledCookieAuth(boolean defaultEnabledCookieAuth) {
        __configs.put(IWebMvcModuleCfg.DEFAULT_ENABLED_COOKIE_AUTH, String.valueOf(defaultEnabledCookieAuth));
        return this;
    }

    public WebMvcModuleConfigurable defaultUseHttpOnly(boolean defaultUseHttpOnly) {
        __configs.put(IWebMvcModuleCfg.DEFAULT_USE_HTTP_ONLY, String.valueOf(defaultUseHttpOnly));
        return this;
    }

    public WebMvcModuleConfigurable uploadTempDir(String uploadTempDir) {
        __configs.put(IWebMvcModuleCfg.UPLOAD_TEMP_DIR, StringUtils.trimToEmpty(uploadTempDir));
        return this;
    }

    public WebMvcModuleConfigurable uploadFileSizeMax(int uploadFileSizeMax) {
        __configs.put(IWebMvcModuleCfg.UPLOAD_FILE_SIZE_MAX, String.valueOf(uploadFileSizeMax));
        return this;
    }

    public WebMvcModuleConfigurable uploadTotalSizeMax(int uploadTotalSizeMax) {
        __configs.put(IWebMvcModuleCfg.UPLOAD_TOTAL_SIZE_MAX, String.valueOf(uploadTotalSizeMax));
        return this;
    }

    public WebMvcModuleConfigurable uploadSizeThreshold(int uploadSizeThreshold) {
        __configs.put(IWebMvcModuleCfg.UPLOAD_SIZE_THRESHOLD, String.valueOf(uploadSizeThreshold));
        return this;
    }

    public WebMvcModuleConfigurable uploadFileListenerClass(Class<? extends ProgressListener> uploadFileListenerClass) {
        __configs.put(IWebMvcModuleCfg.UPLOAD_FILE_LISTENER_CLASS, uploadFileListenerClass.getName());
        return this;
    }

    public WebMvcModuleConfigurable conventionMode(boolean conventionMode) {
        __configs.put(IWebMvcModuleCfg.CONVENTION_MODE, String.valueOf(conventionMode));
        return this;
    }

    public WebMvcModuleConfigurable conventionUrlrewriteMode(boolean conventionUrlrewriteMode) {
        __configs.put(IWebMvcModuleCfg.CONVENTION_URLREWRITE_MODE, String.valueOf(conventionUrlrewriteMode));
        return this;
    }

    public WebMvcModuleConfigurable conventionInterceptorMode(boolean conventionInterceptorMode) {
        __configs.put(IWebMvcModuleCfg.CONVENTION_INTERCEPTOR_MODE, String.valueOf(conventionInterceptorMode));
        return this;
    }

    public WebMvcModuleConfigurable conventionViewPaths(String conventionViewPaths) {
        __configs.put(IWebMvcModuleCfg.CONVENTION_VIEW_PATHS, StringUtils.trimToEmpty(conventionViewPaths));
        return this;
    }

    @Override
    public String getModuleName() {
        return IWebMvc.MODULE_NAME;
    }

    @Override
    public Map<String, String> toMap() {
        return __configs;
    }
}
