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
 * Func Math 功能测试类 - 完整覆盖Func接口中Math相关的所有功能
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-27
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class FuncMathTest {

    @Entity
    public static class TestEntity implements IEntity<String> {

        @Id
        @Property
        private String id;

        @Property
        private Double value;

        @Property
        private Integer amount;

        @Property
        private String name;

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        public Integer getAmount() {
            return amount;
        }

        public void setAmount(Integer amount) {
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testABS() {
        IFunction func = Func.math.ABS("-10");
        String sql = func.build();
        System.out.println("testABS: " + sql);
        Assert.assertEquals("ABS(-10)", sql);
    }

    @Test
    public void testABSLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.ABS(column);
        String sql = func.build();
        System.out.println("testABSLambda: " + sql);
        Assert.assertEquals("ABS(value)", sql);
    }

    @Test
    public void testABSWithFunction() {
        IFunction innerFunc = Func.math.SQRT("100");
        IFunction func = Func.math.ABS(innerFunc);
        String sql = func.build();
        System.out.println("testABSWithFunction: " + sql);
        Assert.assertEquals("ABS(SQRT(100))", sql);
    }

    @Test
    public void testACOS() {
        IFunction func = Func.math.ACOS("0.5");
        String sql = func.build();
        System.out.println("testACOS: " + sql);
        Assert.assertEquals("ACOS(0.5)", sql);
    }

    @Test
    public void testACOSLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.ACOS(column);
        String sql = func.build();
        System.out.println("testACOSLambda: " + sql);
        Assert.assertEquals("ACOS(value)", sql);
    }

    @Test
    public void testACOSWithFunction() {
        IFunction innerFunc = Func.math.SIN("0.5");
        IFunction func = Func.math.ACOS(innerFunc);
        String sql = func.build();
        System.out.println("testACOSWithFunction: " + sql);
        Assert.assertEquals("ACOS(SIN(0.5))", sql);
    }

    @Test
    public void testASIN() {
        IFunction func = Func.math.ASIN("0.5");
        String sql = func.build();
        System.out.println("testASIN: " + sql);
        Assert.assertEquals("ASIN(0.5)", sql);
    }

    @Test
    public void testASINLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.ASIN(column);
        String sql = func.build();
        System.out.println("testASINLambda: " + sql);
        Assert.assertEquals("ASIN(value)", sql);
    }

    @Test
    public void testASINWithFunction() {
        IFunction innerFunc = Func.math.COS("0.5");
        IFunction func = Func.math.ASIN(innerFunc);
        String sql = func.build();
        System.out.println("testASINWithFunction: " + sql);
        Assert.assertEquals("ASIN(COS(0.5))", sql);
    }

    @Test
    public void testATAN() {
        IFunction func = Func.math.ATAN("1");
        String sql = func.build();
        System.out.println("testATAN: " + sql);
        Assert.assertEquals("ATAN(1)", sql);
    }

    @Test
    public void testATANLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.ATAN(column);
        String sql = func.build();
        System.out.println("testATANLambda: " + sql);
        Assert.assertEquals("ATAN(value)", sql);
    }

    @Test
    public void testATANWithFunction() {
        IFunction innerFunc = Func.math.TAN("0.5");
        IFunction func = Func.math.ATAN(innerFunc);
        String sql = func.build();
        System.out.println("testATANWithFunction: " + sql);
        Assert.assertEquals("ATAN(TAN(0.5))", sql);
    }

    @Test
    public void testATANTwoParams() {
        IFunction func = Func.math.ATAN("y", "x");
        String sql = func.build();
        System.out.println("testATANTwoParams: " + sql);
        Assert.assertEquals("ATAN(y, x)", sql);
    }

    @Test
    public void testATANTwoParamsLambda() {
        SFunction<TestEntity, Double> y = TestEntity::getValue;
        SFunction<TestEntity, Integer> x = TestEntity::getAmount;
        IFunction func = Func.math.ATAN(y, x);
        String sql = func.build();
        System.out.println("testATANTwoParamsLambda: " + sql);
        Assert.assertEquals("ATAN(value, amount)", sql);
    }

    @Test
    public void testATANTwoParamsMixed() {
        SFunction<TestEntity, Double> y = TestEntity::getValue;
        IFunction func = Func.math.ATAN(y, "x");
        String sql = func.build();
        System.out.println("testATANTwoParamsMixed: " + sql);
        Assert.assertEquals("ATAN(value, x)", sql);
    }

    @Test
    public void testATANTwoParamsWithFunction() {
        IFunction yFunc = Func.math.SIN("0.5");
        IFunction xFunc = Func.math.COS("0.5");
        IFunction func = Func.math.ATAN(yFunc, xFunc);
        String sql = func.build();
        System.out.println("testATANTwoParamsWithFunction: " + sql);
        Assert.assertEquals("ATAN(SIN(0.5), COS(0.5))", sql);
    }

    @Test
    public void testCEILING() {
        IFunction func = Func.math.CEILING("3.14");
        String sql = func.build();
        System.out.println("testCEILING: " + sql);
        Assert.assertEquals("CEILING(3.14)", sql);
    }

    @Test
    public void testCEILINGLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.CEILING(column);
        String sql = func.build();
        System.out.println("testCEILINGLambda: " + sql);
        Assert.assertEquals("CEILING(value)", sql);
    }

    @Test
    public void testCEILINGWithFunction() {
        IFunction innerFunc = Func.math.ABS("-3.14");
        IFunction func = Func.math.CEILING(innerFunc);
        String sql = func.build();
        System.out.println("testCEILINGWithFunction: " + sql);
        Assert.assertEquals("CEILING(ABS(-3.14))", sql);
    }

    @Test
    public void testCONV() {
        IFunction func = Func.math.CONV("A", 16, 2);
        String sql = func.build();
        System.out.println("testCONV: " + sql);
        Assert.assertEquals("CONV(A, 16, 2)", sql);
    }

    @Test
    public void testCONVLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.CONV(column, 10, 16);
        String sql = func.build();
        System.out.println("testCONVLambda: " + sql);
        Assert.assertEquals("CONV(value, 10, 16)", sql);
    }

    @Test
    public void testCONVWithFunction() {
        IFunction innerFunc = Func.math.ABS("100");
        IFunction func = Func.math.CONV(innerFunc, 10, 2);
        String sql = func.build();
        System.out.println("testCONVWithFunction: " + sql);
        Assert.assertEquals("CONV(ABS(100), 10, 2)", sql);
    }

    @Test
    public void testCOS() {
        IFunction func = Func.math.COS("0.5");
        String sql = func.build();
        System.out.println("testCOS: " + sql);
        Assert.assertEquals("COS(0.5)", sql);
    }

    @Test
    public void testCOSLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.COS(column);
        String sql = func.build();
        System.out.println("testCOSLambda: " + sql);
        Assert.assertEquals("COS(value)", sql);
    }

    @Test
    public void testCOSWithFunction() {
        IFunction innerFunc = Func.math.PI();
        IFunction func = Func.math.COS(innerFunc);
        String sql = func.build();
        System.out.println("testCOSWithFunction: " + sql);
        Assert.assertEquals("COS(PI())", sql);
    }

    @Test
    public void testCOT() {
        IFunction func = Func.math.COT("0.5");
        String sql = func.build();
        System.out.println("testCOT: " + sql);
        Assert.assertEquals("COT(0.5)", sql);
    }

    @Test
    public void testCOTLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.COT(column);
        String sql = func.build();
        System.out.println("testCOTLambda: " + sql);
        Assert.assertEquals("COT(value)", sql);
    }

    @Test
    public void testCOTWithFunction() {
        IFunction innerFunc = Func.math.PI();
        IFunction func = Func.math.COT(innerFunc);
        String sql = func.build();
        System.out.println("testCOTWithFunction: " + sql);
        Assert.assertEquals("COT(PI())", sql);
    }

    @Test
    public void testCRC32() {
        IFunction func = Func.math.CRC32("MySQL");
        String sql = func.build();
        System.out.println("testCRC: " + sql);
        Assert.assertEquals("CRC32(MySQL)", sql);
    }

    @Test
    public void testCRC32Lambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.math.CRC32(column);
        String sql = func.build();
        System.out.println("testCRC: " + sql);
        Assert.assertEquals("CRC32(name)", sql);
    }

    @Test
    public void testCRC32WithFunction() {
        IFunction innerFunc = Func.math.ABS("100");
        IFunction func = Func.math.CRC32(innerFunc);
        String sql = func.build();
        System.out.println("testCRC: " + sql);
        Assert.assertEquals("CRC32(ABS(100))", sql);
    }

    @Test
    public void testDEGREES() {
        IFunction func = Func.math.DEGREES("3.14159");
        String sql = func.build();
        System.out.println("testDEGREES: " + sql);
        Assert.assertEquals("DEGREES(3.14159)", sql);
    }

    @Test
    public void testDEGREESLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.DEGREES(column);
        String sql = func.build();
        System.out.println("testDEGREESLambda: " + sql);
        Assert.assertEquals("DEGREES(value)", sql);
    }

    @Test
    public void testDEGREESWithFunction() {
        IFunction innerFunc = Func.math.PI();
        IFunction func = Func.math.DEGREES(innerFunc);
        String sql = func.build();
        System.out.println("testDEGREESWithFunction: " + sql);
        Assert.assertEquals("DEGREES(PI())", sql);
    }

    @Test
    public void testEXP() {
        IFunction func = Func.math.EXP("2");
        String sql = func.build();
        System.out.println("testEXP: " + sql);
        Assert.assertEquals("EXP(2)", sql);
    }

    @Test
    public void testEXPLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.EXP(column);
        String sql = func.build();
        System.out.println("testEXPLambda: " + sql);
        Assert.assertEquals("EXP(value)", sql);
    }

    @Test
    public void testEXPWithFunction() {
        IFunction innerFunc = Func.math.ABS("2");
        IFunction func = Func.math.EXP(innerFunc);
        String sql = func.build();
        System.out.println("testEXPWithFunction: " + sql);
        Assert.assertEquals("EXP(ABS(2))", sql);
    }

    @Test
    public void testFLOOR() {
        IFunction func = Func.math.FLOOR("3.99");
        String sql = func.build();
        System.out.println("testFLOOR: " + sql);
        Assert.assertEquals("FLOOR(3.99)", sql);
    }

    @Test
    public void testFLOORLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.FLOOR(column);
        String sql = func.build();
        System.out.println("testFLOORLambda: " + sql);
        Assert.assertEquals("FLOOR(value)", sql);
    }

    @Test
    public void testFLOORWithFunction() {
        IFunction innerFunc = Func.math.ABS("-3.99");
        IFunction func = Func.math.FLOOR(innerFunc);
        String sql = func.build();
        System.out.println("testFLOORWithFunction: " + sql);
        Assert.assertEquals("FLOOR(ABS(-3.99))", sql);
    }

    @Test
    public void testLN() {
        IFunction func = Func.math.LN("10");
        String sql = func.build();
        System.out.println("testLN: " + sql);
        Assert.assertEquals("LN(10)", sql);
    }

    @Test
    public void testLNLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.LN(column);
        String sql = func.build();
        System.out.println("testLNLambda: " + sql);
        Assert.assertEquals("LN(value)", sql);
    }

    @Test
    public void testLNWithFunction() {
        IFunction innerFunc = Func.math.ABS("10");
        IFunction func = Func.math.LN(innerFunc);
        String sql = func.build();
        System.out.println("testLNWithFunction: " + sql);
        Assert.assertEquals("LN(ABS(10))", sql);
    }

    @Test
    public void testLOG() {
        IFunction func = Func.math.LOG("10");
        String sql = func.build();
        System.out.println("testLOG: " + sql);
        Assert.assertEquals("LOG(10)", sql);
    }

    @Test
    public void testLOGLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.LOG(column);
        String sql = func.build();
        System.out.println("testLOGLambda: " + sql);
        Assert.assertEquals("LOG(value)", sql);
    }

    @Test
    public void testLOGWithFunction() {
        IFunction innerFunc = Func.math.ABS("10");
        IFunction func = Func.math.LOG(innerFunc);
        String sql = func.build();
        System.out.println("testLOGWithFunction: " + sql);
        Assert.assertEquals("LOG(ABS(10))", sql);
    }

    @Test
    public void testLOGTwoParams() {
        IFunction func = Func.math.LOG("2", "8");
        String sql = func.build();
        System.out.println("testLOGTwoParams: " + sql);
        Assert.assertEquals("LOG(2, 8)", sql);
    }

    @Test
    public void testLOGTwoParamsLambda() {
        SFunction<TestEntity, Double> b = TestEntity::getValue;
        SFunction<TestEntity, Integer> x = TestEntity::getAmount;
        IFunction func = Func.math.LOG(b, x);
        String sql = func.build();
        System.out.println("testLOGTwoParamsLambda: " + sql);
        Assert.assertEquals("LOG(value, amount)", sql);
    }

    @Test
    public void testLOGTwoParamsMixed() {
        SFunction<TestEntity, Integer> x = TestEntity::getAmount;
        IFunction func = Func.math.LOG("2", x);
        String sql = func.build();
        System.out.println("testLOGTwoParamsMixed: " + sql);
        Assert.assertEquals("LOG(2, amount)", sql);
    }

    @Test
    public void testLOGTwoParamsWithFunction() {
        IFunction bFunc = Func.math.ABS("2");
        IFunction xFunc = Func.math.ABS("8");
        IFunction func = Func.math.LOG(bFunc, xFunc);
        String sql = func.build();
        System.out.println("testLOGTwoParamsWithFunction: " + sql);
        Assert.assertEquals("LOG(ABS(2), ABS(8))", sql);
    }

    @Test
    public void testLOG10() {
        IFunction func = Func.math.LOG10("100");
        String sql = func.build();
        System.out.println("testLOG: " + sql);
        Assert.assertEquals("LOG10(100)", sql);
    }

    @Test
    public void testLOG10Lambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.LOG10(column);
        String sql = func.build();
        System.out.println("testLOG: " + sql);
        Assert.assertEquals("LOG10(value)", sql);
    }

    @Test
    public void testLOG10WithFunction() {
        IFunction innerFunc = Func.math.ABS("100");
        IFunction func = Func.math.LOG10(innerFunc);
        String sql = func.build();
        System.out.println("testLOG: " + sql);
        Assert.assertEquals("LOG10(ABS(100))", sql);
    }

    @Test
    public void testLOG2() {
        IFunction func = Func.math.LOG2("8");
        String sql = func.build();
        System.out.println("testLOG: " + sql);
        Assert.assertEquals("LOG2(8)", sql);
    }

    @Test
    public void testLOG2Lambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.LOG2(column);
        String sql = func.build();
        System.out.println("testLOG: " + sql);
        Assert.assertEquals("LOG2(value)", sql);
    }

    @Test
    public void testLOG2WithFunction() {
        IFunction innerFunc = Func.math.ABS("8");
        IFunction func = Func.math.LOG2(innerFunc);
        String sql = func.build();
        System.out.println("testLOG: " + sql);
        Assert.assertEquals("LOG2(ABS(8))", sql);
    }

    @Test
    public void testMOD() {
        IFunction func = Func.math.MOD("10", "3");
        String sql = func.build();
        System.out.println("testMOD: " + sql);
        Assert.assertEquals("MOD(10, 3)", sql);
    }

    @Test
    public void testMODLambda() {
        SFunction<TestEntity, Integer> n = TestEntity::getAmount;
        IFunction func = Func.math.MOD(n, "3");
        String sql = func.build();
        System.out.println("testMODLambda: " + sql);
        Assert.assertEquals("MOD(amount, 3)", sql);
    }

    @Test
    public void testMODTwoParamsLambda() {
        SFunction<TestEntity, Double> n = TestEntity::getValue;
        SFunction<TestEntity, Integer> m = TestEntity::getAmount;
        IFunction func = Func.math.MOD(n, m);
        String sql = func.build();
        System.out.println("testMODTwoParamsLambda: " + sql);
        Assert.assertEquals("MOD(value, amount)", sql);
    }

    @Test
    public void testMODWithFunction() {
        IFunction nFunc = Func.math.ABS("10");
        IFunction mFunc = Func.math.ABS("3");
        IFunction func = Func.math.MOD(nFunc, mFunc);
        String sql = func.build();
        System.out.println("testMODWithFunction: " + sql);
        Assert.assertEquals("MOD(ABS(10), ABS(3))", sql);
    }

    @Test
    public void testPI() {
        IFunction func = Func.math.PI();
        String sql = func.build();
        System.out.println("testPI: " + sql);
        Assert.assertEquals("PI()", sql);
    }

    @Test
    public void testPOW() {
        IFunction func = Func.math.POW("2", "8");
        String sql = func.build();
        System.out.println("testPOW: " + sql);
        Assert.assertEquals("POW(2, 8)", sql);
    }

    @Test
    public void testPOWLambda() {
        SFunction<TestEntity, Double> y = TestEntity::getValue;
        IFunction func = Func.math.POW(y, "8");
        String sql = func.build();
        System.out.println("testPOWLambda: " + sql);
        Assert.assertEquals("POW(value, 8)", sql);
    }

    @Test
    public void testPOWTwoParamsLambda() {
        SFunction<TestEntity, Double> y = TestEntity::getValue;
        SFunction<TestEntity, Integer> x = TestEntity::getAmount;
        IFunction func = Func.math.POW(y, x);
        String sql = func.build();
        System.out.println("testPOWTwoParamsLambda: " + sql);
        Assert.assertEquals("POW(value, amount)", sql);
    }

    @Test
    public void testPOWWithFunction() {
        IFunction yFunc = Func.math.ABS("2");
        IFunction xFunc = Func.math.ABS("8");
        IFunction func = Func.math.POW(yFunc, xFunc);
        String sql = func.build();
        System.out.println("testPOWWithFunction: " + sql);
        Assert.assertEquals("POW(ABS(2), ABS(8))", sql);
    }

    @Test
    public void testPOWER() {
        IFunction func = Func.math.POWER("2", "8");
        String sql = func.build();
        System.out.println("testPOWER: " + sql);
        Assert.assertEquals("POWER(2, 8)", sql);
    }

    @Test
    public void testPOWERLambda() {
        SFunction<TestEntity, Double> y = TestEntity::getValue;
        IFunction func = Func.math.POWER(y, "8");
        String sql = func.build();
        System.out.println("testPOWERLambda: " + sql);
        Assert.assertEquals("POWER(value, 8)", sql);
    }

    @Test
    public void testPOWERTwoParamsLambda() {
        SFunction<TestEntity, Double> y = TestEntity::getValue;
        SFunction<TestEntity, Integer> x = TestEntity::getAmount;
        IFunction func = Func.math.POWER(y, x);
        String sql = func.build();
        System.out.println("testPOWERTwoParamsLambda: " + sql);
        Assert.assertEquals("POWER(value, amount)", sql);
    }

    @Test
    public void testPOWERWithFunction() {
        IFunction yFunc = Func.math.ABS("2");
        IFunction xFunc = Func.math.ABS("8");
        IFunction func = Func.math.POWER(yFunc, xFunc);
        String sql = func.build();
        System.out.println("testPOWERWithFunction: " + sql);
        Assert.assertEquals("POWER(ABS(2), ABS(8))", sql);
    }

    @Test
    public void testRADIANS() {
        IFunction func = Func.math.RADIANS("180");
        String sql = func.build();
        System.out.println("testRADIANS: " + sql);
        Assert.assertEquals("RADIANS(180)", sql);
    }

    @Test
    public void testRADIANSLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.RADIANS(column);
        String sql = func.build();
        System.out.println("testRADIANSLambda: " + sql);
        Assert.assertEquals("RADIANS(value)", sql);
    }

    @Test
    public void testRADIANSWithFunction() {
        IFunction innerFunc = Func.math.ABS("180");
        IFunction func = Func.math.RADIANS(innerFunc);
        String sql = func.build();
        System.out.println("testRADIANSWithFunction: " + sql);
        Assert.assertEquals("RADIANS(ABS(180))", sql);
    }

    @Test
    public void testRAND() {
        IFunction func = Func.math.RAND();
        String sql = func.build();
        System.out.println("testRAND: " + sql);
        Assert.assertEquals("RAND()", sql);
    }

    @Test
    public void testRANDWithParam() {
        IFunction func = Func.math.RAND("10");
        String sql = func.build();
        System.out.println("testRANDWithParam: " + sql);
        Assert.assertEquals("RAND(10)", sql);
    }

    @Test
    public void testRANDLambda() {
        SFunction<TestEntity, Integer> column = TestEntity::getAmount;
        IFunction func = Func.math.RAND(column);
        String sql = func.build();
        System.out.println("testRANDLambda: " + sql);
        Assert.assertEquals("RAND(amount)", sql);
    }

    @Test
    public void testRANDWithFunction() {
        IFunction innerFunc = Func.math.ABS("10");
        IFunction func = Func.math.RAND(innerFunc);
        String sql = func.build();
        System.out.println("testRANDWithFunction: " + sql);
        Assert.assertEquals("RAND(ABS(10))", sql);
    }

    @Test
    public void testROUND() {
        IFunction func = Func.math.ROUND("3.14159");
        String sql = func.build();
        System.out.println("testROUND: " + sql);
        Assert.assertEquals("ROUND(3.14159)", sql);
    }

    @Test
    public void testROUNDLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.ROUND(column);
        String sql = func.build();
        System.out.println("testROUNDLambda: " + sql);
        Assert.assertEquals("ROUND(value)", sql);
    }

    @Test
    public void testROUNDWithPrefixLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.ROUND("t", column);
        String sql = func.build();
        System.out.println("testROUNDWithPrefixLambda: " + sql);
        Assert.assertEquals("ROUND(t.value)", sql);
    }

    @Test
    public void testROUNDWithFunction() {
        IFunction innerFunc = Func.math.PI();
        IFunction func = Func.math.ROUND(innerFunc);
        String sql = func.build();
        System.out.println("testROUNDWithFunction: " + sql);
        Assert.assertEquals("ROUND(PI())", sql);
    }

    @Test
    public void testROUNDWithDecimals() {
        IFunction func = Func.math.ROUND("3.14159", 2);
        String sql = func.build();
        System.out.println("testROUNDWithDecimals: " + sql);
        Assert.assertEquals("ROUND(3.14159, 2)", sql);
    }

    @Test
    public void testROUNDLambdaWithDecimals() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.ROUND(column, 2);
        String sql = func.build();
        System.out.println("testROUNDLambdaWithDecimals: " + sql);
        Assert.assertEquals("ROUND(value, 2)", sql);
    }

    @Test
    public void testROUNDWithPrefixLambdaWithDecimals() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.ROUND("t", column, 2);
        String sql = func.build();
        System.out.println("testROUNDWithPrefixLambdaWithDecimals: " + sql);
        Assert.assertEquals("ROUND(t.value, 2)", sql);
    }

    @Test
    public void testROUNDWithFunctionWithDecimals() {
        IFunction innerFunc = Func.math.PI();
        IFunction func = Func.math.ROUND(innerFunc, 2);
        String sql = func.build();
        System.out.println("testROUNDWithFunctionWithDecimals: " + sql);
        Assert.assertEquals("ROUND(PI(), 2)", sql);
    }

    @Test
    public void testSIGN() {
        IFunction func = Func.math.SIGN("-10");
        String sql = func.build();
        System.out.println("testSIGN: " + sql);
        Assert.assertEquals("SIGN(-10)", sql);
    }

    @Test
    public void testSIGNLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.SIGN(column);
        String sql = func.build();
        System.out.println("testSIGNLambda: " + sql);
        Assert.assertEquals("SIGN(value)", sql);
    }

    @Test
    public void testSIGNWithPrefixLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.SIGN("t", column);
        String sql = func.build();
        System.out.println("testSIGNWithPrefixLambda: " + sql);
        Assert.assertEquals("SIGN(t.value)", sql);
    }

    @Test
    public void testSIGNWithFunction() {
        IFunction innerFunc = Func.math.ABS("-10");
        IFunction func = Func.math.SIGN(innerFunc);
        String sql = func.build();
        System.out.println("testSIGNWithFunction: " + sql);
        Assert.assertEquals("SIGN(ABS(-10))", sql);
    }

    @Test
    public void testSIN() {
        IFunction func = Func.math.SIN("0.5");
        String sql = func.build();
        System.out.println("testSIN: " + sql);
        Assert.assertEquals("SIN(0.5)", sql);
    }

    @Test
    public void testSINLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.SIN(column);
        String sql = func.build();
        System.out.println("testSINLambda: " + sql);
        Assert.assertEquals("SIN(value)", sql);
    }

    @Test
    public void testSINWithPrefixLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.SIN("t", column);
        String sql = func.build();
        System.out.println("testSINWithPrefixLambda: " + sql);
        Assert.assertEquals("SIN(t.value)", sql);
    }

    @Test
    public void testSINWithFunction() {
        IFunction innerFunc = Func.math.PI();
        IFunction func = Func.math.SIN(innerFunc);
        String sql = func.build();
        System.out.println("testSINWithFunction: " + sql);
        Assert.assertEquals("SIN(PI())", sql);
    }

    @Test
    public void testSQRT() {
        IFunction func = Func.math.SQRT("16");
        String sql = func.build();
        System.out.println("testSQRT: " + sql);
        Assert.assertEquals("SQRT(16)", sql);
    }

    @Test
    public void testSQRTLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.SQRT(column);
        String sql = func.build();
        System.out.println("testSQRTLambda: " + sql);
        Assert.assertEquals("SQRT(value)", sql);
    }

    @Test
    public void testSQRTWithPrefixLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.SQRT("t", column);
        String sql = func.build();
        System.out.println("testSQRTWithPrefixLambda: " + sql);
        Assert.assertEquals("SQRT(t.value)", sql);
    }

    @Test
    public void testSQRTWithFunction() {
        IFunction innerFunc = Func.math.ABS("16");
        IFunction func = Func.math.SQRT(innerFunc);
        String sql = func.build();
        System.out.println("testSQRTWithFunction: " + sql);
        Assert.assertEquals("SQRT(ABS(16))", sql);
    }

    @Test
    public void testTAN() {
        IFunction func = Func.math.TAN("0.5");
        String sql = func.build();
        System.out.println("testTAN: " + sql);
        Assert.assertEquals("TAN(0.5)", sql);
    }

    @Test
    public void testTANLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.TAN(column);
        String sql = func.build();
        System.out.println("testTANLambda: " + sql);
        Assert.assertEquals("TAN(value)", sql);
    }

    @Test
    public void testTANWithPrefixLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.TAN("t", column);
        String sql = func.build();
        System.out.println("testTANWithPrefixLambda: " + sql);
        Assert.assertEquals("TAN(t.value)", sql);
    }

    @Test
    public void testTANWithFunction() {
        IFunction innerFunc = Func.math.PI();
        IFunction func = Func.math.TAN(innerFunc);
        String sql = func.build();
        System.out.println("testTANWithFunction: " + sql);
        Assert.assertEquals("TAN(PI())", sql);
    }

    @Test
    public void testTRUNCATE() {
        IFunction func = Func.math.TRUNCATE("3.14159");
        String sql = func.build();
        System.out.println("testTRUNCATE: " + sql);
        Assert.assertEquals("TRUNCATE(3.14159)", sql);
    }

    @Test
    public void testTRUNCATELambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.TRUNCATE(column);
        String sql = func.build();
        System.out.println("testTRUNCATELambda: " + sql);
        Assert.assertEquals("TRUNCATE(value)", sql);
    }

    @Test
    public void testTRUNCATEWithPrefixLambda() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.TRUNCATE("t", column);
        String sql = func.build();
        System.out.println("testTRUNCATEWithPrefixLambda: " + sql);
        Assert.assertEquals("TRUNCATE(t.value)", sql);
    }

    @Test
    public void testTRUNCATEWithFunction() {
        IFunction innerFunc = Func.math.PI();
        IFunction func = Func.math.TRUNCATE(innerFunc);
        String sql = func.build();
        System.out.println("testTRUNCATEWithFunction: " + sql);
        Assert.assertEquals("TRUNCATE(PI())", sql);
    }

    @Test
    public void testTRUNCATEWithDecimals() {
        IFunction func = Func.math.TRUNCATE("3.14159", 2);
        String sql = func.build();
        System.out.println("testTRUNCATEWithDecimals: " + sql);
        Assert.assertEquals("TRUNCATE(3.14159, 2)", sql);
    }

    @Test
    public void testTRUNCATELambdaWithDecimals() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.TRUNCATE(column, 2);
        String sql = func.build();
        System.out.println("testTRUNCATELambdaWithDecimals: " + sql);
        Assert.assertEquals("TRUNCATE(value, 2)", sql);
    }

    @Test
    public void testTRUNCATEWithPrefixLambdaWithDecimals() {
        SFunction<TestEntity, Double> column = TestEntity::getValue;
        IFunction func = Func.math.TRUNCATE("t", column, 2);
        String sql = func.build();
        System.out.println("testTRUNCATEWithPrefixLambdaWithDecimals: " + sql);
        Assert.assertEquals("TRUNCATE(t.value, 2)", sql);
    }

    @Test
    public void testTRUNCATEWithFunctionWithDecimals() {
        IFunction innerFunc = Func.math.PI();
        IFunction func = Func.math.TRUNCATE(innerFunc, 2);
        String sql = func.build();
        System.out.println("testTRUNCATEWithFunctionWithDecimals: " + sql);
        Assert.assertEquals("TRUNCATE(PI(), 2)", sql);
    }

    @Test
    public void testComplexMathFunction() {
        IFunction func = Func.math.ROUND(Func.math.SQRT(Func.math.POW("2", "10")), 2);
        String sql = func.build();
        System.out.println("testComplexMathFunction: " + sql);
        Assert.assertEquals("ROUND(SQRT(POW(2, 10)), 2)", sql);
    }
}
