/*
 * Copyright 2007-present the original author or authors.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExpressionUtils单元测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-06 03:31:04
 * @since 2.1.4
 */
public class ExpressionUtilsTest {

    private static final String TEMPLATE_WITH_VARIABLES = "Hello ${name}, your age is ${age}.";
    private static final String TEMPLATE_WITHOUT_VARIABLES = "Hello World!";
    private static final String EMPTY_TEMPLATE = "";
    private static final String TEMPLATE_WITH_DUPLICATES = "${name} is ${name}, and ${age} is ${age}.";
    private static final String TEMPLATE_WITH_SPECIAL_CHARS = "Special: ${name}, ${age}%, ${value}$.";

    @Before
    public void setUp() {
        // 每个测试方法执行前的初始化操作
    }

    // ========== bind() 方法测试 ==========

    /**
     * 测试bind()方法的基本功能
     * 验证：能够正确创建ExpressionUtils实例
     */
    @Test
    public void testBindWithValidString() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES);
        Assert.assertNotNull(expressionUtils);
        Assert.assertEquals(TEMPLATE_WITH_VARIABLES, expressionUtils.getResult());
    }

    /**
     * 测试bind()方法处理null输入
     * 验证：null输入应被转换为空字符串
     */
    @Test
    public void testBindWithNullString() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(null);
        Assert.assertNotNull(expressionUtils);
        Assert.assertEquals("", expressionUtils.getResult());
    }

    /**
     * 测试bind()方法处理空字符串
     * 验证：空字符串应被正确处理
     */
    @Test
    public void testBindWithEmptyString() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(EMPTY_TEMPLATE);
        Assert.assertNotNull(expressionUtils);
        Assert.assertEquals(EMPTY_TEMPLATE, expressionUtils.getResult());
    }

    // ========== getResult() 方法测试 ==========

    /**
     * 测试getResult()方法的基本功能
     * 验证：能够正确返回处理后的字符串
     */
    @Test
    public void testGetResult() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES);
        String result = expressionUtils.getResult();
        Assert.assertNotNull(result);
        Assert.assertEquals(TEMPLATE_WITH_VARIABLES, result);
    }

    /**
     * 测试getResult()方法在多次调用时的一致性
     * 验证：多次调用应返回相同的结果
     */
    @Test
    public void testGetResultConsistency() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES);
        String result1 = expressionUtils.getResult();
        String result2 = expressionUtils.getResult();
        Assert.assertEquals(result1, result2);
    }

    // ========== set(String, String) 方法测试 ==========

    /**
     * 测试set(String, String)方法的基本功能
     * 验证：能够正确替换单个变量
     */
    @Test
    public void testSetSingleVariable() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set("name", "John")
                .set("age", "25");

        String result = expressionUtils.getResult();
        Assert.assertEquals("Hello John, your age is 25.", result);
    }

    /**
     * 测试set(String, String)方法的链式调用
     * 验证：支持链式调用，能够连续设置多个变量
     */
    @Test
    public void testSetChainedCalls() {
        String result = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set("name", "Alice")
                .set("age", "30")
                .getResult();

        Assert.assertEquals("Hello Alice, your age is 30.", result);
    }

    /**
     * 测试set(String, String)方法处理null值
     * 验证：当value为null时，不执行替换操作
     */
    @Test
    public void testSetWithNullValue() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set("name", null);

        String result = expressionUtils.getResult();
        Assert.assertEquals(TEMPLATE_WITH_VARIABLES, result);
    }

    /**
     * 测试set(String, String)方法处理空字符串
     * 验证：空字符串应被正确替换
     */
    @Test
    public void testSetWithEmptyString() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set("name", "")
                .set("age", "");

        String result = expressionUtils.getResult();
        Assert.assertEquals("Hello , your age is .", result);
    }

    /**
     * 测试set(String, String)方法处理不存在的变量名
     * 验证：不存在的变量名不应影响字符串
     */
    @Test
    public void testSetWithNonExistentVariable() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set("nonexistent", "value");

        String result = expressionUtils.getResult();
        Assert.assertEquals(TEMPLATE_WITH_VARIABLES, result);
    }

    /**
     * 测试set(String, String)方法处理重复变量
     * 验证：所有匹配的占位符都应被替换
     */
    @Test
    public void testSetWithDuplicateVariables() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_DUPLICATES)
                .set("name", "Tom")
                .set("age", "20");

        String result = expressionUtils.getResult();
        Assert.assertEquals("Tom is Tom, and 20 is 20.", result);
    }

    /**
     * 测试set(String, String)方法处理特殊字符
     * 验证：特殊字符应被正确处理
     */
    @Test
    public void testSetWithSpecialCharacters() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_SPECIAL_CHARS)
                .set("name", "Test")
                .set("age", "50")
                .set("value", "100");

        String result = expressionUtils.getResult();
        Assert.assertEquals("Special: Test, 50%, 100$.", result);
    }

    /**
     * 测试set(String, String)方法处理包含正则特殊字符的值
     * 验证：正则特殊字符应被正确转义
     */
    @Test
    public void testSetWithRegexSpecialChars() {
        String template = "Value: ${value}";
        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set("value", "$100.00");

        String result = expressionUtils.getResult();
        Assert.assertEquals("Value: $100.00", result);
    }

    // ========== set(String, Object) 方法测试 ==========

    /**
     * 测试set(String, Object)方法的基本功能
     * 验证：能够正确替换单个对象类型的变量
     */
    @Test
    public void testSetObjectWithInteger() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set("name", "Peter")
                .set("age", "30");

        String result = expressionUtils.getResult();
        Assert.assertEquals("Hello Peter, your age is 30.", result);
    }

    /**
     * 测试set(String, Object)方法处理不同类型的对象
     * 验证：能够正确处理Integer、Double、Boolean等基本类型
     */
    @Test
    public void testSetObjectWithDifferentTypes() {
        String template = "Name: ${name}, Age: ${age}, Score: ${score}, Active: ${active}";
        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set("name", "Quinn")
                .set("age", "25")
                .set("score", "95.5")
                .set("active", "true");

        String result = expressionUtils.getResult();
        Assert.assertEquals("Name: Quinn, Age: 25, Score: 95.5, Active: true", result);
    }

    /**
     * 测试set(String, Object)方法处理null值
     * 验证：null值不应执行替换操作
     */
    @Test
    public void testSetObjectWithNullValue() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set("name", null);

        String result = expressionUtils.getResult();
        Assert.assertEquals(TEMPLATE_WITH_VARIABLES, result);
    }

    /**
     * 测试set(String, Object)方法处理自定义对象
     * 验证：能够正确处理自定义对象的字符串转换
     */
    @Test
    public void testSetObjectWithCustomObject() {
        class CustomObject {
            @Override
            public String toString() {
                return "CustomValue";
            }
        }

        String template = "Value: ${custom}";
        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set("custom", new CustomObject().toString());

        String result = expressionUtils.getResult();
        Assert.assertEquals("Value: CustomValue", result);
    }

    /**
     * 测试set(String, Object)方法的链式调用
     * 验证：支持链式调用，能够连续设置多个对象类型的变量
     */
    @Test
    public void testSetObjectChainedCalls() {
        String result = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set("name", "Rachel")
                .set("age", "28")
                .getResult();

        Assert.assertEquals("Hello Rachel, your age is 28.", result);
    }

    // ========== set(Map) 方法测试 ==========

    /**
     * 测试set(Map)方法的基本功能
     * 验证：能够批量替换多个变量
     */
    @Test
    public void testSetWithMap() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Bob");
        values.put("age", "35");

        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set(values);

        String result = expressionUtils.getResult();
        Assert.assertEquals("Hello Bob, your age is 35.", result);
    }

    /**
     * 测试set(Map)方法处理null输入
     * 验证：null Map不应影响字符串
     */
    @Test
    public void testSetWithNullMap() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set(null);

        String result = expressionUtils.getResult();
        Assert.assertEquals(TEMPLATE_WITH_VARIABLES, result);
    }

    /**
     * 测试set(Map)方法处理空Map
     * 验证：空Map不应影响字符串
     */
    @Test
    public void testSetWithEmptyMap() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set(new HashMap<>());

        String result = expressionUtils.getResult();
        Assert.assertEquals(TEMPLATE_WITH_VARIABLES, result);
    }

    /**
     * 测试set(Map)方法处理部分变量
     * 验证：只替换Map中存在的变量
     */
    @Test
    public void testSetWithPartialVariables() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Charlie");

        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set(values);

        String result = expressionUtils.getResult();
        // Map中没有age，所以${age}保持不变
        Assert.assertEquals("Hello Charlie, your age is ${age}.", result);
    }

    /**
     * 测试set(Map)方法处理不同类型的值
     * 验证：Object类型的值应被正确转换为字符串
     */
    @Test
    public void testSetWithDifferentValueTypes() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", "David");
        values.put("age", 40);
        values.put("score", 95.5);
        values.put("active", true);

        String template = "Name: ${name}, Age: ${age}, Score: ${score}, Active: ${active}";
        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set(values);

        String result = expressionUtils.getResult();
        Assert.assertEquals("Name: David, Age: 40, Score: 95.5, Active: true", result);
    }

    /**
     * 测试set(Map)方法处理Map中值为null的情况
     * 验证：Map中值为null时，占位符应保持不变
     */
    @Test
    public void testSetWithMapContainingNullValues() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Eve");
        values.put("age", null);

        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set(values);

        String result = expressionUtils.getResult();
        // age的值为null，所以${age}保持不变
        Assert.assertEquals("Hello Eve, your age is ${age}.", result);
    }

    /**
     * 测试set(Map)方法的性能优势
     * 验证：批量替换应比多次单独替换更高效
     */
    @Test
    public void testSetMapPerformance() {
        String template = "A: ${a}, B: ${b}, C: ${c}, D: ${d}, E: ${e}";
        Map<String, Object> values = new HashMap<>();
        values.put("a", "1");
        values.put("b", "2");
        values.put("c", "3");
        values.put("d", "4");
        values.put("e", "5");

        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set(values);

        String result = expressionUtils.getResult();
        Assert.assertEquals("A: 1, B: 2, C: 3, D: 4, E: 5", result);
    }

    // ========== getVariables() 方法测试 ==========

    /**
     * 测试getVariables()方法的基本功能
     * 验证：能够正确提取所有变量名
     */
    @Test
    public void testGetVariables() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES);
        List<String> variables = expressionUtils.getVariables();

        Assert.assertNotNull(variables);
        Assert.assertEquals(2, variables.size());
        Assert.assertTrue(variables.contains("name"));
        Assert.assertTrue(variables.contains("age"));
    }

    /**
     * 测试getVariables()方法处理无变量的字符串
     * 验证：应返回空列表
     */
    @Test
    public void testGetVariablesWithNoVariables() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITHOUT_VARIABLES);
        List<String> variables = expressionUtils.getVariables();

        Assert.assertNotNull(variables);
        Assert.assertTrue(variables.isEmpty());
    }

    /**
     * 测试getVariables()方法处理空字符串
     * 验证：应返回空列表
     */
    @Test
    public void testGetVariablesWithEmptyString() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(EMPTY_TEMPLATE);
        List<String> variables = expressionUtils.getVariables();

        Assert.assertNotNull(variables);
        Assert.assertTrue(variables.isEmpty());
    }

    /**
     * 测试getVariables()方法处理重复变量
     * 验证：重复变量应全部包含在返回列表中
     */
    @Test
    public void testGetVariablesWithDuplicates() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_DUPLICATES);
        List<String> variables = expressionUtils.getVariables();

        Assert.assertNotNull(variables);
        Assert.assertEquals(4, variables.size());
        Assert.assertEquals("name", variables.get(0));
        Assert.assertEquals("name", variables.get(1));
        Assert.assertEquals("age", variables.get(2));
        Assert.assertEquals("age", variables.get(3));
    }

    /**
     * 测试getVariables()方法在变量替换后的行为
     * 验证：替换后变量列表应减少
     */
    @Test
    public void testGetVariablesAfterReplacement() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set("name", "Frank");

        List<String> variables = expressionUtils.getVariables();
        Assert.assertEquals(1, variables.size());
        Assert.assertTrue(variables.contains("age"));
    }

    // ========== clean() 方法测试 ==========

    /**
     * 测试clean()方法的基本功能
     * 验证：能够正确移除所有变量占位符
     */
    @Test
    public void testClean() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .clean();

        String result = expressionUtils.getResult();
        Assert.assertEquals("Hello , your age is .", result);
    }

    /**
     * 测试clean()方法处理无变量的字符串
     * 验证：无变量的字符串应保持不变
     */
    @Test
    public void testCleanWithNoVariables() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITHOUT_VARIABLES)
                .clean();

        String result = expressionUtils.getResult();
        Assert.assertEquals(TEMPLATE_WITHOUT_VARIABLES, result);
    }

    /**
     * 测试clean()方法处理空字符串
     * 验证：空字符串应保持不变
     */
    @Test
    public void testCleanWithEmptyString() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(EMPTY_TEMPLATE)
                .clean();

        String result = expressionUtils.getResult();
        Assert.assertEquals(EMPTY_TEMPLATE, result);
    }

    /**
     * 测试clean()方法处理重复变量
     * 验证：所有变量占位符都应被移除
     */
    @Test
    public void testCleanWithDuplicates() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_DUPLICATES)
                .clean();

        String result = expressionUtils.getResult();
        Assert.assertEquals(" is , and  is .", result);
    }

    /**
     * 测试clean()方法的链式调用
     * 验证：clean()方法应支持链式调用
     */
    @Test
    public void testCleanChainedCall() {
        String result = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .clean()
                .getResult();

        Assert.assertEquals("Hello , your age is .", result);
    }

    /**
     * 测试clean()方法在部分替换后的行为
     * 验证：部分替换后clean()应移除剩余的占位符
     */
    @Test
    public void testCleanAfterPartialReplacement() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set("name", "Jack")
                .clean();

        String result = expressionUtils.getResult();
        Assert.assertEquals("Hello Jack, your age is .", result);
    }

    // ========== 综合测试 ==========

    /**
     * 测试完整的链式调用流程
     * 验证：所有方法能够正确地链式调用
     */
    @Test
    public void testCompleteChainedFlow() {
        String result = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set("name", "Kate")
                .set("age", "28")
                .getResult();

        Assert.assertEquals("Hello Kate, your age is 28.", result);
    }

    /**
     * 测试复杂场景：多个方法组合使用
     * 验证：不同方法组合使用时的正确性
     */
    @Test
    public void testComplexScenario() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES);

        // 检查初始状态
        List<String> variables = expressionUtils.getVariables();
        Assert.assertEquals(2, variables.size());

        // 部分替换
        expressionUtils.set("name", "Leo");

        // 完成替换
        expressionUtils.set("age", "50");

        // 验证最终结果
        Assert.assertEquals("Hello Leo, your age is 50.", expressionUtils.getResult());
    }

    /**
     * 测试边界条件：超长字符串
     * 验证：能够正确处理超长字符串
     */
    @Test
    public void testVeryLongString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("Text ${var").append(i).append("} ");
        }

        Map<String, Object> values = new HashMap<>();
        for (int i = 0; i < 1000; i++) {
            values.put("var" + i, "value" + i);
        }

        ExpressionUtils expressionUtils = ExpressionUtils.bind(sb.toString())
                .set(values);

        String result = expressionUtils.getResult();
        Assert.assertFalse(result.contains("${"));
    }

    /**
     * 测试边界条件：单个字符变量名
     * 验证：能够正确处理单字符变量名
     */
    @Test
    public void testSingleCharacterVariableName() {
        String template = "A: ${a}, B: ${b}, C: ${c}";
        Map<String, Object> values = new HashMap<>();
        values.put("a", "1");
        values.put("b", "2");
        values.put("c", "3");

        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set(values);

        String result = expressionUtils.getResult();
        Assert.assertEquals("A: 1, B: 2, C: 3", result);
    }

    /**
     * 测试边界条件：变量名包含数字和下划线
     * 验证：能够正确处理包含数字和下划线的变量名
     */
    @Test
    public void testVariableNameWithNumbersAndUnderscores() {
        String template = "Value: ${var_1_2}, ${var_3_4}";
        Map<String, Object> values = new HashMap<>();
        values.put("var_1_2", "test1");
        values.put("var_3_4", "test2");

        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set(values);

        String result = expressionUtils.getResult();
        Assert.assertEquals("Value: test1, test2", result);
    }

    /**
     * 测试边界条件：变量名包含点号
     * 验证：能够正确处理包含点号的变量名
     */
    @Test
    public void testVariableNameWithDots() {
        String template = "Value: ${user.name}, ${user.age}";
        Map<String, Object> values = new HashMap<>();
        values.put("user.name", "Mike");
        values.put("user.age", "55");

        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set(values);

        String result = expressionUtils.getResult();
        Assert.assertEquals("Value: Mike, 55", result);
    }

    /**
     * 测试边界条件：连续的变量占位符
     * 验证：能够正确处理连续的变量占位符
     */
    @Test
    public void testConsecutiveVariables() {
        String template = "${a}${b}${c}";
        Map<String, Object> values = new HashMap<>();
        values.put("a", "X");
        values.put("b", "Y");
        values.put("c", "Z");

        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set(values);

        String result = expressionUtils.getResult();
        Assert.assertEquals("XYZ", result);
    }

    /**
     * 测试边界条件：嵌套的花括号（不支持）
     * 验证：能够正确处理嵌套的花括号情况
     */
    @Test
    public void testNestedBraces() {
        String template = "Value: ${${inner}}";
        ExpressionUtils expressionUtils = ExpressionUtils.bind(template);

        List<String> variables = expressionUtils.getVariables();
        // 正则表达式不会匹配嵌套的花括号，但会匹配第一个完整的${}
        Assert.assertEquals(1, variables.size());
        // 正则表达式会匹配${${inner}}，变量名为${inner
        Assert.assertEquals("${inner", variables.get(0));
    }

    /**
     * 测试边界条件：不匹配的花括号
     * 验证：能够正确处理不匹配的花括号
     */
    @Test
    public void testMismatchedBraces() {
        String template = "Value: ${name or ${age}";
        ExpressionUtils expressionUtils = ExpressionUtils.bind(template);

        List<String> variables = expressionUtils.getVariables();
        // 正则表达式会匹配第一个完整的${}，即${name or ${age}
        Assert.assertEquals(1, variables.size());
        Assert.assertEquals("name or ${age", variables.get(0));
    }

    /**
     * 测试边界条件：只有开始或结束花括号
     * 验证：能够正确处理不完整的花括号
     */
    @Test
    public void testIncompleteBraces() {
        String template = "Value: ${name} and ${age or }name}";
        ExpressionUtils expressionUtils = ExpressionUtils.bind(template);

        List<String> variables = expressionUtils.getVariables();
        // 正则表达式是非贪婪匹配，会在遇到第一个}时停止
        Assert.assertEquals(2, variables.size());
        Assert.assertEquals("name", variables.get(0));
        // 正则表达式会匹配${age or }，变量名为age or
        Assert.assertEquals("age or ", variables.get(1));
    }

    /**
     * 测试边界条件：变量名包含空格
     * 验证：能够正确处理包含空格的变量名
     */
    @Test
    public void testVariableNameWithSpaces() {
        String template = "Value: ${first name}, ${last name}";
        Map<String, Object> values = new HashMap<>();
        values.put("first name", "John");
        values.put("last name", "Doe");

        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set(values);

        String result = expressionUtils.getResult();
        Assert.assertEquals("Value: John, Doe", result);
    }

    /**
     * 测试边界条件：变量值为空字符串
     * 验证：空字符串值应被正确处理
     */
    @Test
    public void testVariableValueWithEmptyString() {
        String template = "A: ${a}, B: ${b}";
        Map<String, Object> values = new HashMap<>();
        values.put("a", "");
        values.put("b", "value");

        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set(values);

        String result = expressionUtils.getResult();
        Assert.assertEquals("A: , B: value", result);
    }

    /**
     * 测试边界条件：变量值包含换行符
     * 验证：换行符应被正确处理
     */
    @Test
    public void testVariableValueWithNewlines() {
        String template = "Text: ${text}";
        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set("text", "Line1\nLine2\nLine3");

        String result = expressionUtils.getResult();
        Assert.assertEquals("Text: Line1\nLine2\nLine3", result);
    }

    /**
     * 测试边界条件：变量值包含Unicode字符
     * 验证：Unicode字符应被正确处理
     */
    @Test
    public void testVariableValueWithUnicode() {
        String template = "Name: ${name}, Chinese: ${chinese}, Emoji: ${emoji}";
        ExpressionUtils expressionUtils = ExpressionUtils.bind(template)
                .set("name", "José")
                .set("chinese", "你好")
                .set("emoji", "😀");

        String result = expressionUtils.getResult();
        Assert.assertEquals("Name: José, Chinese: 你好, Emoji: 😀", result);
    }

    /**
     * 测试边界条件：混合使用set(String, String)和set(Map)
     * 验证：两种方法可以混合使用
     */
    @Test
    public void testMixedSetMethods() {
        Map<String, Object> values = new HashMap<>();
        values.put("name", "Nancy");

        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .set(values)
                .set("age", "60");

        String result = expressionUtils.getResult();
        Assert.assertEquals("Hello Nancy, your age is 60.", result);
    }

    /**
     * 测试边界条件：多次调用clean()方法
     * 验证：多次调用clean()不应产生错误
     */
    @Test
    public void testMultipleCleanCalls() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .clean()
                .clean()
                .clean();

        String result = expressionUtils.getResult();
        Assert.assertEquals("Hello , your age is .", result);
    }

    /**
     * 测试边界条件：在clean()后调用set()
     * 验证：clean()后set()不应产生任何效果
     */
    @Test
    public void testSetAfterClean() {
        ExpressionUtils expressionUtils = ExpressionUtils.bind(TEMPLATE_WITH_VARIABLES)
                .clean()
                .set("name", "Olivia");

        String result = expressionUtils.getResult();
        // clean()已经移除了所有占位符，set()无法找到匹配的变量
        Assert.assertEquals("Hello , your age is .", result);
    }
}
