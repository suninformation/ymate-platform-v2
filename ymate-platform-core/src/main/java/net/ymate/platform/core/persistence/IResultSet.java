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
package net.ymate.platform.core.persistence;

import net.ymate.platform.core.beans.annotation.Ignored;

import java.util.List;

/**
 * 统一查询结果封装对象接口定义(支持分页)
 *
 * @param <T> 元素类型
 * @author 刘镇 (suninformation@163.com) on 2011-9-24 下午08:32:02
 */
@Ignored
public interface IResultSet<T> {

    /**
     * 获取当前结果集是否可用，即是否为空或元素数量为0
     *
     * @return 若当前结果集可用将返回true，否则返回false
     */
    boolean isResultsAvailable();

    /**
     * 获取当前结果集是否已分页
     *
     * @return 若当前结果集已分页则返回true，否则返回false
     */
    boolean isPaginated();

    /**
     * 获取当前页号
     *
     * @return 返回当前页号
     */
    int getPageNumber();

    /**
     * 获取每页记录数
     *
     * @return 返回每页记录数
     */
    int getPageSize();

    /**
     * 获取总页数
     *
     * @return 返回总页数
     */
    int getPageCount();

    /**
     * 获取总记录数
     *
     * @return 返回总记录数
     */
    long getRecordCount();

    /**
     * 获取结果集数据
     *
     * @return 返回结果集数据
     */
    List<T> getResultData();
}
