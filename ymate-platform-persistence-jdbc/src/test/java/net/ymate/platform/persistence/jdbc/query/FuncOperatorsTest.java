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
package net.ymate.platform.persistence.jdbc.query;

import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.core.annotation.EnableDevMode;
import net.ymate.platform.core.persistence.IFunction;
import net.ymate.platform.core.persistence.annotation.Entity;
import net.ymate.platform.core.persistence.annotation.Id;
import net.ymate.platform.core.persistence.annotation.Property;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.test.YMPJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author 刘镇 (suninformation@163.com) on 2026-02-27 00:00:00
 * @since 3.0.0
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableDevMode
@EnableBeanProxy
@EnableAutoScan
public class FuncOperatorsTest {

    @Entity
    public static class TestEntity implements IEntity<String> {

        @Id
        @Property
        private String id;

        @Property
        private String name;

        @Property
        private Integer age;

        @Property
        private Double salary;

        @Property
        private String description;

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setId(String id) {
            this.id = id;
        }
    }

    @Test
    public void testBracketsWithFunction() {
        IFunction innerFunc = Func.math.ABS("value");
        IFunction func = Func.operators.brackets(innerFunc);
        String sql = func.build();
        System.out.println("testBracketsWithFunction: " + sql);
        Assert.assertEquals("(ABS(value))", sql);
    }

    @Test
    public void testBracketsWithString() {
        IFunction func = Func.operators.brackets("a + b");
        String sql = func.build();
        System.out.println("testBracketsWithString: " + sql);
        Assert.assertEquals("(a + b)", sql);
    }

    @Test
    public void testQuotesWithFunction() {
        IFunction innerFunc = Func.strings.CONCAT("Hello", "World");
        IFunction func = Func.operators.quotes(innerFunc);
        String sql = func.build();
        System.out.println("testQuotesWithFunction: " + sql);
        Assert.assertEquals("'CONCAT(Hello, World)'", sql);
    }

    @Test
    public void testQuotesWithString() {
        IFunction func = Func.operators.quotes("Hello World");
        String sql = func.build();
        System.out.println("testQuotesWithString: " + sql);
        Assert.assertEquals("'Hello World'", sql);
    }

    @Test
    public void testAdditionWithNumber() {
        IFunction func = Func.operators.addition(10);
        String sql = func.build();
        System.out.println("testAdditionWithNumber: " + sql);
        Assert.assertEquals("+ 10", sql);
    }

    @Test
    public void testAdditionWithStringAndNumber() {
        IFunction func = Func.operators.addition("salary", 1000);
        String sql = func.build();
        System.out.println("testAdditionWithStringAndNumber: " + sql);
        Assert.assertEquals("salary + 1000", sql);
    }

    @Test
    public void testAdditionWithFunction() {
        IFunction innerFunc = Func.math.ABS("value");
        IFunction func = Func.operators.addition(innerFunc);
        String sql = func.build();
        System.out.println("testAdditionWithFunction: " + sql);
        Assert.assertEquals("+ ABS(value)", sql);
    }

    @Test
    public void testAdditionWithStringAndFunction() {
        IFunction innerFunc = Func.math.ABS("value");
        IFunction func = Func.operators.addition("salary", innerFunc);
        String sql = func.build();
        System.out.println("testAdditionWithStringAndFunction: " + sql);
        Assert.assertEquals("salary + ABS(value)", sql);
    }

    @Test
    public void testAdditionWithTwoFunctions() {
        IFunction func1 = Func.math.ABS("value1");
        IFunction func2 = Func.math.ABS("value2");
        IFunction func = Func.operators.addition(func1, func2);
        String sql = func.build();
        System.out.println("testAdditionWithTwoFunctions: " + sql);
        Assert.assertEquals("ABS(value1) + ABS(value2)", sql);
    }

