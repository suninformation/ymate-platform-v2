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
package net.ymate.platform.webmvc.impl;

import net.ymate.platform.core.YMP;
import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.webmvc.IRequestProcessor;
import net.ymate.platform.webmvc.IWebErrorProcessor;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.IWebMvcModuleCfg;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.*;

/**
 * 默认WebMVC模块配置接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 下午1:35
 * @version 1.0
 */
public class DefaultModuleCfg implements IWebMvcModuleCfg {

    private static final String __IGNORE = "^.+\\.(jsp|jspx|png|gif|jpg|jpeg|js|css|swf|ico|htm|html|eot|woff|ttf|svg)$";

    private IRequestProcessor __requestProcessor;

    private IWebErrorProcessor __errorProcessor;

    private Locale __locale;

    private String __charsetEncoding;

    private String __requestIgnoreRegex;

    private String __requestMethodParam;

    private String __requestPrefix;

    private String __baseViewPath;

    private String __abstractBaseViewPath;

    private String __cookiePrefix;

    private String __cookieDomain;

    private String __cookiePath;

    private String __cookieAuthKey;

    private String __uploadTempDir;

    private int __uploadFileSizeMax;

    private int __uploadTotalSizeMax;

    private int __uploadSizeThreshold;

    private boolean __conventionMode;

    private List<String> __conventionViewPaths;

    public DefaultModuleCfg(YMP owner) throws Exception {
        Map<String, String> _moduleCfgs = owner.getConfig().getModuleConfigs(IWebMvc.MODULE_NAME);
        //
        String _reqProcessorClass = StringUtils.defaultIfBlank(_moduleCfgs.get("request_processor_class"), "default");
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
        String _errorProcessorClass = _moduleCfgs.get("error_processor_class");
        if (StringUtils.isNotBlank(_errorProcessorClass)) {
            __errorProcessor = ClassUtils.impl(_errorProcessorClass, IWebErrorProcessor.class, this.getClass());
        }
        //
        __locale = LocaleUtils.toLocale(_moduleCfgs.get("default_locale"));
        __charsetEncoding = StringUtils.defaultIfBlank(_moduleCfgs.get("default_charset_encoding"), "UTF-8");
        __requestIgnoreRegex = StringUtils.defaultIfBlank(_moduleCfgs.get("request_ignore_regex"), __IGNORE);
        __requestMethodParam = StringUtils.defaultIfBlank(_moduleCfgs.get("request_method_param"), "_method");
        __requestPrefix = StringUtils.trimToEmpty(_moduleCfgs.get("request_prefix"));
        //
        __baseViewPath = RuntimeUtils.replaceEnvVariable(StringUtils.defaultIfBlank(_moduleCfgs.get("base_view_path"), "/WEB-INF/templates/"));
        __abstractBaseViewPath = __baseViewPath;
        if (__abstractBaseViewPath.startsWith("/WEB-INF")) {
            __abstractBaseViewPath = new File(RuntimeUtils.getRootPath(false), __abstractBaseViewPath).getPath();
        }
        //
        __cookiePrefix = StringUtils.trimToEmpty(_moduleCfgs.get("cookie_prefix"));
        __cookieDomain = StringUtils.trimToEmpty(_moduleCfgs.get("cookie_domain"));
        __cookiePath = StringUtils.defaultIfBlank(_moduleCfgs.get("cookie_path"), "/");
        __cookieAuthKey = StringUtils.trimToEmpty(_moduleCfgs.get("cookie_auth_key"));
        //
        __uploadTempDir = RuntimeUtils.replaceEnvVariable(StringUtils.trimToEmpty(_moduleCfgs.get("upload_temp_dir")));
        __uploadFileSizeMax = Integer.parseInt(StringUtils.defaultIfBlank(_moduleCfgs.get("upload_file_size_max"), "10485760"));
        __uploadTotalSizeMax = Integer.parseInt(StringUtils.defaultIfBlank(_moduleCfgs.get("upload_total_size_max"), "10485760"));
        __uploadSizeThreshold = Integer.parseInt(StringUtils.defaultIfBlank(_moduleCfgs.get("upload_size_threshold"), "10240"));
        //
        __conventionMode = BlurObject.bind(_moduleCfgs.get("convention_mode")).toBooleanValue();
        __conventionViewPaths = Arrays.asList(StringUtils.split(StringUtils.defaultIfBlank(_moduleCfgs.get("convention_view_paths"), ""), "|"));
    }

    public IRequestProcessor getRequestProcessor() {
        return __requestProcessor;
    }

    public IWebErrorProcessor getErrorProcessor() {
        return __errorProcessor;
    }

    public Locale getDefaultLocale() {
        return __locale;
    }

    public String getDefaultCharsetEncoding() {
        return __charsetEncoding;
    }

    public String getRequestIgnoreRegex() {
        return __requestIgnoreRegex;
    }

    public String getRequestMethodParam() {
        return __requestMethodParam;
    }

    public String getRequestPrefix() {
        return __requestPrefix;
    }

    public String getBaseViewPath() {
        return __baseViewPath;
    }

    public String getAbstractBaseViewPath() {
        return __abstractBaseViewPath;
    }

    public String getCookiePrefix() {
        return __cookiePrefix;
    }

    public String getCookieDomain() {
        return __cookieDomain;
    }

    public String getCookiePath() {
        return __cookiePath;
    }

    public String getCookieAuthKey() {
        return __cookieAuthKey;
    }

    public String getUploadTempDir() {
        return __uploadTempDir;
    }

    public int getUploadFileSizeMax() {
        return __uploadFileSizeMax;
    }

    public int getUploadTotalSizeMax() {
        return __uploadTotalSizeMax;
    }

    public int getUploadSizeThreshold() {
        return __uploadSizeThreshold;
    }

    public boolean isConventionMode() {
        return __conventionMode;
    }

    public List<String> getConventionViewPaths() {
        return Collections.unmodifiableList(__conventionViewPaths);
    }
}
