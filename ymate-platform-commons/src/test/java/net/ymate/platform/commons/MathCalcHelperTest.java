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
package net.ymate.platform.commons;

import net.ymate.platform.commons.lang.BlurObject;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * MathCalcHelper测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-07 13:14:26
 * @since 2.1.4
 */
public class MathCalcHelperTest {

    @Test
    public void testEq() {
        // Test eq(double, double)
        Assert.assertTrue(MathCalcHelper.eq(1.0, 1.0));
        Assert.assertFalse(MathCalcHelper.eq(1.0, 2.0));

        // Test eq(float, float)
        Assert.assertTrue(MathCalcHelper.eq(1.0f, 1.0f));
        Assert.assertFalse(MathCalcHelper.eq(1.0f, 2.0f));

        // Test eq(BigDecimal, BigDecimal)
        BigDecimal a = new BigDecimal("1.0");
        BigDecimal b = new BigDecimal("1.0");
        BigDecimal c = new BigDecimal("2.0");
        Assert.assertTrue(MathCalcHelper.eq(a, b));
        Assert.assertFalse(MathCalcHelper.eq(a, c));
        Assert.assertFalse(MathCalcHelper.eq(a, null));
        Assert.assertFalse(MathCalcHelper.eq(null, b));
        Assert.assertTrue(MathCalcHelper.eq(null, null));
    }

    @Test
    public void testBindMethods() {
        // Test bind(double)
        MathCalcHelper helper1 = MathCalcHelper.bind(10.5);
        Assert.assertNotNull(helper1);

        // Test bind(String)
        MathCalcHelper helper2 = MathCalcHelper.bind("10.5");
        Assert.assertNotNull(helper2);

        // Test bind(BigDecimal)
        BigDecimal value = new BigDecimal("10.5");
        MathCalcHelper helper3 = MathCalcHelper.bind(value);
        Assert.assertNotNull(helper3);
    }

