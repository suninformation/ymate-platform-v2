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
package net.ymate.platform.persistence.jdbc;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.IPersistenceConfig;

/**
 * 数据库JDBC持久化配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-29 下午2:15:21
 */
@Ignored
public interface IDatabaseConfig extends IPersistenceConfig<IDatabase, IDatabaseDataSourceConfig> {

    String CONNECTION_URL = "connection_url";

    String SHOW_SQL = "show_sql";

    String STACK_TRACES = "stack_traces";

    String STACK_TRACE_DEPTH = "stack_trace_depth";

    String STACK_TRACE_PACKAGES = "stack_trace_packages";

    String TABLE_PREFIX = "table_prefix";

    String IDENTIFIER_QUOTE = "identifier_quote";

    String ADAPTER_CLASS = "adapter_class";

    String TYPE = "type";

    String DIALECT_CLASS = "dialect_class";

    String CONFIG_FILE = "config_file";

    String DRIVER_CLASS = "driver_class";

    String PARAMS_JDBC_USE_BASE_ENTITY = "jdbc.use_base_entity";

    String PARAMS_JDBC_USE_CLASS_SUFFIX = "jdbc.use_class_suffix";

    String PARAMS_JDBC_USE_CHAIN_MODE = "jdbc.use_chain_mode";

    String PARAMS_JDBC_USE_STATE_SUPPORT = "jdbc.use_state_support";

    String PARAMS_JDBC_NAMED_FILTER_CLASS = "jdbc.named_filter_class";

    String PARAMS_JDBC_DB_NAME = "jdbc.db_name";

    String PARAMS_JDBC_DB_USERNAME = "jdbc.db_username";

    String PARAMS_JDBC_TABLE_PREFIX = "jdbc.table_prefix";

    String PARAMS_JDBC_CLASS_SUFFIX = "jdbc.class_suffix";

    String PARAMS_JDBC_REMOVE_TABLE_PREFIX = "jdbc.remove_table_prefix";

    String PARAMS_JDBC_TABLE_LIST = "jdbc.table_list";

    String PARAMS_JDBC_TABLE_EXCLUDE_LIST = "jdbc.table_exclude_list";

    String PARAMS_JDBC_READONLY_FIELD_LIST = "jdbc.readonly_field_list";

    String PARAMS_JDBC_OUTPUT_PATH = "jdbc.output_path";

    String PARAMS_JDBC_PACKAGE_NAME = "jdbc.package_name";
}
