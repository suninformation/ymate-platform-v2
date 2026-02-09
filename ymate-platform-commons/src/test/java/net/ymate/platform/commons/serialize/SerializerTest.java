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
package net.ymate.platform.commons.serialize;

import net.ymate.platform.commons.serialize.impl.DefaultSerializer;
import net.ymate.platform.commons.serialize.impl.FstSerializer;
import net.ymate.platform.commons.serialize.impl.HessianSerializer;
import net.ymate.platform.commons.serialize.impl.JSONSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 序列化器单元测试类，用于验证所有序列化器的功能和正确性。
 *
 * @author 刘镇 (suninformation@163.com) on 2022/9/19 22:21
 * @since 2.1.2
 */
public class SerializerTest {

    private TestObject testObject;
    private List<TestObject> testList;

    private static boolean hessianAvailable;
    private static boolean fstAvailable;

    static {
        try {
            Class.forName("com.caucho.hessian.io.Hessian2Input");
            hessianAvailable = true;
        } catch (ClassNotFoundException e) {
            hessianAvailable = false;
        }

        try {
            Class.forName("org.nustaq.serialization.FSTConfiguration");
            FstSerializer testSerializer = new FstSerializer();
            testSerializer.serialize("test");
            fstAvailable = true;
        } catch (Exception | NoClassDefFoundError e) {
            fstAvailable = false;
        }
    }

    @Before
    public void setUp() {
        testObject = new TestObject("testName", 100, "testDescription");

        testList = new ArrayList<>();
        testList.add(new TestObject("obj1", 1, "desc1"));
        testList.add(new TestObject("obj2", 2, "desc2"));
        testList.add(new TestObject("obj3", 3, "desc3"));
    }

