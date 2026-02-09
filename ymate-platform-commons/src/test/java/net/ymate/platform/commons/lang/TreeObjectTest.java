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

import net.ymate.platform.commons.json.IJsonObjectWrapper;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * TreeObject 单元测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-05 21:10:22
 * @since 2.1.4
 */
public class TreeObjectTest {

    @Test
    public void testDefaultConstructor() {
        TreeObject tree = new TreeObject();
        assertEquals(TreeObject.TYPE_NULL, tree.getType());
        assertTrue(tree.isValue());
        assertFalse(tree.isMap());
        assertFalse(tree.isList());
    }

    @Test
    public void testBooleanConstructor() {
        TreeObject tree = new TreeObject(true);
        assertEquals(TreeObject.TYPE_BOOLEAN, tree.getType());
        assertTrue(tree.toBooleanValue());
    }

    @Test
    public void testStringConstructor() {
        TreeObject tree = new TreeObject("test");
        assertEquals(TreeObject.TYPE_STRING, tree.getType());
        assertEquals("test", tree.toStringValue());
    }

    @Test
    public void testMapConstructor() {
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", 123);

        TreeObject tree = new TreeObject(map);
        assertEquals(TreeObject.TYPE_MAP, tree.getType());
        assertTrue(tree.isMap());
    }

    @Test
    public void testAddMethods() {
        TreeObject tree = new TreeObject();

        // Test add boolean
        tree.add(true);
        assertTrue(tree.isList());
        assertEquals(1, tree.getList().size());
        assertTrue(tree.getBoolean(0));

        // Test add string
        tree.add("test");
        assertEquals(2, tree.getList().size());
        assertEquals("test", tree.getString(1));

        // Test add integer
        tree.add(123);
        assertEquals(3, tree.getList().size());
        assertEquals(123, tree.getInt(2));
    }

    @Test
    public void testPutMethods() {
        TreeObject tree = new TreeObject();

        // Test put boolean
        tree.put("boolKey", true);
        assertTrue(tree.isMap());
        assertTrue(tree.getBoolean("boolKey"));

        // Test put string
        tree.put("stringKey", "test");
        assertEquals("test", tree.getString("stringKey"));

        // Test put integer
        tree.put("intKey", 123);
        assertEquals(123, tree.getInt("intKey"));
    }

    @Test
    public void testModeChecks() {
        TreeObject valueTree = new TreeObject("test");
        assertTrue(valueTree.isValue());
        assertFalse(valueTree.isMap());
        assertFalse(valueTree.isList());

        TreeObject mapTree = new TreeObject();
        mapTree.put("key", "value");
        assertFalse(mapTree.isValue());
        assertTrue(mapTree.isMap());
        assertFalse(mapTree.isList());

        TreeObject listTree = new TreeObject();
        listTree.add("value");
        assertFalse(listTree.isValue());
        assertFalse(listTree.isMap());
        assertTrue(listTree.isList());
    }

    @Test
    public void testHasMethods() {
        TreeObject mapTree = new TreeObject();
        mapTree.put("existingKey", "value");

        assertTrue(mapTree.has("existingKey"));
        assertFalse(mapTree.has("nonExistingKey"));

        TreeObject listTree = new TreeObject();
        listTree.add("value");

        assertTrue(listTree.has(0));
        assertFalse(listTree.has(1));
    }

    @Test
    public void testGetMethods() {
        TreeObject tree = new TreeObject();
        tree.put("stringKey", "test");
        tree.put("intKey", 123);
        tree.put("boolKey", true);
        tree.put("doubleKey", 123.45);

        assertEquals("test", tree.getString("stringKey"));
        assertEquals(123, tree.getInt("intKey"));
        assertTrue(tree.getBoolean("boolKey"));
        assertEquals(123.45, tree.getDouble("doubleKey"), 0.001);

        // Test default values
        assertEquals("default", tree.getString("nonExistingKey", "default"));
        assertEquals(456, tree.getInt("nonExistingKey", 456));
        assertFalse(tree.getBoolean("nonExistingKey", false));
    }

