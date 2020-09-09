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
package net.ymate.platform.core.impl;

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.*;
import net.ymate.platform.core.annotation.*;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.intercept.InterceptSettings;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.configuration.impl.MapSafeConfigReader;
import net.ymate.platform.core.i18n.II18nEventHandler;
import net.ymate.platform.core.module.IModuleConfigurer;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-07 20:41
 * @since 2.1.0
 */
public final class DefaultApplicationConfigurer extends AbstractApplicationConfigurer {

    private static final String CONFIG_DEV_MODE = "ymp.dev_mode";

    private static final String CONFIG_EXCLUDED_PACKAGES = "ymp.excluded_packages";

    private static final String CONFIG_EXCLUDED_FILES = "ymp.excluded_files";

    private static final String CONFIG_EXCLUDED_NODULES = "ymp.excluded_modules";

    private static final String CONFIG_INCLUDED_NODULES = "ymp.included_modules";

    private static final String CONFIG_PARAMS_PREFIX = "ymp.params.";

    private static final String CONFIG_DEFAULT_LOCALE = "ymp.default_locale";

    private static final String CONFIG_DEFAULT_PASSWORD_PROCESS_CLASS = "ymp.default_password_process_class";

    private static final String CONFIG_I18N_EVENT_HANDLER_CLASS = "ymp.i18n_event_handler_class";

    private static final String CONFIG_INTERCEPT_PREFIX = "ymp.intercept.";

    public DefaultApplicationConfigurer(IApplicationConfigureFactory configureFactory) {
        this(configureFactory, DefaultApplicationConfigureParser.systemDefault());
    }

