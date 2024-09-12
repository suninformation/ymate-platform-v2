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
import net.ymate.platform.core.beans.annotation.InterceptAnnotation;

import java.lang.annotation.Annotation;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/10/12 上午10:22
 */
public abstract class AbstractInterceptor implements IInterceptor {

    @Override
    public Object intercept(InterceptContext context) throws InterceptException {
        Object result = null;
        if (context.getDirection() == Direction.BEFORE) {
            result = before(context);
        } else if (context.getDirection() == Direction.AFTER) {
            result = after(context);
        }
        return result;
    }

    /**
     * 查找指定类型的拦截器注解
     *
     * @param context         拦截器环境上下文对象
     * @param annotationClass 拦截器注解类
     * @param <T>             拦截器注解类型
     * @return 返回拦截器注解对象
     */
    protected <T extends Annotation> T findInterceptAnnotation(InterceptContext context, Class<T> annotationClass) {
        T annotation = null;
        if (annotationClass.isAnnotationPresent(InterceptAnnotation.class)) {
            annotation = context.getTargetMethod().getAnnotation(annotationClass);
            if (annotation == null) {
                annotation = context.getTargetClass().getAnnotation(annotationClass);
                if (annotation == null) {
                    annotation = ClassUtils.getPackageAnnotation(context.getTargetClass(), annotationClass);
                }
            }
        }
        return annotation;
    }

    /**
     * 执行前置拦截方法
     *
     * @param context 拦截器环境上下文对象
     * @return 返回执行结果
     * @throws InterceptException 执行拦截逻辑可能产生的异常
     */
    protected abstract Object before(InterceptContext context) throws InterceptException;

    /**
     * 执行后置拦截方法
     *
     * @param context 拦截器环境上下文对象
     * @return 返回执行结果
     * @throws InterceptException 执行拦截逻辑可能产生的异常
     */
    protected Object after(InterceptContext context) throws InterceptException {
        return null;
    }
}
