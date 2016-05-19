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

import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.CodecUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.context.WebContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.Cookie;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Cookies操作助手，可以有效避免Cookie取值问题，同时支持Cookie加密
 *
 * @author 刘镇 (suninformation@163.com) on 2011-6-10 下午03:50:53
 * @version 1.0
 */
public final class CookieHelper {

    private static final Log __LOG = LogFactory.getLog(CookieHelper.class);

    private IWebMvc __owner;

    /**
     * 是否使用密钥加密
     */
    private boolean __useAuthKey;

    /**
     * 是否使用Base64编码
     */
    private boolean __useBase64;

    /**
     * 加密密钥
     */
    private String __cookieKey = null;

    private String __charsetEncoding;

    private CookieHelper(IWebMvc owner) {
        __owner = owner;
        __useAuthKey = owner.getModuleCfg().isDefaultEnabledCookieAuth();
        __charsetEncoding = __owner.getModuleCfg().getDefaultCharsetEncoding();
        if (StringUtils.isBlank(__charsetEncoding)) {
            __charsetEncoding = WebContext.getRequest().getCharacterEncoding();
        }
    }

    /**
     * @param owner 所属WebMVC对象
     * @return 构建Cookies操作助手类实例
     */
    public static CookieHelper bind(IWebMvc owner) {
        return new CookieHelper(owner);
    }

    private Cookie __doGetCookie(String cookieName) {
        Cookie _cookies[] = WebContext.getRequest().getCookies();
        if (_cookies == null) {
            return null;
        } else {
            for (Cookie _cookie : _cookies) {
                String name = _cookie.getName();
                if (name.equals(cookieName)) {
                    return _cookie;
                }
            }
            return null;
        }
    }

    /**
     * @param key 键
     * @return 获取Cookie
     */
    public BlurObject getCookie(String key) {
        Cookie _c = __doGetCookie(__owner.getModuleCfg().getCookiePrefix() + key);
        if (_c != null) {
            String _v = decodeValue(_c.getValue());
            return new BlurObject(_v);
        }
        return new BlurObject("");
    }

    /**
     * @return 获取全部Cookie
     */
    public Map<String, BlurObject> getCookies() {
        Map<String, BlurObject> _returnValue = new HashMap<String, BlurObject>();
        Cookie[] _cookies = WebContext.getRequest().getCookies();
        if (_cookies != null) {
            String _cookiePre = __owner.getModuleCfg().getCookiePrefix();
            int _preLength = StringUtils.length(_cookiePre);
            for (Cookie _cookie : _cookies) {
                String _name = _cookie.getName();
                if (_name.startsWith(_cookiePre)) {
                    String _v = decodeValue(_cookie.getValue());
                    _returnValue.put(_name.substring(_preLength), new BlurObject(_v));
                }
            }
        }
        return _returnValue;
    }

    /**
     * @param key 键
     * @return 移除Cookie
     */
    public CookieHelper removeCookie(String key) {
        return this.setCookie(key, "", 0);
    }

    /**
     * @param key   键
     * @param value 值
     * @return 添加或重设Cookie，过期时间基于Session时效
     */
    public CookieHelper setCookie(String key, String value) {
        return this.setCookie(key, value, -1);
    }

    /**
     * @param key    键
     * @param value  值
     * @param maxAge 过期时间
     * @return 添加或重设Cookie
     */
    public CookieHelper setCookie(String key, String value, int maxAge) {
        Cookie _cookie = new Cookie(__owner.getModuleCfg().getCookiePrefix() + key, StringUtils.isBlank(value) ? "" : encodeValue(value));
        _cookie.setMaxAge(maxAge);
        _cookie.setPath(__owner.getModuleCfg().getCookiePath());
        if (StringUtils.isNotBlank(__owner.getModuleCfg().getCookieDomain())) {
            _cookie.setDomain(__owner.getModuleCfg().getCookieDomain());
        }
        _cookie.setSecure(WebContext.getRequest().isSecure());
        WebContext.getResponse().addCookie(_cookie);
        return this;
    }

    /**
     * @return 设置开启采用密钥加密(将默认开启Base64编码)
     */
    public CookieHelper allowUseAuthKey() {
        this.__useAuthKey = true;
        return this;
    }

    public CookieHelper disabledUseAuthKey() {
        this.__useAuthKey = false;
        return this;
    }

    /**
     * @return 设置开启采用Base64编码(默认支持UrlEncode编码)
     */
    public CookieHelper allowUseBase64() {
        this.__useBase64 = true;
        return this;
    }

    /**
     * @return 清理所有的Cookie
     */
    public CookieHelper clearCookies() {
        Map<String, BlurObject> _cookies = this.getCookies();
        for (String _name : _cookies.keySet()) {
            this.removeCookie(_name);
        }
        return this;
    }

    /**
     * @return 获取经过MD5加密的Cookie密钥（注：需先开启采用密钥加密，否则返回“”）
     */
    private String __getEncodedAuthKeyStr() {
        if (this.__useAuthKey) {
            String _key = __owner.getModuleCfg().getCookieAuthKey();
            if (StringUtils.isNotBlank(_key)) {
                return DigestUtils.md5Hex(_key + WebContext.getRequest().getHeader("User-Agent"));
            }
        }
        return "";
    }

    public String encodeValue(String value) {
        String _value = value;
        if (StringUtils.isNotBlank(value)) {
            if (this.__useAuthKey) {
                if (__cookieKey == null) {
                    __cookieKey = __getEncodedAuthKeyStr();
                }
                if (StringUtils.isNotBlank(__cookieKey)) {
                    try {
                        _value = new String(Base64.encodeBase64URLSafe(CodecUtils.DES.encrypt(value.getBytes(__charsetEncoding), __cookieKey.getBytes())), __charsetEncoding);
                    } catch (Exception e) {
                        __LOG.warn("", RuntimeUtils.unwrapThrow(e));
                    }
                }
            } else if (this.__useBase64) {
                try {
                    _value = new String(Base64.encodeBase64URLSafe(_value.getBytes(__charsetEncoding)), __charsetEncoding);
                } catch (UnsupportedEncodingException e) {
                    __LOG.warn("", RuntimeUtils.unwrapThrow(e));
                }
            }
        } else {
            _value = "";
        }
        return _value;
    }

    public String decodeValue(String value) {
        String _value = null;
        if (this.__useAuthKey) {
            if (__cookieKey == null) {
                __cookieKey = __getEncodedAuthKeyStr();
            }
            if (StringUtils.isNotBlank(__cookieKey)) {
                try {
                    _value = new String(CodecUtils.DES.decrypt(Base64.decodeBase64(value.getBytes(__charsetEncoding)), __cookieKey.getBytes()));
                } catch (Exception e) {
                    __LOG.warn("", RuntimeUtils.unwrapThrow(e));
                }
            } else {
                _value = value;
            }
        } else if (this.__useBase64) {
            try {
                _value = new String(Base64.decodeBase64(value.getBytes(__charsetEncoding)));
            } catch (UnsupportedEncodingException e) {
                __LOG.warn("", RuntimeUtils.unwrapThrow(e));
            }
        } else {
            _value = value;
        }
        return _value;
    }
}
