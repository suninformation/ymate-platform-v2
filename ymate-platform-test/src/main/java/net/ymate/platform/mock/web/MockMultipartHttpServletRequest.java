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

import net.ymate.platform.webmvc.IMultipartRequestWrapper;
import net.ymate.platform.webmvc.IUploadFileWrapper;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.util.FileUploadHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import java.io.File;
import java.util.*;

public class MockMultipartHttpServletRequest extends MockHttpServletRequest implements IMultipartRequestWrapper {

    private final Log LOG = LogFactory.getLog(getClass());

    private final Map<String, List<IUploadFileWrapper>> multipartFiles = new LinkedHashMap<>();

    public MockMultipartHttpServletRequest() {
        this(null);
    }

    public MockMultipartHttpServletRequest(ServletContext servletContext) {
        super(servletContext);
        setMethod("POST");
        setContentType("multipart/form-data");
    }

    public void addFile(String name, final File file) {
        Objects.requireNonNull(name, "MultipartFile name must not be null");
        Objects.requireNonNull(file, "MultipartFile must not be null");
        if (!multipartFiles.containsKey(name)) {
            multipartFiles.put(name, new LinkedList<>());
        }
        multipartFiles.get(name).add(new FileUploadHelper.UploadFileWrapper(file) {
            @Override
            public void delete() {
                // 不真正删除测试文件
                LOG.info("Delete file \"" + file.getPath() + "\"");
            }
        });
    }

    public Type.HttpMethod getRequestMethod() {
        return Type.HttpMethod.valueOf(getMethod());
    }

    @Override
    public IUploadFileWrapper getUploadFile(String name) {
        if (multipartFiles.containsKey(name)) {
            List<IUploadFileWrapper> fileWrappers = multipartFiles.get(name);
            return fileWrappers.isEmpty() ? null : fileWrappers.get(0);
        }
        return null;
    }

    @Override
    public IUploadFileWrapper[] getUploadFiles(String name) {
        if (multipartFiles.containsKey(name)) {
            List<IUploadFileWrapper> fileWrappers = multipartFiles.get(name);
            return fileWrappers.isEmpty() ? new IUploadFileWrapper[0] : fileWrappers.toArray(new IUploadFileWrapper[0]);
        }
        return null;
    }

    @Override
    public Set<IUploadFileWrapper> getUploadFiles() {
        Set<IUploadFileWrapper> returnValues = new HashSet<>();
        for (List<IUploadFileWrapper> fileWrappers : multipartFiles.values()) {
            returnValues.addAll(fileWrappers);
        }
        return returnValues;
    }

    public static class Builder {
        private final Log LOG = LogFactory.getLog(MockMultipartHttpServletRequest.class);
        private final MockHttpServletRequest.Builder parentBuilder;
        private final Map<String, List<IUploadFileWrapper>> multipartFiles = new LinkedHashMap<>();

        public static Builder create() {
            return new Builder();
        }

        public Builder() {
            this.parentBuilder = MockHttpServletRequest.Builder.create()
                    .method("POST")
                    .contentType("multipart/form-data");
        }

        public Builder servletContext(ServletContext servletContext) {
            parentBuilder.servletContext(servletContext);
            return this;
        }

        public Builder method(String method) {
            parentBuilder.method(method);
            return this;
        }

        public Builder requestURI(String requestURI) {
            parentBuilder.requestURI(requestURI);
            return this;
        }

        public Builder contextPath(String contextPath) {
            parentBuilder.contextPath(contextPath);
            return this;
        }

        public Builder servletPath(String servletPath) {
            parentBuilder.servletPath(servletPath);
            return this;
        }

        public Builder pathInfo(String pathInfo) {
            parentBuilder.pathInfo(pathInfo);
            return this;
        }

        public Builder queryString(String queryString) {
            parentBuilder.queryString(queryString);
            return this;
        }

        public Builder protocol(String protocol) {
            parentBuilder.protocol(protocol);
            return this;
        }

