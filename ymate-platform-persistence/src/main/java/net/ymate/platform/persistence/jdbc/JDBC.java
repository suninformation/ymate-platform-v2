/*
 * Copyright 2007-2107 the original author or authors.
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

import net.ymate.platform.core.Version;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.module.IModule;
import net.ymate.platform.core.module.annotation.Module;
import net.ymate.platform.persistence.IPersistence;
import net.ymate.platform.persistence.annotation.Entity;
import net.ymate.platform.persistence.jdbc.impl.DefaultConnectionHolder;
import net.ymate.platform.persistence.jdbc.impl.DefaultModuleCfg;
import net.ymate.platform.persistence.jdbc.impl.DefaultSession;
import net.ymate.platform.persistence.jdbc.support.EntityHandler;
import net.ymate.platform.persistence.jdbc.transaction.Transactions;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据库模块管理器
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-10 下午11:45:25
 * @version 1.0
 */
@Module
public class JDBC implements IModule, IDatabase {

    public static final Version VERSION = new Version(2, 0, 0, IPersistence.class.getPackage().getImplementationVersion(), Version.VersionType.Alphal);

    private static IDatabase __instance;

    private YMP __owner;

    private IDatabaseModuleCfg __moduleCfg;

    private Map<String, IDataSourceAdapter> __dsCaches;

    private boolean __inited;

    /**
     * @return 返回默认数据库模块管理器实例对象
     */
    public static IDatabase get() {
        if (__instance == null) {
            synchronized (VERSION) {
                if (__instance == null) {
                    __instance = YMP.get().getModule(JDBC.class);
                }
            }
        }
        return __instance;
    }

    /**
     * @param owner YMP框架管理器实例
     * @return 返回指定YMP框架管理器容器内的数据库模块管理器实例
     */
    public static IDatabase get(YMP owner) {
        return owner.getModule(JDBC.class);
    }

    public void init(YMP owner) throws Exception {
        if (!__inited) {
            __owner = owner;
            __moduleCfg = new DefaultModuleCfg(owner);
            __owner.registerHandler(Entity.class, new EntityHandler(__owner));
            //
            __dsCaches = new HashMap<String, IDataSourceAdapter>();
            for (DataSourceCfgMeta _meta : __moduleCfg.getDataSourceCfgs().values()) {
                IDataSourceAdapter _adapter = _meta.getAdapterClass().newInstance();
                _adapter.initialize(_meta);
                // 将数据源适配器添加到缓存
                __dsCaches.put(_meta.getName(), _adapter);
            }
            //
            __inited = true;
        }
    }

    public boolean isInited() {
        return __inited;
    }

    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            //
            for (IDataSourceAdapter _adapter : __dsCaches.values()) {
                _adapter.destroy();
            }
            __dsCaches = null;
            __moduleCfg = null;
            __owner = null;
        }
    }

    public IDatabaseModuleCfg getModuleCfg() {
        return __moduleCfg;
    }

    public IConnectionHolder getDefaultConnectionHolder() throws Exception {
        String _defaultDSName = __moduleCfg.getDataSourceDefaultName();
        return getConnectionHolder(_defaultDSName);
    }

    public IConnectionHolder getConnectionHolder(String dsName) throws Exception {
        IConnectionHolder _returnValue = null;
        if (Transactions.get() != null) {
            _returnValue = Transactions.get().getConnectionHolder(dsName);
            if (_returnValue == null) {
                _returnValue = new DefaultConnectionHolder(__dsCaches.get(dsName));
                Transactions.get().registerConnectionHolder(_returnValue);
            }
        } else {
            _returnValue = new DefaultConnectionHolder(__dsCaches.get(dsName));
        }
        return _returnValue;
    }

    public void releaseConnectionHolder(IConnectionHolder connectionHolder) throws Exception {
        // 需要判断当前连接是否参与事务，若存在事务则不进行关闭操作
        if (Transactions.get() == null) {
            if (connectionHolder != null) {
                connectionHolder.release();
            }
        }
    }

    public <T> T openSession(ISessionExecutor<T> executor) throws Exception {
        return openSession(getDefaultConnectionHolder(), executor);
    }

    public <T> T openSession(IConnectionHolder connectionHolder, ISessionExecutor<T> executor) throws Exception {
        ISession _session = new DefaultSession(connectionHolder);
        try {
            return executor.execute(_session);
        } finally {
            _session.close();
        }
    }
}
