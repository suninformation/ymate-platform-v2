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

/**
 * DELETE语句对象
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/12 下午6:03
 * @version 1.0
 */
public class Delete {

    private String __from;

    private Where __where;

    public static Delete create(IEntity<?> entity) {
        return create(entity.getClass());
    }

    public static Delete create(Class<? extends IEntity> entityClass) {
        return new Delete(EntityMeta.createAndGet(entityClass).getEntityName());
    }

    public static Delete create(String tableName) {
        return new Delete(tableName);
    }

    private Delete(String from) {
        this.__from = from;
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
        return "DELETE FROM ".concat(__from).concat(" ").concat(__where == null ? "" : __where.toString());
    }
}
