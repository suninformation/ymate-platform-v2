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
import net.ymate.platform.core.persistence.IShardingable;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.PropertyMeta;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.annotation.Dialect;
import net.ymate.platform.persistence.jdbc.dialect.AbstractDialect;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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

    @Override
    public String buildCreateSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable) {
        EntityMeta entityMeta = EntityMeta.createAndGet(entityClass);
        if (entityMeta == null) {
            throw new IllegalArgumentException(String.format("Entity class [%s] invalid.", entityClass.getName()));
        }
        ExpressionUtils exp = ExpressionUtils.bind("CREATE TABLE ${table_name} (\n${fields} ${primary_keys} ${indexes}) ${comment} ").set("table_name", buildTableName(prefix, entityMeta, shardingable));
        if (StringUtils.isNotBlank(entityMeta.getComment())) {
            exp.set("comment", String.format("COMMENT='%s'", StringUtils.trimToEmpty(entityMeta.getComment())));
        } else {
            exp.set("comment", StringUtils.EMPTY);
        }
        StringBuilder stringBuilder = new StringBuilder();
        // FIELDS
        List<PropertyMeta> propertyMetas = new ArrayList<>(entityMeta.getProperties());
        propertyMetas.sort(Comparator.comparing(PropertyMeta::getName));
        propertyMetas.forEach(propertyMeta -> doProcessField(propertyMeta, stringBuilder));
        exp.set("fields", stringBuilder.length() > 2 ? stringBuilder.substring(0, stringBuilder.lastIndexOf(LINE_END_FLAG)) : StringUtils.EMPTY);
        // PKS
        stringBuilder.setLength(0);
        entityMeta.getPrimaryKeys().forEach(key -> stringBuilder.append(wrapIdentifierQuote(key)).append(","));
        if (stringBuilder.length() > 0) {
            stringBuilder.setLength(stringBuilder.length() - 1);
            stringBuilder.insert(0, ",\n\tPRIMARY KEY (").append(")");
            exp.set("primary_keys", stringBuilder.toString());
        }
        // INDEXES
        stringBuilder.setLength(0);
        if (!entityMeta.getIndexes().isEmpty()) {
            stringBuilder.append(LINE_END_FLAG);
            entityMeta.getIndexes().stream().filter((indexMeta) -> (!indexMeta.getFields().isEmpty())).forEachOrdered((indexMeta) -> {
                List<String> idxFields = new ArrayList<>(indexMeta.getFields().size());
                indexMeta.getFields().forEach((idxField) -> idxFields.add(wrapIdentifierQuote(idxField)));
                if (indexMeta.isUnique()) {
                    stringBuilder.append("\tUNIQUE KEY ");
                } else {
                    stringBuilder.append("\tINDEX ");
                }
                stringBuilder.append(wrapIdentifierQuote(indexMeta.getName())).append(" (").append(StringUtils.join(idxFields, ",")).append(")").append(LINE_END_FLAG);
            });
            if (stringBuilder.length() > 2) {
                stringBuilder.setLength(stringBuilder.length() - 2);
            }
        }
        return exp.set("indexes", stringBuilder.toString()).getResult();
    }

    private void doProcessField(PropertyMeta propertyMeta, StringBuilder stringBuilder) {
        stringBuilder.append("\t").append(wrapIdentifierQuote(propertyMeta.getName())).append(StringUtils.SPACE);
        Type.FIELD propType;
        if (!Type.FIELD.UNKNOWN.equals(propertyMeta.getType())) {
            propType = propertyMeta.getType();
        } else {
            propType = doGetColumnType(propertyMeta.getField().getType());
        }
        boolean needLength;
        switch (propType) {
            case DATE:
            case TIME:
            case TIMESTAMP:
            case TEXT:
                needLength = false;
                break;
            default:
                needLength = true;
        }
        stringBuilder.append(propType.getName());
        if (needLength) {
            stringBuilder.append("(").append(propertyMeta.getLength());
            if (propertyMeta.getDecimals() > 0) {
                stringBuilder.append(",").append(propertyMeta.getDecimals());
            }
            stringBuilder.append(")");
        }
        if (propertyMeta.isUnsigned()) {
            switch (propType) {
                case NUMBER:
                case LONG:
                case FLOAT:
                case SMALLINT:
                case TINYINT:
                case DOUBLE:
                case INT:
                    stringBuilder.append(" unsigned ");
                    break;
                default:
            }
        }
        if (propertyMeta.isNullable()) {
            if (StringUtils.isNotBlank(propertyMeta.getDefaultValue())) {
                if ("@NULL".equals(propertyMeta.getDefaultValue())) {
                    stringBuilder.append(" DEFAULT NULL");
                } else {
                    stringBuilder.append(" DEFAULT '").append(propertyMeta.getDefaultValue()).append("'");
                }
            }
        } else {
            stringBuilder.append(" NOT NULL");
        }
        if (propertyMeta.isAutoincrement()) {
            stringBuilder.append(" AUTO_INCREMENT");
        }
        if (StringUtils.isNotBlank(propertyMeta.getComment())) {
            stringBuilder.append(" COMMENT '").append(propertyMeta.getComment()).append("'");
        }
        stringBuilder.append(LINE_END_FLAG);
    }

    @Override
    public String buildDropSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable) {
        EntityMeta entityMeta = EntityMeta.createAndGet(entityClass);
        if (entityMeta == null) {
            throw new IllegalArgumentException(String.format("Entity class [%s] invalid.", entityClass.getName()));
        }
        return "DROP TABLE IF EXISTS " + buildTableName(prefix, entityMeta, shardingable);
    }
}
