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
package net.ymate.platform.persistence.jdbc.impl;

import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.AbstractDatabaseDataSourceAdapter;
import net.ymate.platform.persistence.jdbc.annotation.DataSourceAdapter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 基于JNDI的数据源适配器
 *
 * @author 刘镇 (suninformation@163.com) on 2013年12月19日 下午2:43:51
 */
@DataSourceAdapter(Type.DS_ADAPTER.JNDI)
public class JNDIDataSourceAdapter extends AbstractDatabaseDataSourceAdapter {

    private DataSource dataSource;

    @Override
    protected void doInitialize() throws Exception {
        Context initialContext = new InitialContext();
        Context envContext = (Context) initialContext.lookup("java:/comp/env");
        // 从JNDI获取数据库源
        dataSource = (DataSource) envContext.lookup(getDataSourceConfig().getConnectionUrl());
    }

    @Override
    public Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }
}
