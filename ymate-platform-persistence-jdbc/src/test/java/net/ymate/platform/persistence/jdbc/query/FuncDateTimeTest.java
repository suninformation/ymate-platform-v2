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
 * Func DateTime 功能测试类 - 完整覆盖Func接口中DateTime相关的所有功能
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-27
 * @since 2.1.4
 */
@RunWith(YMPJUnit4ClassRunner.class)
@EnableAutoScan
@EnableBeanProxy
@EnableDevMode
public class FuncDateTimeTest {

    @Entity
    public static class TestEntity implements IEntity<String> {

        @Id
        @Property
        private String id;

        @Property
        private String birth_date;

        @Property
        private String create_time;

        @Override
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getBirthDate() {
            return birth_date;
        }

        public void setBirthDate(String birth_date) {
            this.birth_date = birth_date;
        }

        public String getCreateTime() {
            return create_time;
        }

        public void setCreateTime(String create_time) {
            this.create_time = create_time;
        }
    }

    @Test
    public void testADDDATE() {
        IFunction func = Func.dateTime.ADDDATE("2024-01-01", 7);
        String sql = func.build();
        System.out.println("testADDDATE: " + sql);
        Assert.assertEquals("ADDDATE(2024-01-01, 7)", sql);
    }

    @Test
    public void testADDDATEWithFunction() {
        IFunction innerFunc = Func.dateTime.DATE("2024-01-01");
        IFunction func = Func.dateTime.ADDDATE(innerFunc, 7);
        String sql = func.build();
        System.out.println("testADDDATEWithFunction: " + sql);
        Assert.assertEquals("ADDDATE(DATE(2024-01-01), 7)", sql);
    }

    @Test
    public void testADDTIME() {
        IFunction func = Func.dateTime.ADDTIME("2024-01-01 12:00:00", "01:00:00");
        String sql = func.build();
        System.out.println("testADDTIME: " + sql);
        Assert.assertEquals("ADDTIME(2024-01-01 12:00:00, 01:00:00)", sql);
    }

    @Test
    public void testADDTIMEWithFunction() {
        IFunction func1 = Func.dateTime.NOW();
        IFunction func2 = Func.dateTime.CURTIME();
        IFunction func = Func.dateTime.ADDTIME(func1, func2);
        String sql = func.build();
        System.out.println("testADDTIMEWithFunction: " + sql);
        Assert.assertEquals("ADDTIME(NOW(), CURTIME())", sql);
    }

    @Test
    public void testCONVERT_TZ() {
        IFunction func = Func.dateTime.CONVERT_TZ("2024-01-01 12:00:00", "UTC", "Asia/Shanghai");
        String sql = func.build();
        System.out.println("testCONVERT: " + sql);
        Assert.assertEquals("CONVERT_TZ(2024-01-01 12:00:00, UTC, Asia/Shanghai)", sql);
    }

