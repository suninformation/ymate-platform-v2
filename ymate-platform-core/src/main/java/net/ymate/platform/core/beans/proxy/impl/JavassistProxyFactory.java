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

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.beans.proxy.AbstractProxyChain;
import net.ymate.platform.core.beans.proxy.AbstractProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyMethodParamHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/8 3:01 AM
 * @since 2.0.6
 */
public class JavassistProxyFactory extends AbstractProxyFactory {

    /**
     * 代理类缓存（避免重复为目标类生成代理类导致 Metaspace 泄漏）
     */
    private final Map<Class<?>, Class<?>> proxyClassCache = new ConcurrentHashMap<>();

    @Override
    protected void doClose() throws Exception {
        proxyClassCache.clear();
    }

    @SuppressWarnings("unchecked")
    private <T> T doCreateProxy(Class<?> targetClass, MethodHandler methodHandler) {
        Class<?> clazz = proxyClassCache.computeIfAbsent(targetClass, key -> {
            ProxyFactory factory = new ProxyFactory();
            factory.setSuperclass(key);
            return factory.createClass();
        });
        try {
            Object targetObj = clazz.getDeclaredConstructor().newInstance();
            ((Proxy) targetObj).setHandler(methodHandler);
            return (T) targetObj;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw RuntimeUtils.wrapRuntimeThrow(e);
        }
    }

    @Override
    public <T> T createProxy(Class<?> targetClass, List<IProxy> proxies) {
        return doCreateProxy(targetClass, (self, thisMethod, proceed, args) -> new AbstractProxyChain(JavassistProxyFactory.this, targetClass, self, thisMethod, args, proxies) {
            @Override
            protected Object doInvoke() throws Throwable {
                return proceed.invoke(getTargetObject(), getMethodParams());
            }
        }.doProxyChain());
    }

    @Override
    public <T> T createProxy(Class<?> targetClass, IProxyMethodParamHandler methodParamHandler) {
        return doCreateProxy(targetClass, (self, thisMethod, proceed, args) -> proceed.invoke(self, methodParamHandler.handle(self, thisMethod, args)));
    }
}
