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
package net.ymate.platform.persistence.jdbc.dialect;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IShardingRule;
import net.ymate.platform.core.persistence.IShardingable;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.Type;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 数据库方言接口抽象实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-30 下午01:55:13
 */
public abstract class AbstractDialect implements IDialect {

    protected static final String LINE_END_FLAG = ",\n";

    /**
     * 引用标识符-开始
     */
    private String identifierQuoteBegin = StringUtils.EMPTY;

    /**
     * 引用标识符-结束
     */
    private String identifierQuoteEnd = StringUtils.EMPTY;

    public AbstractDialect() {
    }

    public AbstractDialect(String identifierQuoteBegin, String identifierQuoteEnd) {
        this.setIdentifierQuote(identifierQuoteBegin, identifierQuoteEnd);
    }

    @Override
    public String wrapIdentifierQuote(String origin) {
        return identifierQuoteBegin.concat(origin).concat(identifierQuoteEnd);
    }

    @Override
    public void setIdentifierQuote(String identifierQuoteBegin, String identifierQuoteEnd) {
        this.identifierQuoteBegin = StringUtils.trimToEmpty(identifierQuoteBegin);
        this.identifierQuoteEnd = StringUtils.trimToEmpty(identifierQuoteEnd);
    }

    @Override
    public String getIdentifierQuoteBegin() {
        return identifierQuoteBegin;
    }

    @Override
    public String getIdentifierQuoteEnd() {
        return identifierQuoteEnd;
    }

    @Override
    public Map<String, Object> getGeneratedKey(Statement statement, List<String> autoincrementKeys) throws SQLException {
        // 检索由于执行此 Statement 对象而创建的所有自动生成的键
        Map<String, Object> ids = new HashMap<>(autoincrementKeys.size());
        try (ResultSet keySet = statement.getGeneratedKeys()) {
            for (String autoKey : autoincrementKeys) {
                while (keySet.next()) {
                    Object keyValue;
                    try {
                        keyValue = keySet.getObject(autoKey);
                    } catch (SQLException e) {
                        keyValue = keySet.getObject(1);
                    }
                    ids.put(autoKey, keyValue);
                }
            }
        }
        return ids;
    }

    @Override
    public String getSequenceNextValSql(String sequenceName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String buildPagedQuerySql(String originSql, int page, int pageSize) {
        int limit = (page - 1) * pageSize;
        if (pageSize == 0) {
            return originSql.concat(" limit ").concat(Integer.toString(limit));
        } else {
            return originSql.concat(" limit ").concat(Integer.toString(limit)).concat(", ").concat(Integer.toString(pageSize));
        }
    }

    @Override
    public String buildCreateSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String buildDropSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable) {
        throw new UnsupportedOperationException();
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

    /**
     * @param fields    字段名称集合
     * @param suffix    字段名称后缀，可选
     * @param separator 分隔符，可选，默认“, ”
     * @return 将字段名称集合转换成为采用separator分隔的字符串
     */
    protected String doGenerateFieldsFormatStr(Fields fields, String suffix, String separator) {
        StringBuilder fieldsBuilder = new StringBuilder();
        Iterator<String> fieldsIt = fields.fields().iterator();
        suffix = StringUtils.trimToEmpty(suffix);
        separator = StringUtils.defaultIfBlank(separator, ", ");
        while (fieldsIt.hasNext()) {
            fieldsBuilder.append(this.wrapIdentifierQuote(fieldsIt.next())).append(suffix);
            if (fieldsIt.hasNext()) {
                fieldsBuilder.append(separator);
            }
        }
        return fieldsBuilder.toString();
    }

    @Override
    public String buildTableName(String prefix, EntityMeta entityMeta, IShardingable shardingable) {
        String entityName = entityMeta.getEntityName();
        if (shardingable != null && entityMeta.getShardingRule() != null) {
            IShardingRule rule = ClassUtils.impl(entityMeta.getShardingRule(), IShardingRule.class);
            if (rule != null) {
                entityName = rule.getShardName(entityMeta.getEntityName(), shardingable.getShardingParam());
            }
        }
        return this.wrapIdentifierQuote(StringUtils.trimToEmpty(prefix).concat(entityName));
    }

    /**
     * 验证字段是否合法有效
     *
     * @param entityMeta    数据实体属性描述对象
     * @param fields        字段名称集合
     * @param isPrimaryKeys fields中存放的是否为主键
     */
    protected void doValidProperty(EntityMeta entityMeta, Fields fields, boolean isPrimaryKeys) {
        if (isPrimaryKeys) {
            fields.fields().stream().filter((pkField) -> (!entityMeta.isPrimaryKey(pkField))).forEachOrdered((pkField) -> {
                throw new IllegalArgumentException(String.format("'%s' isn't primary key field.", pkField));
            });
        } else {
            fields.fields().stream().filter((field) -> (!entityMeta.containsProperty(field))).forEachOrdered((field) -> {
                throw new IllegalArgumentException(String.format("'%s' isn't table field.", field));
            });
        }
    }

    @Override
    public String buildInsertSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable, Fields fields) {
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        ExpressionUtils exp = ExpressionUtils.bind("INSERT INTO ${table_name} (${fields}) VALUES (${values})")
                .set("table_name", buildTableName(prefix, entityMeta, shardingable));
        //
        Fields newFields = Fields.create();
        if (fields == null || fields.fields().isEmpty()) {
            newFields.add(entityMeta.getPropertyNames());
        } else {
            newFields.add(fields);
            doValidProperty(entityMeta, newFields, false);
        }
        return exp.set("fields", doGenerateFieldsFormatStr(newFields, null, null)).set("values", StringUtils.repeat("?", ", ", newFields.fields().size())).getResult();
    }

