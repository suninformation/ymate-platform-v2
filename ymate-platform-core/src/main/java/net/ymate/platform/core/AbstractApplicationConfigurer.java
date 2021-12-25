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
package net.ymate.platform.core;

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.intercept.InterceptSettings;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.i18n.II18nEventHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-07 16:01
 */
public abstract class AbstractApplicationConfigurer implements IApplicationConfigurer {

    private IApplication.Environment runEnv;

    private IProxyFactory proxyFactory;

    private IBeanLoadFactory beanLoadFactory;

    private final IApplicationConfigureParser configureParser;

    private IPasswordProcessor passwordProcessor;

    private Locale defaultLocale;

    private II18nEventHandler i18nEventHandler;

    private InterceptSettings interceptSettings;

    private final Set<String> packageNames = new HashSet<>();

    private final Set<String> excludedPackageNames = new HashSet<>();

    private final Set<String> excludedFiles = new HashSet<>();

    private final Set<String> excludedModules = new HashSet<>();

    private final Set<String> includedModules = new HashSet<>();

    private final Map<String, String> parameters = new HashMap<>();

    public AbstractApplicationConfigurer(IApplicationConfigureParser configureParser) {
        this.configureParser = configureParser;
    }

    @Override
    public IApplication.Environment getRunEnv() {
        return runEnv;
    }

    public void setRunEnv(IApplication.Environment runEnv) {
        this.runEnv = runEnv;
    }

    @Override
    public IProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public void setProxyFactory(IProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    @Override
    public IBeanLoadFactory getBeanLoadFactory() {
        return beanLoadFactory;
    }

    public void setBeanLoadFactory(IBeanLoadFactory beanLoadFactory) {
        this.beanLoadFactory = beanLoadFactory;
    }

    @Override
    public IApplicationConfigureParser getConfigureParser() {
        return configureParser;
    }

    @Override
    public IPasswordProcessor getPasswordProcessor() {
        return passwordProcessor;
    }

    public void setPasswordProcessor(IPasswordProcessor passwordProcessor) {
        this.passwordProcessor = passwordProcessor;
    }

    @Override
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    @Override
    public II18nEventHandler getI18nEventHandler() {
        return i18nEventHandler;
    }

    @Override
    public InterceptSettings getInterceptSettings() {
        return interceptSettings;
    }

    public void setInterceptSettings(InterceptSettings interceptSettings) {
        this.interceptSettings = interceptSettings;
    }

    public void setI18nEventHandler(II18nEventHandler i18nEventHandler) {
        this.i18nEventHandler = i18nEventHandler;
    }

    @Override
    public Set<String> getPackageNames() {
        return Collections.unmodifiableSet(packageNames);
    }

    @Override
    public Set<String> getExcludedPackageNames() {
        return Collections.unmodifiableSet(excludedPackageNames);
    }

    @Override
    public Set<String> getExcludedFiles() {
        return Collections.unmodifiableSet(excludedFiles);
    }

    @Override
    public Set<String> getExcludedModules() {
        return Collections.unmodifiableSet(excludedModules);
    }

    @Override
    public Set<String> getIncludedModules() {
        return Collections.unmodifiableSet(includedModules);
    }

    public void addPackageNames(List<String> packageNames) {
        packageNames.stream().filter(packageName -> !this.packageNames.contains(packageName)).forEach(this.packageNames::add);
    }

    public void addExcludedPackageNames(List<String> excludedPackageNames) {
        excludedPackageNames.stream().filter(packageName -> !this.excludedPackageNames.contains(packageName)).forEach(this.excludedPackageNames::add);
    }

    public void addExcludedFiles(List<String> excludedFiles) {
        excludedFiles.stream().filter(file -> !this.excludedFiles.contains(file)).forEach(this.excludedFiles::add);
    }

    public void addExcludedModules(List<String> excludedModules) {
        excludedModules.stream().filter(module -> !this.excludedModules.contains(module)).forEach(this.excludedModules::add);
    }

    public void addIncludedModules(List<String> includedModules) {
        includedModules.stream().filter(module -> !this.includedModules.contains(module)).forEach(this.includedModules::add);
    }

    @Override
    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void addParameter(String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            parameters.put(key, value);
        }
    }

    public void addParameters(Map<String, String> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            this.parameters.putAll(parameters);
        }
    }
}
