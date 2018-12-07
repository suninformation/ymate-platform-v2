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
import net.ymate.platform.persistence.IFunction;
import net.ymate.platform.persistence.Page;
import net.ymate.platform.persistence.Params;
import net.ymate.platform.persistence.base.EntityMeta;
import net.ymate.platform.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Select语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午5:59
 * @version 1.0
 */
public final class Select extends Query<Select> {

    private List<String> __froms;

    private Fields __fields;

    private List<Join> __joins;

    private Where __where;

    private List<Union> __unions;

    private String __alias;

    private boolean __distinct;

    private IDBLocker __dbLocker;

    private Page __page;

    public static Select create() {
        return new Select();
    }

    public static Select create(Class<? extends IEntity> entityClass) {
        return new Select().from(null, entityClass, null);
    }

    public static Select create(String prefix, Class<? extends IEntity> entityClass) {
        return new Select().from(prefix, entityClass, null);
    }

    public static Select create(Class<? extends IEntity> entityClass, String alias) {
        return new Select().from(null, entityClass, alias);
    }

    public static Select create(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Select().from(prefix, entityClass, alias);
    }

    public static Select create(Select select) {
        Select _target = new Select(null, select.toString(), null, false);
        _target.where().param(select.getParams());
        return _target;
    }

    public static Select create(String prefix, String from, String alias) {
        return new Select(prefix, from, alias, true);
    }

    public static Select create(String from, String alias) {
        return new Select(null, from, alias, true);
    }

    public static Select create(String from, String alias, boolean safePrefix) {
        return new Select(null, from, alias, safePrefix);
    }

    public static Select create(String from) {
        return new Select(null, from, null, true);
    }

    public static Select create(String from, boolean safePrefix) {
        return new Select(null, from, null, safePrefix);
    }

    private Select() {
        this.__froms = new ArrayList<String>();
        this.__fields = Fields.create();
        this.__joins = new ArrayList<Join>();
        this.__unions = new ArrayList<Union>();
    }

    private Select(String prefix, String from, String alias, boolean safePrefix) {
        this();
        if (safePrefix) {
            from(null, __buildSafeTableName(prefix, from, true), alias);
        } else {
            from(prefix, from, alias);
        }
    }

    public Select from(Class<? extends IEntity> entityClass) {
        return from(null, __buildSafeTableName(null, EntityMeta.createAndGet(entityClass), true), null);
    }

    public Select from(Class<? extends IEntity> entityClass, String alias) {
        return from(null, __buildSafeTableName(null, EntityMeta.createAndGet(entityClass), true), alias);
    }

    public Select from(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return from(null, __buildSafeTableName(prefix, EntityMeta.createAndGet(entityClass), true), alias);
    }

    public Select from(Select select) {
        Select _target = from(null, select.toString(), null);
        _target.where().param(select.getParams());
        return _target;
    }

    public Select from(String tableName, String alias) {
        return from(null, __buildSafeTableName(null, tableName, true), alias);
    }

    public Select from(String tableName) {
        return from(null, __buildSafeTableName(null, tableName, true), null);
    }

    public Select from(String prefix, String from, String alias) {
        from = __buildSafeTableName(prefix, from, false);
        if (StringUtils.isNotBlank(alias)) {
            from = from.concat(" ").concat(alias);
        }
        this.__froms.add(from);
        return this;
    }

    public Fields fields() {
        return this.__fields;
    }

    public Select field(String field) {
        return field(field, true);
    }

    public Select field(String field, boolean wrapIdentifier) {
        this.__fields.add(wrapIdentifier ? __wrapIdentifierField(field) : field);
        return this;
    }

    public Select field(String prefix, String field) {
        return field(prefix, field, true);
    }

    public Select field(String prefix, String field, boolean wrapIdentifier) {
        this.__fields.add(prefix, wrapIdentifier ? __wrapIdentifierField(field) : field);
        return this;
    }

    public Select field(String prefix, String field, String alias) {
        return field(prefix, field, alias, true);
    }

    public Select field(String prefix, String field, String alias, boolean wrapIdentifier) {
        this.__fields.add(prefix, wrapIdentifier ? __wrapIdentifierField(field) : field, alias);
        return this;
    }

    public Select field(Fields fields) {
        return field(fields, true);
    }

    public Select field(Fields fields, boolean wrapIdentifier) {
        Fields _field = __checkFieldExcluded(fields);
        this.__fields.add(wrapIdentifier ? __wrapIdentifierFields(_field.toArray()) : _field);
        return this;
    }

    public Select field(String prefix, Fields fields) {
        return field(prefix, fields, true);
    }

    public Select field(String prefix, Fields fields, boolean wrapIdentifier) {
        for (String _field : __checkFieldExcluded(fields).fields()) {
            this.__fields.add(prefix, wrapIdentifier ? __wrapIdentifierField(_field) : _field);
        }
        return this;
    }

    public Select field(IFunction func, String alias) {
        this.__fields.add(func, alias);
        return this;
    }

    public Select join(Join join) {
        __joins.add(join);
        where().param(join.params());
        return this;
    }

    public Select union(Union union) {
        __unions.add(union);
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
        if (this.__where == null) {
            this.__where = Where.create();
        }
        return __where;
    }

    /**
     * 设置Select语句的别名
     *
     * @param alias 别名
     * @return 返回当前Select对象
     */
    public Select alias(String alias) {
        this.__alias = alias;
        return this;
    }

    public Select distinct() {
        __distinct = true;
        return this;
    }

    public Select forUpdate(IDBLocker dbLocker) {
        __dbLocker = dbLocker;
        return this;
    }

    public Select page(Page page) {
        __page = page;
        return this;
    }

    public Select page(IDialect dialect, Page page) {
        this.dialect(dialect);
        __page = page;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder _selectSB = new StringBuilder("SELECT ");
        if (__distinct) {
            _selectSB.append("DISTINCT ");
        }
        if (__fields.fields().isEmpty()) {
            _selectSB.append(" * ");
        } else {
            _selectSB.append(StringUtils.join(__fields.fields(), ", "));
        }
        _selectSB.append(" FROM ").append(StringUtils.join(__froms, ", "));
        //
        for (Join _join : __joins) {
            _selectSB.append(" ").append(_join);
        }
        //
        if (__where != null) {
            _selectSB.append(" ").append(__where.toString());
        }
        //
        for (Union _union : __unions) {
            _selectSB.append(" UNION ");
            if (_union.isAll()) {
                _selectSB.append("ALL ");
            }
            _selectSB.append(_union.select());
        }
        _selectSB.append(" ");
        //
        if (__page != null) {
            _selectSB = new StringBuilder(this.dialect().buildPagedQuerySQL(_selectSB.toString(), __page.page(), __page.pageSize())).append(" ");
        }
        //
        if (StringUtils.isNotBlank(__alias)) {
            return "(".concat(_selectSB.toString()).concat(") ").concat(__alias);
        }
        //
        if (__dbLocker != null) {
            _selectSB.append(__dbLocker.toSQL());
        }
        return _selectSB.toString();
    }

    public SQL toSQL() {
        return SQL.create(this);
    }
}