        public Builder scheme(String scheme) {
            parentBuilder.scheme(scheme);
            return this;
        }

        public Builder serverName(String serverName) {
            parentBuilder.serverName(serverName);
            return this;
        }

        public Builder serverPort(int serverPort) {
            parentBuilder.serverPort(serverPort);
            return this;
        }

        public Builder remoteAddr(String remoteAddr) {
            parentBuilder.remoteAddr(remoteAddr);
            return this;
        }

        public Builder remoteHost(String remoteHost) {
            parentBuilder.remoteHost(remoteHost);
            return this;
        }

        public Builder remoteUser(String remoteUser) {
            parentBuilder.remoteUser(remoteUser);
            return this;
        }

        public Builder userPrincipal(java.security.Principal userPrincipal) {
            parentBuilder.userPrincipal(userPrincipal);
            return this;
        }

        public Builder secure(boolean secure) {
            parentBuilder.secure(secure);
            return this;
        }

        public Builder requestedSessionIdValid(boolean requestedSessionIdValid) {
            parentBuilder.requestedSessionIdValid(requestedSessionIdValid);
            return this;
        }

        public Builder requestedSessionIdFromCookie(boolean requestedSessionIdFromCookie) {
            parentBuilder.requestedSessionIdFromCookie(requestedSessionIdFromCookie);
            return this;
        }

        public Builder requestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
            parentBuilder.requestedSessionIdFromURL(requestedSessionIdFromURL);
            return this;
        }

        public Builder requestedSessionId(String requestedSessionId) {
            parentBuilder.requestedSessionId(requestedSessionId);
            return this;
        }

        public Builder characterEncoding(String characterEncoding) {
            parentBuilder.characterEncoding(characterEncoding);
            return this;
        }

        public Builder content(byte[] content) {
            parentBuilder.content(content);
            return this;
        }

        public Builder content(String content) {
            parentBuilder.content(content);
            return this;
        }

        public Builder contentType(String contentType) {
            parentBuilder.contentType(contentType);
            return this;
        }

        public Builder asyncSupported(boolean asyncSupported) {
            parentBuilder.asyncSupported(asyncSupported);
            return this;
        }

        public Builder authType(String authType) {
            parentBuilder.authType(authType);
            return this;
        }

        public Builder cookie(javax.servlet.http.Cookie cookie) {
            if (cookie != null) {
                parentBuilder.cookies(cookie);
            }
            return this;
        }

        public Builder cookies(javax.servlet.http.Cookie... cookies) {
            parentBuilder.cookies(cookies);
            return this;
        }

        public Builder attribute(String name, Object value) {
            parentBuilder.addAttribute(name, value);
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            if (attributes != null) {
                attributes.forEach(parentBuilder::addAttribute);
            }
            return this;
        }

        public Builder parameter(String name, String value) {
            parentBuilder.addParameter(name, value);
            return this;
        }

        public Builder parameter(String name, String... values) {
            parentBuilder.addParameter(name, values);
            return this;
        }

        public Builder parameters(Map<String, String[]> parameters) {
            if (parameters != null) {
                parameters.forEach(parentBuilder::addParameter);
            }
            return this;
        }

        public Builder header(String name, Object value) {
            parentBuilder.addHeader(name, value);
            return this;
        }

        public Builder addHeader(String name, Object value) {
            parentBuilder.addHeader(name, value);
            return this;
        }

        public Builder locale(Locale locale) {
            parentBuilder.addLocale(locale);
            return this;
        }

        public Builder locales(List<Locale> locales) {
            if (locales != null) {
                locales.forEach(parentBuilder::addLocale);
            }
            return this;
        }

        public Builder userRole(String role) {
            parentBuilder.addUserRole(role);
            return this;
        }

        public Builder userRoles(Set<String> roles) {
            if (roles != null) {
                roles.forEach(parentBuilder::addUserRole);
            }
            return this;
        }

