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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.beans.annotation.Order;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyChain;

/**
 * 拦截器代理，支持@Before、@After和@Around方法注解
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/19 下午12:01
 */
@Order(-90000)
public class InterceptProxy implements IProxy {

    @Override
    public Object doProxy(IProxyChain proxyChain) throws Throwable {
        // 若当前目标类为拦截器接口实现类则跳过本次拦截
        if (proxyChain.getTargetObject() instanceof IInterceptor) {
            return proxyChain.doProxyChain();
        }
        // 方法声明了@Ignored注解或非PUBLIC方法和Object类方法将被排除
        boolean ignored = proxyChain.getTargetMethod().isAnnotationPresent(Ignored.class);
        if (ignored || !ClassUtils.isNormalMethod(proxyChain.getTargetMethod())) {
            return proxyChain.doProxyChain();
        }
        try {
            IApplication owner = proxyChain.getProxyFactory().getOwner();
            InterceptMeta interceptMeta = owner.getInterceptSettings().getInterceptMeta(owner, proxyChain.getTargetClass(), proxyChain.getTargetMethod());
            //
            InterceptContext context = interceptMeta.hasBeforeIntercepts() ? buildContext(proxyChain, IInterceptor.Direction.BEFORE) : null;
            if (context != null) {
                for (Class<? extends IInterceptor> interceptClass : interceptMeta.getBeforeIntercepts()) {
                    IInterceptor interceptor = owner.getInterceptSettings().getInterceptorInstance(owner, interceptClass);
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
                    context = buildContext(proxyChain, IInterceptor.Direction.AFTER);
                } else {
                    context.setDirection(IInterceptor.Direction.AFTER);
                }
                // 初始化拦截器上下文对象，并将当前方法的执行结果对象赋予后置拦截器使用
                context.setResultObject(returnValue);
                //
                for (Class<? extends IInterceptor> interceptClass : interceptMeta.getAfterIntercepts()) {
                    IInterceptor interceptor = owner.getInterceptSettings().getInterceptorInstance(owner, interceptClass);
                    // 执行后置拦截器
                    Object afterReturnValue = interceptor.intercept(context);
                    if (afterReturnValue != null) {
                        // 若后置拦截器返回的执行结果不为空则赋值
                        returnValue = afterReturnValue;
                    }
                }
            }
            return returnValue;
        } finally {
            InterceptContext.removeLocalAttributes();
        }
    }

    private InterceptContext buildContext(IProxyChain proxyChain, IInterceptor.Direction direction) {
        IApplication owner = proxyChain.getProxyFactory().getOwner();
        return new InterceptContext(direction, proxyChain.getProxyFactory().getOwner(),
                proxyChain.getTargetObject(),
                proxyChain.getTargetClass(),
                proxyChain.getTargetMethod(),
                proxyChain.getMethodParams(), owner.getInterceptSettings().getContextParams(owner, proxyChain.getTargetClass(), proxyChain.getTargetMethod()));
    }
}
