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

/**
 * Update语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:02
 * @version 1.0
 */
public class Update {

    private String __tableName;

    private Fields __fields;

    private Where __where;

    public static Update create(String prefix, Class<? extends IEntity> entityClass, String alias) {
        return new Update(prefix, EntityMeta.createAndGet(entityClass).getEntityName(), alias);
    }

    public static Update create(String prefix, Class<? extends IEntity> entityClass) {
        return new Update(prefix, EntityMeta.createAndGet(entityClass).getEntityName(), null);
    }

    public static Update create(Class<? extends IEntity> entityClass) {
        return new Update(null, EntityMeta.createAndGet(entityClass).getEntityName(), null);
    }

    public static Update create(String prefix, String tableName, String alias) {
        return new Update(prefix, tableName, alias);
    }

    public static Update create(String tableName, String alias) {
        return new Update(null, tableName, alias);
    }

    public static Update create(String tableName) {
        return new Update(null, tableName, null);
    }

    private Update(String prefix, String tableName, String alias) {
        if (StringUtils.isNotBlank(prefix)) {
            tableName = prefix.concat(tableName);
        }
        if (StringUtils.isNotBlank(alias)) {
            tableName = tableName.concat(" ").concat(alias);
        }
        this.__tableName = tableName;
        this.__fields = Fields.create();
    }

    public Fields getFields() {
        return this.__fields;
    }

    public Update field(String field) {
        this.__fields.add(field);
        return this;
    }

    public Update field(Fields fields) {
        this.__fields.add(fields);
        return this;
    }

    public Update where(Where where) {
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
        return "UPDATE ".concat(__tableName).concat(" SET (")
                .concat(StringUtils.join(__fields.getFields(), " = ?, ").concat(" = ?"))
                .concat(") ").concat(__where == null ? "" : __where.toString());
    }
}
