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

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.AbstractDatabaseDataSourceAdapter;
import net.ymate.platform.persistence.jdbc.annotation.DataSourceAdapter;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 基于DBCP连接池的数据源适配器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2013年12月19日 下午2:47:10
 */
@DataSourceAdapter(Type.DS_ADAPTER.DBCP)
public class DBCPDataSourceAdapter extends AbstractDatabaseDataSourceAdapter {

    private static final Log LOG = LogFactory.getLog(DBCPDataSourceAdapter.class);

    private BasicDataSource dataSource;

    @Override
    protected void doInitialize() throws Exception {
        try (InputStream inputStream = getDataSourceConfigFileAsStream(Type.DS_ADAPTER.DBCP, getDataSourceConfig().getName())) {
            if (inputStream != null) {
                dataSource = BasicDataSourceFactory.createDataSource(doCreateConfigProperties(inputStream, false));
            } else if (doCreateDataSourceConfigFile(Type.DS_ADAPTER.DBCP)) {
                try (InputStream newInputStream = getDataSourceConfigFileAsStream(Type.DS_ADAPTER.DBCP, getDataSourceConfig().getName())) {
                    dataSource = BasicDataSourceFactory.createDataSource(doCreateConfigProperties(newInputStream, false));
                }
            }
        }
    }

    @Override
    public void doClose() throws Exception {
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (SQLException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        super.doClose();
    }

    @Override
    public Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }
}
