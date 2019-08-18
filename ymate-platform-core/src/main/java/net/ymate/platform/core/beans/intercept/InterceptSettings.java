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
import net.ymate.platform.core.beans.annotation.After;
import net.ymate.platform.core.beans.annotation.Around;
import net.ymate.platform.core.beans.annotation.Before;
import net.ymate.platform.core.beans.annotation.ContextParam;
import net.ymate.platform.core.configuration.IConfigReader;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 拦截器全局规则设置
 *
 * @author 刘镇 (suninformation@163.com) on 17/4/9 上午12:18
 */
public final class InterceptSettings {

    private static final Log LOG = LogFactory.getLog(InterceptSettings.class);

    private static final String SETTINGS_ENABLED = "settings_enabled";

    private static final String PACKAGES_PREFIX = "packages.";

    private static final String GLOBALS_PREFIX = "globals.";

    private static final String SETTINGS_PREFIX = "settings.";

    private boolean enabled;

    private final Set<Class<? extends IInterceptor>> globals = new HashSet<>();

    private final Map<String, InterceptPackageMeta> packageMetaMap = new HashMap<>();

    private final Map<String, InterceptSettingMeta> settingMetaMap = new HashMap<>();

    private final InterceptAnnHelper interceptAnnHelper = new InterceptAnnHelper();

    public static InterceptSettings create() {
        return new InterceptSettings();
    }

