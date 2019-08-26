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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.persistence.jdbc.base.AbstractResultSetHandler;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;

/**
 * 将数据直接映射到类成员属性的结果集处理接口实现
 *
 * @param <T> 元素类型
 * @author 刘镇 (suninformation@163.com) on 17/1/7 下午10:20
 */
public class BeanResultSetHandler<T> extends AbstractResultSetHandler<T> {

    private final Class<T> beanClass;

    @SuppressWarnings("unchecked")
    public BeanResultSetHandler() {
        beanClass = (Class<T>) ClassUtils.getParameterizedTypes(getClass()).get(0);
    }

    public BeanResultSetHandler(Class<T> beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    protected T processResultRow(ResultSet resultSet) throws Exception {
        ClassUtils.BeanWrapper<T> targetWrapper = ClassUtils.wrapper(beanClass.newInstance());
        for (int idx = 0; idx < getColumnCount(); idx++) {
            Object value = resultSet.getObject(idx + 1);
            if (value != null) {
                targetWrapper.setValue(StringUtils.uncapitalize(EntityMeta.propertyNameToFieldName(getColumnMeta(idx).getName())), value);
            }
        }
        return targetWrapper.getTargetObject();
    }
}
