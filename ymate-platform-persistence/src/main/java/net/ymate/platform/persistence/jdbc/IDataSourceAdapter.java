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

import net.ymate.platform.persistence.jdbc.dialect.IDialect;

import java.sql.Connection;

/**
 * 数据源适配器接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/4/2 上午11:26
 * @version 1.0
 */
public interface IDataSourceAdapter {

    /**
     * 数据源适配器初始化
     *
     * @param cfgMeta 数据源配置参数
     * @throws Exception
     */
    public void initialize(DataSourceCfgMeta cfgMeta) throws Exception;

    /**
     * @return 获取数据源配置参数
     */
    public DataSourceCfgMeta getDataSourceCfgMeta();

    /**
     * @return 获取数据库连接
     * @throws Exception
     */
    public Connection getConnection() throws Exception;

    /**
     * @return 获取数据库方言
     */
    public IDialect getDialect();

    /**
     * 销毁数据源适配器
     */
    public void destroy();
}
