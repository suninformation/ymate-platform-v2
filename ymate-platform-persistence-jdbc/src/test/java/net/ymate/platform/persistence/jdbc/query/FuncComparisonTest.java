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
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableDevMode
@EnableBeanProxy
@EnableAutoScan
public class FuncComparisonTest {

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
    public void testBETWEEN() {
        IFunction func = Func.comparison.BETWEEN(18, 65);
        String sql = func.build();
        System.out.println("testBETWEEN: " + sql);
        Assert.assertEquals("BETWEEN 18 AND 65", sql);
    }

    @Test
    public void testBETWEENWithStrings() {
        IFunction func = Func.comparison.BETWEEN("'A'", "'Z'");
        String sql = func.build();
        System.out.println("testBETWEENWithStrings: " + sql);
        Assert.assertEquals("BETWEEN 'A' AND 'Z'", sql);
    }

    @Test
    public void testBETWEENWithColumns() {
        IFunction func = Func.comparison.BETWEEN("min_age", "max_age");
        String sql = func.build();
        System.out.println("testBETWEENWithColumns: " + sql);
        Assert.assertEquals("BETWEEN min_age AND max_age", sql);
    }

    @Test
    public void testCOALESCE() {
        IFunction func = Func.comparison.COALESCE("NULL", "'default'");
        String sql = func.build();
        System.out.println("testCOALESCE: " + sql);
        Assert.assertEquals("COALESCE(NULL, 'default')", sql);
    }

    @Test
    public void testCOALESCEWithMultipleValues() {
        IFunction func = Func.comparison.COALESCE("value1", "value2", "value3", "'default'");
        String sql = func.build();
        System.out.println("testCOALESCEWithMultipleValues: " + sql);
        Assert.assertEquals("COALESCE(value1, value2, value3, 'default')", sql);
    }

    @Test
    public void testCOALESCEWithColumns() {
        IFunction func = Func.comparison.COALESCE("name", "nickname", "'Unknown'");
        String sql = func.build();
        System.out.println("testCOALESCEWithColumns: " + sql);
        Assert.assertEquals("COALESCE(name, nickname, 'Unknown')", sql);
    }

    @Test
    public void testEXISTSWithString() {
        IFunction func = Func.comparison.EXISTS("SELECT 1 FROM users WHERE id = 1");
        String sql = func.build();
        System.out.println("testEXISTSWithString: " + sql);
        Assert.assertEquals("EXISTS(SELECT 1 FROM users WHERE id = 1)", sql);
    }

    @Test
    public void testEXISTSWithSelect() {
        Select select = Select.create().field("id").from("users").where(Cond.create().eq("id").param("1"));
        IFunction func = Func.comparison.EXISTS(select);
        String sql = func.build();
        System.out.println("testEXISTSWithSelect: " + sql);
        Assert.assertEquals("EXISTS(SELECT  `id` FROM `users`  WHERE  id = ?)", sql);
    }

    @Test
    public void testNOT_EXISTSWithString() {
        IFunction func = Func.comparison.NOT_EXISTS("SELECT 1 FROM users WHERE id = 1");
        String sql = func.build();
        System.out.println("testNOT_EXISTSWithString: " + sql);
        Assert.assertEquals("NOT EXISTS(SELECT 1 FROM users WHERE id = 1)", sql);
    }

    @Test
    public void testNOT_EXISTSWithSelect() {
        Select select = Select.create().field("id").from("users").where(Cond.create().eq("id").param("1"));
        IFunction func = Func.comparison.NOT_EXISTS(select);
        String sql = func.build();
        System.out.println("testNOT_EXISTSWithSelect: " + sql);
        Assert.assertEquals("NOT EXISTS(SELECT  `id` FROM `users`  WHERE  id = ?)", sql);
    }

    @Test
    public void testGREATEST() {
        IFunction func = Func.comparison.GREATEST(10, 20, 30);
        String sql = func.build();
        System.out.println("testGREATEST: " + sql);
        Assert.assertEquals("GREATEST(10, 20, 30)", sql);
    }

    @Test
    public void testGREATESTWithStrings() {
        IFunction func = Func.comparison.GREATEST("'A'", "'B'", "'C'");
        String sql = func.build();
        System.out.println("testGREATESTWithStrings: " + sql);
        Assert.assertEquals("GREATEST('A', 'B', 'C')", sql);
    }

