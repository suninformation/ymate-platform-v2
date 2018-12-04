/*
 * Copyright 2007-2017 the original author or authors.
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
package net.ymate.platform.persistence.jdbc;

import java.util.Map;

/**
 * 数据库JDBC持久化模块配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-29 下午2:15:21
 * @version 1.0
 */
public interface IDatabaseModuleCfg {

    String DS_DEFAULT_NAME = "ds_default_name";

    String DS_NAME_LIST = "ds_name_list";

    String CONNECTION_URL = "connection_url";

    String USERNAME = "username";

    String PASSWORD = "password";

    String PASSWORD_ENCRYPTED = "password_encrypted";

    String PASSWORD_CLASS = "password_class";

    String SHOW_SQL = "show_sql";

    String STACK_TRACES = "stack_traces";

    String STACK_TRACE_DEPTH = "stack_trace_depth";

    String STACK_TRACE_PACKAGE = "stack_trace_package";

    String TABLE_PREFIX = "table_prefix";

    String IDENTIFIER_QUOTE = "identifier_quote";

    String ADAPTER_CLASS = "adapter_class";

    String TYPE = "type";

    String DIALECT_CLASS = "dialect_class";

    String DRIVER_CLASS = "driver_class";

    /**
     * @return 返回默认数据源名称，默认值：default
     */
    String getDataSourceDefaultName();

    /**
     * @return 返回数据源配置映射
     */
    Map<String, DataSourceCfgMeta> getDataSourceCfgs();

    /**
     * @return 返回默认数据源配置
     */
    DataSourceCfgMeta getDefaultDataSourceCfg();

    /**
     * @param name 数据源名称
     * @return 返回指定名称的数据源配置
     */
    DataSourceCfgMeta getDataSourceCfg(String name);
}
