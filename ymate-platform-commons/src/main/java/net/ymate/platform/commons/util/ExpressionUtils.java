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
package net.ymate.platform.commons.util;

import net.ymate.platform.commons.lang.BlurObject;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串表达式工具类，用于处理${variable}字符替换;
 * 例: I am ${name},and sex is ${sex}. <br>
 * name=Henry,sex=M <br>
 * result:I am Henry,and sex is M.
 *
 * @author 刘镇 (suninformation@163.com) on 2010-12-20 上午11:37:00
 */
public final class ExpressionUtils {

    private final static Pattern PATTERN = Pattern.compile("(?<=\\$\\{)(.+?)(?=})");

    private final static String PRE = "\\$\\{";
    private final static String SUF = "}";
    private String result;

    /**
     * @param expressionStr 目标字符串
     * @return 创建表达式工具类实例对象
     */
    public static ExpressionUtils bind(String expressionStr) {
        return new ExpressionUtils(expressionStr);
    }

    private ExpressionUtils(String expressionStr) {
        this.result = expressionStr;
    }

    /**
     * @return 获取结果
     */
    public String getResult() {
        return this.result;
    }

    /**
     * 设置变量值
     *
     * @param key   变量名称
     * @param value 变量值
     * @return 当前表达式工具类实例
     */
    public ExpressionUtils set(String key, String value) {
        if (value != null) {
            String namePattern = PRE + key + SUF;
            this.result = this.result.replaceAll(namePattern, Matcher.quoteReplacement(value));
        }
        return this;
    }

    /**
     * 批量设置变量值
     *
     * @param values 变量值映射
     * @return 当前表达式工具类实例
     */
    public ExpressionUtils set(Map<String, Object> values) {
        getVariables().forEach(var -> set(var, BlurObject.bind(values.get(var)).toStringValue()));
        return this;
    }

    /**
     * @return 返回expressionStr中变量名称集合, 返回的数量将受set方法影响
     */
    public List<String> getVariables() {
        List<String> vars = new ArrayList<>();
        Matcher match = PATTERN.matcher(this.result);
        boolean resultFlag = match.find();
        if (resultFlag) {
            do {
                vars.add(match.group());
                resultFlag = match.find();
            } while (resultFlag);
        }
        return vars;
    }

    /**
     * @return 清理所有变量并返回当前表达式工具类实例
     */
    public ExpressionUtils clean() {
        return set("(.+?)", StringUtils.EMPTY);
    }
}
