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
package net.ymate.platform.core.beans.proxy;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.Order;
import net.ymate.platform.core.beans.intercept.InterceptProxy;

import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/7 5:16 PM
 */
public abstract class AbstractProxyFactory implements IProxyFactory {

    private IApplication owner;

    private boolean initialized;

    private final List<IProxy> proxies = new ArrayList<>();

    public AbstractProxyFactory() {
    }

    /**
     * 执行顺序, 数值小的最先执行
     */
    private void proxiesSort() {
        if (proxies.size() > 1) {
            proxies.sort(Comparator.comparingInt(o -> {
                Order orderAnn = o.getClass().getAnnotation(Order.class);
                return orderAnn != null ? orderAnn.value() : 0;
            }));
        }
    }

    @Override
    public void initialize(IApplication owner) {
        if (!initialized) {
            this.owner = owner;
            registerProxy(new InterceptProxy());
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public IApplication getOwner() {
        return owner;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            proxies.clear();
            owner = null;
        }
    }

    @Override
    public IProxyFactory registerProxy(IProxy proxy) {
        this.proxies.add(proxy);
        proxiesSort();
        return this;
    }

    @Override
    public IProxyFactory registerProxy(Collection<? extends IProxy> proxies) {
        this.proxies.addAll(proxies);
        proxiesSort();
        return this;
    }

    @Override
    public List<IProxy> getProxies() {
        return Collections.unmodifiableList(this.proxies);
    }

    @Override
    public List<IProxy> getProxies(IProxyFilter filter) {
        List<IProxy> returnValue = new ArrayList<>();
        proxies.stream().filter(filter::filter).forEachOrdered(returnValue::add);
        return returnValue;
    }

    @Override
    public <T> T createProxy(Class<?> targetClass) {
        return createProxy(targetClass, proxies);
    }
}
