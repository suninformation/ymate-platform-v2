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
package net.ymate.platform.core.handle;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.IBeanHandler;
import net.ymate.platform.core.beans.intercept.InterceptProxy;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyFactory;

/**
 * 代理对象处理器
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/12 下午5:10
 */
public final class ProxyHandler implements IBeanHandler {

    private final IApplication owner;

    public ProxyHandler(IApplication owner) {
        this.owner = owner;
    }

    @Override
    public Object handle(Class<?> targetClass) throws Exception {
        IProxyFactory proxyFactory = owner.getBeanFactory().getProxyFactory();
        if (proxyFactory != null && ClassUtils.isNormalClass(targetClass) && !targetClass.isInterface() && ClassUtils.isInterfaceOf(targetClass, IProxy.class)) {
            // 排除框架内部拦截器代理类，因为框架已经注册了它
            if (!targetClass.equals(InterceptProxy.class)) {
                proxyFactory.registerProxy((IProxy) targetClass.newInstance());
            }
        }
        return null;
    }
}
