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

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.AbstractDatabaseDataSourceAdapter;
import net.ymate.platform.persistence.jdbc.annotation.DataSourceAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;

/**
 * 基于C3P0连接池的数据源适配器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2013-6-5 下午4:27:09
 */
@DataSourceAdapter(Type.DS_ADAPTER.C3P0)
public class C3P0DataSourceAdapter extends AbstractDatabaseDataSourceAdapter {

    private static final Log LOG = LogFactory.getLog(C3P0DataSourceAdapter.class);

    private ComboPooledDataSource dataSource;

    @Override
    protected void doInitialize() throws Exception {
        String path = RuntimeUtils.getRootPath();
        if (StringUtils.endsWith(path, "/WEB-INF")) {
            path += "/classes";
        }
        if (StringUtils.endsWith(path, "/classes")) {
            File configFile = new File(path, "c3p0.properties");
            if (!configFile.exists()) {
                try (InputStream inputStream = C3P0DataSourceAdapter.class.getClassLoader().getResourceAsStream("META-INF/default-c3p0.properties")) {
                    if (!FileUtils.createFileIfNotExists(configFile, inputStream) && LOG.isWarnEnabled()) {
                        LOG.warn(String.format("Failed to create default c3p0 config file: %s", configFile.getPath()));
                    }
                } catch (IOException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(String.format("An exception occurred while trying to generate the default c3p0 config file: %s", configFile.getPath()), RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
        }
        dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(getDataSourceConfig().getDriverClass());
        dataSource.setJdbcUrl(getDataSourceConfig().getConnectionUrl());
        dataSource.setUser(getDataSourceConfig().getUsername());
        dataSource.setPassword(decryptPasswordIfNeed());
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
