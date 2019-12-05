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

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IInitialization;
import net.ymate.platform.webmvc.cors.CrossDomainSettings;
import org.apache.commons.fileupload.ProgressListener;

import java.util.Set;

/**
 * WebMVC模块配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 下午1:33
 */
@Ignored
public interface IWebMvcConfig extends IInitialization<IWebMvc> {

    String DEFAULT_STR = "default";

    String IGNORE_REGEX_PREFIX = "^.+\\.(";

    String IGNORE_REGEX_SUFFIX = ")$";

    String IGNORE_REGEX = IGNORE_REGEX_PREFIX + "jsp|jspx|png|gif|jpg|jpeg|js|css|swf|ico|htm|html|eot|woff|woff2|ttf|svg|map" + IGNORE_REGEX_SUFFIX;

    String REQUEST_MAPPING_PARSER_CLASS = "request_mapping_parser_class";

    String REQUEST_PROCESSOR_CLASS = "request_processor_class";

    String ERROR_PROCESSOR_CLASS = "error_processor_class";

    String CACHE_PROCESSOR_CLASS = "cache_processor_class";

    String RESOURCES_HOME = "resources_home";

    String RESOURCE_NAME = "resource_name";

    String LANGUAGE_PARAM_NAME = "language_param_name";

    String DEFAULT_CHARSET_ENCODING = "default_charset_encoding";

    String DEFAULT_CONTENT_TYPE = "default_content_type";

    String REQUEST_IGNORE_SUFFIX = "request_ignore_regex";

    String REQUEST_METHOD_PARAM = "request_method_param";

    String REQUEST_PREFIX = "request_prefix";

    String BASE_VIEW_PATH = "base_view_path";

    String COOKIE_PREFIX = "cookie_prefix";

    String COOKIE_DOMAIN = "cookie_domain";

    String COOKIE_PATH = "cookie_path";

    String COOKIE_AUTH_KEY = "cookie_auth_key";

    String COOKIE_AUTH_ENABLED = "cookie_auth_enabled";

    String COOKIE_USE_HTTP_ONLY = "cookie_use_http_only";

    String UPLOAD_TEMP_DIR = "upload_temp_dir";

    String UPLOAD_FILE_SIZE_MAX = "upload_file_size_max";

    String UPLOAD_TOTAL_SIZE_MAX = "upload_total_size_max";

    String UPLOAD_SIZE_THRESHOLD = "upload_size_threshold";

    String UPLOAD_LISTENER_CLASS = "upload_listener_class";

    String CONVENTION_MODE = "convention_mode";

    String CONVENTION_URL_REWRITE_MODE = "convention_url_rewrite_mode";

    String CONVENTION_INTERCEPTOR_MODE = "convention_interceptor_mode";

    String CONVENTION_VIEW_PATHS = "convention_view_paths";

    String CROSS_DOMAIN_SETTINGS_ENABLED = "cross_domain_settings_enabled";

    String CROSS_DOMAIN_OPTIONS_AUTO_REPLY = "cross_domain_options_auto_reply";

    String CROSS_DOMAIN_ALLOWED_ORIGINS = "cross_domain_allowed_origins";

    String CROSS_DOMAIN_ALLOWED_METHODS = "cross_domain_allowed_methods";

    String CROSS_DOMAIN_ALLOWED_HEADERS = "cross_domain_allowed_headers";

    String CROSS_DOMAIN_ALLOWED_CREDENTIALS = "cross_domain_allowed_credentials";

    String CROSS_DOMAIN_MAX_AGE = "cross_domain_max_age";

    /**
     * 控制器请求URL后缀参数名称
     */
    String PARAMS_REQUEST_SUFFIX = "webmvc.request_suffix";

    /**
     * 服务名称参数, 默认值: request.getServerName();
     */
    String PARAMS_SERVER_NAME = "webmvc.server_name";

