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

import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurable;
import net.ymate.platform.webmvc.*;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-15 03:46
 * @since 2.1.0
 */
public final class DefaultWebMvcConfigurable extends DefaultModuleConfigurable {

    public static Builder builder() {
        return new Builder();
    }

    private DefaultWebMvcConfigurable() {
        super(IWebMvc.MODULE_NAME);
    }

    public static final class Builder {

        private final DefaultWebMvcConfigurable configurable = new DefaultWebMvcConfigurable();

        private Builder() {
        }

        public Builder requestMappingParserClass(Class<? extends IRequestMappingParser> requestMappingParserClass) {
            configurable.addConfig(IWebMvcConfig.REQUEST_MAPPING_PARSER_CLASS, requestMappingParserClass.getName());
            return this;
        }

        public Builder requestProcessorClass(String requestProcessorClass) {
            configurable.addConfig(IWebMvcConfig.REQUEST_PROCESSOR_CLASS, StringUtils.trimToEmpty(requestProcessorClass));
            return this;
        }

        public Builder requestProcessorClass(Class<? extends IRequestProcessor> requestProcessorClass) {
            configurable.addConfig(IWebMvcConfig.REQUEST_PROCESSOR_CLASS, requestProcessorClass.getName());
            return this;
        }

        public Builder errorProcessorClass(Class<? extends IWebErrorProcessor> errorProcessorClass) {
            configurable.addConfig(IWebMvcConfig.ERROR_PROCESSOR_CLASS, errorProcessorClass.getName());
            return this;
        }

        public Builder cacheProcessorClass(Class<? extends IWebCacheProcessor> cacheProcessorClass) {
            configurable.addConfig(IWebMvcConfig.CACHE_PROCESSOR_CLASS, cacheProcessorClass.getName());
            return this;
        }

        public Builder resourcesHome(String resourcesHome) {
            configurable.addConfig(IWebMvcConfig.RESOURCES_HOME, StringUtils.trimToEmpty(resourcesHome));
            return this;
        }

        public Builder resourceName(String resourceName) {
            configurable.addConfig(IWebMvcConfig.RESOURCE_NAME, StringUtils.trimToEmpty(resourceName));
            return this;
        }

        public Builder languageParamName(String languageParamName) {
            configurable.addConfig(IWebMvcConfig.LANGUAGE_PARAM_NAME, StringUtils.trimToEmpty(languageParamName));
            return this;
        }

        public Builder defaultCharsetEncoding(String defaultCharsetEncoding) {
            configurable.addConfig(IWebMvcConfig.DEFAULT_CHARSET_ENCODING, StringUtils.trimToEmpty(defaultCharsetEncoding));
            return this;
        }

        public Builder defaultContentType(String defaultContentType) {
            configurable.addConfig(IWebMvcConfig.DEFAULT_CONTENT_TYPE, StringUtils.trimToEmpty(defaultContentType));
            return this;
        }

        public Builder requestIgnoreSuffix(String requestIgnoreSuffix) {
            configurable.addConfig(IWebMvcConfig.REQUEST_IGNORE_SUFFIX, StringUtils.trimToEmpty(requestIgnoreSuffix));
            return this;
        }

        public Builder requestMethodParam(String requestMethodParam) {
            configurable.addConfig(IWebMvcConfig.REQUEST_METHOD_PARAM, StringUtils.trimToEmpty(requestMethodParam));
            return this;
        }

        public Builder requestPrefix(String requestPrefix) {
            configurable.addConfig(IWebMvcConfig.REQUEST_PREFIX, StringUtils.trimToEmpty(requestPrefix));
            return this;
        }

        public Builder requestStrictModeEnabled(boolean requestStrictModeEnabled) {
            configurable.addConfig(IWebMvcConfig.REQUEST_STRICT_MODE_ENABLED, String.valueOf(requestStrictModeEnabled));
            return this;
        }

        public Builder baseViewPath(String baseViewPath) {
            configurable.addConfig(IWebMvcConfig.BASE_VIEW_PATH, StringUtils.trimToEmpty(baseViewPath));
            return this;
        }

        public Builder cookiePrefix(String cookiePrefix) {
            configurable.addConfig(IWebMvcConfig.COOKIE_PREFIX, StringUtils.trimToEmpty(cookiePrefix));
            return this;
        }

        public Builder cookieDomain(String cookieDomain) {
            configurable.addConfig(IWebMvcConfig.COOKIE_DOMAIN, StringUtils.trimToEmpty(cookieDomain));
            return this;
        }

        public Builder cookiePath(String cookiePath) {
            configurable.addConfig(IWebMvcConfig.COOKIE_PATH, StringUtils.trimToEmpty(cookiePath));
            return this;
        }

        public Builder cookieAuthKey(String cookieAuthKey) {
            configurable.addConfig(IWebMvcConfig.COOKIE_AUTH_KEY, StringUtils.trimToEmpty(cookieAuthKey));
            return this;
        }

