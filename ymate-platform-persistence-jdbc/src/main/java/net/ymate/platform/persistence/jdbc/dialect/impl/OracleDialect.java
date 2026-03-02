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
import net.ymate.platform.core.persistence.base.PropertyMeta;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.annotation.Dialect;
import net.ymate.platform.persistence.jdbc.dialect.AbstractDialect;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Oracle数据库方言接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-30 下午01:55:13
 */
@Dialect(value = Type.DATABASE.ORACLE, driverClass = "oracle.jdbc.OracleDriver")
public class OracleDialect extends AbstractDialect {

    public OracleDialect() {
    }

    @Override
    public String getName() {
        return Type.DATABASE.ORACLE;
    }

    @Override
    public String getSequenceNextValSql(String sequenceName) {
        return sequenceName.concat(".nextval");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String buildInsertSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable, Fields fields) {
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        ExpressionUtils exp = ExpressionUtils.bind("INSERT INTO ${table_name} (${fields}) VALUES (${values})")
                .set("table_name", buildTableName(prefix, entityMeta, shardingable));
        //
        Fields newFields = Fields.create();
        Fields values = Fields.create();
        (fields == null || fields.fields().isEmpty() ? entityMeta.getPropertyNames() : fields.fields()).forEach((fieldName) -> {
            PropertyMeta propertyMeta = entityMeta.getPropertyByName(fieldName);
            if (propertyMeta.isAutoincrement()) {
                //  若主键指定了序列, 则该主建需加到字段集合中
                if (StringUtils.isNotBlank(propertyMeta.getSequenceName())) {
                    newFields.add(fieldName);
                    values.add(getSequenceNextValSql(propertyMeta.getSequenceName()));
                }
            } else {
                newFields.add(fieldName);
                values.add("?");
            }
        });
        doValidProperty(entityMeta, newFields, false);
        return exp.set("fields", doGenerateFieldsFormatStr(newFields, null, null)).set("values", StringUtils.join(values.fields(), ", ")).getResult();
    }

    @Override
    public String buildPagedQuerySql(String originSql, int page, int pageSize) {
        StringBuilder returnValue = new StringBuilder(originSql.length() + 100);
        int limit = (page - 1) * pageSize;
        if (pageSize == 0) {
            returnValue.append("SELECT * FROM ( ").append(originSql).append(" ) WHERE rownum <= ").append(limit);
        } else {
            returnValue.append("SELECT * FROM ( SELECT row_.*, rownum rownum_ FROM ( ").append(originSql);
            returnValue.append(" ) row_ ) WHERE rownum_ > ").append(limit).append(" AND rownum_ <= ").append((limit + pageSize));
        }
        return returnValue.toString();
    }

    /**
     * 构建Oracle的UPSERT语句 (MERGE INTO ... USING ... ON ... WHEN MATCHED THEN UPDATE ... WHEN NOT MATCHED THEN INSERT ...)
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
        mergeSql.append("MERGE INTO ${table_name} target ");
        mergeSql.append("USING (SELECT ${using_clause} FROM DUAL) source ");
        mergeSql.append("ON (${on_clause}) ");
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
     * Oracle: MERGE INTO ... WHEN NOT MATCHED THEN INSERT ...
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

        return ExpressionUtils.bind("MERGE INTO ${table_name} target USING (SELECT ${using_clause} FROM DUAL) source ON (${on_clause}) WHEN NOT MATCHED THEN INSERT (${insert_columns}) VALUES (${insert_values})")
                .set("table_name", tableName)
                .set("using_clause", usingClause)
                .set("on_clause", onClause)
                .set("insert_columns", insertParts[0])
                .set("insert_values", insertParts[1])
                .getResult();
    }
}
