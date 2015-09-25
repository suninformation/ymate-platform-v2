/*
 * Copyright 2007-2107 the original author or authors.
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

import net.ymate.platform.persistence.base.EntityMeta;
import net.ymate.platform.persistence.base.IEntity;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Select语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午5:59
 * @version 1.0
 */
public class Select {

    private String __from;

    private Fields __fields;

    private List<Join> __joins;

    private Where __where;

    private List<Union> __unions;

    private String __alias;

    private boolean __distinct;

    public static Select create(Class<? extends IEntity> entityClass) {
        return new Select(null, EntityMeta.createAndGet(entityClass).getEntityName(), null);
    }

    public static Select create(String prefix, Class<? extends IEntity> entityClass) {
        return new Select(prefix, EntityMeta.createAndGet(entityClass).getEntityName(), null);
    }

    public static Select create(Class<? extends IEntity> entityClass, String alias) {
        return new Select(null, EntityMeta.createAndGet(entityClass).getEntityName(), alias);
    }

    public static Select create(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Select(prefix, EntityMeta.createAndGet(entityClass).getEntityName(), alias);
    }

    public static Select create(Select select) {
        return new Select(null, select.toString(), null);
    }

    public static Select create(String prefix, String from, String alias) {
        return new Select(prefix, from, alias);
    }

    public static Select create(String from, String alias) {
        return new Select(null, from, alias);
    }

    public static Select create(String from) {
        return new Select(null, from, null);
    }

    private Select(String prefix, String from, String alias) {
        if (StringUtils.isNotBlank(prefix)) {
            from = prefix.concat(from);
        }
        if (StringUtils.isNotBlank(alias)) {
            from = from.concat(" ").concat(alias);
        }
        this.__from = from;
        this.__fields = Fields.create();
        this.__joins = new ArrayList<Join>();
        this.__unions = new ArrayList<Union>();
    }

    public Fields getFields() {
        return this.__fields;
    }

    public Select field(String field) {
        this.__fields.add(field);
        return this;
    }

    public Select field(Fields fields) {
        this.__fields.add(fields);
        return this;
    }

    public Select join(Join join) {
        __joins.add(join);
        if (__where == null) {
            __where = Where.create();
        }
        __where.param(join.getParams());
        return this;
    }

    public Select union(Union union) {
        __unions.add(union);
        if (__where == null) {
            __where = Where.create();
        }
        __where.param(union.select().getParams());
        return this;
    }

    public Select where(Where where) {
        this.__where = where;
        return this;
    }

    public Params getParams() {
        if (this.__where == null) {
            return Params.create();
        }
        return this.__where.getParams();
    }

    /**
     * 设置Select语句的别名
     *
     * @param alias
     * @return
     */
    public Select alias(String alias) {
        this.__alias = alias;
        return this;
    }

    public Select distinct() {
        __distinct = true;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder _selectSB = new StringBuilder("SELECT ");
        if (__distinct) {
            _selectSB.append("DISTINCT ");
        }
        if (__fields.getFields().isEmpty()) {
            _selectSB.append(" * ");
        } else {
            _selectSB.append(StringUtils.join(__fields.getFields(), ", "));
        }
        _selectSB.append(" FROM ").append(__from);
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
            _selectSB.append(_union.select().toString());
        }
        _selectSB.append(" ");
        //
        if (StringUtils.isNotBlank(__alias)) {
            return "(".concat(_selectSB.toString()).concat(") ").concat(__alias);
        }
        return _selectSB.toString();
    }
}
