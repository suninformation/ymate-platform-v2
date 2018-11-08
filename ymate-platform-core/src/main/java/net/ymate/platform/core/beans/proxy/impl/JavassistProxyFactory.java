/*
 * Copyright 2007-2018 the original author or authors.
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
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import net.ymate.platform.core.beans.proxy.AbstractProxyChain;
import net.ymate.platform.core.beans.proxy.AbstractProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxy;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/8 3:01 AM
 * @version 1.0
 * @since 2.0.6
 */
public class JavassistProxyFactory extends AbstractProxyFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createProxy(final Class<?> targetClass, final List<IProxy> proxies) {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(targetClass);
        Class<?> clazz = factory.createClass();
        try {
            Object targetObj = clazz.newInstance();
            ((ProxyObject) targetObj).setHandler(new MethodHandler() {
                @Override
                public Object invoke(final Object self, Method thisMethod, final Method proceed, final Object[] args) throws Throwable {
                    return new AbstractProxyChain(JavassistProxyFactory.this, targetClass, self, thisMethod, args, proxies) {
                        @Override
                        protected Object doInvoke() throws Throwable {
                            return proceed.invoke(getTargetObject(), getMethodParams());
                        }
                    }.doProxyChain();
                }
            });
            return (T) targetObj;
        } catch (Exception e) {
            throw new Error(e);
        }
    }
}
