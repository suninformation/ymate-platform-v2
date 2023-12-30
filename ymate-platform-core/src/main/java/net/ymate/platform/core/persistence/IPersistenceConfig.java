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

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IInitialization;

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-07-31 15:04
 * @since 2.1.0
 */
@Ignored
@SuppressWarnings("rawtypes")
public interface IPersistenceConfig<OWNER extends IPersistence, DATA_SOURCE_CONFIG extends IDataSourceConfig> extends IInitialization<OWNER> {

    String DEFAULT_STR = "default";

    String DS_DEFAULT_NAME = "ds_default_name";

    String DS_NAME_LIST = "ds_name_list";

    String USERNAME = "username";

    String PASSWORD = "password";

    String PASSWORD_ENCRYPTED = "password_encrypted";

    String PASSWORD_CLASS = "password_class";

    /**
     * 获取默认数据源名称，默认值：default
     *
     * @return 返回默认数据源名称
     */
    String getDefaultDataSourceName();

    /**
     * 获取数据源配置映射
     *
     * @return 返回数据源配置映射
     */
    Map<String, DATA_SOURCE_CONFIG> getDataSourceConfigs();

    /**
     * 获取默认数据源配置
     *
     * @return 返回默认数据源配置
     */
    DATA_SOURCE_CONFIG getDefaultDataSourceConfig();

    /**
     * 获取指定名称的数据源配置
     *
     * @param dataSourceName 数据源名称
     * @return 返回指定名称的数据源配置
     */
    DATA_SOURCE_CONFIG getDataSourceConfig(String dataSourceName);

    /**
     * 注册数据源配置（在使用通过该方法添加数据源配置时需判断其是否已被初始化）
     *
     * @param dataSourceConfig 数据源配置对象
     */
    void addDataSourceConfig(DATA_SOURCE_CONFIG dataSourceConfig);
}
