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
package net.ymate.platform.core.beans.intercept;

import net.ymate.platform.core.beans.annotation.After;
import net.ymate.platform.core.beans.annotation.Before;
import net.ymate.platform.core.beans.annotation.Clean;
import net.ymate.platform.core.beans.annotation.Proxy;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyChain;
import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 拦截器代理，支持@Before和@After方法注解
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/19 下午12:01
 * @version 1.0
 */
@Proxy
public class InterceptProxy implements IProxy {

    public Object doProxy(IProxyChain proxyChain) throws Throwable {
        InterceptContext _context = new InterceptContext(IInterceptor.Direction.BEFORE,
                proxyChain.getProxyFactory().getOwner(),
                proxyChain.getTargetObject(),
                proxyChain.getTargetMethod(),
                proxyChain.getMethodParams());
        // 尝试处理@Before注解
        Object _resultObj = null;
        for (Class<? extends IInterceptor> _interceptClass : __doGetBeforeIntercepts(proxyChain.getTargetClass(), proxyChain.getTargetMethod())) {
            IInterceptor _interceptor = _interceptClass.newInstance();
            // 执行前置拦截器，若其结果对象不为空则返回并停止执行
            _resultObj = _interceptor.intercept(_context);
            if (_resultObj != null) {
                return _resultObj;
            }
        }
        // 若前置拦截器未返回结果，则正常执行目标方法
        Object _returnValue = proxyChain.doProxyChain();
        // 尝试处理@After注解
        // 初始化拦截器上下文对象，并将当前方法的执行结果对象赋予后置拦截器使用
        _context = new InterceptContext(IInterceptor.Direction.AFTER,
                proxyChain.getProxyFactory().getOwner(),
                proxyChain.getTargetObject(),
                proxyChain.getTargetMethod(),
                proxyChain.getMethodParams());
        _context.setResultObject(_returnValue);
        //
        for (Class<? extends IInterceptor> _interceptClass : __doGetAfterIntercepts(proxyChain.getTargetClass(), proxyChain.getTargetMethod())) {
            IInterceptor _interceptor = _interceptClass.newInstance();
            // 执行后置拦截器，所有后置拦截器的执行结果都将被忽略
            _interceptor.intercept(_context);
        }
        return _returnValue;
    }

    private List<Class<? extends IInterceptor>> __doGetBeforeIntercepts(Class<?> targetClass, Method targetMethod) {
        List<Class<? extends IInterceptor>> _classes = new ArrayList<Class<? extends IInterceptor>>();
        if (targetClass.isAnnotationPresent(Before.class)) {
            Before _before = targetClass.getAnnotation(Before.class);
            Clean _clean = __doGetCleanIntercepts(targetMethod);
            //
            if (_clean != null) {
                if (_clean.value().length > 0) {
                    for (Class<? extends IInterceptor> _clazz : _before.value()) {
                        if (ArrayUtils.contains(_clean.value(), _clazz)) {
                            continue;
                        }
                        _classes.add(_clazz);
                    }
                }
            } else {
                Collections.addAll(_classes, _before.value());
            }
        }
        //
        if (targetMethod.isAnnotationPresent(Before.class)) {
            Collections.addAll(_classes, targetMethod.getAnnotation(Before.class).value());
        }
        return _classes;
    }

    private List<Class<? extends IInterceptor>> __doGetAfterIntercepts(Class<?> targetClass, Method targetMethod) {
        List<Class<? extends IInterceptor>> _classes = new ArrayList<Class<? extends IInterceptor>>();
        if (targetClass.isAnnotationPresent(After.class)) {
            After _after = targetClass.getAnnotation(After.class);
            Clean _clean = __doGetCleanIntercepts(targetMethod);
            //
            if (_clean != null) {
                if (_clean.value().length > 0) {
                    for (Class<? extends IInterceptor> _clazz : _after.value()) {
                        if (ArrayUtils.contains(_clean.value(), _clazz)) {
                            continue;
                        }
                        _classes.add(_clazz);
                    }
                }
            } else {
                Collections.addAll(_classes, _after.value());
            }
        }
        //
        if (targetMethod.isAnnotationPresent(After.class)) {
            Collections.addAll(_classes, targetMethod.getAnnotation(After.class).value());
        }
        return _classes;
    }

    private Clean __doGetCleanIntercepts(Method targetMethod) {
        if (targetMethod.isAnnotationPresent(Clean.class)) {
            return targetMethod.getAnnotation(Clean.class);
        }
        return null;
    }
}
