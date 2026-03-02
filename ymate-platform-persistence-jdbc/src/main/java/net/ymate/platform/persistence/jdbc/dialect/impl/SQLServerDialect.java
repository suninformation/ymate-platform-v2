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
package net.ymate.platform.persistence.jdbc.dialect.impl;

import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IShardingable;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.annotation.Dialect;
import net.ymate.platform.persistence.jdbc.dialect.AbstractDialect;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.List;

/**
 * SQLServer2005及以上数据库方言接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2012-4-19 下午3:38:40
 */
@Dialect(value = Type.DATABASE.SQLSERVER, driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver")
public class SQLServerDialect extends AbstractDialect {

    private static final String SELECT = "SELECT";

    private static final String DISTINCT = "DISTINCT";

    public SQLServerDialect() {
        super("[", "]");
    }

    @Override
    public String getName() {
        return Type.DATABASE.SQLSERVER;
    }

    @Override
    public String buildPagedQuerySql(String originSql, int page, int pageSize) {
        int limit = (page - 1) * pageSize;
        String tmpSqlStr = StringUtils.trim(originSql);
        if (Strings.CI.startsWith(tmpSqlStr, SELECT)) {
            tmpSqlStr = StringUtils.trim(StringUtils.substring(tmpSqlStr, SELECT.length()));
        }
        boolean distinct = false;
        if (Strings.CI.startsWith(tmpSqlStr, DISTINCT)) {
            tmpSqlStr = StringUtils.substring(tmpSqlStr, DISTINCT.length());
            distinct = true;
        }
        return ExpressionUtils.bind("SELECT * FROM (SELECT ROW_NUMBER() OVER(ORDER BY __tc__) __rn__, * FROM (SELECT ${_distinct} TOP ${_limit} 0 __tc__, ${_sql}) t) tt WHERE __rn__ > ${_offset}")
                .set("_distinct", distinct ? DISTINCT : StringUtils.EMPTY)
                .set("_limit", String.valueOf(limit + pageSize))
                .set("_sql", tmpSqlStr)
                .set("_offset", String.valueOf(limit)).getResult();
    }

    /**
     * 构建SQLServer的UPSERT语句 (MERGE INTO ... USING ... ON ... WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT ...)
     *
     * @param entityClass  实体模型类
     * @param prefix       实体名称前缀
     * @param shardingable 分片(表)参数对象
     * @param fields       字段名称集合
     * @return UPSERT SQL语句
     * @since 2.1.4
     */
    @Override
    @SuppressWarnings("rawtypes")
    public String buildUpsertSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable, Fields fields) {
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        String tableName = buildTableName(prefix, entityMeta, shardingable);
        Fields insertFields = doBuildInsertFields(entityMeta, fields);
        List<String> pkFields = entityMeta.getPrimaryKeys();

        String usingClause = doBuildMergeUsingClause(entityMeta, pkFields);
        String onClause = doBuildMergeOnClause(entityMeta, pkFields);
        String updateSet = doBuildMergeUpdateSet(entityMeta, insertFields);
        String[] insertParts = doBuildMergeInsertParts(insertFields);

        StringBuilder mergeSql = new StringBuilder();
        mergeSql.append("MERGE INTO ${table_name} AS target ");
        mergeSql.append("USING (SELECT ${using_clause}) AS source ");
        mergeSql.append("ON ${on_clause} ");
        if (!updateSet.isEmpty()) {
            mergeSql.append("WHEN MATCHED THEN UPDATE SET ${update_set} ");
        }
        mergeSql.append("WHEN NOT MATCHED THEN INSERT (${insert_columns}) ");
        mergeSql.append("VALUES (${insert_values})");

        return ExpressionUtils.bind(mergeSql.toString())
                .set("table_name", tableName)
                .set("using_clause", usingClause)
                .set("on_clause", onClause)
                .set("update_set", updateSet)
                .set("insert_columns", insertParts[0])
                .set("insert_values", insertParts[1])
                .getResult();
    }

    /**
     * SQLServer: MERGE INTO ... WHEN NOT MATCHED THEN INSERT ...
     */
    @Override
    @SuppressWarnings("rawtypes")
    public String buildInsertIfNotExistSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable, Fields fields) {
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        String tableName = buildTableName(prefix, entityMeta, shardingable);
        Fields insertFields = doBuildInsertFields(entityMeta, fields);
        List<String> pkFields = entityMeta.getPrimaryKeys();

        String usingClause = doBuildMergeUsingClause(entityMeta, pkFields);
        String onClause = doBuildMergeOnClause(entityMeta, pkFields);
        String[] insertParts = doBuildMergeInsertParts(insertFields);

        return ExpressionUtils.bind("MERGE INTO ${table_name} AS target USING (SELECT ${using_clause}) AS source ON ${on_clause} WHEN NOT MATCHED THEN INSERT (${insert_columns}) VALUES (${insert_values})")
                .set("table_name", tableName)
                .set("using_clause", usingClause)
                .set("on_clause", onClause)
                .set("insert_columns", insertParts[0])
                .set("insert_values", insertParts[1])
                .getResult();
    }
}
