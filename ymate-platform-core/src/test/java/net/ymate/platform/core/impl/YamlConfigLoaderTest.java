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

import java.io.IOException;
import java.io.InputStream;
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
}