    /**
     * 系统异常分析是否关闭参数名称
     */
    String PARAMS_EXCEPTION_ANALYSIS_DISABLED = "webmvc.exception_analysis_disabled";

    /**
     * 默认异常响应视图格式, 默认值: "", 可选范围: json|xml
     */
    String PARAMS_ERROR_DEFAULT_VIEW_FORMAT = "webmvc.error_default_view_format";

    /**
     * 异常信息视图文件参数名称
     */
    String PARAMS_ERROR_VIEW = "webmvc.error_view";

    /**
     * 验证结果消息模板参数名称, 默认值: "${items}"
     */
    String PARAMS_VALIDATION_TEMPLATE_ELEMENT = "webmvc.validation_template_element";

    /**
     * 验证结果消息项模板参数名称, 默认值: "${message}<br>"
     */
    String PARAMS_VALIDATION_TEMPLATE_ITEM = "webmvc.validation_template_item";

    /**
     * 重定向主页URL地址参数名称, 默认值: ""
     */
    String PARAMS_REDIRECT_HOME_URL = "webmvc.redirect_home_url";

    /**
     * 自定义重定向URL地址参数名称
     */
    String PARAMS_REDIRECT_CUSTOM_URL = "webmvc.redirect_custom_url";

    /**
     * 允许访问和重定向的主机名称, 多个主机名称用'|'分隔, 默认值: 空(表示不限制)
     */
    String PARAMS_ALLOWED_ACCESS_HOSTS = "webmvc.allowed_access_hosts";

    /**
     * 允许上传的文件类型验证参数名称
     */
    String PARAMS_ALLOWED_UPLOAD_CONTENT_TYPES = "webmvc.allowed_upload_content_types";

    /**
     * 控制器请求映射路径分析器，可选值为已知分析器名称或自定义分析器类名称，默认为restful，目前支持已知分析器[default|restful|...]
     *
     * @return 返回控制器请求映射路径分析器
     */
    IRequestMappingParser getRequestMappingParser();

    /**
     * 控制器请求处理器，可选值为已知处理器名称或自定义处理器类名称，默认为default，目前支持已知处理器[default|json|xml|...]
     *
     * @return 返回控制器请求处理器
     */
    IRequestProcessor getRequestProcessor();

    /**
     * 异常错误处理器，可选参数，默认值为net.ymate.platform.webmvc.impl.DefaultWebErrorProcessor
     *
     * @return 返回异常错误处理器
     */
    IWebErrorProcessor getErrorProcessor();

    /**
     * 缓存处理器，可选参数
     *
     * @return 返回缓存处理器
     */
    IWebCacheProcessor getCacheProcessor();

    /**
     * 国际化资源文件存放路径，可选参数，默认值为${root}/i18n/
     *
     * @return 返回国际化资源文件存放路径
     */
    String getResourceHome();

    /**
     * 国际化资源文件名称，可选参数，默认值为messages
     *
     * @return 返回国际化资源文件名称
     */
    String getResourceName();

    /**
     * 国际化语言设置参数名称，可选参数，默认值为_lang
     *
     * @return 返回国际化语言设置参数名称
     */
    String getLanguageParamName();

    /**
     * 默认字符编码集设置，可选参数，默认值为UTF-8
     *
     * @return 返回默认字符编码集设置
     */
    String getDefaultCharsetEncoding();

    /**
     * 默认Content-Type设置，可选参数，默认值为text/html
     *
     * @return 返回默认Content-Type设置
     */
    String getDefaultContentType();

    /**
     * 请求忽略后缀集合，可选参数，默认值为jsp|jspx|png|gif|jpg|jpeg|js|css|swf|ico|htm|html|eot|woff|ttf|svg|map
     *
     * @return 返回请求忽略后缀集合
     */
    Set<String> getRequestIgnoreSuffixes();

    /**
     * 请求方法参数名称，可选参数， 默认值为_method
     *
     * @return 返回请求方法参数名称
     */
    String getRequestMethodParam();

