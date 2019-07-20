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
package net.ymate.platform.webmvc.util;

import net.ymate.platform.commons.util.CodecUtils;
import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.commons.util.NetworkUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.support.IContext;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.IWebMvcConfig;
import net.ymate.platform.webmvc.WebMVC;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;
import net.ymate.platform.webmvc.view.impl.FreemarkerView;
import net.ymate.platform.webmvc.view.impl.VelocityView;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Web通用工具类
 *
 * @author 刘镇 (suninformation@163.com) on 14-7-6
 * @since 2.0.6
 */
public class WebUtils {

    private static final Log LOG = LogFactory.getLog(WebUtils.class);

    private static String doGetConfigValue(String confName, String defaultName) {
        return getOwner().getOwner().getParam(confName, defaultName);
    }

    private static String doGetConfigValue(IApplication owner, String confName, String defaultName) {
        return owner.getParam(confName, defaultName);
    }

    private static String doGetSafeServerName(HttpServletRequest request) {
        return doGetConfigValue(IWebMvcConfig.PARAMS_SERVER_NAME, request.getServerName());
    }

    public static IWebMvc getOwner() {
        if (WebContext.getContext() != null) {
            return WebContext.getContext().getOwner();
        }
        return WebMVC.get();
    }

    /**
     * @param request      HttpServletRequest对象
     * @param requestPath  控制器路径
     * @param withBasePath 是否采用完整路径（即非相对路径）
     * @return 构建控制器URL访问路径
     */
    public static String buildUrl(HttpServletRequest request, String requestPath, boolean withBasePath) {
        requestPath = StringUtils.trimToEmpty(requestPath);
        if (withBasePath && StringUtils.isNotBlank(requestPath) && requestPath.charAt(0) == Type.Const.PATH_SEPARATOR_CHAR) {
            requestPath = StringUtils.substringAfter(requestPath, Type.Const.PATH_SEPARATOR);
        }
        return (withBasePath ? baseUrl(request) + requestPath : requestPath) + doGetConfigValue(IWebMvcConfig.PARAMS_REQUEST_SUFFIX, StringUtils.EMPTY);
    }

    /**
     * @param request HttpServletRequest对象
     * @return 获取当前站点基准URL
     */
    public static String baseUrl(HttpServletRequest request) {
        StringBuilder basePath = new StringBuilder();
        String serverName = doGetSafeServerName(request);
        if (!StringUtils.startsWithAny(StringUtils.lowerCase(serverName), new String[]{Type.Const.HTTP_PREFIX, Type.Const.HTTPS_PREFIX})) {
            basePath.append(request.getScheme()).append("://").append(serverName);
            if (request.getServerPort() != Type.Const.HTTP_PORT && request.getServerPort() != Type.Const.HTTPS_PORT) {
                basePath.append(":").append(request.getServerPort());
            }
            if (StringUtils.isNotBlank(request.getContextPath())) {
                basePath.append(request.getContextPath());
            }
        } else {
            basePath.append(serverName);
        }
        if (basePath.charAt(basePath.length() - 1) != Type.Const.PATH_SEPARATOR_CHAR) {
            basePath.append(Type.Const.PATH_SEPARATOR);
        }
        return basePath.toString();
    }

    /**
     * @param request HttpServletRequest对象
     * @param encode  是否编码
     * @return 拼装当前URL请求地址(含QueryString串)
     */
    public static String appendQueryStr(HttpServletRequest request, boolean encode) {
        StringBuffer queryStrBuilder = request.getRequestURL();
        String queryString = request.getQueryString();
        if (StringUtils.isNotBlank(queryString)) {
            queryStrBuilder.append("?").append(queryString);
        }
        if (encode) {
            return encodeUrl(queryStrBuilder.toString());
        }
        return queryStrBuilder.toString();
    }

