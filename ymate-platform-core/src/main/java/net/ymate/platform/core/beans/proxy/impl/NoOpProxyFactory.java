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

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.beans.proxy.IProxyFilter;
import net.ymate.platform.core.beans.proxy.IProxyMethodParamHandler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 空操作代理工厂（使用它表示需要禁用框架的AOP特性, 主要用于Android应用）
 *
 * @author 刘镇 (suninformation@163.com) on 2018/11/8 4:26 PM
 * @since 2.0.6
 */
public class NoOpProxyFactory implements IProxyFactory {

    private IApplication owner;

    private boolean initialized;

    @Override
    public void initialize(IApplication owner) {
        if (!initialized) {
            this.owner = owner;
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
            owner = null;
        }
    }

    @Override
    public IProxyFactory registerProxy(IProxy proxy) {
        return this;
    }

    @Override
    public IProxyFactory registerProxy(Collection<? extends IProxy> proxies) {
        return this;
    }

    @Override
    public List<IProxy> getProxies() {
        return Collections.emptyList();
    }

    @Override
    public List<IProxy> getProxies(IProxyFilter filter) {
        return Collections.emptyList();
    }

    @Override
    public <T> T createProxy(Class<?> targetClass) {
        return null;
    }

    @Override
    public <T> T createProxy(Class<?> targetClass, List<IProxy> proxies) {
        return null;
    }

    @Override
    public <T> T createProxy(Class<?> targetClass, IProxyMethodParamHandler methodParamHandler) {
        return null;
    }
}
