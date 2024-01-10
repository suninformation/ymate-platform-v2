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

import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Insert语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:00
 */
@SuppressWarnings("rawtypes")
public final class Insert extends Query<Insert> {

    private final String prefix;

    private String tableName;

    private Class<? extends IEntity> entityClass;

    private final Fields fields;

    private final Params params;

    private final List<Params> groupParams = new ArrayList<>();

    private Select select;

    private final boolean safePrefix;

    public static Insert create(String prefix, Class<? extends IEntity> entityClass) {
        IDatabase owner = JDBC.get();
        return new Insert(owner, owner.getConfig().getDefaultDataSourceName(), prefix, entityClass);
    }

    public static Insert create(IEntity<?> entity) {
        return create(null, entity.getClass());
    }

    public static Insert create(Class<? extends IEntity> entityClass) {
        return create(null, entityClass);
    }

    public static Insert create(String tableName) {
        return create(tableName, true);
    }

    public static Insert create(String tableName, boolean safePrefix) {
        IDatabase owner = JDBC.get();
        return new Insert(owner, owner.getConfig().getDefaultDataSourceName(), null, tableName, safePrefix);
    }

    public static Insert create(IDatabase owner, String dataSourceName, String prefix, Class<? extends IEntity> entityClass) {
        return new Insert(owner, dataSourceName, prefix, entityClass);
    }

    public static Insert create(IDatabase owner, String dataSourceName, IEntity<?> entity) {
        return create(owner, dataSourceName, entity.getClass());
    }

    public static Insert create(IDatabase owner, String dataSourceName, Class<? extends IEntity> entityClass) {
        return new Insert(owner, dataSourceName, null, entityClass);
    }

    public static Insert create(IDatabase owner, String dataSourceName, String tableName) {
        return new Insert(owner, dataSourceName, null, tableName, true);
    }

    public static Insert create(IDatabase owner, String dataSourceName, String tableName, boolean safePrefix) {
        return new Insert(owner, dataSourceName, null, tableName, safePrefix);
    }

    public static Insert create(Query<?> query, String prefix, Class<? extends IEntity> entityClass) {
        return new Insert(query.owner(), query.dataSourceName(), prefix, entityClass);
    }

    public static Insert create(Query<?> query, String prefix, String tableName, boolean safePrefix) {
        return new Insert(query.owner(), query.dataSourceName(), prefix, tableName, safePrefix);
    }

    public Insert(IDatabase owner, String dataSourceName, String prefix, Class<? extends IEntity> entityClass) {
        super(owner, dataSourceName);
        this.prefix = prefix;
        this.entityClass = entityClass;
        this.safePrefix = true;
        this.fields = Fields.create();
        this.params = Params.create();
    }

    public Insert(IDatabase owner, String dataSourceName, String prefix, String tableName, boolean safePrefix) {
        super(owner, dataSourceName);
        this.prefix = prefix;
        this.tableName = tableName;
        this.safePrefix = safePrefix;
        this.fields = Fields.create();
        this.params = Params.create();
    }

    public Fields fields() {
        return this.fields;
    }

    public Insert field(String prefix, String field, String alias) {
        return field(prefix, field, alias, true);
    }

    public Insert field(String prefix, String field, String alias, boolean wrapIdentifier) {
        this.fields.add(prefix, wrapIdentifier ? wrapIdentifierField(field) : field, alias);
        return this;
    }

    public Insert field(String prefix, String field) {
        return field(prefix, field, true);
    }

    public Insert field(String prefix, String field, boolean wrapIdentifier) {
        this.fields.add(prefix, wrapIdentifier ? wrapIdentifierField(field) : field);
        return this;
    }

    public Insert field(String field) {
        return field(field, true);
    }

    public Insert field(String field, boolean wrapIdentifier) {
        this.fields.add(wrapIdentifier ? wrapIdentifierField(field) : field);
        return this;
    }

    /**
     * @since 2.1.3
     */
    public Insert fieldAlias(String field, String alias) {
        return fieldAlias(field, alias, true);
    }

    /**
     * @since 2.1.3
     */
    public Insert fieldAlias(String field, String alias, boolean wrapIdentifier) {
        this.fields.addAlias(wrapIdentifier ? wrapIdentifierField(field) : field, alias);
        return this;
    }

    public Insert field(Fields fields) {
        return field(fields, true);
    }

    public Insert field(Fields fields, boolean wrapIdentifier) {
        Fields newFields = checkFieldExcluded(fields);
        this.fields.add(wrapIdentifier ? wrapIdentifierFields(newFields.toArray()) : newFields);
        return this;
    }

    public Insert field(String prefix, Fields fields) {
        return field(prefix, fields, true);
    }

    public Insert field(String prefix, Fields fields, boolean wrapIdentifier) {
        checkFieldExcluded(fields).fields().forEach((field) -> this.fields.add(prefix, wrapIdentifier ? wrapIdentifierField(field) : field));
        return this;
    }

    public Params params() {
        return this.params;
    }

    public Insert param(Object param) {
        this.params.add(param);
        return this;
    }

    public Insert param(Params params) {
        this.params.add(params);
        return this;
    }

    public Insert addGroupParam(Params params) {
        groupParams.add(params);
        return this;
    }

    public Insert select(Select select) {
        this.select = select;
        return this;
    }

    @Override
    public String toString() {
        ExpressionUtils expression = ExpressionUtils.bind(getExpressionStr("INSERT INTO ${tableName} ${fields} ${values}"));
        if (queryHandler() != null) {
            queryHandler().beforeBuild(expression, this);
        }
        expression.set("tableName", entityClass != null ? buildSafeTableName(prefix, EntityMeta.createAndGet(entityClass), safePrefix) : buildSafeTableName(prefix, tableName, safePrefix));
        if (select != null) {
            expression.set("values", select.toString());
            if (!fields.isEmpty()) {
                expression.set("fields", String.format("(%s)", StringUtils.join(fields.fields(), LINE_END_FLAG)));
            }
            params.clear().add(select.params());
        } else {
            List<String> valuesStr = new ArrayList<>();
            String valueStr = StringUtils.repeat("?", LINE_END_FLAG, fields.fields().size());
            if (!params.isEmpty()) {
                valuesStr.add(String.format("(%s)", valueStr));
            }
            if (!groupParams.isEmpty()) {
                groupParams.stream().filter(p -> !p.isEmpty()).map(p -> String.format("(%s)", valueStr)).forEach(valuesStr::add);
            }
            if (valuesStr.isEmpty()) {
                // 为了保证SQL语句的完整性
                valuesStr.add(String.format("(%s)", valueStr));
            }
            expression.set("fields", String.format("(%s)", StringUtils.join(fields.fields(), LINE_END_FLAG)));
            expression.set("values", String.format("VALUES %s", StringUtils.join(valuesStr, LINE_END_FLAG)));
        }
        if (queryHandler() != null) {
            queryHandler().afterBuild(expression, this);
        }
        return StringUtils.trimToEmpty(expression.clean().getResult());
    }

    public SQL toSQL() {
        SQL sql = SQL.create(this);
        if (select == null) {
            groupParams.forEach(sql::param);
        }
        return sql;
    }

    public int execute() throws Exception {
        return toSQL().execute(dataSourceName());
    }
}
