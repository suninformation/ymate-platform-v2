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
package net.ymate.platform.webmvc.base;

import net.ymate.platform.webmvc.IRequestMappingParser;
import net.ymate.platform.webmvc.IRequestProcessor;
import net.ymate.platform.webmvc.impl.DefaultRequestMappingParser;
import net.ymate.platform.webmvc.impl.DefaultRequestProcessor;
import net.ymate.platform.webmvc.impl.JSONRequestProcessor;
import net.ymate.platform.webmvc.impl.XMLRequestProcessor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据常量/枚举类型
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/19 上午1:56
 */
public class Type {

    public final static Map<String, Class<? extends IRequestProcessor>> REQUEST_PROCESSORS;

    public final static Map<String, Class<? extends IRequestMappingParser>> REQUEST_MAPPING_PARSERS;

    public final static Map<Integer, String> HTTP_STATUS;

    static {
        Map<String, Class<? extends IRequestProcessor>> processorMap = new HashMap<>();
        //
        processorMap.put("default", DefaultRequestProcessor.class);
        processorMap.put("json", JSONRequestProcessor.class);
        processorMap.put("xml", XMLRequestProcessor.class);
        //
        REQUEST_PROCESSORS = Collections.unmodifiableMap(processorMap);
        //
        Map<String, Class<? extends IRequestMappingParser>> parserMap = new HashMap<>();
        parserMap.put("default", DefaultRequestMappingParser.class);
        //
        REQUEST_MAPPING_PARSERS = Collections.unmodifiableMap(parserMap);
        //
        Map<Integer, String> httpStatusMap = new HashMap<>();
        //
        httpStatusMap.put(400, "Bad Request");
        httpStatusMap.put(401, "Unauthorized");
        httpStatusMap.put(402, "Payment Required");
        httpStatusMap.put(403, "Forbidden");
        httpStatusMap.put(404, "Not Found");
        httpStatusMap.put(405, "Method Not Allowed");
        httpStatusMap.put(406, "Not Acceptable");
        httpStatusMap.put(407, "Proxy Authentication Required");
        httpStatusMap.put(408, "Request Timeout");
        httpStatusMap.put(409, "Conflict");
        httpStatusMap.put(410, "Gone");
        httpStatusMap.put(411, "Length Required");
        httpStatusMap.put(412, "Precondition Failed");
        httpStatusMap.put(413, "Request Entity Too Large");
        httpStatusMap.put(414, "Request URI Too Long");
        httpStatusMap.put(415, "Unsupported Media Type");
        httpStatusMap.put(416, "Requested Range Not Satisfiable");
        httpStatusMap.put(417, "Expectation Failed");
        httpStatusMap.put(421, "Too Many Connections");
        httpStatusMap.put(422, "Unprocessable Entity");
        httpStatusMap.put(423, "Locked");
        httpStatusMap.put(424, "Failed Dependency");
        httpStatusMap.put(425, "Unordered Collection");
        httpStatusMap.put(426, "Ungrade Required");
        httpStatusMap.put(449, "Retry With");
        httpStatusMap.put(451, "Unavailable For Legal Reasons");
        //
        httpStatusMap.put(500, "Internal Server Error");
        httpStatusMap.put(501, "Not Implemented");
        httpStatusMap.put(502, "Bad Gateway");
        httpStatusMap.put(503, "Service Unavailable");
        httpStatusMap.put(504, "Gateway Timeout");
        httpStatusMap.put(505, "HTTP Version Not Supported");
        httpStatusMap.put(506, "Variant Also Negotiates");
        httpStatusMap.put(507, "Insufficient Storage");
        httpStatusMap.put(509, "Bandwidth Limit Exceeded");
        httpStatusMap.put(510, "Not Extended");
        //
        HTTP_STATUS = Collections.unmodifiableMap(httpStatusMap);
    }

    /**
     * 常量
     */
    public interface Const {

