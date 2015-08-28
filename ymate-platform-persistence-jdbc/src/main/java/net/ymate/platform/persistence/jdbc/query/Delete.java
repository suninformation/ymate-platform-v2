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
 * DELETE语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:03
 * @version 1.0
 */
public class Delete {

    private String __from;

    private List<Join> __joins;

    private Where __where;

    public static Delete create(Class<? extends IEntity> entityClass) {
        return new Delete(null, EntityMeta.createAndGet(entityClass).getEntityName(), null);
    }

    public static Delete create(String prefix, Class<? extends IEntity> entityClass) {
        return new Delete(prefix, EntityMeta.createAndGet(entityClass).getEntityName(), null);
    }

    public static Delete create(Class<? extends IEntity> entityClass, String alias) {
        return new Delete(null, EntityMeta.createAndGet(entityClass).getEntityName(), alias);
    }

    public static Delete create(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Delete(prefix, EntityMeta.createAndGet(entityClass).getEntityName(), alias);
    }

    public static Delete create(Select select) {
        return new Delete(null, select.toString(), null);
    }

    public static Delete create(String prefix, String tableName, String alias) {
        return new Delete(prefix, tableName, alias);
    }

    public static Delete create(String tableName, String alias) {
        return new Delete(null, tableName, alias);
    }

    public static Delete create(String tableName) {
        return new Delete(null, tableName, null);
    }

    private Delete(String prefix, String from, String alias) {
        if (StringUtils.isNotBlank(prefix)) {
            from = prefix.concat(from);
        }
        if (StringUtils.isNotBlank(alias)) {
            from = from.concat(" ").concat(alias);
        }
        this.__from = from;
        this.__joins = new ArrayList<Join>();
    }

    public Delete join(Join join) {
        __joins.add(join);
        if (__where == null) {
            __where = Where.create();
        }
        __where.param(join.getParams());
        return this;
    }

    public Delete where(Where where) {
        this.__where = where;
        return this;
    }

    public Params getParams() {
        if (this.__where == null) {
            return Params.create();
        }
        return this.__where.getParams();
    }

    @Override
    public String toString() {
        StringBuilder _deleteSB = new StringBuilder("DELETE FROM ").append(__from).append(" ");
        //
        for (Join _join : __joins) {
            _deleteSB.append(" ").append(_join);
        }
        //
        if (__where != null) {
            _deleteSB.append(" ").append(__where.toString());
        }
        return _deleteSB.toString();
    }
}
