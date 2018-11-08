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
package net.ymate.platform.core.beans.proxy;

import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.annotation.Proxy;

import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/7 5:16 PM
 * @version 1.0
 */
public abstract class AbstractProxyFactory implements IProxyFactory {

    private YMP __owner;

    private final List<IProxy> __proxies;

    public AbstractProxyFactory() {
        this.__proxies = new ArrayList<IProxy>();
    }

    @Override
    public void init(YMP owner) throws Exception {
        __owner = owner;
    }

    @Override
    public YMP getOwner() {
        return __owner;
    }

    private void __doSort() {
        if (__proxies.size() > 1) {
            Collections.sort(__proxies, new Comparator<IProxy>() {
                @Override
                public int compare(IProxy o1, IProxy o2) {
                    Proxy _o1 = o1.getClass().getAnnotation(Proxy.class);
                    Proxy _o2 = o2.getClass().getAnnotation(Proxy.class);
                    return _o1.order().value() - _o2.order().value();
                }
            });
        }
    }

    @Override
    public IProxyFactory registerProxy(IProxy proxy) {
        this.__proxies.add(proxy);
        __doSort();
        return this;
    }

    @Override
    public IProxyFactory registerProxy(Collection<? extends IProxy> proxies) {
        this.__proxies.addAll(proxies);
        __doSort();
        return this;
    }

    @Override
    public List<IProxy> getProxies() {
        return Collections.unmodifiableList(this.__proxies);
    }

    @Override
    public List<IProxy> getProxies(IProxyFilter filter) {
        List<IProxy> _returnValue = new ArrayList<IProxy>();
        for (IProxy _proxy : __proxies) {
            if (filter.filter(_proxy)) {
                _returnValue.add(_proxy);
            }
        }
        return _returnValue;
    }

    @Override
    public <T> T createProxy(Class<?> targetClass) {
        return createProxy(targetClass, __proxies);
    }
}
