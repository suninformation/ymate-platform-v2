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
import net.ymate.platform.core.support.AbstractContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 拦截器环境上下文对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/19 下午3:26
 */
public class InterceptContext extends AbstractContext {

    private static final ThreadLocal<Map<String, Object>> ATTRIBUTES = ThreadLocal.withInitial(() -> new HashMap<>(16));

    public static Map<String, Object> getLocalAttributes() {
        return ATTRIBUTES.get();
    }

    public static void removeLocalAttributes() {
        ATTRIBUTES.remove();
    }

    private IInterceptor.Direction direction;

    private final Object targetObject;

    private final Method targetMethod;

    private final Object[] methodParams;

    private Object resultObject;

    public InterceptContext(IInterceptor.Direction direction, IApplication owner, Object targetObject, Method targetMethod, Object[] methodParams, Map<String, String> contextParams) {
        super(owner, contextParams);
        //
        this.direction = direction;
        this.targetObject = targetObject;
        this.targetMethod = targetMethod;
        this.methodParams = methodParams;
    }

    /**
     * @return 获取当前拦截器执行方式，Before或After
     */
    public IInterceptor.Direction getDirection() {
        return direction;
    }

    void setDirection(IInterceptor.Direction direction) {
        this.direction = direction;
    }

    /**
     * @return 获取被拦截的目标类型
     */
    public Class<?> getTargetClass() {
        return targetObject.getClass();
    }

    /**
     * @return 获取被拦截的目标对象
     */
    public Object getTargetObject() {
        return targetObject;
    }

    /**
     * @return 获取被代理目标方法对象
     */
    public Method getTargetMethod() {
        return targetMethod;
    }

    /**
     * @return 获取方法参数集合
     */
    public Object[] getMethodParams() {
        return methodParams;
    }

    /**
     * @return 获取返回值对象（一般用于后置拦截器获取当前方法的执行结果）
     */
    public Object getResultObject() {
        return resultObject;
    }

    /**
     * 设置返回值
     *
     * @param resultObject 方法结果对象
     */
    void setResultObject(Object resultObject) {
        this.resultObject = resultObject;
    }
}
