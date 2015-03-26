/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.core.plugin.impl;

import net.ymate.platform.core.plugin.IPluginContext;
import net.ymate.platform.core.plugin.IPluginParser;
import net.ymate.platform.core.plugin.PluginMeta;

import java.util.Collections;
import java.util.Map;

/**
 * 默认插件配置文件分析器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/22 下午4:55
 * @version 1.0
 */
public class DefaultPluginParser implements IPluginParser {

    public Map<String, PluginMeta> doParser(IPluginContext context) throws Exception {
        return Collections.emptyMap();
    }
}
