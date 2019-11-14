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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.commons.util.ResourceUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.persistence.AbstractDataSourceAdapter;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 抽象数据源适配器
 *
 * @author 刘镇 (suninformation@163.com) on 2013年8月1日 下午8:30:34
 */
public abstract class AbstractDatabaseDataSourceAdapter extends AbstractDataSourceAdapter<IDatabase, IDatabaseDataSourceConfig, Connection> implements IDatabaseDataSourceAdapter {

    private static final Log LOG = LogFactory.getLog(AbstractDatabaseDataSourceAdapter.class);

    private IDialect dialect;

    protected InputStream getDataSourceConfigFileAsStream(String dsAdapterType, String dataSourceName) throws IOException {
        if (StringUtils.isBlank(dataSourceName)) {
            throw new NullArgumentException("dataSourceName");
        }
        InputStream inputStream = null;
        File configFile = getDataSourceConfig().getConfigFile();
        if (configFile != null) {
            try {
                inputStream = new FileInputStream(configFile);
                if (LOG.isInfoEnabled()) {
                    LOG.info(String.format("Found and load the datasource [%s] config file: %s", dataSourceName, configFile.toURI().toURL()));
                }
            } catch (FileNotFoundException ignored) {
            }
        }
        if (inputStream == null) {
            if (StringUtils.isBlank(dsAdapterType)) {
                throw new NullArgumentException("dsAdapterType");
            }
            List<String> filePaths = new ArrayList<>();
            filePaths.add(RuntimeUtils.replaceEnvVariable(String.format("${root}/cfgs/%s_%s.properties", dsAdapterType, dataSourceName)));
            filePaths.add(RuntimeUtils.replaceEnvVariable(String.format("${root}/cfgs/%s.properties", dsAdapterType)));
            inputStream = FileUtils.loadFileAsStream(filePaths.toArray(new String[0]));
            //
            if (inputStream == null) {
                filePaths.clear();
                filePaths.add(String.format("%s_%s.properties", dsAdapterType, dataSourceName));
                filePaths.add(String.format("%s.properties", dsAdapterType));
                //
                inputStream = ResourceUtils.getResourceAsStream(AbstractDatabaseDataSourceAdapter.class, filePaths.toArray(new String[0]));
            }
        }
        return inputStream;
    }

    protected boolean doCreateDataSourceConfigFile(String dsAdapterType) {
        if (StringUtils.isNotBlank(dsAdapterType)) {
            File configFile = new File(String.format("%s/%s.properties", RuntimeUtils.replaceEnvVariable("${root}/cfgs"), dsAdapterType));
            if (configFile.isAbsolute() && !configFile.exists()) {
                try (InputStream inputStream = AbstractDatabaseDataSourceAdapter.class.getClassLoader().getResourceAsStream(String.format("META-INF/default-%s.properties", dsAdapterType))) {
                    if (inputStream != null) {
                        if (FileUtils.createFileIfNotExists(configFile, inputStream)) {
                            return true;
                        } else if (LOG.isWarnEnabled()) {
                            LOG.warn(String.format("Failed to create default %s config file: %s", dsAdapterType, configFile.getPath()));
                        }
                    }
                } catch (IOException e) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(String.format("An exception occurred while trying to generate the default %s config file: %s", dsAdapterType, configFile.getPath()), RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
        }
        return false;
    }

    protected Properties doCreateConfigProperties(InputStream inputStream, boolean forHikari) throws Exception {
        Properties properties = new Properties();
        if (inputStream != null) {
            properties.load(inputStream);
        }
        //
        properties.put("driverClassName", getDataSourceConfig().getDriverClass());
        properties.put(forHikari ? "jdbcUrl" : "url", getDataSourceConfig().getConnectionUrl());
        properties.put("username", getDataSourceConfig().getUsername());
        properties.put("password", decryptPasswordIfNeed());
        //
        return properties;
    }

    @Override
    protected void doInitialize(IDatabase owner, IDatabaseDataSourceConfig dataSourceConfig) throws Exception {
        if (StringUtils.isNotBlank(dataSourceConfig.getDialectClass())) {
            dialect = ClassUtils.impl(dataSourceConfig.getDialectClass(), IDialect.class, this.getClass());
        } else if (StringUtils.isNotBlank(dataSourceConfig.getType()) && !StringUtils.equals(Type.DATABASE.UNKNOWN, dataSourceConfig.getType())) {
            dialect = JDBC.DB_DIALECTS.get(dataSourceConfig.getType()).newInstance();
        }
        if (dialect != null && StringUtils.isNotBlank(dataSourceConfig.getIdentifierQuote())) {
            char[] quotes = dataSourceConfig.getIdentifierQuote().toCharArray();
            if (quotes.length == 1) {
                dialect.setIdentifierQuote(dataSourceConfig.getIdentifierQuote(), dataSourceConfig.getIdentifierQuote());
            } else if (quotes.length > 1) {
                dialect.setIdentifierQuote(Character.toString(quotes[0]), Character.toString(quotes[1]));
            }
        }
        //
        doInitialize();
    }

    /**
     * 执行初始化
     *
     * @throws Exception 可能产生的任何异常
     */
    protected abstract void doInitialize() throws Exception;

    @Override
    public IDialect getDialect() {
        return dialect;
    }

    @Override
    public void doClose() throws Exception {
        try {
            DriverManager.deregisterDriver(DriverManager.getDriver(getDataSourceConfig().getConnectionUrl()));
        } catch (SQLException ignored) {
        }
        dialect = null;
    }
}
