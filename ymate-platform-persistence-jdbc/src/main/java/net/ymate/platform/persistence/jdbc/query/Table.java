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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.core.persistence.IShardingRule;
import net.ymate.platform.core.persistence.IShardingable;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IndexMeta;
import net.ymate.platform.core.persistence.base.PropertyMeta;
import net.ymate.platform.core.persistence.base.Type;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-11-20 15:14
 * @since 2.1.0
 */
public class Table extends QueryHandleAdapter<Table> {

    public static final String IF_NOT_EXISTS = "IF NOT EXISTS";

    public static final String IF_EXISTS = "IF EXISTS";

    private final IDialect dialect;

    private IShardingRule shardingRule;

    private IShardingable shardingable;

    private final String prefix;

    private final String tableName;

    private String comment;

    private final List<String> primaryKeys = new ArrayList<>();

    private final Map<String, PropertyMeta> properties = new LinkedHashMap<>();

    private final Map<String, IndexMeta> indexes = new LinkedHashMap<>();

    private String propertyExpressionStr = "${fieldName} ${fieldType}${fieldLength} ${unsigned} ${nullable} ${autoIncrement} ${comment}";

    private boolean ifExistsOrNot;

    private final Slot slot = new Slot();

    public Table(IDialect dialect, String tableName) {
        this(dialect, null, tableName);
    }

    public Table(IDialect dialect, String prefix, String tableName) {
        if (dialect == null) {
            throw new NullArgumentException("dialect");
        }
        if (StringUtils.isBlank(tableName)) {
            throw new NullArgumentException("tableName");
        }
        this.dialect = dialect;
        this.prefix = prefix;
        this.tableName = tableName;
    }

    public Table(IDialect dialect, EntityMeta entityMeta) {
        this(dialect, null, entityMeta);
    }

    public Table(IDialect dialect, String prefix, EntityMeta entityMeta) {
        this(dialect, prefix, entityMeta.getEntityName());
        //
        entityMeta.getProperties().forEach(propertyMeta -> properties.put(propertyMeta.getName(), propertyMeta));
        entityMeta.getIndexes().stream()
                .filter(indexMeta -> StringUtils.isNotBlank(indexMeta.getName()) && indexMeta.getFields() != null && !indexMeta.getFields().isEmpty())
                .forEachOrdered(indexMeta -> indexes.put(indexMeta.getName(), indexMeta));
        primaryKeys.addAll(entityMeta.getPrimaryKeys());
        comment = entityMeta.getComment();
        //
        if (entityMeta.getShardingRule() != null) {
            shardingRule = ClassUtils.impl(entityMeta.getShardingRule(), IShardingRule.class);
        }
    }

    public IShardingRule shardingRule() {
        return shardingRule;
    }

    public Table shardingRule(IShardingRule shardingRule) {
        this.shardingRule = shardingRule;
        return this;
    }

    public IShardingable shardingable() {
        return shardingable;
    }

    public Table shardingable(IShardingable shardingable) {
        this.shardingable = shardingable;
        return this;
    }

    public Table comment(String comment) {
        this.comment = comment;
        return this;
    }

    public Table addProperty(PropertyMeta propertyMeta) {
        return addProperty(propertyMeta, false);
    }

    public Table addProperty(PropertyMeta propertyMeta, boolean primaryKey) {
        if (StringUtils.isNotBlank(propertyMeta.getName())) {
            if (primaryKey) {
                primaryKeys.add(propertyMeta.getName());
            }
            properties.put(propertyMeta.getName(), propertyMeta);
        }
        return this;
    }

    public Table addIndex(IndexMeta indexMeta) {
        if (StringUtils.isNotBlank(indexMeta.getName()) && indexMeta.getFields() != null && !indexMeta.getFields().isEmpty()) {
            indexes.put(indexMeta.getName(), indexMeta);
        }
        return this;
    }

    public Slot getSlot() {
        return slot;
    }

