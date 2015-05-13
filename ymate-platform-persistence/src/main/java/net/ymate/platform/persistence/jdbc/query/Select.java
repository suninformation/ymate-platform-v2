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

    private String __alias;

    public static Select create(IEntity<?> entity) {
        return create(entity.getClass());
    }

    public static Select create(Class<? extends IEntity> entityClass) {
        return new Select(EntityMeta.createAndGet(entityClass).getEntityName());
    }

    public static Select create(Select select) {
        return new Select(select.toString());
    }

    public static Select create(String from) {
        return new Select(from);
    }

    private Select(String from) {
        this.__from = from;
        this.__fields = Fields.create();
        this.__joins = new ArrayList<Join>();
    }

    public Fields getFields() {
        return this.__fields;
    }

    public Select addField(String field) {
        this.__fields.add(field);
        return this;
    }

    public Select addField(Fields fields) {
        this.__fields.add(fields);
        return this;
    }

    public Select join(Join join) {
        __joins.add(join);
        __where.addParam(join.getParams());
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

    public Select alias(String alias) {
        this.__alias = alias;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder _selectSB = new StringBuilder("SELECT ");
        if (__fields.getFields().isEmpty()) {
            _selectSB.append(" * ");
        } else {
            _selectSB.append(StringUtils.join(__fields.getFields(), " = ?, ").concat(" = ? "));
        }
        _selectSB.append("FROM ").append(__from).append(" ")
                .append(__where == null ? "" : __where.toString());
        //
        for (Join _join : __joins) {
            _selectSB.append(" ").append(_join);
        }
        //
        if (StringUtils.isNotBlank(__alias)) {
            return "(".concat(_selectSB.toString()).concat(") ").concat(__alias);
        } else {
            return _selectSB.toString();
        }
    }
}