    /**
     * 请求路径前缀，可选参数，默认值为空
     *
     * @return 返回请求路径前缀
     */
    String getRequestPrefix();

    /**
     * 控制器视图文件基础路径（必须是以 '/' 开始和结尾，默认值为/WEB-INF/templates/）
     *
     * @return 返回控制器视图文件基础路径
     */
    String getBaseViewPath();

    /**
     * 控制器视图绝对路径
     *
     * @return 返回控制器视图绝对路径
     */
    String getAbstractBaseViewPath();

    /**
     * Cookie键前缀，可选参数，默认值为空
     *
     * @return 返回Cookie键前缀
     */
    String getCookiePrefix();

    /**
     * Cookie作用域，可选参数，默认值为空
     *
     * @return 返回Cookie作用域
     */
    String getCookieDomain();

    /**
     * Cookie作用路径，可选参数，默认值为'/'
     *
     * @return 返回Cookie作用路径
     */
    String getCookiePath();

    /**
     * Cookie密钥，可选参数，默认值为空
     *
     * @return 返回Cookie密钥
     */
    String getCookieAuthKey();

    /**
     * Cookie密钥验证是否默认开启, 默认值为false
     *
     * @return 返回true表示默认开启
     */
    boolean isCookieAuthEnabled();

    /**
     * Cookie是否默认使用HttpOnly, 默认值为false
     *
     * @return 返回true表示使用HttpOnly
     */
    boolean isCookieUseHttpOnly();

    /**
     * 文件上传临时目录，为空则默认使用：System.getProperty("java.io.tmpdir")
     *
     * @return 返回文件上传临时目录
     */
    String getUploadTempDir();

    /**
     * 上传文件大小最大值（字节），默认值：10485760（注：10485760 = 10M）
     *
     * @return 返回上传文件大小最大值
     */
    int getUploadFileSizeMax();

    /**
     * 上传文件总量大小最大值（字节）, 默认值：10485760（注：10485760 = 10M）
     *
     * @return 返回上传文件总量大小最大值
     */
    int getUploadTotalSizeMax();

    /**
     * 内存缓冲区的大小，默认值： 10240字节（=10K），即如果文件大于10K，将使用临时文件缓存上传文件
     *
     * @return 返回内存缓冲区的大小
     */
    int getUploadSizeThreshold();

    /**
     * 文件上传状态监听器
     *
     * @return 返回文件上传状态监听器
     */
    ProgressListener getUploadListener();

    /**
     * 是否开启视图自动渲染（约定优于配置，无需编写控制器代码，直接匹配并执行视图）模式，可选参数，默认值为false
     *
     * @return 返回true表示开启
     */
    boolean isConventionMode();

    /**
     * Convention模式开启时是否采用URL伪静态 (URL中通过分隔符'_'传递多个请求参数，通过_path[index]方式引用参数值) 模式，可选参数，默认值为false
     *
     * @return 返回true表示开启
     */
    boolean isConventionUrlRewriteMode();

    /**
     * Convention模式开启时是否采用拦截器规则设置，可选参数，默认值为false
     *
     * @return 返回true表示开启
     */
    boolean isConventionInterceptorMode();

    /**
     * Convention模式开启时视图文件路径(基于base_view_path的相对路径, 加号或无符串代表允许访问)，可选参数，默认值为空(即不限制访问路径)，多个路径间用'|'分隔
     *
     * @return 返回允许访问的视图文件路径集合
     */
    Set<String> getConventionViewAllowPaths();

    /**
     * Convention模式开启时视图文件路径(基于base_view_path的相对路径, 减号代表不允许访问)，可选参数，默认值为空(即不限制访问路径)，多个路径间用'|'分隔
     *
     * @return 返回禁止访问的视图文件路径集合
     */
    Set<String> getConventionViewNotAllowPaths();

    /**
     * 获取跨域设置
     *
     * @return 返回跨域设置
     */
    CrossDomainSettings getCrossDomainSettings();
}