    protected Type.FIELD doGetColumnType(Class<?> clazz) {
        Type.FIELD columnType = Type.FIELD.VARCHAR;
        if (BigDecimal.class.equals(clazz)) {
            columnType = Type.FIELD.NUMBER;
        } else if (Boolean.class.equals(clazz) || boolean.class.equals(clazz)) {
            columnType = Type.FIELD.TINYINT;
        } else if (Byte.class.equals(clazz) || byte.class.equals(clazz)) {
            columnType = Type.FIELD.BIT;
        } else if (Short.class.equals(clazz) || short.class.equals(clazz)) {
            columnType = Type.FIELD.SMALLINT;
        } else if (Integer.class.equals(clazz) || int.class.equals(clazz)) {
            columnType = Type.FIELD.INT;
        } else if (Long.class.equals(clazz) || long.class.equals(clazz)) {
            columnType = Type.FIELD.LONG;
        } else if (Float.class.equals(clazz) || float.class.equals(clazz)) {
            columnType = Type.FIELD.FLOAT;
        } else if (Double.class.equals(clazz) || double.class.equals(clazz)) {
            columnType = Type.FIELD.DOUBLE;
        } else if (byte[].class.equals(clazz) || Byte[].class.equals(clazz)) {
            columnType = Type.FIELD.BINARY;
        } else if (java.sql.Date.class.equals(clazz) || java.util.Date.class.equals(clazz)) {
            columnType = Type.FIELD.DATE;
        } else if (java.sql.Time.class.equals(clazz)) {
            columnType = Type.FIELD.TIME;
        } else if (java.sql.Timestamp.class.equals(clazz)) {
            columnType = Type.FIELD.TIMESTAMP;
        } else if (java.sql.Blob.class.equals(clazz)) {
            columnType = Type.FIELD.BLOB;
        } else if (java.sql.Clob.class.equals(clazz)) {
            columnType = Type.FIELD.CLOB;
        }
        return columnType;
    }

    public boolean ifExistsOrNot() {
        return ifExistsOrNot;
    }

    public Table ifExistsOrNot(boolean ifExistsOrNot) {
        this.ifExistsOrNot = ifExistsOrNot;
        return this;
    }

    public String propertyExpressionStr() {
        return propertyExpressionStr;
    }

    public Table propertyExpressionStr(String propertyExpressionStr) {
        if (StringUtils.isNotBlank(propertyExpressionStr)) {
            this.propertyExpressionStr = propertyExpressionStr;
        }
        return this;
    }

    public String processProperty(PropertyMeta propertyMeta) {
        ExpressionUtils expression = ExpressionUtils.bind(propertyExpressionStr())
                .set("fieldName", dialect.wrapIdentifierQuote(propertyMeta.getName()))
                .set("fieldType", propertyMeta.getType().getName());
        List<String> variables = expression.getVariables();
        //
        switch (propertyMeta.getType()) {
            case DATE:
            case TIME:
            case TIMESTAMP:
            case TEXT:
            case LONG:
            case FLOAT:
            case SMALLINT:
            case TINYINT:
            case DOUBLE:
            case BIT:
            case BOOLEAN:
            case BOOL:
            case INT:
                break;
            default:
                String decimals = propertyMeta.getDecimals() > 0 ? String.format(", %d", propertyMeta.getDecimals()) : StringUtils.EMPTY;
                expression.set("fieldLength", String.format("(%d%s)", propertyMeta.getLength(), decimals));
        }
        if (Type.DATABASE.MYSQL.equals(dialect.getName())) {
            if (propertyMeta.isUnsigned() && variables.contains("unsigned")) {
                switch (propertyMeta.getType()) {
                    case NUMBER:
                    case LONG:
                    case FLOAT:
                    case SMALLINT:
                    case TINYINT:
                    case DOUBLE:
                    case INT:
                        expression.set("unsigned", "unsigned");
                        break;
                    default:
                }
            }
            if (propertyMeta.isAutoincrement() && variables.contains("autoIncrement")) {
                expression.set("autoIncrement", "AUTO_INCREMENT");
            }
            if (StringUtils.isNotBlank(propertyMeta.getComment()) && variables.contains("comment")) {
                expression.set("comment", String.format("COMMENT '%s'", propertyMeta.getComment()));
            }
        } else if (Type.DATABASE.SQLSERVER.equals(dialect.getName())) {
            if (propertyMeta.isAutoincrement() && variables.contains("autoIncrement")) {
                expression.set("autoIncrement", "IDENTITY(1,1)");
            }
        }
        if (variables.contains("nullable")) {
            if (propertyMeta.isNullable()) {
                if (StringUtils.isNotBlank(propertyMeta.getDefaultValue())) {
                    if (PropertyMeta.NULL.equals(propertyMeta.getDefaultValue())) {
                        expression.set("nullable", "DEFAULT NULL");
                    } else {
                        expression.set("nullable", String.format("DEFAULT '%s'", propertyMeta.getDefaultValue()));
                    }
                }
            } else {
                expression.set("nullable", "NOT NULL");
            }
        }
        return StringUtils.trimToEmpty(expression.clean().getResult());
    }