    @Test
    public void testAdditionWithFunctionAndString() {
        IFunction func1 = Func.math.ABS("value");
        IFunction func = Func.operators.addition(func1, "100");
        String sql = func.build();
        System.out.println("testAdditionWithFunctionAndString: " + sql);
        Assert.assertEquals("ABS(value) + 100", sql);
    }

    @Test
    public void testAdditionWithFunctionAndNumber() {
        IFunction func1 = Func.math.ABS("value");
        IFunction func = Func.operators.addition(func1, 100);
        String sql = func.build();
        System.out.println("testAdditionWithFunctionAndNumber: " + sql);
        Assert.assertEquals("ABS(value) + 100", sql);
    }

    @Test
    public void testAdditionWithString() {
        IFunction func = Func.operators.addition("10");
        String sql = func.build();
        System.out.println("testAdditionWithString: " + sql);
        Assert.assertEquals("+ 10", sql);
    }

    @Test
    public void testAdditionWithTwoStrings() {
        IFunction func = Func.operators.addition("salary", "1000");
        String sql = func.build();
        System.out.println("testAdditionWithTwoStrings: " + sql);
        Assert.assertEquals("salary + 1000", sql);
    }

    @Test
    public void testSubtractWithNumber() {
        IFunction func = Func.operators.subtract(10);
        String sql = func.build();
        System.out.println("testSubtractWithNumber: " + sql);
        Assert.assertEquals("- 10", sql);
    }

    @Test
    public void testSubtractWithStringAndNumber() {
        IFunction func = Func.operators.subtract("salary", 1000);
        String sql = func.build();
        System.out.println("testSubtractWithStringAndNumber: " + sql);
        Assert.assertEquals("salary - 1000", sql);
    }

    @Test
    public void testSubtractWithNumberAndString() {
        IFunction func = Func.operators.subtract(1000, "salary");
        String sql = func.build();
        System.out.println("testSubtractWithNumberAndString: " + sql);
        Assert.assertEquals("1000 - salary", sql);
    }

    @Test
    public void testSubtractWithFunction() {
        IFunction innerFunc = Func.math.ABS("value");
        IFunction func = Func.operators.subtract(innerFunc);
        String sql = func.build();
        System.out.println("testSubtractWithFunction: " + sql);
        Assert.assertEquals("- ABS(value)", sql);
    }

    @Test
    public void testSubtractWithStringAndFunction() {
        IFunction innerFunc = Func.math.ABS("value");
        IFunction func = Func.operators.subtract("salary", innerFunc);
        String sql = func.build();
        System.out.println("testSubtractWithStringAndFunction: " + sql);
        Assert.assertEquals("salary - ABS(value)", sql);
    }

    @Test
    public void testSubtractWithFunctionAndString() {
        IFunction func1 = Func.math.ABS("value");
        IFunction func = Func.operators.subtract(func1, "100");
        String sql = func.build();
        System.out.println("testSubtractWithFunctionAndString: " + sql);
        Assert.assertEquals("ABS(value) - 100", sql);
    }

    @Test
    public void testSubtractWithTwoFunctions() {
        IFunction func1 = Func.math.ABS("value1");
        IFunction func2 = Func.math.ABS("value2");
        IFunction func = Func.operators.subtract(func1, func2);
        String sql = func.build();
        System.out.println("testSubtractWithTwoFunctions: " + sql);
        Assert.assertEquals("ABS(value1) - ABS(value2)", sql);
    }

    @Test
    public void testSubtractWithString() {
        IFunction func = Func.operators.subtract("10");
        String sql = func.build();
        System.out.println("testSubtractWithString: " + sql);
        Assert.assertEquals("- 10", sql);
    }

    @Test
    public void testSubtractWithTwoStrings() {
        IFunction func = Func.operators.subtract("salary", "1000");
        String sql = func.build();
        System.out.println("testSubtractWithTwoStrings: " + sql);
        Assert.assertEquals("salary - 1000", sql);
    }

