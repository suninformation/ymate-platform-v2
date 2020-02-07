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
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.i18n.II18nEventHandler;
import net.ymate.platform.core.impl.DefaultApplicationConfigureParser;
import net.ymate.platform.core.module.IModuleConfigurer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-07 17:39
 */
public class ApplicationConfigureBuilder {

    private final AbstractApplicationConfigurer configurer;

    private final Map<String, IModuleConfigurer> moduleConfigurers = new HashMap<>();

    public static ApplicationConfigureBuilder builder() {
        IApplicationConfigureParseFactory configureParseFactory = YMP.getConfigureParseFactory();
        return new ApplicationConfigureBuilder(configureParseFactory != null ? configureParseFactory.getConfigureParser() : null);
    }

    public static ApplicationConfigureBuilder builder(IApplicationConfigureParser configureParser) {
        return new ApplicationConfigureBuilder(configureParser);
    }

    private ApplicationConfigureBuilder(IApplicationConfigureParser configureParser) {
        configurer = new AbstractApplicationConfigurer(configureParser != null ? configureParser : DefaultApplicationConfigureParser.defaultEmpty()) {
            @Override
            public IModuleConfigurer getModuleConfigurer(String moduleName) {
                IModuleConfigurer moduleConfigurer = moduleConfigurers.get(moduleName);
                return moduleConfigurer != null ? moduleConfigurer : getConfigureParser().getModuleConfigurer(moduleName);
            }
        };
    }

    public ApplicationConfigureBuilder runEnv(IApplication.Environment runEnv) {
        configurer.setRunEnv(runEnv);
        return this;
    }

    public ApplicationConfigureBuilder proxyFactory(IProxyFactory proxyFactory) {
        configurer.setProxyFactory(proxyFactory);
        return this;
    }

    public ApplicationConfigureBuilder beanLoaderFactory(IBeanLoadFactory beanLoaderFactory) {
        configurer.setBeanLoadFactory(beanLoaderFactory);
        return this;
    }

    public ApplicationConfigureBuilder addPackageNames(String... packageNames) {
        if (packageNames != null && packageNames.length > 0) {
            configurer.addPackageNames(Arrays.asList(packageNames));
        }
        return this;
    }

    public ApplicationConfigureBuilder excludedPackageNames(String... excludedPackageNames) {
        if (excludedPackageNames != null && excludedPackageNames.length > 0) {
            configurer.addExcludedPackageNames(Arrays.asList(excludedPackageNames));
        }
        return this;
    }

    public ApplicationConfigureBuilder excludedFiles(String... excludedFiles) {
        if (excludedFiles != null && excludedFiles.length > 0) {
            configurer.addExcludedFiles(Arrays.asList(excludedFiles));
        }
        return this;
    }

    public ApplicationConfigureBuilder excludedModules(String... excludedModules) {
        if (excludedModules != null && excludedModules.length > 0) {
            configurer.addExcludedModules(Arrays.asList(excludedModules));
        }
        return this;
    }

    public ApplicationConfigureBuilder passwordProcess(IPasswordProcessor passwordProcessor) {
        configurer.setPasswordProcessor(passwordProcessor);
        return this;
    }

    public ApplicationConfigureBuilder defaultLocale(Locale defaultLocale) {
        configurer.setDefaultLocale(defaultLocale);
        return this;
    }

    public ApplicationConfigureBuilder i18nEventHandler(II18nEventHandler i18nEventHandler) {
        configurer.setI18nEventHandler(i18nEventHandler);
        return this;
    }

    public ApplicationConfigureBuilder addParameter(String key, String value) {
        configurer.addParameter(key, value);
        return this;
    }

    public ApplicationConfigureBuilder addParameters(Map<String, String> parameters) {
        configurer.addParameters(parameters);
        return this;
    }

    public ApplicationConfigureBuilder addModuleConfigurers(IModuleConfigurer... configurers) {
        if (configurers != null && configurers.length > 0) {
            Arrays.stream(configurers).forEach(item -> moduleConfigurers.put(item.getModuleName(), item));
        }
        return this;
    }

    public IApplicationConfigurer build() {
        return configurer;
    }
}
