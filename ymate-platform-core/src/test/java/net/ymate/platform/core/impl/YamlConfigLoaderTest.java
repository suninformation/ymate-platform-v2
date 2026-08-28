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
package net.ymate.platform.core.impl;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * {@link YamlConfigLoader} 单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026-08-28 09:50
 * @since 2.1.4
 */
public class YamlConfigLoaderTest {

    @Test
    public void testIsYamlAvailable() {
        Assert.assertTrue(YamlConfigLoader.isYamlAvailable());
    }

    @Test
    public void testFlatten() {
        Map<String, Object> jdbcConfig = new LinkedHashMap<>();
        jdbcConfig.put("connection_url", "jdbc:mysql://localhost/db");

        Map<String, Object> ympConfig = new LinkedHashMap<>();
        ympConfig.put("dev_mode", true);
        ympConfig.put("excluded_packages", Arrays.asList("com.test", "com.demo"));
        ympConfig.put("empty_list", new ArrayList<>());
        ympConfig.put("null_value", null);
        ympConfig.put("configs", Collections.singletonMap("jdbc", jdbcConfig));

        Map<String, Object> source = Collections.singletonMap("ymp", ympConfig);

        Map<String, String> returnValue = YamlConfigLoader.flatten(source);
        Assert.assertEquals(3, returnValue.size());
        Assert.assertEquals("true", returnValue.get("ymp.dev_mode"));
        Assert.assertEquals("com.test|com.demo", returnValue.get("ymp.excluded_packages"));
        Assert.assertEquals("jdbc:mysql://localhost/db", returnValue.get("ymp.configs.jdbc.connection_url"));
        Assert.assertFalse(returnValue.containsKey("ymp.empty_list"));
        Assert.assertFalse(returnValue.containsKey("ymp.null_value"));
    }

    @Test
    public void testLoadAndFlatten() throws IOException {
        try (InputStream inputStream = YamlConfigLoaderTest.class.getClassLoader().getResourceAsStream("test-yaml.yaml")) {
            Assert.assertNotNull(inputStream);
            Map<String, String> returnValue = YamlConfigLoader.loadAndFlatten(inputStream);
            Assert.assertEquals("false", returnValue.get("ymp.dev_mode"));
            Assert.assertEquals("2.1.4", returnValue.get("ymp.version"));
            Assert.assertEquals("com.test|com.demo", returnValue.get("ymp.excluded_packages"));
            Assert.assertEquals("jdbc:mysql://localhost/test", returnValue.get("ymp.configs.jdbc.connection_url"));
            Assert.assertEquals("20", returnValue.get("ymp.configs.jdbc.max_pool_size"));
            Assert.assertFalse(returnValue.containsKey("ymp.null_value"));
            Assert.assertFalse(returnValue.containsKey("ymp.empty_list"));
        }
    }

    @Test
    public void testPropertiesToYamlText() throws IOException {
        // 1. 从 classpath 加载真实项目的全量配置文件
        Properties properties = new Properties();
        try (InputStream inputStream = YamlConfigLoaderTest.class.getClassLoader().getResourceAsStream("ymp-conf_full.properties")) {
            Assert.assertNotNull(inputStream);
            properties.load(inputStream);
        }

        // 2. Properties 转换为 YAML 格式文本并输出
        String yamlText = YamlConfigLoader.toYamlText(properties);
        System.out.println("======== Properties 转换为 YAML 文本 ========");
        System.out.println(yamlText);

        Assert.assertNotNull(yamlText);

        // 3. YAML 格式文本再次转换回键值对集合并输出
        Map<String, String> roundTripMap;
        try (InputStream inputStream = new ByteArrayInputStream(yamlText.getBytes(StandardCharsets.UTF_8))) {
            roundTripMap = YamlConfigLoader.loadAndFlatten(inputStream);
        }
        System.out.println("======== YAML 文本转换回键值对集合 ========");
        System.out.println(roundTripMap);

        // 4. 验证往返转换前后配置项数量与内容完全一致
        Assert.assertEquals(properties.size(), roundTripMap.size());
        properties.forEach((key, value) -> Assert.assertEquals(String.valueOf(value), roundTripMap.get(key)));
    }

    @Test
    public void testYamlAndPropertiesRoundTrip() throws IOException {
        // 1. 加载 YAML 配置文件并扁平化为键值对集合(与 Properties 加载结果结构一致)
        Map<String, String> flatMap;
        try (InputStream inputStream = YamlConfigLoaderTest.class.getClassLoader().getResourceAsStream("test-yaml.yaml")) {
            Assert.assertNotNull(inputStream);
            flatMap = YamlConfigLoader.loadAndFlatten(inputStream);
        }
        Assert.assertEquals(5, flatMap.size());

        // 2. 键值对集合转换为 YAML 格式文本并输出
        String yamlText = YamlConfigLoader.toYamlText(flatMap);
        System.out.println("======== 键值对集合转换为 YAML 文本 ========");
        System.out.println(yamlText);

        // 3. YAML 格式文本再次转换回键值对集合, 输出并验证往返转换的一致性
        Map<String, String> roundTripMap;
        try (InputStream inputStream = new ByteArrayInputStream(yamlText.getBytes(StandardCharsets.UTF_8))) {
            roundTripMap = YamlConfigLoader.loadAndFlatten(inputStream);
        }
        System.out.println("======== YAML 文本转换回键值对集合 ========");
        System.out.println(roundTripMap);

        Assert.assertEquals(flatMap, roundTripMap);
    }
}
