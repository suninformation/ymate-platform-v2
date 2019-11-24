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
import com.mongodb.client.ClientSession;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.IModuleConfigurer;
import net.ymate.platform.core.persistence.AbstractTrade;
import net.ymate.platform.core.persistence.IDataSourceRouter;
import net.ymate.platform.core.persistence.ITrade;
import net.ymate.platform.persistence.mongodb.impl.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/21 上午9:24
 */
public class MongoDB implements IModule, IMongo {

    private static volatile IMongo instance;

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
                IModuleConfigurer moduleConfigurer = owner.getConfigurer().getModuleConfigurer(MODULE_NAME);
                config = moduleConfigurer == null ? DefaultMongoConfig.defaultConfig() : DefaultMongoConfig.create(moduleConfigurer);
            }
            //
            if (!config.isInitialized()) {
                config.initialize(this);
            }
            //
            for (Map.Entry<String, IMongoDataSourceConfig> entry : config.getDataSourceConfigs().entrySet()) {
                IMongoDataSourceAdapter dataSourceAdapter = new MongoDataSourceAdapter();
                dataSourceAdapter.initialize(this, entry.getValue());
                // 将数据源适配器放入缓存
                dataSourceCaches.put(entry.getKey(), dataSourceAdapter);
            }
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

    @Override
    public IMongoConnectionHolder getDefaultConnectionHolder() throws Exception {
        return getConnectionHolder(config.getDefaultDataSourceName());
    }

    @Override
    public IMongoConnectionHolder getConnectionHolder(String dataSourceName) throws Exception {
        IMongoDataSourceAdapter dataSourceAdapter = dataSourceCaches.get(dataSourceName);
        if (dataSourceAdapter == null) {
            throw new IllegalStateException("Datasource '" + dataSourceName + "' not found.");
        }
        return new DefaultMongoConnectionHolder(dataSourceAdapter);
    }

    @Override
    public void releaseConnectionHolder(IMongoConnectionHolder connectionHolder) throws Exception {
        if (connectionHolder != null) {
            connectionHolder.close();
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
    public IMongoDataSourceAdapter getDefaultDataSourceAdapter() throws Exception {
        return dataSourceCaches.get(config.getDefaultDataSourceName());
    }

    @Override
    public IMongoDataSourceAdapter getDataSourceAdapter(String dataSourceName) throws Exception {
        return dataSourceCaches.get(dataSourceName);
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
    public <T> T openSession(IMongoConnectionHolder databaseHolder, IMongoSessionExecutor<T> executor) throws Exception {
        try (IMongoSession session = new DefaultMongoSession(this, databaseHolder)) {
            return executor.execute(session);
        }
    }

    @Override
    public <T> T openSession(IDataSourceRouter dataSourceRouter, IMongoSessionExecutor<T> executor) throws Exception {
        return openSession(getConnectionHolder(dataSourceRouter.getDataSourceName()), executor);
    }

    @Override
    public <T> T openSession(IGridFsSessionExecutor<T> executor) throws Exception {
        return openSession(getDefaultDataSourceAdapter(), null, executor);
    }

    @Override
    public <T> T openSession(String bucketName, IGridFsSessionExecutor<T> executor) throws Exception {
        return openSession(getDefaultDataSourceAdapter(), bucketName, executor);
    }

    @Override
    public <T> T openSession(IMongoDataSourceAdapter dataSourceAdapter, IGridFsSessionExecutor<T> executor) throws Exception {
        return openSession(dataSourceAdapter, null, executor);
    }

    @Override
    public <T> T openSession(IDataSourceRouter dataSourceRouter, IGridFsSessionExecutor<T> executor) throws Exception {
        return openSession(getDataSourceAdapter(dataSourceRouter.getDataSourceName()), null, executor);
    }

    @Override
    public <T> T openSession(IMongoDataSourceAdapter dataSourceAdapter, String bucketName, IGridFsSessionExecutor<T> executor) throws Exception {
        try (IGridFsSession fsSession = new MongoGridFsSession(dataSourceAdapter, bucketName)) {
            return executor.execute(fsSession);
        }
    }

    @Override
    public <T> T openSession(IDataSourceRouter dataSourceRouter, String bucketName, IGridFsSessionExecutor<T> executor) throws Exception {
        return openSession(getDataSourceAdapter(dataSourceRouter.getDataSourceName()), bucketName, executor);
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
    public void openTransaction(IMongoConnectionHolder databaseHolder, ITrade trade) throws Exception {
        openTransaction(databaseHolder, trade, null);
    }

    @Override
    public void openTransaction(IMongoConnectionHolder databaseHolder, ITrade trade, ClientSessionOptions clientSessionOptions) throws Exception {
        try (ClientSession clientSession = databaseHolder.getDataSourceAdapter().getConnection().startSession(clientSessionOptions != null ? clientSessionOptions : ClientSessionOptions.builder().build())) {
            try {
                trade.deal();
                clientSession.commitTransaction();
            } catch (Throwable throwable) {
                clientSession.abortTransaction();
            }
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
    public <T> T openTransaction(AbstractTrade<T> trade) throws Exception {
        return openTransaction(getDefaultConnectionHolder(), trade, null);
    }

    @Override
    public <T> T openTransaction(AbstractTrade<T> trade, ClientSessionOptions clientSessionOptions) throws Exception {
        return openTransaction(getDefaultConnectionHolder(), trade, clientSessionOptions);
    }

    @Override
    public <T> T openTransaction(IMongoConnectionHolder databaseHolder, AbstractTrade<T> trade, ClientSessionOptions clientSessionOptions) throws Exception {
        try (ClientSession clientSession = databaseHolder.getDataSourceAdapter().getConnection().startSession(clientSessionOptions != null ? clientSessionOptions : ClientSessionOptions.builder().build())) {
            try {
                trade.deal();
                clientSession.commitTransaction();
                return trade.getResult();
            } catch (Throwable throwable) {
                clientSession.abortTransaction();
                throw new Exception(RuntimeUtils.unwrapThrow(throwable));
            }
        }
    }

    @Override
    public <T> T openTransaction(IMongoConnectionHolder databaseHolder, AbstractTrade<T> trade) throws Exception {
        return openTransaction(databaseHolder, trade, null);
    }

    @Override
    public <T> T openTransaction(IDataSourceRouter dataSourceRouter, AbstractTrade<T> trade) throws Exception {
        return openTransaction(getConnectionHolder(dataSourceRouter.getDataSourceName()), trade, null);
    }

    @Override
    public <T> T openTransaction(IDataSourceRouter dataSourceRouter, AbstractTrade<T> trade, ClientSessionOptions clientSessionOptions) throws Exception {
        return openTransaction(getConnectionHolder(dataSourceRouter.getDataSourceName()), trade, clientSessionOptions);
    }
}
