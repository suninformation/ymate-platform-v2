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
package net.ymate.platform.commons.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * UUIDUtils类的单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2022/6/5 23:02
 * @since 2.1.2
 */
public class UUIDUtilsTest {

    /**
     * 测试generateCharUUID方法
     * - 正常场景：传入对象生成UUID
     * - 边界条件：传入null
     * - 验证：返回的UUID长度和格式
     */
    @Test
    public void testGenerateCharUUID() {
        // 正常场景测试
        String uuid1 = UUIDUtils.generateCharUUID(this);
        String uuid2 = UUIDUtils.generateCharUUID(this);
        Assert.assertNotNull(uuid1);
        Assert.assertNotNull(uuid2);
        Assert.assertNotEquals(uuid1, uuid2); // 不同调用应生成不同UUID

        // 边界条件测试：传入null
        String uuid3 = UUIDUtils.generateCharUUID(null);
        Assert.assertNotNull(uuid3);
    }

    /**
     * 测试generateNumberUUID方法
     * - 正常场景：传入对象生成UUID
     * - 边界条件：传入null
     * - 验证：返回的UUID格式
     */
    @Test
    public void testGenerateNumberUUID() {
        // 正常场景测试
        String uuid1 = UUIDUtils.generateNumberUUID(this);
        String uuid2 = UUIDUtils.generateNumberUUID(this);
        Assert.assertNotNull(uuid1);
        Assert.assertNotNull(uuid2);
        Assert.assertNotEquals(uuid1, uuid2); // 不同调用应生成不同UUID

        // 验证返回值仅包含数字
        Assert.assertTrue(uuid1.matches("\\d+"));

        // 边界条件测试：传入null
        String uuid3 = UUIDUtils.generateNumberUUID(null);
        Assert.assertNotNull(uuid3);
        Assert.assertTrue(uuid3.matches("\\d+"));
    }

    /**
     * 测试generatePrefixHostUUID方法
     * - 正常场景：传入对象生成带主机名前缀的UUID
     * - 边界条件：传入null
     * - 验证：返回的UUID格式和主机名前缀
     */
    @Test
    public void testGeneratePrefixHostUUID() {
        // 正常场景测试
        String uuid1 = UUIDUtils.generatePrefixHostUUID(this);
        String uuid2 = UUIDUtils.generatePrefixHostUUID(this);
        Assert.assertNotNull(uuid1);
        Assert.assertNotNull(uuid2);
        Assert.assertNotEquals(uuid1, uuid2); // 不同调用应生成不同UUID

        // 验证返回值包含主机名前缀和@符号
        Assert.assertTrue(uuid1.contains("@"));

        // 边界条件测试：传入null
        String uuid3 = UUIDUtils.generatePrefixHostUUID(null);
        Assert.assertNotNull(uuid3);
        Assert.assertTrue(uuid3.contains("@"));
    }

    /**
     * 测试generateRandomUUID方法
     * - 正常场景：生成随机UUID
     * - 验证：返回的UUID长度和格式
     */
    @Test
    public void testGenerateRandomUUID() {
        // 正常场景测试
        String uuid1 = UUIDUtils.generateRandomUUID();
        String uuid2 = UUIDUtils.generateRandomUUID();
        Assert.assertNotNull(uuid1);
        Assert.assertNotNull(uuid2);
        Assert.assertNotEquals(uuid1, uuid2); // 不同调用应生成不同UUID
    }

    /**
     * 测试randomStr方法
     * - 正常场景：生成指定长度的随机字符串
     * - 边界条件：长度为1，长度较大
     * - 特殊情况：仅使用数字
     * - 异常处理：长度为0或负数
     */
    @Test
    public void testRandomStr() {
        // 正常场景测试：生成10位包含字母和数字的随机字符串
        String str1 = UUIDUtils.randomStr(10, false);
        Assert.assertNotNull(str1);
        Assert.assertEquals(10, str1.length());

        // 正常场景测试：生成10位仅包含数字的随机字符串
        String str2 = UUIDUtils.randomStr(10, true);
        Assert.assertNotNull(str2);
        Assert.assertEquals(10, str2.length());
        Assert.assertTrue(str2.matches("\\d{10}"));

        // 边界条件测试：长度为1
        String str3 = UUIDUtils.randomStr(1, false);
        Assert.assertNotNull(str3);
        Assert.assertEquals(1, str3.length());

        // 边界条件测试：长度较大
        String str4 = UUIDUtils.randomStr(100, false);
        Assert.assertNotNull(str4);
        Assert.assertEquals(100, str4.length());

        // 异常处理测试：长度为0
        try {
            UUIDUtils.randomStr(0, false);
            Assert.fail("应该抛出IllegalArgumentException异常");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("length must be positive", e.getMessage());
        }

        // 异常处理测试：长度为负数
        try {
            UUIDUtils.randomStr(-10, false);
            Assert.fail("应该抛出IllegalArgumentException异常");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("length must be positive", e.getMessage());
        }
    }

