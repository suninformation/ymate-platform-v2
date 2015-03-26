/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.core.beans.impl.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxyFilter;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 默认代理工厂接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-3 下午5:06
 * @version 1.0
 */
public class DefaultProxyFactory implements IProxyFactory {

    private List<IProxy> __proxies;

    public DefaultProxyFactory() {
        this.__proxies = new ArrayList<IProxy>();
    }

    public IProxyFactory registerProxy(IProxy proxy) {
        this.__proxies.add(proxy);
        return this;
    }

    public IProxyFactory registerProxy(Collection<? extends IProxy> proxies) {
        this.__proxies.addAll(proxies);
        return this;
    }

    public List<IProxy> getProxies() {
        return Collections.unmodifiableList(this.__proxies);
    }

    public List<IProxy> getProxies(IProxyFilter filter) {
        List<IProxy> _returnValue = new ArrayList<IProxy>();
        for (IProxy _proxy : __proxies) {
            if (filter.filter(_proxy)) {
                _returnValue.add(_proxy);
            }
        }
        return _returnValue;
    }


    public <T> T createProxy(Class<?> targetClass) {
        return createProxy(targetClass, __proxies);
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(final Class<?> targetClass, final List<IProxy> proxies) {
        return (T) Enhancer.create(targetClass, new MethodInterceptor() {
            public Object intercept(Object targetObject, Method targetMethod, Object[] methodParams, MethodProxy methodProxy) throws Throwable {
                return new DefaultProxyChain(targetClass, targetObject, targetMethod, methodProxy, methodParams, proxies).doProxyChain();
            }
        });
    }
}