        String HTTP_PREFIX = "http://";

        String HTTPS_PREFIX = "https://";

        int HTTP_PORT = 80;

        int HTTPS_PORT = 443;

        char PATH_SEPARATOR_CHAR = '/';

        String PATH_SEPARATOR = "/";

        String PATH_SEPARATOR_ALL = "/*";

        String WEB_INF_PREFIX = "/WEB-INF";

        String WEB_INF = WEB_INF_PREFIX + PATH_SEPARATOR;

        String FORMAT_JSON = "json";

        String FORMAT_XML = "xml";

        String PARAM_FORMAT = "format";

        String PARAM_CALLBACK = "callback";

        String PARAM_RET = "ret";

        String PARAM_MSG = "msg";

        String PARAM_DATA = "data";

        String REDIRECT_URL = "redirect_url";

        String CUSTOM_REDIRECT = "custom_redirect";

        // Http headers

        String HTTP_HEADER_CONNECTION = "Connection";

        String HTTP_HEADER_UPGRADE = "Upgrade";

        String HTTP_HEADER_USER_AGENT = "User-Agent";

        String HTTP_HEADER_REFRESH = "REFRESH";

        String HTTP_HEADER_LOCATION = "Location";
    }

    /**
     * 可选参数常量定义
     */
    public interface Optional {

        /**
         * 保持静默
         */
        String OBSERVE_SILENCE = "observe_silence";

        /**
         * 会话处理器类
         */
        String SYSTEM_USER_SESSION_HANDLER_CLASS = "webmvc.user_session_handler_class";

        /**
         * 会话数据存储适配器类
         */
        String SYSTEM_USER_SESSION_STORAGE_ADAPTER_CLASS = "webmvc.user_session_storage_adapter_class";

        /**
         * 会话安全确认处理器类
         */
        String SYSTEM_USER_SESSION_CONFIRM_HANDLER_CLASS = "webmvc.user_session_confirm_handler_class";

        /**
         * 会话安全确认重定向URL地址, 默认值: "confirm?redirect_url=${redirect_url}"
         */
        String CONFIRM_REDIRECT_URL = "webmvc.confirm_redirect_url";

        /**
         * 会话安全确认超时时间(分钟), 默认值: 30
         */
        String CONFIRM_TIMEOUT = "webmvc.confirm_timeout";

        /**
         * 请求令牌参数名称, 默认值: Request-Token
         */
        String REQUEST_TOKEN_NAME = "webmvc.request_token_name";

        /**
         * 重定向登录URL地址参数名称, 默认值: "login?redirect_url=${redirect_url}"
         */
        String REDIRECT_LOGIN_URL = "webmvc.redirect_login_url";

        /**
         * 重定向自动跳转时间间隔参数名称
         */
        String REDIRECT_TIME_INTERVAL = "webmvc.redirect_time_interval";

        /**
         * 签名验证时间间隔(毫秒), 即当前时间与签名时间戳差值在此值范围内视为有效, 默认值: 0 表示不开始时间间隔验证
         */
        String SIGNATURE_TIMESTAMP_INTERVAL = "webmvc.signature_timestamp_interval";

        /**
         * 允许访问和重定向的主机名称, 多个主机名称用'|'分隔, 默认值: 空(表示不限制)
         */
        String ALLOW_ACCESS_HOSTS = "webmvc.allow_access_hosts";

        /**
         * 允许上传的文件类型验证参数名称
         */
        String VALIDATION_ALLOW_UPLOAD_CONTENT_TYPES = "webmvc.validation_allow_upload_content_types";

        /**
         * 是否开启跨域拦截
         */
        String ALLOW_CROSS_DOMAIN = "webmvc.allow_cross_domain";

        /**
         * 针对OPTIONS请求是否自动回复, 默认: true
         */
        String ALLOW_OPTIONS_AUTO_REPLY = "webmvc.allow_options_auto_reply";

