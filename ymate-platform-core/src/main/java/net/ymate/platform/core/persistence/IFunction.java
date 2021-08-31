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

/**
 * 函数接口
 *
 * @author 刘镇 (suninformation@163.com) on 17/6/22 上午2:14
 */
@Ignored
public interface IFunction {

    /**
     * 构建函数表达式
     *
     * @return 返回构建后的函数表达式字符串
     */
    String build();

    /**
     * 获取参数对象集合
     *
     * @return 返回参数对象集合
     */
    Params params();

    /**
     * 添加参数对象
     *
     * @param param 参数对象
     * @return 返回当前函数对象
     */
    IFunction param(Object param);

    /**
     * 添加参数对象集合
     *
     * @param params 参数对象集合
     * @return 返回当前函数对象
     */
    IFunction param(Params params);
}