    /**
     * 测试randomLong方法
     * - 正常场景：生成指定范围内的随机长整数
     * - 边界条件：min等于max
     * - 边界条件：min为Long.MIN_VALUE，max为Long.MAX_VALUE
     * - 异常处理：min大于max
     */
    @Test
    public void testRandomLong() {
        long min = 100L;
        long max = 200L;

        // 正常场景测试：生成指定范围内的随机长整数
        for (int i = 0; i < 100; i++) {
            long random = UUIDUtils.randomLong(min, max);
            Assert.assertTrue(random >= min && random <= max);
        }

        // 边界条件测试：min等于max
        long fixedValue = 1000L;
        for (int i = 0; i < 10; i++) {
            long random = UUIDUtils.randomLong(fixedValue, fixedValue);
            Assert.assertEquals(fixedValue, random);
        }

        // 异常处理测试：min大于max
        try {
            UUIDUtils.randomLong(max, min);
            Assert.fail("应该抛出IllegalArgumentException异常");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("min must be less than or equal to max", e.getMessage());
        }
    }

    /**
     * 测试randomInt方法
     * - 正常场景：生成指定范围内的随机整数
     * - 边界条件：min等于max
     * - 边界条件：min为Integer.MIN_VALUE，max为Integer.MAX_VALUE
     * - 异常处理：min大于max
     */
    @Test
    public void testRandomInt() {
        int min = 100;
        int max = 200;

        // 正常场景测试：生成指定范围内的随机整数
        for (int i = 0; i < 100; i++) {
            int random = UUIDUtils.randomInt(min, max);
            Assert.assertTrue(random >= min && random <= max);
        }

        // 边界条件测试：min等于max
        int fixedValue = 1000;
        for (int i = 0; i < 10; i++) {
            int random = UUIDUtils.randomInt(fixedValue, fixedValue);
            Assert.assertEquals(fixedValue, random);
        }

        // 异常处理测试：min大于max
        try {
            UUIDUtils.randomInt(max, min);
            Assert.fail("应该抛出IllegalArgumentException异常");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("min must be less than or equal to max", e.getMessage());
        }
    }

    /**
     * 测试uuid方法
     * - 正常场景：生成JDK标准UUID
     * - 验证：返回的UUID格式和有效性
     */
    @Test
    public void testUuid() {
        // 正常场景测试
        String uuid1 = UUIDUtils.UUID();
        String uuid2 = UUIDUtils.UUID();
        Assert.assertNotNull(uuid1);
        Assert.assertNotNull(uuid2);
        Assert.assertNotEquals(uuid1, uuid2); // 不同调用应生成不同UUID

        // 验证返回值不包含'-'
        Assert.assertFalse(uuid1.contains("-"));
    }

    /**
     * 测试UUID唯一性
     * - 验证：大量调用生成的UUID均不重复
     */
    @Test
    public void testUUIDUniqueness() {
        int testCount = 10000;
        java.util.Set<String> uuidSet = new java.util.HashSet<>();

        // 测试generateCharUUID方法的唯一性
        for (int i = 0; i < testCount; i++) {
            String uuid = UUIDUtils.generateCharUUID(this);
            uuidSet.add(uuid);
        }
        Assert.assertEquals("generateCharUUID方法生成了重复的UUID", testCount, uuidSet.size());
        uuidSet.clear();

        // 测试generateNumberUUID方法的唯一性
        for (int i = 0; i < testCount; i++) {
            String uuid = UUIDUtils.generateNumberUUID(this);
            uuidSet.add(uuid);
        }
        Assert.assertEquals("generateNumberUUID方法生成了重复的UUID", testCount, uuidSet.size());
        uuidSet.clear();

        // 测试generateRandomUUID方法的唯一性
        for (int i = 0; i < testCount; i++) {
            String uuid = UUIDUtils.generateRandomUUID();
            uuidSet.add(uuid);
        }
        Assert.assertEquals("generateRandomUUID方法生成了重复的UUID", testCount, uuidSet.size());
        uuidSet.clear();

        // 测试uuid方法的唯一性
        for (int i = 0; i < testCount; i++) {
            String uuid = UUIDUtils.UUID();
            uuidSet.add(uuid);
        }
        Assert.assertEquals("uuid方法生成了重复的UUID", testCount, uuidSet.size());
    }
}
