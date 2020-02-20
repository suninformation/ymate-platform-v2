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
package net.ymate.platform.webmvc;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.beans.intercept.IInterceptor;
import net.ymate.platform.core.beans.intercept.InterceptMeta;
import net.ymate.platform.webmvc.annotation.InterceptorRule;
import net.ymate.platform.webmvc.annotation.ResponseCache;
import net.ymate.platform.webmvc.base.Type;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 16/1/8 下午11:27
 */
public class InterceptorRuleMeta {

    private String mapping;

    private Set<Class<? extends IInterceptor>> beforeIntercepts;

    private Map<String, String> contextParams;

    private boolean matchAll;

    private ResponseCache responseCache;

    public InterceptorRuleMeta(IWebMvc owner, Class<? extends IInterceptorRule> targetClass, Method targetMethod) {
        InterceptorRule interceptorRuleAnn = targetMethod.getAnnotation(InterceptorRule.class);
        if (interceptorRuleAnn != null && StringUtils.isNotBlank(interceptorRuleAnn.value())) {
            String requestMapping = interceptorRuleAnn.value();
            if (!StringUtils.startsWith(requestMapping, Type.Const.PATH_SEPARATOR)) {
                requestMapping += Type.Const.PATH_SEPARATOR;
            }
            //
            interceptorRuleAnn = targetClass.getAnnotation(InterceptorRule.class);
            if (interceptorRuleAnn != null) {
                this.mapping = StringUtils.trimToEmpty(interceptorRuleAnn.value());
                if (StringUtils.endsWith(this.mapping, Type.Const.PATH_SEPARATOR)) {
                    this.mapping = StringUtils.substringBeforeLast(this.mapping, Type.Const.PATH_SEPARATOR);
                }
            }
            this.mapping += requestMapping;
            //
            if (!StringUtils.startsWith(this.mapping, Type.Const.PATH_SEPARATOR)) {
                this.mapping += Type.Const.PATH_SEPARATOR;
            }
            if (StringUtils.endsWith(this.mapping, Type.Const.PATH_SEPARATOR_ALL)) {
                matchAll = true;
                this.mapping = StringUtils.substringBeforeLast(this.mapping, Type.Const.PATH_SEPARATOR_ALL);
            }
            //
            InterceptMeta interceptMeta = new InterceptMeta(owner.getOwner(), targetClass, targetMethod);
            beforeIntercepts = interceptMeta.getBeforeIntercepts();
            contextParams = beforeIntercepts.isEmpty() ? Collections.emptyMap() : owner.getOwner().getInterceptSettings().getContextParams(owner.getOwner(), targetClass, targetMethod);
            //
            this.responseCache = targetMethod.getAnnotation(ResponseCache.class);
            if (this.responseCache == null) {
                this.responseCache = targetClass.getAnnotation(ResponseCache.class);
                if (this.responseCache == null) {
                    this.responseCache = ClassUtils.getPackageAnnotation(targetClass, ResponseCache.class);
                }
            }
        }
    }

    public String getMapping() {
        return mapping;
    }

    public Set<Class<? extends IInterceptor>> getBeforeIntercepts() {
        return beforeIntercepts;
    }

    public Map<String, String> getContextParams() {
        return contextParams;
    }

    public boolean isMatchAll() {
        return matchAll;
    }

    public ResponseCache getResponseCache() {
        return responseCache;
    }

}
