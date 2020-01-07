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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.MessageDigest;

/**
 * <p>
 * TokenProcessHelper is responsible for handling all token related functionality.
 * The methods in this class are synchronized to protect token processing from
 * multiple threads.  Servlet containers are allowed to return a different
 * HttpSession object for two threads accessing the same session so it is not
 * possible to synchronize on the session.
 * </p>
 * <p>Copy TokenProcessor.java from Struts 1.1</p>
 *
 * @author 刘镇 (suninformation@163.com) on 14-7-6
 */
public class TokenProcessHelper {

    private static final String TRANSACTION_TOKEN_KEY = "net.ymate.platform.webmvc.TRANSACTION_TOKEN";

    public static final String TOKEN_KEY = "net.ymate.platform.webmvc.TOKEN";

    private static final TokenProcessHelper INSTANCE = new TokenProcessHelper();

    private long previous;

    private TokenProcessHelper() {
        super();
    }

    public static TokenProcessHelper getInstance() {
        return INSTANCE;
    }

    public synchronized boolean isTokenValid(HttpServletRequest request) {
        return this.isTokenValid(request, false);
    }

    public synchronized boolean isTokenValid(HttpServletRequest request, boolean reset) {
        return this.isTokenValid(request, null, reset);
    }

    public synchronized boolean isTokenValid(HttpServletRequest request, String name, String token, boolean reset) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String tokenKey = TokenProcessHelper.TRANSACTION_TOKEN_KEY;
        if (StringUtils.isNotBlank(name)) {
            tokenKey += "|" + name;
        }
        String saved = (String) session.getAttribute(tokenKey);
        if (saved == null) {
            return false;
        }
        if (reset) {
            resetToken(request);
        }
        return saved.equals(token);
    }

    public synchronized boolean isTokenValid(HttpServletRequest request, String name, boolean reset) {
        return isTokenValid(request, name, StringUtils.trimToNull(request.getParameter(TOKEN_KEY)), reset);
    }

    public String getToken(HttpServletRequest request) {
        return getToken(request, null);
    }

    public String getToken(HttpServletRequest request, String name) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            if (StringUtils.isNotBlank(name)) {
                return (String) session.getAttribute(TRANSACTION_TOKEN_KEY + "|" + name);
            }
            return (String) session.getAttribute(TRANSACTION_TOKEN_KEY);
        }
        return null;
    }

    public synchronized void resetToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(TRANSACTION_TOKEN_KEY);
        }
    }

    public synchronized void resetToken(HttpServletRequest request, String name) {
        if (StringUtils.isBlank(name)) {
            throw new NullArgumentException(name);
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(TRANSACTION_TOKEN_KEY + "|" + name);
        }
    }

    public synchronized String saveToken(HttpServletRequest request) {
        String token = generateToken(request);
        if (token != null) {
            HttpSession session = request.getSession();
            session.setAttribute(TRANSACTION_TOKEN_KEY, token);
        }
        return token;
    }

    public synchronized String saveToken(HttpServletRequest request, String name) {
        if (StringUtils.trimToNull(name) == null) {
            throw new NullArgumentException(name);
        }
        String token = generateToken(request);
        if (token != null) {
            HttpSession session = request.getSession();
            session.setAttribute(TRANSACTION_TOKEN_KEY + "|" + name, token);
        }
        return token;
    }

    public synchronized String generateToken(HttpServletRequest request) {
        return generateToken(request.getSession().getId());
    }

    public synchronized String generateToken(String sessionId) {
        long current = System.currentTimeMillis();
        if (current == previous) {
            current++;
        }
        previous = current;
        //
        byte[] now = Long.toString(current).getBytes();
        MessageDigest md = DigestUtils.getMd5Digest();
        md.update(sessionId.getBytes());
        md.update(now);
        return toHex(md.digest());
    }

    private String toHex(byte[] buffer) {
        StringBuilder sb = new StringBuilder(buffer.length * 2);
        for (byte aBuffer : buffer) {
            sb.append(Character.forDigit((aBuffer & 0xf0) >> 4, 16));
            sb.append(Character.forDigit(aBuffer & 0x0f, 16));
        }
        return sb.toString();
    }
}