    public static InterceptSettings create(IConfigReader configReader) {
        InterceptSettings interceptSettings = new InterceptSettings();
        if (configReader != null && !configReader.toMap().isEmpty()) {
            interceptSettings.setEnabled(configReader.getBoolean(SETTINGS_ENABLED));
            if (interceptSettings.isEnabled()) {
                configReader.getMap(PACKAGES_PREFIX).forEach((key, value) -> interceptSettings.registerInterceptPackage(key, InterceptSetting.create(StringUtils.split(value, "|"))));
                configReader.getMap(SETTINGS_PREFIX).forEach((key, value) -> interceptSettings.registerInterceptSettings(key, InterceptSetting.create(StringUtils.split(value, "|"))));
                configReader.getMap(GLOBALS_PREFIX).forEach((key, value) -> {
                    if (StringUtils.equalsIgnoreCase(value, "disabled")) {
                        interceptSettings.registerInterceptGlobal(StringUtils.substringBefore(key, "#"));
                    }
                });
            }
        }
        return interceptSettings;
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends IInterceptor> loadInterceptorClass(String className) {
        try {
            Class<?> clazz = ClassUtils.loadClass(className, InterceptSettings.class);
            if (ClassUtils.isInterfaceOf(clazz, IInterceptor.class)) {
                return (Class<? extends IInterceptor>) clazz;
            }
        } catch (ClassNotFoundException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("Interceptor class not found: %s", className));
            }
        }
        return null;
    }

    private InterceptSettings() {
    }

    public InterceptAnnHelper getInterceptAnnHelper() {
        return interceptAnnHelper;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void registerInterceptPackage(Class<?> targetClass) {
        if (targetClass != null) {
            Package classPackage = targetClass.getPackage();
            if (classPackage != null) {
                Around aroundAnn = classPackage.getAnnotation(Around.class);
                Before beforeAnn = classPackage.getAnnotation(Before.class);
                After afterAnn = classPackage.getAnnotation(After.class);
                if (aroundAnn != null || beforeAnn != null || afterAnn != null) {
                    InterceptPackageMeta packageMeta = packageMetaMap.get(classPackage.getName());
                    if (packageMeta == null) {
                        packageMeta = new InterceptPackageMeta(classPackage.getName());
                        packageMetaMap.put(classPackage.getName(), packageMeta);
                    }
                    if (aroundAnn != null && aroundAnn.value().length > 0) {
                        List<Class<? extends IInterceptor>> intercepts = Arrays.asList(aroundAnn.value());
                        packageMeta.beforeIntercepts.addAll(intercepts);
                        packageMeta.afterIntercepts.addAll(intercepts);
                    }
                    if (beforeAnn != null && beforeAnn.value().length > 0) {
                        packageMeta.beforeIntercepts.addAll(Arrays.asList(beforeAnn.value()));
                    }
                    if (afterAnn != null && afterAnn.value().length > 0) {
                        packageMeta.afterIntercepts.addAll(Arrays.asList(afterAnn.value()));
                    }
                    //
                    ContextParam contextParamAnn = classPackage.getAnnotation(ContextParam.class);
                    if (contextParamAnn != null) {
                        packageMeta.contextParams.add(contextParamAnn);
                    }
                }
            }
        }
    }

    public void registerInterceptPackage(String packageName, InterceptSetting... settings) {
        if (StringUtils.isNotBlank(packageName) && ArrayUtils.isNotEmpty(settings)) {
            InterceptPackageMeta packageMeta = packageMetaMap.get(packageName);
            if (packageMeta == null) {
                packageMeta = new InterceptPackageMeta(packageName);
                packageMetaMap.put(packageName, packageMeta);
            }
            for (InterceptSetting setting : settings) {
                if (setting.isValid()) {
                    switch (setting.getType()) {
                        case ADD_ALL:
                            packageMeta.beforeIntercepts.add(setting.getInterceptorClass());
                            packageMeta.afterIntercepts.add(setting.getInterceptorClass());
                            break;
                        case ADD_BEFORE:
                            packageMeta.beforeIntercepts.add(setting.getInterceptorClass());
                            break;
                        case ADD_AFTER:
                            packageMeta.afterIntercepts.add(setting.getInterceptorClass());
                            break;
                        default:
                    }
                }
            }
        }
    }

    public void registerInterceptGlobal(String className) {
        Class<? extends IInterceptor> clazz = loadInterceptorClass(className);
        if (clazz != null) {
            globals.add(clazz);
        }
    }

    public void registerInterceptSettings(String name, InterceptSetting... settings) {
        String[] nameArr = StringUtils.split(name, "#");
        if (nameArr != null && ArrayUtils.isNotEmpty(settings)) {
            InterceptSettingMeta settingMeta = new InterceptSettingMeta();
            settingMeta.className = nameArr[0];
            if (nameArr.length > 1) {
                settingMeta.methodName = nameArr[1];
            }
            for (InterceptSetting setting : settings) {
                if (setting.isValid()) {
                    switch (setting.getType()) {
                        case CLEAN_ALL:
                            // 表示当前类或方法上的所有拦截器禁用, 则后续规则中只处理增加拦截器逻辑
                            settingMeta.cleanAll = true;
                            settingMeta.beforeCleanIntercepts.clear();
                            settingMeta.afterCleanIntercepts.clear();
                            break;
                        case CLEAN_BEFORE:
                            settingMeta.beforeCleanAll = true;
                            settingMeta.beforeCleanIntercepts.clear();
                            break;
                        case CLEAN_AFTER:
                            settingMeta.afterCleanAll = true;
                            settingMeta.afterCleanIntercepts.clear();
                            break;
                        case ADD_ALL:
                            // 增加新的前置和后置拦截器
                            settingMeta.beforeIntercepts.add(setting.getInterceptorClass());
                            settingMeta.afterIntercepts.add(setting.getInterceptorClass());
                            break;
                        case ADD_BEFORE:
                            // 增加新的前置拦截器
                            settingMeta.beforeIntercepts.add(setting.getInterceptorClass());
                            break;
                        case ADD_AFTER:
                            // 增加新的后置拦截器
                            settingMeta.afterIntercepts.add(setting.getInterceptorClass());
                            break;
                        case REMOVE_ALL:
                            // 禁止前置和后置拦截器
                            settingMeta.beforeCleanIntercepts.add(setting.getInterceptorClass().getName());
                            settingMeta.afterCleanIntercepts.add(setting.getInterceptorClass().getName());
                            break;
                        case REMOVE_BEFORE:
                            // 禁止前置拦截器
                            settingMeta.beforeCleanIntercepts.add(setting.getInterceptorClass().getName());
                            break;
                        case REMOVE_AFTER:
                            settingMeta.afterCleanIntercepts.add(setting.getInterceptorClass().getName());
                            break;
                        default:
                    }
                }
            }
            settingMetaMap.put(settingMeta.toString(), settingMeta);
        }
    }

    public List<InterceptPackageMeta> getInterceptPackages(Class<?> targetClass) {
        List<InterceptPackageMeta> interceptPackageMetas = new ArrayList<>();
        String packageName = targetClass.getPackage().getName();
        if (packageMetaMap.containsKey(packageName)) {
            interceptPackageMetas.add(0, packageMetaMap.get(packageName));
        }
        while (StringUtils.contains(packageName, ".")) {
            packageName = StringUtils.substringBeforeLast(packageName, ".");
            if (packageMetaMap.containsKey(packageName)) {
                interceptPackageMetas.add(0, packageMetaMap.get(packageName));
            }
        }
        return interceptPackageMetas;
    }

    public boolean hasInterceptPackages(Class<?> targetClass) {
        String packageName = targetClass.getPackage().getName();
        if (packageMetaMap.containsKey(packageName)) {
            return true;
        }
        while (StringUtils.contains(packageName, ".")) {
            packageName = StringUtils.substringBeforeLast(packageName, ".");
            if (packageMetaMap.containsKey(packageName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasInterceptSettings(Class<?> targetClass, Method targetMethod) {
        String className = targetClass.getName().concat("#");
        String methodName = className.concat(targetMethod.getName());
        return settingMetaMap.containsKey(className) || settingMetaMap.containsKey(methodName);
    }

    private void interceptSettingFilter(boolean before, InterceptSettingMeta settingMeta, List<Class<? extends IInterceptor>> interceptors, List<Class<? extends IInterceptor>> results) {
        // 若有新增的前置拦截器先添加到集合中
        List<Class<? extends IInterceptor>> targetInterceptors = before ? settingMeta.getBeforeIntercepts() : settingMeta.getAfterIntercepts();
        if (!targetInterceptors.isEmpty()) {
            (before ? settingMeta.getBeforeIntercepts() : settingMeta.getAfterIntercepts()).stream().filter((interceptorClass) -> (!globals.contains(interceptorClass) && !results.contains(interceptorClass) && !settingMeta.beforeCleanIntercepts.contains(interceptorClass.getName()))).forEachOrdered(results::add);
        }
        // 判断并尝试过滤前置拦截器
        if (!settingMeta.isCleanAll() && (before ? !settingMeta.isBeforeCleanAll() : !settingMeta.isAfterCleanAll())) {
            interceptors.forEach((interceptorClass) -> {
                boolean flag = before ? !settingMeta.beforeCleanIntercepts.contains(interceptorClass.getName()) : !settingMeta.afterCleanIntercepts.contains(interceptorClass.getName());
                if (!globals.contains(interceptorClass) && !results.contains(interceptorClass) && flag) {
                    results.add(interceptorClass);
                }
            });
        }
    }

    private List<Class<? extends IInterceptor>> getInterceptors(boolean before, List<Class<? extends IInterceptor>> classes, Class<?> targetClass, Method targetMethod) {
        List<Class<? extends IInterceptor>> interceptorClasses = new ArrayList<>();
        boolean flag = false;
        //
        String className = targetClass.getName().concat("#");
        String methodName = className.concat(targetMethod.getName());
        //
        InterceptSettingMeta settingMeta = settingMetaMap.get(className);
        if (settingMeta != null) {
            flag = true;
            interceptSettingFilter(before, settingMeta, classes, interceptorClasses);
        }
        settingMeta = settingMetaMap.get(methodName);
        if (settingMeta != null) {
            flag = true;
            interceptSettingFilter(before, settingMeta, classes, interceptorClasses);
        }
        return flag ? interceptorClasses : classes;
    }

    public List<Class<? extends IInterceptor>> getBeforeInterceptors(List<Class<? extends IInterceptor>> classes, Class<?> targetClass, Method targetMethod) {
        return getInterceptors(true, classes, targetClass, targetMethod);
    }

    public List<Class<? extends IInterceptor>> getAfterInterceptors(List<Class<? extends IInterceptor>> classes, Class<?> targetClass, Method targetMethod) {
        return getInterceptors(false, classes, targetClass, targetMethod);
    }

    public static final class InterceptPackageMeta {

        private final String packageName;

        private List<Class<? extends IInterceptor>> beforeIntercepts = new ArrayList<>();

        private List<Class<? extends IInterceptor>> afterIntercepts = new ArrayList<>();

        private final List<ContextParam> contextParams = new ArrayList<>();

        public InterceptPackageMeta(String packageName) {
            this.packageName = packageName;
        }

        public String getPackageName() {
            return packageName;
        }

        public List<Class<? extends IInterceptor>> getBeforeIntercepts() {
            return Collections.unmodifiableList(beforeIntercepts);
        }

        public List<Class<? extends IInterceptor>> getAfterIntercepts() {
            return Collections.unmodifiableList(afterIntercepts);
        }

        public List<ContextParam> getContextParams() {
            return Collections.unmodifiableList(contextParams);
        }
    }

    public static final class InterceptSettingMeta {

        private String className;

        private String methodName;

        private boolean cleanAll;

        private boolean beforeCleanAll;

        private boolean afterCleanAll;

        private List<String> beforeCleanIntercepts = new ArrayList<>();

        private List<String> afterCleanIntercepts = new ArrayList<>();

        private List<Class<? extends IInterceptor>> beforeIntercepts = new ArrayList<>();

        private List<Class<? extends IInterceptor>> afterIntercepts = new ArrayList<>();

        public InterceptSettingMeta() {
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public boolean isCleanAll() {
            return cleanAll;
        }

        public boolean isBeforeCleanAll() {
            return beforeCleanAll;
        }

        public boolean isAfterCleanAll() {
            return afterCleanAll;
        }

        public List<String> getBeforeCleanIntercepts() {
            return Collections.unmodifiableList(beforeCleanIntercepts);
        }

        public List<String> getAfterCleanIntercepts() {
            return Collections.unmodifiableList(afterCleanIntercepts);
        }

        public List<Class<? extends IInterceptor>> getBeforeIntercepts() {
            return Collections.unmodifiableList(beforeIntercepts);
        }

        public List<Class<? extends IInterceptor>> getAfterIntercepts() {
            return Collections.unmodifiableList(afterIntercepts);
        }

        @Override
        public String toString() {
            return className.concat("#").concat(StringUtils.trimToEmpty(methodName));
        }
    }
}