    @Test
    public void testDefaultSerializerBasicSerialization() throws Exception {
        DefaultSerializer serializer = new DefaultSerializer();

        byte[] bytes = serializer.serialize(testObject);
        Assert.assertNotNull(bytes);
        Assert.assertTrue(bytes.length > 0);

        TestObject deserialized = serializer.deserialize(bytes, TestObject.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(testObject.getName(), deserialized.getName());
        Assert.assertEquals(testObject.getValue(), deserialized.getValue());
        Assert.assertEquals(testObject.getDescription(), deserialized.getDescription());
    }

    @Test
    public void testDefaultSerializerContentType() {
        DefaultSerializer serializer = new DefaultSerializer();
        Assert.assertEquals("application/x-java-serialized-object", serializer.getContentType());
    }

    @Test
    public void testJSONSerializerBasicSerialization() throws Exception {
        JSONSerializer serializer = new JSONSerializer();

        byte[] bytes = serializer.serialize(testObject);
        Assert.assertNotNull(bytes);
        Assert.assertTrue(bytes.length > 0);

        TestObject deserialized = serializer.deserialize(bytes, TestObject.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(testObject.getName(), deserialized.getName());
        Assert.assertEquals(testObject.getValue(), deserialized.getValue());
        Assert.assertEquals(testObject.getDescription(), deserialized.getDescription());
    }

    @Test
    public void testJSONSerializerListSerializationWithClass() throws Exception {
        JSONSerializer serializer = new JSONSerializer();

        byte[] bytes = serializer.serialize(testList);
        Assert.assertNotNull(bytes);

        @SuppressWarnings("unchecked")
        List<TestObject> deserialized = (List<TestObject>) serializer.deserialize(bytes, List.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(testList.size(), deserialized.size());
    }

    @Test
    public void testJSONSerializerContentType() {
        JSONSerializer serializer = new JSONSerializer();
        Assert.assertEquals("application/json", serializer.getContentType());
    }

    @Test(expected = NullPointerException.class)
    public void testJSONSerializerNullAdapter() throws Exception {
        JSONSerializer serializer = new JSONSerializer(null);
        // 传入null adapter时，构造函数不会立即抛出异常，但在序列化时会抛出
        serializer.serialize(testObject);
    }

    @Test
    public void testHessianSerializerBasicSerialization() throws Exception {
        if (!hessianAvailable) {
            return;
        }
        HessianSerializer serializer = new HessianSerializer();

        byte[] bytes = serializer.serialize(testObject);
        Assert.assertNotNull(bytes);
        Assert.assertTrue(bytes.length > 0);

        TestObject deserialized = serializer.deserialize(bytes, TestObject.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(testObject.getName(), deserialized.getName());
        Assert.assertEquals(testObject.getValue(), deserialized.getValue());
        Assert.assertEquals(testObject.getDescription(), deserialized.getDescription());
    }

    @Test
    public void testHessianSerializerListSerialization() throws Exception {
        if (!hessianAvailable) {
            return;
        }
        HessianSerializer serializer = new HessianSerializer();

        byte[] bytes = serializer.serialize(testList);
        Assert.assertNotNull(bytes);

        @SuppressWarnings("unchecked")
        List<TestObject> deserialized = (List<TestObject>) serializer.deserialize(bytes, List.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(testList.size(), deserialized.size());
    }

    @Test
    public void testHessianSerializerContentType() {
        if (!hessianAvailable) {
            return;
        }
        HessianSerializer serializer = new HessianSerializer();
        Assert.assertEquals("application/x-java-serialized-hessian", serializer.getContentType());
    }

    @Test
    public void testFstSerializerBasicSerialization() throws Exception {
        if (!fstAvailable) {
            return;
        }
        FstSerializer serializer = new FstSerializer();

        byte[] bytes = serializer.serialize(testObject);
        Assert.assertNotNull(bytes);
        Assert.assertTrue(bytes.length > 0);

        TestObject deserialized = serializer.deserialize(bytes, TestObject.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(testObject.getName(), deserialized.getName());
        Assert.assertEquals(testObject.getValue(), deserialized.getValue());
        Assert.assertEquals(testObject.getDescription(), deserialized.getDescription());
    }

    @Test
    public void testFstSerializerListSerialization() throws Exception {
        if (!fstAvailable) {
            return;
        }
        FstSerializer serializer = new FstSerializer();

        byte[] bytes = serializer.serialize(testList);
        Assert.assertNotNull(bytes);

        @SuppressWarnings("unchecked")
        List<TestObject> deserialized = (List<TestObject>) serializer.deserialize(bytes, List.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(testList.size(), deserialized.size());
    }

    @Test
    public void testFstSerializerContentType() {
        if (!fstAvailable) {
            return;
        }
        FstSerializer serializer = new FstSerializer();
        Assert.assertEquals("application/x-java-serialized-fst", serializer.getContentType());
    }

    @Test
    public void testSerializerManagerGetDefaultSerializer() {
        ISerializer serializer = SerializerManager.getDefaultSerializer();
        Assert.assertNotNull(serializer);
        Assert.assertTrue(serializer instanceof DefaultSerializer);
    }

    @Test
    public void testSerializerManagerGetJsonSerializer() {
        ISerializer serializer = SerializerManager.getJsonSerializer();
        Assert.assertNotNull(serializer);
        Assert.assertTrue(serializer instanceof JSONSerializer);
    }

    @Test
    public void testSerializerManagerGetSerializerByName() {
        ISerializer defaultSerializer = SerializerManager.getSerializer("default");
        Assert.assertNotNull(defaultSerializer);
        Assert.assertTrue(defaultSerializer instanceof DefaultSerializer);

        ISerializer jsonSerializer = SerializerManager.getSerializer("json");
        Assert.assertNotNull(jsonSerializer);
        Assert.assertTrue(jsonSerializer instanceof JSONSerializer);

        if (hessianAvailable) {
            // HessianSerializer 没有 @Serializer 注解，使用类名（小写）作为 key
            ISerializer hessianSerializer = SerializerManager.getSerializer("net.ymate.platform.commons.serialize.impl.hessianserializer");
            Assert.assertNotNull(hessianSerializer);
            Assert.assertTrue(hessianSerializer instanceof HessianSerializer);
        }

        if (fstAvailable) {
            // FstSerializer 没有 @Serializer 注解，使用类名（小写）作为 key
            ISerializer fstSerializer = SerializerManager.getSerializer("net.ymate.platform.commons.serialize.impl.fstserializer");
            Assert.assertNotNull(fstSerializer);
            Assert.assertTrue(fstSerializer instanceof FstSerializer);
        }
    }

    @Test
    public void testSerializerManagerGetSerializerByClass() {
        ISerializer serializer = SerializerManager.getSerializer(DefaultSerializer.class);
        Assert.assertNull(serializer);
        serializer = SerializerManager.getSerializer(DefaultSerializer.NAME);
        Assert.assertNotNull(serializer);
        Assert.assertTrue(serializer instanceof DefaultSerializer);
    }

    @Test
    public void testSerializerManagerCaseInsensitive() {
        ISerializer serializer1 = SerializerManager.getSerializer("JSON");
        ISerializer serializer2 = SerializerManager.getSerializer("json");
        ISerializer serializer3 = SerializerManager.getSerializer("Json");

        Assert.assertNotNull(serializer1);
        Assert.assertNotNull(serializer2);
        Assert.assertNotNull(serializer3);
        Assert.assertEquals(serializer1, serializer2);
        Assert.assertEquals(serializer2, serializer3);
    }

    @Test
    public void testSerializerManagerGetNullName() {
        ISerializer serializer = SerializerManager.getSerializer((String) null);
        Assert.assertNull(serializer);
    }

    @Test
    public void testSerializerManagerGetEmptyName() {
        ISerializer serializer = SerializerManager.getSerializer("");
        Assert.assertNull(serializer);
    }

    @Test
    public void testSerializerManagerGetBlankName() {
        ISerializer serializer = SerializerManager.getSerializer("   ");
        Assert.assertNull(serializer);
    }

    @Test
    public void testSerializerManagerGetNullClass() {
        ISerializer serializer = SerializerManager.getSerializer((Class<? extends ISerializer>) null);
        Assert.assertNull(serializer);
    }

    @Test
    public void testMultipleSerializationCycles() throws Exception {
        DefaultSerializer serializer = new DefaultSerializer();

        for (int i = 0; i < 100; i++) {
            TestObject obj = new TestObject("test" + i, i, "desc" + i);
            byte[] bytes = serializer.serialize(obj);
            TestObject deserialized = serializer.deserialize(bytes, TestObject.class);
            Assert.assertEquals(obj.getName(), deserialized.getName());
            Assert.assertEquals(obj.getValue(), deserialized.getValue());
            Assert.assertEquals(obj.getDescription(), deserialized.getDescription());
        }
    }

    @Test
    public void testEmptyStringSerialization() throws Exception {
        DefaultSerializer serializer = new DefaultSerializer();

        byte[] bytes = serializer.serialize("");
        String deserialized = serializer.deserialize(bytes, String.class);
        Assert.assertEquals("", deserialized);
    }

    @Test
    public void testNullFieldSerialization() throws Exception {
        DefaultSerializer serializer = new DefaultSerializer();

        TestObject obj = new TestObject(null, 0, null);
        byte[] bytes = serializer.serialize(obj);
        TestObject deserialized = serializer.deserialize(bytes, TestObject.class);
        Assert.assertNull(deserialized.getName());
        Assert.assertEquals(0, deserialized.getValue());
        Assert.assertNull(deserialized.getDescription());
    }

    @Test
    public void testLargeObjectSerialization() throws Exception {
        DefaultSerializer serializer = new DefaultSerializer();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("test");
        }
        TestObject obj = new TestObject(sb.toString(), 99999, sb.toString());

        byte[] bytes = serializer.serialize(obj);
        Assert.assertNotNull(bytes);
        Assert.assertTrue(bytes.length > 0);

        TestObject deserialized = serializer.deserialize(bytes, TestObject.class);
        Assert.assertEquals(obj.getName(), deserialized.getName());
        Assert.assertEquals(obj.getValue(), deserialized.getValue());
        Assert.assertEquals(obj.getDescription(), deserialized.getDescription());
    }

    @Test
    public void testSerializerManagerRegisterSerializer() throws Exception {
        // 测试注册一个新的序列化器
        SerializerManager.registerSerializer("testDefault", DefaultSerializer.class);
        ISerializer serializer = SerializerManager.getSerializer("testDefault");
        Assert.assertNotNull(serializer);
        Assert.assertTrue(serializer instanceof DefaultSerializer);
    }

    static class TestObject implements Serializable {

        private static final long serialVersionUID = 1L;

        private String name;
        private int value;
        private String description;

        public TestObject() {
        }

        public TestObject(String name, int value, String description) {
            this.name = name;
            this.value = value;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
