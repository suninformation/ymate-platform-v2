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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 批量处理的SQL参数对象
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-28 上午01:52:39
 */
public final class SQLBatchParameter {

    /**
     * 存放SQL参数的集合
     */
    private final List<SQLParameter> parameters;

    public static SQLBatchParameter create() {
        return new SQLBatchParameter();
    }

    private SQLBatchParameter() {
        this.parameters = new ArrayList<>();
    }

    /**
     * 获取SQL参数集合
     *
     * @return 返回SQL参数集合
     */
    public List<SQLParameter> getParameters() {
        return Collections.unmodifiableList(this.parameters);
    }

    /**
     * 添加SQL参数，若参数为NULL则忽略
     *
     * @param parameter SQL参数对象
     * @return 返回当前参数对象
     */
    public SQLBatchParameter addParameter(SQLParameter parameter) {
        if (parameter != null) {
            this.parameters.add(parameter);
        }
        return this;
    }

    /**
     * 添加SQL参数，若参数为NULL则将默认向SQL传递NULL值对象
     *
     * @param parameter SQL参数值
     * @return 返回当前参数对象
     */
    public SQLBatchParameter addParameter(Object parameter) {
        SQLParameter.addParameter(this.parameters, parameter);
        return this;
    }

    @Override
    public String toString() {
        return parameters.toString();
    }
}
