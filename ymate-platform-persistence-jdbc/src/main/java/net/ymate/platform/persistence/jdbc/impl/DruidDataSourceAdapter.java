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

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.AbstractDatabaseDataSourceAdapter;
import net.ymate.platform.persistence.jdbc.annotation.DataSourceAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-02 10:00
 * @since 2.1.0
 */
@DataSourceAdapter(Type.DS_ADAPTER.DRUID)
public class DruidDataSourceAdapter extends AbstractDatabaseDataSourceAdapter {

    private DruidDataSource dataSource;

    private InputStream openInputStream() throws IOException {
        InputStream inputStream = getDataSourceConfigFileAsStream(Type.DS_ADAPTER.DRUID, getDataSourceConfig().getName());
        if (inputStream == null) {
            inputStream = getDataSourceConfigFileAsStream(Type.DS_ADAPTER.DBCP, getDataSourceConfig().getName());
        }
        return inputStream;
    }

    @Override
    protected void doInitialize() throws Exception {
        try (InputStream inputStream = openInputStream()) {
            if (inputStream != null) {
                dataSource = new DruidDataSource();
                DruidDataSourceFactory.config(dataSource, doCreateConfigProperties(inputStream, false));
            } else if (doCreateDataSourceConfigFile(Type.DS_ADAPTER.DRUID)) {
                try (InputStream newInputStream = getDataSourceConfigFileAsStream(Type.DS_ADAPTER.DRUID, getDataSourceConfig().getName())) {
                    dataSource = new DruidDataSource();
                    DruidDataSourceFactory.config(dataSource, doCreateConfigProperties(newInputStream, false));
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
