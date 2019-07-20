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
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 默认WebMVC模块配置接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 下午1:35
 */
public final class DefaultWebMvcConfig implements IWebMvcConfig {

    private IRequestMappingParser mappingParser;

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

    private String baseViewPath;

    private String abstractBaseViewPath;

    private String cookiePrefix;

    private String cookieDomain;

    private String cookiePath;

    private String cookieAuthKey;

    private boolean cookieAuthEnabled;

    private boolean cookieUseHttpOnly;

    private String uploadTempDir;

    private int uploadFileSizeMax;

    private int uploadTotalSizeMax;

    private int uploadSizeThreshold;

    private ProgressListener uploadListener;

    private boolean conventionMode;

    private boolean conventionUrlRewriteMode;

    private boolean conventionInterceptorMode;

    private final Set<String> conventionViewAllowPaths = new HashSet<>();

    private final Set<String> conventionViewNotAllowPaths = new HashSet<>();

    private boolean initialized;

    public static IWebMvcConfig defaultConfig() {
        return builder().build();
    }

    public static IWebMvcConfig create(IModuleConfigurer moduleConfigurer) {
        return new DefaultWebMvcConfig(moduleConfigurer);
    }

    public static Builder builder() {
        return new Builder();
    }

    private DefaultWebMvcConfig() {
    }

