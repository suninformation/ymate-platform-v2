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

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/7 6:00 PM
 * @version 1.0
 */
public abstract class AbstractProxyChain implements IProxyChain {

    private final IProxyFactory __owner;
    private final Class<?> targetClass;
    private final Object targetObject;
    private final Method targetMethod;
    private final Object[] methodParams;

    private final List<IProxy> proxies;
    private int __index = 0;

    public AbstractProxyChain(IProxyFactory owner,
                              Class<?> targetClass,
                              Object targetObject,
                              Method targetMethod,
                              Object[] methodParams,
                              List<IProxy> proxies) {
        this.__owner = owner;
        this.targetClass = targetClass;
        this.targetObject = targetObject;
        this.targetMethod = targetMethod;
        this.methodParams = methodParams;
        this.proxies = proxies;
    }

    @Override
    public IProxyFactory getProxyFactory() {
        return __owner;
    }

    @Override
    public Object[] getMethodParams() {
        return this.methodParams;
    }

    @Override
    public Class<?> getTargetClass() {
        return this.targetClass;
    }

    @Override
    public Object getTargetObject() {
        return this.targetObject;
    }

    @Override
    public Method getTargetMethod() {
        return this.targetMethod;
    }

    @Override
    public Object doProxyChain() throws Throwable {
        Object _result;
        if (__index < proxies.size()) {
            _result = proxies.get(__index++).doProxy(this);
        } else {
            _result = doInvoke();
        }
        return _result;
    }

    protected abstract Object doInvoke() throws Throwable;
}
