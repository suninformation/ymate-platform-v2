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
package net.ymate.platform.core.beans.proxy.impl;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.ymate.platform.core.beans.proxy.AbstractProxyChain;
import net.ymate.platform.core.beans.proxy.AbstractProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxy;

import java.util.List;

/**
 * 默认代理工厂接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-3 下午5:06
 */
public class DefaultProxyFactory extends AbstractProxyFactory {

    public DefaultProxyFactory() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<?> targetClass, List<IProxy> proxies) {
        return (T) Enhancer.create(targetClass, (MethodInterceptor) (targetObject, targetMethod, methodParams, methodProxy) -> new AbstractProxyChain(DefaultProxyFactory.this, targetClass, targetObject, targetMethod, methodParams, proxies) {
            @Override
            protected Object doInvoke() throws Throwable {
                return methodProxy.invokeSuper(getTargetObject(), getMethodParams());
            }
        }.doProxyChain());
    }
}
