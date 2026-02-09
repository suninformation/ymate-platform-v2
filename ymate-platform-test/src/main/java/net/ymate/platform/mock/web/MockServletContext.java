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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.FileTypeMap;
import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class MockServletContext implements ServletContext {

    private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

    private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";

    private static final Set<SessionTrackingMode> DEFAULT_SESSION_TRACKING_MODES = new LinkedHashSet<>(3);

    static {
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.COOKIE);
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.URL);
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.SSL);
    }

    private final Log LOG = LogFactory.getLog(getClass());

    private final ClassLoader resourceLoader;

    private final String resourceBasePath;

    private String contextPath = "";

    private final Map<String, ServletContext> contexts = new HashMap<String, ServletContext>();

    private int majorVersion = 3;

    private int minorVersion = 0;

    private int effectiveMajorVersion = 3;

    private int effectiveMinorVersion = 0;

    private final Map<String, RequestDispatcher> namedRequestDispatchers = new HashMap<String, RequestDispatcher>();

    private String defaultServletName = COMMON_DEFAULT_SERVLET_NAME;

    private final Map<String, String> initParameters = new LinkedHashMap<String, String>();

    private final Map<String, Object> attributes = new LinkedHashMap<String, Object>();

    private String servletContextName = "MockServletContext";

    private final Set<String> declaredRoles = new LinkedHashSet<String>();

    private Set<SessionTrackingMode> sessionTrackingModes;

    private final SessionCookieConfig sessionCookieConfig = new MockSessionCookieConfig();

    public MockServletContext() {
        this("", null);
    }

    public MockServletContext(String resourceBasePath) {
        this(resourceBasePath, null);
    }

    public MockServletContext(ClassLoader resourceLoader) {
        this("", resourceLoader);
    }

    public MockServletContext(String resourceBasePath, ClassLoader resourceLoader) {
        this.resourceLoader = (resourceLoader != null ? resourceLoader : ClassUtils.getDefaultClassLoader());
        this.resourceBasePath = (resourceBasePath != null ? resourceBasePath : "");
        String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
        if (tempDir != null) {
            this.attributes.put("javax.servlet.context.tempdir", new File(tempDir));
        }
        registerNamedDispatcher(this.defaultServletName, new MockRequestDispatcher(this.defaultServletName));
    }

    protected String getResourceLocation(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return this.resourceBasePath + path;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = (contextPath != null ? contextPath : "");
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    public void registerContext(String contextPath, ServletContext context) {
        this.contexts.put(contextPath, context);
    }

    @Override
    public ServletContext getContext(String contextPath) {
        if (this.contextPath.equals(contextPath)) {
            return this;
        }
        return this.contexts.get(contextPath);
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    @Override
    public int getMajorVersion() {
        return this.majorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    @Override
    public int getMinorVersion() {
        return this.minorVersion;
    }

    public void setEffectiveMajorVersion(int effectiveMajorVersion) {
        this.effectiveMajorVersion = effectiveMajorVersion;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return this.effectiveMajorVersion;
    }

    public void setEffectiveMinorVersion(int effectiveMinorVersion) {
        this.effectiveMinorVersion = effectiveMinorVersion;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return this.effectiveMinorVersion;
    }

    private static final Map<String, String> MIME_TYPES = new HashMap<String, String>();

    static {
        MIME_TYPES.put("json", "application/json");
        MIME_TYPES.put("xml", "application/xml");
        MIME_TYPES.put("html", "text/html");
        MIME_TYPES.put("htm", "text/html");
        MIME_TYPES.put("css", "text/css");
        MIME_TYPES.put("js", "application/javascript");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("gif", "image/gif");
    }

    @Override
    public String getMimeType(String filePath) {
        if (filePath != null) {
            int lastDotIndex = filePath.lastIndexOf('.');
            if (lastDotIndex != -1 && lastDotIndex < filePath.length() - 1) {
                String extension = filePath.substring(lastDotIndex + 1).toLowerCase();
                if (MIME_TYPES.containsKey(extension)) {
                    return MIME_TYPES.get(extension);
                }
            }
        }
        String mimeType = FileTypeMap.getDefaultFileTypeMap().getContentType(filePath);
        return ("application/octet-stream".equals(mimeType) ? null : mimeType);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        String actualPath = (path.endsWith("/") ? path : path + "/");
        URL resource = this.resourceLoader.getResource(getResourceLocation(actualPath));
        if (resource == null) {
            return Collections.emptySet();
        }
        try {
            File file = new File(resource.getFile());
            String[] fileList = file.list();
            if (fileList == null || ArrayUtils.isEmpty(fileList)) {
                return Collections.emptySet();
            }
            Set<String> resourcePaths = new LinkedHashSet<>(fileList.length);
            for (String fileEntry : fileList) {
                String resultPath = actualPath + fileEntry;
                if (new File(file, resultPath).isDirectory()) {
                    resultPath += "/";
                }
                resourcePaths.add(resultPath);
            }
            return resourcePaths;
        } catch (Exception ex) {
            LOG.warn("Couldn't get resource paths for " + resource, ex);
            return Collections.emptySet();
        }
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return this.resourceLoader.getResource(getResourceLocation(path));
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        URL resource = this.resourceLoader.getResource(getResourceLocation(path));
        if (resource == null) {
            return null;
        }
        try {
            return resource.openStream();
        } catch (IOException ex) {
            LOG.warn("Couldn't open InputStream for " + resource, ex);
            return null;
        }
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("RequestDispatcher path at ServletContext level must start with '/'");
        }
        return new MockRequestDispatcher(path);
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String path) {
        return this.namedRequestDispatchers.get(path);
    }

    public void registerNamedDispatcher(String name, RequestDispatcher requestDispatcher) {
        Objects.requireNonNull(name, "RequestDispatcher name must not be null");
        Objects.requireNonNull(requestDispatcher, "RequestDispatcher must not be null");
        this.namedRequestDispatchers.put(name, requestDispatcher);
    }

    public void unregisterNamedDispatcher(String name) {
        Objects.requireNonNull(name, "RequestDispatcher name must not be null");
        this.namedRequestDispatchers.remove(name);
    }

    public String getDefaultServletName() {
        return this.defaultServletName;
    }

    public void setDefaultServletName(String defaultServletName) {
        Objects.requireNonNull(defaultServletName, "defaultServletName must not be null");
        if (StringUtils.isBlank(defaultServletName)) {
            throw new IllegalArgumentException("defaultServletName must not be empty");
        }
        unregisterNamedDispatcher(this.defaultServletName);
        this.defaultServletName = defaultServletName;
        registerNamedDispatcher(this.defaultServletName, new MockRequestDispatcher(this.defaultServletName));
    }

    @Override
    @Deprecated
    public Servlet getServlet(String name) {
        return null;
    }

    @Override
    @Deprecated
    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration(Collections.emptySet());
    }

    @Override
    @Deprecated
    public Enumeration<String> getServletNames() {
        return Collections.enumeration(Collections.emptySet());
    }

    @Override
    public void log(String message) {
        LOG.info(message);
    }

    @Override
    @Deprecated
    public void log(Exception ex, String message) {
        LOG.info(message, ex);
    }

    @Override
    public void log(String message, Throwable ex) {
        LOG.info(message, ex);
    }

    @Override
    public String getRealPath(String path) {
        URL resource = this.resourceLoader.getResource(getResourceLocation(path));
        File targetFile = FileUtils.toFile(resource);
        if (targetFile == null) {
            return null;
        }
        return targetFile.getAbsolutePath();
    }

    @Override
    public String getServerInfo() {
        return "MockServletContext";
    }

    @Override
    public String getInitParameter(String name) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        return this.initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        if (this.initParameters.containsKey(name)) {
            return false;
        }
        this.initParameters.put(name, value);
        return true;
    }

    public void addInitParameter(String name, String value) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        this.initParameters.put(name, value);
    }

    @Override
    public Object getAttribute(String name) {
        Objects.requireNonNull(name, "Attribute name must not be null");
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(new LinkedHashSet<>(this.attributes.keySet()));
    }

    @Override
    public void setAttribute(String name, Object value) {
        Objects.requireNonNull(name, "Attribute name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
        } else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        Objects.requireNonNull(name, "Attribute name must not be null");
        this.attributes.remove(name);
    }

    public void setServletContextName(String servletContextName) {
        this.servletContextName = servletContextName;
    }

    @Override
    public String getServletContextName() {
        return this.servletContextName;
    }

    @Override
    public ClassLoader getClassLoader() {
        return ClassUtils.getDefaultClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {
        Objects.requireNonNull(roleNames, "Role names array must not be null");
        for (String roleName : roleNames) {
            Objects.requireNonNull(roleName, "Role name must not be null");
            if (StringUtils.isBlank(roleName)) {
                throw new IllegalArgumentException("Role name must not be empty");
            }
            this.declaredRoles.add(roleName);
        }
    }

    public Set<String> getDeclaredRoles() {
        return Collections.unmodifiableSet(this.declaredRoles);
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
            throws IllegalStateException, IllegalArgumentException {
        this.sessionTrackingModes = sessionTrackingModes;
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return DEFAULT_SESSION_TRACKING_MODES;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return (this.sessionTrackingModes != null ?
                Collections.unmodifiableSet(this.sessionTrackingModes) : DEFAULT_SESSION_TRACKING_MODES);
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return this.sessionCookieConfig;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return Collections.emptyMap();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return null;
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return Collections.emptyMap();
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }

    public static class Builder {
        private String resourceBasePath = "";
        private ClassLoader resourceLoader;
        private String contextPath = "";
        private int majorVersion = 3;
        private int minorVersion = 0;
        private int effectiveMajorVersion = 3;
        private int effectiveMinorVersion = 0;
        private String defaultServletName = COMMON_DEFAULT_SERVLET_NAME;
        private final Map<String, String> initParameters = new LinkedHashMap<>();
        private final Map<String, Object> attributes = new LinkedHashMap<>();
        private String servletContextName = "MockServletContext";
        private final Set<String> declaredRoles = new LinkedHashSet<>();
        private Set<SessionTrackingMode> sessionTrackingModes;
        private final MockSessionCookieConfig sessionCookieConfig = new MockSessionCookieConfig();

        public static Builder create() {
            return new Builder();
        }

        public Builder resourceBasePath(String resourceBasePath) {
            this.resourceBasePath = (resourceBasePath != null ? resourceBasePath : "");
            return this;
        }

        public Builder resourceLoader(ClassLoader resourceLoader) {
            this.resourceLoader = resourceLoader;
            return this;
        }

        public Builder contextPath(String contextPath) {
            this.contextPath = (contextPath != null ? contextPath : "");
            return this;
        }

        public Builder majorVersion(int majorVersion) {
            this.majorVersion = majorVersion;
            return this;
        }

        public Builder minorVersion(int minorVersion) {
            this.minorVersion = minorVersion;
            return this;
        }

        public Builder effectiveMajorVersion(int effectiveMajorVersion) {
            this.effectiveMajorVersion = effectiveMajorVersion;
            return this;
        }

        public Builder effectiveMinorVersion(int effectiveMinorVersion) {
            this.effectiveMinorVersion = effectiveMinorVersion;
            return this;
        }

        public Builder defaultServletName(String defaultServletName) {
            this.defaultServletName = defaultServletName;
            return this;
        }

        public Builder initParameter(String name, String value) {
            Objects.requireNonNull(name, "Parameter name must not be null");
            this.initParameters.put(name, value);
            return this;
        }

        public Builder initParameters(Map<String, String> initParameters) {
            if (initParameters != null) {
                this.initParameters.putAll(initParameters);
            }
            return this;
        }

        public Builder attribute(String name, Object value) {
            Objects.requireNonNull(name, "Attribute name must not be null");
            this.attributes.put(name, value);
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public Builder servletContextName(String servletContextName) {
            this.servletContextName = servletContextName;
            return this;
        }

        public Builder declaredRole(String roleName) {
            Objects.requireNonNull(roleName, "Role name must not be null");
            this.declaredRoles.add(roleName);
            return this;
        }

        public Builder declaredRoles(String... roleNames) {
            if (roleNames != null) {
                for (String roleName : roleNames) {
                    declaredRole(roleName);
                }
            }
            return this;
        }

        public Builder declaredRoles(Set<String> roleNames) {
            if (roleNames != null) {
                this.declaredRoles.addAll(roleNames);
            }
            return this;
        }

        public Builder sessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
            this.sessionTrackingModes = sessionTrackingModes;
            return this;
        }

        public Builder sessionCookieConfig(MockSessionCookieConfig sessionCookieConfig) {
            // 这里可以添加设置 sessionCookieConfig 属性的代码
            return this;
        }

        public MockServletContext build() {
            MockServletContext servletContext = new MockServletContext(resourceBasePath, resourceLoader);
            // 设置属性
            servletContext.setContextPath(contextPath);
            servletContext.setMajorVersion(majorVersion);
            servletContext.setMinorVersion(minorVersion);
            servletContext.setEffectiveMajorVersion(effectiveMajorVersion);
            servletContext.setEffectiveMinorVersion(effectiveMinorVersion);
            if (!COMMON_DEFAULT_SERVLET_NAME.equals(defaultServletName)) {
                servletContext.setDefaultServletName(defaultServletName);
            }
            // 添加初始化参数
            for (Map.Entry<String, String> entry : initParameters.entrySet()) {
                servletContext.addInitParameter(entry.getKey(), entry.getValue());
            }
            // 添加属性
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                servletContext.setAttribute(entry.getKey(), entry.getValue());
            }
            // 设置 servletContextName
            servletContext.setServletContextName(servletContextName);
            // 声明角色
            if (!declaredRoles.isEmpty()) {
                servletContext.declareRoles(declaredRoles.toArray(new String[0]));
            }
            // 设置会话跟踪模式
            if (sessionTrackingModes != null) {
                servletContext.setSessionTrackingModes(sessionTrackingModes);
            }
            return servletContext;
        }
    }
}
