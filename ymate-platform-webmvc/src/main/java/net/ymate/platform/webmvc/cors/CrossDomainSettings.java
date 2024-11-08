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
package net.ymate.platform.webmvc.cors;

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.support.AbstractContext;
import net.ymate.platform.core.support.IInitialization;
import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.RequestMeta;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.cors.annotation.CrossDomainSetting;
import net.ymate.platform.webmvc.cors.impl.DefaultCrossDomainSetting;
import net.ymate.platform.webmvc.util.WebUtils;
import net.ymate.platform.webmvc.validate.IHostNameChecker;
import net.ymate.platform.webmvc.view.IView;
import net.ymate.platform.webmvc.view.View;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-16 14:29
 * @since 2.1.0
 */
public final class CrossDomainSettings implements IInitialization<IWebMvc> {

    private final Map<String, ICrossDomainSetting> resolvedSettings = new ConcurrentHashMap<>();

    private final Map<String, ICrossDomainSetting> settings = new ConcurrentHashMap<>();

    private final DefaultCrossDomainSetting defaultSetting = new DefaultCrossDomainSetting();

    private IWebMvc owner;

    private boolean enabled;

    private boolean initialized;

    public CrossDomainSettings() {
    }

    @Override
    public void initialize(IWebMvc owner) throws Exception {
        if (!initialized) {
            this.owner = owner;
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * 是否开启跨域设置，可选参数，默认值为false
     *
     * @return 返回true表示开启
     */
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (!initialized) {
            this.enabled = enabled;
        }
    }

    public DefaultCrossDomainSetting getDefaultSetting() {
        return defaultSetting;
    }

    public CrossDomainSettings registerSetting(String requestMapping, ICrossDomainSetting setting) {
        if (!initialized) {
            if (StringUtils.isBlank(requestMapping)) {
                throw new NullArgumentException("requestMapping");
            }
            if (setting == null) {
                throw new NullArgumentException("setting");
            }
            ReentrantLockHelper.putIfAbsent(settings, requestMapping, setting);
        }
        return this;
    }

    public ICrossDomainSetting bind(RequestMeta requestMeta, IRequestContext requestContext) throws Exception {
        if (initialized) {
            return ReentrantLockHelper.putIfAbsentAsync(resolvedSettings, requestMeta.getMethod().toString(), () -> {
                String requestMapping = requestContext.getRequestMapping();
                ICrossDomainSetting crossDomainSetting = null;
                for (Map.Entry<String, ICrossDomainSetting> entry : settings.entrySet()) {
                    if (StringUtils.endsWith(entry.getKey(), "/*")) {
                        String key = StringUtils.defaultIfBlank(StringUtils.substringBefore(entry.getKey(), "/*"), "/");
                        if (StringUtils.startsWith(requestMapping, key)) {
                            crossDomainSetting = entry.getValue();
                            break;
                        }
                    } else if (StringUtils.equals(requestMapping, entry.getKey())) {
                        crossDomainSetting = entry.getValue();
                        break;
                    }
                }
                if (crossDomainSetting == null) {
                    // 遍历方法、类及上层包寻找跨域配置注解，直至找不到
                    CrossDomainSetting settingAnn = requestMeta.getMethod().getAnnotation(CrossDomainSetting.class);
                    if (settingAnn == null && (settingAnn = requestMeta.getTargetClass().getAnnotation(CrossDomainSetting.class)) == null) {
                        settingAnn = ClassUtils.getPackageAnnotation(requestMeta.getTargetClass(), CrossDomainSetting.class);
                    }
                    if (settingAnn != null) {
                        crossDomainSetting = DefaultCrossDomainSetting.valueOf(settingAnn);
                    }
                }
                return crossDomainSetting != null ? crossDomainSetting : defaultSetting;
            });
        }
        return defaultSetting;
    }

    public IView process(RequestMeta requestMeta, IRequestContext requestContext, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (enabled) {
            if (WebUtils.isCorsRequest(request)) {
                ICrossDomainSetting domainSetting = bind(requestMeta, requestContext);
                boolean allowed = false;
                String origin = request.getHeader(Type.HttpHead.ORIGIN);
                if (!domainSetting.getAllowedOrigins().isEmpty()) {
                    allowed = domainSetting.getAllowedOrigins().stream().anyMatch(o -> StringUtils.equals(o, "*") || StringUtils.containsIgnoreCase(o, origin));
                }
                if (!allowed) {
                    IHostNameChecker hostNameChecker = domainSetting.getAllowedOriginsChecker();
                    if (hostNameChecker == null) {
                        hostNameChecker = IHostNameChecker.DEFAULT;
                    }
                    IApplication application = owner.getOwner();
                    Map<String, String> contextParams = application.getInterceptSettings().getContextParams(application, requestMeta.getTargetClass(), requestMeta.getMethod());
                    allowed = hostNameChecker.check(new AbstractContext(application, contextParams) {
                    }, origin);
                }
                if (allowed) {
                    response.addHeader(Type.HttpHead.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                } else if (domainSetting.getAllowedOrigins().isEmpty() && domainSetting.getAllowedOriginsChecker() == null) {
                    response.addHeader(Type.HttpHead.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                    allowed = true;
                }
                if (allowed) {
                    if (!domainSetting.getAllowedMethods().isEmpty()) {
                        response.addHeader(Type.HttpHead.ACCESS_CONTROL_ALLOW_METHODS, StringUtils.upperCase(StringUtils.join(domainSetting.getAllowedMethods(), ", ")));
                    }
                    if (!domainSetting.getAllowedHeaders().isEmpty()) {
                        response.addHeader(Type.HttpHead.ACCESS_CONTROL_ALLOW_HEADERS, StringUtils.upperCase(StringUtils.join(domainSetting.getAllowedHeaders(), ", ")));
                    }
                    if (!domainSetting.getExposedHeaders().isEmpty()) {
                        response.addHeader(Type.HttpHead.ACCESS_CONTROL_EXPOSE_HEADERS, StringUtils.upperCase(StringUtils.join(domainSetting.getExposedHeaders(), ", ")));
                    }
                    if (domainSetting.isAllowedCredentials()) {
                        response.addHeader(Type.HttpHead.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.TRUE.toString());
                    }
                    if (domainSetting.getMaxAge() > 0) {
                        response.addHeader(Type.HttpHead.ACCESS_CONTROL_MAX_AGE, String.valueOf(domainSetting.getMaxAge()));
                    }
                }
                if (domainSetting.isOptionsAutoReply() && WebUtils.isCorsOptionsRequest(request)) {
                    return View.nullView();
                }
            }
        }
        return null;
    }
}