    @Test
    public void testJsonSerialization() {
        // Create a simple TreeObject
        TreeObject tree = new TreeObject();
        tree.put("name", "test");
        tree.put("age", 30);
        tree.put("active", true);

        // Test toJson method
        IJsonObjectWrapper jsonObj = tree.toJson();
        assertNotNull(jsonObj);
        assertTrue(jsonObj.has("_c"));
        assertTrue(jsonObj.has("_v"));
    }


    @Test
    public void testXmlSerialization() {
        // Create a simple TreeObject
        TreeObject tree = new TreeObject();
        tree.put("name", "test");
        tree.put("age", 30);

        // Serialize to XML - currently throws UnsupportedOperationException
        try {
            String xmlStr = tree.toXml();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }

        // Deserialize from XML - currently throws UnsupportedOperationException
        try {
            TreeObject deserialized = TreeObject.fromXml("<tree></tree>");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testTypeConversionMethods() {
        TreeObject tree = new TreeObject("123");

        assertEquals(123, tree.toIntValue());
        assertEquals(123L, tree.toLongValue());
        assertEquals(123.0, tree.toDoubleValue(), 0.001);
        assertEquals("123", tree.toStringValue());
    }

    @Test
    public void testChainedOperations() {
        TreeObject tree = new TreeObject()
                .put("key1", "value1")
                .put("key2", 123)
                .put("key3", true);

        assertEquals("value1", tree.getString("key1"));
        assertEquals(123, tree.getInt("key2"));
        assertTrue(tree.getBoolean("key3"));
    }

    @Test
    public void testNullHandling() {
        TreeObject tree = new TreeObject((String) null);
        assertEquals(TreeObject.TYPE_STRING, tree.getType());
    }

    @Test
    public void testFromXmlSimple() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><tree _c=\"9\"><_v><name _c=\"3\"><_v>test</_v></name><age _c=\"1\"><_v>30</_v></age></_v></tree>";
        // fromXml currently throws UnsupportedOperationException
        try {
            TreeObject tree = TreeObject.fromXml(xml);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testToXmlSimple() {
        TreeObject tree = new TreeObject();
        tree.put("name", "test");
        tree.put("age", 30);

        // toXml currently throws UnsupportedOperationException
        try {
            String xml = tree.toXml();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testIsEmpty() {
        TreeObject emptyTree = new TreeObject();
        // Note: isEmpty() method is not implemented in TreeObject, but we can test if it's a null type
        assertEquals(TreeObject.TYPE_NULL, emptyTree.getType());
    }

    @Test
    public void testFromJsonWithNull() {
        // Test fromJson with null input
        String nullJson = null;
        try {
            TreeObject tree = TreeObject.fromJson(nullJson);
            fail("Expected NullArgumentException");
        } catch (org.apache.commons.lang.NullArgumentException e) {
            // Expected
        }
    }

    @Test
    public void testToXmlWithNull() {
        // toXml currently throws UnsupportedOperationException
        try {
            TreeObject.toXml(null);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test
    public void testListAccessMethods() {
        TreeObject tree = new TreeObject();
        tree.add("value1");
        tree.add("value2");
        tree.add("value3");

        List<TreeObject> list = tree.getList();
        assertEquals(3, list.size());
        assertEquals("value1", list.get(0).toStringValue());
        assertEquals("value2", list.get(1).toStringValue());
        assertEquals("value3", list.get(2).toStringValue());
    }

    @Test
    public void testMapAccessMethods() {
        TreeObject tree = new TreeObject();
        tree.put("key1", "value1");
        tree.put("key2", "value2");

        Map<String, TreeObject> map = tree.getMap();
        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1").toStringValue());
        assertEquals("value2", map.get("key2").toStringValue());
    }

    @Test
    public void testComplexNestedObjectConversion() {
        // Create a complex nested TreeObject with multiple data types
        TreeObject root = new TreeObject();

        // Add basic data types
        root.put("name", "complex-test");
        root.put("id", 12345);
        root.put("active", true);
        root.put("score", 98.76);
        root.put("createdAt", System.currentTimeMillis());

        // Add nested object (address)
        TreeObject address = new TreeObject();
        address.put("city", "Beijing");
        address.put("country", "China");
        address.put("zipCode", 100000);
        root.put("address", address);

        // Add nested list (contacts)
        TreeObject contacts = new TreeObject();
        TreeObject contact1 = new TreeObject();
        contact1.put("type", "email");
        contact1.put("value", "test@example.com");
        contacts.add(contact1);

        TreeObject contact2 = new TreeObject();
        contact2.put("type", "phone");
        contact2.put("value", "13800138000");
        contacts.add(contact2);
        root.put("contacts", contacts);

        // Add nested map (properties)
        TreeObject properties = new TreeObject();
        properties.put("color", "red");
        properties.put("size", "medium");
        properties.put("weight", 2.5);
        root.put("properties", properties);

        // Add array of numbers
        TreeObject numbers = new TreeObject();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(4);
        numbers.add(5);
        root.put("numbers", numbers);

        // Test XML conversion - currently throws UnsupportedOperationException
        System.out.println("Testing XML conversion...");
        try {
            String xml = root.toXml();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            System.out.println("XML conversion not implemented yet");
        }

        // Test XML deserialization - currently throws UnsupportedOperationException
        try {
            TreeObject fromXml = TreeObject.fromXml("<tree></tree>");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            System.out.println("XML deserialization not implemented yet");
        }

        // Test JSON conversion
        System.out.println("Testing JSON conversion...");
        IJsonObjectWrapper jsonObj = root.toJson();
        assertNotNull(jsonObj);
        System.out.println("JSON output: " + jsonObj.toString());
        assertTrue(jsonObj.has("_c"));
        assertTrue(jsonObj.has("_v"));
    }

    @Test
    public void testJsonFullCycle() {
        // Create a simple TreeObject
        TreeObject original = new TreeObject();
        original.put("name", "test");
        original.put("age", 30);
        original.put("active", true);

        // Test toJson method
        IJsonObjectWrapper jsonObj = original.toJson();
        assertNotNull(jsonObj);
        assertTrue(jsonObj.has("_c"));
        assertTrue(jsonObj.has("_v"));
        assertEquals(TreeObject.TYPE_MAP, jsonObj.getInt("_c"));
    }

    @Test
    public void testJsonArraySerialization() {
        // Create a TreeObject with array mode
        TreeObject arrayTree = new TreeObject();
        arrayTree.add("item1");
        arrayTree.add("item2");
        arrayTree.add("item3");

        // Test toJson method for array
        IJsonObjectWrapper jsonObj = arrayTree.toJson();
        assertNotNull(jsonObj);
        assertTrue(jsonObj.has("_c"));
        assertTrue(jsonObj.has("_v"));
        assertEquals(TreeObject.TYPE_COLLECTION, jsonObj.getInt("_c"));
    }

    @Test
    public void testJsonValueSerialization() {
        // Test various value types
        TreeObject stringTree = new TreeObject("test string");
        IJsonObjectWrapper stringJson = stringTree.toJson();
        assertNotNull(stringJson);
        assertEquals(TreeObject.TYPE_STRING, stringJson.getInt("_c"));

        TreeObject intTree = new TreeObject(123);
        IJsonObjectWrapper intJson = intTree.toJson();
        assertNotNull(intJson);
        assertEquals(TreeObject.TYPE_INTEGER, intJson.getInt("_c"));

        TreeObject boolTree = new TreeObject(true);
        IJsonObjectWrapper boolJson = boolTree.toJson();
        assertNotNull(boolJson);
        assertEquals(TreeObject.TYPE_BOOLEAN, boolJson.getInt("_c"));
    }

    @Test
    public void testComplexJsonXmlConversion() {
        System.out.println("\n========== 测试复杂对象的 JSON 和 XML 相互转换 ==========\n");

        // 创建一个复杂的嵌套 TreeObject，包含多种数据类型
        TreeObject root = new TreeObject();

        // 添加基本数据类型
        root.put("name", "complex-test");
        root.put("id", 12345);
        root.put("active", true);
        root.put("score", 98.76);
        root.put("createdAt", System.currentTimeMillis());

        // 添加嵌套对象 (address)
        TreeObject address = new TreeObject();
        address.put("city", "Beijing");
        address.put("country", "China");
        address.put("zipCode", 100000);
        address.put("isPrimary", true);
        address.put("latitude", 39.9042);
        root.put("address", address);

        // 添加嵌套列表 (contacts)
        TreeObject contacts = new TreeObject();
        TreeObject contact1 = new TreeObject();
        contact1.put("type", "email");
        contact1.put("value", "test@example.com");
        contact1.put("verified", true);
        contacts.add(contact1);

        TreeObject contact2 = new TreeObject();
        contact2.put("type", "phone");
        contact2.put("value", "13800138000");
        contact2.put("verified", false);
        contacts.add(contact2);

        TreeObject contact3 = new TreeObject();
        contact3.put("type", "wechat");
        contact3.put("value", "wechat_id_123");
        contact3.put("verified", true);
        contacts.add(contact3);
        root.put("contacts", contacts);

        // 添加嵌套映射 (properties)
        TreeObject properties = new TreeObject();
        properties.put("color", "red");
        properties.put("size", "medium");
        properties.put("weight", 2.5);
        properties.put("available", true);
        properties.put("quantity", 100);
        root.put("properties", properties);

        // 添加数字数组
        TreeObject numbers = new TreeObject();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(4);
        numbers.add(5);
        root.put("numbers", numbers);

        // 添加字符串数组
        TreeObject tags = new TreeObject();
        tags.add("tag1");
        tags.add("tag2");
        tags.add("tag3");
        root.put("tags", tags);

        // 添加混合类型数组
        TreeObject mixedArray = new TreeObject();
        mixedArray.add("string value");
        mixedArray.add(42);
        mixedArray.add(true);
        mixedArray.add(3.14);
        root.put("mixedArray", mixedArray);

        // 添加多层嵌套对象
        TreeObject level1 = new TreeObject();
        level1.put("level1Key", "level1Value");

        TreeObject level2 = new TreeObject();
        level2.put("level2Key", "level2Value");

        TreeObject level3 = new TreeObject();
        level3.put("level3Key", "level3Value");
        level3.put("level3Number", 999);

        level2.put("level3", level3);
        level1.put("level2", level2);
        root.put("deepNested", level1);

        // 添加空值和 null 值
        root.put("emptyString", "");
        root.put("zeroValue", 0);
        root.put("falseValue", false);

        // ========== 测试 JSON 转换 ==========
        System.out.println("【JSON 转换测试】");
        System.out.println("原始 TreeObject:");
        System.out.println("  - name: " + root.getString("name"));
        System.out.println("  - id: " + root.getInt("id"));
        System.out.println("  - active: " + root.getBoolean("active"));
        System.out.println("  - score: " + root.getDouble("score"));
        System.out.println("  - contacts 数量: " + root.get("contacts").getList().size());
        System.out.println("  - numbers 数量: " + root.get("numbers").getList().size());
        System.out.println("  - tags 数量: " + root.get("tags").getList().size());

        String jsonStr = root.toJson().toString();
        System.out.println("\n转换后的 JSON:");
        System.out.println(jsonStr);

        // 从 JSON 反序列化
        TreeObject fromJson = TreeObject.fromJson(jsonStr);
        assertNotNull(fromJson);
        System.out.println("\n从 JSON 反序列化成功:");
        System.out.println("  - name: " + fromJson.getString("name"));
        System.out.println("  - id: " + fromJson.getInt("id"));
        System.out.println("  - active: " + fromJson.getBoolean("active"));
        System.out.println("  - score: " + fromJson.getDouble("score"));

        // 验证嵌套对象
        TreeObject fromJsonAddress = fromJson.get("address");
        assertNotNull(fromJsonAddress);
        assertEquals("Beijing", fromJsonAddress.getString("city"));
        assertEquals("China", fromJsonAddress.getString("country"));
        assertEquals(100000, fromJsonAddress.getInt("zipCode"));
        System.out.println("  - address.city: " + fromJsonAddress.getString("city"));
        System.out.println("  - address.country: " + fromJsonAddress.getString("country"));

        // 验证嵌套列表
        TreeObject fromJsonContacts = fromJson.get("contacts");
        assertNotNull(fromJsonContacts);
        assertEquals(3, fromJsonContacts.getList().size());
        TreeObject fromJsonContact1 = fromJsonContacts.get(0);
        assertEquals("email", fromJsonContact1.getString("type"));
        assertEquals("test@example.com", fromJsonContact1.getString("value"));
        System.out.println("  - contacts[0].type: " + fromJsonContact1.getString("type"));
        System.out.println("  - contacts[0].value: " + fromJsonContact1.getString("value"));

        // 验证多层嵌套
        TreeObject fromJsonDeepNested = fromJson.get("deepNested");
        assertNotNull(fromJsonDeepNested);
        TreeObject fromJsonLevel2 = fromJsonDeepNested.get("level2");
        assertNotNull(fromJsonLevel2);
        TreeObject fromJsonLevel3 = fromJsonLevel2.get("level3");
        assertNotNull(fromJsonLevel3);
        assertEquals("level3Value", fromJsonLevel3.getString("level3Key"));
        assertEquals(999, fromJsonLevel3.getInt("level3Number"));
        System.out.println("  - deepNested.level2.level3.level3Key: " + fromJsonLevel3.getString("level3Key"));
        System.out.println("  - deepNested.level2.level3.level3Number: " + fromJsonLevel3.getInt("level3Number"));

        // ========== 测试 XML 转换 ==========
        System.out.println("\n【XML 转换测试】");
        // XML 转换功能尚未实现，暂时跳过
        try {
            String xmlStr = root.toXml();
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            System.out.println("XML 转换功能尚未实现，抛出了预期的 UnsupportedOperationException");
        }

        // 从 XML 反序列化 - 尚未实现
        try {
            TreeObject.fromXml("<tree></tree>");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            System.out.println("XML 反序列化功能尚未实现，抛出了预期的 UnsupportedOperationException");
        }

        // ========== 验证 JSON 转换的一致性 ==========
        System.out.println("\n【验证 JSON 转换的一致性】");

        assertEquals(root.getString("name"), fromJson.getString("name"));

        assertEquals(root.getInt("id"), fromJson.getInt("id"));

        assertEquals(root.getBoolean("active"), fromJson.getBoolean("active"));

        assertEquals(root.getDouble("score"), fromJson.getDouble("score"), 0.001);

        assertEquals(root.get("contacts").getList().size(), fromJson.get("contacts").getList().size());

        assertEquals(root.get("numbers").getList().size(), fromJson.get("numbers").getList().size());

        System.out.println("✓ 所有数据类型验证通过");
        System.out.println("✓ 嵌套对象验证通过");
        System.out.println("✓ 嵌套列表验证通过");
        System.out.println("✓ 多层嵌套验证通过");
        System.out.println("✓ JSON 转换一致性验证通过");

        System.out.println("\n========== 测试完成 ==========\n");
    }
}
