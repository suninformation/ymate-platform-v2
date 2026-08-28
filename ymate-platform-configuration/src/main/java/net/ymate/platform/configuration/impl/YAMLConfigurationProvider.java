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

import net.ymate.platform.commons.util.FileUtils;
import net.ymate.platform.core.configuration.IConfigFileParser;

import java.net.URL;

/**
 * 基于YAML文件的配置提供者接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2026-08-28 14:25
 * @since 2.1.4
 */
public class YAMLConfigurationProvider extends JSONConfigurationProvider {

    @Override
    protected IConfigFileParser buildConfigFileParser(URL cfgFileName) throws Exception {
        return new YAMLConfigFileParser(cfgFileName);
    }

    @Override
    public String getSupportFileExtName() {
        return FileUtils.FILE_SUFFIX_YML;
    }
}
