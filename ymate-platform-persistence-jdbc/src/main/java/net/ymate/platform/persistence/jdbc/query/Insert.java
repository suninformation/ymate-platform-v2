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

/**
 * Insert语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:00
 */
public final class Insert extends Query<Insert> {

    private final String prefix;

    private String tableName;

    private Class<? extends IEntity> entityClass;

    private final Fields fields;

    private final Params params;

    private Select select;

    private final boolean safePrefix;

    public static Insert create(String prefix, Class<? extends IEntity> entityClass) {
        return new Insert(JDBC.get(), prefix, entityClass);
    }

    public static Insert create(IEntity<?> entity) {
        return create(JDBC.get(), entity.getClass());
    }

    public static Insert create(Class<? extends IEntity> entityClass) {
        return new Insert(JDBC.get(), null, entityClass);
    }

    public static Insert create(String tableName) {
        return new Insert(JDBC.get(), null, tableName, true);
    }

    public static Insert create(String tableName, boolean safePrefix) {
        return new Insert(JDBC.get(), null, tableName, safePrefix);
    }

    public static Insert create(IDatabase owner, String prefix, Class<? extends IEntity> entityClass) {
        return new Insert(owner, prefix, entityClass);
    }

    public static Insert create(IDatabase owner, IEntity<?> entity) {
        return create(owner, entity.getClass());
    }

    public static Insert create(IDatabase owner, Class<? extends IEntity> entityClass) {
        return new Insert(owner, null, entityClass);
    }

    public static Insert create(IDatabase owner, String tableName) {
        return new Insert(owner, null, tableName, true);
    }

    public static Insert create(IDatabase owner, String tableName, boolean safePrefix) {
        return new Insert(owner, null, tableName, safePrefix);
    }

    private Insert(IDatabase owner, String prefix, Class<? extends IEntity> entityClass) {
        super(owner);
        this.prefix = prefix;
        this.entityClass = entityClass;
        this.safePrefix = true;
        this.fields = Fields.create();
        this.params = Params.create();
    }

    private Insert(IDatabase owner, String prefix, String tableName, boolean safePrefix) {
        super(owner);
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

    public Insert select(Select select) {
        this.select = select;
        return this;
    }

    @Override
    public String toString() {
        String sqlStr = "INSERT INTO ".concat(safePrefix ? (entityClass != null ? buildSafeTableName(prefix, EntityMeta.createAndGet(entityClass), safePrefix) : buildSafeTableName(prefix, tableName, true)) : tableName)
                .concat(" (").concat(StringUtils.join(fields.fields(), ", "));
        if (select != null) {
            return sqlStr.concat(") ").concat(select.toString());
        }
        return sqlStr.concat(") VALUES (").concat(StringUtils.repeat("?", ", ", params.params().size())).concat(")");
    }

    public SQL toSQL() {
        return SQL.create(this);
    }

    public int execute() throws Exception {
        return toSQL().execute();
    }

    public int execute(String dataSourceName) throws Exception {
        return toSQL().execute(dataSourceName);
    }

    public int execute(IDatabaseConnectionHolder connectionHolder) throws Exception {
        return toSQL().execute(connectionHolder);
    }
}
