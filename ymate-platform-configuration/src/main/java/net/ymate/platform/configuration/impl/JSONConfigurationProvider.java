/*
 * Copyright 2007-2020 the original author or authors.
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

import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.configuration.AbstractConfigurationProvider;
import net.ymate.platform.core.configuration.IConfigFileParser;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/06/16 15:55
 * @since 2.1.0
 */
public class JSONConfigurationProvider extends AbstractConfigurationProvider {

    @Override
    protected IConfigFileParser buildConfigFileParser(URL cfgFileName) throws Exception {
        return new JSONConfigFileParser(cfgFileName);
    }

    @Override
    public List<String> getList(String category, String key) {
        IConfigFileParser.Property prop = getConfigFileParser().getCategory(category).getProperty(key);
        if (prop != null) {
            String content = prop.getContent();
            if (StringUtils.isNotBlank(content)) {
                if (!StringUtils.startsWith(content, "[")) {
                    content = "[" + content;
                }
                if (!StringUtils.endsWith(content, "]")) {
                    content += "]";
                }
                IJsonArrayWrapper jsonArray = JsonWrapper.fromJson(content).getAsJsonArray();
                if (jsonArray != null && !jsonArray.isEmpty()) {
                    return IntStream.range(0, jsonArray.size()).mapToObj(jsonArray::getString).collect(Collectors.toList());
                }
            }
        }
        return new ArrayList<>();
    }
}