    @Test
    public void testGREATESTWithColumns() {
        IFunction func = Func.comparison.GREATEST("price1", "price2", "price3");
        String sql = func.build();
        System.out.println("testGREATESTWithColumns: " + sql);
        Assert.assertEquals("GREATEST(price1, price2, price3)", sql);
    }

    @Test
    public void testGREATESTWithFunctions() {
        IFunction func = Func.comparison.GREATEST(
                Func.math.ABS("value1"),
                Func.math.ABS("value2"),
                Func.math.ABS("value3")
        );
        String sql = func.build();
        System.out.println("testGREATESTWithFunctions: " + sql);
        Assert.assertEquals("GREATEST(ABS(value1), ABS(value2), ABS(value3))", sql);
    }

    @Test
    public void testIN() {
        IFunction func = Func.comparison.IN(1, 2, 3, 4, 5);
        String sql = func.build();
        System.out.println("testIN: " + sql);
        Assert.assertEquals("IN(1, 2, 3, 4, 5)", sql);
    }

    @Test
    public void testINWithStrings() {
        IFunction func = Func.comparison.IN("'A'", "'B'", "'C'");
        String sql = func.build();
        System.out.println("testINWithStrings: " + sql);
        Assert.assertEquals("IN('A', 'B', 'C')", sql);
    }

    @Test
    public void testINWithColumns() {
        IFunction func = Func.comparison.IN("status", 1, 2, 3);
        String sql = func.build();
        System.out.println("testINWithColumns: " + sql);
        Assert.assertEquals("IN(status, 1, 2, 3)", sql);
    }

    @Test
    public void testINWithSingleValue() {
        IFunction func = Func.comparison.IN(1);
        String sql = func.build();
        System.out.println("testINWithSingleValue: " + sql);
        Assert.assertEquals("IN(1)", sql);
    }

    @Test
    public void testNOT_IN() {
        IFunction func = Func.comparison.NOT_IN(1, 2, 3, 4, 5);
        String sql = func.build();
        System.out.println("testNOT: " + sql);
        Assert.assertEquals("NOT IN(1, 2, 3, 4, 5)", sql);
    }

    @Test
    public void testNOT_INWithStrings() {
        IFunction func = Func.comparison.NOT_IN("'A'", "'B'", "'C'");
        String sql = func.build();
        System.out.println("testNOT: " + sql);
        Assert.assertEquals("NOT IN('A', 'B', 'C')", sql);
    }

    @Test
    public void testNOT_INWithColumns() {
        IFunction func = Func.comparison.NOT_IN("status", 1, 2, 3);
        String sql = func.build();
        System.out.println("testNOT: " + sql);
        Assert.assertEquals("NOT IN(status, 1, 2, 3)", sql);
    }

    @Test
    public void testIS() {
        IFunction func = Func.comparison.IS("NULL");
        String sql = func.build();
        System.out.println("testIS: " + sql);
        Assert.assertEquals(" IS NULL", sql);
    }

    @Test
    public void testISWithColumn() {
        IFunction func = Func.comparison.IS("TRUE");
        String sql = func.build();
        System.out.println("testISWithColumn: " + sql);
        Assert.assertEquals(" IS TRUE", sql);
    }

    @Test
    public void testIS_NOT() {
        IFunction func = Func.comparison.IS_NOT("NULL");
        String sql = func.build();
        System.out.println("testIS: " + sql);
        Assert.assertEquals(" IS NOT NULL", sql);
    }

    @Test
    public void testIS_NOTWithColumn() {
        IFunction func = Func.comparison.IS_NOT("TRUE");
        String sql = func.build();
        System.out.println("testIS: " + sql);
        Assert.assertEquals(" IS NOT TRUE", sql);
    }

    @Test
    public void testIS_NULL() {
        IFunction func = Func.comparison.IS_NULL();
        String sql = func.build();
        System.out.println("testIS: " + sql);
        Assert.assertEquals(" IS NULL", sql);
    }

    @Test
    public void testIS_NOT_NULL() {
        IFunction func = Func.comparison.IS_NOT_NULL();
        String sql = func.build();
        System.out.println("testIS: " + sql);
        Assert.assertEquals(" IS NOT NULL", sql);
    }

