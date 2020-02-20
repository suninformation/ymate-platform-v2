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
package net.ymate.platform.webmvc.impl;

import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.core.beans.intercept.IInterceptor;
import net.ymate.platform.core.beans.intercept.InterceptContext;
import net.ymate.platform.webmvc.*;
import net.ymate.platform.webmvc.annotation.ResponseCache;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.support.RequestExecutor;
import net.ymate.platform.webmvc.view.IView;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认请求拦截规则处理器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 16/1/8 下午11:27
 */
public class DefaultInterceptorRuleProcessor implements IInterceptorRuleProcessor {

    private IWebMvc owner;

    private boolean initialized;

    private final Map<String, InterceptorRuleMeta> interceptorRules = new HashMap<>();

    @Override
    public void initialize(IWebMvc owner) throws Exception {
        this.owner = owner;
        this.initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void registerInterceptorRule(Class<? extends IInterceptorRule> targetClass) throws Exception {
        Arrays.stream(targetClass.getMethods()).map(method -> new InterceptorRuleMeta(owner, targetClass, method)).filter(ruleMeta -> StringUtils.isNotBlank(ruleMeta.getMapping())).forEachOrdered(ruleMeta -> interceptorRules.put(ruleMeta.getMapping(), ruleMeta));
    }

    @Override
    public PairObject<IView, ResponseCache> processRequest(IWebMvc owner, IRequestContext requestContext) throws Exception {
        String requestMapping = requestContext.getRequestMapping();
        InterceptorRuleMeta ruleMeta = interceptorRules.get(requestMapping);
        if (ruleMeta == null) {
            while (StringUtils.countMatches(requestMapping, Type.Const.PATH_SEPARATOR) > 1) {
                requestMapping = StringUtils.substringBeforeLast(requestMapping, Type.Const.PATH_SEPARATOR);
                ruleMeta = interceptorRules.get(requestMapping);
                if (ruleMeta != null && ruleMeta.isMatchAll()) {
                    break;
                }
            }
        }
        IView view = null;
        ResponseCache responseCacheAnn = null;
        if (ruleMeta != null) {
            responseCacheAnn = ruleMeta.getResponseCache();
            InterceptContext interceptContext = new InterceptContext(IInterceptor.Direction.BEFORE, owner.getOwner(), null, null, null, ruleMeta.getContextParams());
            //
            for (Class<? extends IInterceptor> interceptClass : ruleMeta.getBeforeIntercepts()) {
                IInterceptor interceptor = owner.getOwner().getInterceptSettings().getInterceptorInstance(owner.getOwner(), interceptClass);
                // 执行前置拦截器，若其结果对象不为空则返回并停止执行
                Object result = interceptor.intercept(interceptContext);
                if (result != null) {
                    view = RequestExecutor.doProcessResultToView(owner, null, result);
                    break;
                }
            }
        }
        return new PairObject<>(view, responseCacheAnn);
    }
}
