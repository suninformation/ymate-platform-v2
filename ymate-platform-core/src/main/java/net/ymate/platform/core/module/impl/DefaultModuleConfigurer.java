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
package net.ymate.platform.core.module.impl;

import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.configuration.impl.MapSafeConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-07 18:52
 * @since 2.1.0
 */
public class DefaultModuleConfigurer implements IModuleConfigurer {

    private final String moduleName;

    private final IConfigReader configReader;

    public DefaultModuleConfigurer(String moduleName, Map<?, ?> cfgMap) {
        this(moduleName, MapSafeConfigReader.bind(cfgMap));
    }

    public DefaultModuleConfigurer(String moduleName, IConfigReader configReader) {
        if (StringUtils.isBlank(moduleName)) {
            throw new NullArgumentException("moduleName");
        }
        if (configReader == null) {
            throw new NullArgumentException("configReader");
        }
        this.moduleName = moduleName;
        this.configReader = configReader;
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public IConfigReader getConfigReader() {
        return configReader;
    }
}
