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
import net.ymate.platform.webmvc.view.View;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 为允许跨域的请求添加必要的请求头参数 (from ymate-framework-core)
 *
 * @author 刘镇 (suninformation@163.com) on 17/3/23 下午5:01
 * @since 2.1.0
 */
public final class CrossDomainAnnotationInterceptor extends AbstractInterceptor {

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
                    response.addHeader(Type.HttpHead.ACCESS_CONTROL_ALLOW_ORIGIN, StringUtils.defaultIfBlank(StringUtils.join(domainSetting.getAllowedOrigins(), ", "), "*"));
                    if (CollectionUtils.isNotEmpty(domainSetting.getAllowedMethods())) {
                        response.addHeader(Type.HttpHead.ACCESS_CONTROL_ALLOW_METHODS, StringUtils.upperCase(StringUtils.join(domainSetting.getAllowedMethods(), ", ")));
                    }
                    if (CollectionUtils.isNotEmpty(domainSetting.getAllowedHeaders())) {
                        response.addHeader(Type.HttpHead.ACCESS_CONTROL_ALLOW_HEADERS, StringUtils.upperCase(StringUtils.join(domainSetting.getAllowedHeaders(), ", ")));
                    }
                    if (CollectionUtils.isNotEmpty(domainSetting.getExposedHeaders())) {
                        response.addHeader(Type.HttpHead.ACCESS_CONTROL_EXPOSE_HEADERS, StringUtils.upperCase(StringUtils.join(domainSetting.getExposedHeaders(), ", ")));
                    }
                    if (domainSetting.isAllowedCredentials()) {
                        response.addHeader(Type.HttpHead.ACCESS_CONTROL_ALLOW_CREDENTIALS, Boolean.TRUE.toString());
                    }
                    if (domainSetting.getMaxAge() > 0) {
                        response.addHeader(Type.HttpHead.ACCESS_CONTROL_MAX_AGE, String.valueOf(domainSetting.getMaxAge()));
                    }
                    //
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

    @Override
    protected Object after(InterceptContext context) throws InterceptException {
        return null;
    }
}
