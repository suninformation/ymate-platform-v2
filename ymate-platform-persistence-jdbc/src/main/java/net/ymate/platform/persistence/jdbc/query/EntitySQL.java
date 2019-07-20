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
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.IDBLocker;

/**
 * 实体SQL及参数对象
 *
 * @param <T> 实体类型
 * @author 刘镇 (suninformation@163.com) on 15/5/9 下午1:14
 */
public final class EntitySQL<T extends IEntity> {

    /**
     * 实体对象
     */
    private final Class<T> entityClass;

    /**
     * 显示字段过滤集合
     */
    private final Fields fields;

    private IDBLocker dbLocker;

    public static <T extends IEntity> EntitySQL<T> create(Class<T> entityClass) {
        return new EntitySQL<>(entityClass);
    }

    private EntitySQL(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.fields = Fields.create();
    }

    public Class<T> getEntityClass() {
        return this.entityClass;
    }

    public EntitySQL<T> field(String field) {
        this.fields.add(field);
        return this;
    }

    public EntitySQL<T> field(Fields fields) {
        this.fields.add(fields);
        return this;
    }

    public Fields fields() {
        return this.fields;
    }

    public EntitySQL<T> forUpdate(IDBLocker dbLocker) {
        this.dbLocker = dbLocker;
        return this;
    }

    public IDBLocker forUpdate() {
        return dbLocker;
    }
}
