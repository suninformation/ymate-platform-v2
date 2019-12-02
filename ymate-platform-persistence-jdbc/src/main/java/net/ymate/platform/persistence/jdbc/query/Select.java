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
import net.ymate.platform.core.persistence.*;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.IDBLocker;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.base.IResultSetHandler;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Select语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午5:59
 */
public final class Select extends Query<Select> {

    private final List<String> froms = new ArrayList<>();

    private final Fields fields = Fields.create();

    private final List<Join> joins = new ArrayList<>();

    private Where where;

    private final List<Union> unions = new ArrayList<>();

    private String alias;

    private boolean distinct;

    private IDBLocker dbLocker;

    private Page page;

    public static Select create() {
        return new Select(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName());
    }

    public static Select create(Class<? extends IEntity> entityClass) {
        return new Select(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName()).from(null, entityClass, null);
    }

    public static Select create(String prefix, Class<? extends IEntity> entityClass) {
        return new Select(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName()).from(prefix, entityClass, null);
    }

    public static Select create(Class<? extends IEntity> entityClass, String alias) {
        return new Select(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName()).from(null, entityClass, alias);
    }

    public static Select create(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Select(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName()).from(prefix, entityClass, alias);
    }

    public static Select create(String prefix, String from, String alias) {
        return new Select(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), prefix, from, alias, true);
    }

    public static Select create(String from, String alias) {
        return new Select(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), null, from, alias, true);
    }

    public static Select create(String from, String alias, boolean safePrefix) {
        return new Select(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), null, from, alias, safePrefix);
    }

    public static Select create(String from) {
        return new Select(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), null, from, null, true);
    }

    public static Select create(String from, boolean safePrefix) {
        return new Select(JDBC.get(), JDBC.get().getConfig().getDefaultDataSourceName(), null, from, null, safePrefix);
    }

    public static Select create(Select select) {
        Select target = new Select(select.owner(), select.dataSourceName(), null, select.toString(), null, false);
        target.where().param(select.params());
        return target;
    }

    public static Select create(IDatabase owner) {
        return new Select(owner, owner.getConfig().getDefaultDataSourceName());
    }

    public static Select create(IDatabase owner, String dataSourceName) {
        return new Select(owner, dataSourceName);
    }

    public static Select create(IDatabase owner, String dataSourceName, Class<? extends IEntity> entityClass) {
        return new Select(owner, dataSourceName).from(null, entityClass, null);
    }

    public static Select create(IDatabase owner, String dataSourceName, String prefix, Class<? extends IEntity> entityClass) {
        return new Select(owner, dataSourceName).from(prefix, entityClass, null);
    }

    public static Select create(IDatabase owner, String dataSourceName, Class<? extends IEntity> entityClass, String alias) {
        return new Select(owner, dataSourceName).from(null, entityClass, alias);
    }

    public static Select create(IDatabase owner, String dataSourceName, String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Select(owner, dataSourceName).from(prefix, entityClass, alias);
    }

    public static Select create(IDatabase owner, String dataSourceName, String prefix, String from, String alias) {
        return new Select(owner, dataSourceName, prefix, from, alias, true);
    }

    public static Select create(IDatabase owner, String dataSourceName, String from, String alias) {
        return new Select(owner, dataSourceName, null, from, alias, true);
    }

    public static Select create(IDatabase owner, String dataSourceName, String from, String alias, boolean safePrefix) {
        return new Select(owner, dataSourceName, null, from, alias, safePrefix);
    }

    public static Select create(IDatabase owner, String dataSourceName, String from) {
        return new Select(owner, dataSourceName, null, from, null, true);
    }

    public static Select create(IDatabase owner, String dataSourceName, String from, boolean safePrefix) {
        return new Select(owner, dataSourceName, null, from, null, safePrefix);
    }

    private Select(IDatabase owner, String dataSourceName) {
        super(owner, dataSourceName);
    }

    private Select(IDatabase owner, String dataSourceName, String prefix, String from, String alias, boolean safePrefix) {
        super(owner, dataSourceName);
        if (safePrefix) {
            from(null, buildSafeTableName(prefix, from, true), alias);
        } else {
            from(prefix, from, alias);
        }
    }

    public Select from(Class<? extends IEntity> entityClass) {
        return from(null, buildSafeTableName(null, EntityMeta.createAndGet(entityClass), true), null);
    }

    public Select from(Class<? extends IEntity> entityClass, String alias) {
        return from(null, buildSafeTableName(null, EntityMeta.createAndGet(entityClass), true), alias);
    }

    public Select from(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return from(null, buildSafeTableName(prefix, EntityMeta.createAndGet(entityClass), true), alias);
    }

    public Select from(Select select) {
        Select target = from(null, select.toString(), null);
        target.where().param(select.params());
        return target;
    }

    public Select from(String tableName, String alias) {
        return from(null, buildSafeTableName(null, tableName, true), alias);
    }

    public Select from(String tableName) {
        return from(null, buildSafeTableName(null, tableName, true), null);
    }

    public Select from(String prefix, String from, String alias) {
        from = buildSafeTableName(prefix, from, false);
        if (StringUtils.isNotBlank(alias)) {
            from = from.concat(StringUtils.SPACE).concat(alias);
        }
        this.froms.add(from);
        return this;
    }

    public Fields fields() {
        return this.fields;
    }

    public Select field(String field) {
        return field(field, true);
    }

    public Select field(String field, boolean wrapIdentifier) {
        this.fields.add(wrapIdentifier ? wrapIdentifierField(field) : field);
        return this;
    }

    public Select field(String prefix, String field) {
        return field(prefix, field, true);
    }

    public Select field(String prefix, String field, boolean wrapIdentifier) {
        this.fields.add(prefix, wrapIdentifier ? wrapIdentifierField(field) : field);
        return this;
    }

    public Select field(String prefix, String field, String alias) {
        return field(prefix, field, alias, true);
    }

    public Select field(String prefix, String field, String alias, boolean wrapIdentifier) {
        this.fields.add(prefix, wrapIdentifier ? wrapIdentifierField(field) : field, alias);
        return this;
    }

    public Select field(Fields fields) {
        return field(fields, true);
    }

    public Select field(Fields fields, boolean wrapIdentifier) {
        Fields newFields = checkFieldExcluded(fields);
        this.fields.add(wrapIdentifier ? wrapIdentifierFields(newFields.toArray()) : newFields);
        return this;
    }

    public Select field(String prefix, Fields fields) {
        return field(prefix, fields, true);
    }

    public Select field(String prefix, Fields fields, boolean wrapIdentifier) {
        checkFieldExcluded(fields).fields().forEach((field) -> this.fields.add(prefix, wrapIdentifier ? wrapIdentifierField(field) : field));
        return this;
    }

    public Select field(IFunction func, String alias) {
        this.fields.add(func, alias);
        return this;
    }

    public Select field(IFunction func) {
        this.fields.add(func);
        return this;
    }

    public Select join(Join join) {
        joins.add(join);
        where().param(join.params());
        return this;
    }

    public Select innerJoin(Select select, String alias, Cond on) {
        return join(Join.inner(select).alias(alias).on(on));
    }

    public Select leftJoin(Select select, String alias, Cond on) {
        return join(Join.left(select).alias(alias).on(on));
    }

    public Select rightJoin(Select select, String alias, Cond on) {
        return join(Join.right(select).alias(alias).on(on));
    }

    //

    public Select innerJoin(String from, Cond on) {
        return join(Join.inner(owner(), dataSourceName(), from).on(on));
    }

    public Select leftJoin(String from, Cond on) {
        return join(Join.left(owner(), dataSourceName(), from).on(on));
    }

    public Select rightJoin(String from, Cond on) {
        return join(Join.right(owner(), dataSourceName(), from).on(on));
    }

    //

    public Select innerJoin(String prefix, String from, Cond on) {
        return join(Join.inner(owner(), dataSourceName(), prefix, from).on(on));
    }

    public Select leftJoin(String prefix, String from, Cond on) {
        return join(Join.left(owner(), dataSourceName(), prefix, from).on(on));
    }

    public Select rightJoin(String prefix, String from, Cond on) {
        return join(Join.right(owner(), dataSourceName(), prefix, from).on(on));
    }

    //

    public Select innerJoin(String prefix, String from, String alias, Cond on) {
        return join(Join.inner(owner(), dataSourceName(), prefix, from).alias(alias).on(on));
    }

    public Select leftJoin(String prefix, String from, String alias, Cond on) {
        return join(Join.left(owner(), dataSourceName(), prefix, from).alias(alias).on(on));
    }

    public Select rightJoin(String prefix, String from, String alias, Cond on) {
        return join(Join.right(owner(), dataSourceName(), prefix, from).alias(alias).on(on));
    }

    //

    public Select innerJoin(String prefix, String from, boolean safePrefix, String alias, Cond on) {
        return join(Join.inner(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(on));
    }

    public Select leftJoin(String prefix, String from, boolean safePrefix, String alias, Cond on) {
        return join(Join.left(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(on));
    }

    public Select rightJoin(String prefix, String from, boolean safePrefix, String alias, Cond on) {
        return join(Join.right(owner(), dataSourceName(), prefix, from, safePrefix).alias(alias).on(on));
    }

    public Select union(Union union) {
        unions.add(union);
        where().param(union.select().params());
        return this;
    }

    public Select union(Select select) {
        return union(Union.create(select));
    }

    public Select unionAll(Select select) {
        return union(Union.create(select).all());
    }

    public Select where(Where where) {
        where().where(where);
        return this;
    }

    public Params params() {
        return where().params();
    }

    public Where where() {
        if (this.where == null) {
            this.where = Where.create(owner());
        }
        return where;
    }

    public Select where(Cond cond) {
        where().cond().cond(cond);
        return this;
    }

    public Select orderBy(OrderBy orderBy) {
        where().orderBy().orderBy(orderBy);
        return this;
    }

    public Select orderByAsc(String field) {
        where().orderBy().asc(field);
        return this;
    }

    public Select orderByAsc(String prefix, String field) {
        where().orderBy().asc(prefix, field);
        return this;
    }

    public Select orderByDesc(String field) {
        where().orderBy().desc(field);
        return this;
    }

    public Select orderByDesc(String prefix, String field) {
        where().orderBy().desc(prefix, field);
        return this;
    }

    public Select groupBy(GroupBy groupBy) {
        where().groupBy(groupBy);
        return this;
    }

    public Select groupBy(Fields fields) {
        where().groupBy(fields);
        return this;
    }

    public Select groupBy(String prefix, String field) {
        where().groupBy(prefix, field);
        return this;
    }

    public Select groupBy(String field) {
        where().groupBy(field);
        return this;
    }

    public Select having(Cond cond) {
        where().having(cond);
        return this;
    }

    /**
     * 设置Select语句的别名
     *
     * @param alias 别名
     * @return 返回当前Select对象
     */
    public Select alias(String alias) {
        this.alias = alias;
        return this;
    }

    public Select distinct() {
        distinct = true;
        return this;
    }

    public Select forUpdate(IDBLocker dbLocker) {
        this.dbLocker = dbLocker;
        return this;
    }

    public Select page(Page page) {
        this.page = page;
        return this;
    }

    public Select page(Integer page) {
        this.page = Page.create(page);
        return this;
    }

    public Select page(Integer page, Integer pageSize) {
        this.page = Page.createIfNeed(page, pageSize);
        return this;
    }

    @Override
    public String toString() {
        ExpressionUtils expression = ExpressionUtils.bind(getExpressionStr("SELECT ${distinct} ${fields} FROM ${froms} ${joins} ${where} ${unions}"));
        if (queryHandler() != null) {
            queryHandler().beforeBuild(expression, this);
        }
        List<String> variables = expression.getVariables();
        if (distinct && variables.contains("distinct")) {
            expression.set("distinct", "DISTINCT");
        }
        if (fields.fields().isEmpty()) {
            expression.set("fields", "*");
        } else {
            expression.set("fields", StringUtils.join(fields.fields(), LINE_END_FLAG));
        }
        expression.set("froms", StringUtils.join(froms, LINE_END_FLAG));
        //
        if (variables.contains("joins")) {
            expression.set("joins", StringUtils.join(joins, StringUtils.SPACE));
        }
        //
        if (where != null && variables.contains("where")) {
            expression.set("where", where.toString());
        }
        //
        if (!unions.isEmpty() && variables.contains("unions")) {
            StringBuilder unionsBuilder = new StringBuilder();
            unions.stream().peek((union) -> {
                unionsBuilder.append("UNION ");
                if (union.isAll()) {
                    unionsBuilder.append("ALL ");
                }
            }).forEachOrdered((union) -> unionsBuilder.append(union.select()));
            unionsBuilder.append(StringUtils.SPACE);
            //
            expression.set("unions", unionsBuilder.toString());
        }
        if (queryHandler() != null) {
            queryHandler().afterBuild(expression, this);
        }
        String resultStr = StringUtils.trimToEmpty(expression.clean().getResult());
        //
        if (page != null) {
            resultStr = dialect().buildPagedQuerySql(resultStr, page.page(), page.pageSize());
        }
        if (dbLocker != null) {
            resultStr += String.format(" %s", dbLocker.toSQL());
        }
        if (StringUtils.isNotBlank(alias)) {
            resultStr = String.format("(%s) %s", resultStr, alias);
        }
        return resultStr;
    }

    public SQL toSQL() {
        return SQL.create(this);
    }

    public <T> T findFirst(IResultSetHandler<T> handler) throws Exception {
        return toSQL().findFirst(dataSourceName(), handler);
    }

    public <T> IResultSet<T> find(IResultSetHandler<T> handler) throws Exception {
        return toSQL().find(dataSourceName(), handler);
    }

    public <T> IResultSet<T> find(IResultSetHandler<T> handler, Page page) throws Exception {
        return toSQL().find(dataSourceName(), handler, page);
    }

    public long count() throws Exception {
        return toSQL().count(dataSourceName());
    }
}
