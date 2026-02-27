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
 * Func Window 功能测试类 - 完整覆盖Func接口中Window相关的所有功能
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-27
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class FuncWindowTest {

    @Entity
    public static class TestEntity implements IEntity<String> {

        @Id
        @Property
        private String id;

        @Property
        private String name;

        @Property
        private Integer salary;

        @Property
        private String departmentId;

        @Property
        private String hireDate;

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getSalary() {
            return salary;
        }

        public void setSalary(Integer salary) {
            this.salary = salary;
        }

        public String getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(String departmentId) {
            this.departmentId = departmentId;
        }

        public String getHireDate() {
            return hireDate;
        }

        public void setHireDate(String hireDate) {
            this.hireDate = hireDate;
        }
    }

    @Test
    public void testROW_NUMBER() {
        IFunction func = Func.window.ROW_NUMBER();
        String sql = func.build();
        System.out.println("testROW: " + sql);
        Assert.assertEquals("ROW_NUMBER()", sql);
    }

    @Test
    public void testROW_NUMBERWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("salary");

        IFunction func = Func.window.ROW_NUMBER(over);
        String sql = func.build();
        System.out.println("testROW: " + sql);
        Assert.assertEquals("ROW_NUMBER() OVER (PARTITION BY department_id ORDER BY salary ASC)", sql);
    }

    @Test
    public void testRANK() {
        IFunction func = Func.window.RANK();
        String sql = func.build();
        System.out.println("testRANK: " + sql);
        Assert.assertEquals("RANK()", sql);
    }

    @Test
    public void testRANKWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByDesc("salary");

        IFunction func = Func.window.RANK(over);
        String sql = func.build();
        System.out.println("testRANKWithOver: " + sql);
        Assert.assertEquals("RANK() OVER (PARTITION BY department_id ORDER BY salary DESC)", sql);
    }

    @Test
    public void testDENSE_RANK() {
        IFunction func = Func.window.DENSE_RANK();
        String sql = func.build();
        System.out.println("testDENSE: " + sql);
        Assert.assertEquals("DENSE_RANK()", sql);
    }

    @Test
    public void testDENSE_RANKWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.window.DENSE_RANK(over);
        String sql = func.build();
        System.out.println("testDENSE: " + sql);
        Assert.assertEquals("DENSE_RANK() OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testPERCENT_RANK() {
        IFunction func = Func.window.PERCENT_RANK();
        String sql = func.build();
        System.out.println("testPERCENT: " + sql);
        Assert.assertEquals("PERCENT_RANK()", sql);
    }

    @Test
    public void testPERCENT_RANKWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("hire_date");

        IFunction func = Func.window.PERCENT_RANK(over);
        String sql = func.build();
        System.out.println("testPERCENT: " + sql);
        Assert.assertEquals("PERCENT_RANK() OVER (PARTITION BY department_id ORDER BY hire_date ASC)", sql);
    }

    @Test
    public void testCUME_DIST() {
        IFunction func = Func.window.CUME_DIST();
        String sql = func.build();
        System.out.println("testCUME: " + sql);
        Assert.assertEquals("CUME_DIST()", sql);
    }

    @Test
    public void testCUME_DISTWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.window.CUME_DIST(over);
        String sql = func.build();
        System.out.println("testCUME: " + sql);
        Assert.assertEquals("CUME_DIST() OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testNTILE() {
        IFunction func = Func.window.NTILE(4);
        String sql = func.build();
        System.out.println("testNTILE: " + sql);
        Assert.assertEquals("NTILE(4)", sql);
    }

    @Test
    public void testNTILEWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByDesc("salary");

        IFunction func = Func.window.NTILE(10, over);
        String sql = func.build();
        System.out.println("testNTILEWithOver: " + sql);
        Assert.assertEquals("NTILE(10) OVER (PARTITION BY department_id ORDER BY salary DESC)", sql);
    }

    @Test
    public void testLAG() {
        IFunction func = Func.window.LAG("salary");
        String sql = func.build();
        System.out.println("testLAG: " + sql);
        Assert.assertEquals("LAG(salary)", sql);
    }

    @Test
    public void testLAGWithLambda() {
        SFunction<TestEntity, Integer> column = TestEntity::getSalary;
        IFunction func = Func.window.LAG(column);
        String sql = func.build();
        System.out.println("testLAGWithLambda: " + sql);
        Assert.assertEquals("LAG(salary)", sql);
    }

    @Test
    public void testLAGWithOffset() {
        IFunction func = Func.window.LAG("salary", 1);
        String sql = func.build();
        System.out.println("testLAGWithOffset: " + sql);
        Assert.assertEquals("LAG(salary, 1)", sql);
    }

    @Test
    public void testLAGWithOffsetAndDefault() {
        IFunction func = Func.window.LAG("salary", 1, "0");
        String sql = func.build();
        System.out.println("testLAGWithOffsetAndDefault: " + sql);
        Assert.assertEquals("LAG(salary, 1, 0)", sql);
    }

    @Test
    public void testLAGWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("hire_date");

        IFunction func = Func.window.LAG("salary", over);
        String sql = func.build();
        System.out.println("testLAGWithOver: " + sql);
        Assert.assertEquals("LAG(salary) OVER (PARTITION BY department_id ORDER BY hire_date ASC)", sql);
    }

    @Test
    public void testLAGWithOffsetAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("hire_date");

        IFunction func = Func.window.LAG("salary", 2, over);
        String sql = func.build();
        System.out.println("testLAGWithOffsetAndOver: " + sql);
        Assert.assertEquals("LAG(salary, 2) OVER (PARTITION BY department_id ORDER BY hire_date ASC)", sql);
    }

    @Test
    public void testLAGWithAllParamsAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("hire_date");

        IFunction func = Func.window.LAG("salary", 1, "0", over);
        String sql = func.build();
        System.out.println("testLAGWithAllParamsAndOver: " + sql);
        Assert.assertEquals("LAG(salary, 1, 0) OVER (PARTITION BY department_id ORDER BY hire_date ASC)", sql);
    }

    @Test
    public void testLEAD() {
        IFunction func = Func.window.LEAD("salary");
        String sql = func.build();
        System.out.println("testLEAD: " + sql);
        Assert.assertEquals("LEAD(salary)", sql);
    }

    @Test
    public void testLEADWithLambda() {
        SFunction<TestEntity, Integer> column = TestEntity::getSalary;
        IFunction func = Func.window.LEAD(column);
        String sql = func.build();
        System.out.println("testLEADWithLambda: " + sql);
        Assert.assertEquals("LEAD(salary)", sql);
    }

    @Test
    public void testLEADWithOffset() {
        IFunction func = Func.window.LEAD("salary", 1);
        String sql = func.build();
        System.out.println("testLEADWithOffset: " + sql);
        Assert.assertEquals("LEAD(salary, 1)", sql);
    }

    @Test
    public void testLEADWithOffsetAndDefault() {
        IFunction func = Func.window.LEAD("salary", 1, "0");
        String sql = func.build();
        System.out.println("testLEADWithOffsetAndDefault: " + sql);
        Assert.assertEquals("LEAD(salary, 1, 0)", sql);
    }

    @Test
    public void testLEADWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("hire_date");

        IFunction func = Func.window.LEAD("salary", over);
        String sql = func.build();
        System.out.println("testLEADWithOver: " + sql);
        Assert.assertEquals("LEAD(salary) OVER (PARTITION BY department_id ORDER BY hire_date ASC)", sql);
    }

    @Test
    public void testLEADWithOffsetAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("hire_date");

        IFunction func = Func.window.LEAD("salary", 2, over);
        String sql = func.build();
        System.out.println("testLEADWithOffsetAndOver: " + sql);
        Assert.assertEquals("LEAD(salary, 2) OVER (PARTITION BY department_id ORDER BY hire_date ASC)", sql);
    }

    @Test
    public void testLEADWithAllParamsAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("hire_date");

        IFunction func = Func.window.LEAD("salary", 1, "0", over);
        String sql = func.build();
        System.out.println("testLEADWithAllParamsAndOver: " + sql);
        Assert.assertEquals("LEAD(salary, 1, 0) OVER (PARTITION BY department_id ORDER BY hire_date ASC)", sql);
    }

    @Test
    public void testFIRST_VALUE() {
        IFunction func = Func.window.FIRST_VALUE("salary");
        String sql = func.build();
        System.out.println("testFIRST: " + sql);
        Assert.assertEquals("FIRST_VALUE(salary)", sql);
    }

    @Test
    public void testFIRST_VALUEWithLambda() {
        SFunction<TestEntity, Integer> column = TestEntity::getSalary;
        IFunction func = Func.window.FIRST_VALUE(column);
        String sql = func.build();
        System.out.println("testFIRST: " + sql);
        Assert.assertEquals("FIRST_VALUE(salary)", sql);
    }

    @Test
    public void testFIRST_VALUEWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("hire_date")
                .rowsBetweenUnbounded();

        IFunction func = Func.window.FIRST_VALUE("salary", over);
        String sql = func.build();
        System.out.println("testFIRST: " + sql);
        Assert.assertEquals("FIRST_VALUE(salary) OVER (PARTITION BY department_id ORDER BY hire_date ASC ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)", sql);
    }

    @Test
    public void testLAST_VALUE() {
        IFunction func = Func.window.LAST_VALUE("salary");
        String sql = func.build();
        System.out.println("testLAST: " + sql);
        Assert.assertEquals("LAST_VALUE(salary)", sql);
    }

    @Test
    public void testLAST_VALUEWithLambda() {
        SFunction<TestEntity, Integer> column = TestEntity::getSalary;
        IFunction func = Func.window.LAST_VALUE(column);
        String sql = func.build();
        System.out.println("testLAST: " + sql);
        Assert.assertEquals("LAST_VALUE(salary)", sql);
    }

    @Test
    public void testLAST_VALUEWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("hire_date")
                .rowsUnboundedPreceding();

        IFunction func = Func.window.LAST_VALUE("salary", over);
        String sql = func.build();
        System.out.println("testLAST: " + sql);
        Assert.assertEquals("LAST_VALUE(salary) OVER (PARTITION BY department_id ORDER BY hire_date ASC ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)", sql);
    }

    @Test
    public void testNTH_VALUE() {
        IFunction func = Func.window.NTH_VALUE("salary", 2);
        String sql = func.build();
        System.out.println("testNTH: " + sql);
        Assert.assertEquals("NTH_VALUE(salary, 2)", sql);
    }

    @Test
    public void testNTH_VALUEWithLambda() {
        SFunction<TestEntity, Integer> column = TestEntity::getSalary;
        IFunction func = Func.window.NTH_VALUE(column, 3);
        String sql = func.build();
        System.out.println("testNTH: " + sql);
        Assert.assertEquals("NTH_VALUE(salary, 3)", sql);
    }

    @Test
    public void testNTH_VALUEWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("hire_date");

        IFunction func = Func.window.NTH_VALUE("salary", 1, over);
        String sql = func.build();
        System.out.println("testNTH: " + sql);
        Assert.assertEquals("NTH_VALUE(salary, 1) OVER (PARTITION BY department_id ORDER BY hire_date ASC)", sql);
    }

    @Test
    public void testAggregateAVGWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.AVG("salary", over);
        String sql = func.build();
        System.out.println("testAggregateAVGWithOver: " + sql);
        Assert.assertEquals("AVG(salary) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testAggregateAVGDistinctWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.AVG(true, "salary", over);
        String sql = func.build();
        System.out.println("testAggregateAVGDistinctWithOver: " + sql);
        Assert.assertEquals("AVG(DISTINCT salary) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testAggregateSUMWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.SUM("salary", over);
        String sql = func.build();
        System.out.println("testAggregateSUMWithOver: " + sql);
        Assert.assertEquals("SUM(salary) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testAggregateCOUNTWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.COUNT("id", over);
        String sql = func.build();
        System.out.println("testAggregateCOUNTWithOver: " + sql);
        Assert.assertEquals("COUNT(id) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testAggregateCOUNTDistinctWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.COUNT(true, "id", over);
        String sql = func.build();
        System.out.println("testAggregateCOUNTDistinctWithOver: " + sql);
        Assert.assertEquals("COUNT(DISTINCT id) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testAggregateMAXWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByDesc("salary");

        IFunction func = Func.aggregate.MAX("salary", over);
        String sql = func.build();
        System.out.println("testAggregateMAXWithOver: " + sql);
        Assert.assertEquals("MAX(salary) OVER (PARTITION BY department_id ORDER BY salary DESC)", sql);
    }

    @Test
    public void testAggregateMINWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("salary");

        IFunction func = Func.aggregate.MIN("salary", over);
        String sql = func.build();
        System.out.println("testAggregateMINWithOver: " + sql);
        Assert.assertEquals("MIN(salary) OVER (PARTITION BY department_id ORDER BY salary ASC)", sql);
    }

    @Test
    public void testAggregateBIT_ANDWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.BIT_AND("status", over);
        String sql = func.build();
        System.out.println("testAggregateBIT: " + sql);
        Assert.assertEquals("BIT_AND(status) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testAggregateBIT_ORWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.BIT_OR("status", over);
        String sql = func.build();
        System.out.println("testAggregateBIT: " + sql);
        Assert.assertEquals("BIT_OR(status) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testAggregateBIT_XORWithOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        IFunction func = Func.aggregate.BIT_XOR("status", over);
        String sql = func.build();
        System.out.println("testAggregateBIT: " + sql);
        Assert.assertEquals("BIT_XOR(status) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testComplexWindowFunction() {
        WindowOver emptyOver = WindowOver.create();

        IFunction sumCountOver = Func.aggregate.SUM(Func.aggregate.COUNT("id"), emptyOver);
        String sql = sumCountOver.build();
        System.out.println("testComplexWindowFunction: " + sql);
        Assert.assertEquals("SUM(COUNT(id)) OVER ()", sql);
    }

    @Test
    public void testComplexWindowFunctionWithPartition() {
        WindowOver deptOver = WindowOver.create()
                .partitionBy("department_id");

        IFunction avgSalaryOver = Func.aggregate.AVG("salary", deptOver);
        String sql = avgSalaryOver.build();
        System.out.println("testComplexWindowFunctionWithPartition: " + sql);
        Assert.assertEquals("AVG(salary) OVER (PARTITION BY department_id)", sql);
    }

    @Test
    public void testWindowFunctionWithRowsFrame() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("hire_date")
                .rowsPreceding(2);

        IFunction func = Func.window.LAG("salary", over);
        String sql = func.build();
        System.out.println("testWindowFunctionWithRowsFrame: " + sql);
        Assert.assertEquals("LAG(salary) OVER (PARTITION BY department_id ORDER BY hire_date ASC ROWS BETWEEN 2 PRECEDING AND CURRENT ROW)", sql);
    }

    @Test
    public void testMultipleWindowFunctions() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByDesc("salary");

        IFunction rowNumber = Func.window.ROW_NUMBER(over);
        IFunction rank = Func.window.RANK(over);
        IFunction denseRank = Func.window.DENSE_RANK(over);

        Assert.assertEquals("ROW_NUMBER() OVER (PARTITION BY department_id ORDER BY salary DESC)", rowNumber.build());
        Assert.assertEquals("RANK() OVER (PARTITION BY department_id ORDER BY salary DESC)", rank.build());
        Assert.assertEquals("DENSE_RANK() OVER (PARTITION BY department_id ORDER BY salary DESC)", denseRank.build());
    }

    @Test
    public void testWindowFunctionWithLambdaAndOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("hire_date");

        SFunction<TestEntity, Integer> column = TestEntity::getSalary;
        IFunction func = Func.window.LAG(column, 1, over);
        String sql = func.build();
        System.out.println("testWindowFunctionWithLambdaAndOver: " + sql);
        Assert.assertEquals("LAG(salary, 1) OVER (PARTITION BY department_id ORDER BY hire_date ASC)", sql);
    }

    @Test
    public void testComplexQueryWithWindowFunctions() {
        // 构建复杂的SQL查询，包含窗口函数、聚合函数、数学函数和比较函数

        // 创建窗口函数OVER子句（无分区和排序）
        WindowOver over = WindowOver.create();

        // fault_count: COUNT(df.id)
        IFunction faultCount = Func.aggregate.COUNT("df.id");

        // fault_count_ratio: ROUND(COUNT(df.id) * 100.0 / NULLIF(SUM(COUNT(df.id)) OVER (), 0), 2)
        IFunction faultCountSum = Func.aggregate.SUM(faultCount, over);
        IFunction faultCountNullIf = Func.controlFlow.NULLIF(faultCountSum.build(), "0");
        IFunction faultCountRatio = Func.math.ROUND(
                Func.operators.divide(
                        Func.operators.multiply(faultCount, 100.0),
                        faultCountNullIf
                ),
                2
        );

        // fault_duration_hour: COALESCE(SUM(dsr.duration), 0) / 3600000
        IFunction faultDurationSum = Func.aggregate.SUM("dsr.duration");
        IFunction faultDurationCoalesce = Func.comparison.COALESCE(faultDurationSum, "0");
        IFunction faultDurationHour = Func.operators.divide(faultDurationCoalesce, 3600000);

        // fault_duration_ratio: ROUND(COALESCE(SUM(dsr.duration), 0) * 100.0 / NULLIF(SUM(COALESCE(SUM(dsr.duration), 0)) OVER (), 0), 2)
        IFunction faultDurationCoalesce2 = Func.comparison.COALESCE(faultDurationSum, "0");
        IFunction faultDurationSum2 = Func.aggregate.SUM(faultDurationCoalesce2, over);
        IFunction faultDurationNullIf = Func.controlFlow.NULLIF(faultDurationSum2.build(), "0");
        IFunction faultDurationRatio = Func.math.ROUND(
                Func.operators.divide(
                        Func.operators.multiply(faultDurationCoalesce2, 100.0),
                        faultDurationNullIf
                ),
                2
        );

        // fault_lost_energy: COALESCE(SUM(dsr.lost_energy), 0)
        IFunction lostEnergySum = Func.aggregate.SUM("dsr.lost_energy");
        IFunction faultLostEnergy = Func.comparison.COALESCE(lostEnergySum, "0");

        // fault_lost_energy_ratio: ROUND(COALESCE(SUM(dsr.lost_energy), 0) * 100.0 / NULLIF(SUM(COALESCE(SUM(dsr.lost_energy), 0)) OVER (), 0), 2)
        IFunction lostEnergyCoalesce = Func.comparison.COALESCE(lostEnergySum, "0");
        IFunction lostEnergySum2 = Func.aggregate.SUM(lostEnergyCoalesce, over);
        IFunction lostEnergyNullIf = Func.controlFlow.NULLIF(lostEnergySum2.build(), "0");
        IFunction faultLostEnergyRatio = Func.math.ROUND(
                Func.operators.divide(
                        Func.operators.multiply(lostEnergyCoalesce, 100.0),
                        lostEnergyNullIf
                ),
                2
        );

        // 构建SELECT语句
        Select select = Select.create();
        select.field("dc.name AS component_name");
        select.field(faultCount, "fault_count");
        select.field(faultCountRatio, "fault_count_ratio");
        select.field(faultDurationHour, "fault_duration_hour");
        select.field(faultDurationRatio, "fault_duration_ratio");
        select.field(faultLostEnergy, "fault_lost_energy");
        select.field(faultLostEnergyRatio, "fault_lost_energy_ratio");

        // 构建FROM和JOIN
        select.from("device_fault", "df");
        select.join(Join.inner("device_component").alias("dc").on(
                Cond.create().eqField("df.device_component_code", "dc.code")
        ));
        select.join(Join.inner("device").alias("d").on(
                Cond.create().eqField("dc.device_model_id", "d.device_model_id")
        ));
        select.join(Join.inner("device_status_process_record_daily").alias("dsr").on(
                Cond.create().eqField("d.id", "dsr.device_id")
                        .and().eqField("df.code", "dsr.plc_first_error")
        ));

        // 构建WHERE条件
        select.where(
                Cond.create().opt("d.plant_id", Cond.OPT.EQ, "'HSQFD'")
                        .and().opt("dsr.stat_date", Cond.OPT.GT_EQ, "1767254400000")
                        .and().opt("dsr.stat_date", Cond.OPT.LT_EQ, "1769932799999")
        );

        // 构建GROUP BY
        select.groupBy("dc.name");

        // 构建HAVING
        select.having(Cond.create().opt(faultCount.build(), Cond.OPT.GT, "0"));

        // 构建ORDER BY
        OrderBy orderBy = OrderBy.create();
        orderBy.asc("component_name");
        select.orderBy(orderBy);

        // 生成SQL
        String sql = select.toString();

        // 打印生成的SQL用于调试
        System.out.println("Generated SQL:");
        System.out.println(sql);
        System.out.println();

        // 验证SQL包含关键部分
        Assert.assertTrue("Should contain SELECT", sql.contains("SELECT"));
        Assert.assertTrue("Should contain component_name", sql.contains("component_name"));
        Assert.assertTrue("Should contain COUNT(df.id) AS fault_count", sql.contains("COUNT(df.id) AS fault_count"));
        Assert.assertTrue("Should contain ROUND(", sql.contains("ROUND("));
        Assert.assertTrue("Should contain NULLIF(", sql.contains("NULLIF("));
        Assert.assertTrue("Should contain OVER ()", sql.contains("OVER ()"));
        Assert.assertTrue("Should contain COALESCE(", sql.contains("COALESCE("));
        Assert.assertTrue("Should contain SUM(", sql.contains("SUM("));
        Assert.assertTrue("Should contain FROM", sql.contains("FROM"));
        Assert.assertTrue("Should contain device_fault", sql.contains("device_fault"));
        Assert.assertTrue("Should contain JOIN", sql.contains("JOIN"));
        Assert.assertTrue("Should contain device_component", sql.contains("device_component"));
        Assert.assertTrue("Should contain device", sql.contains("device"));
        Assert.assertTrue("Should contain alias d", sql.contains(" d ") || sql.contains("`d`"));
        Assert.assertTrue("Should contain device_status_process_record_daily", sql.contains("device_status_process_record_daily"));
        Assert.assertTrue("Should contain WHERE", sql.contains("WHERE"));
        Assert.assertTrue("Should contain d.plant_id = 'HSQFD'", sql.contains("d.plant_id = 'HSQFD'"));
        Assert.assertTrue("Should contain dsr.stat_date >= 1767254400000", sql.contains("dsr.stat_date >= 1767254400000"));
        Assert.assertTrue("Should contain dsr.stat_date <= 1769932799999", sql.contains("dsr.stat_date <= 1769932799999"));
        Assert.assertTrue("Should contain GROUP BY", sql.contains("GROUP BY"));
        Assert.assertTrue("Should contain HAVING", sql.contains("HAVING"));
        Assert.assertTrue("Should contain COUNT(df.id) > 0", sql.contains("COUNT(df.id) > 0"));
        Assert.assertTrue("Should contain ORDER BY", sql.contains("ORDER BY"));
    }
}
