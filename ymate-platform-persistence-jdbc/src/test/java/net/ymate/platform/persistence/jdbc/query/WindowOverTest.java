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
import net.ymate.platform.test.YMPJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * WindowOver 功能测试类 - 完整覆盖WindowOver类的所有功能
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-27
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class WindowOverTest {

    @Test
    public void testCreate() {
        WindowOver over = WindowOver.create();
        Assert.assertNotNull(over);
        Assert.assertTrue(over.isEmpty());
        System.out.println("testCreate: " + over.toSQL());
    }

    @Test
    public void testPartitionBy() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id");

        String sql = over.toSQL();
        Assert.assertEquals("OVER (PARTITION BY department_id)", sql);
        Assert.assertFalse(over.isEmpty());
        System.out.println("testPartitionBy: " + sql);
    }

    @Test
    public void testPartitionByMultipleFields() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .partitionBy("region_id");

        String sql = over.toSQL();
        System.out.println("testPartitionByMultipleFields: " + sql);
        Assert.assertEquals("OVER (PARTITION BY department_id,region_id)", sql);
    }

    @Test
    public void testPartitionByWithPrefix() {
        WindowOver over = WindowOver.create()
                .partitionBy("u.department_id");

        String sql = over.toSQL();
        System.out.println("testPartitionByWithPrefix: " + sql);
        Assert.assertEquals("OVER (PARTITION BY u.department_id)", sql);
    }

    @Test
    public void testPartitionByWithFields() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .partitionBy("region_id");

        String sql = over.toSQL();
        System.out.println("testPartitionByWithFields: " + sql);
        Assert.assertEquals("OVER (PARTITION BY department_id,region_id)", sql);
    }

    @Test
    public void testOrderByAsc() {
        WindowOver over = WindowOver.create()
                .orderByAsc("salary");

        String sql = over.toSQL();
        System.out.println("testOrderByAsc: " + sql);
        Assert.assertEquals("OVER (ORDER BY salary ASC)", sql);
    }

    @Test
    public void testOrderByDesc() {
        WindowOver over = WindowOver.create()
                .orderByDesc("salary");

        String sql = over.toSQL();
        System.out.println("testOrderByDesc: " + sql);
        Assert.assertEquals("OVER (ORDER BY salary DESC)", sql);
    }

    @Test
    public void testOrderByMultipleFields() {
        WindowOver over = WindowOver.create()
                .orderByAsc("department_id")
                .orderByDesc("salary");

        String sql = over.toSQL();
        System.out.println("testOrderByMultipleFields: " + sql);
        Assert.assertEquals("OVER (ORDER BY department_id ASC,salary DESC)", sql);
    }

    @Test
    public void testOrderByWithPrefix() {
        WindowOver over = WindowOver.create()
                .orderByAsc("u", "salary");

        String sql = over.toSQL();
        System.out.println("testOrderByWithPrefix: " + sql);
        Assert.assertEquals("OVER (ORDER BY u.salary ASC)", sql);
    }

    @Test
    public void testPartitionByAndOrderBy() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByDesc("salary");

        String sql = over.toSQL();
        System.out.println("testPartitionByAndOrderBy: " + sql);
        Assert.assertEquals("OVER (PARTITION BY department_id ORDER BY salary DESC)", sql);
    }

    @Test
    public void testRowsBetween() {
        WindowOver over = WindowOver.create()
                .rowsBetween("BETWEEN 1 PRECEDING AND 1 FOLLOWING");

        String sql = over.toSQL();
        System.out.println("testRowsBetween: " + sql);
        Assert.assertEquals("OVER (ROWS BETWEEN 1 PRECEDING AND 1 FOLLOWING)", sql);
    }

    @Test
    public void testRangeBetween() {
        WindowOver over = WindowOver.create()
                .rangeBetween("BETWEEN 10 PRECEDING AND CURRENT ROW");

        String sql = over.toSQL();
        System.out.println("testRangeBetween: " + sql);
        Assert.assertEquals("OVER (RANGE BETWEEN 10 PRECEDING AND CURRENT ROW)", sql);
    }

    @Test
    public void testGroupsBetween() {
        WindowOver over = WindowOver.create()
                .groupsBetween("BETWEEN 1 PRECEDING AND 1 FOLLOWING");

        String sql = over.toSQL();
        System.out.println("testGroupsBetween: " + sql);
        Assert.assertEquals("OVER (GROUPS BETWEEN 1 PRECEDING AND 1 FOLLOWING)", sql);
    }

    @Test
    public void testRowsUnboundedPreceding() {
        WindowOver over = WindowOver.create()
                .rowsUnboundedPreceding();

        String sql = over.toSQL();
        System.out.println("testRowsUnboundedPreceding: " + sql);
        Assert.assertEquals("OVER (ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW)", sql);
    }

    @Test
    public void testRowsUnboundedFollowing() {
        WindowOver over = WindowOver.create()
                .rowsUnboundedFollowing();

        String sql = over.toSQL();
        System.out.println("testRowsUnboundedFollowing: " + sql);
        Assert.assertEquals("OVER (ROWS BETWEEN CURRENT ROW AND UNBOUNDED FOLLOWING)", sql);
    }

    @Test
    public void testRowsBetweenUnbounded() {
        WindowOver over = WindowOver.create()
                .rowsBetweenUnbounded();

        String sql = over.toSQL();
        System.out.println("testRowsBetweenUnbounded: " + sql);
        Assert.assertEquals("OVER (ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)", sql);
    }

    @Test
    public void testRowsCurrentRow() {
        WindowOver over = WindowOver.create()
                .rowsCurrentRow();

        String sql = over.toSQL();
        System.out.println("testRowsCurrentRow: " + sql);
        Assert.assertEquals("OVER (ROWS BETWEEN CURRENT ROW AND CURRENT ROW)", sql);
    }

    @Test
    public void testRowsPreceding() {
        WindowOver over = WindowOver.create()
                .rowsPreceding(3);

        String sql = over.toSQL();
        System.out.println("testRowsPreceding: " + sql);
        Assert.assertEquals("OVER (ROWS BETWEEN 3 PRECEDING AND CURRENT ROW)", sql);
    }

    @Test
    public void testRowsFollowing() {
        WindowOver over = WindowOver.create()
                .rowsFollowing(2);

        String sql = over.toSQL();
        System.out.println("testRowsFollowing: " + sql);
        Assert.assertEquals("OVER (ROWS BETWEEN CURRENT ROW AND 2 FOLLOWING)", sql);
    }

    @Test
    public void testRowsBetweenWithParams() {
        WindowOver over = WindowOver.create()
                .rowsBetween(2, 3);

        String sql = over.toSQL();
        System.out.println("testRowsBetweenWithParams: " + sql);
        Assert.assertEquals("OVER (ROWS BETWEEN 2 PRECEDING AND 3 FOLLOWING)", sql);
    }

    @Test
    public void testComplexWindowOver() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .partitionBy("region_id")
                .orderByAsc("hire_date")
                .orderByDesc("salary")
                .rowsBetweenUnbounded();

        String sql = over.toSQL();
        System.out.println("testComplexWindowOver: " + sql);
        Assert.assertEquals("OVER (PARTITION BY department_id,region_id ORDER BY hire_date ASC,salary DESC ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)", sql);
    }

    @Test
    public void testEmptyWindowOver() {
        WindowOver over = WindowOver.create();

        String sql = over.toSQL();
        System.out.println("testEmptyWindowOver: " + sql);
        Assert.assertEquals("OVER ()", sql);
        Assert.assertTrue(over.isEmpty());
    }

    @Test
    public void testWindowOverWithOrderByObject() {
        OrderBy orderBy = OrderBy.create()
                .asc("department_id")
                .desc("salary");

        WindowOver over = WindowOver.create()
                .partitionBy("region_id")
                .orderBy(orderBy);

        String sql = over.toSQL();
        System.out.println("testWindowOverWithOrderByObject: " + sql);
        Assert.assertEquals("OVER (PARTITION BY region_id ORDER BY `department_id` ASC,`salary` DESC)", sql);
    }

    @Test
    public void testWindowOverToString() {
        WindowOver over = WindowOver.create()
                .partitionBy("department_id")
                .orderByAsc("salary");

        String sql = over.toString();
        System.out.println("testWindowOverToString: " + sql);
        Assert.assertEquals("OVER (PARTITION BY department_id ORDER BY salary ASC)", sql);
    }

    @Test
    public void testWindowOverParams() {
        WindowOver over = WindowOver.create()
                .param("param1")
                .param("param2");

        Assert.assertNotNull(over.params());
        Assert.assertFalse(over.params().isEmpty());
        System.out.println("testWindowOverParams: params = " + over.params());
    }
}
