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

import net.ymate.platform.core.beans.intercept.AbstractInterceptor;
import net.ymate.platform.core.beans.intercept.InterceptContext;
import net.ymate.platform.core.beans.intercept.InterceptException;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.util.WebUtils;
import net.ymate.platform.webmvc.validate.IHostNameChecker;
import net.ymate.platform.webmvc.view.View;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 为允许跨域的请求添加必要的请求头参数 (from ymate-framework-core)
 *
 * @author 刘镇 (suninformation@163.com) on 17/3/23 下午5:01
 * @since 2.1.0
 */
public final class CrossDomainInterceptor extends AbstractInterceptor {

    @Override
    protected Object before(InterceptContext context) throws InterceptException {
        HttpServletRequest httpServletRequest = WebContext.getRequest();
        if (WebUtils.isCorsRequest(httpServletRequest)) {
            CrossDomainSettings settings = WebContext.getContext().getOwner().getConfig().getCrossDomainSettings();
            if (settings.isEnabled()) {
                try {
                    ICrossDomainSetting domainSetting = settings.bind(context, WebContext.getRequestContext());
                    //
                    HttpServletResponse response = WebContext.getResponse();
                    //
                    boolean allowed = false;
                    String origin = httpServletRequest.getHeader(Type.HttpHead.ORIGIN);
                    if (!domainSetting.getAllowedOrigins().isEmpty()) {
                        allowed = domainSetting.getAllowedOrigins().stream().anyMatch(o -> StringUtils.equals(o, "*") || StringUtils.containsIgnoreCase(o, origin));
                    }
                    if (!allowed) {
                        IHostNameChecker hostNameChecker = domainSetting.getAllowedOriginsChecker();
                        if (hostNameChecker == null) {
                            hostNameChecker = IHostNameChecker.DEFAULT;
                        }
                        allowed = hostNameChecker.check(context, origin);
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
                    if (domainSetting.isOptionsAutoReply() && WebUtils.isCorsOptionsRequest(httpServletRequest)) {
                        return View.nullView();
                    }
                } catch (Exception e) {
                    throw new InterceptException(e.getMessage(), e);
                }
            }
        }
        return null;
    }
}
