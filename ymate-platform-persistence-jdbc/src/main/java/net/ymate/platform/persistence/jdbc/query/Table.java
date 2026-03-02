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
import net.ymate.platform.persistence.jdbc.scaffold.Attr;
import net.ymate.platform.persistence.jdbc.scaffold.EntityInfo;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
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

    public Table(IDialect dialect, EntityInfo entityInfo) {
        this(dialect, null, entityInfo);
    }

    public Table(IDialect dialect, String prefix, EntityInfo entityInfo) {
        this(dialect, prefix, entityInfo.getTableName());
        //
        comment = entityInfo.getTableComment();
        // 处理主键字段
        if (!entityInfo.getPrimaryKeys().isEmpty()) {
            for (Attr primaryKey : entityInfo.getPrimaryKeys()) {
                primaryKeys.add(primaryKey.getColumnName());
                // 创建PropertyMeta并添加到properties
                PropertyMeta propertyMeta = createPropertyMetaFromAttr(primaryKey, true);
                if (propertyMeta != null) {
                    properties.put(primaryKey.getColumnName(), propertyMeta);
                }
            }
        } else {
            // 处理单一主键情况
            String primaryKeyName = entityInfo.getPrimaryKeyName();
            if (StringUtils.isNotBlank(primaryKeyName)) {
                // 查找主键对应的字段
                Attr primaryKeyAttr = null;
                for (Attr attr : entityInfo.getFields()) {
                    if (attr.getColumnName().equals(primaryKeyName)) {
                        primaryKeyAttr = attr;
                        break;
                    }
                }
                if (primaryKeyAttr != null) {
                    primaryKeys.add(primaryKeyName);
                    // 创建PropertyMeta并添加到properties
                    PropertyMeta propertyMeta = createPropertyMetaFromAttr(primaryKeyAttr, true);
                    if (propertyMeta != null) {
                        properties.put(primaryKeyName, propertyMeta);
                    }
                }
            }
        }
        // 处理普通字段
        for (Attr field : entityInfo.getFields()) {
            // 跳过已经处理过的主键
            if (!primaryKeys.contains(field.getColumnName())) {
                PropertyMeta propertyMeta = createPropertyMetaFromAttr(field, false);
                if (propertyMeta != null) {
                    properties.put(field.getColumnName(), propertyMeta);
                }
            }
        }
    }

    private PropertyMeta createPropertyMetaFromAttr(Attr attr, boolean isPrimaryKey) {
        Type.FIELD columnType = doGetColumnType(attr.getVarType());
        // 创建PropertyMeta
        PropertyMeta propertyMeta = PropertyMeta.create(attr.getColumnName(), columnType)
                .length(attr.getPrecision())
                .decimals(attr.getScale())
                .nullable(attr.isNullable())
                .defaultValue(attr.getDefaultValue())
                .comment(attr.getRemarks())
                .autoincrement(attr.isAutoIncrement());
        return propertyMeta;
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

    protected Type.FIELD doGetColumnType(String javaType) {
        try {
            Class<?> clazz = Class.forName(javaType);
            return doGetColumnType(clazz);
        } catch (ClassNotFoundException e) {
            if ("byte[]".equals(javaType)) {
                return Type.FIELD.BINARY;
            }
            return Type.FIELD.VARCHAR;
        }
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
                .set("fieldName", dialect.wrapIdentifierQuote(propertyMeta.getName()));
        // 根据数据库方言处理字段类型，特别是日期时间类型
        Type.FIELD fieldTypeEnum = propertyMeta.getType();
        // 当字段类型为UNKNOWN时，尝试通过变量类型进行转换
        if (Type.FIELD.UNKNOWN.equals(fieldTypeEnum)) {
            Field field = propertyMeta.getField();
            if (field != null) {
                fieldTypeEnum = doGetColumnType(field.getType());
            }
        }
        String fieldType = fieldTypeEnum.getName();
        if (Type.DATABASE.SQLSERVER.equals(dialect.getName())) {
            // SQL Server 类型映射
            switch (fieldTypeEnum) {
                case TIMESTAMP:
                    fieldType = "DATETIME2"; // 使用 DATETIME2 以获得更高精度
                    break;
                case DATE:
                case TIME:
                    break;
                case BOOLEAN:
                case BOOL:
                    fieldType = "BIT"; // SQL Server 使用 BIT 表示布尔值
                    break;
                case TEXT:
                    fieldType = "VARCHAR(MAX)"; // SQL Server 使用 VARCHAR(MAX) 代替 TEXT
                    break;
                case BLOB:
                    fieldType = "VARBINARY(MAX)"; // SQL Server 使用 VARBINARY(MAX) 代替 BLOB
                    break;
                default:
                    break;
            }
        } else if (Type.DATABASE.ORACLE.equals(dialect.getName())) {
            // Oracle 类型映射
            switch (fieldTypeEnum) {
                case TIMESTAMP:
                    fieldType = "TIMESTAMP";
                    break;
                case DATE:
                    // Oracle 的 DATE 类型同时包含日期和时间
                    fieldType = "DATE";
                    break;
                case TIME:
                    // Oracle 没有单独的 TIME 类型，使用 DATE 或 TIMESTAMP
                    fieldType = "DATE";
                    break;
                case BOOLEAN:
                case BOOL:
                    fieldType = "NUMBER(1)"; // Oracle 没有原生布尔类型，使用 NUMBER(1)
                    break;
                case TEXT:
                    fieldType = "CLOB"; // Oracle 使用 CLOB 代替 TEXT
                    break;
                case TINYINT:
                    fieldType = "SMALLINT"; // Oracle 没有 TINYINT，使用 SMALLINT
                    break;
                case LONG:
                    fieldType = "NUMBER(19)"; // Oracle 使用 NUMBER(19) 代替 BIGINT
                    break;
                case BIT:
                    fieldType = "NUMBER(1)"; // Oracle 没有原生 BIT 类型，使用 NUMBER(1)
                    break;
                case VARCHAR:
                    fieldType = "VARCHAR2"; // Oracle 使用 VARCHAR2，长度在下方统一处理
                    break;
                default:
                    break;
            }
        } else if (Type.DATABASE.DB2.equals(dialect.getName())) {
            // DB2 类型映射
            switch (fieldTypeEnum) {
                case TIMESTAMP:
                    fieldType = "TIMESTAMP";
                    break;
                case DATE:
                case TIME:
                    break;
                case TEXT:
                    fieldType = "CLOB"; // DB2 使用 CLOB 代替 TEXT
                    break;
                case BLOB:
                    fieldType = "BLOB";
                    break;
                default:
                    break;
            }
        } else if (Type.DATABASE.SQLITE.equals(dialect.getName())) {
            // SQLite 类型映射
            switch (fieldTypeEnum) {
                case TIMESTAMP:
                    fieldType = "DATETIME";
                    break;
                case DATE:
                case TIME:
                    break;
                case BOOLEAN:
                case BOOL:
                    fieldType = "INTEGER"; // SQLite 使用 INTEGER 表示布尔值 (0/1)
                    break;
                case TINYINT:
                case SMALLINT:
                case INT:
                case LONG:
                    fieldType = "INTEGER"; // SQLite 所有整型都使用 INTEGER
                    break;
                case FLOAT:
                case DOUBLE:
                    fieldType = "REAL"; // SQLite 所有浮点型都使用 REAL
                    break;
                case NUMBER:
                    fieldType = "NUMERIC"; // SQLite 使用 NUMERIC 表示数值类型
                    break;
                case BLOB:
                    fieldType = "BLOB";
                    break;
                default:
                    break;
            }
        } else if (Type.DATABASE.POSTGRESQL.equals(dialect.getName())) {
            // PostgreSQL 类型映射
            switch (propertyMeta.getType()) {
                case TINYINT:
                    fieldType = "SMALLINT"; // PostgreSQL 没有 TINYINT，使用 SMALLINT
                    break;
                case BLOB:
                    fieldType = "BYTEA"; // PostgreSQL 使用 BYTEA 代替 BLOB
                    break;
                default:
                    break;
            }
        }
        expression.set("fieldType", fieldType);
        List<String> variables = expression.getVariables();
        //
        // 处理VARCHAR类型，确保总是有长度
        if ("VARCHAR".equals(fieldType) || "VARCHAR2".equals(fieldType)) {
            int varcharLength = propertyMeta.getLength();
            if (varcharLength <= 0) {
                varcharLength = 255; // VARCHAR类型默认长度为255
            }
            expression.set("fieldLength", String.format("(%d)", varcharLength));
        } else if ("NUMBER".equals(fieldType)) {
            // 处理NUMBER类型，确保总是有长度
            int numberLength = propertyMeta.getLength();
            if (numberLength <= 0) {
                numberLength = 10; // NUMBER类型默认长度为10
            }
            int numberDecimals = propertyMeta.getDecimals();
            if (numberDecimals <= 0) {
                numberDecimals = 2; // NUMBER类型默认小数位数为2
            }
            expression.set("fieldLength", String.format("(%d, %d)", numberLength, numberDecimals));
        } else if ("CHAR".equals(fieldType)) {
            // 处理CHAR类型，确保总是有长度
            int charLength = propertyMeta.getLength();
            if (charLength <= 0) {
                charLength = 1; // CHAR类型默认长度为1
            }
            expression.set("fieldLength", String.format("(%d)", charLength));
        } else if ("BINARY".equals(fieldType) || "VARBINARY".equals(fieldType) || "BYTEA".equals(fieldType)) {
            // 处理BINARY/VARBINARY/BYTEA类型，确保总是有长度
            int binaryLength = propertyMeta.getLength();
            if (binaryLength <= 0) {
                binaryLength = 255; // BINARY/VARBINARY/BYTEA类型默认长度为255
            }
            expression.set("fieldLength", String.format("(%d)", binaryLength));
        } else {
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
                    int length = propertyMeta.getLength();
                    if (length > 0) {
                        String decimals = propertyMeta.getDecimals() > 0 ? String.format(", %d", propertyMeta.getDecimals()) : StringUtils.EMPTY;
                        expression.set("fieldLength", String.format("(%d%s)", length, decimals));
                    }
            }
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
            // MySQL 字段注释在字段定义中通过 COMMENT 添加，已在下方统一处理
        } else if (Type.DATABASE.SQLSERVER.equals(dialect.getName())) {
            if (propertyMeta.isAutoincrement() && variables.contains("autoIncrement")) {
                expression.set("autoIncrement", "IDENTITY(1,1)");
            }
        } else if (Type.DATABASE.POSTGRESQL.equals(dialect.getName())) {
            if (propertyMeta.isAutoincrement() && variables.contains("autoIncrement")) {
                expression.set("autoIncrement", "SERIAL");
            }
        } else if (Type.DATABASE.SQLITE.equals(dialect.getName())) {
            if (propertyMeta.isAutoincrement() && variables.contains("autoIncrement")) {
                expression.set("autoIncrement", "AUTOINCREMENT");
            }
        } else if (Type.DATABASE.ORACLE.equals(dialect.getName())) {
            if (propertyMeta.isAutoincrement() && variables.contains("autoIncrement")) {
                // Oracle 使用序列和触发器实现自增，如果指定了序列名则使用序列
                if (StringUtils.isNotBlank(propertyMeta.getSequenceName())) {
                    expression.set("autoIncrement", String.format("DEFAULT %s.NEXTVAL", propertyMeta.getSequenceName()));
                } else {
                    expression.set("autoIncrement", StringUtils.EMPTY);
                }
            }
            // Oracle 字段注释在表创建后通过 COMMENT ON 语句添加
        }
        // 处理字段注释 - 不同数据库有不同的语法
        if (StringUtils.isNotBlank(propertyMeta.getComment()) && variables.contains("comment")) {
            if (Type.DATABASE.MYSQL.equals(dialect.getName())) {
                expression.set("comment", String.format("COMMENT '%s'", propertyMeta.getComment()));
            } else if (Type.DATABASE.POSTGRESQL.equals(dialect.getName())) {
                // PostgreSQL 字段注释在表创建后通过 COMMENT ON 语句添加
                expression.set("comment", StringUtils.EMPTY);
            } else if (Type.DATABASE.SQLSERVER.equals(dialect.getName())) {
                // SQLServer 字段注释在表创建后通过 sp_addextendedproperty 添加
                expression.set("comment", StringUtils.EMPTY);
            } else if (Type.DATABASE.ORACLE.equals(dialect.getName())) {
                // Oracle 字段注释在表创建后通过 COMMENT ON 语句添加
                expression.set("comment", StringUtils.EMPTY);
            } else {
                // 其他数据库默认使用 MySQL 格式
                expression.set("comment", String.format("COMMENT '%s'", propertyMeta.getComment()));
            }
        }
        // 处理 nullable 和默认值
        if (variables.contains("nullable")) {
            StringBuilder nullableBuilder = new StringBuilder();
            if (!propertyMeta.isNullable()) {
                nullableBuilder.append("NOT NULL");
            }
            // 处理默认值 - 无论 nullable 与否，只要有默认值就设置
            if (StringUtils.isNotBlank(propertyMeta.getDefaultValue())) {
                if (nullableBuilder.length() > 0) {
                    nullableBuilder.append(" ");
                }
                if (PropertyMeta.NULL.equals(propertyMeta.getDefaultValue())) {
                    nullableBuilder.append("DEFAULT NULL");
                } else {
                    // 处理默认值 - 所有数据库使用标准 SQL 格式 DEFAULT 'value'
                    // 注意：数值类型在某些数据库中可以不使用引号，但使用引号也是合法的
                    // 特殊值如 CURRENT_TIMESTAMP 等应直接写为 DEFAULT CURRENT_TIMESTAMP（不带引号）
                    String defaultValue = propertyMeta.getDefaultValue();
                    // 检查是否为特殊值（如 CURRENT_TIMESTAMP, NOW() 等）
                    if (isSpecialDefaultValue(defaultValue)) {
                        nullableBuilder.append(String.format("DEFAULT %s", defaultValue));
                    } else {
                        nullableBuilder.append(String.format("DEFAULT '%s'", defaultValue));
                    }
                }
            }
            if (nullableBuilder.length() > 0) {
                expression.set("nullable", nullableBuilder.toString());
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
            List<String> primaryKeyStr = primaryKeys.stream().map(dialect::wrapIdentifierQuote).collect(Collectors.toList());
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
                                    .map(dialect::wrapIdentifierQuote).collect(Collectors.toList()), Query.LINE_END_FLAG))).collect(Collectors.toList());
                    expression.set("indexes", String.format("%s%s", Query.LINE_END_FLAG, StringUtils.join(indexesStr, Query.LINE_END_FLAG)));
                }
                resultStr = expression.clean().getResult();
                break;
            case Type.DATABASE.SQLSERVER:
                resultStr = StringUtils.trimToEmpty(expression.clean().getResult());
                if (!indexes.isEmpty()) {
                    List<String> indexesStr = indexes.values().stream()
                            .map(indexMeta -> String.format("CREATE %s %s ON %s (%s)", indexMeta.isUnique() ? "UNIQUE INDEX " : "INDEX ", dialect.wrapIdentifierQuote(indexMeta.getName()), tableNameBuildStr, StringUtils.join(indexMeta.getFields().stream()
                                    .map(dialect::wrapIdentifierQuote).collect(Collectors.toList()), Query.LINE_END_FLAG))).collect(Collectors.toList());
                    resultStr += String.format("; %s", StringUtils.join(indexesStr, "; "));
                }
                break;
            case Type.DATABASE.POSTGRESQL:
                if (ifExistsOrNot && variables.contains("ifNotExists")) {
                    expression.set("ifNotExists", IF_NOT_EXISTS);
                }
                if (!indexes.isEmpty()) {
                    List<String> indexesStr = indexes.values().stream()
                            .map(indexMeta -> String.format("%s%s (%s)", indexMeta.isUnique() ? "UNIQUE INDEX " : "INDEX ", dialect.wrapIdentifierQuote(indexMeta.getName()), StringUtils.join(indexMeta.getFields().stream()
                                    .map(dialect::wrapIdentifierQuote).collect(Collectors.toList()), Query.LINE_END_FLAG))).collect(Collectors.toList());
                    expression.set("indexes", String.format("%s%s", Query.LINE_END_FLAG, StringUtils.join(indexesStr, Query.LINE_END_FLAG)));
                }
                resultStr = expression.clean().getResult();
                break;
            case Type.DATABASE.SQLITE:
                if (ifExistsOrNot && variables.contains("ifNotExists")) {
                    expression.set("ifNotExists", IF_NOT_EXISTS);
                }
                // SQLite 索引在 CREATE TABLE 中通过 UNIQUE 约束创建
                resultStr = expression.clean().getResult();
                break;
            case Type.DATABASE.ORACLE:
                // Oracle 不支持 IF NOT EXISTS，需要通过其他方式判断
                resultStr = expression.clean().getResult();
                if (!indexes.isEmpty()) {
                    List<String> indexesStr = indexes.values().stream()
                            .map(indexMeta -> String.format("CREATE %s %s ON %s (%s)", indexMeta.isUnique() ? "UNIQUE INDEX " : "INDEX ", dialect.wrapIdentifierQuote(indexMeta.getName()), tableNameBuildStr, StringUtils.join(indexMeta.getFields().stream()
                                    .map(dialect::wrapIdentifierQuote).collect(Collectors.toList()), Query.LINE_END_FLAG))).collect(Collectors.toList());
                    resultStr += String.format("; %s", StringUtils.join(indexesStr, "; "));
                }
                break;
            case Type.DATABASE.H2:
            case Type.DATABASE.DB2:
            case Type.DATABASE.HSQLDB:
            default:
                if (ifExistsOrNot && variables.contains("ifNotExists")) {
                    expression.set("ifNotExists", IF_NOT_EXISTS);
                }
                if (!indexes.isEmpty()) {
                    List<String> indexesStr = indexes.values().stream()
                            .map(indexMeta -> String.format("CREATE %s %s ON %s (%s)", indexMeta.isUnique() ? "UNIQUE INDEX " : "INDEX ", dialect.wrapIdentifierQuote(indexMeta.getName()), tableNameBuildStr, StringUtils.join(indexMeta.getFields().stream()
                                    .map(dialect::wrapIdentifierQuote).collect(Collectors.toList()), Query.LINE_END_FLAG))).collect(Collectors.toList());
                    expression.set("indexes", String.format("%s%s", Query.LINE_END_FLAG, StringUtils.join(indexesStr, Query.LINE_END_FLAG)));
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
        String ifExistsStr;
        switch (dialect.getName()) {
            case Type.DATABASE.MYSQL:
            case Type.DATABASE.POSTGRESQL:
            case Type.DATABASE.SQLITE:
            case Type.DATABASE.H2:
            case Type.DATABASE.HSQLDB:
                // 这些数据库支持 IF EXISTS
                ifExistsStr = IF_EXISTS;
                break;
            case Type.DATABASE.ORACLE:
            case Type.DATABASE.DB2:
            case Type.DATABASE.SQLSERVER:
            default:
                // Oracle、DB2、SQLServer 不支持 IF EXISTS
                ifExistsStr = StringUtils.EMPTY;
        }
        return String.format("DROP TABLE %s %s", ifExistsStr, dialect.buildTableName(prefix, tableName, shardingRule, shardingable));
    }

    /**
     * 构建表数据请空SQL语句
     *
     * @return 返回表数据清空SQL语句
     */
    public String toTruncateSQL() {
        return String.format("TRUNCATE TABLE %s", dialect.buildTableName(prefix, tableName, shardingRule, shardingable));
    }

    /**
     * 判断是否为特殊默认值（如 CURRENT_TIMESTAMP, NOW() 等）
     * 这些值不应该被单引号包裹
     *
     * @param defaultValue 默认值
     * @return 如果是特殊默认值返回 true
     */
    private boolean isSpecialDefaultValue(String defaultValue) {
        if (StringUtils.isBlank(defaultValue)) {
            return false;
        }
        String upperValue = defaultValue.toUpperCase();
        // 常见的特殊默认值关键字
        return upperValue.startsWith("CURRENT_TIMESTAMP") ||
                upperValue.startsWith("CURRENT_DATE") ||
                upperValue.startsWith("CURRENT_TIME") ||
                upperValue.startsWith("NOW()") ||
                upperValue.startsWith("SYSDATE") ||
                upperValue.startsWith("SYSTIMESTAMP") ||
                upperValue.startsWith("GETDATE()") ||
                upperValue.startsWith("UUID()") ||
                upperValue.startsWith("NEWID()") ||
                upperValue.startsWith("RAND()") ||
                upperValue.startsWith("NEXTVAL") ||
                upperValue.startsWith("SEQUENCE") ||
                "TRUE".equals(upperValue) ||
                "FALSE".equals(upperValue);
    }
}
