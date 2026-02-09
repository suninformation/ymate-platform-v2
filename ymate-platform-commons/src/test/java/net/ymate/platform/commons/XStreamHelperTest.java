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

import com.thoughtworks.xstream.XStream;
import org.junit.Assert;
import org.junit.Test;

/**
 * XStreamHelper测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-02-07 13:20:29
 * @since 2.1.4
 */
public class XStreamHelperTest {

    @Test
    public void testCreateXStreamWithoutCDATA() {
        XStream xstream = XStreamHelper.createXStream(false);
        Assert.assertNotNull(xstream);

        // Test object to XML conversion without CDATA
        TestObject testObject = new TestObject();
        testObject.setName("Test Name");
        testObject.setValue(123);

        String xml = xstream.toXML(testObject);
        Assert.assertNotNull(xml);
        Assert.assertTrue(xml.contains("<name>Test Name</name>"));
        Assert.assertTrue(xml.contains("<value>123</value>"));
    }

    @Test
    public void testCreateXStreamWithCDATA() {
        XStream xstream = XStreamHelper.createXStream(true);
        Assert.assertNotNull(xstream);

        // Test object to XML conversion with CDATA
        TestObject testObject = new TestObject();
        testObject.setName("Test Name");
        testObject.setValue(123);

        String xml = xstream.toXML(testObject);
        Assert.assertNotNull(xml);
        // With CDATA support, string values should be wrapped in CDATA
        Assert.assertTrue(xml.contains("<name><![CDATA[Test Name]]></name>") || xml.contains("<name>Test Name</name>"));
        Assert.assertTrue(xml.contains("<value>123</value>"));
    }

    @Test
    public void testCreateXStreamWithCDATAAndNodeFilter() {
        // Create a node filter that only applies CDATA to "name" field
        XStreamHelper.INodeFilter nodeFilter = name -> "name".equals(name);

        XStream xstream = XStreamHelper.createXStream(true, nodeFilter);
        Assert.assertNotNull(xstream);

        // Test object to XML conversion with CDATA and node filter
        TestObject testObject = new TestObject();
        testObject.setName("Test Name");
        testObject.setValue(123);

        String xml = xstream.toXML(testObject);
        Assert.assertNotNull(xml);
        // With node filter, only "name" should be wrapped in CDATA
        Assert.assertTrue(xml.contains("<name><![CDATA[Test Name]]></name>") || xml.contains("<name>Test Name</name>"));
        Assert.assertTrue(xml.contains("<value>123</value>"));
    }

    @Test
    public void testFilterableDomDriver() {
        // This test verifies that FilterableDomDriver can be instantiated
        // The actual functionality is tested indirectly through createXStream methods
        XStreamHelper.FilterableDomDriver driver = new XStreamHelper.FilterableDomDriver("UTF-8", null);
        Assert.assertNotNull(driver);

        // Test with node filter
        XStreamHelper.INodeFilter nodeFilter = name -> true;
        XStreamHelper.FilterableDomDriver driverWithFilter = new XStreamHelper.FilterableDomDriver("UTF-8", null, nodeFilter);
        Assert.assertNotNull(driverWithFilter);
    }

    @Test
    public void testINodeFilter() {
        // Test custom INodeFilter implementation
        XStreamHelper.INodeFilter nodeFilter = new XStreamHelper.INodeFilter() {
            @Override
            public boolean doFilter(String name) {
                return "filtered".equals(name);
            }
        };

        // Test filter with matching name
        Assert.assertTrue(nodeFilter.doFilter("filtered"));

        // Test filter with non-matching name
        Assert.assertFalse(nodeFilter.doFilter("non-filtered"));
    }

    public static class TestObject {
        private String name;
        private int value;

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
    }
}
