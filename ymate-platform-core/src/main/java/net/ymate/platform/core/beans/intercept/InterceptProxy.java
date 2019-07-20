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
package net.ymate.platform.core.beans.intercept;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.*;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyChain;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 拦截器代理，支持@Before、@After和@Around方法注解
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/19 下午12:01
 */
@Order(-90000)
public class InterceptProxy implements IProxy {

    private static final Log LOG = LogFactory.getLog(InterceptProxy.class);

    private static Map<String, InterceptMeta> interceptMetaMap = new ConcurrentHashMap<>();

    private static final Object CACHE_LOCKER = new Object();

    private static final Set<String> EXCLUDED_METHOD_NAMES = new HashSet<>();

    static {
        Arrays.stream(Object.class.getDeclaredMethods()).map(Method::getName).forEach(EXCLUDED_METHOD_NAMES::add);
    }

    @Override
    public Object doProxy(IProxyChain proxyChain) throws Throwable {
        // 若当前目标类为拦截器接口实现类则跳过本次拦截
        if (proxyChain.getTargetObject() instanceof IInterceptor) {
            return proxyChain.doProxyChain();
        }
        // 方法声明了@Ignored注解或非PUBLIC方法和Object类方法将被排除
        boolean ignored = proxyChain.getTargetMethod().isAnnotationPresent(Ignored.class);
        if (ignored || EXCLUDED_METHOD_NAMES.contains(proxyChain.getTargetMethod().getName())
                || proxyChain.getTargetMethod().getDeclaringClass().equals(Object.class)
                || proxyChain.getTargetMethod().getModifiers() != Modifier.PUBLIC) {
            return proxyChain.doProxyChain();
        }
        //
        InterceptMeta interceptMeta = getInterceptMeta(proxyChain.getProxyFactory().getOwner(), proxyChain.getTargetClass(), proxyChain.getTargetMethod());
        //
        InterceptContext context = interceptMeta.hasBeforeIntercepts() ? buildContext(proxyChain, interceptMeta, IInterceptor.Direction.BEFORE) : null;
        if (context != null) {
            for (Class<? extends IInterceptor> interceptClass : interceptMeta.getBeforeIntercepts()) {
                IInterceptor interceptor = getInterceptorInstance(proxyChain.getProxyFactory().getOwner(), interceptClass);
                // 执行前置拦截器，若其结果对象不为空则返回并停止执行
                Object resultObj = interceptor.intercept(context);
                if (resultObj != null) {
                    // 如果目标方法的返回值类型为void则采用异常形式向上层返回拦截器执行结果
                    if (void.class.equals(proxyChain.getTargetMethod().getReturnType())) {
                        throw new InterceptException(resultObj);
                    }
                    return resultObj;
                }
            }
        }
        //
        Object returnValue = proxyChain.doProxyChain();
        //
        if (interceptMeta.hasAfterIntercepts()) {
            if (context == null) {
                context = buildContext(proxyChain, interceptMeta, IInterceptor.Direction.AFTER);
            } else {
                context.setDirection(IInterceptor.Direction.AFTER);
            }
            // 初始化拦截器上下文对象，并将当前方法的执行结果对象赋予后置拦截器使用
            context.setResultObject(returnValue);
            //
            for (Class<? extends IInterceptor> interceptClass : interceptMeta.getAfterIntercepts()) {
                IInterceptor interceptor = getInterceptorInstance(proxyChain.getProxyFactory().getOwner(), interceptClass);
                // 执行后置拦截器，所有后置拦截器的执行结果都将被忽略
                if (interceptor.intercept(context) != null && LOG.isWarnEnabled()) {
                    LOG.warn(String.format("Interceptor class [%s] has a return value in the after direction. Ignored!", interceptClass.getName()));
                }
            }
        }
        return returnValue;
    }

    private InterceptContext buildContext(IProxyChain proxyChain, InterceptMeta interceptMeta, IInterceptor.Direction direction) {
        return new InterceptContext(direction, proxyChain.getProxyFactory().getOwner(),
                proxyChain.getTargetObject(),
                proxyChain.getTargetMethod(),
                proxyChain.getMethodParams(), interceptMeta.getContextParams());
    }

