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
package net.ymate.platform.persistence.jdbc.repo;

import net.ymate.platform.core.beans.annotation.Ignored;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/12/11 上午2:14
 */
@Ignored
public interface IRepositoryDataFilter {

    /**
     * 获取预执行的SQL语句
     *
     * @return 返回SQL语句
     */
    String sql();

    /**
     * 过滤SQL语句查询结果集数据
     *
     * @param results 查询结果集数据
     * @return 返回过滤后的查询结果集数据
     */
    Object filter(Object results);
}
