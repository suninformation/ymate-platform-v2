/*
 * Copyright 2007-2020 the original author or authors.
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
import net.ymate.platform.core.beans.annotation.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/02/19 11:08
 * @since 2.1.0
 */
public final class InterceptMeta {

    public static final InterceptMeta DEFAULT = new InterceptMeta("default");

    private final String id;

    private final Set<Class<? extends IInterceptor>> beforeIntercepts = new LinkedHashSet<>();

    private final Set<Class<? extends IInterceptor>> afterIntercepts = new LinkedHashSet<>();

    public InterceptMeta(String id) {
        this.id = id;
    }

    public InterceptMeta(IApplication owner, Class<?> targetClass, Method targetMethod) {
        this(DigestUtils.md5Hex(targetClass.toString() + targetMethod.toString()), owner, targetClass, targetMethod);
    }

    public InterceptMeta(String id, IApplication owner, Class<?> targetClass, Method targetMethod) {
        this(id);
        //
        InterceptSettings interceptSettings = owner.getInterceptSettings();
        InterceptSettings.InterceptPackageMeta packageMeta = interceptSettings.getInterceptPackages(targetClass);
        if (packageMeta != null) {
            beforeIntercepts.addAll(packageMeta.getBeforeIntercepts());
            afterIntercepts.addAll(packageMeta.getAfterIntercepts());
        }
        //
        InterceptSettings.InterceptSettingMeta settingMeta = interceptSettings.getInterceptSettings(targetClass, targetMethod);
        settingMeta.getBeforeIntercepts().stream()
                .filter(interceptSettings::isNotDisabledInterceptor)
                .forEach(beforeIntercepts::add);
        settingMeta.getAfterIntercepts().stream()
                .filter(interceptSettings::isNotDisabledInterceptor)
                .forEach(afterIntercepts::add);
        //
        Clean cleanAnn = targetMethod.getAnnotation(Clean.class);
        processPackage(owner, targetClass, cleanAnn, settingMeta);
        processAnnotations(owner, targetClass.getAnnotations(), cleanAnn, settingMeta);
        processAnnotations(owner, targetMethod.getAnnotations(), cleanAnn, settingMeta);
    }

    private boolean isCleanAll(Clean cleanAnn) {
        return isCleanAll(cleanAnn, IInterceptor.CleanType.ALL);
    }

    private boolean isCleanAll(Clean cleanAnn, IInterceptor.CleanType cleanType) {
        return cleanAnn != null && cleanAnn.value().length == 0 && cleanAnn.type().equals(cleanType);
    }

    private boolean isNotCleanInterceptor(Clean cleanAnn, IInterceptor.CleanType cleanType, Class<? extends IInterceptor> interceptClass, InterceptSettings.InterceptSettingMeta settingMeta) {
        if (isCleanAll(cleanAnn) || isCleanAll(cleanAnn, cleanType)) {
            return false;
        }
        // 检查全局配置
        boolean cleaned = settingMeta != null && settingMeta.isCleanAll();
        if (!cleaned) {
            if (IInterceptor.CleanType.BEFORE.equals(cleanType)) {
                cleaned = settingMeta != null && settingMeta.isCleanBeforeAll() || settingMeta != null && settingMeta.getCleanBeforeIntercepts().contains(interceptClass.getName());
            } else if (IInterceptor.CleanType.AFTER.equals(cleanType)) {
                cleaned = settingMeta != null && settingMeta.isCleanAfterAll() || settingMeta != null && settingMeta.getCleanAfterIntercepts().contains(interceptClass.getName());
            }
        }
        return !cleaned && (cleanAnn == null || !ArrayUtils.contains(cleanAnn.value(), interceptClass));
    }

