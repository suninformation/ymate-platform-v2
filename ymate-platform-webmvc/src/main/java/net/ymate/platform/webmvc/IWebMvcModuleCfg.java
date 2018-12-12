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
package net.ymate.platform.webmvc;

import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.fileupload.ProgressListener;

import java.util.Set;

/**
 * WebMVC模块配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 下午1:33
 * @version 1.0
 */
public interface IWebMvcModuleCfg {

    String REQUEST_MAPPING_PARSER_CLASS = "request_mapping_parser_class";

    String REQUEST_PROCESSOR_CLASS = "request_processor_class";

    String ERROR_PROCESSOR_CLASS = "error_processor_class";

    String CACHE_PROCESSOR_CLASS = "cache_processor_class";

    String I18N_RESOURCES_HOME = "i18n_resources_home";

    String I18N_RESOURCE_NAME = "i18n_resource_name";

    String I18N_LANGUAGE_PARAM_NAME = "i18n_language_param_name";

    String DEFAULT_CHARSET_ENCODING = "default_charset_encoding";

    String DEFAULT_CONTENT_TYPE = "default_content_type";

    String REQUEST_IGNORE_REGEX = "request_ignore_regex";

    String REQUEST_METHOD_PARAM = "request_method_param";

    String REQUEST_PREFIX = "request_prefix";

    String PARAMETER_ESCAPE_MODE = "parameter_escape_mode";

    String PARAMETER_ESCAPE_ORDER = "parameter_escape_order";

    String BASE_VIEW_PATH = "base_view_path";

    String COOKIE_PREFIX = "cookie_prefix";

    String COOKIE_DOMAIN = "cookie_domain";

    String COOKIE_PATH = "cookie_path";

    String COOKIE_AUTH_KEY = "cookie_auth_key";

    String DEFAULT_ENABLED_COOKIE_AUTH = "default_enabled_cookie_auth";

    String DEFAULT_USE_HTTP_ONLY = "default_use_http_only";

    String UPLOAD_TEMP_DIR = "upload_temp_dir";

    String UPLOAD_FILE_SIZE_MAX = "upload_file_size_max";

    String UPLOAD_TOTAL_SIZE_MAX = "upload_total_size_max";

    String UPLOAD_SIZE_THRESHOLD = "upload_size_threshold";

    String UPLOAD_FILE_LISTENER_CLASS = "upload_file_listener_class";

    String CONVENTION_MODE = "convention_mode";

    String CONVENTION_URLREWRITE_MODE = "convention_urlrewrite_mode";

    String CONVENTION_INTERCEPTOR_MODE = "convention_interceptor_mode";

    String CONVENTION_VIEW_PATHS = "convention_view_paths";

    /**
     * @return 控制器请求映射路径分析器，可选值为已知分析器名称或自定义分析器类名称，默认为restful，目前支持已知分析器[default|restful|...]
     */
    IRequestMappingParser getRequestMappingParser();

    /**
     * @return 控制器请求处理器，可选值为已知处理器名称或自定义处理器类名称，默认为default，目前支持已知处理器[default|json|xml|...]
     */
    IRequestProcessor getRequestProcessor();

    /**
     * @return 异常错误处理器，可选参数，默认值为net.ymate.platform.webmvc.impl.DefaultWebErrorProcessor
     */
    IWebErrorProcessor getErrorProcessor();

    /**
     * @return 缓存处理器，可选参数
     */
    IWebCacheProcessor getCacheProcessor();

    /**
     * @return 国际化资源文件存放路径，可选参数，默认值为${root}/i18n/
     */
    String getI18nResourcesHome();

    /**
     * @return 国际化资源文件名称，可选参数，默认值为messages
     */
    String getI18nResourceName();

    /**
     * @return 国际化语言设置参数名称，可选参数，默认值为_lang
     */
    String getI18nLanguageParamName();

    /**
     * @return 默认字符编码集设置，可选参数，默认值为UTF-8
     */
    String getDefaultCharsetEncoding();