        public Builder file(String name, File file) {
            Objects.requireNonNull(name, "MultipartFile name must not be null");
            Objects.requireNonNull(file, "MultipartFile must not be null");
            if (!multipartFiles.containsKey(name)) {
                multipartFiles.put(name, new LinkedList<>());
            }
            final Log finalLog = LOG;
            multipartFiles.get(name).add(new FileUploadHelper.UploadFileWrapper(file) {
                @Override
                public void delete() {
                    // 不真正删除测试文件
                    finalLog.info("Delete file \"" + file.getPath() + "\"");
                }
            });
            return this;
        }

        public MockMultipartHttpServletRequest build() {
            // 首先构建基础请求对象
            MockHttpServletRequest baseRequest = parentBuilder.build();
            // 创建 Multipart 请求对象
            MockMultipartHttpServletRequest request = new MockMultipartHttpServletRequest(baseRequest.getServletContext());
            // 复制属性
            request.setMethod(baseRequest.getMethod());
            request.setRequestURI(baseRequest.getRequestURI());
            request.setContextPath(baseRequest.getContextPath());
            request.setServletPath(baseRequest.getServletPath());
            request.setPathInfo(baseRequest.getPathInfo());
            request.setQueryString(baseRequest.getQueryString());
            request.setProtocol(baseRequest.getProtocol());
            request.setScheme(baseRequest.getScheme());
            request.setServerName(baseRequest.getServerName());
            request.setServerPort(baseRequest.getServerPort());
            request.setRemoteAddr(baseRequest.getRemoteAddr());
            request.setRemoteHost(baseRequest.getRemoteHost());
            request.setRemoteUser(baseRequest.getRemoteUser());
            request.setUserPrincipal(baseRequest.getUserPrincipal());
            request.setSecure(baseRequest.isSecure());
            request.setRequestedSessionIdValid(baseRequest.isRequestedSessionIdValid());
            request.setRequestedSessionIdFromCookie(baseRequest.isRequestedSessionIdFromCookie());
            request.setRequestedSessionIdFromURL(baseRequest.isRequestedSessionIdFromURL());
            request.setRequestedSessionId(baseRequest.getRequestedSessionId());
            request.setCharacterEncoding(baseRequest.getCharacterEncoding());
            request.setContentType(baseRequest.getContentType());
            request.setAsyncSupported(baseRequest.isAsyncSupported());
            request.setAuthType(baseRequest.getAuthType());
            // 复制 cookies
            javax.servlet.http.Cookie[] cookies = baseRequest.getCookies();
            if (cookies != null) {
                request.setCookies(cookies);
            }
            // 复制 attributes
            Enumeration<String> attributeNames = baseRequest.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String name = attributeNames.nextElement();
                request.setAttribute(name, baseRequest.getAttribute(name));
            }
            // 复制 parameters
            Map<String, String[]> parameters = baseRequest.getParameterMap();
            for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
                request.setParameter(entry.getKey(), entry.getValue());
            }
            // 复制 headers
            Enumeration<String> headerNames = baseRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                Enumeration<String> headerValues = baseRequest.getHeaders(name);
                while (headerValues.hasMoreElements()) {
                    request.addHeader(name, headerValues.nextElement());
                }
            }
            // 复制 locales
            Enumeration<Locale> locales = baseRequest.getLocales();
            while (locales.hasMoreElements()) {
                request.addPreferredLocale(locales.nextElement());
            }
            // 复制 multipart files
            // 注意：由于 request.multipartFiles 是私有属性，我们需要通过反射或提供的方法来添加文件
            // 这里我们修改一下实现，直接访问私有属性
            try {
                java.lang.reflect.Field multipartFilesField = MockMultipartHttpServletRequest.class.getDeclaredField("multipartFiles");
                multipartFilesField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<String, List<IUploadFileWrapper>> targetMultipartFiles = (Map<String, List<IUploadFileWrapper>>) multipartFilesField.get(request);
                targetMultipartFiles.putAll(this.multipartFiles);
            } catch (Exception e) {
                LOG.error("Failed to set multipart files", e);
            }
            return request;
        }
    }
}