    private DefaultWebMvcConfig(IModuleConfigurer moduleConfigurer) {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        String mappingParserClassName = configReader.getString(REQUEST_MAPPING_PARSER_CLASS, DEFAULT_STR);
        Class<? extends IRequestMappingParser> mappingParserClass = Type.REQUEST_MAPPING_PARSERS.get(mappingParserClassName);
        if (mappingParserClass == null && StringUtils.isNotBlank(mappingParserClassName)) {
            mappingParser = ClassUtils.impl(mappingParserClassName, IRequestMappingParser.class, this.getClass());
        } else if (mappingParserClass != null) {
            mappingParser = ClassUtils.impl(mappingParserClass, IRequestMappingParser.class);
        }
        //
        String requestProcessorClassName = configReader.getString(REQUEST_PROCESSOR_CLASS, DEFAULT_STR);
        Class<? extends IRequestProcessor> requestProcessorClass = Type.REQUEST_PROCESSORS.get(requestProcessorClassName);
        if (requestProcessorClass == null && StringUtils.isNotBlank(requestProcessorClassName)) {
            requestProcessor = ClassUtils.impl(requestProcessorClassName, IRequestProcessor.class, this.getClass());
        } else if (requestProcessorClass != null) {
            requestProcessor = ClassUtils.impl(requestProcessorClass, IRequestProcessor.class);
        }
        //
        errorProcessor = configReader.getClassImpl(ERROR_PROCESSOR_CLASS, DefaultWebErrorProcessor.class.getName(), IWebErrorProcessor.class);
        cacheProcessor = configReader.getClassImpl(CACHE_PROCESSOR_CLASS, IWebCacheProcessor.class);
        //
        resourceHome = configReader.getString(RESOURCES_HOME);
        resourceName = configReader.getString(RESOURCE_NAME);
        languageParamName = configReader.getString(LANGUAGE_PARAM_NAME);
        //
        defaultCharsetEncoding = configReader.getString(DEFAULT_CHARSET_ENCODING);
        defaultContentType = configReader.getString(DEFAULT_CONTENT_TYPE);
        //
        requestIgnoreSuffixes.addAll(Arrays.asList(configReader.getArray(REQUEST_IGNORE_SUFFIX, true)));
        requestMethodParam = configReader.getString(REQUEST_METHOD_PARAM);
        requestPrefix = configReader.getString(REQUEST_PREFIX);
        //
        baseViewPath = configReader.getString(BASE_VIEW_PATH);
        //
        cookiePrefix = configReader.getString(COOKIE_PREFIX);
        cookieDomain = configReader.getString(COOKIE_DOMAIN);
        cookiePath = configReader.getString(COOKIE_PATH);
        cookieAuthKey = configReader.getString(COOKIE_AUTH_KEY);
        cookieAuthEnabled = configReader.getBoolean(COOKIE_AUTH_ENABLED);
        cookieUseHttpOnly = configReader.getBoolean(COOKIE_USE_HTTP_ONLY);
        //
        uploadTempDir = configReader.getString(UPLOAD_TEMP_DIR);
        uploadFileSizeMax = configReader.getInt(UPLOAD_FILE_SIZE_MAX);
        uploadTotalSizeMax = configReader.getInt(UPLOAD_TOTAL_SIZE_MAX);
        uploadSizeThreshold = configReader.getInt(UPLOAD_SIZE_THRESHOLD);
        uploadListener = configReader.getClassImpl(UPLOAD_LISTENER_CLASS, ProgressListener.class);
        //
        conventionMode = configReader.getBoolean(CONVENTION_MODE);
        conventionUrlRewriteMode = configReader.getBoolean(CONVENTION_URL_REWRITE_MODE);
        conventionInterceptorMode = configReader.getBoolean(CONVENTION_INTERCEPTOR_MODE);
        //
        String[] conventionViewPaths = configReader.getArray(CONVENTION_VIEW_PATHS);
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
            if (mappingParser == null) {
                mappingParser = new DefaultRequestMappingParser();
            }
            if (requestProcessor == null) {
                requestProcessor = new DefaultRequestProcessor();
            }
            if (errorProcessor == null) {
                errorProcessor = new DefaultWebErrorProcessor();
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
            uploadFileSizeMax = uploadFileSizeMax > 0 ? uploadFileSizeMax : 10485760;
            uploadTotalSizeMax = uploadTotalSizeMax > 0 ? uploadTotalSizeMax : 10485760;
            uploadSizeThreshold = uploadSizeThreshold > 0 ? uploadSizeThreshold : DiskFileItemFactory.DEFAULT_SIZE_THRESHOLD;
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
        return mappingParser;
    }

    public void setMappingParser(IRequestMappingParser mappingParser) {
        if (!initialized) {
            this.mappingParser = mappingParser;
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
    public String getResourcesHome() {
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
    public int getUploadFileSizeMax() {
        return uploadFileSizeMax;
    }

    public void setUploadFileSizeMax(int uploadFileSizeMax) {
        if (!initialized) {
            this.uploadFileSizeMax = uploadFileSizeMax;
        }
    }

    @Override
    public int getUploadTotalSizeMax() {
        return uploadTotalSizeMax;
    }

    public void setUploadTotalSizeMax(int uploadTotalSizeMax) {
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
        if (!initialized) {
            conventionViewAllowPaths.add(conventionViewAllowPath);
        }
    }

    public void addConventionViewNotAllowPath(String conventionViewNotAllowPath) {
        if (!initialized) {
            conventionViewNotAllowPaths.add(conventionViewNotAllowPath);
        }
    }

    public static final class Builder {

        private final DefaultWebMvcConfig config = new DefaultWebMvcConfig();

        private Builder() {
        }

        private Builder mappingParser(IRequestMappingParser mappingParser) {
            config.setMappingParser(mappingParser);
            return this;
        }

        private Builder requestProcessor(IRequestProcessor requestProcessor) {
            config.setRequestProcessor(requestProcessor);
            return this;
        }

        private Builder errorProcessor(IWebErrorProcessor errorProcessor) {
            config.setErrorProcessor(errorProcessor);
            return this;
        }

        private Builder cacheProcessor(IWebCacheProcessor cacheProcessor) {
            config.setCacheProcessor(cacheProcessor);
            return this;
        }

        private Builder resourceHome(String resourceHome) {
            config.setResourceHome(resourceHome);
            return this;
        }

        private Builder resourceName(String resourceName) {
            config.setResourceName(resourceName);
            return this;
        }

        private Builder languageParamName(String languageParamName) {
            config.setLanguageParamName(languageParamName);
            return this;
        }

        private Builder defaultCharsetEncoding(String defaultCharsetEncoding) {
            config.setDefaultCharsetEncoding(defaultCharsetEncoding);
            return this;
        }

        private Builder defaultContentType(String defaultContentType) {
            config.setDefaultContentType(defaultContentType);
            return this;
        }

        private Builder addRequestIgnoreSuffix(String... requestIgnoreSuffixes) {
            if (requestIgnoreSuffixes != null && requestIgnoreSuffixes.length > 0) {
                Arrays.stream(requestIgnoreSuffixes).forEach(config::addRequestIgnoreSuffix);
            }
            return this;
        }

        private Builder requestMethodParam(String requestMethodParam) {
            config.setRequestMethodParam(requestMethodParam);
            return this;
        }

        private Builder requestPrefix(String requestPrefix) {
            config.setRequestPrefix(requestPrefix);
            return this;
        }

        private Builder baseViewPath(String baseViewPath) {
            config.setBaseViewPath(baseViewPath);
            return this;
        }

        private Builder abstractBaseViewPath(String abstractBaseViewPath) {
            config.setAbstractBaseViewPath(abstractBaseViewPath);
            return this;
        }

        private Builder cookiePrefix(String cookiePrefix) {
            config.setCookiePrefix(cookiePrefix);
            return this;
        }

        private Builder cookieDomain(String cookieDomain) {
            config.setCookieDomain(cookieDomain);
            return this;
        }

        private Builder cookiePath(String cookiePath) {
            config.setCookiePath(cookiePath);
            return this;
        }

        private Builder cookieAuthKey(String cookieAuthKey) {
            config.setCookieAuthKey(cookieAuthKey);
            return this;
        }

        private Builder cookieAuthEnabled(boolean cookieAuthEnabled) {
            config.setCookieAuthEnabled(cookieAuthEnabled);
            return this;
        }

        private Builder cookieUseHttpOnly(boolean cookieUseHttpOnly) {
            config.setCookieUseHttpOnly(cookieUseHttpOnly);
            return this;
        }

        private Builder uploadTempDir(String uploadTempDir) {
            config.setUploadTempDir(uploadTempDir);
            return this;
        }

        private Builder uploadFileSizeMax(int uploadFileSizeMax) {
            config.setUploadFileSizeMax(uploadFileSizeMax);
            return this;
        }

        private Builder uploadTotalSizeMax(int uploadTotalSizeMax) {
            config.setUploadTotalSizeMax(uploadTotalSizeMax);
            return this;
        }

        private Builder uploadSizeThreshold(int uploadSizeThreshold) {
            config.setUploadSizeThreshold(uploadSizeThreshold);
            return this;
        }

        private Builder uploadListener(ProgressListener uploadListener) {
            config.setUploadListener(uploadListener);
            return this;
        }

        private Builder conventionMode(boolean conventionMode) {
            config.setConventionMode(conventionMode);
            return this;
        }

        private Builder conventionUrlRewriteMode(boolean conventionUrlRewriteMode) {
            config.setConventionUrlRewriteMode(conventionUrlRewriteMode);
            return this;
        }

        private Builder conventionInterceptorMode(boolean conventionInterceptorMode) {
            config.setConventionInterceptorMode(conventionInterceptorMode);
            return this;
        }

        public Builder addConventionViewAllowPath(String... conventionViewAllowPaths) {
            if (conventionViewAllowPaths != null && conventionViewAllowPaths.length > 0) {
                Arrays.stream(conventionViewAllowPaths).forEach(config::addConventionViewAllowPath);
            }
            return this;
        }

        public Builder addConventionViewNotAllowPath(String... conventionViewNotAllowPaths) {
            if (conventionViewNotAllowPaths != null && conventionViewNotAllowPaths.length > 0) {
                Arrays.stream(conventionViewNotAllowPaths).forEach(config::addConventionViewNotAllowPath);
            }
            return this;
        }

        public IWebMvcConfig build() {
            return config;
        }
    }
}
