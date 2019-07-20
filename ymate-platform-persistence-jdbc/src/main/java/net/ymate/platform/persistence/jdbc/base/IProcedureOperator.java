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
 * 数据库存储过程操作器接口定义
 *
 * @param <T> 元素类型
 * @author 刘镇 (suninformation@163.com) on 2010-12-25 下午02:40:48
 */
@Ignored
public interface IProcedureOperator<T> extends IOperator {

    /**
     * 执行存储过程
     *
     * @param resultSetHandler 结果集数据处理器
     * @return 返回当前操作器
     * @throws Exception 可能产生的任何异常
     */
    IProcedureOperator<T> execute(IResultSetHandler<T> resultSetHandler) throws Exception;

    /**
     * 执行存储过程
     *
     * @param resultProcessor 输出参数结果处理器
     * @return 返回当前操作器
     * @throws Exception 可能产生的任何异常
     */
    IProcedureOperator<T> execute(IOutResultProcessor resultProcessor) throws Exception;

    /**
     * 添加输出参数
     *
     * @param sqlParamType SQL参数类型(参考java.sql.Types)
     * @return 返回当前操作器
     */
    IProcedureOperator<T> addOutParameter(Integer sqlParamType);

    /**
     * 添加参数
     *
     * @param parameter SQL参数对象
     * @return 返回当前操作器
     */
    @Override
    IProcedureOperator<T> addParameter(SQLParameter parameter);

    /**
     * 添加参数
     *
     * @param parameter SQL参数值
     * @return 返回当前操作器
     */
    @Override
    IProcedureOperator<T> addParameter(Object parameter);

    /**
     * 设置输出参数结果处理器
     *
     * @param outResultProcessor 输出参数结果处理器
     * @return 返回当前操作器
     */
    IProcedureOperator<T> setOutResultProcessor(IOutResultProcessor outResultProcessor);

    /**
     * 设置结果集数据处理器
     *
     * @param resultSetHandler 结果集数据处理器
     * @return 返回当前操作器
     */
    IProcedureOperator<T> setResultSetHandler(IResultSetHandler<T> resultSetHandler);

    /**
     * 获取执行结果集合
     *
     * @return 返回结果集合
     */
    List<List<T>> getResultSets();

    /**
     * 输出参数结果处理器接口
     */
    interface IOutResultProcessor {

        /**
         * 处理结果集
         *
         * @param idx       索引下标
         * @param paramType 参数类型
         * @param result    结果集对象
         * @throws Exception 可能产生的任何异常
         */
        void process(int idx, int paramType, Object result) throws Exception;
    }
}
