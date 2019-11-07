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

        String UNKNOWN = "unknown";

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
    }

    public interface HttpHead {

        String CONNECTION = "Connection";

        String UPGRADE = "Upgrade";

        String WEBSOCKET = "websocket";

        String USER_AGENT = "User-Agent";

        String REFRESH = "Refresh";

        String LOCATION = "Location";

        String ACCEPT = "Accept";

        String ACCEPT_CHARSET = "Accept-Charset";

        String ACCEPT_ENCODING = "Accept-Encoding";

        String ACCEPT_LANGUAGE = "Accept-Language";

        String ACCEPT_RANGES = "Accept-Ranges";

        String ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";

        String ACCESS_CONTROL_ALLOW_HEADERS = "Access-Control-Allow-Headers";

        String ACCESS_CONTROL_ALLOW_METHODS = "Access-Control-Allow-Methods";

        String ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

        String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";

        String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";

        String ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";

        String ACCESS_CONTROL_REQUEST_METHOD = "Access-Control-Request-Method";

        String AGE = "Age";

        String MAX_AGE = "Max-Age";

        String ALLOW = "Allow";

        String AUTHORIZATION = "Authorization";

        String CACHE_CONTROL = "Cache-Control";

        String CONTENT_ENCODING = "Content-Encoding";

        String CONTENT_DISPOSITION = "Content-Disposition";

        String CONTENT_LANGUAGE = "Content-Language";

        String CONTENT_LENGTH = "Content-Length";

        String CONTENT_LOCATION = "Content-Location";

        String CONTENT_RANGE = "Content-Range";

        String CONTENT_TYPE = "Content-Type";

        String COOKIE = "Cookie";

        String DATE = "Date";

        String ETAG = "ETag";

        String EXPECT = "Expect";

        String EXPIRES = "Expires";

        String GZIP = "gzip";

        String FROM = "From";

        String HOST = "Host";

        String IF_MATCH = "If-Match";

        String IF_MODIFIED_SINCE = "If-Modified-Since";

        String IF_NONE_MATCH = "If-None-Match";

        String IF_RANGE = "If-Range";

        String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";

        String LAST_MODIFIED = "Last-Modified";

        String LINK = "Link";

        String MAX_FORWARDS = "Max-Forwards";

        String ORIGIN = "Origin";

        String PRAGMA = "Pragma";

        String PROXY_AUTHENTICATE = "Proxy-Authenticate";

        String PROXY_AUTHORIZATION = "Proxy-Authorization";

        String RANGE = "Range";

        String REFERER = "Referer";

        String RETRY_AFTER = "Retry-After";

        String SERVER = "Server";

        String SET_COOKIE = "Set-Cookie";

        String SET_COOKIE2 = "Set-Cookie2";

        String TE = "TE";

        String TRAILER = "Trailer";

        String TRANSFER_ENCODING = "Transfer-Encoding";

        String VARY = "Vary";

        String VIA = "Via";

        String WARNING = "Warning";

        String WWW_AUTHENTICATE = "WWW-Authenticate";

        String X_REQUESTED_WITH = "X-Requested-With";

        String XML_HTTP_REQUEST = "XMLHttpRequest";

        String X_FORWARDED_FOR = "X-Forwarded-For";

        String PROXY_CLIENT_IP = "Proxy-Client-IP";

        String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
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
         * PATCH
         */
        PATCH,

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
        TEXT
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
         * XML
         */
        XML("application/xml"),

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
