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

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.core.persistence.IDataSourceRouter;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import net.ymate.platform.persistence.jdbc.dialect.impl.*;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseConfig;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseSession;
import net.ymate.platform.persistence.jdbc.repo.RepositoryProxy;
import net.ymate.platform.persistence.jdbc.repo.annotation.Repository;
import net.ymate.platform.persistence.jdbc.repo.handle.RepositoryHandler;
import net.ymate.platform.persistence.jdbc.transaction.ITransaction;
import net.ymate.platform.persistence.jdbc.transaction.TransactionProxy;
import net.ymate.platform.persistence.jdbc.transaction.Transactions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2011-9-10 下午11:45:25
 */
@Module
public final class JDBC implements IModule, IDatabase {

    private static volatile IDatabase instance;

    /**
     * 框架提供的已知数据源适配器名称映射
     */
    public static final Map<String, String> DS_ADAPTERS;

    /**
     * 框架提供的已知数据库连接驱动
     */
    public static final Map<Type.DATABASE, String> DB_DRIVERS;

    /**
     * 提供的已知数据库方言
     */
    public static final Map<Type.DATABASE, Class<? extends IDialect>> DB_DIALECTS;

    static {
        Map<String, String> adapters = new HashMap<>(4);
        adapters.put("default", "net.ymate.platform.persistence.jdbc.impl.DefaultDataSourceAdapter");
        adapters.put("jndi", "net.ymate.platform.persistence.jdbc.impl.JNDIDataSourceAdapter");
        adapters.put("c3p0", "net.ymate.platform.persistence.jdbc.impl.C3P0DataSourceAdapter");
        adapters.put("dbcp", "net.ymate.platform.persistence.jdbc.impl.DBCPDataSourceAdapter");
        adapters.put("druid", "net.ymate.platform.persistence.jdbc.impl.DruidDataSourceAdapter");
        adapters.put("hikaricp", "net.ymate.platform.persistence.jdbc.impl.HikariCPDataSourceAdapter");
        DS_ADAPTERS = Collections.unmodifiableMap(adapters);
        //
        Map<Type.DATABASE, String> drivers = new HashMap<>(8);
        drivers.put(Type.DATABASE.MYSQL, "com.mysql.jdbc.Driver");
        drivers.put(Type.DATABASE.ORACLE, "oracle.jdbc.OracleDriver");
        drivers.put(Type.DATABASE.SQLSERVER, "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        drivers.put(Type.DATABASE.DB2, "com.ibm.db2.jcc.DB2Driver");
        drivers.put(Type.DATABASE.SQLITE, "org.sqlite.JDBC");
        drivers.put(Type.DATABASE.POSTGRESQL, "org.postgresql.Driver");
        drivers.put(Type.DATABASE.HSQLDB, "org.hsqldb.jdbcDriver");
        drivers.put(Type.DATABASE.H2, "org.h2.Driver");
        DB_DRIVERS = Collections.unmodifiableMap(drivers);
        //
        Map<Type.DATABASE, Class<? extends IDialect>> dialects = new HashMap<>(8);
        dialects.put(Type.DATABASE.MYSQL, MySQLDialect.class);
        dialects.put(Type.DATABASE.ORACLE, OracleDialect.class);
        dialects.put(Type.DATABASE.SQLSERVER, SQLServerDialect.class);
        dialects.put(Type.DATABASE.DB2, DB2Dialect.class);
        dialects.put(Type.DATABASE.SQLITE, SQLiteDialect.class);
        dialects.put(Type.DATABASE.POSTGRESQL, PostgreSQLDialect.class);
        dialects.put(Type.DATABASE.HSQLDB, HSQLDBDialect.class);
        dialects.put(Type.DATABASE.H2, H2Dialect.class);
        DB_DIALECTS = Collections.unmodifiableMap(dialects);
    }

    public static IDatabase get() {
        IDatabase inst = instance;
        if (inst == null) {
            synchronized (JDBC.class) {
                inst = instance;
                if (inst == null) {
                    instance = inst = YMP.get().getModuleManager().getModule(JDBC.class);
                }
            }
        }
        return inst;
    }

    private IApplication owner;

    private IDatabaseConfig config;

    private Map<String, IDatabaseDataSourceAdapter> dataSourceCaches;

    private boolean initialized;

    public JDBC() {
    }

    public JDBC(IDatabaseConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized) {
            //
            YMP.showModuleVersion("ymate-platform-persistence-jdbc", this);
            //
            this.owner = owner;
            this.owner.getEvents().registerEvent(DatabaseEvent.class);
            //
            if (config == null) {
                IModuleConfigurer moduleConfigurer = owner.getConfigurer().getModuleConfigurer(MODULE_NAME);
                config = moduleConfigurer == null ? DefaultDatabaseConfig.defaultConfig() : DefaultDatabaseConfig.create(moduleConfigurer);
            }
            //
            if (!config.isInitialized()) {
                config.initialize(this);
            }
            //
            IBeanLoadFactory beanLoaderFactory = YMP.getBeanLoadFactory();
            if (beanLoaderFactory != null && beanLoaderFactory.getBeanLoader() != null) {
                beanLoaderFactory.getBeanLoader().registerHandler(Repository.class, new RepositoryHandler(this));
            }
            //
            IProxyFactory proxyFactory = owner.getBeanFactory().getProxyFactory();
            if (proxyFactory != null) {
                proxyFactory.registerProxy(new TransactionProxy());
                proxyFactory.registerProxy(new RepositoryProxy(this));
            }
            //
            for (Map.Entry<String, IDatabaseDataSourceConfig> entry : config.getDataSourceConfigs().entrySet()) {
                IDatabaseDataSourceAdapter dataSourceAdapter = entry.getValue().getAdapterClass().newInstance();
                dataSourceAdapter.initialize(this, entry.getValue());
                // 将数据源适配器放入缓存
                dataSourceCaches.put(entry.getKey(), dataSourceAdapter);
            }
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            for (IDatabaseDataSourceAdapter adapter : dataSourceCaches.values()) {
                adapter.close();
            }
            dataSourceCaches = null;
            config = null;
            owner = null;
        }
    }