    @Test
    public void testISNULL() {
        IFunction func = Func.comparison.ISNULL("value");
        String sql = func.build();
        System.out.println("testISNULL: " + sql);
        Assert.assertEquals("ISNULL(value)", sql);
    }

    @Test
    public void testISNULLWithColumn() {
        IFunction func = Func.comparison.ISNULL("name");
        String sql = func.build();
        System.out.println("testISNULLWithColumn: " + sql);
        Assert.assertEquals("ISNULL(name)", sql);
    }

    @Test
    public void testISNULLWithFunction() {
        IFunction innerFunc = Func.math.ABS("value");
        IFunction func = Func.comparison.ISNULL(innerFunc.build());
        String sql = func.build();
        System.out.println("testISNULLWithFunction: " + sql);
        Assert.assertEquals("ISNULL(ABS(value))", sql);
    }

    @Test
    public void testLEAST() {
        IFunction func = Func.comparison.LEAST(10, 20, 30);
        String sql = func.build();
        System.out.println("testLEAST: " + sql);
        Assert.assertEquals("LEAST(10, 20, 30)", sql);
    }

    @Test
    public void testLEASTWithStrings() {
        IFunction func = Func.comparison.LEAST("'A'", "'B'", "'C'");
        String sql = func.build();
        System.out.println("testLEASTWithStrings: " + sql);
        Assert.assertEquals("LEAST('A', 'B', 'C')", sql);
    }

    @Test
    public void testLEASTWithColumns() {
        IFunction func = Func.comparison.LEAST("price1", "price2", "price3");
        String sql = func.build();
        System.out.println("testLEASTWithColumns: " + sql);
        Assert.assertEquals("LEAST(price1, price2, price3)", sql);
    }

    @Test
    public void testLEASTWithFunctions() {
        IFunction func = Func.comparison.LEAST(
                Func.math.ABS("value1"),
                Func.math.ABS("value2"),
                Func.math.ABS("value3")
        );
        String sql = func.build();
        System.out.println("testLEASTWithFunctions: " + sql);
        Assert.assertEquals("LEAST(ABS(value1), ABS(value2), ABS(value3))", sql);
    }

    @Test
    public void testComplexComparisonWithCOALESCEAndBETWEEN() {
        IFunction coalesceFunc = Func.comparison.COALESCE("age", 0);
        IFunction betweenFunc = Func.comparison.BETWEEN(18, 65);
        String sql = coalesceFunc.build() + " " + betweenFunc.build();
        System.out.println("testComplexComparisonWithCOALESCEAndBETWEEN: " + sql);
        Assert.assertEquals("COALESCE(age, 0) BETWEEN 18 AND 65", sql);
    }

    @Test
    public void testComplexComparisonWithGREATEstAndLEAST() {
        IFunction greatestFunc = Func.comparison.GREATEST("price1", "price2");
        IFunction leastFunc = Func.comparison.LEAST("price1", "price2");
        String sql = greatestFunc.build() + " - " + leastFunc.build();
        System.out.println("testComplexComparisonWithGREATEstAndLEAST: " + sql);
        Assert.assertEquals("GREATEST(price1, price2) - LEAST(price1, price2)", sql);
    }

    @Test
    public void testComparisonWithAggregateFunctions() {
        IFunction coalesceFunc = Func.comparison.COALESCE(
                Func.aggregate.COUNT("id"),
                0
        );
        String sql = coalesceFunc.build();
        System.out.println("testComparisonWithAggregateFunctions: " + sql);
        Assert.assertEquals("COALESCE(COUNT(id), 0)", sql);
    }

    @Test
    public void testComparisonWithMathFunctions() {
        IFunction greatestFunc = Func.comparison.GREATEST(
                Func.math.ABS("value1"),
                Func.math.ABS("value2")
        );
        String sql = greatestFunc.build();
        System.out.println("testComparisonWithMathFunctions: " + sql);
        Assert.assertEquals("GREATEST(ABS(value1), ABS(value2))", sql);
    }

