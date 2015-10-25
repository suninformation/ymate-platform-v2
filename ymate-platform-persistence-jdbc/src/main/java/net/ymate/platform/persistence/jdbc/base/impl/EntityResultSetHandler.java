/*
 * Copyright 2007-2016 the original author or authors.
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

import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.persistence.base.EntityMeta;
import net.ymate.platform.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.base.AbstractResultSetHandler;

import java.io.Serializable;
import java.sql.ResultSet;

/**
 * 采用实体类存储数据的结果集数据处理接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/8 下午3:58
 * @version 1.0
 */
public class EntityResultSetHandler<T extends IEntity> extends AbstractResultSetHandler<T> {

    private Class<T> __entityClass;
    private EntityMeta __entityMeta;

    public EntityResultSetHandler(Class<T> entityClass) {
        this.__entityClass = entityClass;
        this.__entityMeta = EntityMeta.createAndGet(entityClass);
    }

    @SuppressWarnings("unchecked")
    protected T __doProcessResultRow(ResultSet resultSet) throws Exception {
        T _returnValue = __entityClass.newInstance();
        Object _primaryKeyObject = null;
        if (__entityMeta.isMultiplePrimaryKey()) {
            _primaryKeyObject = __entityMeta.getPrimaryKeyClass().newInstance();
            //
            _returnValue.setId((Serializable) _primaryKeyObject);
        }
        for (int _idx = 0; _idx < __doGetColumnCount(); _idx++) {
            EntityMeta.PropertyMeta _meta = __entityMeta.getPropertyByName(_doGetColumnMeta(_idx).getName());
            Object _fValue = BlurObject.bind(resultSet.getObject(_idx + 1)).toObjectValue(_meta.getField().getType());
            if (__entityMeta.isPrimaryKey(_meta.getName()) && __entityMeta.isMultiplePrimaryKey()) {
                _meta.getField().set(_primaryKeyObject, _fValue);
            } else {
                _meta.getField().set(_returnValue, _fValue);
            }
        }
        return _returnValue;
    }
}
