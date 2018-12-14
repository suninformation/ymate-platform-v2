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
package net.ymate.platform.persistence.jdbc.impl;

import net.ymate.platform.core.IConfig;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.support.IConfigReader;
import net.ymate.platform.core.support.IPasswordProcessor;
import net.ymate.platform.core.support.impl.MapSafeConfigReader;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.persistence.jdbc.*;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认数据库JDBC持久化模块配置类
 *
 * @author 刘镇 (suninformation@163.com) on 2012-12-29 下午2:19:18
 * @version 1.0
 */
public class DefaultDatabaseModuleCfg implements IDatabaseModuleCfg {

    private final YMP owner;

    private final String dataSourceDefaultName;

    private final Map<String, DataSourceCfgMeta> dataSourceCfgMetas;

    public DefaultDatabaseModuleCfg(YMP owner) throws Exception {
        this.owner = owner;
        //
        IConfigReader _moduleCfg = MapSafeConfigReader.bind(owner.getConfig().getModuleConfigs(IDatabase.MODULE_NAME));
        //
        this.dataSourceDefaultName = _moduleCfg.getString(DS_DEFAULT_NAME, IConfig.DEFAULT_STR);
        //
        this.dataSourceCfgMetas = new HashMap<String, DataSourceCfgMeta>();
        String _dsNameStr = _moduleCfg.getString(DS_NAME_LIST, IConfig.DEFAULT_STR);
        if (StringUtils.contains(_dsNameStr, this.dataSourceDefaultName)) {
            String[] _dsNameList = StringUtils.split(_dsNameStr, "|");
            for (String _dsName : _dsNameList) {
                DataSourceCfgMeta _meta = __doParserDataSourceCfgMeta(_dsName, _moduleCfg.getMap("ds." + _dsName + "."));
                if (_meta != null) {
                    this.dataSourceCfgMetas.put(_dsName, _meta);
                }
            }
        } else {
            throw new IllegalArgumentException("The default datasource name does not match");
        }
    }

    /**
     * @param dsName         数据源名称
     * @param dataSourceCfgs 数据源配置参数映射
     * @return 分析并封装数据源配置
     * @throws Exception 可能产生的异常
     */
    @SuppressWarnings("unchecked")
    private DataSourceCfgMeta __doParserDataSourceCfgMeta(String dsName, Map<String, String> dataSourceCfgs) throws Exception {
        if (!dataSourceCfgs.isEmpty()) {
            IConfigReader _dataSourceCfg = MapSafeConfigReader.bind(dataSourceCfgs);
            //
            DataSourceCfgMeta _meta = new DataSourceCfgMeta();
            _meta.setName(dsName);
            _meta.setConnectionUrl(RuntimeUtils.replaceEnvVariable(_dataSourceCfg.getString(CONNECTION_URL)));
            _meta.setUsername(_dataSourceCfg.getString(USERNAME));
            // 验证必填参数
            if (StringUtils.isNotBlank(_meta.getConnectionUrl()) && StringUtils.isNotBlank(_meta.getUsername())) {
                // 基础参数
                _meta.setIsShowSQL(_dataSourceCfg.getBoolean(SHOW_SQL));
                _meta.setIsStackTraces(_dataSourceCfg.getBoolean(STACK_TRACES));
                _meta.setStackTraceDepth(_dataSourceCfg.getInt(STACK_TRACE_DEPTH));
                _meta.setStackTracePackage(_dataSourceCfg.getString(STACK_TRACE_PACKAGE));
                _meta.setTablePrefix(_dataSourceCfg.getString(TABLE_PREFIX));
                _meta.setIdentifierQuote(_dataSourceCfg.getString(IDENTIFIER_QUOTE));
                // 数据源适配器
                String _adapterClassName = _dataSourceCfg.getString(ADAPTER_CLASS, IConfig.DEFAULT_STR);
                _adapterClassName = StringUtils.defaultIfBlank(JDBC.DS_ADAPTERS.get(_adapterClassName), _adapterClassName);
                _meta.setAdapterClass((Class<? extends IDataSourceAdapter>) ClassUtils.loadClass(_adapterClassName, this.getClass()));
                //
                // 连接和数据库类型
                try {
                    _meta.setType(JDBC.DATABASE.valueOf(StringUtils.trimToEmpty(_dataSourceCfg.getString(TYPE)).toUpperCase()));
                } catch (IllegalArgumentException e) {
                    // 通过连接字符串分析数据库类型
                    String _connUrl = URI.create(_meta.getConnectionUrl()).toString();
                    String[] _type = StringUtils.split(_connUrl, ":");
                    if (_type != null && _type.length > 0) {
                        if ("microsoft".equals(_type[1])) {
                            _meta.setType(JDBC.DATABASE.SQLSERVER);
                        } else {
                            _meta.setType(JDBC.DATABASE.valueOf(_type[1].toUpperCase()));
                        }
                    }
                }
                //
                _meta.setDialectClass(_dataSourceCfg.getString(DIALECT_CLASS));
                _meta.setDriverClass(_dataSourceCfg.getString(DRIVER_CLASS, JDBC.DB_DRIVERS.get(_meta.getType())));
                _meta.setPassword(_dataSourceCfg.getString(PASSWORD));
                _meta.setIsPasswordEncrypted(_dataSourceCfg.getBoolean(PASSWORD_ENCRYPTED));
                //
                String _passwordClass = _dataSourceCfg.getString(PASSWORD_CLASS);
                if (_meta.isPasswordEncrypted()
                        && StringUtils.isNotBlank(_meta.getPassword())
                        && StringUtils.isNotBlank(_passwordClass)) {
                    if (!StringUtils.equals(owner.getConfig().getDefaultPasswordClass().getName(), _passwordClass)) {
                        _meta.setPasswordClass((Class<? extends IPasswordProcessor>) ClassUtils.loadClass(_passwordClass, this.getClass()));
                    }
                }
                //
                return _meta;
            }
        }
        return null;
    }

    @Override
    public String getDataSourceDefaultName() {
        return dataSourceDefaultName;
    }

    @Override
    public Map<String, DataSourceCfgMeta> getDataSourceCfgs() {
        return Collections.unmodifiableMap(dataSourceCfgMetas);
    }

    @Override
    public DataSourceCfgMeta getDefaultDataSourceCfg() {
        return dataSourceCfgMetas.get(dataSourceDefaultName);
    }

    @Override
    public DataSourceCfgMeta getDataSourceCfg(String name) {
        return dataSourceCfgMetas.get(name);
    }
}
