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
     * 是否生成新的BaseEntity类，默认为false(即表示使用框架提供的BaseEntity类)
     */
    String PARAMS_JDBC_USE_BASE_ENTITY = "jdbc.use_base_entity";

    /**
     * 是否使用类名后缀，不使用和使用的区别如: User-->UserModel，默认为false
     */
    String PARAMS_JDBC_USE_CLASS_SUFFIX = "jdbc.use_class_suffix";

    /**
     * 是否采用链式调用模式，默认为false
     */
    String PARAMS_JDBC_USE_CHAIN_MODE = "jdbc.use_chain_mode";

    /**
     * 是否添加类成员属性值状态变化注解，默认为false
     */
    String PARAMS_JDBC_USE_STATE_SUPPORT = "jdbc.use_state_support";

    /**
     * 实体及属性命名过滤器接口实现类，默认为空
     */
    String PARAMS_JDBC_NAMED_FILTER_CLASS = "jdbc.named_filter_class";

    /**
     * 数据库名称(仅针对特定的数据库使用，如Oracle)，默认为空
     */
    String PARAMS_JDBC_DB_NAME = "jdbc.db_name";

    /**
     * 数据库用户名称(仅针对特定的数据库使用，如Oracle)，默认为空
     */
    String PARAMS_JDBC_DB_USERNAME = "jdbc.db_username";

    /**
     * 数据库表名称前缀，多个用'|'分隔，默认为空
     */
    String PARAMS_JDBC_TABLE_PREFIX = "jdbc.table_prefix";

    /**
     * 否剔除生成的实体映射表名前缀，默认为false
     */
    String PARAMS_JDBC_REMOVE_TABLE_PREFIX = "jdbc.remove_table_prefix";

    /**
     * 预生成实体的数据表名称列表，多个用'|'分隔，默认为空表示全部生成
     */
    String PARAMS_JDBC_TABLE_LIST = "jdbc.table_list";

    /**
     * 排除的数据表名称列表，在此列表内的数据表将不被生成实体，多个用'|'分隔，默认为空
     */
    String PARAMS_JDBC_TABLE_EXCLUDE_LIST = "jdbc.table_exclude_list";

    /**
     * 需要添加@Readonly注解声明的字段名称列表，多个用'|'分隔，默认为空
     */
    String PARAMS_JDBC_READONLY_FIELD_LIST = "jdbc.readonly_field_list";

    /**
     * 生成的代码文件输出路径，默认为${root}
     */
    String PARAMS_JDBC_OUTPUT_PATH = "jdbc.output_path";

    /**
     * 生成的代码所属包名称，默认为: packages
     */
    String PARAMS_JDBC_PACKAGE_NAME = "jdbc.package_name";

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
