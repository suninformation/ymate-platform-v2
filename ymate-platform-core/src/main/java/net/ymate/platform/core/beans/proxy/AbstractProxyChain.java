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

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/7 6:00 PM
 */
public abstract class AbstractProxyChain implements IProxyChain {

    private final IProxyFactory owner;
    private final Class<?> targetClass;
    private final Object targetObject;
    private final Method targetMethod;
    private final Object[] methodParams;

    private final List<IProxy> proxies;
    private int index = 0;

    public AbstractProxyChain(IProxyFactory owner, Class<?> targetClass, Object targetObject, Method targetMethod, Object[] methodParams, List<IProxy> proxies) {
        this.owner = owner;
        this.targetClass = targetClass;
        this.targetObject = targetObject;
        this.targetMethod = targetMethod;
        this.methodParams = methodParams;
        this.proxies = proxies;
    }

    @Override
    public IProxyFactory getProxyFactory() {
        return owner;
    }

    @Override
    public Object[] getMethodParams() {
        return methodParams;
    }

    @Override
    public Class<?> getTargetClass() {
        return targetClass;
    }

    @Override
    public Object getTargetObject() {
        return targetObject;
    }

    @Override
    public Method getTargetMethod() {
        return targetMethod;
    }

    @Override
    public Object doProxyChain() throws Throwable {
        Object result;
        if (index < proxies.size()) {
            result = proxies.get(index++).doProxy(this);
        } else {
            result = doInvoke();
        }
        return result;
    }

    /**
     * 执行方法调用
     *
     * @return 返回方法执行结果
     * @throws Throwable 可能产生任何异常
     */
    protected abstract Object doInvoke() throws Throwable;
}