        public Builder cookieAuthEnabled(boolean cookieAuthEnabled) {
            configurable.addConfig(IWebMvcConfig.COOKIE_AUTH_ENABLED, String.valueOf(cookieAuthEnabled));
            return this;
        }

        public Builder cookieUseHttpOnly(boolean cookieUseHttpOnly) {
            configurable.addConfig(IWebMvcConfig.COOKIE_USE_HTTP_ONLY, String.valueOf(cookieUseHttpOnly));
            return this;
        }

        public Builder uploadTempDir(String uploadTempDir) {
            configurable.addConfig(IWebMvcConfig.UPLOAD_TEMP_DIR, StringUtils.trimToEmpty(uploadTempDir));
            return this;
        }

        public Builder uploadFileCountMax(long uploadFileCountMax) {
            configurable.addConfig(IWebMvcConfig.UPLOAD_FILE_COUNT_MAX, String.valueOf(uploadFileCountMax));
            return this;
        }

        public Builder uploadFileSizeMax(long uploadFileSizeMax) {
            configurable.addConfig(IWebMvcConfig.UPLOAD_FILE_SIZE_MAX, String.valueOf(uploadFileSizeMax));
            return this;
        }

        public Builder uploadTotalSizeMax(long uploadTotalSizeMax) {
            configurable.addConfig(IWebMvcConfig.UPLOAD_TOTAL_SIZE_MAX, String.valueOf(uploadTotalSizeMax));
            return this;
        }

        public Builder uploadSizeThreshold(int uploadSizeThreshold) {
            configurable.addConfig(IWebMvcConfig.UPLOAD_SIZE_THRESHOLD, String.valueOf(uploadSizeThreshold));
            return this;
        }

        public Builder uploadListenerClass(Class<? extends ProgressListener> uploadListenerClass) {
            configurable.addConfig(IWebMvcConfig.UPLOAD_LISTENER_CLASS, uploadListenerClass.getName());
            return this;
        }

        public Builder conventionMode(boolean conventionMode) {
            configurable.addConfig(IWebMvcConfig.CONVENTION_MODE, String.valueOf(conventionMode));
            return this;
        }

        public Builder conventionUrlRewriteMode(boolean conventionUrlRewriteMode) {
            configurable.addConfig(IWebMvcConfig.CONVENTION_URL_REWRITE_MODE, String.valueOf(conventionUrlRewriteMode));
            return this;
        }

        public Builder conventionInterceptorMode(boolean conventionInterceptorMode) {
            configurable.addConfig(IWebMvcConfig.CONVENTION_INTERCEPTOR_MODE, String.valueOf(conventionInterceptorMode));
            return this;
        }

        public Builder conventionViewPaths(String conventionViewPaths) {
            configurable.addConfig(IWebMvcConfig.CONVENTION_VIEW_PATHS, StringUtils.trimToEmpty(conventionViewPaths));
            return this;
        }

        public Builder crossDomainSettingsEnabled(boolean crossDomainSettingsEnabled) {
            configurable.addConfig(IWebMvcConfig.CROSS_DOMAIN_SETTINGS_ENABLED, String.valueOf(crossDomainSettingsEnabled));
            return this;
        }

        public Builder crossDomainOptionsAutoReply(boolean crossDomainOptionsAutoReply) {
            configurable.addConfig(IWebMvcConfig.CROSS_DOMAIN_OPTIONS_AUTO_REPLY, String.valueOf(crossDomainOptionsAutoReply));
            return this;
        }

        public Builder crossDomainAllowedCredentials(boolean crossDomainAllowedCredentials) {
            configurable.addConfig(IWebMvcConfig.CROSS_DOMAIN_ALLOWED_CREDENTIALS, String.valueOf(crossDomainAllowedCredentials));
            return this;
        }

        public Builder crossDomainAllowedOrigins(String crossDomainAllowedOrigins) {
            configurable.addConfig(IWebMvcConfig.CROSS_DOMAIN_ALLOWED_ORIGINS, StringUtils.trimToEmpty(crossDomainAllowedOrigins));
            return this;
        }

        public Builder crossDomainAllowedMethods(String crossDomainAllowedMethods) {
            configurable.addConfig(IWebMvcConfig.CROSS_DOMAIN_ALLOWED_METHODS, StringUtils.trimToEmpty(crossDomainAllowedMethods));
            return this;
        }

        public Builder crossDomainAllowedHeaders(String crossDomainAllowedHeaders) {
            configurable.addConfig(IWebMvcConfig.CROSS_DOMAIN_ALLOWED_HEADERS, StringUtils.trimToEmpty(crossDomainAllowedHeaders));
            return this;
        }

        public Builder crossDomainMaxAge(long crossDomainMaxAge) {
            configurable.addConfig(IWebMvcConfig.CROSS_DOMAIN_MAX_AGE, String.valueOf(crossDomainMaxAge));
            return this;
        }

        public IModuleConfigurer build() {
            return configurable.toModuleConfigurer();
        }
    }
}
