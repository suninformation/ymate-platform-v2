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

/**
 * MySQL数据库方言接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-30 下午01:55:13
 */
@Dialect(value = Type.DATABASE.MYSQL, driverClass = "com.mysql.jdbc.Driver")
public class MySQLDialect extends AbstractDialect {

    public MySQLDialect() {
        super("`", "`");
    }

    @Override
    public String getName() {
        return Type.DATABASE.MYSQL;
    }

    /**
     * 构建MySQL的UPSERT语句 (INSERT ... ON DUPLICATE KEY UPDATE ...)
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

        // 构建 ON DUPLICATE KEY UPDATE 部分（排除主键）
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
                updatePart.append(quotedField).append(" = VALUES(").append(quotedField).append(")");
                first = false;
            }
        }

        // 如果没有非主键字段需要更新，则使用主键自身赋值（避免语法错误）
        if (updatePart.length() == 0) {
            String pkField = entityMeta.getPrimaryKeys().iterator().next();
            String quotedPk = wrapIdentifierQuote(pkField);
            updatePart.append(quotedPk).append(" = ").append(quotedPk);
        }

        return ExpressionUtils.bind("INSERT INTO ${table_name} (${columns}) VALUES (${placeholders}) ON DUPLICATE KEY UPDATE ${update_set}")
                .set("table_name", tableName)
                .set("columns", columns)
                .set("placeholders", placeholders)
                .set("update_set", updatePart.toString())
                .getResult();
    }

    /**
     * MySQL: INSERT IGNORE INTO ...
     */
    @Override
    @SuppressWarnings("rawtypes")
    public String buildInsertIfNotExistSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable, Fields fields) {
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        String tableName = buildTableName(prefix, entityMeta, shardingable);
        Fields insertFields = doBuildInsertFields(entityMeta, fields);

        String columns = doGenerateFieldsFormatStr(insertFields, null, null);
        String placeholders = StringUtils.repeat("?", ", ", insertFields.fields().size());

        return ExpressionUtils.bind("INSERT IGNORE INTO ${table_name} (${columns}) VALUES (${placeholders})")
                .set("table_name", tableName)
                .set("columns", columns)
                .set("placeholders", placeholders)
                .getResult();
    }
}