    @Override
    public String buildDeleteByPkSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable, Fields pkFields) {
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        ExpressionUtils exp = ExpressionUtils.bind("DELETE FROM ${table_name} WHERE ${pk}")
                .set("table_name", buildTableName(prefix, entityMeta, shardingable));
        //
        Fields fields = Fields.create();
        if (pkFields == null || pkFields.fields().isEmpty()) {
            fields.add(entityMeta.getPrimaryKeys());
        } else {
            fields.add(pkFields);
            doValidProperty(entityMeta, fields, true);
        }
        return exp.set("pk", doGenerateFieldsFormatStr(fields, " = ?", " and ")).getResult();
    }

    @Override
    public String buildUpdateByPkSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable, Fields pkFields, Fields fields) {
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        ExpressionUtils exp = ExpressionUtils.bind("UPDATE ${table_name} SET ${fields} WHERE ${pk}")
                .set("table_name", buildTableName(prefix, entityMeta, shardingable));
        //
        Fields newFields = Fields.create();
        for (String field : (fields == null || fields.fields().isEmpty()) ? entityMeta.getPropertyNames() : fields.fields()) {
            if (entityMeta.containsProperty(field)) {
                if (entityMeta.isPrimaryKey(field)) {
                    // 排除主键
                    continue;
                }
                newFields.add(field);
            } else {
                throw new IllegalArgumentException(String.format("'%s' isn't table field", field));
            }
        }
        exp.set("fields", doGenerateFieldsFormatStr(newFields, " = ?", null));
        //
        if (pkFields != null && !pkFields.fields().isEmpty()) {
            newFields = pkFields;
            doValidProperty(entityMeta, newFields, true);
        } else {
            newFields = Fields.create().add(entityMeta.getPrimaryKeys());
        }
        return exp.set("pk", doGenerateFieldsFormatStr(newFields, " = ?", " and ")).getResult();
    }

    @Override
    public String buildSelectByPkSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable, Fields pkFields, Fields fields) {
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        ExpressionUtils exp = ExpressionUtils.bind("SELECT ${fields} FROM ${table_name} WHERE ${pk}")
                .set("table_name", buildTableName(prefix, entityMeta, shardingable));
        //
        if (fields == null || fields.fields().isEmpty()) {
            fields = Fields.create().add(entityMeta.getPropertyNames());
        } else {
            doValidProperty(entityMeta, fields, false);
        }
        exp.set("fields", doGenerateFieldsFormatStr(fields, null, null));
        //
        if (pkFields != null && !pkFields.fields().isEmpty()) {
            doValidProperty(entityMeta, pkFields, true);
        } else {
            pkFields = Fields.create().add(entityMeta.getPrimaryKeys());
        }
        return exp.set("pk", doGenerateFieldsFormatStr(pkFields, " = ?", " and ")).getResult();
    }

    @Override
    public String buildSelectSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable, Fields fields) {
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        ExpressionUtils exp = ExpressionUtils.bind("SELECT ${fields} FROM ${table_name}")
                .set("table_name", buildTableName(prefix, entityMeta, shardingable));
        //
        if (fields == null || fields.fields().isEmpty()) {
            fields = Fields.create().add(entityMeta.getPropertyNames());
        } else {
            doValidProperty(entityMeta, fields, false);
        }
        return exp.set("fields", doGenerateFieldsFormatStr(fields, null, null)).getResult();
    }
}
