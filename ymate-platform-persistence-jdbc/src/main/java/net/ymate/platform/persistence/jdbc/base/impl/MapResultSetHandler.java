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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 采用Map存储数据的结果集数据处理接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-10-25下午11:51:38
 */
public class MapResultSetHandler extends AbstractResultSetHandler<Map<String, Object>> {

    @Override
    protected Map<String, Object> processResultRow(ResultSet resultSet) throws Exception {
        // 要保持字段的顺序!!
        Map<String, Object> result = new LinkedHashMap<>(getColumnCount());
        for (int idx = 0; idx < getColumnCount(); idx++) {
            result.put(getColumnMeta(idx).getName(), resultSet.getObject(idx + 1));
        }
        return result;
    }
}
