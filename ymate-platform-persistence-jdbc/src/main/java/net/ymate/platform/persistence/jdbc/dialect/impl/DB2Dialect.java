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

import java.util.List;

/**
 * DB2数据库方言接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/4/16 上午11:26
 */
@Dialect(value = Type.DATABASE.DB2, driverClass = "com.ibm.db2.jcc.DB2Driver")
public class DB2Dialect extends AbstractDialect {

    @Override
    public String getName() {
        return Type.DATABASE.DB2;
    }

    @Override
    public String buildPagedQuerySql(String originSql, int page, int pageSize) {
        int limit = ((page - 1) * pageSize);
        return ExpressionUtils.bind("SELECT * FROM (SELECT ROW_NUMBER() OVER() AS __rn__, __row__.* FROM (${_sql}) AS __row__) WHERE __rn__ BETWEEN ${_limit} AND ${_offset}")
                .set("_limit", String.valueOf(limit + pageSize))
                .set("_sql", originSql)
                .set("_offset", String.valueOf(limit)).getResult();
    }

    /**
     * 构建DB2的UPSERT语句 (MERGE INTO ... USING ... ON ... WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT ...)
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
        mergeSql.append("USING (SELECT ${using_clause} FROM SYSIBM.SYSDUMMY1) AS source ");
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
     * DB2: MERGE INTO ... WHEN NOT MATCHED THEN INSERT ...
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

        return ExpressionUtils.bind("MERGE INTO ${table_name} AS target USING (SELECT ${using_clause} FROM SYSIBM.SYSDUMMY1) AS source ON ${on_clause} WHEN NOT MATCHED THEN INSERT (${insert_columns}) VALUES (${insert_values})")
                .set("table_name", tableName)
                .set("using_clause", usingClause)
                .set("on_clause", onClause)
                .set("insert_columns", insertParts[0])
                .set("insert_values", insertParts[1])
                .getResult();
    }
}