    @Override
    public IApplication getOwner() {
        return owner;
    }

    @Override
    public IDatabaseConfig getConfig() {
        return config;
    }

    @Override
    public IDatabaseConnectionHolder getDefaultConnectionHolder() throws Exception {
        return getConnectionHolder(config.getDefaultDataSourceName());
    }

    @Override
    public IDatabaseConnectionHolder getConnectionHolder(String dataSourceName) throws Exception {
        IDatabaseConnectionHolder connectionHolder;
        ITransaction transaction = Transactions.get();
        if (transaction != null) {
            connectionHolder = transaction.getConnectionHolder(dataSourceName);
            if (connectionHolder == null) {
                connectionHolder = new DefaultDatabaseConnectionHolder(dataSourceCaches.get(dataSourceName));
                transaction.registerConnectionHolder(connectionHolder);
            }
        } else {
            connectionHolder = new DefaultDatabaseConnectionHolder(dataSourceCaches.get(dataSourceName));
        }
        return connectionHolder;
    }

    @Override
    public void releaseConnectionHolder(IDatabaseConnectionHolder connectionHolder) throws Exception {
        // 需要判断当前连接是否参与事务，若存在事务则不进行关闭操作
        if (Transactions.get() == null) {
            if (connectionHolder != null) {
                connectionHolder.close();
            }
        }
    }

    @Override
    public <T> T openSession(IDatabaseSessionExecutor<T> executor) throws Exception {
        return openSession(getDefaultConnectionHolder(), executor);
    }

    @Override
    public <T> T openSession(String dataSourceName, IDatabaseSessionExecutor<T> executor) throws Exception {
        return openSession(getConnectionHolder(dataSourceName), executor);
    }

    @Override
    public <T> T openSession(IDatabaseConnectionHolder connectionHolder, IDatabaseSessionExecutor<T> executor) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(this, connectionHolder)) {
            return executor.execute(session);
        }
    }

    @Override
    public <T> T openSession(IDataSourceRouter dataSourceRouter, IDatabaseSessionExecutor<T> executor) throws Exception {
        return openSession(getConnectionHolder(dataSourceRouter.getDataSourceName()), executor);
    }

    @Override
    public IDatabaseSession openSession() throws Exception {
        return new DefaultDatabaseSession(this, getDefaultConnectionHolder());
    }

    @Override
    public IDatabaseSession openSession(String dataSourceName) throws Exception {
        return new DefaultDatabaseSession(this, getConnectionHolder(dataSourceName));
    }

    @Override
    public IDatabaseSession openSession(IDatabaseConnectionHolder connectionHolder) throws Exception {
        return new DefaultDatabaseSession(this, connectionHolder);
    }

    @Override
    public IDatabaseSession openSession(IDataSourceRouter dataSourceRouter) throws Exception {
        return new DefaultDatabaseSession(this, getConnectionHolder(dataSourceRouter.getDataSourceName()));
    }
}