    @Test
    public void testScaleMethod() {
        MathCalcHelper helper = MathCalcHelper.bind(10.5);

        // Test scale with positive value
        MathCalcHelper result = helper.scale(2);
        Assert.assertSame(helper, result);

        // Test scale with zero
        result = helper.scale(0);
        Assert.assertSame(helper, result);

        // Test scale with negative value (should throw exception)
        try {
            helper.scale(-1);
            Assert.fail("Expected IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException e) {
            // Expected exception
        }
    }

    @Test
    public void testRoundingModeMethods() {
        MathCalcHelper helper = MathCalcHelper.bind(10.5);

        // Test roundUp
        MathCalcHelper result = helper.roundUp();
        Assert.assertSame(helper, result);

        // Test roundDown
        result = helper.roundDown();
        Assert.assertSame(helper, result);

        // Test roundCeiling
        result = helper.roundCeiling();
        Assert.assertSame(helper, result);

        // Test roundFloor
        result = helper.roundFloor();
        Assert.assertSame(helper, result);

        // Test roundHalfUp
        result = helper.roundHalfUp();
        Assert.assertSame(helper, result);

        // Test roundHalfDown
        result = helper.roundHalfDown();
        Assert.assertSame(helper, result);

        // Test roundHalfEven
        result = helper.roundHalfEven();
        Assert.assertSame(helper, result);

        // Test roundUnnecessary
        result = helper.roundUnnecessary();
        Assert.assertSame(helper, result);
    }

    @Test
    public void testAddMethods() {
        // Test add(double)
        MathCalcHelper helper1 = MathCalcHelper.bind(10.0);
        MathCalcHelper result1 = helper1.add(5.0);
        Assert.assertSame(helper1, result1);
        Assert.assertEquals(new BigDecimal("15.0"), helper1.value());

        // Test add(String)
        MathCalcHelper helper2 = MathCalcHelper.bind(10.0);
        MathCalcHelper result2 = helper2.add("5.0");
        Assert.assertSame(helper2, result2);
        Assert.assertEquals(new BigDecimal("15.0"), helper2.value());

        // Test add(BigDecimal)
        MathCalcHelper helper3 = MathCalcHelper.bind(10.0);
        MathCalcHelper result3 = helper3.add(new BigDecimal("5.0"));
        Assert.assertSame(helper3, result3);
        Assert.assertEquals(new BigDecimal("15.0"), helper3.value());
    }

    @Test
    public void testSubtractMethods() {
        // Test subtract(double)
        MathCalcHelper helper1 = MathCalcHelper.bind(10.0);
        MathCalcHelper result1 = helper1.subtract(5.0);
        Assert.assertSame(helper1, result1);
        Assert.assertEquals(new BigDecimal("5.0"), helper1.value());

        // Test subtract(String)
        MathCalcHelper helper2 = MathCalcHelper.bind(10.0);
        MathCalcHelper result2 = helper2.subtract("5.0");
        Assert.assertSame(helper2, result2);
        Assert.assertEquals(new BigDecimal("5.0"), helper2.value());

        // Test subtract(BigDecimal)
        MathCalcHelper helper3 = MathCalcHelper.bind(10.0);
        MathCalcHelper result3 = helper3.subtract(new BigDecimal("5.0"));
        Assert.assertSame(helper3, result3);
        Assert.assertEquals(new BigDecimal("5.0"), helper3.value());
    }

    @Test
    public void testMultiplyMethods() {
        // Test multiply(double)
        MathCalcHelper helper1 = MathCalcHelper.bind(10.0);
        MathCalcHelper result1 = helper1.multiply(5.0);
        Assert.assertSame(helper1, result1);
        Assert.assertEquals(new BigDecimal("50.00"), helper1.value());

        // Test multiply(String)
        MathCalcHelper helper2 = MathCalcHelper.bind(10.0);
        MathCalcHelper result2 = helper2.multiply("5.0");
        Assert.assertSame(helper2, result2);
        Assert.assertEquals(new BigDecimal("50.00"), helper2.value());

        // Test multiply(BigDecimal)
        MathCalcHelper helper3 = MathCalcHelper.bind(10.0);
        MathCalcHelper result3 = helper3.multiply(new BigDecimal("5.0"));
        Assert.assertSame(helper3, result3);
        Assert.assertEquals(new BigDecimal("50.00"), helper3.value());
    }

    @Test
    public void testDivideMethods() {
        // Test divide(double)
        MathCalcHelper helper1 = MathCalcHelper.bind(10.0);
        MathCalcHelper result1 = helper1.divide(3.0);
        Assert.assertSame(helper1, result1);

        // Test divide(String)
        MathCalcHelper helper2 = MathCalcHelper.bind(10.0);
        MathCalcHelper result2 = helper2.divide("3.0");
        Assert.assertSame(helper2, result2);

        // Test divide(BigDecimal)
        MathCalcHelper helper3 = MathCalcHelper.bind(10.0);
        MathCalcHelper result3 = helper3.divide(new BigDecimal("3.0"));
        Assert.assertSame(helper3, result3);
    }

    @Test
    public void testDivideWithScale() {
        // Test divide with custom scale
        MathCalcHelper helper = MathCalcHelper.bind(10.0)
                .scale(2)
                .roundHalfUp()
                .divide(3.0);
        Assert.assertEquals(new BigDecimal("3.33"), helper.value());
    }

    @Test
    public void testRoundMethod() {
        // Test round with custom scale
        MathCalcHelper helper = MathCalcHelper.bind(10.5678)
                .scale(2)
                .roundHalfUp()
                .round();
        Assert.assertEquals(new BigDecimal("10.57"), helper.value());
    }

    @Test
    public void testValueMethod() {
        // Test value() method
        MathCalcHelper helper = MathCalcHelper.bind(10.5);
        BigDecimal value = helper.value();
        Assert.assertNotNull(value);
        Assert.assertEquals(new BigDecimal("10.5"), value);
    }

    @Test
    public void testToBlurObject() {
        // Test toBlurObject() method
        MathCalcHelper helper = MathCalcHelper.bind(10.5);
        BlurObject blurObject = helper.toBlurObject();
        Assert.assertNotNull(blurObject);
        Assert.assertEquals(10.5, blurObject.toDoubleValue(), 0.0001);
    }

    @Test
    public void testChainedOperations() {
        // Test chained arithmetic operations
        MathCalcHelper helper = MathCalcHelper.bind(10.0)
                .add(5.0)
                .subtract(2.0)
                .multiply(3.0)
                .divide(2.0)
                .scale(2)
                .roundHalfUp()
                .round();

        Assert.assertEquals(new BigDecimal("19.50"), helper.value());
    }

    @Test
    public void testNegativeValues() {
        // Test operations with negative values
        MathCalcHelper helper = MathCalcHelper.bind(-10.0)
                .add(5.0)
                .subtract(-2.0)
                .multiply(-3.0)
                .scale(2)
                .roundHalfUp()
                .round();

        Assert.assertEquals(new BigDecimal("9.00"), helper.value());
    }

    @Test
    public void testZeroValues() {
        // Test operations with zero
        MathCalcHelper helper = MathCalcHelper.bind(10.0)
                .add(0.0)
                .subtract(10.0)
                .multiply(5.0)
                .scale(2)
                .roundHalfUp()
                .round();

        Assert.assertEquals(new BigDecimal("0.00"), helper.value());
    }
}
