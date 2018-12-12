/*
 * Copyright 2007-2016 the original author or authors.
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

import net.ymate.platform.core.IConfig;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.i18n.I18N;
import net.ymate.platform.core.support.IContext;
import net.ymate.platform.core.util.CodecUtils;
import net.ymate.platform.core.util.ExpressionUtils;
import net.ymate.platform.core.util.NetworkUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.WebMVC;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
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
 * @version 1.0
 * @since 2.0.6
 */
public class WebUtils {

    private static final Log _LOG = LogFactory.getLog(WebUtils.class);

    private static String __doGetConfigValue(String confName, String defaultName) {
        return getOwner().getOwner().getConfig().getParam(confName, defaultName);
    }

    private static String __doGetConfigValue(YMP owner, String confName, String defaultName) {
        return owner.getConfig().getParam(confName, defaultName);
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
    public static String buildURL(HttpServletRequest request, String requestPath, boolean withBasePath) {
        requestPath = StringUtils.trimToEmpty(requestPath);
        if (withBasePath && !"".equals(requestPath) && requestPath.charAt(0) == '/') {
            requestPath = StringUtils.substringAfter(requestPath, "/");
        }
        return (withBasePath ? baseURL(request) + requestPath : requestPath) + __doGetConfigValue(Type.Const.REQUEST_SUFFIX, StringUtils.EMPTY);
    }

    private static String __doGetSafeServerName(HttpServletRequest request) {
        return __doGetConfigValue(Type.Const.SERVER_NAME, request.getServerName());
    }

    /**
     * @param request HttpServletRequest对象
     * @return 获取当前站点基准URL
     */
    public static String baseURL(HttpServletRequest request) {
        StringBuilder basePath = new StringBuilder();
        String _serverName = __doGetSafeServerName(request);
        if (!StringUtils.startsWithAny(StringUtils.lowerCase(_serverName), new String[]{"http://", "https://"})) {
            basePath.append(request.getScheme()).append("://").append(_serverName);
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                basePath.append(":").append(request.getServerPort());
            }
            if (StringUtils.isNotBlank(request.getContextPath())) {
                basePath.append(request.getContextPath());
            }
        } else {
            basePath.append(_serverName);
        }
        if (basePath.charAt(basePath.length() - 1) != '/') {
            basePath.append("/");
        }
        return basePath.toString();
    }

    /**
     * @param request HttpServletRequest对象
     * @param encode  是否编码
     * @return 拼装当前URL请求地址(含QueryString串)
     */
    public static String appendQueryStr(HttpServletRequest request, boolean encode) {
        StringBuffer _returnUrlBuffer = request.getRequestURL();
        String _queryStr = request.getQueryString();
        if (StringUtils.isNotBlank(_queryStr)) {
            _returnUrlBuffer.append("?").append(_queryStr);
        }
        if (encode) {
            return encodeURL(_returnUrlBuffer.toString());
        }
        return _returnUrlBuffer.toString();
    }

