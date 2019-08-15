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

import net.ymate.platform.core.beans.annotation.Ignored;

import java.util.List;

/**
 * 数据库批量更新操作者接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 2010-12-27 下午12:02:56
 */
@Ignored
public interface IBatchUpdateOperator extends IOperator {

    /**
     * 对于 INSERT、UPDATE 或 DELETE 语句，返回行数; 对于无返回结果的SQL语句，返回 0
     *
     * @return int[] 获取批量更新操作执行后，每组SQL影响的记录行数的数组；
     */
    int[] getEffectCounts();

    /**
     * 获取累计受影响的总记录数
     *
     * @return 返回累计受影响的总记录数
     */
    int getEffectCountsTotal();

    /**
     * 添加SQL语句
     *
     * @param sql SQL语句
     * @return 返回当前批量操作器
     */
    IBatchUpdateOperator addBatchSQL(String sql);

    /**
     * 添加批量SQL参数
     *
     * @param parameter 批量SQL参数对象
     * @return 返回当前批量操作器
     */
    IBatchUpdateOperator addBatchParameter(SQLBatchParameter parameter);

    /**
     * 获取批量SQL参数集合
     *
     * @return 返回批量SQL参数集合
     */
    List<SQLBatchParameter> getBatchParameters();
}