        /**
         * 允许跨域的原始主机
         */
        String ALLOW_ORIGIN_HOSTS = "webmvc.allow_origin_hosts";

        /**
         * 允许跨域请求的方法
         */
        String ALLOW_CROSS_METHODS = "webmvc.allow_cross_methods";

        /**
         * 允许跨域请求携带的请求头
         */
        String ALLOW_CROSS_HEADERS = "webmvc.allow_cross_headers";

        /**
         * 是否允许跨域请求带有验证信息
         */
        String NOT_ALLOW_CREDENTIALS = "not_allow_credentials";
    }

    /**
     * HTTP请求方式枚举
     * <p>
     * Create At 2012-12-10 下午10:34:44
     * </p>
     */
    public enum HttpMethod {

        /**
         * GET
         */
        GET,

        /**
         * HEAD
         */
        HEAD,

        /**
         * POST
         */
        POST,

        /**
         * PUT
         */
        PUT,

        /**
         * DELETE
         */
        DELETE,

        /**
         * OPTIONS
         */
        OPTIONS,

        /**
         * TRACE
         */
        TRACE
    }

    /**
     * HTTP请求头数据类型
     */
    public enum HeaderType {

        /**
         * 字符型
         */
        STRING,

        /**
         * 数值型
         */
        INT,

        /**
         * 日期型
         */
        DATE
    }

    /**
     * 视图类型枚举
     * <p>
     * Create At 2015-6-4 上午08:01:45
     * </p>
     */
    public enum View {

        /**
         * 二进制流
         */
        BINARY,

        /**
         * 重定向
         */
        FORWARD,

        /**
         * Freemarker
         */
        FREEMARKER,

        /**
         * Velocity
         */
        VELOCITY,

        /**
         * HTML
         */
        HTML,

        /**
         * HTTP状态响应
         */
        HTTP_STATES,

        /**
         * JSON
         */
        JSON,

        /**
         * JSP
         */
        JSP,

        /**
         * 空视图
         */
        NULL,

        /**
         * 浏览器重定向
         */
        REDIRECT,

        /**
         * 文本
         */
        TEXT,

        /**
         * BEETL
         */
        BEETL
    }

    public enum ContentType {

        /**
         * 文本
         */
        TEXT("text/plain"),

        /**
         * HTML
         */
        HTML("text/html"),

        /**
         * JSON
         */
        JSON("application/json"),

        /**
         * JS
         */
        JAVASCRIPT("text/javascript"),

        /**
         * 字节流
         */
        OCTET_STREAM("application/octet-stream");

        private final String contentType;

        ContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getContentType() {
            return contentType;
        }

        @Override
        public String toString() {
            return contentType;
        }
    }

    public interface Context {

        String SESSION = "net.ymate.platform.webmvc.context.SESSION";

        String APPLICATION = "net.ymate.platform.webmvc.context.APPLICATION";

        String REQUEST = "net.ymate.platform.webmvc.context.REQUEST";

        String PARAMETERS = "net.ymate.platform.webmvc.context.PARAMETERS";

        String LOCALE = "net.ymate.platform.webmvc.context.LOCALE";

        String HTTP_REQUEST = "net.ymate.platform.webmvc.context.HTTP_SERVLET_REQUEST";

        String HTTP_RESPONSE = "net.ymate.platform.webmvc.context.HTTP_SERVLET_RESPONSE";

        String SERVLET_CONTEXT = "net.ymate.platform.webmvc.context.SERVLET_CONTEXT";

        String PAGE_CONTEXT = "net.ymate.platform.webmvc.context.PAGE_CONTEXT";

        String WEB_REQUEST_CONTEXT = "net.ymate.platform.webmvc.context.WEB_REQUEST_CONTEXT";

        String WEB_CONTEXT_OWNER = "net.ymate.platform.webmvc.context.WEB_CONTEXT_OWNER";
    }
}
