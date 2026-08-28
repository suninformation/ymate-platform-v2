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

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.IApplicationConfigureParser;
import net.ymate.platform.core.configuration.IConfigReader;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@link DefaultApplicationConfigureParser} 分层配置加载单元测试
 *
 * @author 刘镇 (suninformation@163.com) on 2026-08-28 09:55
 * @since 2.1.4
 */
public class DefaultApplicationConfigureParserTest {

    @BeforeClass
    public static void setUp() {
        // 固定运行环境, 避免外部系统属性干扰测试结果
        System.setProperty(IApplication.SYSTEM_ENV, IApplication.Environment.DEV.name());
    }

    @Test
    public void testSystemDefaultLayeredLoading() {
        IApplicationConfigureParser parser = DefaultApplicationConfigureParser.systemDefault();
        IConfigReader configReader = parser.getConfigReader();
        // 全量基础配置文件(ymp-conf.properties)应被加载
        Assert.assertEquals("true", configReader.getString("ymp.dev_mode"));
        Assert.assertEquals("from-properties", configReader.getString("ymp.configs.test.base_value"));
        // 同层级存在 properties 文件时, yaml 文件应被跳过
        Assert.assertFalse(configReader.contains("ymp.yaml_only_key"));
        // 运行环境特定配置文件(ymp-conf_DEV.properties)应覆盖全量基础配置中的同名键
        Assert.assertEquals("from-dev-env", configReader.getString("ymp.configs.test.overridden_value"));
        Assert.assertEquals("dev-value", configReader.getString("ymp.configs.test.dev_only_value"));
    }
}
