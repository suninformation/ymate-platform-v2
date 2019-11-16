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
     * 构建自定义函数
     *
     * @param funcName 函数名称
     * @return 返回函数接口对象
     */
    static AbstractFunction create(String funcName) {
        return new AbstractFunction(funcName) {
            @Override
            public void onBuild() {
            }
        };
    }

    /**
     * 构建自定义函数(无名)
     *
     * @return 返回函数接口对象
     */
    static AbstractFunction create() {
        return new AbstractFunction() {
            @Override
            public void onBuild() {
            }
        };
    }

    /**
     * 操作符函数
     *
     * @param opt   操作符号
     * @param param 参数
     * @return 返回函数接口对象
     */
    static IFunction operate(String opt, String param) {
        return new AbstractFunction() {
            @Override
            public void onBuild() {
                operate(opt, param);
            }
        };
    }

    /**
     * 操作符函数
     *
     * @param paramOne 参数1
     * @param opt      操作符号
     * @param paramTwo 参数2
     * @return 返回函数接口对象
     */
    static IFunction operate(String paramOne, String opt, String paramTwo) {
        return new AbstractFunction() {
            @Override
            public void onBuild() {
                param(paramOne).operate(opt, paramTwo);
            }
        };
    }

    // ---

    /**
     * 加法
     *
     * @param param 数值型被加数
     * @return 返回函数接口对象
     */
    static IFunction addition(Number param) {
        return addition(param.toString());
    }

    static IFunction addition(String paramOne, Number param) {
        return addition(paramOne, param.toString());
    }

    /**
     * 加法
     *
     * @param param 函数型被加数
     * @return 返回函数接口对象
     */
    static IFunction addition(IFunction param) {
        return addition(param.toString());
    }

    static IFunction addition(String paramOne, IFunction paramTwo) {
        return addition(paramOne, paramTwo.toString());
    }

    /**
     * 加法
     *
     * @param param 字符串型被加数
     * @return 返回函数接口对象
     */
    static IFunction addition(String param) {
        return operate("+", param);
    }

    static IFunction addition(String paramOne, String paramTwo) {
        return operate(paramOne, "+", paramTwo);
    }

    // ---

    /**
     * 减法
     *
     * @param param 数值型被加数
     * @return 返回函数接口对象
     */
    static IFunction subtract(Number param) {
        return subtract(param.toString());
    }

    static IFunction subtract(String paramOne, Number paramTwo) {
        return subtract(paramOne, paramTwo.toString());
    }

    /**
     * 减法
     *
     * @param param 函数型被加数
     * @return 返回函数接口对象
     */
    static IFunction subtract(IFunction param) {
        return subtract(param.build());
    }

    static IFunction subtract(String paramOne, IFunction paramTwo) {
        return subtract(paramOne, paramTwo.build());
    }

    /**
     * 减法
     *
     * @param param 字符串被加数
     * @return 返回函数接口对象
     */
    static IFunction subtract(String param) {
        return operate("-", param);
    }

    static IFunction subtract(String paramOne, String paramTwo) {
        return operate(paramOne, "-", paramTwo);
    }

    // ---

    /**
     * 乘法
     *
     * @param param 数值型被加数
     * @return 返回函数接口对象
     */
    static IFunction multiply(Number param) {
        return multiply(param.toString());
    }

    static IFunction multiply(String paramOne, Number paramTwo) {
        return multiply(paramOne, paramTwo.toString());
    }

    /**
     * 乘法
     *
     * @param param 函数型被加数
     * @return 返回函数接口对象
     */
    static IFunction multiply(IFunction param) {
        return multiply(param.build());
    }

    static IFunction multiply(String paramOne, IFunction paramTwo) {
        return multiply(paramOne, paramTwo.build());
    }

    /**
     * 乘法
     *
     * @param param 字符串被加数
     * @return 返回函数接口对象
     */
    static IFunction multiply(String param) {
        return operate("*", param);
    }

    static IFunction multiply(String paramOne, String paramTwo) {
        return operate(paramOne, "*", paramTwo);
    }

    // ---

    /**
     * 除法
     *
     * @param param 数值型被加数
     * @return 返回函数接口对象
     */
    static IFunction divide(Number param) {
        return divide(param.toString());
    }

    static IFunction divide(String paramOne, Number paramTwo) {
        return divide(paramOne, paramTwo.toString());
    }

    /**
     * 除法
     *
     * @param param 函数型被加数
     * @return 返回函数接口对象
     */
    static IFunction divide(IFunction param) {
        return divide(param.build());
    }

    static IFunction divide(String paramOne, IFunction paramTwo) {
        return divide(paramOne, paramTwo.build());
    }

    /**
     * 除法
     *
     * @param param 字符串被加数
     * @return 返回函数接口对象
     */
    static IFunction divide(String param) {
        return operate("/", param);
    }

    static IFunction divide(String paramOne, String paramTwo) {
        return operate(paramOne, "/", paramTwo);
    }

    /**
     * 构建函数表达式
     *
     * @return 返回构建后的函数表达式字符串
     */
    String build();
}