    @Test
    public void testComparisonWithControlFlowFunctions() {
        IFunction coalesceFunc = Func.comparison.COALESCE(
                Func.controlFlow.IF(
                        Cond.create().eq("status").param("1"),
                        "'Active'",
                        "'Inactive'"
                ),
                "'Unknown'"
        );
        String sql = coalesceFunc.build();
        System.out.println("testComparisonWithControlFlowFunctions: " + sql);
        Assert.assertEquals("COALESCE(IF( status = ? , 'Active', 'Inactive'), 'Unknown')", sql);
    }

    @Test
    public void testEXISTSWithComplexSelect() {
        Select select = Select.create()
                .field("COUNT(*)")
                .from("orders")
                .where(Cond.create().eq("user_id").param("users.id"));
        IFunction func = Func.comparison.EXISTS(select);
        String sql = func.build();
        System.out.println("testEXISTSWithComplexSelect: " + sql);
        Assert.assertEquals("EXISTS(SELECT  `COUNT(*)` FROM `orders`  WHERE  user_id = ?)", sql);
    }

    @Test
    public void testNOT_EXISTSWithComplexSelect() {
        Select select = Select.create()
                .field("COUNT(*)")
                .from("orders")
                .where(Cond.create().eq("user_id").param("users.id"));
        IFunction func = Func.comparison.NOT_EXISTS(select);
        String sql = func.build();
        System.out.println("testNOT_EXISTSWithComplexSelect: " + sql);
        Assert.assertEquals("NOT EXISTS(SELECT  `COUNT(*)` FROM `orders`  WHERE  user_id = ?)", sql);
    }

    @Test
    public void testINWithSubquery() {
        IFunction func = Func.comparison.IN(
                "id",
                "SELECT user_id FROM orders WHERE amount > 1000"
        );
        String sql = func.build();
        System.out.println("testINWithSubquery: " + sql);
        Assert.assertEquals("IN(id, SELECT user_id FROM orders WHERE amount > 1000)", sql);
    }

    @Test
    public void testNOT_INWithSubquery() {
        IFunction func = Func.comparison.NOT_IN(
                "id",
                "SELECT user_id FROM orders WHERE amount > 1000"
        );
        String sql = func.build();
        System.out.println("testNOT: " + sql);
        Assert.assertEquals("NOT IN(id, SELECT user_id FROM orders WHERE amount > 1000)", sql);
    }

    @Test
    public void testBETWEENWithDates() {
        IFunction func = Func.comparison.BETWEEN("'2020-01-01'", "'2020-12-31'");
        String sql = func.build();
        System.out.println("testBETWEENWithDates: " + sql);
        Assert.assertEquals("BETWEEN '2020-01-01' AND '2020-12-31'", sql);
    }

    @Test
    public void testBETWEENWithFunctions() {
        IFunction func = Func.comparison.BETWEEN(
                Func.dateTime.DATE("'2020-01-01'"),
                Func.dateTime.DATE("'2020-12-31'")
        );
        String sql = func.build();
        System.out.println("testBETWEENWithFunctions: " + sql);
        Assert.assertEquals("BETWEEN DATE('2020-01-01') AND DATE('2020-12-31')", sql);
    }

    @Test
    public void testCOALESCEWithDateTimeFunctions() {
        IFunction func = Func.comparison.COALESCE(
                Func.dateTime.NOW(),
                Func.dateTime.DATE("'1970-01-01'")
        );
        String sql = func.build();
        System.out.println("testCOALESCEWithDateTimeFunctions: " + sql);
        Assert.assertEquals("COALESCE(NOW(), DATE('1970-01-01'))", sql);
    }

    @Test
    public void testGREATESTWithMixedTypes() {
        IFunction func = Func.comparison.GREATEST(
                Func.math.ABS("value1"),
                "value2",
                100
        );
        String sql = func.build();
        System.out.println("testGREATESTWithMixedTypes: " + sql);
        Assert.assertEquals("GREATEST(ABS(value1), value2, 100)", sql);
    }

    @Test
    public void testLEASTWithMixedTypes() {
        IFunction func = Func.comparison.LEAST(
                Func.math.ABS("value1"),
                "value2",
                0
        );
        String sql = func.build();
        System.out.println("testLEASTWithMixedTypes: " + sql);
        Assert.assertEquals("LEAST(ABS(value1), value2, 0)", sql);
    }
}
