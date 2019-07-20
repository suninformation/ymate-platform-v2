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
package net.ymate.platform.starter.impl;

import net.ymate.platform.core.AbstractApplicationConfigurer;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationConfigureParser;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.intercept.InterceptSettings;
import net.ymate.platform.core.configuration.impl.MapSafeConfigReader;
import net.ymate.platform.core.i18n.II18nEventHandler;
import net.ymate.platform.core.module.IModuleConfigurer;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-07 20:41
 * @since 2.1.0
 */
public class DefaultApplicationConfigurer extends AbstractApplicationConfigurer {

    private static final String CONFIG_DEV_MODE = "ymp.dev_mode";

    private static final String CONFIG_EXCLUDED_PACKAGES = "ymp.excluded_packages";

    private static final String CONFIG_EXCLUDED_FILES = "ymp.excluded_files";

    private static final String CONFIG_EXCLUDED_NODULES = "ymp.excluded_modules";

    private static final String CONFIG_PARAMS_PREFIX = "ymp.params.";

    private static final String CONFIG_DEFAULT_LOCALE = "ymp.default_locale";

    private static final String CONFIG_I18N_EVENT_HANDLER_CLASS = "ymp.i18n_event_handler_class";

    private static final String CONFIG_INTERCEPT_PREFIX = "ymp.intercept.";

    public DefaultApplicationConfigurer() {
        this(DefaultApplicationConfigureParser.systemDefault());
    }

    public DefaultApplicationConfigurer(IApplicationConfigureParser configureParser) {
        super(configureParser);
        if (configureParser != null) {
            setRunEnv(YMP.getPriorityRunEnv(configureParser.getConfigReader().getBoolean(CONFIG_DEV_MODE) ? IApplication.Environment.DEV : null));
            //
            setProxyFactory(YMP.getProxyFactory());
            setBeanLoadFactory(YMP.getBeanLoadFactory());
            //
            addPackageNames(parseArrayValue(IApplication.SYSTEM_PACKAGES, IApplication.SYSTEM_PACKAGES));
            addExcludedPackageNames(parseArrayValue(CONFIG_EXCLUDED_PACKAGES));
            addExcludedFiles(parseArrayValue(CONFIG_EXCLUDED_FILES));
            addExcludedModules(parseArrayValue(CONFIG_EXCLUDED_NODULES));
            //
            setDefaultLocale(LocaleUtils.toLocale(StringUtils.trimToNull(configureParser.getConfigReader().getString(CONFIG_DEFAULT_LOCALE))));
            setI18nEventHandler(configureParser.getConfigReader().getClassImpl(CONFIG_I18N_EVENT_HANDLER_CLASS, II18nEventHandler.class));
            //
            setInterceptSettings(InterceptSettings.create(MapSafeConfigReader.bind(configureParser.getConfigReader().getMap(CONFIG_INTERCEPT_PREFIX))));
            //
            addParameters(configureParser.getConfigReader().getMap(CONFIG_PARAMS_PREFIX));
        }
    }

    private List<String> parseArrayValue(String configVarName) {
        return parseArrayValue(null, configVarName);
    }

    private List<String> parseArrayValue(String systemVarName, String configVarName) {
        String[] packageNames = StringUtils.split(StringUtils.defaultIfBlank(StringUtils.isBlank(systemVarName) ? null : System.getProperty(systemVarName), getConfigureParser() != null ? getConfigureParser().getConfigReader().getString(configVarName) : StringUtils.EMPTY), "|");
        if (packageNames != null && packageNames.length > 0) {
            return Arrays.asList(packageNames);
        }
        return Collections.emptyList();
    }

    @Override
    public IModuleConfigurer getModuleConfigurer(String moduleName) {
        return getConfigureParser() == null ? null : getConfigureParser().getModuleConfigurer(moduleName);
    }
}
