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
 * @version 1.0
 */
public class Type {

    /**
     * 常量
     */
    public interface Const {

        String FORMAT_JSON = "json";
        String FORMAT_XML = "xml";

        String PARAM_FORMAT = "format";
        String PARAM_CALLBACK = "callback";
        String PARAM_RET = "ret";
        String PARAM_MSG = "msg";
        String PARAM_DATA = "data";

        String REDIRECT_URL = "redirect_url";

        String CUSTOM_REDIRECT = "custom_redirect";

        // ----------

        /**
         * 控制器请求URL后缀参数名称
         */
        String REQUEST_SUFFIX = "webmvc.request_suffix";

        /**
         * 服务名称参数, 默认值: request.getServerName();
         */
        String SERVER_NAME = "webmvc.server_name";

        // ----------

        /**
         * 系统异常分析是否关闭参数名称
         */
        String SYSTEM_EXCEPTION_ANALYSIS_DISABLED = "webmvc.exception_analysis_disabled";

        /**
         * 系统错误消息是否指定ContentType响应头
         */
        String SYSTEM_ERROR_WITH_CONTENT_TYPE = "webmvc.error_with_content_type";

        /**
         * 默认异常响应视图格式, 默认值: "", 可选范围: json|xml
         */
        String ERROR_DEFAULT_VIEW_FORMAT = "webmvc.error_default_view_format";

        /**
         * 异常信息视图文件参数名称
         */
        String ERROR_VIEW = "webmvc.error_view";

        // ----------

        /**
         * 验证结果消息模板参数名称, 默认值: "${items}"
         */
        String VALIDATION_TEMPLATE_ELEMENT = "webmvc.validation_template_element";

        /**
         * 验证结果消息项模板参数名称, 默认值: "${message}<br>"
         */
        String VALIDATION_TEMPLATE_ITEM = "webmvc.validation_template_item";

        // ----------

        /**
         * 重定向主页URL地址参数名称, 默认值: ""
         */
        String REDIRECT_HOME_URL = "webmvc.redirect_home_url";

        /**
         * 自定义重定向URL地址参数名称
         */
        String REDIRECT_CUSTOM_URL = "webmvc.redirect_custom_url";
    }

    /**
     * HTTP请求方式枚举
     * <p>
     * Create At 2012-12-10 下午10:34:44
     * </p>
     */
    public enum HttpMethod {
        GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE
    }

    /**
     * HTTP请求头数据类型
     */
    public enum HeaderType {
        STRING, INT, DATE
    }

    /**
     * 字符串参数转义范围
     */
    public enum EscapeScope {
        JAVA, JS, HTML, XML, SQL, CSV, DEFAULT
    }

    /**
     * 执行字符串参数转义顺序
     */
    public enum EscapeOrder {
        BEFORE, AFTER
    }

    /**
     * 视图类型枚举
     * <p>
     * Create At 2015-6-4 上午08:01:45
     * </p>
     */
    public enum View {
        BINARY, FORWARD, FREEMARKER, VELOCITY, HTML, HTTP_STATES, JSON, JSP, NULL, REDIRECT, TEXT, BEETL
    }

    public enum ContentType {

        TEXT("text/plain"),
        HTML("text/html"),
        JSON("application/json"),
        JAVASCRIPT("text/javascript"),

        /**
         * 字节流
         */
        OCTET_STREAM("application/octet-stream");

        private final String __contentType;

        ContentType(String contentType) {
            __contentType = contentType;
        }

        public String getContentType() {
            return __contentType;
        }

        @Override
        public String toString() {
            return __contentType;
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

    public final static Map<String, Class<? extends IRequestProcessor>> REQUEST_PROCESSORS;

    public final static Map<String, Class<? extends IRequestMappingParser>> REQUEST_MAPPING_PARSERS;

    public final static Map<Integer, String> HTTP_STATUS;

    static {
        REQUEST_PROCESSORS = new HashMap<String, Class<? extends IRequestProcessor>>();
        REQUEST_PROCESSORS.put("default", DefaultRequestProcessor.class);
        REQUEST_PROCESSORS.put("json", JSONRequestProcessor.class);
        REQUEST_PROCESSORS.put("xml", XMLRequestProcessor.class);
        //
        REQUEST_MAPPING_PARSERS = new HashMap<String, Class<? extends IRequestMappingParser>>();
        REQUEST_MAPPING_PARSERS.put("default", DefaultRequestMappingParser.class);
        //
        Map<Integer, String> _httpStatus = new HashMap<Integer, String>();
        //
        _httpStatus.put(400, "Bad Request");
        _httpStatus.put(401, "Unauthorized");
        _httpStatus.put(402, "Payment Required");
        _httpStatus.put(403, "Forbidden");
        _httpStatus.put(404, "Not Found");
        _httpStatus.put(405, "Method Not Allowed");
        _httpStatus.put(406, "Not Acceptable");
        _httpStatus.put(407, "Proxy Authentication Required");
        _httpStatus.put(408, "Request Timeout");
        _httpStatus.put(409, "Conflict");
        _httpStatus.put(410, "Gone");
        _httpStatus.put(411, "Length Required");
        _httpStatus.put(412, "Precondition Failed");
        _httpStatus.put(413, "Request Entity Too Large");
        _httpStatus.put(414, "Request URI Too Long");
        _httpStatus.put(415, "Unsupported Media Type");
        _httpStatus.put(416, "Requested Range Not Satisfiable");
        _httpStatus.put(417, "Expectation Failed");
        _httpStatus.put(421, "Too Many Connections");
        _httpStatus.put(422, "Unprocessable Entity");
        _httpStatus.put(423, "Locked");
        _httpStatus.put(424, "Failed Dependency");
        _httpStatus.put(425, "Unordered Collection");
        _httpStatus.put(426, "Ungrade Required");
        _httpStatus.put(449, "Retry With");
        _httpStatus.put(451, "Unavailable For Legal Reasons");
        //
        _httpStatus.put(500, "Internal Server Error");
        _httpStatus.put(501, "Not Implemented");
        _httpStatus.put(502, "Bad Gateway");
        _httpStatus.put(503, "Service Unavailable");
        _httpStatus.put(504, "Gateway Timeout");
        _httpStatus.put(505, "HTTP Version Not Supported");
        _httpStatus.put(506, "Variant Also Negotiates");
        _httpStatus.put(507, "Insufficient Storage");
        _httpStatus.put(509, "Bandwidth Limit Exceeded");
        _httpStatus.put(510, "Not Extended");
        //
        HTTP_STATUS = Collections.unmodifiableMap(_httpStatus);
    }
}
