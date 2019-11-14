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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.AbstractDatabaseDataSourceAdapter;
import net.ymate.platform.persistence.jdbc.annotation.DataSourceAdapter;

import java.io.InputStream;
import java.sql.Connection;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-02 14:03
 * @since 2.1.0
 */
@DataSourceAdapter(Type.DS_ADAPTER.HIKARICP)
public class HikariCPDataSourceAdapter extends AbstractDatabaseDataSourceAdapter {

    private HikariDataSource dataSource;

    @Override
    protected void doInitialize() throws Exception {
        try (InputStream inputStream = getDataSourceConfigFileAsStream(Type.DS_ADAPTER.HIKARICP, getDataSourceConfig().getName())) {
            dataSource = new HikariDataSource(new HikariConfig(doCreateConfigProperties(inputStream, true)));
        }
        //
        try (InputStream inputStream = getDataSourceConfigFileAsStream(Type.DS_ADAPTER.HIKARICP, getDataSourceConfig().getName())) {
            if (inputStream != null) {
                dataSource = new HikariDataSource(new HikariConfig(doCreateConfigProperties(inputStream, true)));
            } else if (doCreateDataSourceConfigFile(Type.DS_ADAPTER.HIKARICP)) {
                try (InputStream newInputStream = getDataSourceConfigFileAsStream(Type.DS_ADAPTER.HIKARICP, getDataSourceConfig().getName())) {
                    dataSource = new HikariDataSource(new HikariConfig(doCreateConfigProperties(newInputStream, true)));
                }
            }
        }
    }

    @Override
    public void doClose() throws Exception {
        if (dataSource != null) {
            dataSource.close();
        }
        super.doClose();
    }

    @Override
    public Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }
}
