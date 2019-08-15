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


import net.ymate.platform.persistence.jdbc.base.AbstractResultSetHandler;

import java.sql.ResultSet;

/**
 * 采用Object[]存储数据的结果集数据处理接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-22 下午04:40:31
 */
public class ArrayResultSetHandler extends AbstractResultSetHandler<Object[]> {

    @Override
    protected Object[] processResultRow(ResultSet resultSet) throws Exception {
        Object[] result = new Object[getColumnCount()];
        for (int idx = 0; idx < getColumnCount(); idx++) {
            result[idx] = new Object[]{getColumnMeta(idx).getName(), resultSet.getObject(idx + 1)};
        }
        return result;
    }
}