    private void processAnnotations(IApplication owner, Annotation[] annotations, Clean cleanAnn, InterceptSettings.InterceptSettingMeta settingMeta) {
        if (settingMeta != null && settingMeta.isCleanAll()) {
            // 若全局配置为清理全部则直接返回
            return;
        }
        if (ArrayUtils.isNotEmpty(annotations) && !isCleanAll(cleanAnn)) {
            for (Annotation annotation : annotations) {
                InterceptAnnotation interceptAnnotation = annotation.annotationType().getAnnotation(InterceptAnnotation.class);
                if (interceptAnnotation != null) {
                    Class<? extends IInterceptor> interceptorClass = owner.getInterceptSettings().getInterceptAnnotations().get(annotation.annotationType());
                    if (interceptorClass != null) {
                        if (isNotCleanInterceptor(cleanAnn, IInterceptor.CleanType.BEFORE, interceptorClass, settingMeta)) {
                            if (interceptAnnotation.value().length == 0 || ArrayUtils.contains(interceptAnnotation.value(), IInterceptor.Direction.BEFORE)) {
                                beforeIntercepts.add(interceptorClass);
                            }
                        }
                        if (isNotCleanInterceptor(cleanAnn, IInterceptor.CleanType.AFTER, interceptorClass, settingMeta)) {
                            if (interceptAnnotation.value().length == 0 || ArrayUtils.contains(interceptAnnotation.value(), IInterceptor.Direction.AFTER)) {
                                afterIntercepts.add(interceptorClass);
                            }
                        }
                    }
                } else if (annotation instanceof Around) {
                    Arrays.stream(((Around) annotation).value()).forEach(interceptorClass -> {
                        if (isNotCleanInterceptor(cleanAnn, IInterceptor.CleanType.BEFORE, interceptorClass, settingMeta)) {
                            beforeIntercepts.add(interceptorClass);
                        }
                        if (isNotCleanInterceptor(cleanAnn, IInterceptor.CleanType.AFTER, interceptorClass, settingMeta)) {
                            afterIntercepts.add(interceptorClass);
                        }
                    });
                } else if (annotation instanceof Before) {
                    Arrays.stream(((Before) annotation).value())
                            .filter(interceptorClass -> isNotCleanInterceptor(cleanAnn, IInterceptor.CleanType.BEFORE, interceptorClass, settingMeta))
                            .forEach(beforeIntercepts::add);
                } else if (annotation instanceof After) {
                    Arrays.stream(((After) annotation).value())
                            .filter(interceptorClass -> isNotCleanInterceptor(cleanAnn, IInterceptor.CleanType.AFTER, interceptorClass, settingMeta))
                            .forEach(afterIntercepts::add);
                }
            }
        }
    }

    /**
     * 从目标类所在包开始向上层遍历
     *
     * @param owner       所属容器对象
     * @param targetClass 目标类
     */
    private void processPackage(IApplication owner, Class<?> targetClass, Clean cleanAnn, InterceptSettings.InterceptSettingMeta settingMeta) {
        if (settingMeta != null && settingMeta.isCleanAll()) {
            return;
        }
        if (!isCleanAll(cleanAnn)) {
            Package targetPackage = targetClass.getPackage();
            if (targetPackage != null) {
                // 优先查找并处理上级包
                Class<?> parentPackage = ClassUtils.findParentPackage(targetClass);
                if (parentPackage != null) {
                    processPackage(owner, parentPackage, cleanAnn, settingMeta);
                }
                // 分析当前包中拦截器相关注解
                processAnnotations(owner, targetPackage.getAnnotations(), cleanAnn, settingMeta);
            }
        }
    }

    public String getId() {
        return id;
    }

    public Set<Class<? extends IInterceptor>> getBeforeIntercepts() {
        return beforeIntercepts;
    }

    public Set<Class<? extends IInterceptor>> getAfterIntercepts() {
        return afterIntercepts;
    }

    boolean hasBeforeIntercepts() {
        return !beforeIntercepts.isEmpty();
    }

    boolean hasAfterIntercepts() {
        return !afterIntercepts.isEmpty();
    }
}