    @Test
    public void testMultiplyWithNumber() {
        IFunction func = Func.operators.multiply(10);
        String sql = func.build();
        System.out.println("testMultiplyWithNumber: " + sql);
        Assert.assertEquals("* 10", sql);
    }

    @Test
    public void testMultiplyWithStringAndNumber() {
        IFunction func = Func.operators.multiply("salary", 2);
        String sql = func.build();
        System.out.println("testMultiplyWithStringAndNumber: " + sql);
        Assert.assertEquals("salary * 2", sql);
    }

    @Test
    public void testMultiplyWithFunction() {
        IFunction innerFunc = Func.math.ABS("value");
        IFunction func = Func.operators.multiply(innerFunc);
        String sql = func.build();
        System.out.println("testMultiplyWithFunction: " + sql);
        Assert.assertEquals("* ABS(value)", sql);
    }

    @Test
    public void testMultiplyWithStringAndFunction() {
        IFunction innerFunc = Func.math.ABS("value");
        IFunction func = Func.operators.multiply("salary", innerFunc);
        String sql = func.build();
        System.out.println("testMultiplyWithStringAndFunction: " + sql);
        Assert.assertEquals("salary * ABS(value)", sql);
    }

    @Test
    public void testMultiplyWithTwoFunctions() {
        IFunction func1 = Func.math.ABS("value1");
        IFunction func2 = Func.math.ABS("value2");
        IFunction func = Func.operators.multiply(func1, func2);
        String sql = func.build();
        System.out.println("testMultiplyWithTwoFunctions: " + sql);
        Assert.assertEquals("ABS(value1) * ABS(value2)", sql);
    }

    @Test
    public void testMultiplyWithFunctionAndString() {
        IFunction func1 = Func.math.ABS("value");
        IFunction func = Func.operators.multiply(func1, "2");
        String sql = func.build();
        System.out.println("testMultiplyWithFunctionAndString: " + sql);
        Assert.assertEquals("ABS(value) * 2", sql);
    }

    @Test
    public void testMultiplyWithFunctionAndNumber() {
        IFunction func1 = Func.math.ABS("value");
        IFunction func = Func.operators.multiply(func1, 2);
        String sql = func.build();
        System.out.println("testMultiplyWithFunctionAndNumber: " + sql);
        Assert.assertEquals("ABS(value) * 2", sql);
    }

    @Test
    public void testMultiplyWithString() {
        IFunction func = Func.operators.multiply("10");
        String sql = func.build();
        System.out.println("testMultiplyWithString: " + sql);
        Assert.assertEquals("* 10", sql);
    }

    @Test
    public void testMultiplyWithTwoStrings() {
        IFunction func = Func.operators.multiply("salary", "2");
        String sql = func.build();
        System.out.println("testMultiplyWithTwoStrings: " + sql);
        Assert.assertEquals("salary * 2", sql);
    }

    @Test
    public void testDivideWithNumber() {
        IFunction func = Func.operators.divide(10);
        String sql = func.build();
        System.out.println("testDivideWithNumber: " + sql);
        Assert.assertEquals("/ 10", sql);
    }

    @Test
    public void testDivideWithStringAndNumber() {
        IFunction func = Func.operators.divide("salary", 2);
        String sql = func.build();
        System.out.println("testDivideWithStringAndNumber: " + sql);
        Assert.assertEquals("salary / 2", sql);
    }

    @Test
    public void testDivideWithNumberAndString() {
        IFunction func = Func.operators.divide(1000, "salary");
        String sql = func.build();
        System.out.println("testDivideWithNumberAndString: " + sql);
        Assert.assertEquals("1000 / salary", sql);
    }

    @Test
    public void testDivideWithFunction() {
        IFunction innerFunc = Func.math.ABS("value");
        IFunction func = Func.operators.divide(innerFunc);
        String sql = func.build();
        System.out.println("testDivideWithFunction: " + sql);
        Assert.assertEquals("/ ABS(value)", sql);
    }

