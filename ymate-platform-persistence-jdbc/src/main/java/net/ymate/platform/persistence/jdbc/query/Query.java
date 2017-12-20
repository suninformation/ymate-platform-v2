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
package net.ymate.platform.persistence.jdbc.query;

import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.persistence.Fields;
import net.ymate.platform.persistence.IShardingable;
import net.ymate.platform.persistence.base.EntityMeta;
import net.ymate.platform.persistence.jdbc.IConnectionHolder;
import net.ymate.platform.persistence.jdbc.ISession;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/12/14 下午11:43
 * @version 1.0
 */
public class Query<T> {

    private static final Log _LOG = LogFactory.getLog(Query.class);

    private String __tablePrefix;

    private IDialect __dialect;

    private IShardingable __shardingable;

    @SuppressWarnings("unchecked")
    public T set(ISession session) {
        return set(session.getConnectionHolder());
    }

    @SuppressWarnings("unchecked")
    public T set(IConnectionHolder connectionHolder) {
        __tablePrefix = connectionHolder.getDataSourceCfgMeta().getTablePrefix();
        __dialect = connectionHolder.getDialect();
        //
        return (T) this;
    }

    /**
     * @return 返回表前缀，若未设置则返回默认数据源配置的表前缀
     */
    public String tablePrefix() {
        if (StringUtils.isBlank(__tablePrefix)) {
            ISession _session = null;
            try {
                _session = JDBC.get().openSession();
                if (_session != null) {
                    __tablePrefix = _session.getConnectionHolder().getDataSourceCfgMeta().getTablePrefix();
                }
            } catch (Exception e) {
                _LOG.warn("", RuntimeUtils.unwrapThrow(e));
            } finally {
                if (_session != null) {
                    _session.close();
                }
            }
        }
        return __tablePrefix;
    }

    @SuppressWarnings("unchecked")
    public T tablePrefix(String tablePrefix) {
        __tablePrefix = tablePrefix;
        //
        return (T) this;
    }

    /**
     * @return 返回当前数据库方言，若未设置则返回默认数据源配置的方言
     */
    public IDialect dialect() {
        if (__dialect == null) {
            ISession _session = null;
            try {
                _session = JDBC.get().openSession();
                if (_session != null) {
                    __dialect = _session.getConnectionHolder().getDialect();
                }
            } catch (Exception e) {
                _LOG.warn("", RuntimeUtils.unwrapThrow(e));
            } finally {
                if (_session != null) {
                    _session.close();
                }
            }
        }
        return __dialect;
    }

    @SuppressWarnings("unchecked")
    public T dialect(IDialect dialect) {
        __dialect = dialect;
        //
        return (T) this;
    }

    public IShardingable shardingable() {
        return __shardingable;
    }

    @SuppressWarnings("unchecked")
    public T shardingable(IShardingable shardingable) {
        __shardingable = shardingable;
        //
        return (T) this;
    }

    // ----------

    protected String __buildSafeTableName(String prefix, String tableName, boolean safePrefix) {
        if (safePrefix) {
            prefix = StringUtils.defaultIfBlank(prefix, this.tablePrefix());
            return this.dialect().wrapIdentifierQuote(StringUtils.trimToEmpty(prefix).concat(tableName));
        }
        return StringUtils.trimToEmpty(prefix).concat(tableName);
    }

    protected String __buildSafeTableName(String prefix, EntityMeta entityMeta, boolean safePrefix) {
        if (safePrefix) {
            prefix = StringUtils.defaultIfBlank(prefix, this.tablePrefix());
            return this.dialect().buildTableName(prefix, entityMeta, this.shardingable());
        }
        return StringUtils.trimToEmpty(prefix).concat(entityMeta.getEntityName());
    }

    protected Fields __wrapIdentifierFields(String... fields) {
        Fields _returnValue = Fields.create();
        if (fields != null) {
            for (String _field : fields) {
                String[] _split = StringUtils.split(_field, ".");
                if (_split != null) {
                    if (_split.length == 2) {
                        _returnValue.add(_split[0] + "." + this.dialect().wrapIdentifierQuote(_split[1]));
                    } else {
                        _returnValue.add(this.dialect().wrapIdentifierQuote(_field));
                    }
                }
            }
        }
        return _returnValue;
    }

    protected String __wrapIdentifierField(String field) {
        return this.dialect().wrapIdentifierQuote(field);
    }

    public static String wrapIdentifierField(IConnectionHolder connectionHolder, String field) {
        String[] _split = StringUtils.split(field, ".");
        if (_split != null && _split.length == 2) {
            return _split[0] + "." + connectionHolder.getDialect().wrapIdentifierQuote(_split[1]);
        }
        return connectionHolder.getDialect().wrapIdentifierQuote(field);
    }
}
