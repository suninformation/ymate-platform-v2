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
 * Func Control Flow 功能测试类 - 完整覆盖Func接口中Control Flow相关的所有功能
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-27
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class FuncControlFlowTest {

    @Entity
    public static class TestEntity implements IEntity<String> {

        @Id
        @Property
        private String id;

        @Property
        private Integer status;

        @Property
        private String name;

        @Property
        private Double value;

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    @Test
    public void testCASEWithWhen() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("status = 1", "'Active'"),
                Func.controlFlow.WHEN("status = 0", "'Inactive'")
        };
        IFunction func = Func.controlFlow.CASE(whenFn);
        String sql = func.build();
        System.out.println("testCASEWithWhen: " + sql);
        Assert.assertEquals("CASE WHEN status = 1 THEN 'Active'  WHEN status = 0 THEN 'Inactive'  END", sql);
    }

    @Test
    public void testCASEWithWhenAndElse() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("status = 1", "'Active'"),
                Func.controlFlow.WHEN("status = 0", "'Inactive'")
        };
        IFunction func = Func.controlFlow.CASE((String) null, whenFn, "'Unknown'");
        String sql = func.build();
        System.out.println("testCASEWithWhenAndElse: " + sql);
        Assert.assertEquals("CASE WHEN status = 1 THEN 'Active'  WHEN status = 0 THEN 'Inactive'  'Unknown' END", sql);
    }

    @Test
    public void testCASEWithWhenAndElseFunction() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("status = 1", "'Active'"),
                Func.controlFlow.WHEN("status = 0", "'Inactive'")
        };
        IFunction elseFn = Func.strings.UPPER("'unknown'");
        IFunction func = Func.controlFlow.CASE((String) null, whenFn, elseFn);
        String sql = func.build();
        System.out.println("testCASEWithWhenAndElseFunction: " + sql);
        Assert.assertEquals("CASE WHEN status = 1 THEN 'Active'  WHEN status = 0 THEN 'Inactive'  UPPER('unknown') END", sql);
    }

    @Test
    public void testCASEWithValueAndWhen() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("1", "'Active'"),
                Func.controlFlow.WHEN("0", "'Inactive'")
        };
        IFunction func = Func.controlFlow.CASE("status", whenFn);
        String sql = func.build();
        System.out.println("testCASEWithValueAndWhen: " + sql);
        Assert.assertEquals("CASE status WHEN 1 THEN 'Active'  WHEN 0 THEN 'Inactive'  END", sql);
    }

    @Test
    public void testCASEWithValueWhenAndElse() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("1", "'Active'"),
                Func.controlFlow.WHEN("0", "'Inactive'")
        };
        IFunction func = Func.controlFlow.CASE("status", whenFn, "'Unknown'");
        String sql = func.build();
        System.out.println("testCASEWithValueWhenAndElse: " + sql);
        Assert.assertEquals("CASE status WHEN 1 THEN 'Active'  WHEN 0 THEN 'Inactive'  'Unknown' END", sql);
    }

    @Test
    public void testCASEWithValueWhenAndElseFunction() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("1", "'Active'"),
                Func.controlFlow.WHEN("0", "'Inactive'")
        };
        IFunction elseFn = Func.strings.UPPER("'unknown'");
        IFunction func = Func.controlFlow.CASE("status", whenFn, elseFn);
        String sql = func.build();
        System.out.println("testCASEWithValueWhenAndElseFunction: " + sql);
        Assert.assertEquals("CASE status WHEN 1 THEN 'Active'  WHEN 0 THEN 'Inactive'  UPPER('unknown') END", sql);
    }

    @Test
    public void testCASEWithFunctionValueAndWhen() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("1", "'Active'"),
                Func.controlFlow.WHEN("0", "'Inactive'")
        };
        IFunction valueFn = Func.math.ABS("status");
        IFunction func = Func.controlFlow.CASE(valueFn, whenFn);
        String sql = func.build();
        System.out.println("testCASEWithFunctionValueAndWhen: " + sql);
        Assert.assertEquals("CASE ABS(status) WHEN 1 THEN 'Active'  WHEN 0 THEN 'Inactive'  END", sql);
    }

    @Test
    public void testCASEWithFunctionValueWhenAndElse() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("1", "'Active'"),
                Func.controlFlow.WHEN("0", "'Inactive'")
        };
        IFunction valueFn = Func.math.ABS("status");
        IFunction elseFn = Func.strings.UPPER("'unknown'");
        IFunction func = Func.controlFlow.CASE(valueFn, whenFn, elseFn);
        String sql = func.build();
        System.out.println("testCASEWithFunctionValueWhenAndElse: " + sql);
        Assert.assertEquals("CASE ABS(status) WHEN 1 THEN 'Active'  WHEN 0 THEN 'Inactive'  UPPER('unknown') END", sql);
    }

    @Test
    public void testCASEWithFunctionValueWhenAndElseFunction() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("1", "'Active'"),
                Func.controlFlow.WHEN("0", "'Inactive'")
        };
        IFunction valueFn = Func.math.ABS("status");
        IFunction elseFn = Func.strings.UPPER("'unknown'");
        IFunction func = Func.controlFlow.CASE(valueFn, whenFn, elseFn);
        String sql = func.build();
        System.out.println("testCASEWithFunctionValueWhenAndElseFunction: " + sql);
        Assert.assertEquals("CASE ABS(status) WHEN 1 THEN 'Active'  WHEN 0 THEN 'Inactive'  UPPER('unknown') END", sql);
    }

    @Test
    public void testCASEWithCondValueAndWhen() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("1", "'Active'"),
                Func.controlFlow.WHEN("0", "'Inactive'")
        };
        Cond valueCond = Cond.create().eq("status", "1");
        IFunction func = Func.controlFlow.CASE(valueCond, whenFn, "'Unknown'");
        String sql = func.build();
        System.out.println("testCASEWithCondValueAndWhen: " + sql);
        Assert.assertEquals("CASE  status.1 = ?  WHEN 1 THEN 'Active'  WHEN 0 THEN 'Inactive'  'Unknown' END", sql);
    }

    @Test
    public void testCASEWithCondValueWhenAndElse() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("1", "'Active'"),
                Func.controlFlow.WHEN("0", "'Inactive'")
        };
        Cond valueCond = Cond.create().eq("status", "1");
        IFunction elseFn = Func.strings.UPPER("'unknown'");
        IFunction func = Func.controlFlow.CASE(valueCond, whenFn, elseFn);
        String sql = func.build();
        System.out.println("testCASEWithCondValueWhenAndElse: " + sql);
        Assert.assertEquals("CASE  status.1 = ?  WHEN 1 THEN 'Active'  WHEN 0 THEN 'Inactive'  UPPER('unknown') END", sql);
    }

    @Test
    public void testCASEWithCondValueWhenAndElseFunction() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("1", "'Active'"),
                Func.controlFlow.WHEN("0", "'Inactive'")
        };
        Cond valueCond = Cond.create().eq("status", "1");
        IFunction elseFn = Func.strings.UPPER("'unknown'");
        IFunction func = Func.controlFlow.CASE(valueCond, whenFn, elseFn);
        String sql = func.build();
        System.out.println("testCASEWithCondValueWhenAndElseFunction: " + sql);
        Assert.assertEquals("CASE  status.1 = ?  WHEN 1 THEN 'Active'  WHEN 0 THEN 'Inactive'  UPPER('unknown') END", sql);
    }

    @Test
    public void testCASEWithSingleWhen() {
        IFunction whenFn = Func.controlFlow.WHEN("status = 1", "'Active'");
        IFunction func = Func.controlFlow.CASE(new IFunction[]{whenFn});
        String sql = func.build();
        System.out.println("testCASEWithSingleWhen: " + sql);
        Assert.assertEquals("CASE WHEN status = 1 THEN 'Active'  END", sql);
    }

    @Test
    public void testCASEWithSingleWhenAndElse() {
        IFunction whenFn = Func.controlFlow.WHEN("status = 1", "'Active'");
        IFunction elseFn = Func.strings.UPPER("'inactive'");
        IFunction func = Func.controlFlow.CASE(new IFunction[]{whenFn}, elseFn);
        String sql = func.build();
        System.out.println("testCASEWithSingleWhenAndElse: " + sql);
        Assert.assertEquals("CASE WHEN status = 1 THEN 'Active'  UPPER('inactive') END", sql);
    }

    @Test
    public void testWHENWithString() {
        IFunction func = Func.controlFlow.WHEN("status = 1");
        String sql = func.build();
        System.out.println("testWHENWithString: " + sql);
        Assert.assertEquals("WHEN status = 1 THEN ? ", sql);
    }

    @Test
    public void testWHENWithStringAndResult() {
        IFunction func = Func.controlFlow.WHEN("status = 1", "'Active'");
        String sql = func.build();
        System.out.println("testWHENWithStringAndResult: " + sql);
        Assert.assertEquals("WHEN status = 1 THEN 'Active' ", sql);
    }

    @Test
    public void testWHENWithFunction() {
        IFunction expr = Func.math.ABS("status");
        IFunction func = Func.controlFlow.WHEN(expr);
        String sql = func.build();
        System.out.println("testWHENWithFunction: " + sql);
        Assert.assertEquals("WHEN ABS(status) THEN ? ", sql);
    }

    @Test
    public void testWHENWithFunctionAndResult() {
        IFunction expr = Func.math.ABS("status");
        IFunction result = Func.strings.UPPER("'active'");
        IFunction func = Func.controlFlow.WHEN(expr, result);
        String sql = func.build();
        System.out.println("testWHENWithFunctionAndResult: " + sql);
        Assert.assertEquals("WHEN ABS(status) THEN UPPER('active') ", sql);
    }

    @Test
    public void testWHENWithCond() {
        Cond cond = Cond.create().eq("status", "1");
        IFunction func = Func.controlFlow.WHEN(cond);
        String sql = func.build();
        System.out.println("testWHENWithCond: " + sql);
        Assert.assertEquals("WHEN  status.1 = ?  THEN ? ", sql);
    }

    @Test
    public void testWHENWithCondAndResult() {
        Cond cond = Cond.create().eq("status", "1");
        IFunction result = Func.strings.UPPER("'active'");
        IFunction func = Func.controlFlow.WHEN(cond, result);
        String sql = func.build();
        System.out.println("testWHENWithCondAndResult: " + sql);
        Assert.assertEquals("WHEN  status.1 = ?  THEN UPPER('active') ", sql);
    }

    @Test
    public void testELSE() {
        IFunction func = Func.controlFlow.ELSE();
        String sql = func.build();
        System.out.println("testELSE: " + sql);
        Assert.assertEquals("ELSE ? ", sql);
    }

    @Test
    public void testELSEWithString() {
        IFunction func = Func.controlFlow.ELSE("'Unknown'");
        String sql = func.build();
        System.out.println("testELSEWithString: " + sql);
        Assert.assertEquals("ELSE 'Unknown' ", sql);
    }

    @Test
    public void testELSEWithFunction() {
        IFunction result = Func.strings.UPPER("'unknown'");
        IFunction func = Func.controlFlow.ELSE(result);
        String sql = func.build();
        System.out.println("testELSEWithFunction: " + sql);
        Assert.assertEquals("ELSE UPPER('unknown') ", sql);
    }

    @Test
    public void testIFWithCond() {
        Cond cond = Cond.create().eq("status", "1");
        IFunction func = Func.controlFlow.IF(cond);
        String sql = func.build();
        System.out.println("testIFWithCond: " + sql);
        Assert.assertEquals("IF( status.1 = ? , ?, ?)", sql);
    }

    @Test
    public void testIFWithCondAndStrings() {
        Cond cond = Cond.create().eq("status", "1");
        IFunction func = Func.controlFlow.IF(cond, "'Active'", "'Inactive'");
        String sql = func.build();
        System.out.println("testIFWithCondAndStrings: " + sql);
        Assert.assertEquals("IF( status.1 = ? , 'Active', 'Inactive')", sql);
    }

    @Test
    public void testIFWithCondAndFunctionString() {
        Cond cond = Cond.create().eq("status", "1");
        IFunction expr2 = Func.strings.UPPER("'active'");
        IFunction func = Func.controlFlow.IF(cond, expr2, "'Inactive'");
        String sql = func.build();
        System.out.println("testIFWithCondAndFunctionString: " + sql);
        Assert.assertEquals("IF( status.1 = ? , UPPER('active'), 'Inactive')", sql);
    }

    @Test
    public void testIFWithCondAndStringFunction() {
        Cond cond = Cond.create().eq("status", "1");
        IFunction expr3 = Func.strings.UPPER("'inactive'");
        IFunction func = Func.controlFlow.IF(cond, "'Active'", expr3);
        String sql = func.build();
        System.out.println("testIFWithCondAndStringFunction: " + sql);
        Assert.assertEquals("IF( status.1 = ? , 'Active', UPPER('inactive'))", sql);
    }

    @Test
    public void testIFWithCondAndFunctions() {
        Cond cond = Cond.create().eq("status", "1");
        IFunction expr2 = Func.strings.UPPER("'active'");
        IFunction expr3 = Func.strings.UPPER("'inactive'");
        IFunction func = Func.controlFlow.IF(cond, expr2, expr3);
        String sql = func.build();
        System.out.println("testIFWithCondAndFunctions: " + sql);
        Assert.assertEquals("IF( status.1 = ? , UPPER('active'), UPPER('inactive'))", sql);
    }

    @Test
    public void testIFWithStrings() {
        IFunction func = Func.controlFlow.IF("status = 1", "'Active'", "'Inactive'");
        String sql = func.build();
        System.out.println("testIFWithStrings: " + sql);
        Assert.assertEquals("IF(status = 1, 'Active', 'Inactive')", sql);
    }

    @Test
    public void testIFNULL() {
        IFunction func = Func.controlFlow.IFNULL();
        String sql = func.build();
        System.out.println("testIFNULL: " + sql);
        Assert.assertEquals("IFNULL(?, ?)", sql);
    }

    @Test
    public void testIFNULLWithStrings() {
        IFunction func = Func.controlFlow.IFNULL("name", "'Unknown'");
        String sql = func.build();
        System.out.println("testIFNULLWithStrings: " + sql);
        Assert.assertEquals("IFNULL(name, 'Unknown')", sql);
    }

    @Test
    public void testIFNULLWithFunctions() {
        IFunction expr1 = Func.strings.TRIM("name");
        IFunction expr2 = Func.strings.UPPER("'unknown'");
        IFunction func = Func.controlFlow.IFNULL(expr1, expr2);
        String sql = func.build();
        System.out.println("testIFNULLWithFunctions: " + sql);
        Assert.assertEquals("IFNULL(TRIM(name), UPPER('unknown'))", sql);
    }

    @Test
    public void testNULLIF() {
        IFunction func = Func.controlFlow.NULLIF();
        String sql = func.build();
        System.out.println("testNULLIF: " + sql);
        Assert.assertEquals("NULLIF(?, ?)", sql);
    }

    @Test
    public void testNULLIFWithStrings() {
        IFunction func = Func.controlFlow.NULLIF("value", "0");
        String sql = func.build();
        System.out.println("testNULLIFWithStrings: " + sql);
        Assert.assertEquals("NULLIF(value, 0)", sql);
    }

    @Test
    public void testNULLIFWithFunctions() {
        IFunction expr1 = Func.math.ABS("value");
        IFunction expr2 = Func.math.ROUND("0", 2);
        IFunction func = Func.controlFlow.NULLIF(expr1, expr2);
        String sql = func.build();
        System.out.println("testNULLIFWithFunctions: " + sql);
        Assert.assertEquals("NULLIF(ABS(value), ROUND(0, 2))", sql);
    }

    @Test
    public void testComplexControlFlow() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("status = 1", "'Active'"),
                Func.controlFlow.WHEN("status = 0", "'Inactive'")
        };
        IFunction caseFunc = Func.controlFlow.CASE((String) null, whenFn, "'Unknown'");

        IFunction ifFunc = Func.controlFlow.IF(
                Cond.create().eq("value", "0"),
                "'Zero'",
                caseFunc
        );

        String sql = ifFunc.build();
        System.out.println("testComplexControlFlow: " + sql);
        Assert.assertEquals("IF( value.0 = ? , 'Zero', CASE WHEN status = 1 THEN 'Active'  WHEN status = 0 THEN 'Inactive'  'Unknown' END)", sql);
    }

    @Test
    public void testNestedIF() {
        IFunction innerIF = Func.controlFlow.IF(
                Cond.create().eq("status", "1"),
                "'Active'",
                "'Inactive'"
        );

        IFunction outerIF = Func.controlFlow.IF(
                Cond.create().eq("value", "0"),
                "'Zero'",
                innerIF
        );

        String sql = outerIF.build();
        System.out.println("testNestedIF: " + sql);
        Assert.assertEquals("IF( value.0 = ? , 'Zero', IF( status.1 = ? , 'Active', 'Inactive'))", sql);
    }

    @Test
    public void testCASEWithMathFunctions() {
        IFunction[] whenFn = new IFunction[]{
                Func.controlFlow.WHEN("value > 100", "'High'"),
                Func.controlFlow.WHEN("value > 50", "'Medium'"),
                Func.controlFlow.WHEN("value > 0", "'Low'")
        };
        IFunction func = Func.controlFlow.CASE((String) null, whenFn, "'Zero'");
        String sql = func.build();
        System.out.println("testCASEWithMathFunctions: " + sql);
        Assert.assertEquals("CASE WHEN value > 100 THEN 'High'  WHEN value > 50 THEN 'Medium'  WHEN value > 0 THEN 'Low'  'Zero' END", sql);
    }

    @Test
    public void testIFWithAggregateFunctions() {
        Cond cond = Cond.create();
        IFunction func = Func.controlFlow.IF(
                cond.gt(Func.aggregate.COUNT("id"), 0),
                Func.aggregate.AVG("value"),
                "0"
        );
        String sql = func.build();
        System.out.println("testIFWithAggregateFunctions: " + sql);
        Assert.assertEquals("IF( COUNT(id) > ? , AVG(value), 0)", sql);
    }

    @Test
    public void testIFNULLWithDateTimeFunctions() {
        IFunction func = Func.controlFlow.IFNULL(
                Func.dateTime.DATE("create_time"),
                Func.dateTime.CURDATE()
        );
        String sql = func.build();
        System.out.println("testIFNULLWithDateTimeFunctions: " + sql);
        Assert.assertEquals("IFNULL(DATE(create_time), CURDATE())", sql);
    }

    @Test
    public void testNULLIFWithStringFunctions() {
        IFunction func = Func.controlFlow.NULLIF(
                Func.strings.TRIM("name"),
                Func.strings.UPPER("'empty'")
        );
        String sql = func.build();
        System.out.println("testNULLIFWithStringFunctions: " + sql);
        Assert.assertEquals("NULLIF(TRIM(name), UPPER('empty'))", sql);
    }
}