    @Test
    public void testDivideWithStringAndFunction() {
        IFunction innerFunc = Func.math.ABS("value");
        IFunction func = Func.operators.divide("salary", innerFunc);
        String sql = func.build();
        System.out.println("testDivideWithStringAndFunction: " + sql);
        Assert.assertEquals("salary / ABS(value)", sql);
    }

    @Test
    public void testDivideWithFunctionAndString() {
        IFunction func1 = Func.math.ABS("value");
        IFunction func = Func.operators.divide(func1, "2");
        String sql = func.build();
        System.out.println("testDivideWithFunctionAndString: " + sql);
        Assert.assertEquals("ABS(value) / 2", sql);
    }

    @Test
    public void testDivideWithFunctionAndNumber() {
        IFunction func1 = Func.math.ABS("value");
        IFunction func = Func.operators.divide(func1, 2);
        String sql = func.build();
        System.out.println("testDivideWithFunctionAndNumber: " + sql);
        Assert.assertEquals("ABS(value) / 2", sql);
    }

    @Test
    public void testDivideWithTwoFunctions() {
        IFunction func1 = Func.math.ABS("value1");
        IFunction func2 = Func.math.ABS("value2");
        IFunction func = Func.operators.divide(func1, func2);
        String sql = func.build();
        System.out.println("testDivideWithTwoFunctions: " + sql);
        Assert.assertEquals("ABS(value1) / ABS(value2)", sql);
    }

    @Test
    public void testDivideWithString() {
        IFunction func = Func.operators.divide("10");
        String sql = func.build();
        System.out.println("testDivideWithString: " + sql);
        Assert.assertEquals("/ 10", sql);
    }

    @Test
    public void testDivideWithTwoStrings() {
        IFunction func = Func.operators.divide("salary", "2");
        String sql = func.build();
        System.out.println("testDivideWithTwoStrings: " + sql);
        Assert.assertEquals("salary / 2", sql);
    }

    @Test
    public void testComplexOperatorExpression() {
        IFunction func = Func.operators.addition(
                Func.operators.multiply("salary", 1.1),
                Func.operators.subtract("bonus", 100)
        );
        String sql = func.build();
        System.out.println("testComplexOperatorExpression: " + sql);
        Assert.assertEquals("salary * 1.1 + bonus - 100", sql);
    }

    @Test
    public void testNestedOperators() {
        IFunction innerFunc = Func.operators.addition("a", "b");
        IFunction func = Func.operators.multiply(innerFunc, "c");
        String sql = func.build();
        System.out.println("testNestedOperators: " + sql);
        Assert.assertEquals("a + b * c", sql);
    }

    @Test
    public void testOperatorsWithBrackets() {
        IFunction innerFunc = Func.operators.addition("a", "b");
        IFunction func = Func.operators.brackets(innerFunc);
        IFunction result = Func.operators.multiply(func, "c");
        String sql = result.build();
        System.out.println("testOperatorsWithBrackets: " + sql);
        Assert.assertEquals("(a + b) * c", sql);
    }

    @Test
    public void testOperatorsWithAggregateFunctions() {
        IFunction sumFunc = Func.aggregate.SUM("salary");
        IFunction countFunc = Func.aggregate.COUNT("id");
        IFunction func = Func.operators.divide(sumFunc, countFunc);
        String sql = func.build();
        System.out.println("testOperatorsWithAggregateFunctions: " + sql);
        Assert.assertEquals("SUM(salary) / COUNT(id)", sql);
    }

    @Test
    public void testOperatorsWithMathFunctions() {
        IFunction absFunc = Func.math.ABS("value");
        IFunction roundFunc = Func.math.ROUND("price", 2);
        IFunction func = Func.operators.addition(absFunc, roundFunc);
        String sql = func.build();
        System.out.println("testOperatorsWithMathFunctions: " + sql);
        Assert.assertEquals("ABS(value) + ROUND(price, 2)", sql);
    }
}
