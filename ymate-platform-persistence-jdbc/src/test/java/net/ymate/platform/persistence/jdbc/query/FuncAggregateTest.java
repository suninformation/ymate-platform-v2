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
import net.ymate.platform.core.persistence.LambdaUtils.SFunction;
import net.ymate.platform.core.persistence.annotation.Entity;
import net.ymate.platform.core.persistence.annotation.Id;
import net.ymate.platform.core.persistence.annotation.Property;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.test.YMPJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Func Aggregate 功能测试类 - 完整覆盖Func接口中Aggregate相关的所有功能
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-27
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class FuncAggregateTest {

    @Entity
    public static class TestEntity implements IEntity<String> {

        @Id
        @Property
        private String id;

        @Property
        private Double salary;

        @Property
        private Integer age;

        @Property
        private String departmentId;

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Double getSalary() {
            return salary;
        }

        public void setSalary(Double salary) {
            this.salary = salary;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(String departmentId) {
            this.departmentId = departmentId;
        }
    }

    @Test
    public void testAVG() {
        IFunction func = Func.aggregate.AVG("salary");
        String sql = func.build();
        System.out.println("testAVG: " + sql);
        Assert.assertEquals("AVG(salary)", sql);
    }

    @Test
    public void testAVGLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.AVG(column);
        String sql = func.build();
        System.out.println("testAVGLambda: " + sql);
        Assert.assertEquals("AVG(salary)", sql);
    }

    @Test
    public void testAVGLambdaWithPrefix() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.AVG("t", column);
        String sql = func.build();
        System.out.println("testAVGLambdaWithPrefix: " + sql);
        Assert.assertEquals("AVG(t.salary)", sql);
    }

    @Test
    public void testAVGLambdaDistinct() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.AVG(true, column);
        String sql = func.build();
        System.out.println("testAVGLambdaDistinct: " + sql);
        Assert.assertEquals("AVG(DISTINCT salary)", sql);
    }

    @Test
    public void testAVGLambdaWithPrefixDistinct() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.AVG("t", column, true);
        String sql = func.build();
        System.out.println("testAVGLambdaWithPrefixDistinct: " + sql);
        Assert.assertEquals("AVG(DISTINCT t.salary)", sql);
    }

    @Test
    public void testAVGWithFunction() {
        IFunction innerFunc = Func.math.ABS("salary");
        IFunction func = Func.aggregate.AVG(innerFunc);
        String sql = func.build();
        System.out.println("testAVGWithFunction: " + sql);
        Assert.assertEquals("AVG(ABS(salary))", sql);
    }

    @Test
    public void testAVGDistinct() {
        IFunction func = Func.aggregate.AVG(true, "salary");
        String sql = func.build();
        System.out.println("testAVGDistinct: " + sql);
        Assert.assertEquals("AVG(DISTINCT salary)", sql);
    }

    @Test
    public void testAVGDistinctWithFunction() {
        IFunction innerFunc = Func.math.ABS("salary");
        IFunction func = Func.aggregate.AVG(true, innerFunc.build());
        String sql = func.build();
        System.out.println("testAVGDistinctWithFunction: " + sql);
        Assert.assertEquals("AVG(DISTINCT ABS(salary))", sql);
    }

    @Test
    public void testAVGWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.AVG("salary", over);
        String sql = func.build();
        System.out.println("testAVGWithOver: " + sql);
        Assert.assertEquals("AVG(salary) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testAVGDistinctWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.AVG(true, "salary", over);
        String sql = func.build();
        System.out.println("testAVGDistinctWithOver: " + sql);
        Assert.assertEquals("AVG(DISTINCT salary) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testAVGWithFunctionAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction innerFunc = Func.math.ABS("salary");
        IFunction func = Func.aggregate.AVG(innerFunc, over);
        String sql = func.build();
        System.out.println("testAVGWithFunctionAndOver: " + sql);
        Assert.assertEquals("AVG(ABS(salary)) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testBIT_AND() {
        IFunction func = Func.aggregate.BIT_AND("status");
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_AND(status)", sql);
    }

    @Test
    public void testBIT_ANDWithFunction() {
        IFunction innerFunc = Func.math.ABS("status");
        IFunction func = Func.aggregate.BIT_AND(innerFunc);
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_AND(ABS(status))", sql);
    }

    @Test
    public void testBIT_ANDWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.BIT_AND("status", over);
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_AND(status) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testBIT_ANDWithFunctionAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction innerFunc = Func.math.ABS("status");
        IFunction func = Func.aggregate.BIT_AND(innerFunc, over);
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_AND(ABS(status)) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testBIT_OR() {
        IFunction func = Func.aggregate.BIT_OR("status");
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_OR(status)", sql);
    }

    @Test
    public void testBIT_ORWithFunction() {
        IFunction innerFunc = Func.math.ABS("status");
        IFunction func = Func.aggregate.BIT_OR(innerFunc);
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_OR(ABS(status))", sql);
    }

    @Test
    public void testBIT_ORWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.BIT_OR("status", over);
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_OR(status) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testBIT_ORWithFunctionAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction innerFunc = Func.math.ABS("status");
        IFunction func = Func.aggregate.BIT_OR(innerFunc, over);
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_OR(ABS(status)) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testBIT_XOR() {
        IFunction func = Func.aggregate.BIT_XOR("status");
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_XOR(status)", sql);
    }

    @Test
    public void testBIT_XORWithFunction() {
        IFunction innerFunc = Func.math.ABS("status");
        IFunction func = Func.aggregate.BIT_XOR(innerFunc);
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_XOR(ABS(status))", sql);
    }

    @Test
    public void testBIT_XORWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.BIT_XOR("status", over);
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_XOR(status) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testBIT_XORWithFunctionAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction innerFunc = Func.math.ABS("status");
        IFunction func = Func.aggregate.BIT_XOR(innerFunc, over);
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_XOR(ABS(status)) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testCOUNT() {
        IFunction func = Func.aggregate.COUNT("id");
        String sql = func.build();
        System.out.println("testCOUNT: " + sql);
        Assert.assertEquals("COUNT(id)", sql);
    }

    @Test
    public void testCOUNTLambda() {
        SFunction<TestEntity, String> column = TestEntity::getId;
        IFunction func = Func.aggregate.COUNT(column);
        String sql = func.build();
        System.out.println("testCOUNTLambda: " + sql);
        Assert.assertEquals("COUNT(id)", sql);
    }

    @Test
    public void testCOUNTLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getId;
        IFunction func = Func.aggregate.COUNT("t", column);
        String sql = func.build();
        System.out.println("testCOUNTLambdaWithPrefix: " + sql);
        Assert.assertEquals("COUNT(t.id)", sql);
    }

    @Test
    public void testCOUNTLambdaDistinct() {
        SFunction<TestEntity, String> column = TestEntity::getId;
        IFunction func = Func.aggregate.COUNT(true, column);
        String sql = func.build();
        System.out.println("testCOUNTLambdaDistinct: " + sql);
        Assert.assertEquals("COUNT(DISTINCT id)", sql);
    }

    @Test
    public void testCOUNTLambdaWithPrefixDistinct() {
        SFunction<TestEntity, String> column = TestEntity::getId;
        IFunction func = Func.aggregate.COUNT("t", column, true);
        String sql = func.build();
        System.out.println("testCOUNTLambdaWithPrefixDistinct: " + sql);
        Assert.assertEquals("COUNT(DISTINCT t.id)", sql);
    }

    @Test
    public void testCOUNTWithFunction() {
        IFunction innerFunc = Func.math.ABS("id");
        IFunction func = Func.aggregate.COUNT(innerFunc);
        String sql = func.build();
        System.out.println("testCOUNTWithFunction: " + sql);
        Assert.assertEquals("COUNT(ABS(id))", sql);
    }

    @Test
    public void testCOUNTDistinct() {
        IFunction func = Func.aggregate.COUNT(true, "id");
        String sql = func.build();
        System.out.println("testCOUNTDistinct: " + sql);
        Assert.assertEquals("COUNT(DISTINCT id)", sql);
    }

    @Test
    public void testCOUNTDistinctWithFunction() {
        IFunction innerFunc = Func.math.ABS("id");
        IFunction func = Func.aggregate.COUNT(true, innerFunc.build());
        String sql = func.build();
        System.out.println("testCOUNTDistinctWithFunction: " + sql);
        Assert.assertEquals("COUNT(DISTINCT ABS(id))", sql);
    }

    @Test
    public void testCOUNTWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.COUNT("id", over);
        String sql = func.build();
        System.out.println("testCOUNTWithOver: " + sql);
        Assert.assertEquals("COUNT(id) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testCOUNTDistinctWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.COUNT(true, "id", over);
        String sql = func.build();
        System.out.println("testCOUNTDistinctWithOver: " + sql);
        Assert.assertEquals("COUNT(DISTINCT id) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testCOUNTWithFunctionAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction innerFunc = Func.math.ABS("id");
        IFunction func = Func.aggregate.COUNT(innerFunc, over);
        String sql = func.build();
        System.out.println("testCOUNTWithFunctionAndOver: " + sql);
        Assert.assertEquals("COUNT(ABS(id)) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testGROUP_CONCAT() {
        IFunction func = Func.aggregate.GROUP_CONCAT("name");
        String sql = func.build();
        System.out.println("testGROUP: " + sql);
        Assert.assertEquals("GROUP_CONCAT(name)", sql);
    }

    @Test
    public void testGROUP_CONCATDistinct() {
        IFunction func = Func.aggregate.GROUP_CONCAT(true, "name");
        String sql = func.build();
        System.out.println("testGROUP: " + sql);
        Assert.assertEquals("GROUP_CONCAT(DISTINCT name)", sql);
    }

    @Test
    public void testGROUP_CONCATWithOrderBy() {
        OrderBy orderBy = OrderBy.create().asc("name");
        IFunction func = Func.aggregate.GROUP_CONCAT(false, orderBy, null, "name");
        String sql = func.build();
        System.out.println("testGROUP: " + sql);
        Assert.assertEquals("GROUP_CONCAT(name ORDER BY `name` ASC)", sql);
    }

    @Test
    public void testGROUP_CONCATWithSeparator() {
        IFunction func = Func.aggregate.GROUP_CONCAT("name");
        String sql = func.build();
        System.out.println("testGROUP: " + sql);
        Assert.assertEquals("GROUP_CONCAT(name)", sql);
    }

    @Test
    public void testGROUP_CONCATWithOrderByAndSeparator() {
        OrderBy orderBy = OrderBy.create().asc("name");
        IFunction func = Func.aggregate.GROUP_CONCAT(false, orderBy, ",", "name");
        String sql = func.build();
        System.out.println("testGROUP: " + sql);
        Assert.assertEquals("GROUP_CONCAT(name ORDER BY `name` ASC SEPARATOR ,)", sql);
    }

    @Test
    public void testGROUP_CONCATMultipleFields() {
        IFunction func = Func.aggregate.GROUP_CONCAT("name", "age");
        String sql = func.build();
        System.out.println("testGROUP: " + sql);
        Assert.assertEquals("GROUP_CONCAT(name, age)", sql);
    }

    @Test
    public void testGROUP_CONCATWithFunction() {
        IFunction func1 = Func.strings.UPPER("name");
        IFunction func2 = Func.math.ABS("age");
        IFunction func = Func.aggregate.GROUP_CONCAT(func1, func2);
        String sql = func.build();
        System.out.println("testGROUP: " + sql);
        Assert.assertEquals("GROUP_CONCAT(UPPER(name), ABS(age))", sql);
    }

    @Test
    public void testMAX() {
        IFunction func = Func.aggregate.MAX("salary");
        String sql = func.build();
        System.out.println("testMAX: " + sql);
        Assert.assertEquals("MAX(salary)", sql);
    }

    @Test
    public void testMAXLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.MAX(column);
        String sql = func.build();
        System.out.println("testMAXLambda: " + sql);
        Assert.assertEquals("MAX(salary)", sql);
    }

    @Test
    public void testMAXLambdaWithPrefix() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.MAX("t", column);
        String sql = func.build();
        System.out.println("testMAXLambdaWithPrefix: " + sql);
        Assert.assertEquals("MAX(t.salary)", sql);
    }

    @Test
    public void testMAXLambdaDistinct() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.MAX(true, column);
        String sql = func.build();
        System.out.println("testMAXLambdaDistinct: " + sql);
        Assert.assertEquals("MAX(DISTINCT salary)", sql);
    }

    @Test
    public void testMAXLambdaWithPrefixDistinct() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.MAX("t", column, true);
        String sql = func.build();
        System.out.println("testMAXLambdaWithPrefixDistinct: " + sql);
        Assert.assertEquals("MAX(DISTINCT t.salary)", sql);
    }

    @Test
    public void testMAXWithFunction() {
        IFunction innerFunc = Func.math.ABS("salary");
        IFunction func = Func.aggregate.MAX(innerFunc);
        String sql = func.build();
        System.out.println("testMAXWithFunction: " + sql);
        Assert.assertEquals("MAX(ABS(salary))", sql);
    }

    @Test
    public void testMAXDistinct() {
        IFunction func = Func.aggregate.MAX(true, "salary");
        String sql = func.build();
        System.out.println("testMAXDistinct: " + sql);
        Assert.assertEquals("MAX(DISTINCT salary)", sql);
    }

    @Test
    public void testMAXDistinctWithFunction() {
        IFunction innerFunc = Func.math.ABS("salary");
        IFunction func = Func.aggregate.MAX(true, innerFunc);
        String sql = func.build();
        System.out.println("testMAXDistinctWithFunction: " + sql);
        Assert.assertEquals("MAX(DISTINCT ABS(salary))", sql);
    }

    @Test
    public void testMAXWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByDesc("salary");

        IFunction func = Func.aggregate.MAX("salary", over);
        String sql = func.build();
        System.out.println("testMAXWithOver: " + sql);
        Assert.assertEquals("MAX(salary) OVER (PARTITION BY department_id ORDER BY salary DESC)", sql);
    }

    @Test
    public void testMAXDistinctWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.MAX(true, "salary", over);
        String sql = func.build();
        System.out.println("testMAXDistinctWithOver: " + sql);
        Assert.assertEquals("MAX(DISTINCT salary) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testMAXWithFunctionAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction innerFunc = Func.math.ABS("salary");
        IFunction func = Func.aggregate.MAX(innerFunc, over);
        String sql = func.build();
        System.out.println("testMAXWithFunctionAndOver: " + sql);
        Assert.assertEquals("MAX(ABS(salary)) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testMIN() {
        IFunction func = Func.aggregate.MIN("salary");
        String sql = func.build();
        System.out.println("testMIN: " + sql);
        Assert.assertEquals("MIN(salary)", sql);
    }

    @Test
    public void testMINLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.MIN(column);
        String sql = func.build();
        System.out.println("testMINLambda: " + sql);
        Assert.assertEquals("MIN(salary)", sql);
    }

    @Test
    public void testMINLambdaWithPrefix() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.MIN("t", column);
        String sql = func.build();
        System.out.println("testMINLambdaWithPrefix: " + sql);
        Assert.assertEquals("MIN(t.salary)", sql);
    }

    @Test
    public void testMINLambdaDistinct() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.MIN(true, column);
        String sql = func.build();
        System.out.println("testMINLambdaDistinct: " + sql);
        Assert.assertEquals("MIN(DISTINCT salary)", sql);
    }

    @Test
    public void testMINLambdaWithPrefixDistinct() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.MIN("t", column, true);
        String sql = func.build();
        System.out.println("testMINLambdaWithPrefixDistinct: " + sql);
        Assert.assertEquals("MIN(DISTINCT t.salary)", sql);
    }

    @Test
    public void testMINWithFunction() {
        IFunction innerFunc = Func.math.ABS("salary");
        IFunction func = Func.aggregate.MIN(innerFunc);
        String sql = func.build();
        System.out.println("testMINWithFunction: " + sql);
        Assert.assertEquals("MIN(ABS(salary))", sql);
    }

    @Test
    public void testMINDistinct() {
        IFunction func = Func.aggregate.MIN(true, "salary");
        String sql = func.build();
        System.out.println("testMINDistinct: " + sql);
        Assert.assertEquals("MIN(DISTINCT salary)", sql);
    }

    @Test
    public void testMINDistinctWithFunction() {
        IFunction innerFunc = Func.math.ABS("salary");
        IFunction func = Func.aggregate.MIN(true, innerFunc);
        String sql = func.build();
        System.out.println("testMINDistinctWithFunction: " + sql);
        Assert.assertEquals("MIN(DISTINCT ABS(salary))", sql);
    }

    @Test
    public void testMINWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("salary");

        IFunction func = Func.aggregate.MIN("salary", over);
        String sql = func.build();
        System.out.println("testMINWithOver: " + sql);
        Assert.assertEquals("MIN(salary) OVER (PARTITION BY department_id ORDER BY salary ASC)", sql);
    }

    @Test
    public void testMINDistinctWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.MIN(true, "salary", over);
        String sql = func.build();
        System.out.println("testMINDistinctWithOver: " + sql);
        Assert.assertEquals("MIN(DISTINCT salary) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testMINWithFunctionAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction innerFunc = Func.math.ABS("salary");
        IFunction func = Func.aggregate.MIN(innerFunc, over);
        String sql = func.build();
        System.out.println("testMINWithFunctionAndOver: " + sql);
        Assert.assertEquals("MIN(ABS(salary)) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testSUM() {
        IFunction func = Func.aggregate.SUM("salary");
        String sql = func.build();
        System.out.println("testSUM: " + sql);
        Assert.assertEquals("SUM(salary)", sql);
    }

    @Test
    public void testSUMLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.SUM(column);
        String sql = func.build();
        System.out.println("testSUMLambda: " + sql);
        Assert.assertEquals("SUM(salary)", sql);
    }

    @Test
    public void testSUMLambdaWithPrefix() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.SUM("t", column);
        String sql = func.build();
        System.out.println("testSUMLambdaWithPrefix: " + sql);
        Assert.assertEquals("SUM(t.salary)", sql);
    }

    @Test
    public void testSUMLambdaDistinct() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.SUM(true, column);
        String sql = func.build();
        System.out.println("testSUMLambdaDistinct: " + sql);
        Assert.assertEquals("SUM(DISTINCT salary)", sql);
    }

    @Test
    public void testSUMLambdaWithPrefixDistinct() {
        SFunction<TestEntity, Double> column = TestEntity::getSalary;
        IFunction func = Func.aggregate.SUM("t", column, true);
        String sql = func.build();
        System.out.println("testSUMLambdaWithPrefixDistinct: " + sql);
        Assert.assertEquals("SUM(DISTINCT t.salary)", sql);
    }

    @Test
    public void testSUMWithFunction() {
        IFunction innerFunc = Func.math.ABS("salary");
        IFunction func = Func.aggregate.SUM(innerFunc);
        String sql = func.build();
        System.out.println("testSUMWithFunction: " + sql);
        Assert.assertEquals("SUM(ABS(salary))", sql);
    }

    @Test
    public void testSUMDistinct() {
        IFunction func = Func.aggregate.SUM(true, "salary");
        String sql = func.build();
        System.out.println("testSUMDistinct: " + sql);
        Assert.assertEquals("SUM(DISTINCT salary)", sql);
    }

    @Test
    public void testSUMDistinctWithFunction() {
        IFunction innerFunc = Func.math.ABS("salary");
        IFunction func = Func.aggregate.SUM(true, innerFunc);
        String sql = func.build();
        System.out.println("testSUMDistinctWithFunction: " + sql);
        Assert.assertEquals("SUM(DISTINCT ABS(salary))", sql);
    }

    @Test
    public void testSUMWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.SUM("salary", over);
        String sql = func.build();
        System.out.println("testSUMWithOver: " + sql);
        Assert.assertEquals("SUM(salary) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testSUMDistinctWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.SUM(true, "salary", over);
        String sql = func.build();
        System.out.println("testSUMDistinctWithOver: " + sql);
        Assert.assertEquals("SUM(DISTINCT salary) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testSUMWithFunctionAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction innerFunc = Func.math.ABS("salary");
        IFunction func = Func.aggregate.SUM(innerFunc, over);
        String sql = func.build();
        System.out.println("testSUMWithFunctionAndOver: " + sql);
        Assert.assertEquals("SUM(ABS(salary)) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testComplexAggregateFunction() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.math.ROUND(
                Func.aggregate.SUM("salary", over),
                2
        );
        String sql = func.build();
        System.out.println("testComplexAggregateFunction: " + sql);
        Assert.assertEquals("ROUND(SUM(salary) OVER (PARTITION BY department_id), 2)", sql);
    }

    @Test
    public void testNestedAggregateFunction() {
        IFunction func = Func.math.ROUND(
                Func.aggregate.AVG(
                        Func.math.ABS("salary")
                ),
                2
        );
        String sql = func.build();
        System.out.println("testNestedAggregateFunction: " + sql);
        Assert.assertEquals("ROUND(AVG(ABS(salary)), 2)", sql);
    }
}
