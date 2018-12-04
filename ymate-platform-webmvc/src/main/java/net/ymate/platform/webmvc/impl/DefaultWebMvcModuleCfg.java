/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.core.IConfig;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.support.IConfigReader;
import net.ymate.platform.core.support.impl.MapSafeConfigReader;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.webmvc.*;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 默认WebMVC模块配置接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 下午1:35
 * @version 1.0
 */
public class DefaultWebMvcModuleCfg implements IWebMvcModuleCfg {

    private static final String __IGNORE = "^.+\\.(jsp|jspx|png|gif|jpg|jpeg|js|css|swf|ico|htm|html|eot|woff|woff2|ttf|svg|map)$";

    private IRequestMappingParser __mappingParser;

    private IRequestProcessor __requestProcessor;

    private IWebErrorProcessor __errorProcessor;

    private IWebCacheProcessor __cacheProcessor;

    private final String __charsetEncoding;

    private final String __contentType;

    private final String __requestIgnoreRegex;

    private final String __requestMethodParam;

    private final String __requestPrefix;

    private final boolean __parameterEscapeMode;

    private final Type.EscapeOrder __parameterEscapeOrder;

    private final String __baseViewPath;

    private String __abstractBaseViewPath;

    private final String __cookiePrefix;

    private final String __cookieDomain;

    private final String __cookiePath;

    private final String __cookieAuthKey;

    private final boolean __defaultEnabledCookieAuth;

    private final boolean __defaultUseHttpOnly;

    private final String __uploadTempDir;

    private final int __uploadFileSizeMax;

    private final int __uploadTotalSizeMax;

    private final int __uploadSizeThreshold;

    private final ProgressListener __uploadFileListener;

    private final boolean __conventionMode;

    private final boolean __conventionUrlrewriteMode;

    private final boolean __conventionInterceptorMode;

    private final Set<String> __conventionViewAllowPaths;

    private final Set<String> __conventionViewNotAllowPaths;