    @Test
    public void testCONVERT_TZWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.CONVERT_TZ(innerFunc, "UTC", "Asia/Shanghai");
        String sql = func.build();
        System.out.println("testCONVERT: " + sql);
        Assert.assertEquals("CONVERT_TZ(NOW(), UTC, Asia/Shanghai)", sql);
    }

    @Test
    public void testCURDATE() {
        IFunction func = Func.dateTime.CURDATE();
        String sql = func.build();
        System.out.println("testCURDATE: " + sql);
        Assert.assertEquals("CURDATE()", sql);
    }

    @Test
    public void testCURTIME() {
        IFunction func = Func.dateTime.CURTIME();
        String sql = func.build();
        System.out.println("testCURTIME: " + sql);
        Assert.assertEquals("CURTIME()", sql);
    }

    @Test
    public void testDATE() {
        IFunction func = Func.dateTime.DATE("2024-01-01 12:00:00");
        String sql = func.build();
        System.out.println("testDATE: " + sql);
        Assert.assertEquals("DATE(2024-01-01 12:00:00)", sql);
    }

    @Test
    public void testDATELambda() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DATE(column);
        String sql = func.build();
        System.out.println("testDATELambda: " + sql);
        Assert.assertEquals("DATE(birth_date)", sql);
    }

    @Test
    public void testDATELambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DATE("t", column);
        String sql = func.build();
        System.out.println("testDATELambdaWithPrefix: " + sql);
        Assert.assertEquals("DATE(t.birth_date)", sql);
    }

    @Test
    public void testDATEWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.DATE(innerFunc);
        String sql = func.build();
        System.out.println("testDATEWithFunction: " + sql);
        Assert.assertEquals("DATE(NOW())", sql);
    }

    @Test
    public void testDATE_FORMAT() {
        IFunction func = Func.dateTime.DATE_FORMAT("2024-01-01", "%Y-%m-%d");
        String sql = func.build();
        System.out.println("testDATE: " + sql);
        Assert.assertEquals("DATE_FORMAT(2024-01-01, %Y-%m-%d)", sql);
    }

    @Test
    public void testDATE_FORMATLambda() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DATE_FORMAT(column, "%Y-%m-%d");
        String sql = func.build();
        System.out.println("testDATE: " + sql);
        Assert.assertEquals("DATE_FORMAT(birth_date, %Y-%m-%d)", sql);
    }

    @Test
    public void testDATE_FORMATLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DATE_FORMAT("t", column, "%Y-%m-%d");
        String sql = func.build();
        System.out.println("testDATE: " + sql);
        Assert.assertEquals("DATE_FORMAT(t.birth_date, %Y-%m-%d)", sql);
    }

    @Test
    public void testDATE_FORMATWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.DATE_FORMAT(innerFunc, "%Y-%m-%d");
        String sql = func.build();
        System.out.println("testDATE: " + sql);
        Assert.assertEquals("DATE_FORMAT(NOW(), %Y-%m-%d)", sql);
    }

    @Test
    public void testDATEDIFF() {
        IFunction func = Func.dateTime.DATEDIFF("2024-01-10", "2024-01-01");
        String sql = func.build();
        System.out.println("testDATEDIFF: " + sql);
        Assert.assertEquals("DATEDIFF(2024-01-10, 2024-01-01)", sql);
    }

    @Test
    public void testDATEDIFFLambda() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DATEDIFF(column, "2024-01-01");
        String sql = func.build();
        System.out.println("testDATEDIFFLambda: " + sql);
        Assert.assertEquals("DATEDIFF(birth_date, 2024-01-01)", sql);
    }

    @Test
    public void testDATEDIFFLambdaReverse() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DATEDIFF("2024-01-01", column);
        String sql = func.build();
        System.out.println("testDATEDIFFLambdaReverse: " + sql);
        Assert.assertEquals("DATEDIFF(2024-01-01, birth_date)", sql);
    }

    @Test
    public void testDATEDIFFLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DATEDIFF("t", column, "2024-01-01");
        String sql = func.build();
        System.out.println("testDATEDIFFLambdaWithPrefix: " + sql);
        Assert.assertEquals("DATEDIFF(t.birth_date, 2024-01-01)", sql);
    }

    @Test
    public void testDATEDIFFLambdaReverseWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DATEDIFF("2024-01-01", "t", column);
        String sql = func.build();
        System.out.println("testDATEDIFFLambdaReverseWithPrefix: " + sql);
        Assert.assertEquals("DATEDIFF(2024-01-01, t.birth_date)", sql);
    }

    @Test
    public void testDATEDIFFWithFunction() {
        IFunction func1 = Func.dateTime.NOW();
        IFunction func2 = Func.dateTime.DATE("2024-01-01");
        IFunction func = Func.dateTime.DATEDIFF(func1, func2);
        String sql = func.build();
        System.out.println("testDATEDIFFWithFunction: " + sql);
        Assert.assertEquals("DATEDIFF(NOW(), DATE(2024-01-01))", sql);
    }

    @Test
    public void testDAYNAME() {
        IFunction func = Func.dateTime.DAYNAME("2024-01-01");
        String sql = func.build();
        System.out.println("testDAYNAME: " + sql);
        Assert.assertEquals("DAYNAME(2024-01-01)", sql);
    }

    @Test
    public void testDAYNAMELambda() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DAYNAME(column);
        String sql = func.build();
        System.out.println("testDAYNAMELambda: " + sql);
        Assert.assertEquals("DAYNAME(birth_date)", sql);
    }

    @Test
    public void testDAYNAMELambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DAYNAME("t", column);
        String sql = func.build();
        System.out.println("testDAYNAMELambdaWithPrefix: " + sql);
        Assert.assertEquals("DAYNAME(t.birth_date)", sql);
    }

    @Test
    public void testDAYNAMEWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.DAYNAME(innerFunc);
        String sql = func.build();
        System.out.println("testDAYNAMEWithFunction: " + sql);
        Assert.assertEquals("DAYNAME(NOW())", sql);
    }

    @Test
    public void testDAYOFMONTH() {
        IFunction func = Func.dateTime.DAYOFMONTH("2024-01-15");
        String sql = func.build();
        System.out.println("testDAYOFMONTH: " + sql);
        Assert.assertEquals("DAYOFMONTH(2024-01-15)", sql);
    }

    @Test
    public void testDAYOFMONTHLambda() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DAYOFMONTH(column);
        String sql = func.build();
        System.out.println("testDAYOFMONTHLambda: " + sql);
        Assert.assertEquals("DAYOFMONTH(birth_date)", sql);
    }

    @Test
    public void testDAYOFMONTHLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DAYOFMONTH("t", column);
        String sql = func.build();
        System.out.println("testDAYOFMONTHLambdaWithPrefix: " + sql);
        Assert.assertEquals("DAYOFMONTH(t.birth_date)", sql);
    }

    @Test
    public void testDAYOFMONTHWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.DAYOFMONTH(innerFunc);
        String sql = func.build();
        System.out.println("testDAYOFMONTHWithFunction: " + sql);
        Assert.assertEquals("DAYOFMONTH(NOW())", sql);
    }

    @Test
    public void testDAYOFWEEK() {
        IFunction func = Func.dateTime.DAYOFWEEK("2024-01-01");
        String sql = func.build();
        System.out.println("testDAYOFWEEK: " + sql);
        Assert.assertEquals("DAYOFWEEK(2024-01-01)", sql);
    }

    @Test
    public void testDAYOFWEEKLambda() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DAYOFWEEK(column);
        String sql = func.build();
        System.out.println("testDAYOFWEEKLambda: " + sql);
        Assert.assertEquals("DAYOFWEEK(birth_date)", sql);
    }

    @Test
    public void testDAYOFWEEKLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DAYOFWEEK("t", column);
        String sql = func.build();
        System.out.println("testDAYOFWEEKLambdaWithPrefix: " + sql);
        Assert.assertEquals("DAYOFWEEK(t.birth_date)", sql);
    }

    @Test
    public void testDAYOFWEEKWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.DAYOFWEEK(innerFunc);
        String sql = func.build();
        System.out.println("testDAYOFWEEKWithFunction: " + sql);
        Assert.assertEquals("DAYOFWEEK(NOW())", sql);
    }

    @Test
    public void testDAYOFYEAR() {
        IFunction func = Func.dateTime.DAYOFYEAR("2024-02-01");
        String sql = func.build();
        System.out.println("testDAYOFYEAR: " + sql);
        Assert.assertEquals("DAYOFYEAR(2024-02-01)", sql);
    }

    @Test
    public void testDAYOFYEARLambda() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DAYOFYEAR(column);
        String sql = func.build();
        System.out.println("testDAYOFYEARLambda: " + sql);
        Assert.assertEquals("DAYOFYEAR(birth_date)", sql);
    }

    @Test
    public void testDAYOFYEARLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.DAYOFYEAR("t", column);
        String sql = func.build();
        System.out.println("testDAYOFYEARLambdaWithPrefix: " + sql);
        Assert.assertEquals("DAYOFYEAR(t.birth_date)", sql);
    }

    @Test
    public void testDAYOFYEARWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.DAYOFYEAR(innerFunc);
        String sql = func.build();
        System.out.println("testDAYOFYEARWithFunction: " + sql);
        Assert.assertEquals("DAYOFYEAR(NOW())", sql);
    }

    @Test
    public void testFROM_UNIXTIME() {
        IFunction func = Func.dateTime.FROM_UNIXTIME("1704067200");
        String sql = func.build();
        System.out.println("testFROM: " + sql);
        Assert.assertEquals("FROM_UNIXTIME(1704067200)", sql);
    }

    @Test
    public void testFROM_UNIXTIMELambda() {
        SFunction<TestEntity, String> column = TestEntity::getCreateTime;
        IFunction func = Func.dateTime.FROM_UNIXTIME(column);
        String sql = func.build();
        System.out.println("testFROM: " + sql);
        Assert.assertEquals("FROM_UNIXTIME(create_time)", sql);
    }

    @Test
    public void testFROM_UNIXTIMELambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getCreateTime;
        IFunction func = Func.dateTime.FROM_UNIXTIME("t", column);
        String sql = func.build();
        System.out.println("testFROM: " + sql);
        Assert.assertEquals("FROM_UNIXTIME(t.create_time)", sql);
    }

    @Test
    public void testFROM_UNIXTIMEWithFunction() {
        IFunction innerFunc = Func.dateTime.UNIX_TIMESTAMP();
        IFunction func = Func.dateTime.FROM_UNIXTIME(innerFunc);
        String sql = func.build();
        System.out.println("testFROM: " + sql);
        Assert.assertEquals("FROM_UNIXTIME(UNIX_TIMESTAMP())", sql);
    }

    @Test
    public void testFROM_UNIXTIMEWithFormat() {
        IFunction func = Func.dateTime.FROM_UNIXTIME("1704067200", "%Y-%m-%d %H:%i:%s");
        String sql = func.build();
        System.out.println("testFROM: " + sql);
        Assert.assertEquals("FROM_UNIXTIME(1704067200, %Y-%m-%d %H:%i:%s)", sql);
    }

    @Test
    public void testFROM_UNIXTIMELambdaWithFormat() {
        SFunction<TestEntity, String> column = TestEntity::getCreateTime;
        IFunction func = Func.dateTime.FROM_UNIXTIME(column, "%Y-%m-%d");
        String sql = func.build();
        System.out.println("testFROM: " + sql);
        Assert.assertEquals("FROM_UNIXTIME(create_time, %Y-%m-%d)", sql);
    }

    @Test
    public void testFROM_UNIXTIMELambdaWithPrefixAndFormat() {
        SFunction<TestEntity, String> column = TestEntity::getCreateTime;
        IFunction func = Func.dateTime.FROM_UNIXTIME("t", column, "%Y-%m-%d");
        String sql = func.build();
        System.out.println("testFROM: " + sql);
        Assert.assertEquals("FROM_UNIXTIME(t.create_time, %Y-%m-%d)", sql);
    }

    @Test
    public void testFROM_UNIXTIMEWithFunctionAndFormat() {
        IFunction innerFunc = Func.dateTime.UNIX_TIMESTAMP();
        IFunction func = Func.dateTime.FROM_UNIXTIME(innerFunc, "%Y-%m-%d");
        String sql = func.build();
        System.out.println("testFROM: " + sql);
        Assert.assertEquals("FROM_UNIXTIME(UNIX_TIMESTAMP(), %Y-%m-%d)", sql);
    }

    @Test
    public void testUNIX_TIMESTAMP() {
        IFunction func = Func.dateTime.UNIX_TIMESTAMP();
        String sql = func.build();
        System.out.println("testUNIX: " + sql);
        Assert.assertEquals("UNIX_TIMESTAMP()", sql);
    }

    @Test
    public void testUNIX_TIMESTAMPWithDate() {
        IFunction func = Func.dateTime.UNIX_TIMESTAMP("2024-01-01");
        String sql = func.build();
        System.out.println("testUNIX: " + sql);
        Assert.assertEquals("UNIX_TIMESTAMP(2024-01-01)", sql);
    }

    @Test
    public void testUNIX_TIMESTAMPLambda() {
        SFunction<TestEntity, String> column = TestEntity::getCreateTime;
        IFunction func = Func.dateTime.UNIX_TIMESTAMP(column);
        String sql = func.build();
        System.out.println("testUNIX: " + sql);
        Assert.assertEquals("UNIX_TIMESTAMP(create_time)", sql);
    }

    @Test
    public void testUNIX_TIMESTAMPLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getCreateTime;
        IFunction func = Func.dateTime.UNIX_TIMESTAMP("t", column);
        String sql = func.build();
        System.out.println("testUNIX: " + sql);
        Assert.assertEquals("UNIX_TIMESTAMP(t.create_time)", sql);
    }

    @Test
    public void testUNIX_TIMESTAMPWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.UNIX_TIMESTAMP(innerFunc);
        String sql = func.build();
        System.out.println("testUNIX: " + sql);
        Assert.assertEquals("UNIX_TIMESTAMP(NOW())", sql);
    }

    @Test
    public void testGET_FORMAT() {
        IFunction func = Func.dateTime.GET_FORMAT("DATE", "USA");
        String sql = func.build();
        System.out.println("testGET: " + sql);
        Assert.assertEquals("GET_FORMAT(DATE, USA)", sql);
    }

    @Test
    public void testGET_FORMATLambda() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.GET_FORMAT(column, "EUR");
        String sql = func.build();
        System.out.println("testGET: " + sql);
        Assert.assertEquals("GET_FORMAT(birth_date, EUR)", sql);
    }

    @Test
    public void testGET_FORMATLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.GET_FORMAT("t", column, "JIS");
        String sql = func.build();
        System.out.println("testGET: " + sql);
        Assert.assertEquals("GET_FORMAT(t.birth_date, JIS)", sql);
    }

    @Test
    public void testGET_FORMATWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.GET_FORMAT(innerFunc, "ISO");
        String sql = func.build();
        System.out.println("testGET: " + sql);
        Assert.assertEquals("GET_FORMAT(NOW(), ISO)", sql);
    }

    @Test
    public void testHOUR() {
        IFunction func = Func.dateTime.HOUR("12:30:45");
        String sql = func.build();
        System.out.println("testHOUR: " + sql);
        Assert.assertEquals("HOUR(12:30:45)", sql);
    }

    @Test
    public void testHOURLambda() {
        SFunction<TestEntity, String> column = TestEntity::getCreateTime;
        IFunction func = Func.dateTime.HOUR(column);
        String sql = func.build();
        System.out.println("testHOURLambda: " + sql);
        Assert.assertEquals("HOUR(create_time)", sql);
    }

    @Test
    public void testHOURLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getCreateTime;
        IFunction func = Func.dateTime.HOUR("t", column);
        String sql = func.build();
        System.out.println("testHOURLambdaWithPrefix: " + sql);
        Assert.assertEquals("HOUR(t.create_time)", sql);
    }

    @Test
    public void testHOURWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.HOUR(innerFunc);
        String sql = func.build();
        System.out.println("testHOURWithFunction: " + sql);
        Assert.assertEquals("HOUR(NOW())", sql);
    }

    @Test
    public void testLAST_DAY() {
        IFunction func = Func.dateTime.LAST_DAY("2024-02-01");
        String sql = func.build();
        System.out.println("testLAST: " + sql);
        Assert.assertEquals("LAST_DAY(2024-02-01)", sql);
    }

    @Test
    public void testLAST_DAYLambda() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.LAST_DAY(column);
        String sql = func.build();
        System.out.println("testLAST: " + sql);
        Assert.assertEquals("LAST_DAY(birth_date)", sql);
    }

    @Test
    public void testLAST_DAYLambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.LAST_DAY("t", column);
        String sql = func.build();
        System.out.println("testLAST: " + sql);
        Assert.assertEquals("LAST_DAY(t.birth_date)", sql);
    }

    @Test
    public void testLAST_DAYWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.LAST_DAY(innerFunc);
        String sql = func.build();
        System.out.println("testLAST: " + sql);
        Assert.assertEquals("LAST_DAY(NOW())", sql);
    }

    @Test
    public void testMAKEDATE() {
        IFunction func = Func.dateTime.MAKEDATE("2024", "32");
        String sql = func.build();
        System.out.println("testMAKEDATE: " + sql);
        Assert.assertEquals("MAKEDATE(2024, 32)", sql);
    }

    @Test
    public void testMAKEDATELambda() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.MAKEDATE(column, "32");
        String sql = func.build();
        System.out.println("testMAKEDATELambda: " + sql);
        Assert.assertEquals("MAKEDATE(birth_date, 32)", sql);
    }

    @Test
    public void testMAKEDATELambdaReverse() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.MAKEDATE("2024", column);
        String sql = func.build();
        System.out.println("testMAKEDATELambdaReverse: " + sql);
        Assert.assertEquals("MAKEDATE(2024, birth_date)", sql);
    }

    @Test
    public void testMAKEDATELambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.MAKEDATE("t", column, "32");
        String sql = func.build();
        System.out.println("testMAKEDATELambdaWithPrefix: " + sql);
        Assert.assertEquals("MAKEDATE(t.birth_date, 32)", sql);
    }

    @Test
    public void testMAKEDATELambdaReverseWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getBirthDate;
        IFunction func = Func.dateTime.MAKEDATE("2024", "t", column);
        String sql = func.build();
        System.out.println("testMAKEDATELambdaReverseWithPrefix: " + sql);
        Assert.assertEquals("MAKEDATE(2024, t.birth_date)", sql);
    }

    @Test
    public void testMAKEDATEWithFunction() {
        IFunction func1 = Func.dateTime.YEAR("2024-01-01");
        IFunction func2 = Func.dateTime.DAYOFYEAR("2024-02-01");
        IFunction func = Func.dateTime.MAKEDATE(func1, func2);
        String sql = func.build();
        System.out.println("testMAKEDATEWithFunction: " + sql);
        Assert.assertEquals("MAKEDATE(YEAR(2024-01-01), DAYOFYEAR(2024-02-01))", sql);
    }

    @Test
    public void testMAKETIME() {
        IFunction func = Func.dateTime.MAKETIME("12", "30", "45");
        String sql = func.build();
        System.out.println("testMAKETIME: " + sql);
        Assert.assertEquals("MAKETIME(12, 30, 45)", sql);
    }

    @Test
    public void testMAKETIMELambda() {
        SFunction<TestEntity, String> column = TestEntity::getCreateTime;
        IFunction func = Func.dateTime.MAKETIME(column, "30", "45");
        String sql = func.build();
        System.out.println("testMAKETIMELambda: " + sql);
        Assert.assertEquals("MAKETIME(create_time, 30, 45)", sql);
    }

    @Test
    public void testMAKETIMELambdaWithPrefix() {
        SFunction<TestEntity, String> column = TestEntity::getCreateTime;
        IFunction func = Func.dateTime.MAKETIME("t", column, "30", "45");
        String sql = func.build();
        System.out.println("testMAKETIMELambdaWithPrefix: " + sql);
        Assert.assertEquals("MAKETIME(t.create_time, 30, 45)", sql);
    }

    @Test
    public void testMAKETIMEWithFunction() {
        IFunction func1 = Func.dateTime.HOUR("12:30:45");
        IFunction func2 = Func.dateTime.MINUTE("12:30:45");
        IFunction func3 = Func.dateTime.SECOND("12:30:45");
        IFunction func = Func.dateTime.MAKETIME(func1, func2, func3);
        String sql = func.build();
        System.out.println("testMAKETIMEWithFunction: " + sql);
        Assert.assertEquals("MAKETIME(HOUR(12:30:45), MINUTE(12:30:45), SECOND(12:30:45))", sql);
    }

    @Test
    public void testMICROSECOND() {
        IFunction func = Func.dateTime.MICROSECOND("12:30:45.123456");
        String sql = func.build();
        System.out.println("testMICROSECOND: " + sql);
        Assert.assertEquals("MICROSECOND(12:30:45.123456)", sql);
    }

    @Test
    public void testMICROSECONDWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.MICROSECOND(innerFunc);
        String sql = func.build();
        System.out.println("testMICROSECONDWithFunction: " + sql);
        Assert.assertEquals("MICROSECOND(NOW())", sql);
    }

    @Test
    public void testMINUTE() {
        IFunction func = Func.dateTime.MINUTE("12:30:45");
        String sql = func.build();
        System.out.println("testMINUTE: " + sql);
        Assert.assertEquals("MINUTE(12:30:45)", sql);
    }

    @Test
    public void testMINUTEWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.MINUTE(innerFunc);
        String sql = func.build();
        System.out.println("testMINUTEWithFunction: " + sql);
        Assert.assertEquals("MINUTE(NOW())", sql);
    }

    @Test
    public void testMONTH() {
        IFunction func = Func.dateTime.MONTH("2024-01-15");
        String sql = func.build();
        System.out.println("testMONTH: " + sql);
        Assert.assertEquals("MONTH(2024-01-15)", sql);
    }

    @Test
    public void testMONTHWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.MONTH(innerFunc);
        String sql = func.build();
        System.out.println("testMONTHWithFunction: " + sql);
        Assert.assertEquals("MONTH(NOW())", sql);
    }

    @Test
    public void testMONTHNAME() {
        IFunction func = Func.dateTime.MONTHNAME("2024-01-15");
        String sql = func.build();
        System.out.println("testMONTHNAME: " + sql);
        Assert.assertEquals("MONTHNAME(2024-01-15)", sql);
    }

    @Test
    public void testMONTHNAMEWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.MONTHNAME(innerFunc);
        String sql = func.build();
        System.out.println("testMONTHNAMEWithFunction: " + sql);
        Assert.assertEquals("MONTHNAME(NOW())", sql);
    }

    @Test
    public void testNOW() {
        IFunction func = Func.dateTime.NOW();
        String sql = func.build();
        System.out.println("testNOW: " + sql);
        Assert.assertEquals("NOW()", sql);
    }

    @Test
    public void testPERIOD_ADD() {
        IFunction func = Func.dateTime.PERIOD_ADD("202401", "2");
        String sql = func.build();
        System.out.println("testPERIOD: " + sql);
        Assert.assertEquals("PERIOD_ADD(202401, 2)", sql);
    }

    @Test
    public void testPERIOD_ADDWithFunction() {
        IFunction func1 = Func.dateTime.DATE_FORMAT("2024-01-01", "%Y%m");
        IFunction func2 = Func.math.ABS("2");
        IFunction func = Func.dateTime.PERIOD_ADD(func1, func2);
        String sql = func.build();
        System.out.println("testPERIOD: " + sql);
        Assert.assertEquals("PERIOD_ADD(DATE_FORMAT(2024-01-01, %Y%m), ABS(2))", sql);
    }

    @Test
    public void testPERIOD_DIFF() {
        IFunction func = Func.dateTime.PERIOD_DIFF("202403", "202401");
        String sql = func.build();
        System.out.println("testPERIOD: " + sql);
        Assert.assertEquals("PERIOD_DIFF(202403, 202401)", sql);
    }

    @Test
    public void testPERIOD_DIFFWithFunction() {
        IFunction func1 = Func.dateTime.DATE_FORMAT("2024-03-01", "%Y%m");
        IFunction func2 = Func.dateTime.DATE_FORMAT("2024-01-01", "%Y%m");
        IFunction func = Func.dateTime.PERIOD_DIFF(func1, func2);
        String sql = func.build();
        System.out.println("testPERIOD: " + sql);
        Assert.assertEquals("PERIOD_DIFF(DATE_FORMAT(2024-03-01, %Y%m), DATE_FORMAT(2024-01-01, %Y%m))", sql);
    }

    @Test
    public void testQUARTER() {
        IFunction func = Func.dateTime.QUARTER("2024-03-15");
        String sql = func.build();
        System.out.println("testQUARTER: " + sql);
        Assert.assertEquals("QUARTER(2024-03-15)", sql);
    }

    @Test
    public void testQUARTERWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.QUARTER(innerFunc);
        String sql = func.build();
        System.out.println("testQUARTERWithFunction: " + sql);
        Assert.assertEquals("QUARTER(NOW())", sql);
    }

    @Test
    public void testSEC_TO_TIME() {
        IFunction func = Func.dateTime.SEC_TO_TIME("3661");
        String sql = func.build();
        System.out.println("testSEC: " + sql);
        Assert.assertEquals("SEC_TO_TIME(3661)", sql);
    }

    @Test
    public void testSEC_TO_TIMEWithFunction() {
        IFunction innerFunc = Func.math.ABS("3661");
        IFunction func = Func.dateTime.SEC_TO_TIME(innerFunc);
        String sql = func.build();
        System.out.println("testSEC: " + sql);
        Assert.assertEquals("SEC_TO_TIME(ABS(3661))", sql);
    }

    @Test
    public void testSECOND() {
        IFunction func = Func.dateTime.SECOND("12:30:45");
        String sql = func.build();
        System.out.println("testSECOND: " + sql);
        Assert.assertEquals("SECOND(12:30:45)", sql);
    }

    @Test
    public void testSECONDWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.SECOND(innerFunc);
        String sql = func.build();
        System.out.println("testSECONDWithFunction: " + sql);
        Assert.assertEquals("SECOND(NOW())", sql);
    }

    @Test
    public void testSTR_TO_DATE() {
        IFunction func = Func.dateTime.STR_TO_DATE("01,5,2024", "%d,%m,%Y");
        String sql = func.build();
        System.out.println("testSTR: " + sql);
        Assert.assertEquals("STR_TO_DATE(01,5,2024, %d,%m,%Y)", sql);
    }

    @Test
    public void testSTR_TO_DATEWithFunction() {
        IFunction innerFunc = Func.strings.CONCAT("01", "-", "05", "-", "2024");
        IFunction func = Func.dateTime.STR_TO_DATE(innerFunc, "%d-%m-%Y");
        String sql = func.build();
        System.out.println("testSTR: " + sql);
        Assert.assertEquals("STR_TO_DATE(CONCAT(01, -, 05, -, 2024), %d-%m-%Y)", sql);
    }

    @Test
    public void testSYSDATE() {
        IFunction func = Func.dateTime.SYSDATE();
        String sql = func.build();
        System.out.println("testSYSDATE: " + sql);
        Assert.assertEquals("SYSDATE()", sql);
    }

    @Test
    public void testTIME() {
        IFunction func = Func.dateTime.TIME("2024-01-01 12:30:45");
        String sql = func.build();
        System.out.println("testTIME: " + sql);
        Assert.assertEquals("TIME(2024-01-01 12:30:45)", sql);
    }

    @Test
    public void testTIMEWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.TIME(innerFunc);
        String sql = func.build();
        System.out.println("testTIMEWithFunction: " + sql);
        Assert.assertEquals("TIME(NOW())", sql);
    }

    @Test
    public void testTIME_FORMAT() {
        IFunction func = Func.dateTime.TIME_FORMAT("12:30:45", "%H:%i:%s");
        String sql = func.build();
        System.out.println("testTIME: " + sql);
        Assert.assertEquals("TIME_FORMAT(12:30:45, %H:%i:%s)", sql);
    }

    @Test
    public void testTIME_FORMATWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.TIME_FORMAT(innerFunc, "%H:%i");
        String sql = func.build();
        System.out.println("testTIME: " + sql);
        Assert.assertEquals("TIME_FORMAT(NOW(), %H:%i)", sql);
    }

    @Test
    public void testTIME_TO_SEC() {
        IFunction func = Func.dateTime.TIME_TO_SEC("01:01:01");
        String sql = func.build();
        System.out.println("testTIME: " + sql);
        Assert.assertEquals("TIME_TO_SEC(01:01:01)", sql);
    }

    @Test
    public void testTIME_TO_SECWithFunction() {
        IFunction innerFunc = Func.dateTime.CURTIME();
        IFunction func = Func.dateTime.TIME_TO_SEC(innerFunc);
        String sql = func.build();
        System.out.println("testTIME: " + sql);
        Assert.assertEquals("TIME_TO_SEC(CURTIME())", sql);
    }

    @Test
    public void testTIMEDIFF() {
        IFunction func = Func.dateTime.TIMEDIFF("2024-01-01 12:30:45", "2024-01-01 12:00:00");
        String sql = func.build();
        System.out.println("testTIMEDIFF: " + sql);
        Assert.assertEquals("TIMEDIFF(2024-01-01 12:30:45, 2024-01-01 12:00:00)", sql);
    }

    @Test
    public void testTIMEDIFFWithFunction() {
        IFunction func1 = Func.dateTime.NOW();
        IFunction func2 = Func.dateTime.CURTIME();
        IFunction func = Func.dateTime.TIMEDIFF(func1, func2);
        String sql = func.build();
        System.out.println("testTIMEDIFFWithFunction: " + sql);
        Assert.assertEquals("TIMEDIFF(NOW(), CURTIME())", sql);
    }

    @Test
    public void testTIMESTAMP() {
        IFunction func = Func.dateTime.TIMESTAMP("2024-01-01");
        String sql = func.build();
        System.out.println("testTIMESTAMP: " + sql);
        Assert.assertEquals("TIMESTAMP(2024-01-01)", sql);
    }

    @Test
    public void testTIMESTAMPWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.TIMESTAMP(innerFunc);
        String sql = func.build();
        System.out.println("testTIMESTAMPWithFunction: " + sql);
        Assert.assertEquals("TIMESTAMP(NOW())", sql);
    }

    @Test
    public void testTIMESTAMPTwoParams() {
        IFunction func = Func.dateTime.TIMESTAMP("2024-01-01", "12:30:45");
        String sql = func.build();
        System.out.println("testTIMESTAMPTwoParams: " + sql);
        Assert.assertEquals("TIMESTAMP(2024-01-01, 12:30:45)", sql);
    }

    @Test
    public void testTIMESTAMPTwoParamsWithFunction() {
        IFunction func1 = Func.dateTime.DATE("2024-01-01");
        IFunction func2 = Func.dateTime.CURTIME();
        IFunction func = Func.dateTime.TIMESTAMP(func1, func2);
        String sql = func.build();
        System.out.println("testTIMESTAMPTwoParamsWithFunction: " + sql);
        Assert.assertEquals("TIMESTAMP(DATE(2024-01-01), CURTIME())", sql);
    }

    @Test
    public void testTIMESTAMPDIFF() {
        IFunction func = Func.dateTime.TIMESTAMPDIFF("DAY", "2024-01-10", "2024-01-01");
        String sql = func.build();
        System.out.println("testTIMESTAMPDIFF: " + sql);
        Assert.assertEquals("TIMESTAMPDIFF(DAY, 2024-01-10, 2024-01-01)", sql);
    }

    @Test
    public void testTIMESTAMPDIFFWithFunction() {
        IFunction func1 = Func.dateTime.NOW();
        IFunction func2 = Func.dateTime.DATE("2024-01-01");
        IFunction func = Func.dateTime.TIMESTAMPDIFF("HOUR", func1, func2);
        String sql = func.build();
        System.out.println("testTIMESTAMPDIFFWithFunction: " + sql);
        Assert.assertEquals("TIMESTAMPDIFF(HOUR, NOW(), DATE(2024-01-01))", sql);
    }

    @Test
    public void testTO_DAYS() {
        IFunction func = Func.dateTime.TO_DAYS("2024-01-01");
        String sql = func.build();
        System.out.println("testTO: " + sql);
        Assert.assertEquals("TO_DAYS(2024-01-01)", sql);
    }

    @Test
    public void testTO_DAYSWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.TO_DAYS(innerFunc);
        String sql = func.build();
        System.out.println("testTO: " + sql);
        Assert.assertEquals("TO_DAYS(NOW())", sql);
    }

    @Test
    public void testUTC_DATE() {
        IFunction func = Func.dateTime.UTC_DATE();
        String sql = func.build();
        System.out.println("testUTC: " + sql);
        Assert.assertEquals("UTC_DATE()", sql);
    }

    @Test
    public void testUTC_TIME() {
        IFunction func = Func.dateTime.UTC_TIME();
        String sql = func.build();
        System.out.println("testUTC: " + sql);
        Assert.assertEquals("UTC_TIME()", sql);
    }

    @Test
    public void testUTC_TIMESTAMP() {
        IFunction func = Func.dateTime.UTC_TIMESTAMP();
        String sql = func.build();
        System.out.println("testUTC: " + sql);
        Assert.assertEquals("UTC_TIMESTAMP()", sql);
    }

    @Test
    public void testWEEK() {
        IFunction func = Func.dateTime.WEEK("2024-01-01");
        String sql = func.build();
        System.out.println("testWEEK: " + sql);
        Assert.assertEquals("WEEK(2024-01-01)", sql);
    }

    @Test
    public void testWEEKWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.WEEK(innerFunc);
        String sql = func.build();
        System.out.println("testWEEKWithFunction: " + sql);
        Assert.assertEquals("WEEK(NOW())", sql);
    }

    @Test
    public void testWEEKWithMode() {
        IFunction func = Func.dateTime.WEEK("2024-01-01", 3);
        String sql = func.build();
        System.out.println("testWEEKWithMode: " + sql);
        Assert.assertEquals("WEEK(2024-01-01, 3)", sql);
    }

    @Test
    public void testWEEKWithFunctionAndMode() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.WEEK(innerFunc, 3);
        String sql = func.build();
        System.out.println("testWEEKWithFunctionAndMode: " + sql);
        Assert.assertEquals("WEEK(NOW(), 3)", sql);
    }

    @Test
    public void testWEEKDAY() {
        IFunction func = Func.dateTime.WEEKDAY("2024-01-01");
        String sql = func.build();
        System.out.println("testWEEKDAY: " + sql);
        Assert.assertEquals("WEEKDAY(2024-01-01)", sql);
    }

    @Test
    public void testWEEKDAYWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.WEEKDAY(innerFunc);
        String sql = func.build();
        System.out.println("testWEEKDAYWithFunction: " + sql);
        Assert.assertEquals("WEEKDAY(NOW())", sql);
    }

    @Test
    public void testWEEKOFYEAR() {
        IFunction func = Func.dateTime.WEEKOFYEAR("2024-01-01");
        String sql = func.build();
        System.out.println("testWEEKOFYEAR: " + sql);
        Assert.assertEquals("WEEKOFYEAR(2024-01-01)", sql);
    }

    @Test
    public void testWEEKOFYEARWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.WEEKOFYEAR(innerFunc);
        String sql = func.build();
        System.out.println("testWEEKOFYEARWithFunction: " + sql);
        Assert.assertEquals("WEEKOFYEAR(NOW())", sql);
    }

    @Test
    public void testYEAR() {
        IFunction func = Func.dateTime.YEAR("2024-01-01");
        String sql = func.build();
        System.out.println("testYEAR: " + sql);
        Assert.assertEquals("YEAR(2024-01-01)", sql);
    }

    @Test
    public void testYEARWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.YEAR(innerFunc);
        String sql = func.build();
        System.out.println("testYEARWithFunction: " + sql);
        Assert.assertEquals("YEAR(NOW())", sql);
    }

    @Test
    public void testYEARWEEK() {
        IFunction func = Func.dateTime.YEARWEEK("2024-01-01");
        String sql = func.build();
        System.out.println("testYEARWEEK: " + sql);
        Assert.assertEquals("YEARWEEK(2024-01-01)", sql);
    }

    @Test
    public void testYEARWEEKWithFunction() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.YEARWEEK(innerFunc);
        String sql = func.build();
        System.out.println("testYEARWEEKWithFunction: " + sql);
        Assert.assertEquals("YEARWEEK(NOW())", sql);
    }

    @Test
    public void testYEARWEEKWithMode() {
        IFunction func = Func.dateTime.YEARWEEK("2024-01-01", 3);
        String sql = func.build();
        System.out.println("testYEARWEEKWithMode: " + sql);
        Assert.assertEquals("YEARWEEK(2024-01-01, 3)", sql);
    }

    @Test
    public void testYEARWEEKWithFunctionAndMode() {
        IFunction innerFunc = Func.dateTime.NOW();
        IFunction func = Func.dateTime.YEARWEEK(innerFunc, 3);
        String sql = func.build();
        System.out.println("testYEARWEEKWithFunctionAndMode: " + sql);
        Assert.assertEquals("YEARWEEK(NOW(), 3)", sql);
    }

    @Test
    public void testComplexDateTimeFunction() {
        IFunction func = Func.dateTime.DATE_FORMAT(
                Func.dateTime.ADDDATE(
                        Func.dateTime.NOW(),
                        7
                ),
                "%Y-%m-%d"
        );
        String sql = func.build();
        System.out.println("testComplexDateTimeFunction: " + sql);
        Assert.assertEquals("DATE_FORMAT(ADDDATE(NOW(), 7), %Y-%m-%d)", sql);
    }
}
