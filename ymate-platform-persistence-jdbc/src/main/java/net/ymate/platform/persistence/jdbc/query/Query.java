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
import net.ymate.platform.core.persistence.IShardingRule;
import net.ymate.platform.core.persistence.IShardingable;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @param <T> 当前实现类类型
 * @author 刘镇 (suninformation@163.com) on 2017/12/14 下午11:43
 */
public class Query<T> extends QueryHandleAdapter<T> {

    private static final Log LOG = LogFactory.getLog(Query.class);

    public static final String LINE_END_FLAG = ",";

    private final IDatabase owner;

    private String dataSourceName;

    private IDialect dialect;

    private IShardingRule shardingRule;

    private IShardingable shardingable;

    public Query(IDatabase owner, String dataSourceName) {
        this.owner = owner;
        this.dataSourceName = dataSourceName;
    }

    public IDatabase owner() {
        return owner;
    }

    public String dataSourceName() {
        return StringUtils.isNotBlank(dataSourceName) ? dataSourceName : owner.getConfig().getDefaultDataSourceName();
    }

    @SuppressWarnings("unchecked")
    public T dataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
        return (T) this;
    }

    /**
     * @return 返回当前数据库方言，若未设置则返回默认数据源配置的方言
     */
    public IDialect dialect() {
        if (dialect == null) {
            try (IDatabaseConnectionHolder connectionHolder = owner.getConnectionHolder(dataSourceName())) {
                dialect = connectionHolder.getDialect();
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
        return (T) this;
    }

    public IShardingRule shardingRule() {
        return shardingRule;
    }

    @SuppressWarnings("unchecked")
    public T shardingRule(IShardingRule shardingRule) {
        this.shardingRule = shardingRule;
        return (T) this;
    }

    public IShardingable shardingable() {
        return shardingable;
    }

    @SuppressWarnings("unchecked")
    public T shardingable(IShardingable shardingable) {
        this.shardingable = shardingable;
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
            return dialect().buildTableName(prefix, tableName, shardingRule(), shardingable());
        }
        if (StringUtils.isNotBlank(prefix) && StringUtils.startsWith(tableName, prefix)) {
            prefix = StringUtils.EMPTY;
        }
        return StringUtils.trimToEmpty(prefix).concat(tableName);
    }

    protected String buildSafeTableName(String prefix, EntityMeta entityMeta, boolean safePrefix) {
        if (safePrefix) {
            return dialect().buildTableName(prefix, entityMeta, shardingable());
        }
        if (StringUtils.isNotBlank(prefix) && StringUtils.startsWith(entityMeta.getEntityName(), prefix)) {
            prefix = StringUtils.EMPTY;
        }
        return StringUtils.trimToEmpty(prefix).concat(entityMeta.getEntityName());
    }

    protected Fields wrapIdentifierFields(String... fields) {
        if (!this.dialect().hasIdentifierQuote()) {
            return Fields.create(fields);
        }
        Fields returnValue = Fields.create();
        if (fields != null) {
            for (String field : fields) {
                returnValue.add(wrapIdentifierField(field));
            }
        }
        return returnValue;
    }

    protected String wrapIdentifierField(String field) {
        return wrapIdentifierField(dialect(), field);
    }

    public static String wrapIdentifierField(IDialect dialect, String field) {
        if (dialect.hasIdentifierQuote()) {
            String[] splits = StringUtils.split(field, ".");
            if (splits != null && splits.length > 0) {
                if (splits.length == 2) {
                    String[] alias = StringUtils.split(splits[1]);
                    if (alias != null && alias.length == 2) {
                        return String.format("%s.%s %s", splits[0], dialect.wrapIdentifierQuote(alias[0]), alias[1]);
                    }
                    return String.format("%s.%s", splits[0], dialect.wrapIdentifierQuote(splits[1]));
                } else if (splits.length == 1) {
                    return dialect.wrapIdentifierQuote(splits[0]);
                }
            }
        }
        return field;
    }

    public static String wrapIdentifierField(IDatabaseConnectionHolder connectionHolder, String field) {
        return wrapIdentifierField(connectionHolder.getDialect(), field);
    }
}
