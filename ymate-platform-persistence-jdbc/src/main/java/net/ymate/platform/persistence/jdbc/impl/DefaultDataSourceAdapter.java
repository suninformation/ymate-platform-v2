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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 默认数据源适配器
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-29 下午4:12:05
 */
@DataSourceAdapter(Type.DS_ADAPTER.DEFAULT)
public class DefaultDataSourceAdapter extends AbstractDatabaseDataSourceAdapter {

    private String password;

    @Override
    protected void doInitialize() throws Exception {
        Class.forName(getDataSourceConfig().getDriverClass());
        //
        password = decryptPasswordIfNeed();
    }

    @Override
    public Connection getConnection() throws Exception {
        return DriverManager.getConnection(getDataSourceConfig().getConnectionUrl(), getDataSourceConfig().getUsername(), password);
    }

    @Override
    public DataSource getDataSource() {
        return null;
    }
}