    public DefaultWebMvcModuleCfg(YMP owner) throws Exception {
        IConfigReader _moduleCfg = MapSafeConfigReader.bind(owner.getConfig().getModuleConfigs(IWebMvc.MODULE_NAME));
        //
        String _reqMappingParserClass = _moduleCfg.getString(REQUEST_MAPPING_PARSER_CLASS, IConfig.DEFAULT_STR);
        Class<? extends IRequestMappingParser> _mappingParserClass = Type.REQUEST_MAPPING_PARSERS.get(_reqMappingParserClass);
        if (_mappingParserClass == null && StringUtils.isNotBlank(_reqMappingParserClass)) {
            __mappingParser = ClassUtils.impl(_reqMappingParserClass, IRequestMappingParser.class, this.getClass());
        } else if (_mappingParserClass != null) {
            __mappingParser = _mappingParserClass.newInstance();
        }
        if (__mappingParser == null) {
            __mappingParser = new DefaultRequestMappingParser();
        }
        //
        String _reqProcessorClass = _moduleCfg.getString(REQUEST_PROCESSOR_CLASS, IConfig.DEFAULT_STR);
        Class<? extends IRequestProcessor> _requestProcessorClass = Type.REQUEST_PROCESSORS.get(_reqProcessorClass);
        if (_requestProcessorClass == null && StringUtils.isNotBlank(_reqProcessorClass)) {
            __requestProcessor = ClassUtils.impl(_reqProcessorClass, IRequestProcessor.class, this.getClass());
        } else if (_requestProcessorClass != null) {
            __requestProcessor = _requestProcessorClass.newInstance();
        }
        if (__requestProcessor == null) {
            __requestProcessor = new DefaultRequestProcessor();
        }
        //
        String _errorProcessorClass = _moduleCfg.getString(ERROR_PROCESSOR_CLASS);
        if (StringUtils.isNotBlank(_errorProcessorClass)) {
            __errorProcessor = ClassUtils.impl(_errorProcessorClass, IWebErrorProcessor.class, this.getClass());
        }
        //
        String _cacheProcessorClass = _moduleCfg.getString(CACHE_PROCESSOR_CLASS);
        if (StringUtils.isNotBlank(_cacheProcessorClass)) {
            __cacheProcessor = ClassUtils.impl(_cacheProcessorClass, IWebCacheProcessor.class, this.getClass());
        }
        //
        __charsetEncoding = _moduleCfg.getString(DEFAULT_CHARSET_ENCODING, IConfig.DEFAULT_CHARSET);
        __contentType = _moduleCfg.getString(DEFAULT_CONTENT_TYPE, Type.ContentType.HTML.getContentType());
        __requestIgnoreRegex = _moduleCfg.getString(REQUEST_IGNORE_REGEX, __IGNORE);
        __requestMethodParam = _moduleCfg.getString(REQUEST_METHOD_PARAM, "_method");
        __requestPrefix = StringUtils.trimToEmpty(_moduleCfg.getString(REQUEST_PREFIX));
        //
        __parameterEscapeMode = _moduleCfg.getBoolean(PARAMETER_ESCAPE_MODE);
        __parameterEscapeOrder = Type.EscapeOrder.valueOf(_moduleCfg.getString(PARAMETER_ESCAPE_ORDER, "after").toUpperCase());
        //
        __baseViewPath = RuntimeUtils.replaceEnvVariable(_moduleCfg.getString(BASE_VIEW_PATH, "/WEB-INF/templates/"));
        __abstractBaseViewPath = __baseViewPath;
        if (__abstractBaseViewPath.startsWith("/WEB-INF")) {
            __abstractBaseViewPath = new File(RuntimeUtils.getRootPath(false), __abstractBaseViewPath).getPath();
        }
        //
        __cookiePrefix = StringUtils.trimToEmpty(_moduleCfg.getString(COOKIE_PREFIX));
        __cookieDomain = StringUtils.trimToEmpty(_moduleCfg.getString(COOKIE_DOMAIN));
        __cookiePath = _moduleCfg.getString(COOKIE_PATH, "/");
        __cookieAuthKey = StringUtils.trimToEmpty(_moduleCfg.getString(COOKIE_AUTH_KEY));
        __defaultEnabledCookieAuth = _moduleCfg.getBoolean(DEFAULT_ENABLED_COOKIE_AUTH);
        __defaultUseHttpOnly = _moduleCfg.getBoolean(DEFAULT_USE_HTTP_ONLY);
        //
        __uploadTempDir = RuntimeUtils.replaceEnvVariable(StringUtils.trimToEmpty(_moduleCfg.getString(UPLOAD_TEMP_DIR)));
        __uploadFileSizeMax = _moduleCfg.getInt(UPLOAD_FILE_SIZE_MAX, 10485760);
        __uploadTotalSizeMax = _moduleCfg.getInt(UPLOAD_TOTAL_SIZE_MAX, 10485760);
        __uploadSizeThreshold = _moduleCfg.getInt(UPLOAD_SIZE_THRESHOLD, 10240);
        __uploadFileListener = _moduleCfg.getClassImpl(UPLOAD_FILE_LISTENER_CLASS, ProgressListener.class);
        //
        __conventionMode = _moduleCfg.getBoolean(CONVENTION_MODE);
        __conventionUrlrewriteMode = _moduleCfg.getBoolean(CONVENTION_URLREWRITE_MODE);
        __conventionInterceptorMode = _moduleCfg.getBoolean(CONVENTION_INTERCEPTOR_MODE);
        //
        __conventionViewAllowPaths = new HashSet<String>();
        __conventionViewNotAllowPaths = new HashSet<String>();
        //
        String[] _cViewPaths = _moduleCfg.getArray(CONVENTION_VIEW_PATHS);
        if (_cViewPaths != null) {
            for (String _cvPath : _cViewPaths) {
                _cvPath = StringUtils.trimToNull(_cvPath);
                if (_cvPath != null) {
                    boolean _flag = true;
                    if (_cvPath.length() > 1) {
                        char _c = _cvPath.charAt(_cvPath.length() - 1);
                        if (_c == '+') {
                            _cvPath = StringUtils.substring(_cvPath, 0, _cvPath.length() - 1);
                        } else if (_c == '-') {
                            _cvPath = StringUtils.substring(_cvPath, 0, _cvPath.length() - 1);
                            _flag = false;
                        }
                    }
                    if (_cvPath.charAt(0) != '/') {
                        _cvPath = "/" + _cvPath;
                    }
                    if (_flag) {
                        __conventionViewAllowPaths.add(_cvPath);
                    } else {
                        __conventionViewNotAllowPaths.add(_cvPath);
                    }
                }
            }
        }
    }

