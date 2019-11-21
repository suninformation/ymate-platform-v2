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
package net.ymate.platform.persistence.jdbc.query;

import net.ymate.platform.commons.util.ExpressionUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-11-21 09:23
 * @since 2.1.0
 */
public interface IQueryHandler<T> {

    /**
     * 获取查询表达式
     *
     * @return 返回表达式字符串
     */
    String getExpressionStr();

    /**
     * 查询语句构建前事件调用
     *
     * @param expression 表达式工具对象
     * @param target     当前调用者对象
     */
    void beforeBuild(ExpressionUtils expression, T target);

    /**
     * 查询语句构建后事件调用
     *
     * @param expression 表达式工具对象
     * @param target     当前调用者对象
     */
    void afterBuild(ExpressionUtils expression, T target);
}
