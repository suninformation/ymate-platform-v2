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
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationConfigureFactory;
import net.ymate.platform.core.IApplicationConfigurer;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurer;
import net.ymate.platform.core.persistence.IDataSourceRouter;
import net.ymate.platform.persistence.jdbc.annotation.DataSourceAdapter;
import net.ymate.platform.persistence.jdbc.annotation.Dialect;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseConfig;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseSession;
import net.ymate.platform.persistence.jdbc.repo.RepositoryProxy;
import net.ymate.platform.persistence.jdbc.repo.annotation.Repository;
import net.ymate.platform.persistence.jdbc.repo.handle.RepositoryHandler;
import net.ymate.platform.persistence.jdbc.transaction.ITransaction;
import net.ymate.platform.persistence.jdbc.transaction.TransactionProxy;
import net.ymate.platform.persistence.jdbc.transaction.Transactions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2011-9-10 下午11:45:25
 */
public final class JDBC implements IModule, IDatabase {

    private static final Log LOG = LogFactory.getLog(JDBC.class);

    private static volatile IDatabase instance;

    /**
     * 框架提供的已知数据源适配器名称映射
     */
    public static final Map<String, String> DS_ADAPTERS;

    /**
     * 框架提供的已知数据库连接驱动
     */
    public static final Map<String, String> DB_DRIVERS;

    /**
     * 提供的已知数据库方言
     */
    public static final Map<String, Class<? extends IDialect>> DB_DIALECTS;

    static {
        Map<String, Class<? extends IDialect>> dbDialects = new HashMap<>();
        Map<String, String> dbDrivers = new HashMap<>();
        Map<String, String> dbAdapters = new HashMap<>();
        try {
            ClassUtils.getExtensionLoader(IDialect.class, true).getExtensionClasses().forEach(dialectClass -> {
                Dialect dialectAnn = dialectClass.getAnnotation(Dialect.class);
                if (dialectAnn != null) {
                    dbDialects.put(dialectAnn.value(), dialectClass);
                    if (StringUtils.isNotBlank(dialectAnn.driverClass())) {
                        dbDrivers.put(dialectAnn.value(), dialectAnn.driverClass());
                    }
                }
            });
            ClassUtils.getExtensionLoader(IDatabaseDataSourceAdapter.class, true).getExtensionClasses().forEach(adapterClass -> {
                DataSourceAdapter adapterAnn = adapterClass.getAnnotation(DataSourceAdapter.class);
                if (adapterAnn != null) {
                    dbAdapters.put(adapterAnn.value(), adapterClass.getName());
                }
            });
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        DB_DIALECTS = Collections.unmodifiableMap(dbDialects);
        DB_DRIVERS = Collections.unmodifiableMap(dbDrivers);
        DS_ADAPTERS = Collections.unmodifiableMap(dbAdapters);
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

    private Map<String, IDatabaseDataSourceAdapter> dataSourceCaches = new ConcurrentHashMap<>();

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
            IApplicationConfigureFactory configureFactory = owner.getConfigureFactory();
            if (configureFactory != null) {
                IApplicationConfigurer configurer = configureFactory.getConfigurer();
                if (configurer != null) {
                    IBeanLoadFactory beanLoaderFactory = configurer.getBeanLoadFactory();
                    if (beanLoaderFactory != null) {
                        IBeanLoader beanLoader = beanLoaderFactory.getBeanLoader();
                        if (beanLoader != null) {
                            beanLoader.registerHandler(Repository.class, new RepositoryHandler(this));
                        }
                    }
                }
                if (config == null) {
                    IModuleConfigurer moduleConfigurer = configurer == null ? null : configurer.getModuleConfigurer(MODULE_NAME);
                    if (moduleConfigurer != null) {
                        config = DefaultDatabaseConfig.create(configureFactory.getMainClass(), moduleConfigurer);
                    } else {
                        config = DefaultDatabaseConfig.create(configureFactory.getMainClass(), DefaultModuleConfigurer.createEmpty(MODULE_NAME));
                    }
                }
            }
            if (config == null) {
                config = DefaultDatabaseConfig.defaultConfig();
            }
            //
            if (!config.isInitialized()) {
                config.initialize(this);
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

    private IDatabaseDataSourceAdapter doSafeGetDataSourceAdapter(String dataSourceName) {
        IDatabaseDataSourceAdapter dataSourceAdapter = dataSourceCaches.get(dataSourceName);
        if (dataSourceAdapter == null) {
            throw new IllegalStateException(String.format("Datasource '%s' not found.", dataSourceName));
        }
        return dataSourceAdapter;
    }

    @Override
    public IDatabaseConnectionHolder getConnectionHolder(String dataSourceName) throws Exception {
        IDatabaseConnectionHolder connectionHolder;
        ITransaction transaction = Transactions.get();
        if (transaction != null) {
            connectionHolder = transaction.getConnectionHolder(dataSourceName);
            if (connectionHolder == null) {
                connectionHolder = new DefaultDatabaseConnectionHolder(doSafeGetDataSourceAdapter(dataSourceName));
                transaction.registerConnectionHolder(connectionHolder);
            }
        } else {
            connectionHolder = new DefaultDatabaseConnectionHolder(doSafeGetDataSourceAdapter(dataSourceName));
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
    public IDatabaseDataSourceAdapter getDefaultDataSourceAdapter() {
        return getDataSourceAdapter(config.getDefaultDataSourceName());
    }

    @Override
    public IDatabaseDataSourceAdapter getDataSourceAdapter(String dataSourceName) {
        return doSafeGetDataSourceAdapter(dataSourceName);
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
