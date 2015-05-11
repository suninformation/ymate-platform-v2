/*
 * Copyright 2007-2107 the original author or authors.
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
 * @author 刘镇 (suninformation@163.com) on 15/3/30 上午2:01
 * @version 1.0
 */
public interface IDatabaseModuleCfg {

    /**
     * @return 返回默认数据源名称，默认值：default
     */
    public String getDataSourceDefaultName();

    /**
     * @return 返回数据源配置映射
     */
    public Map<String, DataSourceCfgMeta> getDataSourceCfgs();

    /**
     * @return 返回默认数据源配置
     */
    public DataSourceCfgMeta getDefaultDataSourceCfg();

    /**
     * @param name 数据源名称
     * @return 返回指定名称的数据源配置
     */
    public DataSourceCfgMeta getDataSourceCfg(String name);
}
