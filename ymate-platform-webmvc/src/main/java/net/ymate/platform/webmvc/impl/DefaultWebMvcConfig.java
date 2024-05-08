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
package net.ymate.platform.webmvc.impl;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.webmvc.*;
import net.ymate.platform.webmvc.annotation.EnableConventionMode;
import net.ymate.platform.webmvc.annotation.EnableCrossDomainSettings;
import net.ymate.platform.webmvc.annotation.WebConf;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.cors.CrossDomainSettings;
import net.ymate.platform.webmvc.cors.impl.DefaultCrossDomainSetting;
import net.ymate.platform.webmvc.validate.IHostNameChecker;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

/**
 * 默认WebMVC模块配置接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 下午1:35
 */
public final class DefaultWebMvcConfig implements IWebMvcConfig {

    private IRequestMappingParser requestMappingParser;

    private IRequestProcessor requestProcessor;

    private IWebErrorProcessor errorProcessor;

    private IWebCacheProcessor cacheProcessor;

    private String resourceHome;

    private String resourceName;

    private String languageParamName;

    private String defaultCharsetEncoding;

    private String defaultContentType;

    private final Set<String> requestIgnoreSuffixes = new HashSet<>();

    private String requestMethodParam;

    private String requestPrefix;

    private boolean requestStrictModeEnabled;

    private String baseViewPath;

    private String abstractBaseViewPath;

    private String cookiePrefix;

    private String cookieDomain;

    private String cookiePath;

    private String cookieAuthKey;

    private boolean cookieAuthEnabled;

    private boolean cookieUseHttpOnly;

    private String uploadTempDir;

    private long uploadFileCountMax;

    private long uploadFileSizeMax;

    private long uploadTotalSizeMax;

    private int uploadSizeThreshold;

    private ProgressListener uploadListener;

    private boolean conventionMode;

    private boolean conventionUrlRewriteMode;

    private boolean conventionInterceptorMode;

    private final Set<String> conventionViewAllowPaths = new HashSet<>();

    private final Set<String> conventionViewNotAllowPaths = new HashSet<>();

    private final CrossDomainSettings crossDomainSettings = new CrossDomainSettings();

    private boolean initialized;

    public static DefaultWebMvcConfig defaultConfig() {
        return builder().build();
    }

    public static DefaultWebMvcConfig create(IModuleConfigurer moduleConfigurer) {
        return new DefaultWebMvcConfig(null, moduleConfigurer);
    }

