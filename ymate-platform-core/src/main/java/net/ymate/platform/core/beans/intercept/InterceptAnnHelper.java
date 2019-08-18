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

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 16/1/9 上午12:18
 */
public final class InterceptAnnHelper {

    private final Map<Class<?>, Class<? extends IInterceptor>> interceptAnnotations = new ConcurrentHashMap<>();

    public static void parseContextParamValue(IApplication owner, ContextParam contextParam, Map<String, String> paramsMap) {
        if (contextParam != null) {
            for (ParamItem paramItem : contextParam.value()) {
                String key = paramItem.key();
                String value = paramItem.value();
                boolean flag = value.length() > 1 && value.charAt(0) == '$';
                if (StringUtils.isBlank(key)) {
                    if (flag) {
                        key = value.substring(1);
                        value = StringUtils.trimToEmpty(owner.getParam(key));
                    } else {
                        key = value;
                    }
                } else if (flag) {
                    value = StringUtils.trimToEmpty(owner.getParam(value.substring(1)));
                }
                paramsMap.put(key, value);
            }
        }
    }

    public static Map<String, String> getContextParams(IApplication owner, Class<?> targetClass) {
        Map<String, String> contextParams = new HashMap<>(16);
        parseContextParamValue(owner, targetClass.getAnnotation(ContextParam.class), contextParams);
        return contextParams;
    }

    public static Map<String, String> getContextParams(IApplication owner, Class<?> targetClass, Method targetMethod) {
        Map<String, String> contextParams = new HashMap<>(16);
        //
        parseContextParamValue(owner, (targetClass != null ? targetClass : targetMethod.getDeclaringClass()).getAnnotation(ContextParam.class), contextParams);
        parseContextParamValue(owner, targetMethod.getAnnotation(ContextParam.class), contextParams);
        //
        return contextParams;
    }

    public InterceptAnnHelper() {
    }

    public void registerInterceptAnnotation(Class<? extends Annotation> annotationClass, Class<? extends IInterceptor> interceptClass) {
        if (!annotationClass.equals(Annotation.class) && !annotationClass.equals(InterceptAnnotation.class) && annotationClass.isAnnotationPresent(InterceptAnnotation.class)) {
            ReentrantLockHelper.putIfAbsent(interceptAnnotations, annotationClass, interceptClass);
        }
    }

