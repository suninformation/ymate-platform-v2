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
package net.ymate.platform.core.beans.proxy.impl;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.*;
import net.bytebuddy.matcher.ElementMatchers;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.beans.proxy.AbstractProxyChain;
import net.ymate.platform.core.beans.proxy.AbstractProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyMethodParamHandler;

import java.lang.reflect.Method;
import java.util.List;

/**
 * ByteBuddy代理工厂接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2026/3/12 23:12
 * @since 2.1.4
 */
public class ByteBuddyProxyFactory extends AbstractProxyFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<?> targetClass, List<IProxy> proxies) {
        try (DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .subclass(targetClass)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(new ProxyInterceptor(this, targetClass, proxies)))
                .make()) {
            return (T) unloaded.load(getClass().getClassLoader())
                    .getLoaded()
                    .newInstance();
        } catch (Exception e) {
            throw RuntimeUtils.wrapRuntimeThrow(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(Class<?> targetClass, IProxyMethodParamHandler methodParamHandler) {
        try (DynamicType.Unloaded<?> unloaded = new ByteBuddy()
                .subclass(targetClass)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(new MethodParamInterceptor(methodParamHandler)))
                .make()) {
            return (T) unloaded.load(getClass().getClassLoader())
                    .getLoaded()
                    .newInstance();
        } catch (Exception e) {
            throw RuntimeUtils.wrapRuntimeThrow(e);
        }
    }

    /**
     * 代理链拦截器
     */
    public static class ProxyInterceptor {

        private final AbstractProxyFactory proxyFactory;

        private final Class<?> targetClass;

        private final List<IProxy> proxies;

        public ProxyInterceptor(AbstractProxyFactory proxyFactory, Class<?> targetClass, List<IProxy> proxies) {
            this.proxyFactory = proxyFactory;
            this.targetClass = targetClass;
            this.proxies = proxies;
        }

        @RuntimeType
        public Object intercept(@This Object target, @AllArguments Object[] args, @Origin Method method, @SuperMethod Method superMethod) throws Throwable {
            return new AbstractProxyChain(proxyFactory, targetClass, target, method, args, proxies) {
                @Override
                protected Object doInvoke() throws Throwable {
                    return superMethod.invoke(target, args);
                }
            }.doProxyChain();
        }
    }

    /**
     * 方法参数拦截器
     */
    public static class MethodParamInterceptor {

        private final IProxyMethodParamHandler methodParamHandler;

        public MethodParamInterceptor(IProxyMethodParamHandler methodParamHandler) {
            this.methodParamHandler = methodParamHandler;
        }

        @RuntimeType
        public Object intercept(@This Object target, @AllArguments Object[] args, @Origin Method method, @SuperMethod Method superMethod) throws Throwable {
            Object[] newArgs = methodParamHandler.handle(target, method, args);
            return superMethod.invoke(target, newArgs);
        }
    }
}