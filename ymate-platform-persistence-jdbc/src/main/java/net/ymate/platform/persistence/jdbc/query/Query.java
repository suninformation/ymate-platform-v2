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
package net.ymate.platform.persistence.jdbc.query;

import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IShardingable;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.IDatabaseSession;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @param <T> 当前实现类类型
 * @author 刘镇 (suninformation@163.com) on 2017/12/14 下午11:43
 */
public class Query<T> {

    private static final Log LOG = LogFactory.getLog(Query.class);

    private final IDatabase owner;

    private String tablePrefix;

    private IDialect dialect;

    private IShardingable shardingable;

    public Query(IDatabase owner) {
        this.owner = owner;
    }

    public IDatabase owner() {
        return owner;
    }

    /**
     * @return 返回表前缀，若未设置则返回默认数据源配置的表前缀
     */
    public String tablePrefix() {
        if (StringUtils.isBlank(tablePrefix)) {
            try (IDatabaseSession session = owner.openSession()) {
                if (session != null) {
                    tablePrefix = session.getConnectionHolder().getDataSourceConfig().getTablePrefix();
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return tablePrefix;
    }

    @SuppressWarnings("unchecked")
    public T tablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
        //
        return (T) this;
    }

    /**
     * @return 返回当前数据库方言，若未设置则返回默认数据源配置的方言
     */
    public IDialect dialect() {
        if (dialect == null) {
            try (IDatabaseSession session = owner.openSession()) {
                if (session != null) {
                    dialect = session.getConnectionHolder().getDialect();
                }
            } catch (Exception e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                }
            }
        }
        return dialect;
    }

    @SuppressWarnings("unchecked")
    public T dialect(IDialect dialect) {
        this.dialect = dialect;
        //
        return (T) this;
    }

    public IShardingable shardingable() {
        return shardingable;
    }

    @SuppressWarnings("unchecked")
    public T shardingable(IShardingable shardingable) {
        this.shardingable = shardingable;
        //
        return (T) this;
    }

    // ----------

    protected Fields checkFieldExcluded(Fields fields) {
        if (fields.isExcluded()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Query fields do not support exclusion and have been cleaned up.");
            }
            return Fields.create();
        }
        return fields;
    }

    protected String buildSafeTableName(String prefix, String tableName, boolean safePrefix) {
        if (safePrefix) {
            prefix = StringUtils.defaultIfBlank(prefix, this.tablePrefix());
            return this.dialect().wrapIdentifierQuote(StringUtils.trimToEmpty(prefix).concat(tableName));
        }
        return StringUtils.trimToEmpty(prefix).concat(tableName);
    }

    protected String buildSafeTableName(String prefix, EntityMeta entityMeta, boolean safePrefix) {
        if (safePrefix) {
            prefix = StringUtils.defaultIfBlank(prefix, this.tablePrefix());
            return this.dialect().buildTableName(prefix, entityMeta, this.shardingable());
        }
        return StringUtils.trimToEmpty(prefix).concat(entityMeta.getEntityName());
    }

    protected Fields wrapIdentifierFields(String... fields) {
        Fields returnValue = Fields.create();
        if (fields != null) {
            for (String field : fields) {
                returnValue.add(wrapIdentifierField(field));
            }
        }
        return returnValue;
    }

    protected String wrapIdentifierField(String field) {
        return wrapIdentifierField(this.dialect, field);
    }

    public static String wrapIdentifierField(IDialect dialect, String field) {
        String[] splits = StringUtils.split(field, ".");
        if (splits != null && splits.length == 2) {
            String[] alias = StringUtils.split(splits[1]);
            if (alias != null && alias.length == 2) {
                return splits[0] + "." + dialect.wrapIdentifierQuote(alias[0]) + " " + alias[1];
            }
            return splits[0] + "." + dialect.wrapIdentifierQuote(splits[1]);
        }
        return dialect.wrapIdentifierQuote(field);
    }

    public static String wrapIdentifierField(IDatabaseConnectionHolder connectionHolder, String field) {
        return wrapIdentifierField(connectionHolder.getDialect(), field);
    }
}
