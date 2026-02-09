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
package net.ymate.platform.commons.lang;

import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.lang.converter.JsonWrapperConverter;
import net.ymate.platform.commons.lang.converter.TimestampConverter;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.Assert.*;

/**
 * BlurObject单元测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-05 15:25:40
 * @since 2.1.4
 */
public class BlurObjectTest {

    @Test
    public void testBindAndConstructor() {
        // 测试静态bind方法
        BlurObject blurObject1 = BlurObject.bind("test");
        assertNotNull(blurObject1);
        assertEquals("test", blurObject1.toStringValue());

        // 测试构造方法
        BlurObject blurObject2 = new BlurObject(123);
        assertNotNull(blurObject2);
        assertEquals(123, blurObject2.toIntValue());

        // 测试null值
        BlurObject blurObject3 = BlurObject.bind(null);
        assertNotNull(blurObject3);
        assertNull(blurObject3.toObjectValue());
    }

    @Test
    public void testToObjectValue() {
        Object original = "original";
        BlurObject blurObject = BlurObject.bind(original);
        assertSame(original, blurObject.toObjectValue());

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.toObjectValue());
    }

    @Test
    public void testToBlurObjectValue() {
        // 绑定原始对象
        BlurObject blurObject1 = BlurObject.bind("test");
        BlurObject result1 = blurObject1.toBlurObjectValue();
        assertSame(blurObject1, result1);

        // 绑定BlurObject对象
        BlurObject blurObject2 = BlurObject.bind(blurObject1);
        BlurObject result2 = blurObject2.toBlurObjectValue();
        assertSame(blurObject1, result2);
    }

    @Test
    public void testToMapValue() {
        // 测试Map类型
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 123);
        BlurObject blurObject = BlurObject.bind(map);
        Map<?, ?> result = blurObject.toMapValue();
        assertSame(map, result);
        assertEquals(2, result.size());

        // 测试非Map类型
        blurObject = BlurObject.bind("not a map");
        result = blurObject.toMapValue();
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // 测试null值
        blurObject = BlurObject.bind(null);
        result = blurObject.toMapValue();
        assertNull(result);
    }

    @Test
    public void testToListValue() {
        // 测试List类型
        List<String> list = Arrays.asList("a", "b", "c");
        BlurObject blurObject = BlurObject.bind(list);
        List<?> result = blurObject.toListValue();
        assertSame(list, result);
        assertEquals(3, result.size());

        // 测试Collection类型
        Set<String> set = new HashSet<>(Arrays.asList("x", "y", "z"));
        blurObject = BlurObject.bind(set);
        result = blurObject.toListValue();
        assertNotSame(set, result);
        assertTrue(result instanceof ArrayList);
        assertEquals(3, result.size());

        // 测试其他类型
        blurObject = BlurObject.bind("single");
        result = blurObject.toListValue();
        assertTrue(result instanceof ArrayList);
        assertEquals(1, result.size());
        assertEquals("single", result.get(0));

        // 测试null值
        blurObject = BlurObject.bind(null);
        result = blurObject.toListValue();
        assertNull(result);
    }

    @Test
    public void testToSetValue() {
        // 测试Set类型
        Set<String> set = new HashSet<>(Arrays.asList("a", "b", "c"));
        BlurObject blurObject = BlurObject.bind(set);
        Set<?> result = blurObject.toSetValue();
        assertSame(set, result);
        assertEquals(3, result.size());

        // 测试Collection类型
        List<String> list = Arrays.asList("x", "y", "z");
        blurObject = BlurObject.bind(list);
        result = blurObject.toSetValue();
        assertNotSame(list, result);
        assertTrue(result instanceof HashSet);
        assertEquals(3, result.size());

        // 测试其他类型
        blurObject = BlurObject.bind("single");
        result = blurObject.toSetValue();
        assertTrue(result instanceof HashSet);
        assertEquals(1, result.size());
        assertEquals("single", result.iterator().next());

        // 测试null值
        blurObject = BlurObject.bind(null);
        result = blurObject.toSetValue();
        assertNull(result);
    }

    @Test
    public void testToBoolean() {
        // 测试Boolean类型
        BlurObject blurObject = BlurObject.bind(true);
        assertTrue(blurObject.toBoolean());
        assertTrue(blurObject.toBooleanValue());

        blurObject = BlurObject.bind(false);
        assertFalse(blurObject.toBoolean());
        assertFalse(blurObject.toBooleanValue());

        // 测试字符串类型
        blurObject = BlurObject.bind("true");
        assertTrue(blurObject.toBoolean());

        blurObject = BlurObject.bind("TRUE");
        assertTrue(blurObject.toBoolean());

        blurObject = BlurObject.bind("on");
        assertTrue(blurObject.toBoolean());

        blurObject = BlurObject.bind("1");
        assertTrue(blurObject.toBoolean());

        blurObject = BlurObject.bind("false");
        assertFalse(blurObject.toBoolean());

        // 测试数值类型
        blurObject = BlurObject.bind(1);
        assertTrue(blurObject.toBoolean());

        blurObject = BlurObject.bind(0);
        assertFalse(blurObject.toBoolean());

        blurObject = BlurObject.bind(1.5);
        assertTrue(blurObject.toBoolean());

        // 测试集合类型
        blurObject = BlurObject.bind(Arrays.asList(1, 2, 3));
        assertTrue(blurObject.toBoolean());

        blurObject = BlurObject.bind(new ArrayList<>());
        assertFalse(blurObject.toBoolean());

        // 测试Map类型
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        blurObject = BlurObject.bind(map);
        assertTrue(blurObject.toBoolean());

        blurObject = BlurObject.bind(new HashMap<>());
        assertFalse(blurObject.toBoolean());

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.toBoolean());
        assertFalse(blurObject.toBooleanValue());
    }

    @Test
    public void testToInteger() {
        // 测试Integer类型
        BlurObject blurObject = BlurObject.bind(123);
        assertEquals(123, blurObject.toInteger().intValue());
        assertEquals(123, blurObject.toIntValue());

        // 测试其他数值类型
        blurObject = BlurObject.bind(456L);
        assertEquals(456, blurObject.toInteger().intValue());

        blurObject = BlurObject.bind(78.9);
        assertEquals(78, blurObject.toInteger().intValue());

        // 测试字符串类型
        blurObject = BlurObject.bind("100");
        assertEquals(100, blurObject.toInteger().intValue());

        blurObject = BlurObject.bind("invalid");
        assertNull(blurObject.toInteger());
        assertEquals(0, blurObject.toIntValue());

        // 测试布尔类型
        blurObject = BlurObject.bind(true);
        assertEquals(1, blurObject.toInteger().intValue());

        blurObject = BlurObject.bind(false);
        assertEquals(0, blurObject.toInteger().intValue());

        // 测试集合类型
        blurObject = BlurObject.bind(Arrays.asList(1, 2, 3));
        assertEquals(3, blurObject.toInteger().intValue());

        // 测试Map类型
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        blurObject = BlurObject.bind(map);
        assertEquals(2, blurObject.toInteger().intValue());

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.toInteger());
        assertEquals(0, blurObject.toIntValue());
    }

    @Test
    public void testToStringValue() {
        // 测试String类型
        BlurObject blurObject = BlurObject.bind("test");
        assertEquals("test", blurObject.toStringValue());
        assertEquals("test", blurObject.toString());

        // 测试char数组
        char[] charArray = {'a', 'b', 'c'};
        blurObject = BlurObject.bind(charArray);
        assertEquals("abc", blurObject.toStringValue());

        // 测试Character数组
        Character[] characterArray = {'x', 'y', 'z'};
        blurObject = BlurObject.bind(characterArray);
        assertEquals("xyz", blurObject.toStringValue());

        // 测试Object数组
        Object[] objectArray = {1, "two", 3.0};
        blurObject = BlurObject.bind(objectArray);
        assertEquals("1|two|3.0", blurObject.toStringValue());

        // 测试数值类型
        blurObject = BlurObject.bind(123);
        assertEquals("123", blurObject.toStringValue());

        blurObject = BlurObject.bind(123.45);
        assertEquals("123.45", blurObject.toStringValue());

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.toStringValue());
        assertNull(blurObject.toString());
    }

    @Test
    public void testToFloat() {
        // 测试Float类型
        BlurObject blurObject = BlurObject.bind(123.45f);
        assertEquals(123.45f, blurObject.toFloat(), 0.0001f);
        assertEquals(123.45f, blurObject.toFloatValue(), 0.0001f);

        // 测试其他数值类型
        blurObject = BlurObject.bind(678);
        assertEquals(678.0f, blurObject.toFloat(), 0.0001f);

        blurObject = BlurObject.bind(901.23);
        assertEquals(901.23f, blurObject.toFloat(), 0.0001f);

        // 测试字符串类型
        blurObject = BlurObject.bind("123.45");
        assertEquals(123.45f, blurObject.toFloat(), 0.0001f);

        blurObject = BlurObject.bind("invalid");
        assertNull(blurObject.toFloat());
        assertEquals(0.0f, blurObject.toFloatValue(), 0.0001f);

        // 测试布尔类型
        blurObject = BlurObject.bind(true);
        assertEquals(1.0f, blurObject.toFloat(), 0.0001f);

        blurObject = BlurObject.bind(false);
        assertEquals(0.0f, blurObject.toFloat(), 0.0001f);

        // 测试集合类型
        blurObject = BlurObject.bind(Arrays.asList(1, 2, 3, 4));
        assertEquals(4.0f, blurObject.toFloat(), 0.0001f);

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.toFloat());
        assertEquals(0.0f, blurObject.toFloatValue(), 0.0001f);
    }

    @Test
    public void testToDouble() {
        // 测试Double类型
        BlurObject blurObject = BlurObject.bind(123.45);
        assertEquals(123.45, blurObject.toDouble(), 0.0001);
        assertEquals(123.45, blurObject.toDoubleValue(), 0.0001);

        // 测试其他数值类型
        blurObject = BlurObject.bind(678);
        assertEquals(678.0, blurObject.toDouble(), 0.0001);

        blurObject = BlurObject.bind(901.23f);
        assertEquals(901.23f, blurObject.toDouble(), 0.0001);

        // 测试字符串类型
        blurObject = BlurObject.bind("123.45");
        assertEquals(123.45, blurObject.toDouble(), 0.0001);

        blurObject = BlurObject.bind("invalid");
        assertNull(blurObject.toDouble());
        assertEquals(0.0, blurObject.toDoubleValue(), 0.0001);

        // 测试布尔类型
        blurObject = BlurObject.bind(true);
        assertEquals(1.0, blurObject.toDouble(), 0.0001);

        blurObject = BlurObject.bind(false);
        assertEquals(0.0, blurObject.toDouble(), 0.0001);

        // 测试集合类型
        blurObject = BlurObject.bind(Arrays.asList(1, 2, 3, 4, 5));
        assertEquals(5.0, blurObject.toDouble(), 0.0001);

        // 测试Map类型
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        blurObject = BlurObject.bind(map);
        assertEquals(3.0, blurObject.toDouble(), 0.0001);

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.toDouble());
        assertEquals(0.0, blurObject.toDoubleValue(), 0.0001);
    }

    @Test
    public void testToLong() {
        // 测试Long类型
        BlurObject blurObject = BlurObject.bind(123456789L);
        assertEquals(123456789L, blurObject.toLong().longValue());
        assertEquals(123456789L, blurObject.toLongValue());

        // 测试其他数值类型
        blurObject = BlurObject.bind(987);
        assertEquals(987L, blurObject.toLong().longValue());

        blurObject = BlurObject.bind(654.321);
        assertEquals(654L, blurObject.toLong().longValue());

        // 测试Date类型
        Date date = new Date(1000000000000L);
        blurObject = BlurObject.bind(date);
        assertEquals(1000000000000L, blurObject.toLong().longValue());

        // 测试Calendar类型
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        blurObject = BlurObject.bind(calendar);
        assertEquals(1000000000000L, blurObject.toLong().longValue());

        // 测试LocalDate类型
        LocalDate localDate = LocalDate.of(2023, 1, 1);
        blurObject = BlurObject.bind(localDate);
        // 测试转换为Long类型
        assertNotNull(blurObject.toLong());

        // 测试LocalDateTime类型
        LocalDateTime localDateTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        blurObject = BlurObject.bind(localDateTime);
        // 测试转换为Long类型
        assertNotNull(blurObject.toLong());

        // 测试ZonedDateTime类型
        ZonedDateTime zonedDateTime = ZonedDateTime.of(2023, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        blurObject = BlurObject.bind(zonedDateTime);
        // 验证转换结果与直接获取时间戳一致
        assertEquals(zonedDateTime.toInstant().toEpochMilli(), blurObject.toLong().longValue());

        // 测试字符串类型
        blurObject = BlurObject.bind("123456789");
        assertEquals(123456789L, blurObject.toLong().longValue());

        blurObject = BlurObject.bind("invalid");
        assertNull(blurObject.toLong());
        assertEquals(0L, blurObject.toLongValue());

        // 测试布尔类型
        blurObject = BlurObject.bind(true);
        assertEquals(1L, blurObject.toLong().longValue());

        // 测试集合类型
        blurObject = BlurObject.bind(Arrays.asList(1, 2, 3));
        assertEquals(3L, blurObject.toLong().longValue());

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.toLong());
        assertEquals(0L, blurObject.toLongValue());
    }

    @Test
    public void testToByte() {
        // 测试Byte类型
        BlurObject blurObject = BlurObject.bind((byte) 100);
        assertEquals((byte) 100, blurObject.toByte().byteValue());
        assertEquals((byte) 100, blurObject.toByteValue());

        // 测试其他数值类型
        blurObject = BlurObject.bind(20);
        assertEquals((byte) 20, blurObject.toByte().byteValue());

        // 测试字符串类型
        blurObject = BlurObject.bind("50");
        assertEquals((byte) 50, blurObject.toByte().byteValue());

        blurObject = BlurObject.bind("invalid");
        assertNull(blurObject.toByte());
        assertEquals((byte) 0, blurObject.toByteValue());

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.toByte());
        assertEquals((byte) 0, blurObject.toByteValue());
    }

    @Test
    public void testToBytes() {
        // 测试byte数组
        byte[] byteArray = {1, 2, 3, 4, 5};
        BlurObject blurObject = BlurObject.bind(byteArray);
        assertSame(byteArray, blurObject.toBytesValue());
        assertArrayEquals(byteArray, blurObject.toBytesValue());

        // 测试Byte数组
        Byte[] byteObjectArray = {6, 7, 8, 9, 10};
        blurObject = BlurObject.bind(byteObjectArray);
        assertArrayEquals(new byte[]{6, 7, 8, 9, 10}, blurObject.toBytesValue());
        assertArrayEquals(byteObjectArray, blurObject.toBytes());

        // 测试非字节类型
        blurObject = BlurObject.bind("test");
        assertNull(blurObject.toBytesValue());
        assertNull(blurObject.toBytes());

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.toBytesValue());
        assertNull(blurObject.toBytes());
    }

    @Test
    public void testToShort() {
        // 测试Short类型
        BlurObject blurObject = BlurObject.bind((short) 12345);
        assertEquals((short) 12345, blurObject.toShort().shortValue());
        assertEquals((short) 12345, blurObject.toShortValue());

        // 测试其他数值类型
        blurObject = BlurObject.bind(6789);
        assertEquals((short) 6789, blurObject.toShort().shortValue());

        // 测试字符串类型
        blurObject = BlurObject.bind("4321");
        assertEquals((short) 4321, blurObject.toShort().shortValue());

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.toShort());
        assertEquals((short) 0, blurObject.toShortValue());
    }

    @Test
    public void testToChar() {
        // 测试Character类型
        BlurObject blurObject = BlurObject.bind('A');
        assertEquals('A', blurObject.toCharValue());

        // 测试字符串类型
        blurObject = BlurObject.bind("B");
        assertEquals(Character.MIN_CODE_POINT, blurObject.toCharValue());

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertEquals(Character.MIN_CODE_POINT, blurObject.toCharValue());
    }

    @Test
    public void testToChars() {
        // 测试char数组
        char[] charArray = {'a', 'b', 'c'};
        BlurObject blurObject = BlurObject.bind(charArray);
        assertSame(charArray, blurObject.toCharsValue());
        assertArrayEquals(charArray, blurObject.toCharsValue());

        // 测试Character数组
        Character[] charObjectArray = {'x', 'y', 'z'};
        blurObject = BlurObject.bind(charObjectArray);
        assertArrayEquals(new char[]{'x', 'y', 'z'}, blurObject.toCharsValue());
        assertArrayEquals(charObjectArray, blurObject.toCharacters());

        // 测试非字符类型
        blurObject = BlurObject.bind(123);
        assertNull(blurObject.toCharsValue());
        assertNull(blurObject.toCharacters());

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.toCharsValue());
        assertNull(blurObject.toCharacters());
    }

    @Test
    public void testToObjectValueWithClass() {
        // 测试String类型
        BlurObject blurObject = BlurObject.bind("test");
        Object result = blurObject.toObjectValue(String.class);
        assertEquals("test", result);

        // 测试Integer类型
        blurObject = BlurObject.bind(123);
        result = blurObject.toObjectValue(Integer.class);
        assertEquals(123, result);

        // 测试int原始类型
        result = blurObject.toObjectValue(int.class);
        assertEquals(123, result);

        // 测试List类型
        List<String> list = Arrays.asList("a", "b", "c");
        blurObject = BlurObject.bind(list);
        result = blurObject.toObjectValue(List.class);
        assertSame(list, result);

        // 测试Map类型
        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        blurObject = BlurObject.bind(map);
        result = blurObject.toObjectValue(Map.class);
        assertSame(map, result);

        // 测试Set类型
        Set<String> set = new HashSet<>(Arrays.asList("x", "y", "z"));
        blurObject = BlurObject.bind(set);
        result = blurObject.toObjectValue(Set.class);
        assertSame(set, result);

        // 测试BigInteger类型
        blurObject = BlurObject.bind("12345678901234567890");
        result = blurObject.toObjectValue(BigInteger.class);
        assertEquals(new BigInteger("12345678901234567890"), result);

        // 测试BigDecimal类型
        blurObject = BlurObject.bind("123.456789");
        result = blurObject.toObjectValue(BigDecimal.class);
        assertEquals(new BigDecimal("123.456789"), result);

        // 测试null值
        blurObject = BlurObject.bind(null);
        result = blurObject.toObjectValue(String.class);
        assertNull(result);

        // 测试nullable参数
        result = blurObject.toObjectValue(String.class, true);
        assertNull(result);

        result = blurObject.toObjectValue(int.class, true);
        assertEquals(0, result);
    }

    @Test
    public void testGetObjectClass() {
        // 测试字符串类型
        BlurObject blurObject = BlurObject.bind("test");
        assertEquals(String.class, blurObject.getObjectClass());

        // 测试整数类型
        blurObject = BlurObject.bind(123);
        assertEquals(Integer.class, blurObject.getObjectClass());

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.getObjectClass());
    }

    @Test
    public void testEqualsAndHashCode() {
        // 测试相等的对象
        BlurObject blurObject1 = BlurObject.bind("test");
        BlurObject blurObject2 = BlurObject.bind("test");
        assertEquals(blurObject1, blurObject2);
        assertEquals(blurObject1.hashCode(), blurObject2.hashCode());

        // 测试相同的对象
        assertEquals(blurObject1, blurObject1);

        // 测试不同类型的对象
        BlurObject blurObject3 = BlurObject.bind(123);
        assertNotEquals(blurObject1, blurObject3);

        // 测试null值
        BlurObject blurObject4 = BlurObject.bind(null);
        BlurObject blurObject5 = BlurObject.bind(null);
        assertEquals(blurObject4, blurObject5);

        // 测试与null比较
        assertNotEquals(blurObject1, null);

        // 测试与其他类型比较
        assertNotEquals(blurObject1, "test");
    }

    @Test
    public void testToString() {
        // 测试字符串类型
        BlurObject blurObject = BlurObject.bind("test");
        assertEquals("test", blurObject.toString());

        // 测试数值类型
        blurObject = BlurObject.bind(123);
        assertEquals("123", blurObject.toString());

        // 测试null值
        blurObject = BlurObject.bind(null);
        assertNull(blurObject.toString());
    }

    @Test
    public void testNestedBlurObject() {
        // 测试嵌套BlurObject
        BlurObject inner = BlurObject.bind("inner");
        BlurObject outer = BlurObject.bind(inner);

        // 测试toBlurObjectValue方法
        BlurObject result = outer.toBlurObjectValue();
        assertSame(inner, result);

        // 测试转换方法
        assertEquals("inner", outer.toStringValue());
        assertEquals("inner", outer.toBlurObjectValue().toStringValue());
    }

    @Test
    public void testTimestampConverter() {
        // 测试通过registerConverter注册的TimestampConverter进行转换
        // 测试Long类型转换
        BlurObject blurObject1 = BlurObject.bind(System.currentTimeMillis());
        Object result1 = blurObject1.toObjectValue(Timestamp.class);
        assertNotNull(result1);
        assertTrue(result1 instanceof Timestamp);

        // 测试Integer类型转换
        BlurObject blurObject2 = BlurObject.bind(1000);
        Object result2 = blurObject2.toObjectValue(Timestamp.class);
        assertNotNull(result2);
        assertTrue(result2 instanceof Timestamp);
        assertEquals(1000L, ((Timestamp) result2).getTime());

        // 测试Double类型转换
        BlurObject blurObject3 = BlurObject.bind(1000.5);
        Object result3 = blurObject3.toObjectValue(Timestamp.class);
        assertNotNull(result3);
        assertTrue(result3 instanceof Timestamp);
        assertEquals(1000L, ((Timestamp) result3).getTime());

        // 测试Float类型转换
        BlurObject blurObject4 = BlurObject.bind(1000.5f);
        Object result4 = blurObject4.toObjectValue(Timestamp.class);
        assertNotNull(result4);
        assertTrue(result4 instanceof Timestamp);
        assertEquals(1000L, ((Timestamp) result4).getTime());

        // 测试String类型转换
        Timestamp expectedTimestamp = new Timestamp(1000L);
        BlurObject blurObject5 = BlurObject.bind(expectedTimestamp.toString());
        Object result5 = blurObject5.toObjectValue(Timestamp.class);
        assertNotNull(result5);
        assertTrue(result5 instanceof Timestamp);
        assertEquals(expectedTimestamp, result5);

        // 测试空字符串转换
        BlurObject blurObject6 = BlurObject.bind("");
        Object result6 = blurObject6.toObjectValue(Timestamp.class);
        assertNull(result6);

        // 测试null转换
        BlurObject blurObject7 = BlurObject.bind(null);
        Object result7 = blurObject7.toObjectValue(Timestamp.class);
        assertNull(result7);

        // 直接测试TimestampConverter的convert方法
        TimestampConverter converter = new TimestampConverter();
        Timestamp result8 = converter.convert(1000L);
        assertNotNull(result8);
        assertEquals(1000L, result8.getTime());
    }

    @Test
    public void testJsonWrapperConverter() {
        // 测试通过registerConverter注册的JsonWrapperConverter进行转换
        String jsonStr = "{\"name\": \"test\", \"age\": 18}";

        // 测试null转换
        BlurObject blurObject1 = BlurObject.bind(null);
        Object result1 = blurObject1.toObjectValue(JsonWrapper.class);
        assertNull(result1);

        // 测试空字符串转换
        BlurObject blurObject2 = BlurObject.bind("");
        Object result2 = blurObject2.toObjectValue(JsonWrapper.class);
        assertNull(result2);

        // 测试有效JSON字符串转换
        BlurObject blurObject3 = BlurObject.bind(jsonStr);
        Object result3 = blurObject3.toObjectValue(JsonWrapper.class);
        assertNotNull(result3);
        assertTrue(result3 instanceof JsonWrapper);
        JsonWrapper jsonWrapper = (JsonWrapper) result3;
        assertTrue(jsonWrapper.isJsonObject());
        assertEquals("test", jsonWrapper.getAsJsonObject().getString("name"));
        assertEquals(18, jsonWrapper.getAsJsonObject().getAsInteger("age").intValue());

        // 直接测试JsonWrapperConverter的convert方法
        JsonWrapperConverter converter = new JsonWrapperConverter();
        JsonWrapper result4 = converter.convert(jsonStr);
        assertNotNull(result4);
        assertTrue(result4.isJsonObject());
        assertEquals("test", result4.getAsJsonObject().getString("name"));
        assertEquals(18, result4.getAsJsonObject().getAsInteger("age").intValue());

        // 测试无效JSON字符串转换
        JsonWrapper result5 = converter.convert("invalid json");
        assertNull(result5);
    }


}
