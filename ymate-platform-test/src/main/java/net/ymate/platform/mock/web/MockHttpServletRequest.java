/*
 * Copyright 2007-present the original author or authors.
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
package net.ymate.platform.mock.web;

import net.ymate.platform.mock.MockUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;

public class MockHttpServletRequest implements HttpServletRequest {

    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final String HOST_HEADER = "Host";

    private static final String CHARSET_PREFIX = "charset=";

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static final ServletInputStream EMPTY_SERVLET_INPUT_STREAM = new DelegatingServletInputStream(IOUtils.toInputStream("", StandardCharsets.UTF_8));

    private static final BufferedReader EMPTY_BUFFERED_READER = new BufferedReader(new StringReader(""));

    private static final String[] DATE_FORMATS = new String[]{
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy"
    };

    public static final String DEFAULT_PROTOCOL = "HTTP/1.1";

    public static final String DEFAULT_SCHEME = HTTP;

    public static final String DEFAULT_SERVER_ADDR = "127.0.0.1";

    public static final String DEFAULT_SERVER_NAME = "localhost";

    public static final int DEFAULT_SERVER_PORT = 80;

    public static final String DEFAULT_REMOTE_ADDR = "127.0.0.1";

    public static final String DEFAULT_REMOTE_HOST = "localhost";

    private final ServletContext servletContext;

    private boolean active = true;

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    private String characterEncoding;

    private byte[] content;

    private String contentType;

    private final Map<String, String[]> parameters = new LinkedHashMap<>();

    private String protocol = DEFAULT_PROTOCOL;

    private String scheme = DEFAULT_SCHEME;

    private String serverName = DEFAULT_SERVER_NAME;

    private int serverPort = DEFAULT_SERVER_PORT;

    private String remoteAddr = DEFAULT_REMOTE_ADDR;

    private String remoteHost = DEFAULT_REMOTE_HOST;

    private final List<Locale> locales = new LinkedList<>();

    private boolean secure = false;

    private int remotePort = DEFAULT_SERVER_PORT;

    private String localName = DEFAULT_SERVER_NAME;

    private String localAddr = DEFAULT_SERVER_ADDR;

    private int localPort = DEFAULT_SERVER_PORT;

    private boolean asyncStarted = false;

    private boolean asyncSupported = false;

    private MockAsyncContext asyncContext;

    private DispatcherType dispatcherType = DispatcherType.REQUEST;

    private String authType;

    private Cookie[] cookies;

    private final Map<String, HeaderValueHolder> headers = new LinkedHashMap<>();

    private String method;

    private String pathInfo;

    private String contextPath = "";

    private String queryString;

    private String remoteUser;

    private final Set<String> userRoles = new HashSet<>();

    private Principal userPrincipal;

    private String requestedSessionId;

    private String requestURI;

    private String servletPath = "";

    private HttpSession session;

    private boolean requestedSessionIdValid = true;

    private boolean requestedSessionIdFromCookie = true;

    private boolean requestedSessionIdFromURL = false;

    private final Map<String, Part> parts = new LinkedHashMap<>();

    /**
     * 无参构造方法，创建默认的MockHttpServletRequest
     */
    public MockHttpServletRequest() {
        this(null, "", "");
    }

    /**
     * 使用指定的请求方法和URI创建MockHttpServletRequest
     *
     * @param method     请求方法
     * @param requestURI 请求URI
     */
    public MockHttpServletRequest(String method, String requestURI) {
        this(null, method, requestURI);
    }

    /**
     * 使用指定的ServletContext创建MockHttpServletRequest
     *
     * @param servletContext ServletContext
     */
    public MockHttpServletRequest(ServletContext servletContext) {
        this(servletContext, "", "");
    }

    /**
     * 使用指定的ServletContext、请求方法和URI创建MockHttpServletRequest
     *
     * @param servletContext ServletContext
     * @param method         请求方法
     * @param requestURI     请求URI
     */
    public MockHttpServletRequest(ServletContext servletContext, String method, String requestURI) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.method = method;
        this.requestURI = requestURI;
        this.locales.add(Locale.ENGLISH);
    }

    /**
     * 获取ServletContext
     *
     * @return ServletContext
     */
    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    /**
     * 检查请求是否处于活动状态
     *
     * @return 是否活动
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * 关闭请求，将其标记为非活动状态
     */
    public void close() {
        this.active = false;
    }

    /**
     * 使请求无效，关闭并清除所有属性
     */
    public void invalidate() {
        close();
        clearAttributes();
    }

    /**
     * 检查请求是否处于活动状态，否则抛出异常
     *
     * @throws IllegalStateException 如果请求已关闭
     */
    protected void checkActive() throws IllegalStateException {
        if (!this.active) {
            throw new IllegalStateException("Request is not active anymore");
        }
    }

    /**
     * 获取请求属性
     *
     * @param name 属性名称
     * @return 属性值
     * @throws IllegalStateException 如果请求已关闭
     */
    @Override
    public Object getAttribute(String name) {
        checkActive();
        return this.attributes.get(name);
    }

    /**
     * 获取所有请求属性名称
     *
     * @return 属性名称枚举
     * @throws IllegalStateException 如果请求已关闭
     */
    @Override
    public Enumeration<String> getAttributeNames() {
        checkActive();
        return Collections.enumeration(new LinkedHashSet<>(this.attributes.keySet()));
    }

    /**
     * 获取字符编码
     *
     * @return 字符编码
     */
    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    /**
     * 设置字符编码
     *
     * @param characterEncoding 字符编码
     */
    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
        updateContentTypeHeader();
    }

    /**
     * 更新Content-Type头部，添加字符编码信息
     */
    private void updateContentTypeHeader() {
        if (StringUtils.isNotBlank(this.contentType)) {
            StringBuilder sb = new StringBuilder(this.contentType);
            if (!this.contentType.toLowerCase().contains(CHARSET_PREFIX) &&
                    StringUtils.isNotBlank(this.characterEncoding)) {
                sb.append(";").append(CHARSET_PREFIX).append(this.characterEncoding);
            }
            doAddHeaderValue(CONTENT_TYPE_HEADER, sb.toString(), true);
        }
    }

    /**
     * 设置请求内容
     *
     * @param content 内容字节数组
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * 获取内容长度
     *
     * @return 内容长度
     */
    @Override
    public int getContentLength() {
        return (this.content != null ? this.content.length : -1);
    }

    /**
     * 获取内容长度（长整型）
     *
     * @return 内容长度
     */
    public long getContentLengthLong() {
        return getContentLength();
    }

    /**
     * 设置内容类型
     *
     * @param contentType 内容类型
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
        if (contentType != null) {
            int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
            if (charsetIndex != -1) {
                this.characterEncoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
            }
            updateContentTypeHeader();
        }
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    /**
     * 获取ServletInputStream
     *
     * @return ServletInputStream
     */
    @Override
    public ServletInputStream getInputStream() {
        if (this.content != null) {
            return new DelegatingServletInputStream(new ByteArrayInputStream(this.content));
        }
        return EMPTY_SERVLET_INPUT_STREAM;
    }

    /**
     * 设置请求参数
     *
     * @param name  参数名称
     * @param value 参数值
     */
    public void setParameter(String name, String value) {
        setParameter(name, new String[]{value});
    }

    /**
     * 设置请求参数（多个值）
     *
     * @param name   参数名称
     * @param values 参数值数组
     * @throws NullPointerException 如果name为null
     */
    public void setParameter(String name, String... values) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        this.parameters.put(name, values);
    }

    /**
     * 设置请求参数（从Map）
     *
     * @param params 参数Map
     * @throws NullPointerException     如果params为null
     * @throws IllegalArgumentException 如果参数值类型不正确
     */
    public void setParameters(Map<String, ?> params) {
        Objects.requireNonNull(params, "Parameter map must not be null");
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value instanceof String) {
                setParameter(key, (String) value);
            } else if (value instanceof String[]) {
                setParameter(key, (String[]) value);
            } else {
                throw new IllegalArgumentException("Parameter map value must be single value " + " or array of type [" + String.class.getName() + "]");
            }
        }
    }

    /**
     * 添加请求参数
     *
     * @param name  参数名称
     * @param value 参数值
     */
    public void addParameter(String name, String value) {
        addParameter(name, new String[]{value});
    }

    /**
     * 添加请求参数（多个值）
     *
     * @param name   参数名称
     * @param values 参数值数组
     * @throws NullPointerException 如果name为null
     */
    public void addParameter(String name, String... values) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        String[] oldArr = this.parameters.get(name);
        if (oldArr != null) {
            String[] newArr = new String[oldArr.length + values.length];
            System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
            System.arraycopy(values, 0, newArr, oldArr.length, values.length);
            this.parameters.put(name, newArr);
        } else {
            this.parameters.put(name, values);
        }
    }

    /**
     * 添加请求参数（从Map）
     *
     * @param params 参数Map
     * @throws NullPointerException     如果params为null
     * @throws IllegalArgumentException 如果参数值类型不正确
     */
    public void addParameters(Map<String, ?> params) {
        Objects.requireNonNull(params, "Parameter map must not be null");
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value instanceof String) {
                addParameter(key, (String) value);
            } else if (value instanceof String[]) {
                addParameter(key, (String[]) value);
            } else {
                throw new IllegalArgumentException("Parameter map value must be single value " + " or array of type [" + String.class.getName() + "]");
            }
        }
    }

    /**
     * 移除请求参数
     *
     * @param name 参数名称
     * @throws NullPointerException 如果name为null
     */
    public void removeParameter(String name) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        this.parameters.remove(name);
    }

    /**
     * 移除所有请求参数
     */
    public void removeAllParameters() {
        this.parameters.clear();
    }

    /**
     * 获取请求参数
     *
     * @param name 参数名称
     * @return 参数值
     */
    @Override
    public String getParameter(String name) {
        String[] arr = (name != null ? this.parameters.get(name) : null);
        return (arr != null && arr.length > 0 ? arr[0] : null);
    }

    /**
     * 获取所有请求参数名称
     *
     * @return 参数名称枚举
     */
    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    /**
     * 获取请求参数值数组
     *
     * @param name 参数名称
     * @return 参数值数组
     */
    @Override
    public String[] getParameterValues(String name) {
        return (name != null ? this.parameters.get(name) : null);
    }

    /**
     * 获取请求参数Map
     *
     * @return 参数Map
     */
    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(this.parameters);
    }

    /**
     * 设置协议
     *
     * @param protocol 协议
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * 获取协议
     *
     * @return 协议
     */
    @Override
    public String getProtocol() {
        return this.protocol;
    }

    /**
     * 设置方案
     *
     * @param scheme 方案
     */
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    /**
     * 获取方案
     *
     * @return 方案
     */
    @Override
    public String getScheme() {
        return this.scheme;
    }

    /**
     * 设置服务器名称
     *
     * @param serverName 服务器名称
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * 获取服务器名称
     *
     * @return 服务器名称
     * @throws IllegalArgumentException 如果Host头部格式无效
     */
    @Override
    public String getServerName() {
        String rawHostHeader = getHeader(HOST_HEADER);
        String host = rawHostHeader;
        if (host != null) {
            host = host.trim();
            if (host.startsWith("[")) {
                int indexOfClosingBracket = host.indexOf(']');
                if (indexOfClosingBracket == -1) {
                    throw new IllegalArgumentException("Invalid Host header: " + rawHostHeader);
                }
                host = host.substring(0, indexOfClosingBracket + 1);
            } else if (host.contains(":")) {
                host = host.substring(0, host.indexOf(':'));
            }
            return host;
        }
        return this.serverName;
    }

    /**
     * 设置服务器端口
     *
     * @param serverPort 服务器端口
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * 获取服务器端口
     *
     * @return 服务器端口
     * @throws IllegalArgumentException 如果Host头部格式无效
     */
    @Override
    public int getServerPort() {
        String rawHostHeader = getHeader(HOST_HEADER);
        String host = rawHostHeader;
        if (host != null) {
            host = host.trim();
            int idx;
            if (host.startsWith("[")) {
                int indexOfClosingBracket = host.indexOf(']');
                if (indexOfClosingBracket == -1) {
                    throw new IllegalArgumentException("Invalid Host header: " + rawHostHeader);
                }
                idx = host.indexOf(':', indexOfClosingBracket);
            } else {
                idx = host.indexOf(':');
            }
            if (idx != -1) {
                return Integer.parseInt(host.substring(idx + 1));
            }
        }
        return this.serverPort;
    }

    /**
     * 获取BufferedReader
     *
     * @return BufferedReader
     * @throws UnsupportedEncodingException 如果字符编码不支持
     */
    @Override
    public BufferedReader getReader() throws UnsupportedEncodingException {
        if (this.content != null) {
            InputStream sourceStream = new ByteArrayInputStream(this.content);
            Reader sourceReader = (this.characterEncoding != null) ?
                    new InputStreamReader(sourceStream, this.characterEncoding) :
                    new InputStreamReader(sourceStream);
            return new BufferedReader(sourceReader);
        } else {
            return EMPTY_BUFFERED_READER;
        }
    }

    /**
     * 设置远程地址
     *
     * @param remoteAddr 远程地址
     */
    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    /**
     * 获取远程地址
     *
     * @return 远程地址
     */
    @Override
    public String getRemoteAddr() {
        return this.remoteAddr;
    }

    /**
     * 设置远程主机
     *
     * @param remoteHost 远程主机
     */
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    /**
     * 获取远程主机
     *
     * @return 远程主机
     */
    @Override
    public String getRemoteHost() {
        return this.remoteHost;
    }

    /**
     * 设置请求属性
     *
     * @param name  属性名称
     * @param value 属性值
     * @throws IllegalStateException 如果请求已关闭
     * @throws NullPointerException  如果name为null
     */
    @Override
    public void setAttribute(String name, Object value) {
        checkActive();
        Objects.requireNonNull(name, "Attribute name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        checkActive();
        Objects.requireNonNull(name, "Attribute name must not be null");
        this.attributes.remove(name);
    }

    public void clearAttributes() {
        this.attributes.clear();
    }

    public void addPreferredLocale(Locale locale) {
        Objects.requireNonNull(locale, "Locale must not be null");
        this.locales.add(0, locale);
    }

    public void setPreferredLocales(List<Locale> locales) {
        Objects.requireNonNull(locales, "Locale list must not be null");
        if (locales.isEmpty()) {
            throw new IllegalArgumentException("Locale list must not be empty");
        }
        this.locales.clear();
        this.locales.addAll(locales);
    }

    @Override
    public Locale getLocale() {
        return this.locales.get(0);
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(this.locales);
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    @Override
    public boolean isSecure() {
        return (this.secure || HTTPS.equalsIgnoreCase(this.scheme));
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return new MockRequestDispatcher(path);
    }

    @Override
    @Deprecated
    public String getRealPath(String path) {
        return this.servletContext.getRealPath(path);
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    @Override
    public int getRemotePort() {
        return this.remotePort;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Override
    public String getLocalName() {
        return this.localName;
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    @Override
    public String getLocalAddr() {
        return this.localAddr;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    @Override
    public int getLocalPort() {
        return this.localPort;
    }

    @Override
    public AsyncContext startAsync() {
        return startAsync(this, null);
    }

    @Override
    public AsyncContext startAsync(ServletRequest request, ServletResponse response) {
        if (!this.asyncSupported) {
            throw new IllegalStateException("Async not supported");
        }
        this.asyncStarted = true;
        this.asyncContext = new MockAsyncContext(request, response);
        return this.asyncContext;
    }

    public void setAsyncStarted(boolean asyncStarted) {
        this.asyncStarted = asyncStarted;
    }

    @Override
    public boolean isAsyncStarted() {
        return this.asyncStarted;
    }

    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    @Override
    public boolean isAsyncSupported() {
        return this.asyncSupported;
    }

    public void setAsyncContext(MockAsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return this.asyncContext;
    }

    public void setDispatcherType(DispatcherType dispatcherType) {
        this.dispatcherType = dispatcherType;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return this.dispatcherType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    @Override
    public String getAuthType() {
        return this.authType;
    }

    public void setCookies(Cookie... cookies) {
        this.cookies = cookies;
    }

    @Override
    public Cookie[] getCookies() {
        return this.cookies;
    }

    public void addHeader(String name, Object value) {
        if (CONTENT_TYPE_HEADER.equalsIgnoreCase(name) && !this.headers.containsKey(CONTENT_TYPE_HEADER)) {
            setContentType(value.toString());
        } else {
            doAddHeaderValue(name, value, false);
        }
    }

    private void doAddHeaderValue(String name, Object value, boolean replace) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        Objects.requireNonNull(value, "Header value must not be null");
        if (header == null || replace) {
            header = new HeaderValueHolder();
            this.headers.put(name, header);
        }
        if (value instanceof Collection) {
            header.addValues((Collection<?>) value);
        } else if (value.getClass().isArray()) {
            header.addValueArray(value);
        } else {
            header.addValue(value);
        }
    }

    public void removeHeader(String name) {
        Objects.requireNonNull(name, "Header name must not be null");
        this.headers.remove(name);
    }

    @Override
    public long getDateHeader(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        Object value = (header != null ? header.getValue() : null);
        if (value instanceof Date) {
            return ((Date) value).getTime();
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return MockUtils.parseDateHeader(name, (String) value);
        } else if (value != null) {
            throw new IllegalArgumentException("Value for header '" + name + "' is not a Date, Number, or String: " + value);
        } else {
            return -1L;
        }
    }

    @Override
    public String getHeader(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        return (header != null ? header.getStringValue() : null);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        return Collections.enumeration(header != null ? header.getStringValues() : new LinkedList<String>());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.headers.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        HeaderValueHolder header = HeaderValueHolder.getByName(this.headers, name);
        Object value = (header != null ? header.getValue() : null);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        } else if (value != null) {
            throw new NumberFormatException("Value for header '" + name + "' is not a Number: " + value);
        } else {
            return -1;
        }
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    public String getPathInfo() {
        return this.pathInfo;
    }

    @Override
    public String getPathTranslated() {
        return (this.pathInfo != null ? getRealPath(this.pathInfo) : null);
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getQueryString() {
        return this.queryString;
    }

    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    @Override
    public String getRemoteUser() {
        return this.remoteUser;
    }

    public void addUserRole(String role) {
        this.userRoles.add(role);
    }

    @Override
    public boolean isUserInRole(String role) {
        return (this.userRoles.contains(role) || (this.servletContext instanceof MockServletContext &&
                ((MockServletContext) this.servletContext).getDeclaredRoles().contains(role)));
    }

    public void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override
    public Principal getUserPrincipal() {
        return this.userPrincipal;
    }

    public void setRequestedSessionId(String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    @Override
    public String getRequestedSessionId() {
        return this.requestedSessionId;
    }

    public void setRequestURI(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public String getRequestURI() {
        return this.requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        String scheme = getScheme();
        String server = getServerName();
        int port = getServerPort();
        String uri = getRequestURI();

        StringBuffer url = new StringBuffer(scheme).append("://").append(server);
        if (port > 0 && ((HTTP.equalsIgnoreCase(scheme) && port != 80) ||
                (HTTPS.equalsIgnoreCase(scheme) && port != 443))) {
            url.append(':').append(port);
        }
        if (StringUtils.isNotBlank(uri)) {
            url.append(uri);
        }
        return url;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public String getServletPath() {
        return this.servletPath;
    }

    public void setSession(HttpSession session) {
        this.session = session;
        if (session instanceof MockHttpSession) {
            MockHttpSession mockSession = ((MockHttpSession) session);
            mockSession.access();
        }
    }

    @Override
    public HttpSession getSession(boolean create) {
        checkActive();
        if (this.session instanceof MockHttpSession && ((MockHttpSession) this.session).isInvalid()) {
            this.session = null;
        }
        if (this.session == null && create) {
            this.session = new MockHttpSession(this.servletContext);
        }
        return this.session;
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    public String changeSessionId() {
        Objects.requireNonNull(this.session, "The request does not have a session");
        if (this.session instanceof MockHttpSession) {
            return ((MockHttpSession) this.session).changeSessionId();
        }
        return this.session.getId();
    }

    public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
        this.requestedSessionIdValid = requestedSessionIdValid;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return this.requestedSessionIdValid;
    }

    public void setRequestedSessionIdFromCookie(boolean requestedSessionIdFromCookie) {
        this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return this.requestedSessionIdFromCookie;
    }

    public void setRequestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
        this.requestedSessionIdFromURL = requestedSessionIdFromURL;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return this.requestedSessionIdFromURL;
    }

    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() throws ServletException {
        this.userPrincipal = null;
        this.remoteUser = null;
        this.authType = null;
    }

    public void addPart(Part part) {
        parts.put(part.getName(), part);
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        return this.parts.get(name);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return parts.values();
    }

    public static class Builder {
        private ServletContext servletContext;
        private String method;
        private String requestURI;
        private String contextPath;
        private String servletPath;
        private String pathInfo;
        private String queryString;
        private String protocol;
        private String scheme;
        private String serverName;
        private int serverPort;
        private String remoteAddr;
        private String remoteHost;
        private String remoteUser;
        private Principal userPrincipal;
        private boolean secure;
        private boolean requestedSessionIdValid;
        private boolean requestedSessionIdFromCookie;
        private boolean requestedSessionIdFromURL;
        private String requestedSessionId;
        private String characterEncoding;
        private byte[] content;
        private String contentType;
        private boolean asyncSupported;
        private String authType;
        private Cookie[] cookies;
        private final Map<String, Object> attributes = new LinkedHashMap<>();
        private final Map<String, String[]> parameters = new LinkedHashMap<>();
        private final Map<String, Object> headers = new LinkedHashMap<>();
        private final List<Locale> locales = new LinkedList<>();
        private final Set<String> userRoles = new HashSet<>();

        public static Builder create() {
            return new Builder();
        }

        public Builder servletContext(ServletContext servletContext) {
            this.servletContext = servletContext;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder requestURI(String requestURI) {
            this.requestURI = requestURI;
            return this;
        }

        public Builder contextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public Builder servletPath(String servletPath) {
            this.servletPath = servletPath;
            return this;
        }

        public Builder pathInfo(String pathInfo) {
            this.pathInfo = pathInfo;
            return this;
        }

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder serverName(String serverName) {
            this.serverName = serverName;
            return this;
        }

        public Builder serverPort(int serverPort) {
            this.serverPort = serverPort;
            return this;
        }

        public Builder remoteAddr(String remoteAddr) {
            this.remoteAddr = remoteAddr;
            return this;
        }

        public Builder remoteHost(String remoteHost) {
            this.remoteHost = remoteHost;
            return this;
        }

        public Builder remoteUser(String remoteUser) {
            this.remoteUser = remoteUser;
            return this;
        }

        public Builder userPrincipal(Principal userPrincipal) {
            this.userPrincipal = userPrincipal;
            return this;
        }

        public Builder secure(boolean secure) {
            this.secure = secure;
            return this;
        }

        public Builder requestedSessionIdValid(boolean requestedSessionIdValid) {
            this.requestedSessionIdValid = requestedSessionIdValid;
            return this;
        }

        public Builder requestedSessionIdFromCookie(boolean requestedSessionIdFromCookie) {
            this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
            return this;
        }

        public Builder requestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
            this.requestedSessionIdFromURL = requestedSessionIdFromURL;
            return this;
        }

        public Builder requestedSessionId(String requestedSessionId) {
            this.requestedSessionId = requestedSessionId;
            return this;
        }

        public Builder characterEncoding(String characterEncoding) {
            this.characterEncoding = characterEncoding;
            return this;
        }

        public Builder content(byte[] content) {
            this.content = content;
            return this;
        }

        public Builder content(String content) {
            try {
                this.content = content.getBytes(characterEncoding != null ? characterEncoding : "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new IllegalStateException("Failed to convert content to bytes", ex);
            }
            return this;
        }

        public Builder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public Builder asyncSupported(boolean asyncSupported) {
            this.asyncSupported = asyncSupported;
            return this;
        }

        public Builder authType(String authType) {
            this.authType = authType;
            return this;
        }

        public Builder cookies(Cookie... cookies) {
            this.cookies = cookies;
            return this;
        }

        public Builder addAttribute(String name, Object value) {
            Objects.requireNonNull(name, "Attribute name must not be null");
            this.attributes.put(name, value);
            return this;
        }

        public Builder addParameter(String name, String value) {
            Objects.requireNonNull(name, "Parameter name must not be null");
            this.parameters.put(name, new String[]{value});
            return this;
        }

        public Builder addParameter(String name, String... values) {
            Objects.requireNonNull(name, "Parameter name must not be null");
            this.parameters.put(name, values);
            return this;
        }

        public Builder addHeader(String name, Object value) {
            Objects.requireNonNull(name, "Header name must not be null");
            this.headers.put(name, value);
            return this;
        }

        public Builder addLocale(Locale locale) {
            Objects.requireNonNull(locale, "Locale must not be null");
            this.locales.add(locale);
            return this;
        }

        public Builder addUserRole(String role) {
            Objects.requireNonNull(role, "Role must not be null");
            this.userRoles.add(role);
            return this;
        }

        public MockHttpServletRequest build() {
            MockHttpServletRequest request = new MockHttpServletRequest(servletContext, method, requestURI);
            if (contextPath != null) {
                request.setContextPath(contextPath);
            }
            if (servletPath != null) {
                request.setServletPath(servletPath);
            }
            if (pathInfo != null) {
                request.setPathInfo(pathInfo);
            }
            if (queryString != null) {
                request.setQueryString(queryString);
            }
            if (protocol != null) {
                request.setProtocol(protocol);
            }
            if (scheme != null) {
                request.setScheme(scheme);
            }
            if (serverName != null) {
                request.setServerName(serverName);
            }
            if (serverPort > 0) {
                request.setServerPort(serverPort);
            }
            if (remoteAddr != null) {
                request.setRemoteAddr(remoteAddr);
            }
            if (remoteHost != null) {
                request.setRemoteHost(remoteHost);
            }
            if (remoteUser != null) {
                request.setRemoteUser(remoteUser);
            }
            if (userPrincipal != null) {
                request.setUserPrincipal(userPrincipal);
            }
            request.setSecure(secure);
            if (requestedSessionIdValid) {
                request.setRequestedSessionIdValid(requestedSessionIdValid);
            }
            if (requestedSessionIdFromCookie) {
                request.setRequestedSessionIdFromCookie(requestedSessionIdFromCookie);
            }
            if (requestedSessionIdFromURL) {
                request.setRequestedSessionIdFromURL(requestedSessionIdFromURL);
            }
            if (requestedSessionId != null) {
                request.setRequestedSessionId(requestedSessionId);
            }
            if (characterEncoding != null) {
                request.setCharacterEncoding(characterEncoding);
            }
            if (content != null) {
                request.setContent(content);
            }
            if (contentType != null) {
                request.setContentType(contentType);
            }
            if (asyncSupported) {
                request.setAsyncSupported(asyncSupported);
            }
            if (authType != null) {
                request.setAuthType(authType);
            }
            if (cookies != null) {
                request.setCookies(cookies);
            }
            attributes.forEach(request::setAttribute);
            parameters.forEach(request::setParameter);
            headers.forEach(request::addHeader);
            for (Locale locale : locales) {
                request.addPreferredLocale(locale);
            }
            for (String role : userRoles) {
                request.addUserRole(role);
            }
            return request;
        }
    }
}
