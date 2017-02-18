/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.core.beans.annotation.*;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyChain;
import org.apache.commons.codec.digest.DigestUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 拦截器代理，支持@Before和@After方法注解
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/19 下午12:01
 * @version 1.0
 */
@Proxy(order = @Order(-999))
public class InterceptProxy implements IProxy {

    private static Map<String, List<Class<? extends IInterceptor>>> __beforeInterceptsCache;

    private static Map<String, List<Class<? extends IInterceptor>>> __afterInterceptsCache;

    private static final Object __beforeCacheLocker = new Object();

    private static final Object __afterCacheLocker = new Object();

    private static final Set<String> __excludedMethodNames;

    static {
        __beforeInterceptsCache = new ConcurrentHashMap<String, List<Class<? extends IInterceptor>>>();
        __afterInterceptsCache = new ConcurrentHashMap<String, List<Class<? extends IInterceptor>>>();
        //
        __excludedMethodNames = new HashSet<String>();
        for (Method _method : Object.class.getDeclaredMethods()) {
            __excludedMethodNames.add(_method.getName());
        }
    }

    public Object doProxy(IProxyChain proxyChain) throws Throwable {
        // 方法声明了@Ignored注解或非PUBLIC方法和Object类方法将被排除
        boolean _ignored = proxyChain.getTargetMethod().isAnnotationPresent(Ignored.class);
        if (_ignored || __excludedMethodNames.contains(proxyChain.getTargetMethod().getName())
                || proxyChain.getTargetMethod().getDeclaringClass().equals(Object.class)
                || proxyChain.getTargetMethod().getModifiers() != Modifier.PUBLIC) {
            return proxyChain.doProxyChain();
        }
        //
        Map<String, String> _contextParams = null;
        // 尝试处理@Before注解
        if (proxyChain.getTargetClass().isAnnotationPresent(Before.class)
                || proxyChain.getTargetMethod().isAnnotationPresent(Before.class)) {

            _contextParams = InterceptAnnoHelper.getContextParams(proxyChain.getProxyFactory().getOwner(), proxyChain.getTargetClass(), proxyChain.getTargetMethod());

            InterceptContext _context = new InterceptContext(IInterceptor.Direction.BEFORE,
                    proxyChain.getProxyFactory().getOwner(),
                    proxyChain.getTargetObject(),
                    proxyChain.getTargetMethod(),
                    proxyChain.getMethodParams(), _contextParams);
            //
            for (Class<? extends IInterceptor> _interceptClass : __doGetBeforeIntercepts(proxyChain.getTargetClass(), proxyChain.getTargetMethod())) {
                IInterceptor _interceptor = _interceptClass.newInstance();
                // 执行前置拦截器，若其结果对象不为空则返回并停止执行
                Object _resultObj = _interceptor.intercept(_context);
                if (_resultObj != null) {
                    return _resultObj;
                }
            }
        }
        // 若前置拦截器未返回结果，则正常执行目标方法
        Object _returnValue = proxyChain.doProxyChain();
        // 尝试处理@After注解
        if (proxyChain.getTargetClass().isAnnotationPresent(After.class)
                || proxyChain.getTargetMethod().isAnnotationPresent(After.class)) {

            if (_contextParams == null) {
                _contextParams = InterceptAnnoHelper.getContextParams(proxyChain.getProxyFactory().getOwner(), proxyChain.getTargetClass(), proxyChain.getTargetMethod());
            }
            // 初始化拦截器上下文对象，并将当前方法的执行结果对象赋予后置拦截器使用
            InterceptContext _context = new InterceptContext(IInterceptor.Direction.AFTER,
                    proxyChain.getProxyFactory().getOwner(),
                    proxyChain.getTargetObject(),
                    proxyChain.getTargetMethod(),
                    proxyChain.getMethodParams(), _contextParams);
            _context.setResultObject(_returnValue);
            //
            for (Class<? extends IInterceptor> _interceptClass : __doGetAfterIntercepts(proxyChain.getTargetClass(), proxyChain.getTargetMethod())) {
                IInterceptor _interceptor = _interceptClass.newInstance();
                // 执行后置拦截器，所有后置拦截器的执行结果都将被忽略
                _interceptor.intercept(_context);
            }
        }
        return _returnValue;
    }

    private List<Class<? extends IInterceptor>> __doGetBeforeIntercepts(Class<?> targetClass, Method targetMethod) {
        String _cacheKey = DigestUtils.md5Hex(targetClass.toString() + targetMethod.toString());
        //
        if (__beforeInterceptsCache.containsKey(_cacheKey)) {
            return __beforeInterceptsCache.get(_cacheKey);
        }
        synchronized (__beforeCacheLocker) {
            List<Class<? extends IInterceptor>> _classes = __beforeInterceptsCache.get(_cacheKey);
            if (_classes != null) {
                return _classes;
            }
            _classes = InterceptAnnoHelper.getBeforeIntercepts(targetClass, targetMethod);
            //
            if (!_classes.isEmpty()) {
                __beforeInterceptsCache.put(_cacheKey, _classes);
            }
            //
            return _classes;
        }
    }

    private List<Class<? extends IInterceptor>> __doGetAfterIntercepts(Class<?> targetClass, Method targetMethod) {
        String _cacheKey = DigestUtils.md5Hex(targetClass.toString() + targetMethod.toString());
        //
        if (__afterInterceptsCache.containsKey(_cacheKey)) {
            return __afterInterceptsCache.get(_cacheKey);
        }
        synchronized (__afterCacheLocker) {
            List<Class<? extends IInterceptor>> _classes = __afterInterceptsCache.get(_cacheKey);
            if (_classes != null) {
                return _classes;
            }
            _classes = InterceptAnnoHelper.getAfterIntercepts(targetClass, targetMethod);
            //
            if (!_classes.isEmpty()) {
                __afterInterceptsCache.put(_cacheKey, _classes);
            }
            //
            return _classes;
        }
    }
}
