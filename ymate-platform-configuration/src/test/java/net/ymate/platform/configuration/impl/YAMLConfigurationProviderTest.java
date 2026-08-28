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

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * {@link YAMLConfigurationProvider} 单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026-08-28 14:35
 * @since 2.1.4
 */
public class YAMLConfigurationProviderTest {

    private YAMLConfigurationProvider loadProvider() throws Exception {
        YAMLConfigurationProvider provider = new YAMLConfigurationProvider();
        provider.load("src/test/resources/configuration.yaml");
        return provider;
    }

    @Test
    public void testLoadAndGet() throws Exception {
        YAMLConfigurationProvider provider = loadProvider();
        // 支持的文件扩展名
        Assert.assertEquals("yml", provider.getSupportFileExtName());
        // 标量属性值
        Assert.assertEquals("Apple Inc.", provider.getString("company_name"));
        Assert.assertEquals("Apple Inc.", provider.getString("default", "company_name", null));
        // 集合属性值
        List<String> products = provider.getList("products");
        Assert.assertEquals(4, products.size());
        Assert.assertEquals("iphone", products.get(0));
        Assert.assertEquals("itouch", products.get(3));
        // MAP属性键值对
        Map<String, String> spec = provider.getMap("product_spec");
        Assert.assertEquals(5, spec.size());
        Assert.assertEquals("red", spec.get("color"));
        Assert.assertEquals("2015", spec.get("age"));
    }
}