    public static DefaultWebMvcConfig create(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        return new DefaultWebMvcConfig(mainClass, moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultWebMvcConfig() {
    }

    private DefaultWebMvcConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        WebConf confAnn = mainClass == null ? null : mainClass.getAnnotation(WebConf.class);
        //
        String mappingParserClassName = configReader.getString(REQUEST_MAPPING_PARSER_CLASS, StringUtils.defaultIfBlank(confAnn != null && !confAnn.mappingParserClass().equals(IRequestMappingParser.class) ? confAnn.mappingParserClass().getName() : null, DEFAULT_STR));
        Class<? extends IRequestMappingParser> mappingParserClass = Type.REQUEST_MAPPING_PARSERS.get(mappingParserClassName);
        if (mappingParserClass == null && StringUtils.isNotBlank(mappingParserClassName)) {
            requestMappingParser = ClassUtils.impl(mappingParserClassName, IRequestMappingParser.class, this.getClass());
        } else if (mappingParserClass != null) {
            requestMappingParser = ClassUtils.impl(mappingParserClass, IRequestMappingParser.class);
        }
        //
        String requestProcessorClassName = configReader.getString(REQUEST_PROCESSOR_CLASS, StringUtils.defaultIfBlank(confAnn != null && !confAnn.requestProcessClass().equals(IRequestProcessor.class) ? confAnn.requestProcessClass().getName() : null, DEFAULT_STR));
        Class<? extends IRequestProcessor> requestProcessorClass = Type.REQUEST_PROCESSORS.get(requestProcessorClassName);
        if (requestProcessorClass == null && StringUtils.isNotBlank(requestProcessorClassName)) {
            requestProcessor = ClassUtils.impl(requestProcessorClassName, IRequestProcessor.class, this.getClass());
        } else if (requestProcessorClass != null) {
            requestProcessor = ClassUtils.impl(requestProcessorClass, IRequestProcessor.class);
        }
        //
        errorProcessor = configReader.getClassImpl(ERROR_PROCESSOR_CLASS, confAnn != null && !confAnn.errorProcessorClass().equals(IWebErrorProcessor.class) ? confAnn.errorProcessorClass().getName() : null, IWebErrorProcessor.class);
        cacheProcessor = configReader.getClassImpl(CACHE_PROCESSOR_CLASS, confAnn == null || confAnn.cacheProcessorClass().equals(IWebCacheProcessor.class) ? null : confAnn.cacheProcessorClass().getName(), IWebCacheProcessor.class);
        //
        resourceHome = configReader.getString(RESOURCES_HOME, confAnn == null ? null : confAnn.resourceHome());
        resourceName = configReader.getString(RESOURCE_NAME, confAnn == null ? null : confAnn.resourceName());
        languageParamName = configReader.getString(LANGUAGE_PARAM_NAME, confAnn == null ? null : confAnn.languageParamName());
        //
        defaultCharsetEncoding = configReader.getString(DEFAULT_CHARSET_ENCODING, confAnn == null ? null : confAnn.defaultCharsetEncoding());
        defaultContentType = configReader.getString(DEFAULT_CONTENT_TYPE, confAnn == null ? null : confAnn.defaultContentType());
        //
        List<String> reqIgnoreSuffix = new ArrayList<>(Arrays.asList(configReader.getArray(REQUEST_IGNORE_SUFFIX, confAnn != null ? confAnn.requestIgnoreSuffixes() : new String[0])));
        if (!reqIgnoreSuffix.isEmpty() && StringUtils.equals(reqIgnoreSuffix.get(0), "~")) {
            if (reqIgnoreSuffix.size() > 1) {
                requestIgnoreSuffixes.addAll(Arrays.asList(StringUtils.split(IGNORE_REGEX_DEFAULT, "|")));
            }
            reqIgnoreSuffix.remove(0);
        }
        requestIgnoreSuffixes.addAll(reqIgnoreSuffix);
        requestMethodParam = configReader.getString(REQUEST_METHOD_PARAM, confAnn == null ? null : confAnn.requestMethodParam());
        requestPrefix = configReader.getString(REQUEST_PREFIX, confAnn == null ? null : confAnn.requestPrefix());
        requestStrictModeEnabled = configReader.getBoolean(REQUEST_STRICT_MODE_ENABLED, confAnn != null && confAnn.requestStrictModeEnabled());
        //
        baseViewPath = configReader.getString(BASE_VIEW_PATH, confAnn == null ? null : confAnn.baseViewPath());
        //
        cookiePrefix = configReader.getString(COOKIE_PREFIX, confAnn == null ? null : confAnn.cookiePrefix());
        cookieDomain = configReader.getString(COOKIE_DOMAIN, confAnn == null ? null : confAnn.cookieDomain());
        cookiePath = configReader.getString(COOKIE_PATH, confAnn == null ? null : confAnn.cookiePath());
        cookieAuthKey = configReader.getString(COOKIE_AUTH_KEY, confAnn == null ? null : confAnn.cookieAuthKey());
        cookieAuthEnabled = configReader.getBoolean(COOKIE_AUTH_ENABLED, confAnn != null && confAnn.cookieAuthEnabled());
        cookieUseHttpOnly = configReader.getBoolean(COOKIE_USE_HTTP_ONLY, confAnn != null && confAnn.cookieUseHttpOnly());
        //
        uploadTempDir = configReader.getString(UPLOAD_TEMP_DIR, confAnn == null ? null : confAnn.uploadTempDir());
        uploadFileCountMax = configReader.getLong(UPLOAD_FILE_COUNT_MAX, confAnn == null ? 0 : confAnn.uploadFileCountMax());
        uploadFileSizeMax = configReader.getLong(UPLOAD_FILE_SIZE_MAX, confAnn == null ? 0 : confAnn.uploadFileSizeMax());
        uploadTotalSizeMax = configReader.getLong(UPLOAD_TOTAL_SIZE_MAX, confAnn == null ? 0 : confAnn.uploadTotalSizeMax());
        uploadSizeThreshold = configReader.getInt(UPLOAD_SIZE_THRESHOLD, confAnn == null ? 0 : confAnn.uploadSizeThreshold());
        uploadListener = configReader.getClassImpl(UPLOAD_LISTENER_CLASS, confAnn == null || confAnn.uploadListenerClass().equals(ProgressListener.class) ? null : confAnn.uploadListenerClass().getName(), ProgressListener.class);
        //
        EnableConventionMode conventionModeAnn = mainClass == null ? null : mainClass.getAnnotation(EnableConventionMode.class);
        conventionMode = configReader.getBoolean(CONVENTION_MODE, conventionModeAnn != null);
        if (conventionMode) {
            conventionUrlRewriteMode = configReader.getBoolean(CONVENTION_URL_REWRITE_MODE, conventionModeAnn != null && conventionModeAnn.urlRewriteMode());
            conventionInterceptorMode = configReader.getBoolean(CONVENTION_INTERCEPTOR_MODE, conventionModeAnn != null && conventionModeAnn.interceptorMode());
            //
            String[] viewPaths = configReader.getArray(CONVENTION_VIEW_PATHS);
            if (ArrayUtils.isNotEmpty(viewPaths)) {
                parseConventionViewPaths(viewPaths);
            } else if (conventionModeAnn != null) {
                conventionViewAllowPaths.addAll(Arrays.asList(conventionModeAnn.viewAllowPaths()));
                conventionViewNotAllowPaths.addAll(Arrays.asList(conventionModeAnn.viewNotAllowPaths()));
            }
        }
        //
        EnableCrossDomainSettings crossDomainSettingsAnn = mainClass == null ? null : mainClass.getAnnotation(EnableCrossDomainSettings.class);
        crossDomainSettings.setEnabled(configReader.getBoolean(CROSS_DOMAIN_SETTINGS_ENABLED, crossDomainSettingsAnn != null));
        if (crossDomainSettings.isEnabled()) {
            DefaultCrossDomainSetting defaultSetting = crossDomainSettings.getDefaultSetting();
            defaultSetting.setOptionsAutoReply(configReader.getBoolean(CROSS_DOMAIN_OPTIONS_AUTO_REPLY, crossDomainSettingsAnn != null && crossDomainSettingsAnn.optionsAutoReply()));
            defaultSetting.setAllowedCredentials(configReader.getBoolean(CROSS_DOMAIN_ALLOWED_CREDENTIALS, crossDomainSettingsAnn != null && crossDomainSettingsAnn.allowedCredentials()));
            defaultSetting.setMaxAge(configReader.getLong(CROSS_DOMAIN_MAX_AGE, crossDomainSettingsAnn == null ? 0 : crossDomainSettingsAnn.maxAge()));
            //
            defaultSetting.setAllowedOriginsChecker(configReader.getClassImpl(CROSS_DOMAIN_ALLOWED_ORIGINS_CHECKER_CLASS, crossDomainSettingsAnn != null ? crossDomainSettingsAnn.allowedOriginsChecker().getName() : null, IHostNameChecker.class));
            defaultSetting.addAllowedOrigin(configReader.getArray(CROSS_DOMAIN_ALLOWED_ORIGINS, crossDomainSettingsAnn != null ? crossDomainSettingsAnn.allowedOrigins() : null));
            defaultSetting.addAllowedMethod(configReader.getArray(CROSS_DOMAIN_ALLOWED_METHODS, crossDomainSettingsAnn != null ? Arrays.stream(crossDomainSettingsAnn.allowedMethods()).map(Enum::name).toArray(String[]::new) : null));
            defaultSetting.addAllowedHeader(configReader.getArray(CROSS_DOMAIN_ALLOWED_HEADERS, crossDomainSettingsAnn != null ? crossDomainSettingsAnn.allowedHeaders() : null));
            defaultSetting.addExposedHeader(configReader.getArray(CROSS_DOMAIN_EXPOSED_HEADERS, crossDomainSettingsAnn != null ? crossDomainSettingsAnn.exposedHeaders() : null));
        }
    }

    private void parseConventionViewPaths(String[] conventionViewPaths) {
        if (conventionViewPaths != null) {
            for (String viewPath : conventionViewPaths) {
                viewPath = StringUtils.trimToNull(viewPath);
                if (viewPath != null) {
                    boolean allowFlag = true;
                    if (viewPath.length() > 1) {
                        int lostPosition = viewPath.length() - 1;
                        char lastChar = viewPath.charAt(lostPosition);
                        if (lastChar == '+') {
                            viewPath = StringUtils.substring(viewPath, 0, lostPosition);
                        } else if (lastChar == '-') {
                            viewPath = StringUtils.substring(viewPath, 0, lostPosition);
                            allowFlag = false;
                        }
                    }
                    if (viewPath.charAt(0) != Type.Const.PATH_SEPARATOR_CHAR) {
                        viewPath = Type.Const.PATH_SEPARATOR + viewPath;
                    }
                    if (allowFlag) {
                        conventionViewAllowPaths.add(viewPath);
                    } else {
                        conventionViewNotAllowPaths.add(viewPath);
                    }
                }
            }
        }
    }

    @Override
    public void initialize(IWebMvc owner) throws Exception {
        if (!initialized) {
            if (requestMappingParser == null) {
                requestMappingParser = ClassUtils.loadClass(IRequestMappingParser.class, DefaultRequestMappingParser.class);
            }
            if (requestProcessor == null) {
                requestProcessor = ClassUtils.loadClass(IRequestProcessor.class, DefaultRequestProcessor.class);
            }
            if (cacheProcessor == null) {
                cacheProcessor = ClassUtils.loadClass(IWebCacheProcessor.class, DefaultWebCacheProcessor.class);
            }
            if (errorProcessor == null) {
                errorProcessor = ClassUtils.loadClass(IWebErrorProcessor.class, DefaultWebErrorProcessor.class);
            }
            if (errorProcessor instanceof IWebInitialization) {
                ((IWebInitialization) errorProcessor).initialize(owner);
            }
            resourceHome = RuntimeUtils.replaceEnvVariable(StringUtils.defaultIfBlank(resourceHome, "${root}/i18n/"));
            resourceName = StringUtils.defaultIfBlank(resourceName, "messages");
            languageParamName = StringUtils.defaultIfBlank(languageParamName, "_lang");
            //
            defaultCharsetEncoding = StringUtils.defaultIfBlank(defaultCharsetEncoding, "UTF-8");
            defaultContentType = StringUtils.defaultIfBlank(defaultContentType, Type.ContentType.HTML.getContentType());
            //
            requestMethodParam = StringUtils.defaultIfBlank(requestMethodParam, "_method");
            requestPrefix = StringUtils.trimToEmpty(requestPrefix);
            //
            abstractBaseViewPath = baseViewPath = RuntimeUtils.replaceEnvVariable(StringUtils.defaultIfBlank(baseViewPath, "/WEB-INF/templates/"));
            if (abstractBaseViewPath.startsWith(Type.Const.WEB_INF_PREFIX)) {
                abstractBaseViewPath = new File(RuntimeUtils.getRootPath(false), abstractBaseViewPath).getPath();
            }
            //
            cookiePrefix = StringUtils.trimToEmpty(cookiePrefix);
            cookieDomain = StringUtils.trimToEmpty(cookieDomain);
            cookiePath = StringUtils.defaultIfBlank(cookiePath, Type.Const.PATH_SEPARATOR);
            cookieAuthKey = StringUtils.trimToEmpty(cookieAuthKey);
            //
            uploadTempDir = RuntimeUtils.replaceEnvVariable(uploadTempDir);
            uploadFileCountMax = uploadFileCountMax > 0 ? uploadFileCountMax : -1;
            uploadFileSizeMax = uploadFileSizeMax > 0 ? uploadFileSizeMax : -1;
            uploadTotalSizeMax = uploadTotalSizeMax > 0 ? uploadTotalSizeMax : -1;
            uploadSizeThreshold = uploadSizeThreshold > 0 ? uploadSizeThreshold : DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD;
            //
            if (!crossDomainSettings.isInitialized()) {
                crossDomainSettings.initialize(owner);
            }
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public IRequestMappingParser getRequestMappingParser() {
        return requestMappingParser;
    }

    public void setRequestMappingParser(IRequestMappingParser mappingParser) {
        if (!initialized) {
            this.requestMappingParser = mappingParser;
        }
    }

    @Override
    public IRequestProcessor getRequestProcessor() {
        return requestProcessor;
    }

    public void setRequestProcessor(IRequestProcessor requestProcessor) {
        if (!initialized) {
            this.requestProcessor = requestProcessor;
        }
    }

    @Override
    public IWebErrorProcessor getErrorProcessor() {
        return errorProcessor;
    }

    public void setErrorProcessor(IWebErrorProcessor errorProcessor) {
        if (!initialized) {
            this.errorProcessor = errorProcessor;
        }
    }

    @Override
    public IWebCacheProcessor getCacheProcessor() {
        return cacheProcessor;
    }

    public void setCacheProcessor(IWebCacheProcessor cacheProcessor) {
        if (!initialized) {
            this.cacheProcessor = cacheProcessor;
        }
    }

    @Override
    public String getResourceHome() {
        return resourceHome;
    }

    public void setResourceHome(String resourceHome) {
        if (!initialized) {
            this.resourceHome = resourceHome;
        }
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        if (!initialized) {
            this.resourceName = resourceName;
        }
    }

    @Override
    public String getLanguageParamName() {
        return languageParamName;
    }

    public void setLanguageParamName(String languageParamName) {
        if (!initialized) {
            this.languageParamName = languageParamName;
        }
    }

    @Override
    public String getDefaultCharsetEncoding() {
        return defaultCharsetEncoding;
    }

    public void setDefaultCharsetEncoding(String defaultCharsetEncoding) {
        if (!initialized) {
            this.defaultCharsetEncoding = defaultCharsetEncoding;
        }
    }

    @Override
    public String getDefaultContentType() {
        return defaultContentType;
    }

    @Override
    public Set<String> getRequestIgnoreSuffixes() {
        return requestIgnoreSuffixes;
    }

    public void setDefaultContentType(String defaultContentType) {
        if (!initialized) {
            this.defaultContentType = defaultContentType;
        }
    }

    public void addRequestIgnoreSuffix(String requestIgnoreSuffix) {
        if (!initialized) {
            this.requestIgnoreSuffixes.add(requestIgnoreSuffix);
        }
    }

    @Override
    public String getRequestMethodParam() {
        return requestMethodParam;
    }

    public void setRequestMethodParam(String requestMethodParam) {
        if (!initialized) {
            this.requestMethodParam = requestMethodParam;
        }
    }

    @Override
    public String getRequestPrefix() {
        return requestPrefix;
    }

    public void setRequestPrefix(String requestPrefix) {
        if (!initialized) {
            this.requestPrefix = requestPrefix;
        }
    }

    @Override
    public boolean isRequestStrictModeEnabled() {
        return requestStrictModeEnabled;
    }

    public void setRequestStrictModeEnabled(boolean requestStrictModeEnabled) {
        if (!initialized) {
            this.requestStrictModeEnabled = requestStrictModeEnabled;
        }
    }

    @Override
    public String getBaseViewPath() {
        return baseViewPath;
    }

    public void setBaseViewPath(String baseViewPath) {
        if (!initialized) {
            this.baseViewPath = baseViewPath;
        }
    }

    @Override
    public String getAbstractBaseViewPath() {
        return abstractBaseViewPath;
    }

    public void setAbstractBaseViewPath(String abstractBaseViewPath) {
        if (!initialized) {
            this.abstractBaseViewPath = abstractBaseViewPath;
        }
    }

    @Override
    public String getCookiePrefix() {
        return cookiePrefix;
    }

    public void setCookiePrefix(String cookiePrefix) {
        if (!initialized) {
            this.cookiePrefix = cookiePrefix;
        }
    }

    @Override
    public String getCookieDomain() {
        return cookieDomain;
    }

    public void setCookieDomain(String cookieDomain) {
        if (!initialized) {
            this.cookieDomain = cookieDomain;
        }
    }

    @Override
    public String getCookiePath() {
        return cookiePath;
    }

    public void setCookiePath(String cookiePath) {
        if (!initialized) {
            this.cookiePath = cookiePath;
        }
    }

    @Override
    public String getCookieAuthKey() {
        return cookieAuthKey;
    }

    public void setCookieAuthKey(String cookieAuthKey) {
        if (!initialized) {
            this.cookieAuthKey = cookieAuthKey;
        }
    }

    @Override
    public boolean isCookieAuthEnabled() {
        return cookieAuthEnabled;
    }

    public void setCookieAuthEnabled(boolean cookieAuthEnabled) {
        if (!initialized) {
            this.cookieAuthEnabled = cookieAuthEnabled;
        }
    }

    @Override
    public boolean isCookieUseHttpOnly() {
        return cookieUseHttpOnly;
    }

    public void setCookieUseHttpOnly(boolean cookieUseHttpOnly) {
        if (!initialized) {
            this.cookieUseHttpOnly = cookieUseHttpOnly;
        }
    }

    @Override
    public String getUploadTempDir() {
        return uploadTempDir;
    }

    public void setUploadTempDir(String uploadTempDir) {
        if (!initialized) {
            this.uploadTempDir = uploadTempDir;
        }
    }

    @Override
    public long getUploadFileCountMax() {
        return uploadFileCountMax;
    }

    public void setUploadFileCountMax(long uploadFileCountMax) {
        if (!initialized) {
            this.uploadFileCountMax = uploadFileCountMax;
        }
    }

    @Override
    public long getUploadFileSizeMax() {
        return uploadFileSizeMax;
    }

    public void setUploadFileSizeMax(long uploadFileSizeMax) {
        if (!initialized) {
            this.uploadFileSizeMax = uploadFileSizeMax;
        }
    }

    @Override
    public long getUploadTotalSizeMax() {
        return uploadTotalSizeMax;
    }

    public void setUploadTotalSizeMax(long uploadTotalSizeMax) {
        if (!initialized) {
            this.uploadTotalSizeMax = uploadTotalSizeMax;
        }
    }

    @Override
    public int getUploadSizeThreshold() {
        return uploadSizeThreshold;
    }

    public void setUploadSizeThreshold(int uploadSizeThreshold) {
        if (!initialized) {
            this.uploadSizeThreshold = uploadSizeThreshold;
        }
    }

    @Override
    public ProgressListener getUploadListener() {
        return uploadListener;
    }

    public void setUploadListener(ProgressListener uploadListener) {
        if (!initialized) {
            this.uploadListener = uploadListener;
        }
    }

    @Override
    public boolean isConventionMode() {
        return conventionMode;
    }

    public void setConventionMode(boolean conventionMode) {
        if (!initialized) {
            this.conventionMode = conventionMode;
        }
    }

    @Override
    public boolean isConventionUrlRewriteMode() {
        return conventionUrlRewriteMode;
    }

    public void setConventionUrlRewriteMode(boolean conventionUrlRewriteMode) {
        if (!initialized) {
            this.conventionUrlRewriteMode = conventionUrlRewriteMode;
        }
    }

    @Override
    public boolean isConventionInterceptorMode() {
        return conventionInterceptorMode;
    }

    public void setConventionInterceptorMode(boolean conventionInterceptorMode) {
        if (!initialized) {
            this.conventionInterceptorMode = conventionInterceptorMode;
        }
    }

    @Override
    public Set<String> getConventionViewAllowPaths() {
        return Collections.unmodifiableSet(conventionViewAllowPaths);
    }

    @Override
    public Set<String> getConventionViewNotAllowPaths() {
        return Collections.unmodifiableSet(conventionViewNotAllowPaths);
    }

    public void addConventionViewAllowPath(String conventionViewAllowPath) {
        if (!initialized && StringUtils.isNotBlank(conventionViewAllowPath)) {
            conventionViewAllowPaths.add(conventionViewAllowPath);
        }
    }

    public void addConventionViewNotAllowPath(String conventionViewNotAllowPath) {
        if (!initialized && StringUtils.isNotBlank(conventionViewNotAllowPath)) {
            conventionViewNotAllowPaths.add(conventionViewNotAllowPath);
        }
    }

    @Override
    public CrossDomainSettings getCrossDomainSettings() {
        return crossDomainSettings;
    }

    public void setCrossDomainSettingsEnabled(boolean crossDomainSettingsEnabled) {
        if (!initialized) {
            crossDomainSettings.setEnabled(crossDomainSettingsEnabled);
        }
    }

    public void setCrossDomainOptionsAutoReply(boolean crossDomainOptionsAutoReply) {
        if (!initialized) {
            crossDomainSettings.getDefaultSetting().setOptionsAutoReply(crossDomainOptionsAutoReply);
        }
    }

    public void setCrossDomainAllowedCredentials(boolean crossDomainAllowedCredentials) {
        if (!initialized) {
            crossDomainSettings.getDefaultSetting().setAllowedCredentials(crossDomainAllowedCredentials);
        }
    }

    public void addCrossDomainAllowedOrigin(String crossDomainAllowedOrigin) {
        if (!initialized && StringUtils.isNotBlank(crossDomainAllowedOrigin)) {
            crossDomainSettings.getDefaultSetting().addAllowedOrigin(crossDomainAllowedOrigin);
        }
    }

    public void addCrossDomainAllowedMethod(String crossDomainAllowedMethod) {
        if (!initialized && StringUtils.isNotBlank(crossDomainAllowedMethod)) {
            crossDomainSettings.getDefaultSetting().addAllowedMethod(crossDomainAllowedMethod);
        }
    }

    public void addCrossDomainAllowedHeader(String crossDomainAllowedHeader) {
        if (!initialized && StringUtils.isNotBlank(crossDomainAllowedHeader)) {
            crossDomainSettings.getDefaultSetting().addAllowedHeader(crossDomainAllowedHeader);
        }
    }

    public void setCrossDomainMaxAge(long crossDomainMaxAge) {
        if (!initialized) {
            crossDomainSettings.getDefaultSetting().setMaxAge(crossDomainMaxAge);
        }
    }

    public static final class Builder {

        private final DefaultWebMvcConfig config = new DefaultWebMvcConfig();

        private Builder() {
        }

        public Builder requestMappingParser(IRequestMappingParser requestMappingParser) {
            config.setRequestMappingParser(requestMappingParser);
            return this;
        }

        public Builder requestProcessor(IRequestProcessor requestProcessor) {
            config.setRequestProcessor(requestProcessor);
            return this;
        }

        public Builder errorProcessor(IWebErrorProcessor errorProcessor) {
            config.setErrorProcessor(errorProcessor);
            return this;
        }

        public Builder cacheProcessor(IWebCacheProcessor cacheProcessor) {
            config.setCacheProcessor(cacheProcessor);
            return this;
        }

        public Builder resourceHome(String resourceHome) {
            config.setResourceHome(resourceHome);
            return this;
        }

        public Builder resourceName(String resourceName) {
            config.setResourceName(resourceName);
            return this;
        }

        public Builder languageParamName(String languageParamName) {
            config.setLanguageParamName(languageParamName);
            return this;
        }

        public Builder defaultCharsetEncoding(String defaultCharsetEncoding) {
            config.setDefaultCharsetEncoding(defaultCharsetEncoding);
            return this;
        }

        public Builder defaultContentType(String defaultContentType) {
            config.setDefaultContentType(defaultContentType);
            return this;
        }

        public Builder addRequestIgnoreSuffix(String... requestIgnoreSuffixes) {
            if (requestIgnoreSuffixes != null && requestIgnoreSuffixes.length > 0) {
                Arrays.stream(requestIgnoreSuffixes).forEach(config::addRequestIgnoreSuffix);
            }
            return this;
        }

        public Builder requestMethodParam(String requestMethodParam) {
            config.setRequestMethodParam(requestMethodParam);
            return this;
        }

        public Builder requestPrefix(String requestPrefix) {
            config.setRequestPrefix(requestPrefix);
            return this;
        }

        public Builder requestStrictModeEnabled(boolean requestStrictModeEnabled) {
            config.setRequestStrictModeEnabled(requestStrictModeEnabled);
            return this;
        }

        public Builder baseViewPath(String baseViewPath) {
            config.setBaseViewPath(baseViewPath);
            return this;
        }

        public Builder abstractBaseViewPath(String abstractBaseViewPath) {
            config.setAbstractBaseViewPath(abstractBaseViewPath);
            return this;
        }

        public Builder cookiePrefix(String cookiePrefix) {
            config.setCookiePrefix(cookiePrefix);
            return this;
        }

        public Builder cookieDomain(String cookieDomain) {
            config.setCookieDomain(cookieDomain);
            return this;
        }

        public Builder cookiePath(String cookiePath) {
            config.setCookiePath(cookiePath);
            return this;
        }

        public Builder cookieAuthKey(String cookieAuthKey) {
            config.setCookieAuthKey(cookieAuthKey);
            return this;
        }

        public Builder cookieAuthEnabled(boolean cookieAuthEnabled) {
            config.setCookieAuthEnabled(cookieAuthEnabled);
            return this;
        }

        public Builder cookieUseHttpOnly(boolean cookieUseHttpOnly) {
            config.setCookieUseHttpOnly(cookieUseHttpOnly);
            return this;
        }

        public Builder uploadTempDir(String uploadTempDir) {
            config.setUploadTempDir(uploadTempDir);
            return this;
        }

        public Builder uploadFileCountMax(long uploadFileCountMax) {
            config.setUploadFileSizeMax(uploadFileCountMax);
            return this;
        }

        public Builder uploadFileSizeMax(long uploadFileSizeMax) {
            config.setUploadFileSizeMax(uploadFileSizeMax);
            return this;
        }

        public Builder uploadTotalSizeMax(long uploadTotalSizeMax) {
            config.setUploadTotalSizeMax(uploadTotalSizeMax);
            return this;
        }

        public Builder uploadSizeThreshold(int uploadSizeThreshold) {
            config.setUploadSizeThreshold(uploadSizeThreshold);
            return this;
        }

        public Builder uploadListener(ProgressListener uploadListener) {
            config.setUploadListener(uploadListener);
            return this;
        }

        public Builder conventionMode(boolean conventionMode) {
            config.setConventionMode(conventionMode);
            return this;
        }

        public Builder conventionUrlRewriteMode(boolean conventionUrlRewriteMode) {
            config.setConventionUrlRewriteMode(conventionUrlRewriteMode);
            return this;
        }

        public Builder conventionInterceptorMode(boolean conventionInterceptorMode) {
            config.setConventionInterceptorMode(conventionInterceptorMode);
            return this;
        }

        public Builder addConventionViewAllowPath(String... conventionViewAllowPaths) {
            if (ArrayUtils.isNotEmpty(conventionViewAllowPaths)) {
                Arrays.stream(conventionViewAllowPaths).forEach(config::addConventionViewAllowPath);
            }
            return this;
        }

        public Builder addConventionViewNotAllowPath(String... conventionViewNotAllowPaths) {
            if (ArrayUtils.isNotEmpty(conventionViewNotAllowPaths)) {
                Arrays.stream(conventionViewNotAllowPaths).forEach(config::addConventionViewNotAllowPath);
            }
            return this;
        }

        public Builder crossDomainSettingsEnabled(boolean crossDomainSettingsEnabled) {
            config.setCrossDomainSettingsEnabled(crossDomainSettingsEnabled);
            return this;
        }

        public Builder crossDomainOptionsAutoReply(boolean crossDomainOptionsAutoReply) {
            config.setCrossDomainOptionsAutoReply(crossDomainOptionsAutoReply);
            return this;
        }

        public Builder crossDomainAllowedCredentials(boolean crossDomainAllowedCredentials) {
            config.setCrossDomainAllowedCredentials(crossDomainAllowedCredentials);
            return this;
        }

        public Builder addCrossDomainAllowedOrigin(String... crossDomainAllowedOrigins) {
            if (ArrayUtils.isNotEmpty(crossDomainAllowedOrigins)) {
                Arrays.stream(crossDomainAllowedOrigins).forEach(config::addCrossDomainAllowedOrigin);
            }
            return this;
        }

        public Builder addCrossDomainAllowedMethod(String... crossDomainAllowedMethods) {
            if (ArrayUtils.isNotEmpty(crossDomainAllowedMethods)) {
                Arrays.stream(crossDomainAllowedMethods).forEach(config::addCrossDomainAllowedMethod);
            }
            return this;
        }

        public Builder addCrossDomainAllowedHeader(String... crossDomainAllowedHeaders) {
            if (ArrayUtils.isNotEmpty(crossDomainAllowedHeaders)) {
                Arrays.stream(crossDomainAllowedHeaders).forEach(config::addCrossDomainAllowedHeader);
            }
            return this;
        }

        public Builder crossDomainMaxAge(long crossDomainMaxAge) {
            config.setCrossDomainMaxAge(crossDomainMaxAge);
            return this;
        }

        public DefaultWebMvcConfig build() {
            return config;
        }
    }
}
