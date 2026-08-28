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
package net.ymate.platform.configuration.impl;

import net.ymate.platform.core.configuration.IConfigFileParser;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * {@link YAMLConfigFileParser} 单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026-08-28 14:30
 * @since 2.1.4
 */
public class YAMLConfigFileParserTest {

    private IConfigFileParser loadParser() throws IOException {
        try (InputStream inputStream = YAMLConfigFileParserTest.class.getClassLoader().getResourceAsStream("configuration.yaml")) {
            Assert.assertNotNull(inputStream);
            return new YAMLConfigFileParser(inputStream).load(true);
        }
    }

    @Test
    public void testParseYamlConfiguration() throws IOException {
        IConfigFileParser parser = loadParser();
        // 分类解析及默认分类
        Assert.assertNotNull(parser.getDefaultCategory());
        Assert.assertNotNull(parser.getCategory("default"));
        // 标量属性值
        Assert.assertEquals("Apple Inc.", parser.getCategory("default").getProperty("company_name").getContent());
        // 集合属性值被序列化为JSON数组字符串
        IConfigFileParser.Property products = parser.getCategory("default").getProperty("products");
        Assert.assertNotNull(products);
        Assert.assertTrue(products.getContent().startsWith("["));
        Assert.assertTrue(products.getContent().endsWith("]"));
        // MAP属性键值对
        IConfigFileParser.Property spec = parser.getCategory("default").getProperty("product_spec");
        Assert.assertEquals("spec.", spec.getContent());
        Assert.assertEquals("xzy", spec.getAttribute("abc").getValue());
        Assert.assertEquals("red", spec.getAttribute("color").getValue());
        // 数值型属性值转换为字符串
        Assert.assertEquals("2015", spec.getAttribute("age").getValue());
    }

    @Test
    public void testWriteToAndReload() throws IOException {
        IConfigFileParser parser = loadParser();
        // 配置内容序列化为YAML格式文本并输出
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        parser.writeTo(outputStream);
        String yamlText = new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
        System.out.println("======== YAML 配置文件序列化输出 ========");
        System.out.println(yamlText);
        Assert.assertTrue(yamlText.contains("categories:"));
        // 序列化文本重新加载, 验证往返转换的一致性
        IConfigFileParser reloadParser;
        try (InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
            reloadParser = new YAMLConfigFileParser(inputStream).load(true);
        }
        Assert.assertEquals(parser.getCategories().keySet(), reloadParser.getCategories().keySet());
        // 标量属性值
        Assert.assertEquals(parser.getCategory("default").getProperty("company_name").getContent(),
                reloadParser.getCategory("default").getProperty("company_name").getContent());
        // 集合属性值
        Assert.assertEquals(parser.getCategory("default").getProperty("products").getContent(),
                reloadParser.getCategory("default").getProperty("products").getContent());
        // MAP属性键值对
        IConfigFileParser.Property spec = reloadParser.getCategory("default").getProperty("product_spec");
        Assert.assertEquals("red", spec.getAttribute("color").getValue());
        Assert.assertEquals("xzy", spec.getAttribute("abc").getValue());
        Assert.assertEquals("2015", spec.getAttribute("age").getValue());
    }
}
