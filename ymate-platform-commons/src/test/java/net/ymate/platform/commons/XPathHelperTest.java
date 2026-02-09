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

import net.ymate.platform.commons.annotation.XPathNode;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

/**
 * XPathHelper测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-07 13:19:21
 * @since 2.1.4
 */
public class XPathHelperTest {

    private static final String TEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<root>" +
            "    <name>Test Root</name>" +
            "    <value>123</value>" +
            "    <active>true</active>" +
            "    <items>" +
            "        <item>" +
            "            <id>1</id>" +
            "            <label>Item 1</label>" +
            "        </item>" +
            "        <item>" +
            "            <id>2</id>" +
            "            <label>Item 2</label>" +
            "        </item>" +
            "    </items>" +
            "</root>";

    @Test
    public void testNewDocumentBuilderFactory() throws Exception {
        DocumentBuilderFactory factory = XPathHelper.newDocumentBuilderFactory();
        Assert.assertNotNull(factory);
        Assert.assertFalse(factory.isXIncludeAware());
    }

    @Test
    public void testNewDocumentBuilder() throws Exception {
        Document document = XPathHelper.newDocumentBuilder().parse(new InputSource(new StringReader(TEST_XML)));
        Assert.assertNotNull(document);
    }

    @Test
    public void testNewXPathFactory() {
        XPathFactory factory = XPathHelper.newXPathFactory();
        Assert.assertNotNull(factory);
    }

    @Test
    public void testBuilder() throws Exception {
        // Test build with content
        XPathHelper helper1 = XPathHelper.Builder.create().build(TEST_XML);
        Assert.assertNotNull(helper1);
        Assert.assertNotNull(helper1.getDocument());

        // Test build with InputSource
        InputSource inputSource = new InputSource(new StringReader(TEST_XML));
        XPathHelper helper2 = XPathHelper.Builder.create().build(inputSource);
        Assert.assertNotNull(helper2);
        Assert.assertNotNull(helper2.getDocument());

        // Test build with Document
        Document document = XPathHelper.newDocumentBuilder().parse(new InputSource(new StringReader(TEST_XML)));
        XPathHelper helper3 = XPathHelper.Builder.create().build(document);
        Assert.assertNotNull(helper3);
        Assert.assertNotNull(helper3.getDocument());
    }

    @Test
    public void testGetStringValue() throws Exception {
        XPathHelper helper = XPathHelper.Builder.create().build(TEST_XML);

        // Test getStringValue with root node
        String name = helper.getStringValue("/root/name");
        Assert.assertEquals("Test Root", name);

        // Test getStringValue with non-existent node (should return empty string)
        String nonExistent = helper.getStringValue("/root/non-existent");
        Assert.assertEquals("", nonExistent);
    }

    @Test
    public void testGetNumberValue() throws Exception {
        XPathHelper helper = XPathHelper.Builder.create().build(TEST_XML);

        // Test getNumberValue with numeric node
        Number value = helper.getNumberValue("/root/value");
        Assert.assertEquals(123, value.intValue());
    }

    @Test
    public void testGetBooleanValue() throws Exception {
        XPathHelper helper = XPathHelper.Builder.create().build(TEST_XML);

        // Test getBooleanValue with boolean node
        Boolean active = helper.getBooleanValue("/root/active");
        Assert.assertTrue(active);
    }

    @Test
    public void testGetNode() throws Exception {
        XPathHelper helper = XPathHelper.Builder.create().build(TEST_XML);

        // Test getNode with existing node
        Node nameNode = helper.getNode("/root/name");
        Assert.assertNotNull(nameNode);
        Assert.assertEquals("name", nameNode.getNodeName());

        // Test getNode with non-existent node (should return null)
        Node nonExistentNode = helper.getNode("/root/non-existent");
        Assert.assertNull(nonExistentNode);
    }

    @Test
    public void testGetNodeList() throws Exception {
        XPathHelper helper = XPathHelper.Builder.create().build(TEST_XML);

        // Test getNodeList with existing nodes
        NodeList itemNodes = helper.getNodeList("/root/items/item");
        Assert.assertNotNull(itemNodes);
        Assert.assertEquals(2, itemNodes.getLength());

        // Test getNodeList with non-existent nodes (should return empty NodeList)
        NodeList nonExistentNodes = helper.getNodeList("/root/non-existent");
        Assert.assertNotNull(nonExistentNodes);
        Assert.assertEquals(0, nonExistentNodes.getLength());
    }

    @Test
    public void testToMap() throws Exception {
        XPathHelper helper = XPathHelper.Builder.create().build(TEST_XML);

        // Test toMap with root node
        Map<String, Object> map = helper.toMap();
        Assert.assertNotNull(map);
        Assert.assertEquals(3, map.size());
        Assert.assertEquals("Test Root", map.get("name"));
        Assert.assertEquals("123", map.get("value"));
        Assert.assertEquals("true", map.get("active"));

        // Test toMap with specific node
        Node rootNode = helper.getNode("/root");
        Map<String, Object> rootMap = helper.toMap(rootNode);
        Assert.assertNotNull(rootMap);
        Assert.assertEquals(3, rootMap.size());
    }

    @Test
    public void testToObject() throws Exception {
        XPathHelper helper = XPathHelper.Builder.create().build(TEST_XML);

        // Test toObject with class
        TestObject testObject1 = helper.toObject(TestObject.class);
        Assert.assertNotNull(testObject1);
        Assert.assertEquals("Test Root", testObject1.getName());
        Assert.assertEquals(123, testObject1.getValue());
        Assert.assertTrue(testObject1.isActive());

        // Test toObject with existing instance
        TestObject testObject2 = new TestObject();
        TestObject result = helper.toObject(testObject2);
        Assert.assertSame(testObject2, result);
        Assert.assertEquals("Test Root", testObject2.getName());
    }

    @Test
    public void testToObjectWithChildNodes() throws Exception {
        XPathHelper helper = XPathHelper.Builder.create().build(TEST_XML);

        TestObjectWithItems testObject = helper.toObject(TestObjectWithItems.class);
        Assert.assertNotNull(testObject);
        Assert.assertEquals("Test Root", testObject.getName());
        Assert.assertNotNull(testObject.getItems());
        Assert.assertEquals(2, testObject.getItems().size());
        Assert.assertEquals(1, testObject.getItems().get(0).getId());
        Assert.assertEquals("Item 1", testObject.getItems().get(0).getLabel());
        Assert.assertEquals(2, testObject.getItems().get(1).getId());
        Assert.assertEquals("Item 2", testObject.getItems().get(1).getLabel());
    }

    @XPathNode("/root")
    public static class TestObject {

        @XPathNode("name")
        private String name;

        @XPathNode("value")
        private int value;

        @XPathNode("active")
        private boolean active;

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

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }

    @XPathNode("/root")
    public static class TestObjectWithItems {

        @XPathNode("name")
        private String name;

        @XPathNode(value = "items/item", child = true, implClass = TestItem.class)
        private List<TestItem> items;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<TestItem> getItems() {
            return items;
        }

        public void setItems(List<TestItem> items) {
            this.items = items;
        }
    }

    @XPathNode
    public static class TestItem {

        @XPathNode("id")
        private int id;

        @XPathNode("label")
        private String label;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