    private List<Class<? extends IInterceptor>> getAnnotationInterceptorClasses(Class<?>... annotationClasses) {
        List<Class<? extends IInterceptor>> results = new ArrayList<>();
        if (annotationClasses != null && annotationClasses.length > 0) {
            results = Arrays.stream(annotationClasses)
                    .filter(annotationClass -> annotationClass.isAnnotation() && !annotationClass.equals(InterceptAnnotation.class) && annotationClass.isAnnotationPresent(InterceptAnnotation.class))
                    .map(interceptAnnotations::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return results;
    }

    public List<Class<? extends IInterceptor>> getAnnotationInterceptorClasses(Method targetMethod) {
        List<Class<?>> classes = Arrays.stream(targetMethod.getDeclaringClass().getAnnotations())
                .map(Annotation::annotationType).collect(Collectors.toList());
        Arrays.stream(targetMethod.getAnnotations())
                .map(Annotation::annotationType).forEachOrdered(classes::add);
        return getAnnotationInterceptorClasses(classes.toArray(new Class<?>[0]));
    }

    public boolean hasInterceptAnnotationAny(Class<?>... annotationClasses) {
        if (annotationClasses != null && annotationClasses.length > 0) {
            return Arrays.stream(annotationClasses)
                    .filter(annotationClass -> annotationClass.isAnnotation() && !annotationClass.equals(InterceptAnnotation.class) && annotationClass.isAnnotationPresent(InterceptAnnotation.class))
                    .anyMatch(interceptAnnotations::containsKey);
        }
        return false;
    }

    public boolean hasInterceptAnnotationAny(Method targetMethod) {
        boolean flag = hasInterceptAnnotationAny(Arrays.stream(targetMethod.getAnnotations()).map(Annotation::annotationType).distinct().toArray(Class<?>[]::new));
        if (!flag) {
            flag = hasInterceptAnnotationAny(Arrays.stream(targetMethod.getDeclaringClass().getAnnotations()).map(Annotation::annotationType).distinct().toArray(Class<?>[]::new));
        }
        return flag;
    }

    public List<Class<? extends IInterceptor>> getBeforeInterceptors(Class<?> targetClass, Method targetMethod) {
        List<Class<? extends IInterceptor>> interceptorClasses = getAnnotationInterceptorClasses(targetMethod)
                .stream().filter(interceptorClass -> !isInterceptorCleaned(targetMethod, IInterceptor.CleanType.BEFORE, interceptorClass)).collect(Collectors.toList());
        if (targetClass.isAnnotationPresent(Around.class)) {
            parseIntercept(targetMethod, IInterceptor.CleanType.BEFORE, targetClass.getAnnotation(Around.class).value(), interceptorClasses);
        }
        if (targetClass.isAnnotationPresent(Before.class)) {
            parseIntercept(targetMethod, IInterceptor.CleanType.BEFORE, targetClass.getAnnotation(Before.class).value(), interceptorClasses);
        }
        if (targetMethod.isAnnotationPresent(Around.class)) {
            Collections.addAll(interceptorClasses, targetMethod.getAnnotation(Around.class).value());
        }
        if (targetMethod.isAnnotationPresent(Before.class)) {
            Collections.addAll(interceptorClasses, targetMethod.getAnnotation(Before.class).value());
        }
        return interceptorClasses;
    }

    public List<Class<? extends IInterceptor>> getAfterInterceptors(Class<?> targetClass, Method targetMethod) {
        List<Class<? extends IInterceptor>> interceptorClasses = getAnnotationInterceptorClasses(targetMethod)
                .stream().filter(interceptorClass -> !isInterceptorCleaned(targetMethod, IInterceptor.CleanType.AFTER, interceptorClass)).collect(Collectors.toList());
        if (targetClass.isAnnotationPresent(Around.class)) {
            parseIntercept(targetMethod, IInterceptor.CleanType.AFTER, targetClass.getAnnotation(Around.class).value(), interceptorClasses);
        }
        if (targetClass.isAnnotationPresent(After.class)) {
            parseIntercept(targetMethod, IInterceptor.CleanType.AFTER, targetClass.getAnnotation(After.class).value(), interceptorClasses);
        }
        if (targetMethod.isAnnotationPresent(Around.class)) {
            Collections.addAll(interceptorClasses, targetMethod.getAnnotation(Around.class).value());
        }
        if (targetMethod.isAnnotationPresent(After.class)) {
            Collections.addAll(interceptorClasses, targetMethod.getAnnotation(After.class).value());
        }
        return interceptorClasses;
    }

    public Clean getInterceptorClean(Method targetMethod) {
        if (targetMethod.isAnnotationPresent(Clean.class)) {
            return targetMethod.getAnnotation(Clean.class);
        }
        return null;
    }

    private boolean isInterceptorCleaned(Method targetMethod, IInterceptor.CleanType cleanType, Class<? extends IInterceptor> interceptorClass) {
        Clean cleanAnn = getInterceptorClean(targetMethod);
        if (cleanAnn != null &&
                (cleanAnn.type().equals(IInterceptor.CleanType.ALL) || cleanAnn.type().equals(cleanType))) {
            if (cleanAnn.value().length > 0) {
                return ArrayUtils.contains(cleanAnn.value(), interceptorClass);
            }
        }
        return false;
    }

    private void parseIntercept(Method targetMethod, IInterceptor.CleanType cleanType, Class<? extends IInterceptor>[] interceptors, List<Class<? extends IInterceptor>> results) {
        for (Class<? extends IInterceptor> interceptorClass : interceptors) {
            if (!isInterceptorCleaned(targetMethod, cleanType, interceptorClass)) {
                results.add(interceptorClass);
            }
        }
    }
}