    public DefaultApplicationConfigurer(IApplicationConfigureFactory configureFactory, IApplicationConfigureParser configureParser) {
        super(configureParser);
        if (configureParser != null) {
            IApplication.Environment runEnv = configureParser.getConfigReader().getBoolean(CONFIG_DEV_MODE) ? IApplication.Environment.DEV : null;
            IProxyFactory proxyFactory = YMP.getProxyFactory();
            IBeanLoadFactory beanLoadFactory = YMP.getBeanLoadFactory();
            IPasswordProcessor passwordProcessor = configureParser.getConfigReader().getClassImpl(CONFIG_DEFAULT_PASSWORD_PROCESS_CLASS, IPasswordProcessor.class);
            Locale defaultLocale = LocaleUtils.toLocale(StringUtils.trimToNull(configureParser.getConfigReader().getString(CONFIG_DEFAULT_LOCALE)));
            II18nEventHandler i18nEventHandler = configureParser.getConfigReader().getClassImpl(CONFIG_I18N_EVENT_HANDLER_CLASS, II18nEventHandler.class);
            //
            List<String> packageNames = parseArrayValue(IApplication.SYSTEM_PACKAGES, IApplication.SYSTEM_PACKAGES);
            List<String> excludedPackageNames = parseArrayValue(CONFIG_EXCLUDED_PACKAGES);
            List<String> excludedFiles = parseArrayValue(CONFIG_EXCLUDED_FILES);
            List<String> excludedModules = parseArrayValue(CONFIG_EXCLUDED_NODULES);
            List<String> includedModules = parseArrayValue(CONFIG_INCLUDED_NODULES);
            Map<String, String> parameterMap = configureParser.getConfigReader().getMap(CONFIG_PARAMS_PREFIX);
            if (configureFactory.getMainClass() != null) {
                if (runEnv == null && configureFactory.getMainClass().isAnnotationPresent(EnableDevMode.class)) {
                    runEnv = IApplication.Environment.DEV;
                }
                // 处理代理工厂配置注解类
                if (proxyFactory == null) {
                    EnableBeanProxy enableBeanProxyAnn = configureFactory.getMainClass().getAnnotation(EnableBeanProxy.class);
                    if (enableBeanProxyAnn != null && !enableBeanProxyAnn.factoryClass().equals(IProxyFactory.class)) {
                        proxyFactory = ClassUtils.impl(enableBeanProxyAnn.factoryClass(), IProxyFactory.class);
                    }
                }
                // 处理自定义加载器配置注解类
                EnableAutoScan enableAutoScanAnn = configureFactory.getMainClass().getAnnotation(EnableAutoScan.class);
                if (enableAutoScanAnn != null) {
                    if (beanLoadFactory == null && !enableAutoScanAnn.factoryClass().equals(IBeanLoadFactory.class)) {
                        beanLoadFactory = ClassUtils.impl(enableAutoScanAnn.factoryClass(), IBeanLoadFactory.class);
                    }
                    if (packageNames.isEmpty()) {
                        packageNames.add(configureFactory.getMainClass().getPackage().getName());
                        packageNames.addAll(Arrays.asList(enableAutoScanAnn.value()));
                    }
                    if (excludedPackageNames.isEmpty()) {
                        excludedPackageNames.addAll(Arrays.asList(enableAutoScanAnn.excluded()));
                    }
                    if (excludedFiles.isEmpty()) {
                        excludedFiles.addAll(Arrays.asList(enableAutoScanAnn.excludedFiles()));
                    }
                    if (excludedModules.isEmpty()) {
                        excludedModules.addAll(Arrays.asList(enableAutoScanAnn.excludedModules()));
                    }
                    if (includedModules.isEmpty()) {
                        includedModules.addAll(Arrays.asList(enableAutoScanAnn.includedModules()));
                    }
                }
                // 处理默认密码处理器配置注解类
                if (passwordProcessor == null) {
                    DefaultPasswordProcessClass passwordProcessClass = configureFactory.getMainClass().getAnnotation(DefaultPasswordProcessClass.class);
                    if (passwordProcessClass != null && !passwordProcessClass.value().equals(IPasswordProcessor.class)) {
                        passwordProcessor = ClassUtils.impl(passwordProcessClass.value(), IPasswordProcessor.class);
                    }
                }
                // 处理国际化配置注解类
                I18nConf i18nConfAnn = configureFactory.getMainClass().getAnnotation(I18nConf.class);
                if (i18nConfAnn != null) {
                    if (defaultLocale == null) {
                        defaultLocale = LocaleUtils.toLocale(i18nConfAnn.defaultLocale());
                    }
                    if (i18nEventHandler == null && !II18nEventHandler.class.equals(i18nConfAnn.eventHandlerClass())) {
                        i18nEventHandler = ClassUtils.impl(i18nConfAnn.eventHandlerClass(), II18nEventHandler.class);
                    }
                }
                if (parameterMap.isEmpty()) {
                    // 处理自定义参数配置注解类
                    Params params = configureFactory.getMainClass().getAnnotation(Params.class);
                    if (params != null) {
                        Arrays.stream(params.value()).forEachOrdered(param -> parameterMap.put(param.name(), StringUtils.join(param.value(), '|')));
                    } else {
                        Param param = configureFactory.getMainClass().getAnnotation(Param.class);
                        if (param != null) {
                            parameterMap.put(param.name(), StringUtils.join(param.value(), '|'));
                        }
                    }
                }
            }
            setRunEnv(YMP.getPriorityRunEnv(runEnv));
            setProxyFactory(proxyFactory);
            setBeanLoadFactory(beanLoadFactory);
            setPasswordProcessor(passwordProcessor);
            //
            setDefaultLocale(defaultLocale);
            setI18nEventHandler(i18nEventHandler);
            //
            addPackageNames(packageNames);
            addExcludedPackageNames(excludedPackageNames);
            addExcludedFiles(excludedFiles);
            addExcludedModules(excludedModules);
            addIncludedModules(includedModules);
            //
            setInterceptSettings(InterceptSettings.create(MapSafeConfigReader.bind(configureParser.getConfigReader().getMap(CONFIG_INTERCEPT_PREFIX))));
            //
            addParameters(parameterMap);
        }
    }

    private List<String> parseArrayValue(String configVarName) {
        return parseArrayValue(null, configVarName);
    }

    private List<String> parseArrayValue(String systemVarName, String configVarName) {
        List<String> returnValue = new ArrayList<>();
        String[] packageNames = StringUtils.split(StringUtils.defaultIfBlank(StringUtils.isBlank(systemVarName) ? null : System.getProperty(systemVarName), getConfigureParser() != null ? getConfigureParser().getConfigReader().getString(configVarName) : StringUtils.EMPTY), "|");
        if (packageNames != null && packageNames.length > 0) {
            returnValue.addAll(Arrays.asList(packageNames));
        }
        return returnValue;
    }

    @Override
    public IModuleConfigurer getModuleConfigurer(String moduleName) {
        return getConfigureParser() == null ? null : getConfigureParser().getModuleConfigurer(moduleName);
    }
}
