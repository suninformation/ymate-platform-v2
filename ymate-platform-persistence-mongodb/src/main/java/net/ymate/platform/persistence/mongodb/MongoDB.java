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
package net.ymate.platform.persistence.mongodb;

import com.mongodb.ClientSessionOptions;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationConfigureFactory;
import net.ymate.platform.core.IApplicationConfigurer;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.module.impl.DefaultModuleConfigurer;
import net.ymate.platform.core.persistence.AbstractTrade;
import net.ymate.platform.core.persistence.IDataSourceRouter;
import net.ymate.platform.core.persistence.ITrade;
import net.ymate.platform.persistence.mongodb.impl.*;
import net.ymate.platform.persistence.mongodb.transaction.ITransaction;
import net.ymate.platform.persistence.mongodb.transaction.Transactions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/21 上午9:24
 */
public class MongoDB implements IModule, IMongo {

    private static volatile IMongo instance;

    private static final ReentrantLockHelper LOCKER = new ReentrantLockHelper();

    private IApplication owner;

    private IMongoConfig config;

    private Map<String, IMongoDataSourceAdapter> dataSourceCaches = new ConcurrentHashMap<>();

    private boolean initialized;

    public static IMongo get() {
        IMongo inst = instance;
        if (inst == null) {
            synchronized (MongoDB.class) {
                inst = instance;
                if (inst == null) {
                    instance = inst = YMP.get().getModuleManager().getModule(MongoDB.class);
                }
            }
        }
        return inst;
    }

    public MongoDB() {
    }

    public MongoDB(IMongoConfig config) {
        this.config = config;
    }

    @Override
    public String getName() {
        return IMongo.MODULE_NAME;
    }

