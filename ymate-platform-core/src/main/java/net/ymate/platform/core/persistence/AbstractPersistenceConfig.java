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

import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.configuration.impl.MapSafeConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @param <OWNER>              所属持久化容器类型
 * @param <DATA_SOURCE_CONFIG> 数据源配置类型
 * @author 刘镇 (suninformation@163.com) on 2019-07-31 15:19
 */
public abstract class AbstractPersistenceConfig<OWNER extends IPersistence, DATA_SOURCE_CONFIG extends IDataSourceConfig> implements IPersistenceConfig<OWNER, DATA_SOURCE_CONFIG> {

    private String dataSourceDefaultName;

    private final Map<String, DATA_SOURCE_CONFIG> dataSourceConfigs = new HashMap<>();

    private boolean initialized;

    public AbstractPersistenceConfig() {
    }

    public AbstractPersistenceConfig(IModuleConfigurer moduleConfigurer) throws Exception {
        IConfigReader configReader = moduleConfigurer.getConfigReader();
        //
        this.dataSourceDefaultName = configReader.getString(DS_DEFAULT_NAME, DEFAULT_STR);
        //
        String[] dsNameList = StringUtils.split(configReader.getString(DS_NAME_LIST, DEFAULT_STR), "|");
        for (String dsName : dsNameList) {
            if (!dataSourceConfigs.containsKey(dsName)) {
                Map<String, String> dsConfigMap = configReader.getMap(String.format("ds.%s.", dsName));
                if (!dsConfigMap.isEmpty()) {
                    this.dataSourceConfigs.put(dsName, buildDataSourceConfig(dsName, MapSafeConfigReader.bind(dsConfigMap)));
                }
            }
        }
    }

    /**
     * 由子类实现具体实例对象构建过程
     *
     * @param dataSourceName 数据源名称
     * @param configReader   配置读取器
     * @return 返回数据源配置对象
     * @throws Exception 可能产生的任何异常
     */
    protected abstract DATA_SOURCE_CONFIG buildDataSourceConfig(String dataSourceName, IConfigReader configReader) throws Exception;

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(OWNER owner) throws Exception {
        if (!initialized) {
            dataSourceDefaultName = StringUtils.defaultIfBlank(dataSourceDefaultName, DEFAULT_STR);
            //
            for (DATA_SOURCE_CONFIG dataSourceConfig : dataSourceConfigs.values()) {
                dataSourceConfig.initialize(owner);
            }
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public String getDefaultDataSourceName() {
        return dataSourceDefaultName;
    }

    public void setDataSourceDefaultName(String dataSourceDefaultName) {
        if (!initialized) {
            this.dataSourceDefaultName = dataSourceDefaultName;
        }
    }

    @Override
    public Map<String, DATA_SOURCE_CONFIG> getDataSourceConfigs() {
        return Collections.unmodifiableMap(dataSourceConfigs);
    }

    public void addDataSourceConfig(DATA_SOURCE_CONFIG dataSourceConfig) {
        if (!initialized) {
            dataSourceConfigs.put(dataSourceConfig.getName(), dataSourceConfig);
        }
    }

    @Override
    public DATA_SOURCE_CONFIG getDefaultDataSourceConfig() {
        return dataSourceConfigs.get(dataSourceDefaultName);
    }

    @Override
    public DATA_SOURCE_CONFIG getDataSourceConfig(String dataSourceName) {
        return dataSourceConfigs.get(dataSourceName);
    }
}
