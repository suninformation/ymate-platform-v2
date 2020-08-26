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

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.CodecUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.context.WebContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Cookies操作助手，可以有效避免Cookie取值问题，同时支持Cookie加密
 *
 * @author 刘镇 (suninformation@163.com) on 2011-6-10 下午03:50:53
 */
public final class CookieHelper {

    private static final Log LOG = LogFactory.getLog(CookieHelper.class);

    private final IWebMvc owner;

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    /**
     * 是否使用密钥加密
     */
    private boolean useAuthKey;

    /**
     * 是否使用Base64编码
     */
    private boolean useBase64;

    /**
     * 是否使用URLEncoder/URLDecoder编码
     */
    private boolean useURLCoder;

    private boolean useHttpOnly;

    /**
     * 加密密钥
     */
    private String cookieKey;

    private String charsetEncoding;

    private CookieHelper(IWebMvc owner, HttpServletRequest request, HttpServletResponse response) {
        this.owner = owner;
        this.request = request;
        this.response = response;
        //
        useAuthKey = owner.getConfig().isCookieAuthEnabled();
        useHttpOnly = owner.getConfig().isCookieUseHttpOnly();
        charsetEncoding = owner.getConfig().getDefaultCharsetEncoding();
        //
        if (StringUtils.isBlank(charsetEncoding)) {
            charsetEncoding = this.request.getCharacterEncoding();
        }
    }

    /**
     * @param owner 所属WebMVC对象
     * @return 构建Cookies操作助手类实例
     */
    public static CookieHelper bind(IWebMvc owner) {
        return new CookieHelper(owner, WebContext.getRequest(), WebContext.getResponse());
    }

    public static CookieHelper bind() {
        return new CookieHelper(WebContext.getContext().getOwner(), WebContext.getRequest(), WebContext.getResponse());
    }

    public static CookieHelper bind(IWebMvc owner, HttpServletRequest request, HttpServletResponse response) {
        return new CookieHelper(owner, request, response);
    }

    public static CookieHelper bind(HttpServletRequest request, HttpServletResponse response) {
        return new CookieHelper(WebContext.getContext().getOwner(), request, response);
    }

    private Cookie doGetCookie(String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equals(cookieName)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    /**
     * @param key 键
     * @return 获取Cookie
     */
    public BlurObject getCookie(String key) {
        Cookie cookie = doGetCookie(owner.getConfig().getCookiePrefix() + key);
        if (cookie != null) {
            return new BlurObject(decodeValue(cookie.getValue()));
        }
        return new BlurObject(StringUtils.EMPTY);
    }

    /**
     * @return 获取全部Cookie
     */
    public Map<String, BlurObject> getCookies() {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Map<String, BlurObject> returnValue = new HashMap<>(cookies.length);
            String cookiePrefix = owner.getConfig().getCookiePrefix();
            int prefixLength = StringUtils.length(cookiePrefix);
            for (Cookie cookie : cookies) {
                String cookieName = cookie.getName();
                if (cookieName.startsWith(cookiePrefix)) {
                    returnValue.put(cookieName.substring(prefixLength), new BlurObject(decodeValue(cookie.getValue())));
                }
            }
            return returnValue;
        }
        return Collections.emptyMap();
    }

    /**
     * @param key 键
     * @return 移除Cookie
     */
    public CookieHelper removeCookie(String key) {
        return setCookie(key, StringUtils.EMPTY, 0);
    }

    /**
     * @param key   键
     * @param value 值
     * @return 添加或重设Cookie，过期时间基于Session时效
     */
    public CookieHelper setCookie(String key, String value) {
        return setCookie(key, value, -1);
    }

    /**
     * @param key    键
     * @param value  值
     * @param maxAge 过期时间
     * @return 添加或重设Cookie
     */
    public CookieHelper setCookie(String key, String value, int maxAge) {
        Cookie cookie = new Cookie(owner.getConfig().getCookiePrefix() + key, StringUtils.isBlank(value) ? StringUtils.EMPTY : encodeValue(value));
        cookie.setMaxAge(maxAge);
        cookie.setPath(owner.getConfig().getCookiePath());
        cookie.setSecure(request.isSecure());
        cookie.setHttpOnly(useHttpOnly);
        if (StringUtils.isNotBlank(owner.getConfig().getCookieDomain())) {
            cookie.setDomain(owner.getConfig().getCookieDomain());
        }
        response.addCookie(cookie);
        return this;
    }

    /**
     * @return 设置开启采用密钥加密
     */
    public CookieHelper allowUseAuthKey() {
        this.useAuthKey = true;
        return this;
    }

    public CookieHelper disabledUseAuthKey() {
        this.useAuthKey = false;
        return this;
    }

    /**
     * @return 设置开启采用Base64编码
     */
    public CookieHelper allowUseBase64() {
        this.useBase64 = true;
        return this;
    }

    /**
     * @return 设置开启URLEncoder/URLDecoder编码
     */
    public CookieHelper allowUseURLCoder() {
        this.useURLCoder = true;
        return this;
    }

    public CookieHelper allowUseHttpOnly() {
        this.useHttpOnly = true;
        return this;
    }

    public CookieHelper disabledUseHttpOnly() {
        this.useHttpOnly = false;
        return this;
    }

    /**
     * @return 清理所有的Cookie
     */
    public CookieHelper clearCookies() {
        Map<String, BlurObject> cookies = getCookies();
        cookies.keySet().forEach(this::removeCookie);
        return this;
    }

    /**
     * @return 获取经过MD5加密的Cookie密钥（注：需先开启采用密钥加密，否则返回“”）
     */
    private String doGetEncodedAuthKeyStr() {
        if (useAuthKey) {
            String cookieAuthKey = owner.getConfig().getCookieAuthKey();
            if (StringUtils.isNotBlank(cookieAuthKey)) {
                return DigestUtils.md5Hex(cookieAuthKey + request.getHeader("User-Agent"));
            }
        }
        return StringUtils.EMPTY;
    }

    public String encodeValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            try {
                if (useAuthKey) {
                    if (cookieKey == null) {
                        cookieKey = doGetEncodedAuthKeyStr();
                    }
                    if (StringUtils.isNotBlank(cookieKey)) {
                        value = new String(Base64.encodeBase64(CodecUtils.DES.encrypt(value.getBytes(charsetEncoding), cookieKey.getBytes())), charsetEncoding);
                    }
                }
                if (useBase64) {
                    value = new String(Base64.encodeBase64(value.getBytes(charsetEncoding)), charsetEncoding);
                }
                if (useURLCoder) {
                    value = URLEncoder.encode(value, charsetEncoding);
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return StringUtils.trimToEmpty(value);
    }

    public String decodeValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            try {
                if (useURLCoder) {
                    value = URLDecoder.decode(value, charsetEncoding);
                }
                if (useBase64) {
                    value = new String(Base64.decodeBase64(value.getBytes(charsetEncoding)), charsetEncoding);
                }
                if (useAuthKey) {
                    if (cookieKey == null) {
                        cookieKey = doGetEncodedAuthKeyStr();
                    }
                    if (StringUtils.isNotBlank(cookieKey)) {
                        value = new String(CodecUtils.DES.decrypt(Base64.decodeBase64(value.getBytes(charsetEncoding)), cookieKey.getBytes()));
                    }
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return StringUtils.trimToEmpty(value);
    }
}