    @Override
    public void initialize(IApplication owner) throws Exception {
        if (!initialized) {
            //
            YMP.showModuleVersion("ymate-platform-persistence-mongodb", this);
            //
            this.owner = owner;
            //
            if (config == null) {
                IApplicationConfigureFactory configureFactory = owner.getConfigureFactory();
                if (configureFactory != null) {
                    IApplicationConfigurer configurer = configureFactory.getConfigurer();
                    IModuleConfigurer moduleConfigurer = configurer == null ? null : configurer.getModuleConfigurer(MODULE_NAME);
                    if (moduleConfigurer != null) {
                        config = DefaultMongoConfig.create(configureFactory.getMainClass(), moduleConfigurer);
                    } else {
                        config = DefaultMongoConfig.create(configureFactory.getMainClass(), DefaultModuleConfigurer.createEmpty(MODULE_NAME));
                    }
                }
                if (config == null) {
                    config = DefaultMongoConfig.defaultConfig();
                }
            }
            //
            if (!config.isInitialized()) {
                config.initialize(this);
            }
            // 处理设置为自动连接的数据源
            config.getDataSourceConfigs()
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().isAutoConnection())
                    .map(Map.Entry::getKey)
                    .forEach(this::doSafeGetDataSourceAdapter);
            //
            initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public IApplication getOwner() {
        return owner;
    }

    @Override
    public IMongoConfig getConfig() {
        return config;
    }

    private IMongoDataSourceAdapter doSafeGetDataSourceAdapter(String dataSourceName) {
        IMongoDataSourceAdapter dataSourceAdapter = dataSourceCaches.get(dataSourceName);
        if (dataSourceAdapter == null) {
            ReentrantLock lock = null;
            try {
                lock = LOCKER.getLocker(dataSourceName);
                lock.lock();
                IMongoDataSourceConfig dataSourceConfig = config.getDataSourceConfig(dataSourceName);
                if (dataSourceConfig != null) {
                    if (!dataSourceConfig.isInitialized()) {
                        dataSourceConfig.initialize(this);
                    }
                    // 实例化数据源适配器并放入缓存
                    dataSourceAdapter = dataSourceCaches.get(dataSourceName);
                    if (dataSourceAdapter == null) {
                        dataSourceAdapter = new MongoDataSourceAdapter();
                    }
                    if (!dataSourceAdapter.isInitialized()) {
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
    public IMongoConnectionHolder getDefaultConnectionHolder() throws Exception {
        return getConnectionHolder(config.getDefaultDataSourceName());
    }

    @Override
    public IMongoConnectionHolder getConnectionHolder(String dataSourceName) throws Exception {
        IMongoConnectionHolder connectionHolder;
        ITransaction transaction = Transactions.get();
        if (transaction != null) {
            connectionHolder = transaction.getConnectionHolder(dataSourceName);
        } else {
            connectionHolder = new DefaultMongoConnectionHolder(doSafeGetDataSourceAdapter(dataSourceName));
        }
        return connectionHolder;
    }

    @Override
    public void releaseConnectionHolder(IMongoConnectionHolder connectionHolder) throws Exception {
        if (Transactions.get() == null) {
            if (connectionHolder != null) {
                connectionHolder.close();
            }
        }
    }

    @Override
    public IMongoSession openSession() throws Exception {
        return openSession(config.getDefaultDataSourceName());
    }

    @Override
    public IMongoSession openSession(String dataSourceName) throws Exception {
        return new DefaultMongoSession(this, getConnectionHolder(dataSourceName));
    }

    @Override
    public IMongoSession openSession(IMongoConnectionHolder connectionHolder) throws Exception {
        return new DefaultMongoSession(this, connectionHolder);
    }

    @Override
    public IMongoSession openSession(IDataSourceRouter dataSourceRouter) throws Exception {
        return openSession(dataSourceRouter.getDataSourceName());
    }

    @Override
    public IMongoDataSourceAdapter getDefaultDataSourceAdapter() {
        return doSafeGetDataSourceAdapter(config.getDefaultDataSourceName());
    }

    @Override
    public IMongoDataSourceAdapter getDataSourceAdapter(String dataSourceName) {
        return doSafeGetDataSourceAdapter(dataSourceName);
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            //
            for (IMongoDataSourceAdapter adapter : dataSourceCaches.values()) {
                adapter.close();
            }
            dataSourceCaches = null;
            config = null;
            owner = null;
        }
    }

    @Override
    public <T> T openSession(IMongoSessionExecutor<T> executor) throws Exception {
        return openSession(getDefaultConnectionHolder(), executor);
    }

    @Override
    public <T> T openSession(IMongoConnectionHolder connectionHolder, IMongoSessionExecutor<T> executor) throws Exception {
        try (IMongoSession session = new DefaultMongoSession(this, connectionHolder)) {
            return executor.execute(session);
        }
    }

    @Override
    public <T> T openSession(String dataSourceName, IMongoSessionExecutor<T> executor) throws Exception {
        return openSession(getConnectionHolder(dataSourceName), executor);
    }

    @Override
    public <T> T openSession(IDataSourceRouter dataSourceRouter, IMongoSessionExecutor<T> executor) throws Exception {
        return openSession(getConnectionHolder(dataSourceRouter.getDataSourceName()), executor);
    }

    @Override
    public <T> T openGridFsSession(IGridFsSessionExecutor<T> executor) throws Exception {
        return openGridFsSession(getDefaultConnectionHolder(), null, executor);
    }

    @Override
    public <T> T openGridFsSession(String bucketName, IGridFsSessionExecutor<T> executor) throws Exception {
        return openGridFsSession(getDefaultConnectionHolder(), bucketName, executor);
    }

    @Override
    public <T> T openGridFsSession(String dataSourceName, String bucketName, IGridFsSessionExecutor<T> executor) throws Exception {
        return openGridFsSession(getConnectionHolder(dataSourceName), bucketName, executor);
    }

    @Override
    public <T> T openGridFsSession(IMongoConnectionHolder connectionHolder, IGridFsSessionExecutor<T> executor) throws Exception {
        return openGridFsSession(connectionHolder, null, executor);
    }

    @Override
    public <T> T openGridFsSession(IDataSourceRouter dataSourceRouter, IGridFsSessionExecutor<T> executor) throws Exception {
        return openGridFsSession(getConnectionHolder(dataSourceRouter.getDataSourceName()), null, executor);
    }

    @Override
    public <T> T openGridFsSession(IMongoConnectionHolder connectionHolder, String bucketName, IGridFsSessionExecutor<T> executor) throws Exception {
        try (IGridFsSession fsSession = new MongoGridFsSession(connectionHolder, bucketName)) {
            return executor.execute(fsSession);
        }
    }

    @Override
    public <T> T openGridFsSession(IDataSourceRouter dataSourceRouter, String bucketName, IGridFsSessionExecutor<T> executor) throws Exception {
        return openGridFsSession(getConnectionHolder(dataSourceRouter.getDataSourceName()), bucketName, executor);
    }

    @Override
    public IGridFsSession openGridFsSession() throws Exception {
        return new MongoGridFsSession(getDefaultConnectionHolder());
    }

    @Override
    public IGridFsSession openGridFsSession(String bucketName) throws Exception {
        return new MongoGridFsSession(getDefaultConnectionHolder(), bucketName);
    }

    @Override
    public IGridFsSession openGridFsSession(String dataSourceName, String bucketName) throws Exception {
        return new MongoGridFsSession(getConnectionHolder(dataSourceName), bucketName);
    }

    @Override
    public IGridFsSession openGridFsSession(IMongoConnectionHolder connectionHolder, String bucketName) throws Exception {
        return new MongoGridFsSession(connectionHolder, bucketName);
    }

    @Override
    public IGridFsSession openGridFsSession(IMongoConnectionHolder connectionHolder) throws Exception {
        return new MongoGridFsSession(connectionHolder);
    }

    @Override
    public IGridFsSession openGridFsSession(IDataSourceRouter dataSourceRouter, String bucketName) throws Exception {
        return new MongoGridFsSession(getConnectionHolder(dataSourceRouter.getDataSourceName()), bucketName);
    }

    @Override
    public IGridFsSession openGridFsSession(IDataSourceRouter dataSourceRouter) throws Exception {
        return new MongoGridFsSession(getConnectionHolder(dataSourceRouter.getDataSourceName()));
    }

    @Override
    public void openTransaction(ITrade trade) throws Exception {
        openTransaction(getDefaultConnectionHolder(), trade, null);

    }

    @Override
    public void openTransaction(ITrade trade, ClientSessionOptions clientSessionOptions) throws Exception {
        openTransaction(getDefaultConnectionHolder(), trade, clientSessionOptions);
    }

    @Override
    public void openTransaction(IMongoConnectionHolder connectionHolder, ITrade trade) throws Exception {
        openTransaction(connectionHolder, trade, null);
    }

    @Override
    public void openTransaction(IMongoConnectionHolder connectionHolder, ITrade trade, ClientSessionOptions clientSessionOptions) throws Exception {
        try {
            Transactions.create(connectionHolder, clientSessionOptions);
            trade.deal();
            Transactions.commit();
        } catch (Throwable throwable) {
            Transactions.rollback();
            if (throwable instanceof Exception) {
                throw (Exception) throwable;
            } else {
                throw new Exception(RuntimeUtils.unwrapThrow(throwable));
            }
        } finally {
            Transactions.close();
        }
    }

    @Override
    public void openTransaction(IDataSourceRouter dataSourceRouter, ITrade trade) throws Exception {
        openTransaction(getConnectionHolder(dataSourceRouter.getDataSourceName()), trade, null);
    }

    @Override
    public void openTransaction(IDataSourceRouter dataSourceRouter, ITrade trade, ClientSessionOptions clientSessionOptions) throws Exception {
        openTransaction(getConnectionHolder(dataSourceRouter.getDataSourceName()), trade, clientSessionOptions);
    }

    @Override
    public void openTransaction(String dataSourceName, ITrade trade) throws Exception {
        openTransaction(getConnectionHolder(dataSourceName), trade, null);
    }

    @Override
    public void openTransaction(String dataSourceName, ITrade trade, ClientSessionOptions clientSessionOptions) throws Exception {
        openTransaction(getConnectionHolder(dataSourceName), trade, clientSessionOptions);
    }

    @Override
    public <T> T openTransaction(AbstractTrade<T> trade) throws Exception {
        return openTransaction(getDefaultConnectionHolder(), trade, null);
    }

    @Override
    public <T> T openTransaction(AbstractTrade<T> trade, ClientSessionOptions clientSessionOptions) throws Exception {
        return openTransaction(getDefaultConnectionHolder(), trade, clientSessionOptions);
    }

    @Override
    public <T> T openTransaction(IMongoConnectionHolder connectionHolder, AbstractTrade<T> trade, ClientSessionOptions clientSessionOptions) throws Exception {
        openTransaction(connectionHolder, (ITrade) trade, clientSessionOptions);
        return trade.getResult();
    }

    @Override
    public <T> T openTransaction(IMongoConnectionHolder connectionHolder, AbstractTrade<T> trade) throws Exception {
        return openTransaction(connectionHolder, trade, null);
    }

    @Override
    public <T> T openTransaction(IDataSourceRouter dataSourceRouter, AbstractTrade<T> trade) throws Exception {
        return openTransaction(getConnectionHolder(dataSourceRouter.getDataSourceName()), trade, null);
    }

    @Override
    public <T> T openTransaction(IDataSourceRouter dataSourceRouter, AbstractTrade<T> trade, ClientSessionOptions clientSessionOptions) throws Exception {
        return openTransaction(getConnectionHolder(dataSourceRouter.getDataSourceName()), trade, clientSessionOptions);
    }

    @Override
    public <T> T openTransaction(String dataSourceName, AbstractTrade<T> trade) throws Exception {
        return openTransaction(getConnectionHolder(dataSourceName), trade, null);
    }

    @Override
    public <T> T openTransaction(String dataSourceName, AbstractTrade<T> trade, ClientSessionOptions clientSessionOptions) throws Exception {
        return openTransaction(getConnectionHolder(dataSourceName), trade, clientSessionOptions);
    }
}
