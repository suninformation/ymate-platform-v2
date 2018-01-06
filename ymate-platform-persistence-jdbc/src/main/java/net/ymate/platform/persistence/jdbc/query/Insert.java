/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.persistence.Fields;
import net.ymate.platform.persistence.Params;
import net.ymate.platform.persistence.base.EntityMeta;
import net.ymate.platform.persistence.base.IEntity;
import org.apache.commons.lang.StringUtils;

/**
 * Insert语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:00
 * @version 1.0
 */
public final class Insert extends Query<Insert> {

    private final String __prefix;

    private String __tableName;

    private Class<? extends IEntity> __entityClass;

    private final Fields __fields;

    private final Params __params;

    private Select __select;

    private final boolean __safePrefix;

    public static Insert create(String prefix, Class<? extends IEntity> entityClass) {
        return new Insert(prefix, entityClass);
    }

    public static Insert create(IEntity<?> entity) {
        return create(entity.getClass());
    }

    public static Insert create(Class<? extends IEntity> entityClass) {
        return new Insert(null, entityClass);
    }

    public static Insert create(String tableName) {
        return new Insert(null, tableName, true);
    }

    public static Insert create(String tableName, boolean safePrefix) {
        return new Insert(null, tableName, safePrefix);
    }

    private Insert(String prefix, Class<? extends IEntity> entityClass) {
        this.__prefix = prefix;
        this.__entityClass = entityClass;
        this.__safePrefix = true;
        this.__fields = Fields.create();
        this.__params = Params.create();
    }

    private Insert(String prefix, String tableName, boolean safePrefix) {
        this.__prefix = prefix;
        this.__tableName = tableName;
        this.__safePrefix = safePrefix;
        this.__fields = Fields.create();
        this.__params = Params.create();
    }

    public Fields fields() {
        return this.__fields;
    }

    public Insert field(String prefix, String field, String alias) {
        return field(prefix, field, alias, true);
    }

    public Insert field(String prefix, String field, String alias, boolean wrapIdentifier) {
        this.__fields.add(prefix, wrapIdentifier ? __wrapIdentifierField(field) : field, alias);
        return this;
    }

    public Insert field(String prefix, String field) {
        return field(prefix, field, true);
    }

    public Insert field(String prefix, String field, boolean wrapIdentifier) {
        this.__fields.add(prefix, wrapIdentifier ? __wrapIdentifierField(field) : field);
        return this;
    }

    public Insert field(String field) {
        return field(field, true);
    }

    public Insert field(String field, boolean wrapIdentifier) {
        this.__fields.add(wrapIdentifier ? __wrapIdentifierField(field) : field);
        return this;
    }

    public Insert field(Fields fields) {
        return field(fields, true);
    }

    public Insert field(Fields fields, boolean wrapIdentifier) {
        this.__fields.add(wrapIdentifier ? __wrapIdentifierFields(fields.toArray()) : fields);
        return this;
    }

    public Insert field(String prefix, Fields fields) {
        return field(prefix, fields, true);
    }

    public Insert field(String prefix, Fields fields, boolean wrapIdentifier) {
        for (String _field : fields.fields()) {
            this.__fields.add(prefix, wrapIdentifier ? __wrapIdentifierField(_field) : _field);
        }
        return this;
    }

    public Params params() {
        return this.__params;
    }

    public Insert param(Object param) {
        this.__params.add(param);
        return this;
    }

    public Insert param(Params params) {
        this.__params.add(params);
        return this;
    }

    public Insert select(Select select) {
        this.__select = select;
        return this;
    }

    @Override
    public String toString() {
        String _sqlStr = "INSERT INTO ".concat(__safePrefix ? (__entityClass != null ? __buildSafeTableName(__prefix, EntityMeta.createAndGet(__entityClass), __safePrefix) : __buildSafeTableName(__prefix, __tableName, true)) : __tableName)
                .concat(" (").concat(StringUtils.join(__fields.fields(), ", "));
        if (__select != null) {
            return _sqlStr.concat(") ").concat(__select.toString());
        }
        return _sqlStr.concat(") VALUES (").concat(StringUtils.repeat("?", ", ", __params.params().size())).concat(")");
    }

    public SQL toSQL() {
        return SQL.create(this);
    }
}