    public static String encodeURL(String url) {
        try {
            return URLEncoder.encode(url, IConfig.DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            _LOG.warn("", RuntimeUtils.unwrapThrow(e));
        }
        return url;
    }

    public static String decodeURL(String url) {
        try {
            return URLDecoder.decode(url, IConfig.DEFAULT_CHARSET);
        } catch (UnsupportedEncodingException e) {
            _LOG.warn("", RuntimeUtils.unwrapThrow(e));
        }
        return url;
    }

    /**
     * @param url           URL地址
     * @param needStartWith 是否以'/'开始
     * @param needEndWith   是否以'/'结束
     * @return 返回修正后的URL地址
     */
    public static String fixURL(String url, boolean needStartWith, boolean needEndWith) {
        url = StringUtils.trimToNull(url);
        if (url != null) {
            if (needStartWith && !StringUtils.startsWith(url, "/")) {
                url = '/' + url;
            } else if (!needStartWith && StringUtils.startsWith(url, "/")) {
                url = StringUtils.substringAfter(url, "/");
            }
            if (needEndWith && !StringUtils.endsWith(url, "/")) {
                url = url + '/';
            } else if (!needEndWith && StringUtils.endsWith(url, "/")) {
                url = StringUtils.substringBeforeLast(url, "/");
            }
            return url;
        }
        return "";
    }

    /**
     * @param request HttpServletRequest对象
     * @return 是否AJAX请求（需要在使用Ajax请求时设置请求头）
     */
    public static boolean isAjax(HttpServletRequest request) {
        // 判断条件: (x-requested-with = XMLHttpRequest)
        String _httpx = request.getHeader("x-requested-with");
        return StringUtils.isNotBlank(_httpx) && "XMLHttpRequest".equalsIgnoreCase(_httpx);
    }

    public static boolean isAjax(HttpServletRequest request, boolean ifJson, boolean ifXml) {
        if (isAjax(request)) {
            return true;
        }
        if (ifJson || ifXml) {
            String _format = StringUtils.trimToNull(request.getParameter(Type.Const.PARAM_FORMAT));
            if (ifJson && StringUtils.equalsIgnoreCase(_format, Type.Const.FORMAT_JSON)) {
                return true;
            }
            return ifXml && StringUtils.equalsIgnoreCase(_format, Type.Const.FORMAT_XML);
        }
        return false;
    }

    /**
     * @param request HttpServletRequest对象
     * @return 判断当前请求是否采用POST方式提交
     */
    public static boolean isPost(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod());
    }

    /**
     * @param url 目标URL地址
     * @return 执行JS方式的页面跳转
     */
    public static String doRedirectJavaScript(String url) {
        return "<script type=\"text/javascript\">window.location.href=\"" + url + "\"</script>";
    }

    /**
     * @param response HttpServletResponse对象
     * @param url      目标URL地址
     * @return 通过设置Header的Location属性执行页面跳转
     */
    public static String doRedirectHeaderLocation(HttpServletResponse response, String url) {
        response.setHeader("Location", url);
        return "http:" + HttpServletResponse.SC_MOVED_PERMANENTLY;
    }

    /**
     * @param response    HttpServletResponse对象
     * @param templateUrl JSP等模板文件URL
     * @param time        间隔时间
     * @param url         页面URL地址，空为当前页面
     * @return 通过设置Header的Refresh属性执行页面刷新或跳转，若url参数为空，则仅向Header添加time时间后自动刷新当前页面
     */
    public static String doRedirectHeaderRefresh(HttpServletResponse response, String templateUrl, int time, String url) {
        if (StringUtils.isBlank(url)) {
            response.setIntHeader("REFRESH", time);
        } else {
            String _content = time + ";URL=" + url;
            response.setHeader("REFRESH", _content);
        }
        return templateUrl;
    }

    /**
     * @param request HttpServletRequest对象
     * @return 获取用户IP地址(当存在多个IP地址时仅返回第一个)
     */
    public static String getRemoteAddr(HttpServletRequest request) {
        String[] _ips = getRemoteAddrs(request);
        if (_ips != null && _ips.length > 0) {
            return _ips[0];
        }
        return null;
    }

    /**
     * @param request HttpServletRequest对象
     * @return 获取用户IP地址(以数组的形式返回所有IP)
     */
    public static String[] getRemoteAddrs(HttpServletRequest request) {
        String _ip = request.getHeader("x-forwarded-for");
        if (StringUtils.isBlank(_ip) || "unknown".equalsIgnoreCase(_ip)) {
            _ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(_ip) || "unknown".equalsIgnoreCase(_ip)) {
            _ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(_ip) || "unknown".equalsIgnoreCase(_ip)) {
            _ip = request.getRemoteAddr();
            if (StringUtils.equals(_ip, "127.0.0.1") || StringUtils.equals(_ip, "0:0:0:0:0:0:0:1")) {
                _ip = StringUtils.join(NetworkUtils.IP.getHostIPAddrs(), ",");
            }
        }
        return StringUtils.split(_ip, ',');
    }

    /**
     * @param source 源字符串
     * @param key    键
     * @param value  值
     * @return 占位符替换
     */
    public static String replaceRegText(String source, String key, String value) {
        String _regex = "@\\{" + key + "}";
        return source.replaceAll(_regex, value);
    }

