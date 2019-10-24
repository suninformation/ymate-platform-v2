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

import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.JDBC;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Update语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:02
 */
public final class Update extends Query<Update> {

    private final List<String> tables = new ArrayList<>();

    private final Fields fields = Fields.create();

    private final List<Join> joins = new ArrayList<>();

    private Where where;

    public static Update create() {
        return new Update(JDBC.get());
    }

    public static Update create(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Update(JDBC.get()).table(prefix, entityClass, alias);
    }

    public static Update create(String prefix, Class<? extends IEntity> entityClass) {
        return new Update(JDBC.get()).table(prefix, entityClass, null);
    }

    public static Update create(Class<? extends IEntity> entityClass) {
        return new Update(JDBC.get()).table(entityClass, null);
    }

    public static Update create(String prefix, String tableName, String alias) {
        return new Update(JDBC.get(), prefix, tableName, alias, true);
    }

    public static Update create(String prefix, String tableName, String alias, boolean safePrefix) {
        return new Update(JDBC.get(), prefix, tableName, alias, safePrefix);
    }

    public static Update create(String tableName, String alias) {
        return new Update(JDBC.get(), null, tableName, alias, true);
    }

    public static Update create(String tableName, String alias, boolean safePrefix) {
        return new Update(JDBC.get(), null, tableName, alias, safePrefix);
    }

    public static Update create(String tableName) {
        return new Update(JDBC.get(), null, tableName, null, true);
    }

    public static Update create(String tableName, boolean safePrefix) {
        return new Update(JDBC.get(), null, tableName, null, safePrefix);
    }

    public static Update create(IDatabase owner) {
        return new Update(owner);
    }

    public static Update create(IDatabase owner, String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Update(owner).table(prefix, entityClass, alias);
    }

    public static Update create(IDatabase owner, String prefix, Class<? extends IEntity> entityClass) {
        return new Update(owner).table(prefix, entityClass, null);
    }

    public static Update create(IDatabase owner, Class<? extends IEntity> entityClass) {
        return new Update(owner).table(entityClass, null);
    }

    public static Update create(IDatabase owner, String prefix, String tableName, String alias) {
        return new Update(owner, prefix, tableName, alias, true);
    }

    public static Update create(IDatabase owner, String prefix, String tableName, String alias, boolean safePrefix) {
        return new Update(owner, prefix, tableName, alias, safePrefix);
    }

    public static Update create(IDatabase owner, String tableName, String alias) {
        return new Update(owner, null, tableName, alias, true);
    }

    public static Update create(IDatabase owner, String tableName, String alias, boolean safePrefix) {
        return new Update(owner, null, tableName, alias, safePrefix);
    }

    public static Update create(IDatabase owner, String tableName) {
        return new Update(owner, null, tableName, null, true);
    }

    public static Update create(IDatabase owner, String tableName, boolean safePrefix) {
        return new Update(owner, null, tableName, null, safePrefix);
    }

    private Update(IDatabase owner) {
        super(owner);
    }

    private Update(IDatabase owner, String prefix, String tableName, String alias, boolean safePrefix) {
        super(owner);
        //
        table(prefix, tableName, alias, safePrefix);
    }

    public Update table(Class<? extends IEntity> entityClass) {
        return table(null, buildSafeTableName(null, EntityMeta.createAndGet(entityClass), true), null, false);
    }

    public Update table(Class<? extends IEntity> entityClass, String alias) {
        return table(null, buildSafeTableName(null, EntityMeta.createAndGet(entityClass), true), alias, false);
    }

    public Update table(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return table(null, buildSafeTableName(prefix, EntityMeta.createAndGet(entityClass), true), alias, false);
    }

    public Update table(String tableName, String alias) {
        return table(null, tableName, alias, true);
    }

    public Update table(String tableName) {
        return table(null, tableName, null, true);
    }

    public Update table(String prefix, String from, String alias) {
        return table(prefix, from, alias, true);
    }

    public Update table(String prefix, String from, String alias, boolean safePrefix) {
        from = buildSafeTableName(prefix, from, safePrefix);
        if (StringUtils.isNotBlank(alias)) {
            from = from.concat(StringUtils.SPACE).concat(alias);
        }
        this.tables.add(from);
        return this;
    }

    public Fields fields() {
        return this.fields;
    }

    public Update field(String field) {
        return field(field, true);
    }

    public Update field(String field, boolean wrapIdentifier) {
        this.fields.add(wrapIdentifier ? wrapIdentifierField(field) : field);
        return this;
    }

    public Update field(String prefix, String field) {
        return field(prefix, field, true);
    }

    public Update field(String prefix, String field, boolean wrapIdentifier) {
        this.fields.add(prefix, wrapIdentifier ? wrapIdentifierField(field) : field);
        return this;
    }

    public Update field(String prefix, String field, String alias) {
        return field(prefix, field, alias, true);
    }

    public Update field(String prefix, String field, String alias, boolean wrapIdentifier) {
        this.fields.add(prefix, wrapIdentifier ? wrapIdentifierField(field) : field, alias);
        return this;
    }

    public Update field(Fields fields) {
        return field(fields, true);
    }

    public Update field(Fields fields, boolean wrapIdentifier) {
        Fields newFields = checkFieldExcluded(fields);
        this.fields.add(wrapIdentifier ? wrapIdentifierFields(newFields.toArray()) : newFields);
        return this;
    }

    public Update field(String prefix, Fields fields) {
        return field(prefix, fields, true);
    }

    public Update field(String prefix, Fields fields, boolean wrapIdentifier) {
        checkFieldExcluded(fields).fields().forEach((field) -> this.fields.add(prefix, wrapIdentifier ? wrapIdentifierField(field) : field));
        return this;
    }

    public Update join(Join join) {
        joins.add(join);
        where().param(join.params());
        return this;
    }

    public Update where(Where where) {
        where().where(where);
        return this;
    }

    public Params getParams() {
        return where().getParams();
    }

    public Update param(Object param) {
        where().param(param);
        return this;
    }

    public Update param(Params params) {
        where().param(params);
        return this;
    }

    public Where where() {
        if (this.where == null) {
            this.where = Where.create(owner());
        }
        return where;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("UPDATE ")
                .append(StringUtils.join(tables, ", "));
        //
        joins.forEach((join) -> stringBuilder.append(StringUtils.SPACE).append(join));
        //
        stringBuilder.append(" SET ");
        boolean flag = false;
        for (String field : fields.fields()) {
            if (flag) {
                stringBuilder.append(", ");
            }
            //
            stringBuilder.append(field);
            //
            if (!field.contains("=")) {
                stringBuilder.append(" = ?");
            }
            //
            flag = true;
        }
        //
        if (where != null) {
            stringBuilder.append(StringUtils.SPACE).append(where);
        }
        return stringBuilder.toString();
    }

    public SQL toSQL() {
        return SQL.create(this);
    }

    public int execute() throws Exception {
        return owner().openSession(session -> session.executeForUpdate(toSQL()));
    }

    public int execute(String dataSourceName) throws Exception {
        return owner().openSession(dataSourceName, session -> session.executeForUpdate(toSQL()));
    }

    public int execute(IDatabaseConnectionHolder connectionHolder) throws Exception {
        return owner().openSession(connectionHolder, session -> session.executeForUpdate(toSQL()));
    }
}
