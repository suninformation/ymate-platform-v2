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

import java.util.Iterator;
import java.util.List;

/**
 * PostgreSQL数据库方言接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/4/16 上午11:29
 */
@Dialect(value = Type.DATABASE.POSTGRESQL, driverClass = "org.postgresql.Driver")
public class PostgreSQLDialect extends AbstractDialect {

    public PostgreSQLDialect() {
        super("\"", "\"");
    }

    @Override
    public String getName() {
        return Type.DATABASE.POSTGRESQL;
    }

    @Override
    public String buildPagedQuerySql(String originSql, int page, int pageSize) {
        int limit = (page - 1) * pageSize;
        if (pageSize == 0) {
            return String.format("%s LIMIT %d", originSql, limit);
        }
        return String.format("%s LIMIT %d OFFSET %d", originSql, pageSize, limit);
    }

    /**
     * 构建PostgreSQL的UPSERT语句 (INSERT ... ON CONFLICT ... DO UPDATE SET ...)
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

        String columns = doGenerateFieldsFormatStr(insertFields, null, null);
        String placeholders = StringUtils.repeat("?", ", ", insertFields.fields().size());

        // 构建 ON CONFLICT 冲突目标（主键字段）
        List<String> pkFieldList = entityMeta.getPrimaryKeys();
        String conflictTarget = doGenerateFieldsFormatStr(Fields.create(pkFieldList), null, ", ");

        // 构建 DO UPDATE SET 部分（排除主键）
        StringBuilder updatePart = new StringBuilder();
        Iterator<String> fieldIter = insertFields.fields().iterator();
        boolean first = true;
        while (fieldIter.hasNext()) {
            String field = fieldIter.next();
            if (!entityMeta.isPrimaryKey(field)) {
                if (!first) {
                    updatePart.append(", ");
                }
                String quotedField = wrapIdentifierQuote(field);
                updatePart.append(quotedField).append(" = EXCLUDED.").append(quotedField);
                first = false;
            }
        }

        // 如果没有非主键字段需要更新，则使用 DO NOTHING
        if (updatePart.length() == 0) {
            return ExpressionUtils.bind("INSERT INTO ${table_name} (${columns}) VALUES (${placeholders}) ON CONFLICT (${conflict_target}) DO NOTHING")
                    .set("table_name", tableName)
                    .set("columns", columns)
                    .set("placeholders", placeholders)
                    .set("conflict_target", conflictTarget)
                    .getResult();
        }

        return ExpressionUtils.bind("INSERT INTO ${table_name} (${columns}) VALUES (${placeholders}) ON CONFLICT (${conflict_target}) DO UPDATE SET ${update_set}")
                .set("table_name", tableName)
                .set("columns", columns)
                .set("placeholders", placeholders)
                .set("conflict_target", conflictTarget)
                .set("update_set", updatePart.toString())
                .getResult();
    }

    /**
     * PostgreSQL: INSERT INTO ... ON CONFLICT DO NOTHING
     */
    @Override
    @SuppressWarnings("rawtypes")
    public String buildInsertIfNotExistSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable, Fields fields) {
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        String tableName = buildTableName(prefix, entityMeta, shardingable);
        Fields insertFields = doBuildInsertFields(entityMeta, fields);

        String columns = doGenerateFieldsFormatStr(insertFields, null, null);
        String placeholders = StringUtils.repeat("?", ", ", insertFields.fields().size());

        // 构建 ON CONFLICT 冲突目标（主键字段）
        List<String> pkFieldList = entityMeta.getPrimaryKeys();
        String conflictTarget = doGenerateFieldsFormatStr(Fields.create(pkFieldList), null, ", ");

        return ExpressionUtils.bind("INSERT INTO ${table_name} (${columns}) VALUES (${placeholders}) ON CONFLICT (${conflict_target}) DO NOTHING")
                .set("table_name", tableName)
                .set("columns", columns)
                .set("placeholders", placeholders)
                .set("conflict_target", conflictTarget)
                .getResult();
    }
}