    public static String replaceRegClear(String source) {
        return replaceRegText(source, "(.+?)", "");
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
    public static String includeJSP(HttpServletRequest request, HttpServletResponse response, String jspFile, String charsetEncoding) throws ServletException, IOException {
        final OutputStream _output = new ByteArrayOutputStream();
        includeJSP(request, response, jspFile, charsetEncoding, _output);
        return _output.toString();
    }

    public static void includeJSP(HttpServletRequest request, HttpServletResponse response, String jspFile, String charsetEncoding, final OutputStream outputStream) throws ServletException, IOException {
        final PrintWriter _writer = new PrintWriter(new OutputStreamWriter(outputStream, StringUtils.defaultIfBlank(charsetEncoding, response.getCharacterEncoding())));
        final ServletOutputStream _servletOutput = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                outputStream.write(b, off, len);
            }
        };
        HttpServletResponse _response = new HttpServletResponseWrapper(response) {
            @Override
            public ServletOutputStream getOutputStream() {
                return _servletOutput;
            }

            @Override
            public PrintWriter getWriter() {
                return _writer;
            }
        };
        request.getRequestDispatcher(jspFile).include(request, _response);
        _writer.flush();
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
        return Base64.encodeBase64URLSafeString(CodecUtils.DES.encrypt(dataStr.getBytes(), DigestUtils.md5(request.getRemoteAddr() + request.getHeader("User-Agent"))));
    }

    public static String encryptStr(HttpServletRequest request, byte[] bytes) throws Exception {
        return Base64.encodeBase64URLSafeString(CodecUtils.DES.encrypt(bytes, DigestUtils.md5(request.getRemoteAddr() + request.getHeader("User-Agent"))));
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
        return new String(CodecUtils.DES.decrypt(Base64.decodeBase64(dataStr), DigestUtils.md5(request.getRemoteAddr() + request.getHeader("User-Agent"))));
    }

    public static byte[] decryptStr(HttpServletRequest request, byte[] bytes) throws Exception {
        return CodecUtils.DES.decrypt(Base64.decodeBase64(bytes), DigestUtils.md5(request.getRemoteAddr() + request.getHeader("User-Agent")));
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

    public static String messageWithTemplate(YMP owner, String message) {
        return messageWithTemplate(owner, null, message);
    }

    public static String messageWithTemplate(YMP owner, String name, String message) {
        return messageWithTemplate(owner, null, Collections.singletonList(new ValidateResult(name, message)));
    }

    public static String messageWithTemplate(YMP owner, Collection<ValidateResult> messages) {
        return messageWithTemplate(owner, null, messages);
    }

    public static String messageWithTemplate(YMP owner, String title, Collection<ValidateResult> messages) {
        StringBuilder _messages = new StringBuilder();
        for (ValidateResult _vResult : messages) {
            ExpressionUtils _item = ExpressionUtils.bind(__doGetConfigValue(owner, Type.Const.VALIDATION_TEMPLATE_ITEM, "${message}<br>"));
            _item.set("name", _vResult.getName());
            _item.set("message", _vResult.getMsg());
            //
            _messages.append(_item.clean().getResult());
        }
        ExpressionUtils _element = ExpressionUtils.bind(__doGetConfigValue(owner, Type.Const.VALIDATION_TEMPLATE_ELEMENT, "${title}"));
        if (StringUtils.isNotBlank(title)) {
            _element.set("title", title);
        }
        return StringUtils.trimToEmpty(_element.set("items", _messages.toString()).clean().getResult());
    }

    public static String buildRedirectURL(IContext context, HttpServletRequest request, String redirectUrl, boolean needPrefix) {
        String _redirectUrl = StringUtils.trimToNull(redirectUrl);
        if (_redirectUrl == null) {
            _redirectUrl = StringUtils.defaultIfBlank(request.getParameter(Type.Const.REDIRECT_URL), context != null ? context.getContextParams().get(Type.Const.REDIRECT_URL) : "");
            if (StringUtils.isBlank(_redirectUrl)) {
                if (context != null) {
                    _redirectUrl = __doGetConfigValue(context.getOwner(), Type.Const.REDIRECT_HOME_URL, null);
                }
                if (StringUtils.isBlank(_redirectUrl)) {
                    _redirectUrl = baseURL(WebContext.getRequest());
                }
            }
        }
        if (needPrefix && !StringUtils.startsWithIgnoreCase(_redirectUrl, "http://") && !StringUtils.startsWithIgnoreCase(_redirectUrl, "https://")) {
            _redirectUrl = WebUtils.buildURL(request, _redirectUrl, true);
        }
        return _redirectUrl;
    }

    public static String buildRedirectURL(IContext context, String defaultValue) {
        String _returnValue = null;
        if (context.getContextParams().containsKey(Type.Const.CUSTOM_REDIRECT)) {
            String _value = context.getContextParams().get(Type.Const.CUSTOM_REDIRECT);
            if (StringUtils.equalsIgnoreCase(_value, Type.Const.CUSTOM_REDIRECT)) {
                _value = Type.Const.REDIRECT_CUSTOM_URL;
            } else if (StringUtils.startsWithIgnoreCase(_value, "http://") || StringUtils.startsWithIgnoreCase(_value, "https://")) {
                return _value;
            }
            if (StringUtils.isNotBlank(_value)) {
                _returnValue = __doGetConfigValue(context.getOwner(), _value, null);
            }
        }
        return StringUtils.trimToEmpty(StringUtils.defaultIfBlank(_returnValue, defaultValue));
    }

    public static IView buildErrorView(IWebMvc owner, int code, String msg) {
        return buildErrorView(owner, code, msg, null, 0);
    }

    public static IView buildErrorView(IWebMvc owner, int code, String msg, String redirectUrl, int timeInterval) {
        return buildErrorView(owner, code, msg, redirectUrl, timeInterval, null);
    }

    public static IView buildErrorView(IWebMvc owner, int code, String msg, String redirectUrl, int timeInterval, Map<String, Object> data) {
        IView _view;
        String _errorViewPath = __doGetConfigValue(owner.getOwner(), Type.Const.ERROR_VIEW, "error.jsp");
        if (StringUtils.endsWithIgnoreCase(_errorViewPath, ".ftl")) {
            _view = View.freemarkerView(owner, _errorViewPath);
        } else if (StringUtils.endsWithIgnoreCase(_errorViewPath, ".vm")) {
            _view = View.velocityView(owner, _errorViewPath);
        } else {
            _view = View.jspView(owner, _errorViewPath);
        }
        _view.addAttribute(Type.Const.PARAM_RET, code);
        _view.addAttribute(Type.Const.PARAM_MSG, msg);
        if (data != null && !data.isEmpty()) {
            _view.addAttribute(Type.Const.PARAM_DATA, data);
        }
        //
        if (StringUtils.isNotBlank(redirectUrl) && timeInterval > 0) {
            _view.addHeader("REFRESH", timeInterval + ";URL=" + redirectUrl);
        }
        //
        return _view;
    }

    /**
     * 加载i18n资源键值
     *
     * @param owner       所属容器
     * @param resourceKey 键
     * @return 返回resourceKey指定的键值
     */
    public static String i18nStr(IWebMvc owner, String resourceKey) {
        return I18N.load(owner.getModuleCfg().getI18nResourceName(), resourceKey);
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
        return I18N.load(owner.getModuleCfg().getI18nResourceName(), resourceKey, defaultValue);
    }

    public static String httpStatusI18n(IWebMvc owner, int code) {
        String _statusText = Type.HTTP_STATUS.get(code);
        if (StringUtils.isBlank(_statusText)) {
            code = 400;
            _statusText = Type.HTTP_STATUS.get(code);
        }
        return i18nStr(owner, "webmvc.http_status_" + code, _statusText);
    }

    public static String errorCodeI18n(IWebMvc owner, int code, String defaultValue) {
        return i18nStr(owner, "webmvc.error_code_" + code, defaultValue);
    }

    public static String errorCodeI18n(IWebMvc owner, IExceptionProcessor.Result result) {
        String _msg = WebUtils.errorCodeI18n(owner, result.getCode(), result.getMessage());
        return StringUtils.defaultIfBlank(_msg, result.getMessage());
    }
}