    @Override
    public IRequestMappingParser getRequestMappingParser() {
        return __mappingParser;
    }

    @Override
    public IRequestProcessor getRequestProcessor() {
        return __requestProcessor;
    }

    @Override
    public IWebErrorProcessor getErrorProcessor() {
        return __errorProcessor;
    }

    @Override
    public IWebCacheProcessor getCacheProcessor() {
        return __cacheProcessor;
    }

    @Override
    public String getDefaultCharsetEncoding() {
        return __charsetEncoding;
    }

    @Override
    public String getDefaultContentType() {
        return __contentType;
    }

    @Override
    public String getRequestIgnoreRegex() {
        return __requestIgnoreRegex;
    }

    @Override
    public String getRequestMethodParam() {
        return __requestMethodParam;
    }

    @Override
    public String getRequestPrefix() {
        return __requestPrefix;
    }

    @Override
    public String getBaseViewPath() {
        return __baseViewPath;
    }

    @Override
    public String getAbstractBaseViewPath() {
        return __abstractBaseViewPath;
    }

    @Override
    public String getCookiePrefix() {
        return __cookiePrefix;
    }

    @Override
    public String getCookieDomain() {
        return __cookieDomain;
    }

    @Override
    public String getCookiePath() {
        return __cookiePath;
    }

    @Override
    public String getCookieAuthKey() {
        return __cookieAuthKey;
    }

    @Override
    public boolean isDefaultEnabledCookieAuth() {
        return __defaultEnabledCookieAuth;
    }

    @Override
    public boolean isDefaultUseHttpOnly() {
        return __defaultUseHttpOnly;
    }

    @Override
    public String getUploadTempDir() {
        return __uploadTempDir;
    }

    @Override
    public int getUploadFileSizeMax() {
        return __uploadFileSizeMax;
    }

    @Override
    public int getUploadTotalSizeMax() {
        return __uploadTotalSizeMax;
    }

    @Override
    public int getUploadSizeThreshold() {
        return __uploadSizeThreshold;
    }

    @Override
    public ProgressListener getUploadFileListener() {
        return __uploadFileListener;
    }

    @Override
    public boolean isConventionMode() {
        return __conventionMode;
    }

    @Override
    public boolean isConventionUrlrewriteMode() {
        return __conventionUrlrewriteMode;
    }

    @Override
    public boolean isConventionInterceptorMode() {
        return __conventionInterceptorMode;
    }

    @Override
    public Set<String> getConventionViewAllowPaths() {
        return Collections.unmodifiableSet(__conventionViewAllowPaths);
    }

    @Override
    public Set<String> getConventionViewNotAllowPaths() {
        return Collections.unmodifiableSet(__conventionViewNotAllowPaths);
    }

    @Override
    public boolean isParameterEscapeMode() {
        return __parameterEscapeMode;
    }

    @Override
    public Type.EscapeOrder getParameterEscapeOrder() {
        return __parameterEscapeOrder;
    }
}
