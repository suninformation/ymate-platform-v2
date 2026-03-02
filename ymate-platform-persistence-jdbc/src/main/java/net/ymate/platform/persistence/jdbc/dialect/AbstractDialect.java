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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IShardingRule;
import net.ymate.platform.core.persistence.IShardingable;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.query.Table;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * 数据库方言接口抽象实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-30 下午01:55:13
 */
public abstract class AbstractDialect implements IDialect {

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
        if (hasIdentifierQuote()) {
            String[] originArr = StringUtils.split(origin, ".");
            if (ArrayUtils.isNotEmpty(originArr)) {
                IntStream.range(0, originArr.length).forEach(idx -> {
                    originArr[idx] = StringUtils.trim(originArr[idx]);
                    if (!Strings.CS.equalsAny(originArr[idx], "*", "?")) {
                        if (!Strings.CS.startsWith(originArr[idx], identifierQuoteBegin)) {
                            originArr[idx] = identifierQuoteBegin + originArr[idx];
                        }
                        if (!Strings.CS.endsWith(originArr[idx], identifierQuoteEnd)) {
                            originArr[idx] += identifierQuoteEnd;
                        }
                    }
                });
                return StringUtils.join(originArr, ".");
            }
        }
        return origin;
    }

    @Override
    public void setIdentifierQuote(String identifierQuoteBegin, String identifierQuoteEnd) {
        this.identifierQuoteBegin = StringUtils.trimToEmpty(identifierQuoteBegin);
        this.identifierQuoteEnd = StringUtils.trimToEmpty(identifierQuoteEnd);
    }

    @Override
    public boolean hasIdentifierQuote() {
        return StringUtils.isNotBlank(identifierQuoteBegin) && StringUtils.isNotBlank(identifierQuoteEnd);
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
            return String.format("%s LIMIT %d", originSql, limit);
        }
        return String.format("%s LIMIT %d, %d", originSql, limit, pageSize);
    }

    protected String doRemoveOrderByElements(String originSql) {
        try {
            net.sf.jsqlparser.statement.Statement statement = new CCJSqlParserManager().parse(new StringReader(originSql));
            if (statement instanceof PlainSelect) {
                PlainSelect select = ((Select) statement).getPlainSelect();
                if (null != select.getOrderByElements()) {
                    select.setOrderByElements(null);
                }
                return select.toString();
            }
        } catch (JSQLParserException ignored) {
        }
        return originSql;
    }

    @Override
    public String buildCountSQL(String originSql) {
        return String.format("SELECT count(*) FROM (%s) c_t", doRemoveOrderByElements(originSql));
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String buildCreateSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable) {
        return new Table(this, prefix, EntityMeta.load(entityClass)).shardingable(shardingable).toCreateSQL();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String buildDropSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable) {
        return new Table(this, prefix, EntityMeta.load(entityClass)).shardingable(shardingable).toDropSQL();
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
        IShardingRule shardingRule = null;
        if (shardingable != null && entityMeta.getShardingRule() != null) {
            shardingRule = ClassUtils.impl(entityMeta.getShardingRule(), IShardingRule.class);
        }
        return buildTableName(prefix, entityMeta.getEntityName(), shardingRule, shardingable);
    }

    @Override
    public String buildTableName(String prefix, String tableName, IShardingRule shardingRule, IShardingable shardingable) {
        if (shardingable != null && shardingRule != null) {
            tableName = shardingRule.getShardName(tableName, shardingable.getShardingParam());
        }
        if (StringUtils.isNotBlank(prefix) && Strings.CS.startsWith(tableName, prefix)) {
            prefix = StringUtils.EMPTY;
        }
        return this.wrapIdentifierQuote(StringUtils.trimToEmpty(prefix).concat(tableName));
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
    @SuppressWarnings("rawtypes")
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
    @SuppressWarnings("rawtypes")
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
    @SuppressWarnings("rawtypes")
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
    @SuppressWarnings("rawtypes")
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
    @SuppressWarnings("rawtypes")
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

    @Override
    @SuppressWarnings("rawtypes")
    public String buildUpsertSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable, Fields fields) {
        throw new UnsupportedOperationException("Upsert operation is not supported by default, please implement it in specific dialect");
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String buildInsertIfNotExistSql(Class<? extends IEntity> entityClass, String prefix, IShardingable shardingable, Fields fields) {
        throw new UnsupportedOperationException("InsertIfNotExist operation is not supported by default, please implement it in specific dialect");
    }

    /**
     * 构建插入字段列表
     *
     * @param entityMeta 实体元数据
     * @param fields     字段过滤集合
     * @return 构建好的字段列表
     */
    protected Fields doBuildInsertFields(EntityMeta entityMeta, Fields fields) {
        Fields insertFields = Fields.create();
        if (fields == null || fields.fields().isEmpty()) {
            insertFields.add(entityMeta.getPropertyNames());
        } else {
            insertFields.add(fields);
            doValidProperty(entityMeta, insertFields, false);
        }
        return insertFields;
    }

    /**
     * 构建MERGE语句的USING子句（主键字段作为参数）
     *
     * @param entityMeta 实体元数据
     * @param pkFields   主键字段列表
     * @return USING子句字符串
     */
    protected String doBuildMergeUsingClause(EntityMeta entityMeta, List<String> pkFields) {
        StringBuilder usingClause = new StringBuilder();
        Iterator<String> pkIter = pkFields.iterator();
        boolean first = true;
        while (pkIter.hasNext()) {
            String pk = pkIter.next();
            if (!first) {
                usingClause.append(", ");
            }
            usingClause.append("? AS ").append(wrapIdentifierQuote(pk));
            first = false;
        }
        return usingClause.toString();
    }

    /**
     * 构建MERGE语句的ON条件
     *
     * @param entityMeta 实体元数据
     * @param pkFields   主键字段列表
     * @return ON条件字符串
     */
    protected String doBuildMergeOnClause(EntityMeta entityMeta, List<String> pkFields) {
        StringBuilder onClause = new StringBuilder();
        Iterator<String> pkIter = pkFields.iterator();
        boolean first = true;
        while (pkIter.hasNext()) {
            String pk = pkIter.next();
            if (!first) {
                onClause.append(" AND ");
            }
            String quotedPk = wrapIdentifierQuote(pk);
            onClause.append("target.").append(quotedPk)
                    .append(" = source.").append(quotedPk);
            first = false;
        }
        return onClause.toString();
    }

    /**
     * 构建MERGE语句的UPDATE SET部分
     *
     * @param entityMeta   实体元数据
     * @param insertFields 插入字段列表
     * @return UPDATE SET字符串
     */
    protected String doBuildMergeUpdateSet(EntityMeta entityMeta, Fields insertFields) {
        StringBuilder updateSet = new StringBuilder();
        Iterator<String> fieldIter = insertFields.fields().iterator();
        boolean first = true;
        while (fieldIter.hasNext()) {
            String field = fieldIter.next();
            if (!entityMeta.isPrimaryKey(field)) {
                if (!first) {
                    updateSet.append(", ");
                }
                String quotedField = wrapIdentifierQuote(field);
                updateSet.append(quotedField).append(" = source.").append(quotedField);
                first = false;
            }
        }
        return updateSet.toString();
    }

    /**
     * 构建MERGE语句的INSERT列名和值部分
     *
     * @param insertFields 插入字段列表
     * @return 包含列名和值的字符串数组，[0]=列名, [1]=值
     */
    protected String[] doBuildMergeInsertParts(Fields insertFields) {
        StringBuilder insertColumns = new StringBuilder();
        StringBuilder insertValues = new StringBuilder();
        Iterator<String> fieldIter = insertFields.fields().iterator();
        boolean first = true;
        while (fieldIter.hasNext()) {
            String field = fieldIter.next();
            if (!first) {
                insertColumns.append(", ");
                insertValues.append(", ");
            }
            String quotedField = wrapIdentifierQuote(field);
            insertColumns.append(quotedField);
            insertValues.append("source.").append(quotedField);
            first = false;
        }
        return new String[]{insertColumns.toString(), insertValues.toString()};
    }
}
