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
package net.ymate.platform.persistence.jdbc.base;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * 结果集数据处理接口抽象实现
 *
 * @param <T> 元素类型
 * @author 刘镇 (suninformation@163.com) on 2011-9-22 下午04:14:15
 */
public abstract class AbstractResultSetHandler<T> implements IResultSetHandler<T> {

    private int columnCount;

    private ColumnMeta[] columnMetas;

    @Override
    public List<T> handle(ResultSet resultSet) throws Exception {
        // 分析结果集字段信息
        ResultSetMetaData metaData = resultSet.getMetaData();
        columnCount = metaData.getColumnCount();
        columnMetas = new ColumnMeta[columnCount];
        for (int idx = 0; idx < columnCount; idx++) {
            columnMetas[idx] = new ColumnMeta(metaData.getColumnLabel(idx + 1), metaData.getColumnType(idx + 1));
        }
        //
        return processResult(resultSet);
    }

    protected List<T> processResult(ResultSet resultSet) throws Exception {
        List<T> results = new ArrayList<>();
        while (resultSet.next()) {
            results.add(processResultRow(resultSet));
        }
        return results;
    }

    /**
     * 处理当前行结果集数据
     *
     * @param resultSet 数据结果集对象，切勿对其进行游标移动等操作，仅约定用于提取当前行字段数据
     * @return 返回指定的T类型对象
     * @throws Exception 可能产生的异常
     */
    protected abstract T processResultRow(ResultSet resultSet) throws Exception;

    protected int getColumnCount() {
        return columnCount;
    }

    protected ColumnMeta getColumnMeta(int idx) {
        return columnMetas[idx];
    }

    /**
     * 字段描述对象
     */
    public static class ColumnMeta {

        private final String name;

        private final int type;

        public ColumnMeta(String name, int type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public int getType() {
            return type;
        }
    }
}