    private IInterceptor getInterceptorInstance(IApplication owner, Class<? extends IInterceptor> interceptClass) throws IllegalAccessException, InstantiationException {
        IInterceptor instance = owner.getBeanFactory().getBean(interceptClass);
        return instance != null ? instance : interceptClass.newInstance();
    }

    private InterceptMeta getInterceptMeta(IApplication owner, Class<?> targetClass, Method targetMethod) {
        String id = DigestUtils.md5Hex(targetClass.toString() + targetMethod.toString());
        if (interceptMetaMap.containsKey(id)) {
            return interceptMetaMap.get(id);
        }
        if (targetClass.isAnnotationPresent(Before.class) || targetClass.isAnnotationPresent(After.class) || targetClass.isAnnotationPresent(Around.class)
                || targetMethod.isAnnotationPresent(Before.class) || targetMethod.isAnnotationPresent(After.class) || targetMethod.isAnnotationPresent(Around.class)
                || owner.getInterceptSettings().hasInterceptPackages(targetClass) || InterceptAnnHelper.hasInterceptAnnotationAny(targetMethod)) {
            synchronized (CACHE_LOCKER) {
                return interceptMetaMap.computeIfAbsent(id, i -> new InterceptMeta(owner, i, targetClass, targetMethod));
            }
        }
        return InterceptMeta.DEFAULT;
    }

    static class InterceptMeta {

        static InterceptMeta DEFAULT = new InterceptMeta("default");

        private final String id;

        private List<Class<? extends IInterceptor>> beforeIntercepts;
        private List<Class<? extends IInterceptor>> afterIntercepts;

        private final Map<String, String> contextParams;

        InterceptMeta(String id) {
            this.id = id;
            this.beforeIntercepts = Collections.emptyList();
            this.afterIntercepts = Collections.emptyList();
            this.contextParams = Collections.emptyMap();
        }

        InterceptMeta(IApplication owner, String id, Class<?> targetClass, Method targetMethod) {
            this.id = id;
            this.contextParams = new HashMap<>();
            this.beforeIntercepts = InterceptAnnHelper.getBeforeInterceptors(targetClass, targetMethod);
            this.afterIntercepts = InterceptAnnHelper.getAfterInterceptors(targetClass, targetMethod);
            //
            owner.getInterceptSettings().getInterceptPackages(targetClass).stream().peek((packageMeta) -> {
                if (!packageMeta.getBeforeIntercepts().isEmpty()) {
                    this.beforeIntercepts.addAll(0, packageMeta.getBeforeIntercepts());
                }
            }).peek((packageMeta) -> {
                if (!packageMeta.getAfterIntercepts().isEmpty()) {
                    this.afterIntercepts.addAll(0, packageMeta.getAfterIntercepts());
                }
            }).forEachOrdered((packageMeta) -> packageMeta.getContextParams().forEach((contextParam) -> {
                InterceptAnnHelper.parseContextParamValue(owner, contextParam, this.contextParams);
            }));
            this.contextParams.putAll(InterceptAnnHelper.getContextParams(owner, targetClass, targetMethod));
            if (owner.getInterceptSettings().isEnabled()) {
                this.beforeIntercepts = owner.getInterceptSettings().getBeforeInterceptors(this.beforeIntercepts, targetClass, targetMethod);
                this.afterIntercepts = owner.getInterceptSettings().getAfterInterceptors(this.afterIntercepts, targetClass, targetMethod);
            }
        }

        String getId() {
            return id;
        }

        List<Class<? extends IInterceptor>> getBeforeIntercepts() {
            return Collections.unmodifiableList(beforeIntercepts);
        }

        List<Class<? extends IInterceptor>> getAfterIntercepts() {
            return Collections.unmodifiableList(afterIntercepts);
        }

        Map<String, String> getContextParams() {
            return Collections.unmodifiableMap(contextParams);
        }

        boolean hasBeforeIntercepts() {
            return !beforeIntercepts.isEmpty();
        }

        boolean hasAfterIntercepts() {
            return !afterIntercepts.isEmpty();
        }
    }
}