    /**
     * @return 默认Content-Type设置，可选参数，默认值为text/html
     */
    String getDefaultContentType();

    /**
     * @return 请求忽略正则表达式，可选参数，默认值为^.+\.(jsp|jspx|png|gif|jpg|jpeg|js|css|swf|ico|htm|html|eot|woff|ttf|svg|map)$
     */
    String getRequestIgnoreRegex();

    /**
     * @return 请求方法参数名称，可选参数， 默认值为_method
     */
    String getRequestMethodParam();

    /**
     * @return 请求路径前缀，可选参数，默认值为空
     */
    String getRequestPrefix();

    /**
     * @return 控制器视图文件基础路径（必须是以 '/' 开始和结尾，默认值为/WEB-INF/templates/）
     */
    String getBaseViewPath();

    /**
     * @return 尽量返回控制器视图绝对路径
     */
    String getAbstractBaseViewPath();

    /**
     * @return Cookie键前缀，可选参数，默认值为空
     */
    String getCookiePrefix();

    /**
     * @return Cookie作用域，可选参数，默认值为空
     */
    String getCookieDomain();

    /**
     * @return Cookie作用路径，可选参数，默认值为'/'
     */
    String getCookiePath();

    /**
     * @return Cookie密钥，可选参数，默认值为空
     */
    String getCookieAuthKey();

    /**
     * @return Cookie密钥验证是否默认开启, 默认值为false
     */
    boolean isDefaultEnabledCookieAuth();

    /**
     * @return Cookie是否默认使用HttpOnly, 默认值为false
     */
    boolean isDefaultUseHttpOnly();

    /**
     * @return 文件上传临时目录，为空则默认使用：System.getProperty("java.io.tmpdir")
     */
    String getUploadTempDir();

    /**
     * @return 上传文件大小最大值（字节），默认值：10485760（注：10485760 = 10M）
     */
    int getUploadFileSizeMax();

    /**
     * @return 上传文件总量大小最大值（字节）, 默认值：10485760（注：10485760 = 10M）
     */
    int getUploadTotalSizeMax();

    /**
     * @return 内存缓冲区的大小，默认值： 10240字节（=10K），即如果文件大于10K，将使用临时文件缓存上传文件
     */
    int getUploadSizeThreshold();

    /**
     * @return 文件上传状态监听器
     */
    ProgressListener getUploadFileListener();

    /**
     * @return 是否开启视图自动渲染（约定优于配置，无需编写控制器代码，直接匹配并执行视图）模式，可选参数，默认值为false
     */
    boolean isConventionMode();

    /**
     * @return Convention模式开启时是否采用URL伪静态 (URL中通过分隔符'_'传递多个请求参数，通过_path[index]方式引用参数值) 模式，可选参数，默认值为false
     */
    boolean isConventionUrlrewriteMode();

    /**
     * @return Convention模式开启时是否采用拦截器规则设置，可选参数，默认值为false
     */
    boolean isConventionInterceptorMode();

    /**
     * @return Convention模式开启时视图文件路径(基于base_view_path的相对路径, 加号或无符串代表允许访问)，可选参数，默认值为空(即不限制访问路径)，多个路径间用'|'分隔
     */
    Set<String> getConventionViewAllowPaths();

    /**
     * @return Convention模式开启时视图文件路径(基于base_view_path的相对路径, 减号代表不允许访问)，可选参数，默认值为空(即不限制访问路径)，多个路径间用'|'分隔
     */
    Set<String> getConventionViewNotAllowPaths();

    /**
     * @return 请求参数转义模式是否开启（开启状态时，控制器方法的所有参数将默认支持转义，可针对具体控制器方法或参数设置忽略转义操作），可选参数，默认值为false
     */
    boolean isParameterEscapeMode();

    /**
     * @return 执行请求参数转义顺序，可选参数，取值范围：before(参数验证之前)和after(参数验证之后)，默认值为after
     */
    Type.EscapeOrder getParameterEscapeOrder();
}
