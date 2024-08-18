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
package net.ymate.platform.core.persistence.impl;

import net.ymate.platform.core.persistence.IResultSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 统一查询结果封装对象接口默认实现
 *
 * @param <T> 元素类型
 * @author 刘镇 (suninformation@163.com) on 2011-9-24 下午08:32:02
 */
public class DefaultResultSet<T> implements IResultSet<T> {

    private final int pageNumber;

    private final int pageSize;

    private int pageCount;

    private final long recordCount;

    private final List<T> resultData;

    /**
     * 构造方法，用于类型转换
     *
     * @param resultSet 源结果封装对象
     * @since 2.1.3
     */
    public DefaultResultSet(IResultSet<? extends T> resultSet) {
        this(new ArrayList<>(resultSet.getResultData()), resultSet.getPageNumber(), resultSet.getPageSize(), resultSet.getRecordCount());
    }

    /**
     * 构造方法，不采用分页方式
     *
     * @param resultData 结果集
     */
    public DefaultResultSet(List<T> resultData) {
        this(resultData, 0, 0, 0);
    }

    /**
     * 构造方法，采用分页计算
     *
     * @param resultData  当前页数据
     * @param pageNumber  当前页号
     * @param pageSize    每页记录数
     * @param recordCount 总记录数
     */
    public DefaultResultSet(List<T> resultData, int pageNumber, int pageSize, long recordCount) {
        this.resultData = resultData;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.recordCount = recordCount;
        // pageNumber和pageSize两者均小于0则视为不分页
        if (pageNumber > 0 && pageSize > 0) {
            // 根据记录总数和分页参数计算总页数
            if (recordCount > 0) {
                if (recordCount % pageSize > 0) {
                    this.pageCount = (int) (recordCount / pageSize + 1);
                } else {
                    this.pageCount = (int) (recordCount / pageSize);
                }
            }
        }
    }

    @Override
    public boolean isResultsAvailable() {
        return resultData != null && !resultData.isEmpty();
    }

    @Override
    public boolean isPaginated() {
        return pageNumber > 0 && pageSize > 0;
    }

    @Override
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public int getPageSize() {
        return pageSize;
    }

    @Override
    public int getPageCount() {
        return pageCount;
    }

    @Override
    public long getRecordCount() {
        return recordCount;
    }

    @Override
    public List<T> getResultData() {
        return Collections.unmodifiableList(resultData);
    }
}
