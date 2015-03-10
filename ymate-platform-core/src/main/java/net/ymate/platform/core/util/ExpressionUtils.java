/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.core.util;

import java.util.regex.Matcher;

/**
 * <p>
 * 字符串表达式工具类，用于处理${variable}字符替换；<br/>
 * 例: I am ${name},and sex is ${sex}. <br>
 * name=Henry,sex=M <br>
 * result:I am Henry,and sex is M.
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 14/12/26 上午3:29
 * @version 1.0
 */
public class ExpressionUtils {

    private final static String __pre = "\\$\\{";
    private final static String __suf = "\\}";
    private String __result;

    /**
     * 创建表达式工具类实例对象
     *
     * @param expressionStr
     * @return
     */
    public static ExpressionUtils bind(String expressionStr) {
        return new ExpressionUtils(expressionStr);
    }

    /**
     * 构造器
     */
    private ExpressionUtils(String expressionStr) {
        this.__result = expressionStr;
    }

    /**
     * 获取结果
     *
     * @return
     */
    public String getResult() {
        return this.__result;
    }

    /**
     * 设置变量值
     *
     * @param key   变量名称
     * @param value 变量值
     * @return
     */
    public ExpressionUtils set(String key, String value) {
        String namePattern = __pre + key + __suf;
        this.__result = this.__result.replaceAll(namePattern, Matcher.quoteReplacement(value));
        return this;
    }

}
