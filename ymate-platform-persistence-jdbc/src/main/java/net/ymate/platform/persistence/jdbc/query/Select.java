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

import net.ymate.platform.core.persistence.*;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.IDBLocker;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.base.IResultSetHandler;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
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
        return new Select(JDBC.get());
    }

    public static Select create(Class<? extends IEntity> entityClass) {
        return new Select(JDBC.get()).from(null, entityClass, null);
    }

    public static Select create(String prefix, Class<? extends IEntity> entityClass) {
        return new Select(JDBC.get()).from(prefix, entityClass, null);
    }

    public static Select create(Class<? extends IEntity> entityClass, String alias) {
        return new Select(JDBC.get()).from(null, entityClass, alias);
    }

    public static Select create(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Select(JDBC.get()).from(prefix, entityClass, alias);
    }

    public static Select create(String prefix, String from, String alias) {
        return new Select(JDBC.get(), prefix, from, alias, true);
    }

    public static Select create(String from, String alias) {
        return new Select(JDBC.get(), null, from, alias, true);
    }

    public static Select create(String from, String alias, boolean safePrefix) {
        return new Select(JDBC.get(), null, from, alias, safePrefix);
    }

    public static Select create(String from) {
        return new Select(JDBC.get(), null, from, null, true);
    }

    public static Select create(String from, boolean safePrefix) {
        return new Select(JDBC.get(), null, from, null, safePrefix);
    }

    public static Select create(IDatabase owner) {
        return new Select(owner);
    }

    public static Select create(IDatabase owner, Class<? extends IEntity> entityClass) {
        return new Select(owner).from(null, entityClass, null);
    }

    public static Select create(IDatabase owner, String prefix, Class<? extends IEntity> entityClass) {
        return new Select(owner).from(prefix, entityClass, null);
    }

    public static Select create(IDatabase owner, Class<? extends IEntity> entityClass, String alias) {
        return new Select(owner).from(null, entityClass, alias);
    }

    public static Select create(IDatabase owner, String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Select(owner).from(prefix, entityClass, alias);
    }

    public static Select create(Select select) {
        Select target = new Select(select.owner(), null, select.toString(), null, false);
        target.where().param(select.getParams());
        return target;
    }

    public static Select create(IDatabase owner, String prefix, String from, String alias) {
        return new Select(owner, prefix, from, alias, true);
    }

    public static Select create(IDatabase owner, String from, String alias) {
        return new Select(owner, null, from, alias, true);
    }

    public static Select create(IDatabase owner, String from, String alias, boolean safePrefix) {
        return new Select(owner, null, from, alias, safePrefix);
    }

    public static Select create(IDatabase owner, String from) {
        return new Select(owner, null, from, null, true);
    }

    public static Select create(IDatabase owner, String from, boolean safePrefix) {
        return new Select(owner, null, from, null, safePrefix);
    }

    private Select(IDatabase owner) {
        super(owner);
    }

    private Select(IDatabase owner, String prefix, String from, String alias, boolean safePrefix) {
        super(owner);
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
        target.where().param(select.getParams());
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

    public Select join(Join join) {
        joins.add(join);
        where().param(join.params());
        return this;
    }

    public Select union(Union union) {
        unions.add(union);
        where().param(union.select().getParams());
        return this;
    }

    public Select where(Where where) {
        where().where(where);
        return this;
    }

    public Params getParams() {
        return where().getParams();
    }

    public Where where() {
        if (this.where == null) {
            this.where = Where.create(owner());
        }
        return where;
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

    public Select page(IDialect dialect, Page page) {
        this.dialect(dialect);
        this.page = page;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("SELECT ");
        if (distinct) {
            stringBuilder.append("DISTINCT ");
        }
        if (fields.fields().isEmpty()) {
            stringBuilder.append(" * ");
        } else {
            stringBuilder.append(StringUtils.join(fields.fields(), ", "));
        }
        stringBuilder.append(" FROM ").append(StringUtils.join(froms, ", "));
        //
        for (Join join : joins) {
            stringBuilder.append(StringUtils.SPACE).append(join);
        }
        //
        if (where != null) {
            stringBuilder.append(StringUtils.SPACE).append(where.toString());
        }
        //
        for (Union union : unions) {
            stringBuilder.append(" UNION ");
            if (union.isAll()) {
                stringBuilder.append("ALL ");
            }
            stringBuilder.append(union.select());
        }
        stringBuilder.append(StringUtils.SPACE);
        //
        if (page != null) {
            stringBuilder = new StringBuilder(this.dialect().buildPagedQuerySql(stringBuilder.toString(), page.page(), page.pageSize())).append(StringUtils.SPACE);
        }
        //
        if (StringUtils.isNotBlank(alias)) {
            return "(".concat(stringBuilder.toString()).concat(") ").concat(alias);
        }
        //
        if (dbLocker != null) {
            stringBuilder.append(dbLocker.toSQL());
        }
        return stringBuilder.toString();
    }

    public SQL toSQL() {
        return SQL.create(this);
    }

    public <T> IResultSet<T> execute(IResultSetHandler<T> handler) throws Exception {
        return owner().openSession(session -> session.find(toSQL(), handler));
    }

    public <T> IResultSet<T> execute(String dataSourceName, IResultSetHandler<T> handler) throws Exception {
        return owner().openSession(dataSourceName, session -> session.find(toSQL(), handler));
    }

    public <T> IResultSet<T> execute(IDatabaseConnectionHolder connectionHolder, IResultSetHandler<T> handler) throws Exception {
        return owner().openSession(connectionHolder, session -> session.find(toSQL(), handler));
    }
}
