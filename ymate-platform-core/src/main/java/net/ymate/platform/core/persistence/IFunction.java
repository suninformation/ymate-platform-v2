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
     * 加法
     *
     * @param param 数值参数
     * @return 返回当前函数对象
     */
    IFunction addition(Number param);

    /**
     * 加法
     *
     * @param param 字符串参数
     * @return 返回当前函数对象
     */
    IFunction addition(String param);

    /**
     * 加法
     *
     * @param param 函数参数
     * @return 返回当前函数对象
     */
    IFunction addition(IFunction param);

    /**
     * 减法
     *
     * @param param 数值参数
     * @return 返回当前函数对象
     */
    IFunction subtract(Number param);

    /**
     * 减法
     *
     * @param param 字符串参数
     * @return 返回当前函数对象
     */
    IFunction subtract(String param);

    /**
     * 减法
     *
     * @param param 函数参数
     * @return 返回当前函数对象
     */
    IFunction subtract(IFunction param);

    /**
     * 乘法
     *
     * @param param 数值参数
     * @return 返回当前函数对象
     */
    IFunction multiply(Number param);

    /**
     * 乘法
     *
     * @param param 字符串参数
     * @return 返回当前函数对象
     */
    IFunction multiply(String param);

    /**
     * 乘法
     *
     * @param param 函数参数
     * @return 返回当前函数对象
     */
    IFunction multiply(IFunction param);

    /**
     * 除法
     *
     * @param param 数值参数
     * @return 返回当前函数对象
     */
    IFunction divide(Number param);

    /**
     * 除法
     *
     * @param param 字符串参数
     * @return 返回当前函数对象
     */
    IFunction divide(String param);

    /**
     * 除法
     *
     * @param param 函数参数
     * @return 返回当前函数对象
     */
    IFunction divide(IFunction param);

    /**
     * 设置参数
     *
     * @param param 数值参数
     * @return 返回当前函数对象
     */
    IFunction param(Number param);

    /**
     * 设置参数
     *
     * @param params 数值参数集合
     * @return 返回当前函数对象
     */
    IFunction param(Number[] params);

    /**
     * 设置参数分隔符
     *
     * @return 返回当前函数对象
     */
    IFunction separator();

    /**
     * 添加空格符
     *
     * @return 返回当前函数对象
     */
    IFunction space();

    /**
     * 添加左括号
     *
     * @return 返回当前函数对象
     */
    IFunction bracketBegin();

    /**
     * 添加右括号
     *
     * @return 返回当前函数对象
     */
    IFunction bracketEnd();

    /**
     * 设置参数
     *
     * @param param 函数参数
     * @return 返回当前函数对象
     */
    IFunction param(IFunction param);

    /**
     * 设置参数
     *
     * @param param 字符串参数
     * @return 返回当前函数对象
     */
    IFunction param(String param);

    /**
     * 设置参数
     *
     * @param params 字符串参数集合
     * @return 返回当前函数对象
     */
    IFunction param(String[] params);

    /**
     * 设置参数
     *
     * @param prefix 前缀
     * @param field  字段名称
     * @return 返回当前函数对象
     */
    IFunction param(String prefix, String field);

    /**
     * 设置参数
     *
     * @param params 参数对象集合
     * @return 返回当前函数对象
     */
    IFunction paramWS(Object... params);

    /**
     * 构建函数表达式
     *
     * @return 返回构建后的函数表达式字符串
     */
    String build();
}
