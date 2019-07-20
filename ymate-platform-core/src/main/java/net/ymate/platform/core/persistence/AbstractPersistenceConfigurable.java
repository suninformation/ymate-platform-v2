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
package net.ymate.platform.core.persistence;

import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-15 04:05
 * @since 2.1.0
 */
public abstract class AbstractPersistenceConfigurable<DATA_SOURCE extends IDataSourceConfigurable> extends DefaultModuleConfigurable {

    private final Map<String, DATA_SOURCE> dataSources = new HashMap<>();

    protected AbstractPersistenceConfigurable(String moduleName) {
        super(moduleName);
    }

    public void setDefaultDataSourceName(String defaultDataSourceName) {
        addConfig(IPersistenceConfig.DS_DEFAULT_NAME, StringUtils.trimToEmpty(defaultDataSourceName));
    }

    public void addDataSource(DATA_SOURCE dataSourceConfig) {
        dataSources.put(dataSourceConfig.getDataSourceName(), dataSourceConfig);
    }

    @Override
    public IModuleConfigurer toModuleConfigurer() {
        if (!dataSources.isEmpty()) {
            addConfig(IPersistenceConfig.DS_NAME_LIST, StringUtils.join(dataSources.keySet(), "|"));
            dataSources.values().stream().map(IDataSourceConfigurable::toMap).forEach(this::addConfig);
        }
        return super.toModuleConfigurer();
    }

    protected static abstract class AbstractBuilder<T extends AbstractBuilder, CONFIGURABLE extends AbstractPersistenceConfigurable, DATA_SOURCE extends IDataSourceConfigurable> {

        protected final CONFIGURABLE configurable;

        protected AbstractBuilder(CONFIGURABLE configurable) {
            this.configurable = configurable;
        }

        @SuppressWarnings("unchecked")
        public T defaultDataSourceName(String defaultDataSourceName) {
            configurable.setDefaultDataSourceName(defaultDataSourceName);
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T addDataSources(DATA_SOURCE... dataSourceConfigurables) {
            if (ArrayUtils.isNotEmpty(dataSourceConfigurables)) {
                Arrays.stream(dataSourceConfigurables).forEach(configurable::addDataSource);
            }
            return (T) this;
        }

        public IModuleConfigurer build() {
            return configurable.toModuleConfigurer();
        }
    }
}
