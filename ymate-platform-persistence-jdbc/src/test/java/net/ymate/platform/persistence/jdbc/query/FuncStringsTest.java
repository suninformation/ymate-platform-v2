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
 * Func Strings 功能测试类 - 完整覆盖Func接口中Strings相关的所有功能
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-27
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class FuncStringsTest {

    @Entity
    public static class TestEntity implements IEntity<String> {

        @Id
        @Property
        private String id;

        @Property
        private String name;

        @Property
        private String content;

        @Property
        private Integer length;

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

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public Integer getLength() {
            return length;
        }

        public void setLength(Integer length) {
            this.length = length;
        }
    }

    @Test
    public void testASCII() {
        IFunction func = Func.strings.ASCII("A");
        String sql = func.build();
        System.out.println("testASCII: " + sql);
        Assert.assertEquals("ASCII(A)", sql);
    }

    @Test
    public void testASCIILambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.ASCII(column);
        String sql = func.build();
        System.out.println("testASCIILambda: " + sql);
        Assert.assertEquals("ASCII(name)", sql);
    }

    @Test
    public void testASCIILambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.ASCII("t", column);
        String sql = func.build();
        System.out.println("testASCIILambdaWithPrefix: " + sql);
        Assert.assertEquals("ASCII(t.name)", sql);
    }

    @Test
    public void testASCIIWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("A");
        IFunction func = Func.strings.ASCII(innerFunc);
        String sql = func.build();
        System.out.println("testASCIIWithFunction: " + sql);
        Assert.assertEquals("ASCII(LOWER(A))", sql);
    }

    @Test
    public void testBIN() {
        IFunction func = Func.strings.BIN("10");
        String sql = func.build();
        System.out.println("testBIN: " + sql);
        Assert.assertEquals("BIN(10)", sql);
    }

    @Test
    public void testBINLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.BIN(column);
        String sql = func.build();
        System.out.println("testBINLambda: " + sql);
        Assert.assertEquals("BIN(name)", sql);
    }

    @Test
    public void testBINLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.BIN("t", column);
        String sql = func.build();
        System.out.println("testBINLambdaWithPrefix: " + sql);
        Assert.assertEquals("BIN(t.name)", sql);
    }

    @Test
    public void testBINWithFunction() {
        IFunction innerFunc = Func.strings.ASCII("A");
        IFunction func = Func.strings.BIN(innerFunc);
        String sql = func.build();
        System.out.println("testBINWithFunction: " + sql);
        Assert.assertEquals("BIN(ASCII(A))", sql);
    }

    @Test
    public void testBIT_LENGTH() {
        IFunction func = Func.strings.BIT_LENGTH("test");
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_LENGTH(test)", sql);
    }

    @Test
    public void testBIT_LENGTHLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.BIT_LENGTH(column);
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_LENGTH(name)", sql);
    }

    @Test
    public void testBIT_LENGTHLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.BIT_LENGTH("t", column);
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_LENGTH(t.name)", sql);
    }

    @Test
    public void testBIT_LENGTHWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("test");
        IFunction func = Func.strings.BIT_LENGTH(innerFunc);
        String sql = func.build();
        System.out.println("testBIT: " + sql);
        Assert.assertEquals("BIT_LENGTH(LOWER(test))", sql);
    }

    @Test
    public void testCHAR() {
        IFunction func = Func.strings.CHAR("77", "121", "83", "81", "76");
        String sql = func.build();
        System.out.println("testCHAR: " + sql);
        Assert.assertEquals("CHAR(77, 121, 83, 81, 76)", sql);
    }

    @Test
    public void testCHARWithFunction() {
        IFunction func1 = Func.strings.ASCII("M");
        IFunction func2 = Func.strings.ASCII("y");
        IFunction func = Func.strings.CHAR(func1, func2);
        String sql = func.build();
        System.out.println("testCHARWithFunction: " + sql);
        Assert.assertEquals("CHAR(ASCII(M), ASCII(y))", sql);
    }

    @Test
    public void testCHAR_LENGTH() {
        IFunction func = Func.strings.CHAR_LENGTH("test");
        String sql = func.build();
        System.out.println("testCHAR: " + sql);
        Assert.assertEquals("CHAR_LENGTH(test)", sql);
    }

    @Test
    public void testCHAR_LENGTHLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.CHAR_LENGTH(column);
        String sql = func.build();
        System.out.println("testCHAR: " + sql);
        Assert.assertEquals("CHAR_LENGTH(name)", sql);
    }

    @Test
    public void testCHAR_LENGTHLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.CHAR_LENGTH("t", column);
        String sql = func.build();
        System.out.println("testCHAR: " + sql);
        Assert.assertEquals("CHAR_LENGTH(t.name)", sql);
    }

    @Test
    public void testCHAR_LENGTHWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("test");
        IFunction func = Func.strings.CHAR_LENGTH(innerFunc);
        String sql = func.build();
        System.out.println("testCHAR: " + sql);
        Assert.assertEquals("CHAR_LENGTH(LOWER(test))", sql);
    }

    @Test
    public void testCHARACTER_LENGTH() {
        IFunction func = Func.strings.CHARACTER_LENGTH("test");
        String sql = func.build();
        System.out.println("testCHARACTER: " + sql);
        Assert.assertEquals("CHARACTER_LENGTH(test)", sql);
    }

    @Test
    public void testCHARACTER_LENGTHLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.CHARACTER_LENGTH(column);
        String sql = func.build();
        System.out.println("testCHARACTER: " + sql);
        Assert.assertEquals("CHARACTER_LENGTH(name)", sql);
    }

    @Test
    public void testCHARACTER_LENGTHLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.CHARACTER_LENGTH("t", column);
        String sql = func.build();
        System.out.println("testCHARACTER: " + sql);
        Assert.assertEquals("CHARACTER_LENGTH(t.name)", sql);
    }

    @Test
    public void testCHARACTER_LENGTHWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("test");
        IFunction func = Func.strings.CHARACTER_LENGTH(innerFunc);
        String sql = func.build();
        System.out.println("testCHARACTER: " + sql);
        Assert.assertEquals("CHARACTER_LENGTH(LOWER(test))", sql);
    }

    @Test
    public void testCONCAT() {
        IFunction func = Func.strings.CONCAT("Hello", " ", "World");
        String sql = func.build();
        System.out.println("testCONCAT: " + sql);
        Assert.assertEquals("CONCAT(Hello,  , World)", sql);
    }

    @Test
    public void testCONCATWithFunction() {
        IFunction func1 = Func.strings.LOWER("Hello");
        IFunction func2 = Func.strings.UPPER("World");
        IFunction func = Func.strings.CONCAT(func1, func2);
        String sql = func.build();
        System.out.println("testCONCATWithFunction: " + sql);
        Assert.assertEquals("CONCAT(LOWER(Hello), UPPER(World))", sql);
    }

    @Test
    public void testCONCAT_WS() {
        IFunction func = Func.strings.CONCAT_WS(",", "a", "b", "c");
        String sql = func.build();
        System.out.println("testCONCAT: " + sql);
        Assert.assertEquals("CONCAT_WS(,, a, b, c)", sql);
    }

    @Test
    public void testCONCAT_WSWithFunction() {
        IFunction func1 = Func.strings.LOWER("a");
        IFunction func2 = Func.strings.UPPER("b");
        IFunction func = Func.strings.CONCAT_WS(",", func1, func2);
        String sql = func.build();
        System.out.println("testCONCAT: " + sql);
        Assert.assertEquals("CONCAT_WS(,, LOWER(a), UPPER(b))", sql);
    }

    @Test
    public void testELT() {
        IFunction func = Func.strings.ELT("2", "a", "b", "c");
        String sql = func.build();
        System.out.println("testELT: " + sql);
        Assert.assertEquals("ELT(2, a, b, c)", sql);
    }

    @Test
    public void testELTWithFunction() {
        IFunction nFunc = Func.strings.ASCII("A");
        IFunction func1 = Func.strings.LOWER("a");
        IFunction func2 = Func.strings.UPPER("b");
        IFunction func = Func.strings.ELT(nFunc, func1, func2);
        String sql = func.build();
        System.out.println("testELTWithFunction: " + sql);
        Assert.assertEquals("ELT(ASCII(A), LOWER(a), UPPER(b))", sql);
    }

    @Test
    public void testFIELD() {
        IFunction func = Func.strings.FIELD("b", "a", "b", "c");
        String sql = func.build();
        System.out.println("testFIELD: " + sql);
        Assert.assertEquals("FIELD(b, a, b, c)", sql);
    }

    @Test
    public void testFIELDWithFunction() {
        IFunction strFunc = Func.strings.LOWER("b");
        IFunction func1 = Func.strings.UPPER("a");
        IFunction func2 = Func.strings.UPPER("b");
        IFunction func = Func.strings.FIELD(strFunc, func1, func2);
        String sql = func.build();
        System.out.println("testFIELDWithFunction: " + sql);
        Assert.assertEquals("FIELD(LOWER(b), UPPER(a), UPPER(b))", sql);
    }

    @Test
    public void testFIND_IN_SET() {
        IFunction func = Func.strings.FIND_IN_SET("b", "a,b,c");
        String sql = func.build();
        System.out.println("testFIND: " + sql);
        Assert.assertEquals("FIND_IN_SET(b, a,b,c)", sql);
    }

    @Test
    public void testFIND_IN_SETWithFunction() {
        IFunction xFunc = Func.strings.LOWER("b");
        IFunction listFunc = Func.strings.UPPER("a,b,c");
        IFunction func = Func.strings.FIND_IN_SET(xFunc, listFunc);
        String sql = func.build();
        System.out.println("testFIND: " + sql);
        Assert.assertEquals("FIND_IN_SET(LOWER(b), UPPER(a,b,c))", sql);
    }

    @Test
    public void testFORMAT() {
        IFunction func = Func.strings.FORMAT("12345.6789", 2);
        String sql = func.build();
        System.out.println("testFORMAT: " + sql);
        Assert.assertEquals("FORMAT(12345.6789, 2)", sql);
    }

    @Test
    public void testFORMATWithFunction() {
        IFunction innerFunc = Func.math.ABS("12345.6789");
        IFunction func = Func.strings.FORMAT(innerFunc, 2);
        String sql = func.build();
        System.out.println("testFORMATWithFunction: " + sql);
        Assert.assertEquals("FORMAT(ABS(12345.6789), 2)", sql);
    }

    @Test
    public void testFORMATWithLocale() {
        IFunction func = Func.strings.FORMAT("12345.6789", 2, "de_DE");
        String sql = func.build();
        System.out.println("testFORMATWithLocale: " + sql);
        Assert.assertEquals("FORMAT(12345.6789, 2, de_DE)", sql);
    }

    @Test
    public void testHEX() {
        IFunction func = Func.strings.HEX("abc");
        String sql = func.build();
        System.out.println("testHEX: " + sql);
        Assert.assertEquals("HEX(abc)", sql);
    }

    @Test
    public void testHEXWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("ABC");
        IFunction func = Func.strings.HEX(innerFunc);
        String sql = func.build();
        System.out.println("testHEXWithFunction: " + sql);
        Assert.assertEquals("HEX(LOWER(ABC))", sql);
    }

    @Test
    public void testFROM_BASE64() {
        IFunction func = Func.strings.FROM_BASE64("SGVsbG8=");
        String sql = func.build();
        System.out.println("testFROM: " + sql);
        Assert.assertEquals("FROM_BASE64(SGVsbG8=)", sql);
    }

    @Test
    public void testFROM_BASE64WithFunction() {
        IFunction innerFunc = Func.strings.HEX("test");
        IFunction func = Func.strings.FROM_BASE64(innerFunc);
        String sql = func.build();
        System.out.println("testFROM: " + sql);
        Assert.assertEquals("FROM_BASE64(HEX(test))", sql);
    }

    @Test
    public void testTO_BASE64() {
        IFunction func = Func.strings.TO_BASE64("Hello");
        String sql = func.build();
        System.out.println("testTO: " + sql);
        Assert.assertEquals("TO_BASE64(Hello)", sql);
    }

    @Test
    public void testTO_BASE64WithFunction() {
        IFunction innerFunc = Func.strings.LOWER("Hello");
        IFunction func = Func.strings.TO_BASE64(innerFunc);
        String sql = func.build();
        System.out.println("testTO: " + sql);
        Assert.assertEquals("TO_BASE64(LOWER(Hello))", sql);
    }

    @Test
    public void testINSERT() {
        IFunction func = Func.strings.INSERT("Quadratic", 3, 4, "What");
        String sql = func.build();
        System.out.println("testINSERT: " + sql);
        Assert.assertEquals("INSERT(Quadratic, 3, 4, What)", sql);
    }

    @Test
    public void testINSERTWithFunction() {
        IFunction strFunc = Func.strings.LOWER("Quadratic");
        IFunction newstrFunc = Func.strings.UPPER("What");
        IFunction func = Func.strings.INSERT(strFunc, 3, 4, newstrFunc);
        String sql = func.build();
        System.out.println("testINSERTWithFunction: " + sql);
        Assert.assertEquals("INSERT(LOWER(Quadratic), 3, 4, UPPER(What))", sql);
    }

    @Test
    public void testINSTR() {
        IFunction func = Func.strings.INSTR("foobarbar", "bar");
        String sql = func.build();
        System.out.println("testINSTR: " + sql);
        Assert.assertEquals("INSTR(foobarbar, bar)", sql);
    }

    @Test
    public void testINSTRWithFunction() {
        IFunction strFunc = Func.strings.LOWER("foobarbar");
        IFunction substrFunc = Func.strings.UPPER("bar");
        IFunction func = Func.strings.INSTR(strFunc, substrFunc);
        String sql = func.build();
        System.out.println("testINSTRWithFunction: " + sql);
        Assert.assertEquals("INSTR(LOWER(foobarbar), UPPER(bar))", sql);
    }

    @Test
    public void testLEFT() {
        IFunction func = Func.strings.LEFT("foobarbar", 5);
        String sql = func.build();
        System.out.println("testLEFT: " + sql);
        Assert.assertEquals("LEFT(foobarbar, 5)", sql);
    }

    @Test
    public void testLEFTWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("foobarbar");
        IFunction func = Func.strings.LEFT(innerFunc, 5);
        String sql = func.build();
        System.out.println("testLEFTWithFunction: " + sql);
        Assert.assertEquals("LEFT(LOWER(foobarbar), 5)", sql);
    }

    @Test
    public void testLENGTH() {
        IFunction func = Func.strings.LENGTH("test");
        String sql = func.build();
        System.out.println("testLENGTH: " + sql);
        Assert.assertEquals("LENGTH(test)", sql);
    }

    @Test
    public void testLENGTHLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.LENGTH(column);
        String sql = func.build();
        System.out.println("testLENGTHLambda: " + sql);
        Assert.assertEquals("LENGTH(name)", sql);
    }

    @Test
    public void testLENGTHLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.LENGTH("t", column);
        String sql = func.build();
        System.out.println("testLENGTHLambdaWithPrefix: " + sql);
        Assert.assertEquals("LENGTH(t.name)", sql);
    }

    @Test
    public void testLENGTHWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("test");
        IFunction func = Func.strings.LENGTH(innerFunc);
        String sql = func.build();
        System.out.println("testLENGTHWithFunction: " + sql);
        Assert.assertEquals("LENGTH(LOWER(test))", sql);
    }

    @Test
    public void testLOAD_FILE() {
        IFunction func = Func.strings.LOAD_FILE("/tmp/test.txt");
        String sql = func.build();
        System.out.println("testLOAD: " + sql);
        Assert.assertEquals("LOAD_FILE(/tmp/test.txt)", sql);
    }

    @Test
    public void testLOAD_FILELambda() {
        SFunction<TestEntity, String> column = TestEntity::getContent;
        IFunction func = Func.strings.LOAD_FILE(column);
        String sql = func.build();
        System.out.println("testLOAD: " + sql);
        Assert.assertEquals("LOAD_FILE(content)", sql);
    }

    @Test
    public void testLOAD_FILELambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getContent;
        IFunction func = Func.strings.LOAD_FILE("t", column);
        String sql = func.build();
        System.out.println("testLOAD: " + sql);
        Assert.assertEquals("LOAD_FILE(t.content)", sql);
    }

    @Test
    public void testLOAD_FILEWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("/tmp/test.txt");
        IFunction func = Func.strings.LOAD_FILE(innerFunc);
        String sql = func.build();
        System.out.println("testLOAD: " + sql);
        Assert.assertEquals("LOAD_FILE(LOWER(/tmp/test.txt))", sql);
    }

    @Test
    public void testLOCATE() {
        IFunction func = Func.strings.LOCATE("bar", "foobarbar");
        String sql = func.build();
        System.out.println("testLOCATE: " + sql);
        Assert.assertEquals("LOCATE(bar, foobarbar)", sql);
    }

    @Test
    public void testLOCATEWithFunction() {
        IFunction substrFunc = Func.strings.LOWER("bar");
        IFunction strFunc = Func.strings.UPPER("foobarbar");
        IFunction func = Func.strings.LOCATE(substrFunc, strFunc);
        String sql = func.build();
        System.out.println("testLOCATEWithFunction: " + sql);
        Assert.assertEquals("LOCATE(LOWER(bar), UPPER(foobarbar))", sql);
    }

    @Test
    public void testLOCATEWithPos() {
        IFunction func = Func.strings.LOCATE("bar", "foobarbar", "5");
        String sql = func.build();
        System.out.println("testLOCATEWithPos: " + sql);
        Assert.assertEquals("LOCATE(bar, foobarbar, 5)", sql);
    }

    @Test
    public void testLOCATEWithPosAndFunction() {
        IFunction substrFunc = Func.strings.LOWER("bar");
        IFunction strFunc = Func.strings.UPPER("foobarbar");
        IFunction func = Func.strings.LOCATE(substrFunc, strFunc, "5");
        String sql = func.build();
        System.out.println("testLOCATEWithPosAndFunction: " + sql);
        Assert.assertEquals("LOCATE(LOWER(bar), UPPER(foobarbar), 5)", sql);
    }

    @Test
    public void testLOWER() {
        IFunction func = Func.strings.LOWER("HELLO");
        String sql = func.build();
        System.out.println("testLOWER: " + sql);
        Assert.assertEquals("LOWER(HELLO)", sql);
    }

    @Test
    public void testLOWERLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.LOWER(column);
        String sql = func.build();
        System.out.println("testLOWERLambda: " + sql);
        Assert.assertEquals("LOWER(name)", sql);
    }

    @Test
    public void testLOWERLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.LOWER("t", column);
        String sql = func.build();
        System.out.println("testLOWERLambdaWithPrefix: " + sql);
        Assert.assertEquals("LOWER(t.name)", sql);
    }

    @Test
    public void testLOWERWithFunction() {
        IFunction innerFunc = Func.strings.UPPER("HELLO");
        IFunction func = Func.strings.LOWER(innerFunc);
        String sql = func.build();
        System.out.println("testLOWERWithFunction: " + sql);
        Assert.assertEquals("LOWER(UPPER(HELLO))", sql);
    }

    @Test
    public void testLPAD() {
        IFunction func = Func.strings.LPAD("hi", 5, "??");
        String sql = func.build();
        System.out.println("testLPAD: " + sql);
        Assert.assertEquals("LPAD(hi, 5, ??)", sql);
    }

    @Test
    public void testLPADLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.LPAD(column, 10, "*");
        String sql = func.build();
        System.out.println("testLPADLambda: " + sql);
        Assert.assertEquals("LPAD(name, 10, *)", sql);
    }

    @Test
    public void testLPADLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.LPAD("t", column, 10, "*");
        String sql = func.build();
        System.out.println("testLPADLambdaWithPrefix: " + sql);
        Assert.assertEquals("LPAD(t.name, 10, *)", sql);
    }

    @Test
    public void testLPADWithFunction() {
        IFunction strFunc = Func.strings.LOWER("hi");
        IFunction padstrFunc = Func.strings.UPPER("??");
        IFunction func = Func.strings.LPAD(strFunc, 5, padstrFunc);
        String sql = func.build();
        System.out.println("testLPADWithFunction: " + sql);
        Assert.assertEquals("LPAD(LOWER(hi), 5, UPPER(??))", sql);
    }

    @Test
    public void testLTRIM() {
        IFunction func = Func.strings.LTRIM("  barbar");
        String sql = func.build();
        System.out.println("testLTRIM: " + sql);
        Assert.assertEquals("LTRIM(  barbar)", sql);
    }

    @Test
    public void testLTRIMLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.LTRIM(column);
        String sql = func.build();
        System.out.println("testLTRIMLambda: " + sql);
        Assert.assertEquals("LTRIM(name)", sql);
    }

    @Test
    public void testLTRIMLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.LTRIM("t", column);
        String sql = func.build();
        System.out.println("testLTRIMLambdaWithPrefix: " + sql);
        Assert.assertEquals("LTRIM(t.name)", sql);
    }

    @Test
    public void testLTRIMWithFunction() {
        IFunction innerFunc = Func.strings.UPPER("  barbar");
        IFunction func = Func.strings.LTRIM(innerFunc);
        String sql = func.build();
        System.out.println("testLTRIMWithFunction: " + sql);
        Assert.assertEquals("LTRIM(UPPER(  barbar))", sql);
    }

    @Test
    public void testOCT() {
        IFunction func = Func.strings.OCT("12");
        String sql = func.build();
        System.out.println("testOCT: " + sql);
        Assert.assertEquals("OCT(12)", sql);
    }

    @Test
    public void testOCTLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.OCT(column);
        String sql = func.build();
        System.out.println("testOCTLambda: " + sql);
        Assert.assertEquals("OCT(name)", sql);
    }

    @Test
    public void testOCTLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.OCT("t", column);
        String sql = func.build();
        System.out.println("testOCTLambdaWithPrefix: " + sql);
        Assert.assertEquals("OCT(t.name)", sql);
    }

    @Test
    public void testOCTWithFunction() {
        IFunction innerFunc = Func.strings.ASCII("A");
        IFunction func = Func.strings.OCT(innerFunc);
        String sql = func.build();
        System.out.println("testOCTWithFunction: " + sql);
        Assert.assertEquals("OCT(ASCII(A))", sql);
    }

    @Test
    public void testORD() {
        IFunction func = Func.strings.ORD("2");
        String sql = func.build();
        System.out.println("testORD: " + sql);
        Assert.assertEquals("ORD(2)", sql);
    }

    @Test
    public void testORDLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.ORD(column);
        String sql = func.build();
        System.out.println("testORDLambda: " + sql);
        Assert.assertEquals("ORD(name)", sql);
    }

    @Test
    public void testORDLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.ORD("t", column);
        String sql = func.build();
        System.out.println("testORDLambdaWithPrefix: " + sql);
        Assert.assertEquals("ORD(t.name)", sql);
    }

    @Test
    public void testORDWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("2");
        IFunction func = Func.strings.ORD(innerFunc);
        String sql = func.build();
        System.out.println("testORDWithFunction: " + sql);
        Assert.assertEquals("ORD(LOWER(2))", sql);
    }

    @Test
    public void testQUOTE() {
        IFunction func = Func.strings.QUOTE("Don't");
        String sql = func.build();
        System.out.println("testQUOTE: " + sql);
        Assert.assertEquals("QUOTE(Don't)", sql);
    }

    @Test
    public void testQUOTELambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.QUOTE(column);
        String sql = func.build();
        System.out.println("testQUOTELambda: " + sql);
        Assert.assertEquals("QUOTE(name)", sql);
    }

    @Test
    public void testQUOTELambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.QUOTE("t", column);
        String sql = func.build();
        System.out.println("testQUOTELambdaWithPrefix: " + sql);
        Assert.assertEquals("QUOTE(t.name)", sql);
    }

    @Test
    public void testQUOTEWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("Don't");
        IFunction func = Func.strings.QUOTE(innerFunc);
        String sql = func.build();
        System.out.println("testQUOTEWithFunction: " + sql);
        Assert.assertEquals("QUOTE(LOWER(Don't))", sql);
    }

    @Test
    public void testREPEAT() {
        IFunction func = Func.strings.REPEAT("MySQL", 3);
        String sql = func.build();
        System.out.println("testREPEAT: " + sql);
        Assert.assertEquals("REPEAT(MySQL, 3)", sql);
    }

    @Test
    public void testREPEATLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.REPEAT(column, 2);
        String sql = func.build();
        System.out.println("testREPEATLambda: " + sql);
        Assert.assertEquals("REPEAT(name, 2)", sql);
    }

    @Test
    public void testREPEATLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.REPEAT("t", column, 2);
        String sql = func.build();
        System.out.println("testREPEATLambdaWithPrefix: " + sql);
        Assert.assertEquals("REPEAT(t.name, 2)", sql);
    }

    @Test
    public void testREPEATWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("MySQL");
        IFunction func = Func.strings.REPEAT(innerFunc, 3);
        String sql = func.build();
        System.out.println("testREPEATWithFunction: " + sql);
        Assert.assertEquals("REPEAT(LOWER(MySQL), 3)", sql);
    }

    @Test
    public void testREPLACE() {
        IFunction func = Func.strings.REPLACE("www.mysql.com", "w", "Ww");
        String sql = func.build();
        System.out.println("testREPLACE: " + sql);
        Assert.assertEquals("REPLACE(www.mysql.com, w, Ww)", sql);
    }

    @Test
    public void testREPLACELambda() {
        SFunction<TestEntity, String> column = TestEntity::getContent;
        IFunction func = Func.strings.REPLACE(column, "old", "new");
        String sql = func.build();
        System.out.println("testREPLACELambda: " + sql);
        Assert.assertEquals("REPLACE(content, old, new)", sql);
    }

    @Test
    public void testREPLACELambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getContent;
        IFunction func = Func.strings.REPLACE("t", column, "old", "new");
        String sql = func.build();
        System.out.println("testREPLACELambdaWithPrefix: " + sql);
        Assert.assertEquals("REPLACE(t.content, old, new)", sql);
    }

    @Test
    public void testREPLACEWithFunction() {
        IFunction strFunc = Func.strings.LOWER("www.mysql.com");
        IFunction fromStrFunc = Func.strings.UPPER("w");
        IFunction toStrFunc = Func.strings.UPPER("Ww");
        IFunction func = Func.strings.REPLACE(strFunc, fromStrFunc, toStrFunc);
        String sql = func.build();
        System.out.println("testREPLACEWithFunction: " + sql);
        Assert.assertEquals("REPLACE(LOWER(www.mysql.com), UPPER(w), UPPER(Ww))", sql);
    }

    @Test
    public void testREVERSE() {
        IFunction func = Func.strings.REVERSE("abc");
        String sql = func.build();
        System.out.println("testREVERSE: " + sql);
        Assert.assertEquals("REVERSE(abc)", sql);
    }

    @Test
    public void testREVERSELambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.REVERSE(column);
        String sql = func.build();
        System.out.println("testREVERSELambda: " + sql);
        Assert.assertEquals("REVERSE(name)", sql);
    }

    @Test
    public void testREVERSELambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.REVERSE("t", column);
        String sql = func.build();
        System.out.println("testREVERSELambdaWithPrefix: " + sql);
        Assert.assertEquals("REVERSE(t.name)", sql);
    }

    @Test
    public void testREVERSEWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("abc");
        IFunction func = Func.strings.REVERSE(innerFunc);
        String sql = func.build();
        System.out.println("testREVERSEWithFunction: " + sql);
        Assert.assertEquals("REVERSE(LOWER(abc))", sql);
    }

    @Test
    public void testRIGHT() {
        IFunction func = Func.strings.RIGHT("foobarbar", 4);
        String sql = func.build();
        System.out.println("testRIGHT: " + sql);
        Assert.assertEquals("RIGHT(foobarbar, 4)", sql);
    }

    @Test
    public void testRIGHTLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.RIGHT(column, 5);
        String sql = func.build();
        System.out.println("testRIGHTLambda: " + sql);
        Assert.assertEquals("RIGHT(name, 5)", sql);
    }

    @Test
    public void testRIGHTLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.RIGHT("t", column, 5);
        String sql = func.build();
        System.out.println("testRIGHTLambdaWithPrefix: " + sql);
        Assert.assertEquals("RIGHT(t.name, 5)", sql);
    }

    @Test
    public void testRIGHTWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("foobarbar");
        IFunction func = Func.strings.RIGHT(innerFunc, 4);
        String sql = func.build();
        System.out.println("testRIGHTWithFunction: " + sql);
        Assert.assertEquals("RIGHT(LOWER(foobarbar), 4)", sql);
    }

    @Test
    public void testRPAD() {
        IFunction func = Func.strings.RPAD("hi", 5, "?");
        String sql = func.build();
        System.out.println("testRPAD: " + sql);
        Assert.assertEquals("RPAD(hi, 5, ?)", sql);
    }

    @Test
    public void testRPADLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.RPAD(column, 10, "*");
        String sql = func.build();
        System.out.println("testRPADLambda: " + sql);
        Assert.assertEquals("RPAD(name, 10, *)", sql);
    }

    @Test
    public void testRPADLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.RPAD("t", column, 10, "*");
        String sql = func.build();
        System.out.println("testRPADLambdaWithPrefix: " + sql);
        Assert.assertEquals("RPAD(t.name, 10, *)", sql);
    }

    @Test
    public void testRPADWithFunction() {
        IFunction strFunc = Func.strings.LOWER("hi");
        IFunction padstrFunc = Func.strings.UPPER("?");
        IFunction func = Func.strings.RPAD(strFunc, 5, padstrFunc);
        String sql = func.build();
        System.out.println("testRPADWithFunction: " + sql);
        Assert.assertEquals("RPAD(LOWER(hi), 5, UPPER(?))", sql);
    }

    @Test
    public void testRTRIM() {
        IFunction func = Func.strings.RTRIM("barbar   ");
        String sql = func.build();
        System.out.println("testRTRIM: " + sql);
        Assert.assertEquals("RTRIM(barbar   )", sql);
    }

    @Test
    public void testRTRIMLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.RTRIM(column);
        String sql = func.build();
        System.out.println("testRTRIMLambda: " + sql);
        Assert.assertEquals("RTRIM(name)", sql);
    }

    @Test
    public void testRTRIMLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.RTRIM("t", column);
        String sql = func.build();
        System.out.println("testRTRIMLambdaWithPrefix: " + sql);
        Assert.assertEquals("RTRIM(t.name)", sql);
    }

    @Test
    public void testRTRIMWithFunction() {
        IFunction innerFunc = Func.strings.UPPER("barbar   ");
        IFunction func = Func.strings.RTRIM(innerFunc);
        String sql = func.build();
        System.out.println("testRTRIMWithFunction: " + sql);
        Assert.assertEquals("RTRIM(UPPER(barbar   ))", sql);
    }

    @Test
    public void testSOUNDEX() {
        IFunction func = Func.strings.SOUNDEX("Hello");
        String sql = func.build();
        System.out.println("testSOUNDEX: " + sql);
        Assert.assertEquals("SOUNDEX(Hello)", sql);
    }

    @Test
    public void testSOUNDEXLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.SOUNDEX(column);
        String sql = func.build();
        System.out.println("testSOUNDEXLambda: " + sql);
        Assert.assertEquals("SOUNDEX(name)", sql);
    }

    @Test
    public void testSOUNDEXLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.SOUNDEX("t", column);
        String sql = func.build();
        System.out.println("testSOUNDEXLambdaWithPrefix: " + sql);
        Assert.assertEquals("SOUNDEX(t.name)", sql);
    }

    @Test
    public void testSOUNDEXWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("Hello");
        IFunction func = Func.strings.SOUNDEX(innerFunc);
        String sql = func.build();
        System.out.println("testSOUNDEXWithFunction: " + sql);
        Assert.assertEquals("SOUNDEX(LOWER(Hello))", sql);
    }

    @Test
    public void testSPACE() {
        IFunction func = Func.strings.SPACE(5);
        String sql = func.build();
        System.out.println("testSPACE: " + sql);
        Assert.assertEquals("SPACE(5)", sql);
    }

    @Test
    public void testSTRCMP() {
        IFunction func = Func.strings.STRCMP("text", "text2");
        String sql = func.build();
        System.out.println("testSTRCMP: " + sql);
        Assert.assertEquals("STRCMP(text, text2)", sql);
    }

    @Test
    public void testSTRCMPLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.STRCMP(column, "test");
        String sql = func.build();
        System.out.println("testSTRCMPLambda: " + sql);
        Assert.assertEquals("STRCMP(name, test)", sql);
    }

    @Test
    public void testSTRCMPLambdaReverse() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.STRCMP("test", column);
        String sql = func.build();
        System.out.println("testSTRCMPLambdaReverse: " + sql);
        Assert.assertEquals("STRCMP(test, name)", sql);
    }

    @Test
    public void testSTRCMPTwoLambdas() {
        SFunction<TestEntity, String> column1 = TestEntity::getName;
        SFunction<TestEntity, String> column2 = TestEntity::getContent;
        IFunction func = Func.strings.STRCMP(column1, column2);
        String sql = func.build();
        System.out.println("testSTRCMPTwoLambdas: " + sql);
        Assert.assertEquals("STRCMP(name, content)", sql);
    }

    @Test
    public void testSTRCMPLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.STRCMP("t", column, "test");
        String sql = func.build();
        System.out.println("testSTRCMPLambdaWithPrefix: " + sql);
        Assert.assertEquals("STRCMP(t.name, test)", sql);
    }

    @Test
    public void testSTRCMPLambdaReverseWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.STRCMP("test", "t", column);
        String sql = func.build();
        System.out.println("testSTRCMPLambdaReverseWithPrefix: " + sql);
        Assert.assertEquals("STRCMP(test, t.name)", sql);
    }

    @Test
    public void testSTRCMPTwoLambdasWithPrefix() {
        SFunction<TestEntity, String> column1 = TestEntity::getName;
        SFunction<TestEntity, String> column2 = TestEntity::getContent;
        IFunction func = Func.strings.STRCMP("t1", column1, "t2", column2);
        String sql = func.build();
        System.out.println("testSTRCMPTwoLambdasWithPrefix: " + sql);
        Assert.assertEquals("STRCMP(t1.name, t2.content)", sql);
    }

    @Test
    public void testSTRCMPWithFunction() {
        IFunction expr1Func = Func.strings.LOWER("text");
        IFunction expr2Func = Func.strings.UPPER("text2");
        IFunction func = Func.strings.STRCMP(expr1Func, expr2Func);
        String sql = func.build();
        System.out.println("testSTRCMPWithFunction: " + sql);
        Assert.assertEquals("STRCMP(LOWER(text), UPPER(text2))", sql);
    }

    @Test
    public void testSUBSTRING() {
        IFunction func = Func.strings.SUBSTRING("Quadratically", 5);
        String sql = func.build();
        System.out.println("testSUBSTRING: " + sql);
        Assert.assertEquals("SUBSTRING(Quadratically, 5)", sql);
    }

    @Test
    public void testSUBSTRINGLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.SUBSTRING(column, 3);
        String sql = func.build();
        System.out.println("testSUBSTRINGLambda: " + sql);
        Assert.assertEquals("SUBSTRING(name, 3)", sql);
    }

    @Test
    public void testSUBSTRINGLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.SUBSTRING("t", column, 3);
        String sql = func.build();
        System.out.println("testSUBSTRINGLambdaWithPrefix: " + sql);
        Assert.assertEquals("SUBSTRING(t.name, 3)", sql);
    }

    @Test
    public void testSUBSTRINGWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("Quadratically");
        IFunction func = Func.strings.SUBSTRING(innerFunc, 5);
        String sql = func.build();
        System.out.println("testSUBSTRINGWithFunction: " + sql);
        Assert.assertEquals("SUBSTRING(LOWER(Quadratically), 5)", sql);
    }

    @Test
    public void testSUBSTRINGWithLen() {
        IFunction func = Func.strings.SUBSTRING("Quadratically", 5, 6);
        String sql = func.build();
        System.out.println("testSUBSTRINGWithLen: " + sql);
        Assert.assertEquals("SUBSTRING(Quadratically, 5, 6)", sql);
    }

    @Test
    public void testSUBSTRINGLambdaWithLen() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.SUBSTRING(column, 3, 5);
        String sql = func.build();
        System.out.println("testSUBSTRINGLambdaWithLen: " + sql);
        Assert.assertEquals("SUBSTRING(name, 3, 5)", sql);
    }

    @Test
    public void testSUBSTRINGLambdaWithPrefixWithLen() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.SUBSTRING("t", column, 3, 5);
        String sql = func.build();
        System.out.println("testSUBSTRINGLambdaWithPrefixWithLen: " + sql);
        Assert.assertEquals("SUBSTRING(t.name, 3, 5)", sql);
    }

    @Test
    public void testSUBSTRINGWithFunctionWithLen() {
        IFunction innerFunc = Func.strings.LOWER("Quadratically");
        IFunction func = Func.strings.SUBSTRING(innerFunc, 5, 6);
        String sql = func.build();
        System.out.println("testSUBSTRINGWithFunctionWithLen: " + sql);
        Assert.assertEquals("SUBSTRING(LOWER(Quadratically), 5, 6)", sql);
    }

    @Test
    public void testSUBSTRING_INDEX() {
        IFunction func = Func.strings.SUBSTRING_INDEX("www.mysql.com", ".", 2);
        String sql = func.build();
        System.out.println("testSUBSTRING: " + sql);
        Assert.assertEquals("SUBSTRING_INDEX(www.mysql.com, ., 2)", sql);
    }

    @Test
    public void testSUBSTRING_INDEXLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.SUBSTRING_INDEX(column, ".", 1);
        String sql = func.build();
        System.out.println("testSUBSTRING: " + sql);
        Assert.assertEquals("SUBSTRING_INDEX(name, ., 1)", sql);
    }

    @Test
    public void testSUBSTRING_INDEXLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.SUBSTRING_INDEX("t", column, ".", 1);
        String sql = func.build();
        System.out.println("testSUBSTRING: " + sql);
        Assert.assertEquals("SUBSTRING_INDEX(t.name, ., 1)", sql);
    }

    @Test
    public void testSUBSTRING_INDEXWithFunction() {
        IFunction strFunc = Func.strings.LOWER("www.mysql.com");
        IFunction delimFunc = Func.strings.UPPER(".");
        IFunction func = Func.strings.SUBSTRING_INDEX(strFunc, delimFunc, 2);
        String sql = func.build();
        System.out.println("testSUBSTRING: " + sql);
        Assert.assertEquals("SUBSTRING_INDEX(LOWER(www.mysql.com), UPPER(.), 2)", sql);
    }

    @Test
    public void testTRIM() {
        IFunction func = Func.strings.TRIM("  bar  ");
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(  bar  )", sql);
    }

    @Test
    public void testTRIMLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.TRIM(column);
        String sql = func.build();
        System.out.println("testTRIMLambda: " + sql);
        Assert.assertEquals("TRIM(name)", sql);
    }

    @Test
    public void testTRIMLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.TRIM("t", column);
        String sql = func.build();
        System.out.println("testTRIMLambdaWithPrefix: " + sql);
        Assert.assertEquals("TRIM(t.name)", sql);
    }

    @Test
    public void testTRIMWithFunction() {
        IFunction innerFunc = Func.strings.UPPER("  bar  ");
        IFunction func = Func.strings.TRIM(innerFunc);
        String sql = func.build();
        System.out.println("testTRIMWithFunction: " + sql);
        Assert.assertEquals("TRIM(UPPER(  bar  ))", sql);
    }

    @Test
    public void testTRIM_BOTH() {
        IFunction func = Func.strings.TRIM_BOTH("x", "xxxbarxxx");
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(BOTH x FROM xxxbarxxx)", sql);
    }

    @Test
    public void testTRIM_BOTHLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.TRIM_BOTH("x", column);
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(BOTH x FROM name)", sql);
    }

    @Test
    public void testTRIM_BOTHLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.TRIM_BOTH("x", "t", column);
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(BOTH x FROM t.name)", sql);
    }

    @Test
    public void testTRIM_BOTHWithFunction() {
        IFunction remstrFunc = Func.strings.LOWER("x");
        IFunction strFunc = Func.strings.UPPER("xxxbarxxx");
        IFunction func = Func.strings.TRIM_BOTH(remstrFunc, strFunc);
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(BOTH LOWER(x) FROM UPPER(xxxbarxxx))", sql);
    }

    @Test
    public void testTRIM_LEADIN() {
        IFunction func = Func.strings.TRIM_LEADIN("x", "xxxbarxxx");
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(LEADIN x FROM xxxbarxxx)", sql);
    }

    @Test
    public void testTRIM_LEADINLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.TRIM_LEADIN("x", column);
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(LEADIN x FROM name)", sql);
    }

    @Test
    public void testTRIM_LEADINLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.TRIM_LEADIN("x", "t", column);
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(LEADIN x FROM t.name)", sql);
    }

    @Test
    public void testTRIM_LEADINWithFunction() {
        IFunction remstrFunc = Func.strings.LOWER("x");
        IFunction strFunc = Func.strings.UPPER("xxxbarxxx");
        IFunction func = Func.strings.TRIM_LEADIN(remstrFunc, strFunc);
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(LEADIN LOWER(x) FROM UPPER(xxxbarxxx))", sql);
    }

    @Test
    public void testTRIM_TRAILING() {
        IFunction func = Func.strings.TRIM_TRAILING("x", "xxxbarxxx");
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(TRAILING x FROM xxxbarxxx)", sql);
    }

    @Test
    public void testTRIM_TRAILINGLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.TRIM_TRAILING("x", column);
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(TRAILING x FROM name)", sql);
    }

    @Test
    public void testTRIM_TRAILINGLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.TRIM_TRAILING("x", "t", column);
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(TRAILING x FROM t.name)", sql);
    }

    @Test
    public void testTRIM_TRAILINGWithFunction() {
        IFunction remstrFunc = Func.strings.LOWER("x");
        IFunction strFunc = Func.strings.UPPER("xxxbarxxx");
        IFunction func = Func.strings.TRIM_TRAILING(remstrFunc, strFunc);
        String sql = func.build();
        System.out.println("testTRIM: " + sql);
        Assert.assertEquals("TRIM(TRAILING LOWER(x) FROM UPPER(xxxbarxxx))", sql);
    }

    @Test
    public void testUNHEX() {
        IFunction func = Func.strings.UNHEX("4D7953514C");
        String sql = func.build();
        System.out.println("testUNHEX: " + sql);
        Assert.assertEquals("UNHEX(4D7953514C)", sql);
    }

    @Test
    public void testUNHEXWithFunction() {
        IFunction innerFunc = Func.strings.HEX("test");
        IFunction func = Func.strings.UNHEX(innerFunc);
        String sql = func.build();
        System.out.println("testUNHEXWithFunction: " + sql);
        Assert.assertEquals("UNHEX(HEX(test))", sql);
    }

    @Test
    public void testUPPER() {
        IFunction func = Func.strings.UPPER("hello");
        String sql = func.build();
        System.out.println("testUPPER: " + sql);
        Assert.assertEquals("UPPER(hello)", sql);
    }

    @Test
    public void testUPPERLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.UPPER(column);
        String sql = func.build();
        System.out.println("testUPPERLambda: " + sql);
        Assert.assertEquals("UPPER(name)", sql);
    }

    @Test
    public void testUPPERLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.UPPER("t", column);
        String sql = func.build();
        System.out.println("testUPPERLambdaWithPrefix: " + sql);
        Assert.assertEquals("UPPER(t.name)", sql);
    }

    @Test
    public void testUPPERWithFunction() {
        IFunction innerFunc = Func.strings.LOWER("hello");
        IFunction func = Func.strings.UPPER(innerFunc);
        String sql = func.build();
        System.out.println("testUPPERWithFunction: " + sql);
        Assert.assertEquals("UPPER(LOWER(hello))", sql);
    }

    @Test
    public void testREGEXP_INSTR() {
        IFunction func = Func.strings.REGEXP_INSTR("cat", "at");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_INSTR(cat, at)", sql);
    }

    @Test
    public void testREGEXP_INSTRLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.REGEXP_INSTR(column, "at");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_INSTR(name, at)", sql);
    }

    @Test
    public void testREGEXP_INSTRLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.REGEXP_INSTR("t", column, "at");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_INSTR(t.name, at)", sql);
    }

    @Test
    public void testREGEXP_INSTRWithFunction() {
        IFunction strFunc = Func.strings.LOWER("cat");
        IFunction patternFunc = Func.strings.UPPER("at");
        IFunction func = Func.strings.REGEXP_INSTR(strFunc, patternFunc);
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_INSTR(LOWER(cat), UPPER(at))", sql);
    }

    @Test
    public void testREGEXP_INSTRWithPos() {
        IFunction func = Func.strings.REGEXP_INSTR("cat", "at", 1);
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_INSTR(cat, at, 1)", sql);
    }

    @Test
    public void testREGEXP_INSTRWithPosAndOccurrence() {
        IFunction func = Func.strings.REGEXP_INSTR("cat", "at", 1, 1);
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_INSTR(cat, at, 1, 1)", sql);
    }

    @Test
    public void testREGEXP_INSTRWithAllParams() {
        IFunction func = Func.strings.REGEXP_INSTR("cat", "at", 1, 1, "0", "i");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_INSTR(cat, at, 1, 1, 0, i)", sql);
    }

    @Test
    public void testREGEXP_LIKE() {
        IFunction func = Func.strings.REGEXP_LIKE("cat", "at");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_LIKE(cat, at)", sql);
    }

    @Test
    public void testREGEXP_LIKELambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.REGEXP_LIKE(column, "at");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_LIKE(name, at)", sql);
    }

    @Test
    public void testREGEXP_LIKELambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.REGEXP_LIKE("t", column, "at");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_LIKE(t.name, at)", sql);
    }

    @Test
    public void testREGEXP_LIKEWithFunction() {
        IFunction strFunc = Func.strings.LOWER("cat");
        IFunction patternFunc = Func.strings.UPPER("at");
        IFunction func = Func.strings.REGEXP_LIKE(strFunc, patternFunc);
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_LIKE(LOWER(cat), UPPER(at))", sql);
    }

    @Test
    public void testREGEXP_LIKEWithMatchType() {
        IFunction func = Func.strings.REGEXP_LIKE("cat", "at", "i");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_LIKE(cat, at, i)", sql);
    }

    @Test
    public void testREGEXP_REPLACE() {
        IFunction func = Func.strings.REGEXP_REPLACE("cat", "at", "dog");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_REPLACE(cat, at, dog)", sql);
    }

    @Test
    public void testREGEXP_REPLACELambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.REGEXP_REPLACE(column, "at", "dog");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_REPLACE(name, at, dog)", sql);
    }

    @Test
    public void testREGEXP_REPLACELambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.REGEXP_REPLACE("t", column, "at", "dog");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_REPLACE(t.name, at, dog)", sql);
    }

    @Test
    public void testREGEXP_REPLACEWithFunction() {
        IFunction strFunc = Func.strings.LOWER("cat");
        IFunction patternFunc = Func.strings.UPPER("at");
        IFunction replacementFunc = Func.strings.UPPER("dog");
        IFunction func = Func.strings.REGEXP_REPLACE(strFunc, patternFunc, replacementFunc);
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_REPLACE(LOWER(cat), UPPER(at), UPPER(dog))", sql);
    }

    @Test
    public void testREGEXP_REPLACEWithPos() {
        IFunction func = Func.strings.REGEXP_REPLACE("cat", "at", "dog", 1);
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_REPLACE(cat, at, dog, 1)", sql);
    }

    @Test
    public void testREGEXP_REPLACEWithAllParams() {
        IFunction func = Func.strings.REGEXP_REPLACE("cat", "at", "dog", 1, 1, "i");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_REPLACE(cat, at, dog, 1, 1, i)", sql);
    }

    @Test
    public void testREGEXP_SUBSTR() {
        IFunction func = Func.strings.REGEXP_SUBSTR("cat", "at");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_SUBSTR(cat, at)", sql);
    }

    @Test
    public void testREGEXP_SUBSTRLambda() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.REGEXP_SUBSTR(column, "at");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_SUBSTR(name, at)", sql);
    }

    @Test
    public void testREGEXP_SUBSTRLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getName;
        IFunction func = Func.strings.REGEXP_SUBSTR("t", column, "at");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_SUBSTR(t.name, at)", sql);
    }

    @Test
    public void testREGEXP_SUBSTRWithFunction() {
        IFunction strFunc = Func.strings.LOWER("cat");
        IFunction patternFunc = Func.strings.UPPER("at");
        IFunction func = Func.strings.REGEXP_SUBSTR(strFunc, patternFunc);
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_SUBSTR(LOWER(cat), UPPER(at))", sql);
    }

    @Test
    public void testREGEXP_SUBSTRWithPos() {
        IFunction func = Func.strings.REGEXP_SUBSTR("cat", "at", 1);
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_SUBSTR(cat, at, 1)", sql);
    }

    @Test
    public void testREGEXP_SUBSTRWithAllParams() {
        IFunction func = Func.strings.REGEXP_SUBSTR("cat", "at", 1, 1, "i");
        String sql = func.build();
        System.out.println("testREGEXP: " + sql);
        Assert.assertEquals("REGEXP_SUBSTR(cat, at, 1, 1, i)", sql);
    }

    @Test
    public void testComplexStringFunction() {
        IFunction func = Func.strings.REPLACE(
                Func.strings.UPPER("hello world"),
                Func.strings.UPPER("WORLD"),
                Func.strings.LOWER("MySQL")
        );
        String sql = func.build();
        System.out.println("testComplexStringFunction: " + sql);
        Assert.assertEquals("REPLACE(UPPER(hello world), UPPER(WORLD), LOWER(MySQL))", sql);
    }
}