    public static String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, getOwner().getConfig().getDefaultCharsetEncoding());
        } catch (UnsupportedEncodingException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return url;
    }

    public static String decodeUrl(String url) {
        try {
            return URLDecoder.decode(url, getOwner().getConfig().getDefaultCharsetEncoding());
        } catch (UnsupportedEncodingException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return url;
    }

    /**
     * @param url           URL地址
     * @param needStartWith 是否以'/'开始
     * @param needEndWith   是否以'/'结束
     * @return 返回修正后的URL地址
     */
    public static String fixUrl(String url, boolean needStartWith, boolean needEndWith) {
        url = StringUtils.trimToNull(url);
        if (url != null) {
            if (needStartWith && !StringUtils.startsWith(url, Type.Const.PATH_SEPARATOR)) {
                url = Type.Const.PATH_SEPARATOR + url;
            } else if (!needStartWith && StringUtils.startsWith(url, Type.Const.PATH_SEPARATOR)) {
                url = StringUtils.substringAfter(url, Type.Const.PATH_SEPARATOR);
            }
            if (needEndWith && !StringUtils.endsWith(url, Type.Const.PATH_SEPARATOR)) {
                url = url + Type.Const.PATH_SEPARATOR;
            } else if (!needEndWith && StringUtils.endsWith(url, Type.Const.PATH_SEPARATOR)) {
                url = StringUtils.substringBeforeLast(url, Type.Const.PATH_SEPARATOR);
            }
            return url;
        }
        return StringUtils.EMPTY;
    }

    /**
     * @param request HttpServletRequest对象
     * @return 是否AJAX请求（需要在使用Ajax请求时设置请求头）
     */
    public static boolean isAjax(HttpServletRequest request) {
        // 判断条件: (x-requested-with = XMLHttpRequest)
        String requestHeader = request.getHeader("x-requested-with");
        return StringUtils.isNotBlank(requestHeader) && "XMLHttpRequest".equalsIgnoreCase(requestHeader);
    }

    public static boolean isAjax(HttpServletRequest request, boolean ifJson, boolean ifXml) {
        return isAjax(request, ifJson, ifXml, null);
    }

    public static boolean isAjax(HttpServletRequest request, boolean ifJson, boolean ifXml, String paramFormat) {
        if (isAjax(request)) {
            return true;
        }
        if (ifJson || ifXml) {
            if (ifJson && isJsonAccepted(request, paramFormat)) {
                return true;
            }
            return ifXml && isXmlAccepted(request, paramFormat);
        }
        return false;
    }

    public static boolean isJsonAccepted(HttpServletRequest request) {
        return isJsonAccepted(request, null);
    }

    public static boolean isJsonAccepted(HttpServletRequest request, String paramFormat) {
        return StringUtils.containsIgnoreCase(request.getHeader("Accept"), "application/json") || StringUtils.equalsIgnoreCase(request.getParameter(StringUtils.defaultIfBlank(paramFormat, Type.Const.PARAM_FORMAT)), Type.Const.FORMAT_JSON);
    }

    public static boolean isXmlAccepted(HttpServletRequest request) {
        return isXmlAccepted(request, null);
    }

    public static boolean isXmlAccepted(HttpServletRequest request, String paramFormat) {
        return StringUtils.containsIgnoreCase(request.getHeader("Accept"), "application/xml") || StringUtils.equalsIgnoreCase(request.getParameter(StringUtils.defaultIfBlank(paramFormat, Type.Const.PARAM_FORMAT)), Type.Const.FORMAT_XML);
    }

    /**
     * @param request HttpServletRequest对象
     * @return 判断当前请求是否采用POST方式提交
     */
    public static boolean isPost(HttpServletRequest request) {
        return Type.HttpMethod.POST.name().equalsIgnoreCase(request.getMethod());
    }

    /**
     * @param url 目标URL地址
     * @return 执行JS方式的页面跳转
     */
    public static String redirectJavaScript(String url) {
        return "<script type=\"text/javascript\">window.location.href=\"" + url + "\"</script>";
    }

    /**
     * @param response HttpServletResponse对象
     * @param url      目标URL地址
     * @return 通过设置Header的Location属性执行页面跳转
     */
    public static String redirectHeaderLocation(HttpServletResponse response, String url) {
        response.setHeader(Type.Const.HTTP_HEADER_LOCATION, url);
        return "http:" + HttpServletResponse.SC_MOVED_PERMANENTLY;
    }

    /**
     * @param response    HttpServletResponse对象
     * @param templateUrl JSP等模板文件URL
     * @param time        间隔时间
     * @param url         页面URL地址，空为当前页面
     * @return 通过设置Header的Refresh属性执行页面刷新或跳转，若url参数为空，则仅向Header添加time时间后自动刷新当前页面
     */
    public static String redirectHeaderRefresh(HttpServletResponse response, String templateUrl, int time, String url) {
        if (StringUtils.isBlank(url)) {
            response.setIntHeader(Type.Const.HTTP_HEADER_REFRESH, time);
        } else {
            response.setHeader(Type.Const.HTTP_HEADER_REFRESH, time + ";URL=" + url);
        }
        return templateUrl;
    }

    /**
     * @param request HttpServletRequest对象
     * @return 获取用户IP地址(当存在多个IP地址时仅返回第一个)
     */
    public static String getRemoteAddress(HttpServletRequest request) {
        String[] remoteAddresses = getRemoteAddresses(request);
        if (remoteAddresses != null && remoteAddresses.length > 0) {
            return remoteAddresses[0];
        }
        return null;
    }

    /**
     * @param request HttpServletRequest对象
     * @return 获取用户IP地址(以数组的形式返回所有IP)
     */
    public static String[] getRemoteAddresses(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            if (StringUtils.equals(ip, "127.0.0.1") || StringUtils.equals(ip, "0:0:0:0:0:0:0:1")) {
                ip = StringUtils.join(NetworkUtils.IP.getHostIPAddresses(), ",");
            }
        }
        return StringUtils.split(ip, ',');
    }

    /**
     * @param source 源字符串
     * @param key    键
     * @param value  值
     * @return 占位符替换
     */
    public static String replaceRegText(String source, String key, String value) {
        if (StringUtils.isBlank(source)) {
            throw new NullArgumentException("source");
        }
        if (StringUtils.isBlank(key)) {
            throw new NullArgumentException("key");
        }
        return source.replaceAll("@\\{" + key + "}", value);
    }

    public static String replaceRegClear(String source) {
        return replaceRegText(source, "(.+?)", StringUtils.EMPTY);
    }

    /**
     * @param request         HttpServletRequest对象
     * @param response        HttpServletResponse对象
     * @param jspFile         JSP文件路径
     * @param charsetEncoding 字符编码
     * @return 执行JSP并返回HTML源码
     * @throws ServletException 可能产生的异常
     * @throws IOException      可能产生的异常
     */
    public static String includeJsp(HttpServletRequest request, HttpServletResponse response, String jspFile, String charsetEncoding) throws ServletException, IOException {
        final OutputStream outputStream = new ByteArrayOutputStream();
        includeJsp(request, response, jspFile, charsetEncoding, outputStream);
        return outputStream.toString();
    }

    public static void includeJsp(HttpServletRequest request, HttpServletResponse response, String jspFile, String charsetEncoding, final OutputStream outputStream) throws ServletException, IOException {
        final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, StringUtils.defaultIfBlank(charsetEncoding, response.getCharacterEncoding())));
        final ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                outputStream.write(b, off, len);
            }
        };
        HttpServletResponse responseWrapper = new HttpServletResponseWrapper(response) {
            @Override
            public ServletOutputStream getOutputStream() {
                return servletOutputStream;
            }

            @Override
            public PrintWriter getWriter() {
                return printWriter;
            }
        };
        request.getRequestDispatcher(jspFile).include(request, responseWrapper);
        printWriter.flush();
    }

    /**
     * 内容加密(基于客户端IP和浏览器类型)
     *
     * @param request HttpServletRequest对象
     * @param dataStr 待加密的内容
     * @return 加密后的内容
     * @throws Exception 可能产生的异常
     */
    public static String encryptStr(HttpServletRequest request, String dataStr) throws Exception {
        return Base64.encodeBase64URLSafeString(CodecUtils.DES.encrypt(dataStr.getBytes(), DigestUtils.md5(request.getRemoteAddr() + request.getHeader(Type.Const.HTTP_HEADER_USER_AGENT))));
    }

    public static String encryptStr(HttpServletRequest request, byte[] bytes) throws Exception {
        return Base64.encodeBase64URLSafeString(CodecUtils.DES.encrypt(bytes, DigestUtils.md5(request.getRemoteAddr() + request.getHeader(Type.Const.HTTP_HEADER_USER_AGENT))));
    }

    /**
     * 内容加密
     *
     * @param dataStr 待加密的内容
     * @param key     密钥
     * @return 加密后的内容
     * @throws Exception 可能产生的异常
     */
    public static String encryptStr(String dataStr, String key) throws Exception {
        return Base64.encodeBase64URLSafeString(CodecUtils.DES.encrypt(dataStr.getBytes(), DigestUtils.md5(key)));
    }

    /**
     * 内容解密(基于客户端IP和浏览器类型)
     *
     * @param request HttpServletRequest对象
     * @param dataStr 待解密的内容
     * @return 解密后的内容
     * @throws Exception 可能产生的异常
     */
    public static String decryptStr(HttpServletRequest request, String dataStr) throws Exception {
        return new String(CodecUtils.DES.decrypt(Base64.decodeBase64(dataStr), DigestUtils.md5(request.getRemoteAddr() + request.getHeader(Type.Const.HTTP_HEADER_USER_AGENT))));
    }

    public static byte[] decryptStr(HttpServletRequest request, byte[] bytes) throws Exception {
        return CodecUtils.DES.decrypt(Base64.decodeBase64(bytes), DigestUtils.md5(request.getRemoteAddr() + request.getHeader(Type.Const.HTTP_HEADER_USER_AGENT)));
    }

    /**
     * 内容解密
     *
     * @param dataStr 待解密的内容
     * @param key     密钥
     * @return 解密后的内容
     * @throws Exception 可能产生的异常
     */
    public static String decryptStr(String dataStr, String key) throws Exception {
        return new String(CodecUtils.DES.decrypt(Base64.decodeBase64(dataStr), DigestUtils.md5(key)));
    }

    public static String messageWithTemplate(IApplication owner, String message) {
        return messageWithTemplate(owner, null, message);
    }

    public static String messageWithTemplate(IApplication owner, String name, String message) {
        return messageWithTemplate(owner, null, Collections.singletonList(new ValidateResult(name, message)));
    }

    public static String messageWithTemplate(IApplication owner, Collection<ValidateResult> messages) {
        return messageWithTemplate(owner, null, messages);
    }

    public static String messageWithTemplate(IApplication owner, String title, Collection<ValidateResult> messages) {
        StringBuilder messagesBuilder = new StringBuilder();
        messages.stream().map((validateResult) -> {
            ExpressionUtils item = ExpressionUtils.bind(doGetConfigValue(owner, IWebMvcConfig.PARAMS_VALIDATION_TEMPLATE_ITEM, "${message}<br>"));
            item.set("name", validateResult.getName());
            item.set("message", validateResult.getMsg());
            return item;
        }).forEachOrdered((item) -> {
            messagesBuilder.append(item.clean().getResult());
        });
        ExpressionUtils element = ExpressionUtils.bind(doGetConfigValue(owner, IWebMvcConfig.PARAMS_VALIDATION_TEMPLATE_ELEMENT, "${title}"));
        if (StringUtils.isNotBlank(title)) {
            element.set("title", title);
        }
        return StringUtils.trimToEmpty(element.set("items", messagesBuilder.toString()).clean().getResult());
    }

    public static String buildRedirectUrl(IContext context, HttpServletRequest request, String redirectUrl, boolean needPrefix) {
        String fixedRedirectUrl = StringUtils.trimToNull(redirectUrl);
        if (fixedRedirectUrl == null) {
            fixedRedirectUrl = StringUtils.defaultIfBlank(request.getParameter(Type.Const.REDIRECT_URL), context != null ? context.getContextParams().get(Type.Const.REDIRECT_URL) : StringUtils.EMPTY);
            if (StringUtils.isBlank(fixedRedirectUrl)) {
                if (context != null) {
                    fixedRedirectUrl = doGetConfigValue(context.getOwner(), IWebMvcConfig.PARAMS_REDIRECT_HOME_URL, null);
                }
                if (StringUtils.isBlank(fixedRedirectUrl)) {
                    fixedRedirectUrl = baseUrl(request);
                }
            }
        }
        if (needPrefix && !StringUtils.startsWithIgnoreCase(fixedRedirectUrl, Type.Const.HTTP_PREFIX) && !StringUtils.startsWithIgnoreCase(fixedRedirectUrl, Type.Const.HTTPS_PREFIX)) {
            fixedRedirectUrl = WebUtils.buildUrl(request, fixedRedirectUrl, true);
        }
        return fixedRedirectUrl;
    }

    public static String buildRedirectUrl(IContext context, String defaultValue) {
        String returnValue = null;
        if (context.getContextParams().containsKey(Type.Const.CUSTOM_REDIRECT)) {
            String value = context.getContextParams().get(Type.Const.CUSTOM_REDIRECT);
            if (StringUtils.equalsIgnoreCase(value, Type.Const.CUSTOM_REDIRECT)) {
                value = IWebMvcConfig.PARAMS_REDIRECT_CUSTOM_URL;
            } else if (StringUtils.startsWithIgnoreCase(value, Type.Const.HTTP_PREFIX) || StringUtils.startsWithIgnoreCase(value, Type.Const.HTTPS_PREFIX)) {
                return value;
            }
            if (StringUtils.isNotBlank(value)) {
                returnValue = doGetConfigValue(context.getOwner(), value, null);
            }
        }
        return StringUtils.trimToEmpty(StringUtils.defaultIfBlank(returnValue, defaultValue));
    }

    public static IView buildErrorView(IWebMvc owner, int code, String msg) {
        return buildErrorView(owner, code, msg, null, 0);
    }

    public static IView buildErrorView(IWebMvc owner, ErrorCode errorCode) {
        return buildErrorView(owner, errorCode, null, 0);
    }

    public static IView buildErrorView(IWebMvc owner, String resourceName, ErrorCode errorCode) {
        return buildErrorView(owner, resourceName, errorCode, null, 0);
    }

    public static IView buildErrorView(IWebMvc owner, int code, String msg, String redirectUrl, int timeInterval) {
        return buildErrorView(owner, code, msg, redirectUrl, timeInterval, null);
    }

    public static IView buildErrorView(IWebMvc owner, ErrorCode errorCode, String redirectUrl, int timeInterval) {
        return buildErrorView(owner, null, errorCode, redirectUrl, timeInterval);
    }

    public static IView buildErrorView(IWebMvc owner, String resourceName, ErrorCode errorCode, String redirectUrl, int timeInterval) {
        Map<String, Object> data = errorCode.getAttribute(Type.Const.PARAM_DATA);
        return buildErrorView(owner, resourceName, errorCode.getCode(), errorCode.getMessage(), redirectUrl, timeInterval, data);
    }

    public static IView buildErrorView(IWebMvc owner, int code, String msg, String redirectUrl, int timeInterval, Map<String, Object> data) {
        return buildErrorView(owner, null, code, msg, redirectUrl, timeInterval, data);
    }

    public static IView buildErrorView(IWebMvc owner, String resourceName, int code, String msg, String redirectUrl, int timeInterval, Map<String, Object> data) {
        IView returnView;
        String errorViewPath = doGetConfigValue(owner.getOwner(), IWebMvcConfig.PARAMS_ERROR_VIEW, "error.jsp");
        if (StringUtils.endsWithIgnoreCase(errorViewPath, FreemarkerView.FILE_SUFFIX)) {
            returnView = View.freemarkerView(owner, errorViewPath);
        } else if (StringUtils.endsWithIgnoreCase(errorViewPath, VelocityView.FILE_SUFFIX)) {
            returnView = View.velocityView(owner, errorViewPath);
        } else {
            returnView = View.jspView(owner, errorViewPath);
        }
        returnView.addAttribute(Type.Const.PARAM_RET, code);
        returnView.addAttribute(Type.Const.PARAM_MSG, errorCodeI18n(owner, resourceName, code, msg));
        if (data != null && !data.isEmpty()) {
            returnView.addAttribute(Type.Const.PARAM_DATA, data);
        }
        //
        if (StringUtils.isNotBlank(redirectUrl) && timeInterval > 0) {
            returnView.addHeader(Type.Const.HTTP_HEADER_REFRESH, timeInterval + ";URL=" + redirectUrl);
        }
        //
        return returnView;
    }

    /**
     * 加载i18n资源键值
     *
     * @param owner       所属容器
     * @param resourceKey 键
     * @return 返回resourceKey指定的键值
     */
    public static String i18nStr(IWebMvc owner, String resourceKey) {
        return owner.getOwner().getI18n().load(owner.getConfig().getResourceName(), resourceKey);
    }

    /**
     * 加载i18n资源键值
     *
     * @param owner        所属容器
     * @param resourceKey  键
     * @param defaultValue 默认值
     * @return 返回resourceKey指定的键值
     */
    public static String i18nStr(IWebMvc owner, String resourceKey, String defaultValue) {
        return i18nStr(owner, null, resourceKey, defaultValue);
    }

    public static String i18nStr(IWebMvc owner, String resourceName, String resourceKey, String defaultValue) {
        return owner.getOwner().getI18n().load(StringUtils.defaultIfBlank(resourceName, owner.getConfig().getResourceName()), resourceKey, defaultValue);
    }

    public static String httpStatusI18n(IWebMvc owner, int code) {
        return httpStatusI18n(owner, null, code);
    }

    public static String httpStatusI18n(IWebMvc owner, String resourceName, int code) {
        String statusText = Type.HTTP_STATUS.get(code);
        if (StringUtils.isBlank(statusText)) {
            code = 400;
            statusText = Type.HTTP_STATUS.get(code);
        }
        return i18nStr(owner, resourceName, "webmvc.http_status_" + code, statusText);
    }

    public static String errorCodeI18n(IWebMvc owner, int code, String defaultValue) {
        return errorCodeI18n(owner, null, code, defaultValue);
    }

    public static String errorCodeI18n(IWebMvc owner, String resourceName, int code, String defaultValue) {
        if (code == 0) {
            return defaultValue;
        }
        return i18nStr(owner, resourceName, "webmvc.error_code_" + code, defaultValue);
    }

    public static String errorCodeI18n(IWebMvc owner, IExceptionProcessor.Result result) {
        return errorCodeI18n(owner, null, result);
    }

    public static String errorCodeI18n(IWebMvc owner, String resourceName, IExceptionProcessor.Result result) {
        String msg = WebUtils.errorCodeI18n(owner, resourceName, result.getCode(), result.getMessage());
        return StringUtils.defaultIfBlank(msg, result.getMessage());
    }
}
