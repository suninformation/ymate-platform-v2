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
import net.ymate.platform.core.beans.annotation.ContextParam;
import net.ymate.platform.core.beans.annotation.InterceptAnnotation;
import net.ymate.platform.core.beans.annotation.ParamItem;
import net.ymate.platform.core.configuration.IConfigReader;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private static final String DISABLED = "disabled";

    private static final Map<String, InterceptMeta> INTERCEPT_META_MAP = new ConcurrentHashMap<>();

    private static final Map<String, Map<String, String>> CONTEXT_PARAMS = new ConcurrentHashMap<>();

    private boolean enabled;

    private final Map<Class<?>, Class<? extends IInterceptor>> interceptAnnotations = new HashMap<>();

    /**
     * 全局禁用拦截器集合
     */
    private final Set<Class<? extends IInterceptor>> globals = new HashSet<>();

    /**
     * 新增包级拦截器配置映射
     */
    private final Map<String, InterceptPackageMeta> packageMetaMap = new HashMap<>();

    /**
     * 拦截器配置规则映射
     */
    private final Map<String, InterceptSettingMeta> settingMetaMap = new HashMap<>();

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
                    if (StringUtils.equalsIgnoreCase(value, DISABLED)) {
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private void parseParamItemValue(IApplication owner, ParamItem paramItemAnn, Map<String, String> contextParams) {
        if (paramItemAnn != null) {
            String key = paramItemAnn.key();
            String value = paramItemAnn.value();
            if (StringUtils.isNotBlank(value)) {
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
                if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
                    contextParams.put(key, value);
                }
            }
        }
    }

    /**
     * 分析并注册上下文参数注解
     *
     * @param owner           所属容器对象
     * @param contextParamAnn 上下文参数注解
     * @param contextParams   目标参数存储映射
     */
    private void parseContextParamValue(IApplication owner, ContextParam contextParamAnn, Map<String, String> contextParams) {
        if (contextParamAnn != null) {
            Arrays.stream(contextParamAnn.value()).forEach(paramItem -> parseParamItemValue(owner, paramItem, contextParams));
        }
    }

    private void parsePackageContextParams(IApplication owner, Class<?> targetClass, Map<String, String> contextParams) {
        Package targetPackage = targetClass.getPackage();
        if (targetPackage != null) {
            // 优先查找并处理上级包
            Class<?> parentPackage = ClassUtils.findParentPackage(targetClass);
            if (parentPackage != null) {
                parsePackageContextParams(owner, parentPackage, contextParams);
            }
            parseParamItemValue(owner, targetPackage.getAnnotation(ParamItem.class), contextParams);
            parseContextParamValue(owner, targetPackage.getAnnotation(ContextParam.class), contextParams);
        }
    }

    public Map<String, String> getContextParams(IApplication owner, Class<?> targetClass) {
        String id = DigestUtils.md5Hex(targetClass.getName());
        if (CONTEXT_PARAMS.containsKey(id)) {
            return CONTEXT_PARAMS.get(id);
        }
        return CONTEXT_PARAMS.computeIfAbsent(id, i -> {
            Map<String, String> contextParams = new HashMap<>(16);
            parsePackageContextParams(owner, targetClass, contextParams);
            parseContextParamValue(owner, targetClass.getAnnotation(ContextParam.class), contextParams);
            return contextParams.isEmpty() ? Collections.emptyMap() : contextParams;
        });
    }

    public Map<String, String> getContextParams(IApplication owner, Class<?> targetClass, Method targetMethod) {
        Map<String, String> contextParams = new HashMap<>(getContextParams(owner, targetClass));
        //
        parseParamItemValue(owner, targetMethod.getAnnotation(ParamItem.class), contextParams);
        parseContextParamValue(owner, targetMethod.getAnnotation(ContextParam.class), contextParams);
        //
        return contextParams;
    }

    /**
     * 注册拦截器注解
     *
     * @param annotationClass 注解类
     * @param interceptClass  拦截器类
     */
    public void registerInterceptAnnotation(Class<? extends Annotation> annotationClass, Class<? extends IInterceptor> interceptClass) {
        if (!annotationClass.equals(Annotation.class) && !annotationClass.equals(InterceptAnnotation.class) && annotationClass.isAnnotationPresent(InterceptAnnotation.class)) {
            interceptAnnotations.put(annotationClass, interceptClass);
        }
    }

    public Map<Class<?>, Class<? extends IInterceptor>> getInterceptAnnotations() {
        return interceptAnnotations;
    }

    public IInterceptor getInterceptorInstance(IApplication owner, Class<? extends IInterceptor> interceptClass) throws IllegalAccessException, InstantiationException {
        IInterceptor instance = owner.getBeanFactory().getBean(interceptClass);
        return instance != null ? instance : interceptClass.newInstance();
    }

    public InterceptMeta getInterceptMeta(IApplication owner, Class<?> targetClass, Method targetMethod) {
        String id = DigestUtils.md5Hex(targetClass.toString() + targetMethod.toString());
        if (INTERCEPT_META_MAP.containsKey(id)) {
            return INTERCEPT_META_MAP.get(id);
        }
        return INTERCEPT_META_MAP.computeIfAbsent(id, i -> {
            InterceptMeta interceptMeta = new InterceptMeta(i, owner, targetClass, targetMethod);
            return interceptMeta.hasBeforeIntercepts() || interceptMeta.hasAfterIntercepts() ? interceptMeta : InterceptMeta.DEFAULT;
        });
    }

    public void registerInterceptPackage(String packageName, InterceptSetting... settings) {
        if (StringUtils.isNotBlank(packageName) && ArrayUtils.isNotEmpty(settings)) {
            InterceptPackageMeta packageMeta = packageMetaMap.computeIfAbsent(packageName, i -> new InterceptPackageMeta(packageName));
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
            InterceptSettingMeta settingMeta = new InterceptSettingMeta(nameArr[0]);
            if (nameArr.length > 1) {
                settingMeta.methodName = nameArr[1];
            }
            for (InterceptSetting setting : settings) {
                if (setting.isValid()) {
                    switch (setting.getType()) {
                        case CLEAN_ALL:
                            // 表示当前类或方法上的所有拦截器禁用, 则后续规则中只处理增加拦截器逻辑
                            settingMeta.cleanAll = true;
                            settingMeta.cleanBeforeIntercepts.clear();
                            settingMeta.cleanAfterIntercepts.clear();
                            break;
                        case CLEAN_BEFORE:
                            settingMeta.cleanBeforeAll = true;
                            settingMeta.cleanBeforeIntercepts.clear();
                            break;
                        case CLEAN_AFTER:
                            settingMeta.cleanAfterAll = true;
                            settingMeta.cleanAfterIntercepts.clear();
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
                            settingMeta.cleanBeforeIntercepts.add(setting.getInterceptorClass().getName());
                            settingMeta.cleanAfterIntercepts.add(setting.getInterceptorClass().getName());
                            break;
                        case REMOVE_BEFORE:
                            // 禁止前置拦截器
                            settingMeta.cleanBeforeIntercepts.add(setting.getInterceptorClass().getName());
                            break;
                        case REMOVE_AFTER:
                            settingMeta.cleanAfterIntercepts.add(setting.getInterceptorClass().getName());
                            break;
                        default:
                    }
                }
            }
            settingMetaMap.put(settingMeta.toString(), settingMeta);
        }
    }

    /**
     * 获取指定类及所有上级包中声明的拦截器(已排除全局禁用拦截器类)
     *
     * @param targetClass 目标类
     * @return 返回拦截器包描述对象
     */
    public InterceptPackageMeta getInterceptPackages(Class<?> targetClass) {
        List<InterceptPackageMeta> packageMetas = new ArrayList<>();
        String packageName = targetClass.getPackage().getName();
        InterceptPackageMeta packageMeta = packageMetaMap.get(packageName);
        if (packageMeta != null) {
            packageMetas.add(0, packageMeta);
        }
        while (StringUtils.contains(packageName, ClassUtils.PACKAGE_SEPARATOR)) {
            packageName = StringUtils.substringBeforeLast(packageName, ClassUtils.PACKAGE_SEPARATOR);
            packageMeta = packageMetaMap.get(packageName);
            if (packageMeta != null) {
                packageMetas.add(0, packageMeta);
            }
        }
        if (packageMetas.isEmpty()) {
            return null;
        }
        InterceptPackageMeta returnValue = new InterceptPackageMeta(targetClass.getPackage().getName());
        packageMetas.forEach(item -> {
            item.getBeforeIntercepts().stream().filter(this::isNotDisabledInterceptor).forEach(returnValue.beforeIntercepts::add);
            item.getAfterIntercepts().stream().filter(this::isNotDisabledInterceptor).forEach(returnValue.afterIntercepts::add);
        });
        return returnValue;
    }

    public InterceptSettingMeta getInterceptSettings(Class<?> targetClass, Method targetMethod) {
        InterceptSettingMeta settingMeta = new InterceptSettingMeta(targetClass.getName());
        settingMeta.merge(settingMetaMap.get(settingMeta.toString()));
        if (targetMethod != null) {
            settingMeta.methodName = targetMethod.getName();
            settingMeta.merge(settingMetaMap.get(settingMeta.toString()));
        }
        return settingMeta;
    }

    public boolean isNotDisabledInterceptor(Class<? extends IInterceptor> interceptorClass) {
        return !globals.contains(interceptorClass);
    }

    public boolean hasInterceptPackages(Class<?> targetClass) {
        String packageName = targetClass.getPackage().getName();
        if (packageMetaMap.containsKey(packageName)) {
            return true;
        }
        while (StringUtils.contains(packageName, ClassUtils.PACKAGE_SEPARATOR)) {
            packageName = StringUtils.substringBeforeLast(packageName, ClassUtils.PACKAGE_SEPARATOR);
            if (packageMetaMap.containsKey(packageName)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasInterceptSettings(Class<?> targetClass, Method targetMethod) {
        String className = targetClass.getName().concat("#");
        return settingMetaMap.containsKey(className) || settingMetaMap.containsKey(className.concat(targetMethod.getName()));
    }

    public static final class InterceptPackageMeta {

        private final String packageName;

        private final Set<Class<? extends IInterceptor>> beforeIntercepts = new LinkedHashSet<>();

        private final Set<Class<? extends IInterceptor>> afterIntercepts = new LinkedHashSet<>();

        public InterceptPackageMeta(String packageName) {
            this.packageName = packageName;
        }

        public String getPackageName() {
            return packageName;
        }

        public Set<Class<? extends IInterceptor>> getBeforeIntercepts() {
            return beforeIntercepts;
        }

        public Set<Class<? extends IInterceptor>> getAfterIntercepts() {
            return afterIntercepts;
        }
    }

    public static final class InterceptSettingMeta {

        private final String className;

        private String methodName;

        private boolean cleanAll;

        private boolean cleanBeforeAll;

        private boolean cleanAfterAll;

        private final Set<String> cleanBeforeIntercepts = new LinkedHashSet<>();

        private final Set<String> cleanAfterIntercepts = new LinkedHashSet<>();

        private final Set<Class<? extends IInterceptor>> beforeIntercepts = new LinkedHashSet<>();

        private final Set<Class<? extends IInterceptor>> afterIntercepts = new LinkedHashSet<>();

        InterceptSettingMeta(String className) {
            if (StringUtils.isBlank(className)) {
                throw new NullArgumentException("className");
            }
            this.className = className + "#";
        }

        void merge(InterceptSettingMeta settingMeta) {
            if (settingMeta != null) {
                cleanAll = cleanAll || settingMeta.isCleanAll();
                if (!cleanAll) {
                    cleanBeforeAll = cleanBeforeAll || settingMeta.isCleanBeforeAll();
                    if (!cleanBeforeAll) {
                        cleanBeforeIntercepts.addAll(settingMeta.getCleanBeforeIntercepts());
                    }
                    cleanAfterAll = cleanAfterAll || settingMeta.isCleanAfterAll();
                    if (!cleanAfterAll) {
                        cleanAfterIntercepts.addAll(settingMeta.getCleanAfterIntercepts());
                    }
                }
                beforeIntercepts.addAll(settingMeta.getBeforeIntercepts());
                afterIntercepts.addAll(settingMeta.getAfterIntercepts());
            }
        }

        public boolean isCleanAll() {
            return cleanAll;
        }

        public boolean isCleanBeforeAll() {
            return cleanBeforeAll;
        }

        public boolean isCleanAfterAll() {
            return cleanAfterAll;
        }

        public Set<String> getCleanBeforeIntercepts() {
            return cleanBeforeIntercepts;
        }

        public Set<String> getCleanAfterIntercepts() {
            return cleanAfterIntercepts;
        }

        public Set<Class<? extends IInterceptor>> getBeforeIntercepts() {
            return beforeIntercepts;
        }

        public Set<Class<? extends IInterceptor>> getAfterIntercepts() {
            return afterIntercepts;
        }

        @Override
        public String toString() {
            return className + StringUtils.trimToEmpty(methodName);
        }
    }
}
