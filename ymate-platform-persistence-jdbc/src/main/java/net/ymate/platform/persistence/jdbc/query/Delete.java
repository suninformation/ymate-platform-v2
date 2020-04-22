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
 * DELETE语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:03
 */
public final class Delete extends Query<Delete> {

    private final List<String> froms = new ArrayList<>();

    private final Fields fields = Fields.create();

    private final List<Join> joins = new ArrayList<>();

    private Where where;

    public static Delete create() {
        IDatabase owner = JDBC.get();
        return new Delete(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static Delete create(Class<? extends IEntity> entityClass) {
        return create(null, entityClass, null);
    }

    public static Delete create(String prefix, Class<? extends IEntity> entityClass) {
        return create(prefix, entityClass, null);
    }

    public static Delete create(Class<? extends IEntity> entityClass, String alias) {
        return create(null, entityClass, alias);
    }

    public static Delete create(String prefix, Class<? extends IEntity> entityClass, String alias) {
        IDatabase owner = JDBC.get();
        return new Delete(owner, owner.getConfig().getDefaultDataSourceName()).from(prefix, entityClass, alias);
    }

    public static Delete create(String prefix, String tableName, String alias) {
        IDatabase owner = JDBC.get();
        return new Delete(owner, owner.getConfig().getDefaultDataSourceName(), prefix, tableName, alias, true);
    }

    public static Delete create(String tableName, String alias) {
        return create((String) null, tableName, alias);
    }

    public static Delete create(String tableName, String alias, boolean safePrefix) {
        IDatabase owner = JDBC.get();
        return new Delete(owner, owner.getConfig().getDefaultDataSourceName(), null, tableName, alias, safePrefix);
    }

    public static Delete create(String tableName) {
        return create((String) null, tableName, null);
    }

    public static Delete create(String tableName, boolean safePrefix) {
        return create(tableName, null, safePrefix);
    }

    public static Delete create(Select select) {
        Delete target = new Delete(select.owner(), select.dataSourceName(), null, select.toString(), null, false);
        target.where().param(select.params());
        return target;
    }

    public static Delete create(IDatabase owner) {
        return new Delete(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static Delete create(IDatabase owner, String dataSourceName) {
        return new Delete(owner, dataSourceName);
    }

    public static Delete create(IDatabase owner, String dataSourceName, Class<? extends IEntity> entityClass) {
        return new Delete(owner, dataSourceName).from(entityClass);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String prefix, Class<? extends IEntity> entityClass) {
        return new Delete(owner, dataSourceName).from(prefix, entityClass, null);
    }

    public static Delete create(IDatabase owner, String dataSourceName, Class<? extends IEntity> entityClass, String alias) {
        return new Delete(owner, dataSourceName).from(null, entityClass, alias);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Delete(owner, dataSourceName).from(prefix, entityClass, alias);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String prefix, String tableName, String alias) {
        return new Delete(owner, dataSourceName, prefix, tableName, alias, true);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String tableName, String alias) {
        return new Delete(owner, dataSourceName, null, tableName, alias, true);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String tableName, String alias, boolean safePrefix) {
        return new Delete(owner, dataSourceName, null, tableName, alias, safePrefix);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String tableName) {
        return new Delete(owner, dataSourceName, null, tableName, null, true);
    }

    public static Delete create(IDatabase owner, String dataSourceName, String tableName, boolean safePrefix) {
        return new Delete(owner, dataSourceName, null, tableName, null, safePrefix);
    }

    public static Delete create(Query<?> query) {
        return new Delete(query.owner(), query.dataSourceName());
    }

    public static Delete create(Query<?> query, String prefix, String from, String alias, boolean safePrefix) {
        return new Delete(query.owner(), query.dataSourceName(), prefix, from, alias, safePrefix);
    }

    public Delete(IDatabase owner, String dataSourceName) {
        super(owner, dataSourceName);
    }

    public Delete(IDatabase owner, String dataSourceName, String prefix, String from, String alias, boolean safePrefix) {
        super(owner, dataSourceName);
        //
        from(prefix, from, alias, safePrefix);
    }

    public Delete from(Class<? extends IEntity> entityClass) {
        return from(entityClass, true);
    }

    public Delete from(Class<? extends IEntity> entityClass, boolean safePrefix) {
        return from(null, buildSafeTableName(null, EntityMeta.createAndGet(entityClass), safePrefix), null);
    }

    public Delete from(Class<? extends IEntity> entityClass, String alias) {
        return from(entityClass, alias, true);
    }

    public Delete from(Class<? extends IEntity> entityClass, String alias, boolean safePrefix) {
        return from(null, buildSafeTableName(null, EntityMeta.createAndGet(entityClass), safePrefix), alias);
    }

    public Delete from(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return from(prefix, entityClass, alias, true);
    }

    public Delete from(String prefix, Class<? extends IEntity> entityClass, String alias, boolean safePrefix) {
        return from(null, buildSafeTableName(prefix, EntityMeta.createAndGet(entityClass), safePrefix), alias);
    }

    public Delete from(Select select) {
        Delete target = from(null, select.toString(), null);
        target.where().param(select.params());
        return target;
    }

    public Delete from(String tableName, String alias) {
        return from(null, buildSafeTableName(null, tableName, true), alias);
    }

    public Delete from(String tableName) {
        return from(null, buildSafeTableName(null, tableName, true), null);
    }

    public Delete from(String prefix, String from, String alias) {
        return from(prefix, from, alias, false);
    }

    public Delete from(String prefix, String from, String alias, boolean safePrefix) {
        from = buildSafeTableName(prefix, from, safePrefix);
        if (StringUtils.isNotBlank(alias)) {
            from = from.concat(StringUtils.SPACE).concat(alias);
        }
        this.froms.add(from);
        return this;
    }

    public Delete table(String tableName) {
        this.fields.add(tableName);
        return this;
    }

    public Delete table(String prefix, String tableName) {
        this.fields.add(prefix, tableName);
        return this;
    }

    public Delete join(Join join) {
        joins.add(join);
        where().param(join.params());
        return this;
    }

    public Delete where(Where where) {
        where().where(where);
        return this;
    }

    public Params params() {
        return where().params();
    }

    public Delete param(Object param) {
        where().param(param);
        return this;
    }

    public Delete param(Params params) {
        where().param(params);
        return this;
    }

    public Where where() {
        if (this.where == null) {
            this.where = Where.create(this);
        }
        return where;
    }

    public Delete where(Cond cond) {
        where().cond().cond(cond);
        return this;
    }

    public Delete orderBy(OrderBy orderBy) {
        where().orderBy().orderBy(orderBy);
        return this;
    }

    @Override
    public String toString() {
        ExpressionUtils expression = ExpressionUtils.bind(getExpressionStr("DELETE ${fields} FROM ${froms} ${joins} ${where}"));
        if (queryHandler() != null) {
            queryHandler().beforeBuild(expression, this);
        }
        List<String> variables = expression.getVariables();
        //
        if (!fields.fields().isEmpty() && variables.contains("fields")) {
            expression.set("fields", StringUtils.join(fields.fields(), LINE_END_FLAG));
        }
        expression.set("forms", StringUtils.join(froms, LINE_END_FLAG));
        if (where != null && variables.contains("where")) {
            expression.set("where", where.toString());
        }
        if (variables.contains("joins")) {
            expression.set("joins", StringUtils.join(joins, StringUtils.SPACE));
        }
        if (queryHandler() != null) {
            queryHandler().afterBuild(expression, this);
        }
        return StringUtils.trimToEmpty(expression.clean().getResult());
    }

    public SQL toSQL() {
        return SQL.create(this);
    }

    public int execute() throws Exception {
        return toSQL().execute(dataSourceName());
    }
}
