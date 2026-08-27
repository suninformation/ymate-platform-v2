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

import net.ymate.platform.commons.LazyHolder;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationConfigureFactory;
import net.ymate.platform.core.IApplicationConfigurer;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.IBeanLoader;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.module.AbstractModule;
import net.ymate.platform.core.module.IModuleConfigurer;
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
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 刘镇 (suninformation@163.com) on 2011-9-10 下午11:45:25
 */
public final class JDBC extends AbstractModule<IDatabaseConfig> implements IDatabase {

    private static final Log LOG = LogFactory.getLog(JDBC.class);

    private static final LazyHolder<IDatabase> instance = LazyHolder.of(() -> YMP.get().getModuleManager().getModule(JDBC.class));

    private static final ReentrantLockHelper LOCKER = new ReentrantLockHelper();

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

    /**
     * 全局数据库会话事件监听器
     *
     * @since 2.1.4
     */
    private static final DatabaseSessionEventListener globalSessionEventListener = new DatabaseSessionEventListener();

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
            ClassUtils.getExtensionLoader(IDatabaseSessionEventListener.class, true).getExtensions().forEach(globalSessionEventListener::addListener);
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
        return instance.get();
    }

    private Map<String, IDatabaseDataSourceAdapter> dataSourceCaches = new ConcurrentHashMap<>();

    public JDBC() {
    }

    public JDBC(IDatabaseConfig config) {
        doSetConfig(config);
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @Override
    protected String doGetModuleVersion() {
        return "ymate-platform-persistence-jdbc";
    }

    @Override
    protected IDatabaseConfig doCreateModuleConfig(Class<?> mainClass, IModuleConfigurer moduleConfigurer) throws Exception {
        return DefaultDatabaseConfig.create(mainClass, moduleConfigurer);
    }

    @Override
    protected IDatabaseConfig doCreateDefaultConfig() {
        return DefaultDatabaseConfig.defaultConfig();
    }

    @Override
    protected void onInit(IApplication owner) throws Exception {
        owner.getEvents().registerEvent(DatabaseEvent.class);
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
        }
        //
        IProxyFactory proxyFactory = owner.getBeanFactory().getProxyFactory();
        if (proxyFactory != null) {
            proxyFactory.registerProxy(new TransactionProxy());
            proxyFactory.registerProxy(new RepositoryProxy(this));
        }
        // 处理设置为自动连接的数据源
        getConfig().getDataSourceConfigs()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().isAutoConnection())
                .map(Map.Entry::getKey)
                .forEach(this::doSafeGetDataSourceAdapter);
    }

    @Override
    protected void onClose() throws Exception {
        for (IDatabaseDataSourceAdapter adapter : dataSourceCaches.values()) {
            adapter.close();
        }
        dataSourceCaches = null;
    }

    @Override
    public IDatabaseConnectionHolder getDefaultConnectionHolder() throws Exception {
        return getConnectionHolder(getConfig().getDefaultDataSourceName());
    }

    private IDatabaseDataSourceAdapter doSafeGetDataSourceAdapter(String dataSourceName) {
        IDatabaseDataSourceAdapter dataSourceAdapter = dataSourceCaches.get(dataSourceName);
        if (dataSourceAdapter == null) {
            ReentrantLock lock = null;
            try {
                lock = LOCKER.getLocker(dataSourceName);
                lock.lock();
                IDatabaseDataSourceConfig dataSourceConfig = getConfig().getDataSourceConfig(dataSourceName);
                if (dataSourceConfig != null) {
                    if (!dataSourceConfig.isInitialized()) {
                        dataSourceConfig.initialize(this);
                    }
                    // 实例化数据源适配器并放入缓存
                    dataSourceAdapter = dataSourceCaches.get(dataSourceName);
                    if (dataSourceAdapter == null) {
                        dataSourceAdapter = ClassUtils.impl(dataSourceConfig.getAdapterClass(), IDatabaseDataSourceAdapter.class);
                    }
                    if (dataSourceAdapter != null && !dataSourceAdapter.isInitialized()) {
                        dataSourceAdapter.initialize(this, dataSourceConfig);
                    }
                    dataSourceCaches.put(dataSourceName, dataSourceAdapter);
                }
            } catch (Exception e) {
                throw RuntimeUtils.wrapRuntimeThrow(e);
            } finally {
                ReentrantLockHelper.unlock(lock);
            }
        }
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
        return getDataSourceAdapter(getConfig().getDefaultDataSourceName());
    }

    @Override
    public IDatabaseDataSourceAdapter getDataSourceAdapter(String dataSourceName) {
        return doSafeGetDataSourceAdapter(dataSourceName);
    }

    @Override
    public IDatabase registerGlobalSessionEventListener(IDatabaseSessionEventListener... listeners) {
        globalSessionEventListener.addListener(listeners);
        return this;
    }

    @Override
    public IDatabaseSessionEventListener getGlobalSessionEventListener() {
        return globalSessionEventListener;
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