    /**
     * 构建表创建SQL对象
     *
     * @return 返回表创建SQL对象
     */
    public String toCreateSQL() {
        ExpressionUtils expression = ExpressionUtils.bind(getExpressionStr("CREATE TABLE ${ifNotExists} ${tableName} (${fields} ${primaryKeys} ${indexes}) ${slot} ${comment}"));
        if (queryHandler() != null) {
            queryHandler().beforeBuild(expression, this);
        }
        List<String> variables = expression.getVariables();
        //
        String tableNameBuildStr = dialect.buildTableName(prefix, tableName, shardingRule, shardingable);
        expression.set("tableName", tableNameBuildStr);
        //
        if (variables.contains("fields")) {
            List<String> fields = properties.values().stream().map(this::processProperty).collect(Collectors.toList());
            expression.set("fields", StringUtils.join(fields, Query.LINE_END_FLAG));
        }
        //
        if (!primaryKeys.isEmpty() && variables.contains("primaryKeys")) {
            List<String> primaryKeyStr = primaryKeys.stream().map(primaryKey -> dialect.wrapIdentifierQuote(primaryKey)).collect(Collectors.toList());
            expression.set("primaryKeys", String.format("%s PRIMARY KEY (%s)", Query.LINE_END_FLAG, StringUtils.join(primaryKeyStr, Query.LINE_END_FLAG)));
        }
        //
        if (variables.contains("slot")) {
            if (Type.DATABASE.MYSQL.equals(dialect.getName())) {
                slot.addSlotContent("ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            }
            if (slot.hasSlotContent()) {
                expression.set("slot", slot.buildSlot());
            }
        }
        //
        if (queryHandler() != null) {
            queryHandler().afterBuild(expression, this);
        }
        //
        String resultStr;
        switch (dialect.getName()) {
            case Type.DATABASE.MYSQL:
                expression.set("ifNotExists", IF_NOT_EXISTS);
                if (StringUtils.isNotBlank(comment)) {
                    expression.set("comment", String.format("COMMENT='%s'", comment));
                }
                if (!indexes.isEmpty()) {
                    List<String> indexesStr = indexes.values().stream()
                            .map(indexMeta -> String.format("%s%s (%s)", indexMeta.isUnique() ? "UNIQUE INDEX " : "INDEX ", dialect.wrapIdentifierQuote(indexMeta.getName()), StringUtils.join(indexMeta.getFields().stream()
                                    .map(idxField -> dialect.wrapIdentifierQuote(idxField)).collect(Collectors.toList()), Query.LINE_END_FLAG))).collect(Collectors.toList());
                    expression.set("indexes", String.format("%s%s", Query.LINE_END_FLAG, StringUtils.join(indexesStr, Query.LINE_END_FLAG)));
                }
                resultStr = expression.clean().getResult();
                break;
            case Type.DATABASE.SQLSERVER:
                resultStr = StringUtils.trimToEmpty(expression.clean().getResult());
                if (!indexes.isEmpty()) {
                    List<String> indexesStr = indexes.values().stream()
                            .map(indexMeta -> String.format("CREATE %s %s ON %s (%s)", indexMeta.isUnique() ? "UNIQUE INDEX " : "INDEX ", dialect.wrapIdentifierQuote(indexMeta.getName()), tableNameBuildStr, StringUtils.join(indexMeta.getFields().stream()
                                    .map(idxField -> dialect.wrapIdentifierQuote(idxField)).collect(Collectors.toList()), Query.LINE_END_FLAG))).collect(Collectors.toList());
                    resultStr += String.format("; %s", StringUtils.join(indexesStr, "; "));
                }
                break;
            case Type.DATABASE.ORACLE:
            default:
                if (ifExistsOrNot && variables.contains("ifNotExists")) {
                    expression.set("ifNotExists", IF_NOT_EXISTS);
                }
                resultStr = expression.clean().getResult();
        }
        return StringUtils.trimToEmpty(resultStr);
    }

    /**
     * 构建表删除SQL语句
     *
     * @return 返回表删除SQL语句
     */
    public String toDropSQL() {
        return String.format("DROP TABLE %s %s", (StringUtils.equals(Type.DATABASE.MYSQL, dialect.getName()) || ifExistsOrNot ? IF_EXISTS : StringUtils.EMPTY), dialect.buildTableName(prefix, tableName, shardingRule, shardingable));
    }

    /**
     * 构建表数据请空SQL语句
     *
     * @return 返回表数据清空SQL语句
     */
    public String toTruncateSQL() {
        return String.format("TRUNCATE TABLE %s", dialect.buildTableName(prefix, tableName, shardingRule, shardingable));
    }
}
