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
package net.ymate.platform.persistence.jdbc.base.impl;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.PropertyMeta;
import net.ymate.platform.persistence.jdbc.base.AbstractResultSetHandler;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.ResultSet;

/**
 * 采用实体类存储数据的结果集数据处理接口实现
 *
 * @param <T> 实体类型
 * @author 刘镇 (suninformation@163.com) on 15/5/8 下午3:58
 */
@SuppressWarnings("rawtypes")
public class EntityResultSetHandler<T extends IEntity> extends AbstractResultSetHandler<T> {

    private final Class<T> entityClass;

    private final EntityMeta entityMeta;

    @SuppressWarnings("unchecked")
    public EntityResultSetHandler() {
        this.entityClass = (Class<T>) ClassUtils.getParameterizedTypes(getClass()).get(0);
        this.entityMeta = EntityMeta.createAndGet(this.entityClass);
    }

    public EntityResultSetHandler(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.entityMeta = EntityMeta.createAndGet(entityClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected T processResultRow(ResultSet resultSet) throws Exception {
        T returnValue = entityClass.newInstance();
        Object primaryKeyObject = null;
        if (entityMeta.isMultiplePrimaryKey()) {
            primaryKeyObject = entityMeta.getPrimaryKeyClass().newInstance();
            //
            returnValue.setId((Serializable) primaryKeyObject);
        }
        for (int idx = 0; idx < getColumnCount(); idx++) {
            PropertyMeta propertyMeta = entityMeta.getPropertyByName(getColumnMeta(idx).getName());
            if (propertyMeta != null) {
                Field field = propertyMeta.getField();
                Object fieldValue = processValueRenderer(field, BlurObject.bind(resultSet.getObject(idx + 1)).toObjectValue(field.getType()));
                if (entityMeta.isPrimaryKey(propertyMeta.getName()) && entityMeta.isMultiplePrimaryKey()) {
                    propertyMeta.getField().set(primaryKeyObject, fieldValue);
                } else {
                    propertyMeta.getField().set(returnValue, fieldValue);
                }
            }
        }
        return returnValue;
    }
}
