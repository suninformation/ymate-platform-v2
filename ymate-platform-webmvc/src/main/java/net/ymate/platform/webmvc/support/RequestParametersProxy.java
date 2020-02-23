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
package net.ymate.platform.webmvc.support;

import com.alibaba.fastjson.JSON;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.beans.annotation.Order;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyChain;
import net.ymate.platform.validation.IValidation;
import net.ymate.platform.validation.ValidateResult;
import net.ymate.platform.validation.Validations;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.RequestMeta;
import net.ymate.platform.webmvc.annotation.RequestMapping;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.IView;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求参数代理, 用于处理控制器请求参数验证等
 *
 * @author 刘镇 (suninformation@163.com) on 16/3/21 下午8:42
 */
@Order(-80000)
public class RequestParametersProxy implements IProxy {

    @Override
    public Object doProxy(IProxyChain proxyChain) throws Throwable {
        // 该代理仅处理控制器中声明@RequestMapping的方法
        if (proxyChain.getTargetMethod().isAnnotationPresent(RequestMapping.class) && ClassUtils.isNormalMethod(proxyChain.getTargetMethod())) {
            WebContext context = WebContext.getContext();
            IWebMvc owner = context.getOwner();
            RequestMeta requestMeta = context.getAttribute(RequestMeta.class.getName());
            Map<String, Object> paramValues = context.getAttribute(RequestParametersProxy.class.getName());
            //
            IValidation validation = owner.getOwner().getModuleManager().getModule(Validations.class);
            Map<String, ValidateResult> validateResultMap = new HashMap<>(16);
            if (!requestMeta.isSingleton()) {
                validateResultMap = validation.validate(requestMeta.getTargetClass(), paramValues);
            }
            if (!requestMeta.getMethodParamNames().isEmpty()) {
                validateResultMap.putAll(validation.validate(requestMeta.getTargetClass(), requestMeta.getMethod(), paramValues));
            }
            if (!validateResultMap.isEmpty()) {
                IView validationView = null;
                if (owner.getConfig().getErrorProcessor() != null) {
                    validationView = owner.getConfig().getErrorProcessor().onValidation(owner, validateResultMap);
                }
                if (validationView == null) {
                    throw new IllegalArgumentException(JSON.toJSONString(validateResultMap.values()));
                } else {
                    return validationView;
                }
            }
            if (!requestMeta.isSingleton()) {
                ClassUtils.wrapper(proxyChain.getTargetObject()).fromMap(paramValues);
            }
        }
        //
        return proxyChain.doProxyChain();
    }
}
