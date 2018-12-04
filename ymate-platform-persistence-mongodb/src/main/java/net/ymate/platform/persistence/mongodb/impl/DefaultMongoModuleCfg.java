/*
 * Copyright 2007-2017 the original author or authors.
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
package net.ymate.platform.persistence.mongodb.impl;

import com.mongodb.ServerAddress;
import net.ymate.platform.core.IConfig;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.support.IConfigReader;
import net.ymate.platform.core.support.IPasswordProcessor;
import net.ymate.platform.core.support.impl.MapSafeConfigReader;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IMongoClientOptionsHandler;
import net.ymate.platform.persistence.mongodb.IMongoModuleCfg;
import net.ymate.platform.persistence.mongodb.MongoDataSourceCfgMeta;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/22 上午12:42
 * @version 1.0
 */
public class DefaultMongoModuleCfg implements IMongoModuleCfg {

    private String dataSourceDefaultName;

    private IMongoClientOptionsHandler clientOptionsHandler;

    private Map<String, MongoDataSourceCfgMeta> dataSourceCfgMetas;

    public DefaultMongoModuleCfg(YMP owner) throws Exception {
        IConfigReader _moduleCfg = MapSafeConfigReader.bind(owner.getConfig().getModuleConfigs(IMongo.MODULE_NAME));
        //
        this.dataSourceDefaultName = _moduleCfg.getString(DS_DEFAULT_NAME, IConfig.DEFAULT_STR);
        this.clientOptionsHandler = _moduleCfg.getClassImpl(DS_OPTIONS_HANDLER_CLASS, IMongoClientOptionsHandler.class);
        //
        this.dataSourceCfgMetas = new HashMap<String, MongoDataSourceCfgMeta>();
        String _dsNameStr = _moduleCfg.getString(DS_NAME_LIST, IConfig.DEFAULT_STR);
        if (StringUtils.contains(_dsNameStr, this.dataSourceDefaultName)) {
            String[] _dsNameList = StringUtils.split(_dsNameStr, "|");
            for (String _dsName : _dsNameList) {
                MongoDataSourceCfgMeta _meta = __doParserDataSourceCfgMeta(_dsName, _moduleCfg.getMap("ds." + _dsName + "."));
                if (_meta != null) {
                    this.dataSourceCfgMetas.put(_dsName, _meta);
                }
            }
        } else {
            throw new IllegalArgumentException("The default datasource name does not match");
        }
    }

    @SuppressWarnings("unchecked")
    protected MongoDataSourceCfgMeta __doParserDataSourceCfgMeta(String dsName, Map<String, String> dataSourceCfgs) throws Exception {
        MongoDataSourceCfgMeta _meta = null;
        if (!dataSourceCfgs.isEmpty()) {
            IConfigReader _dataSourceCfg = MapSafeConfigReader.bind(dataSourceCfgs);
            //
            String _connectionUrl = StringUtils.trimToNull(_dataSourceCfg.getString(CONNECTION_URL));
            if (_connectionUrl != null) {
                _meta = new MongoDataSourceCfgMeta(dsName,
                        _dataSourceCfg.getString(COLLECTION_PREFIX),
                        _connectionUrl,
                        _dataSourceCfg.getString(DATABASE_NAME));
            } else {
                List<ServerAddress> _servers = new ArrayList<ServerAddress>();
                String[] _serversArr = _dataSourceCfg.getArray(SERVERS);
                if (_serversArr != null) {
                    for (String _serverStr : _serversArr) {
                        String[] _server = StringUtils.split(_serverStr, ":");
                        if (_server.length > 1) {
                            _servers.add(new ServerAddress(_server[0], Integer.parseInt(_server[1])));
                        } else {
                            _servers.add(new ServerAddress(_server[0]));
                        }
                    }
                }
                //
                boolean _isPwdEncrypted = _dataSourceCfg.getBoolean(PASSWORD_ENCRYPTED);
                Class<? extends IPasswordProcessor> _passwordClass = null;
                if (_isPwdEncrypted) {
                    _passwordClass = (Class<? extends IPasswordProcessor>) ClassUtils.loadClass(_dataSourceCfg.getString(PASSWORD_CLASS), this.getClass());
                }
                _meta = new MongoDataSourceCfgMeta(dsName,
                        _dataSourceCfg.getString(COLLECTION_PREFIX),
                        _servers,
                        _dataSourceCfg.getString(USERNAME),
                        _dataSourceCfg.getString(PASSWORD),
                        _dataSourceCfg.getString(DATABASE_NAME),
                        _isPwdEncrypted,
                        _passwordClass);
            }
        }
        return _meta;
    }

    @Override
    public String getDataSourceDefaultName() {
        return dataSourceDefaultName;
    }

    @Override
    public IMongoClientOptionsHandler getClientOptionsHandler() {
        return clientOptionsHandler;
    }

    @Override
    public Map<String, MongoDataSourceCfgMeta> getDataSourceCfgs() {
        return Collections.unmodifiableMap(dataSourceCfgMetas);
    }

    @Override
    public MongoDataSourceCfgMeta getDefaultDataSourceCfg() {
        return dataSourceCfgMetas.get(dataSourceDefaultName);
    }

    @Override
    public MongoDataSourceCfgMeta getDataSourceCfg(String name) {
        return dataSourceCfgMetas.get(name);
    }
}
