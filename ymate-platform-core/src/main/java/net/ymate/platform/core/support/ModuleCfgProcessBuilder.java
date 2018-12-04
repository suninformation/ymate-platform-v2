/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.core.support;

import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-04 02:17
 * @version 1.0
 */
public class ModuleCfgProcessBuilder {

    private final Map<String, Map<String, String>> __moduleCfgCaches = new HashMap<String, Map<String, String>>();

    private Properties __properties;

    public static ModuleCfgProcessBuilder create() {
        return new ModuleCfgProcessBuilder();
    }

    public static ModuleCfgProcessBuilder create(Properties properties) {
        return new ModuleCfgProcessBuilder(properties);
    }

    public ModuleCfgProcessBuilder() {
    }

    public ModuleCfgProcessBuilder(Properties properties) {
        __properties = properties;
    }

    public ModuleCfgProcessBuilder putModuleCfg(String moduleName, IModuleConfigurable moduleConfigurable) {
        __moduleCfgCaches.put(moduleName, moduleConfigurable.toMap());
        return this;
    }

    public IModuleCfgProcessor build() {
        return new IModuleCfgProcessor() {
            @Override
            public Map<String, String> getModuleCfg(String moduleName) {
                Map<String, String> _cfgMap = __moduleCfgCaches.get(moduleName);
                if (_cfgMap == null) {
                    _cfgMap = new HashMap<String, String>();
                    if (__properties != null) {
                        // 提取模块配置
                        for (Object _key : __properties.keySet()) {
                            String _prefix = "ymp.configs." + moduleName + ".";
                            if (StringUtils.startsWith((String) _key, _prefix)) {
                                String _cfgKey = StringUtils.substring((String) _key, _prefix.length());
                                String _cfgValue = __properties.getProperty((String) _key);
                                //
                                _cfgMap.put(_cfgKey, _cfgValue);
                            }
                        }
                        __moduleCfgCaches.put(moduleName, _cfgMap);
                    }
                }
                return _cfgMap;
            }
        };
    }
}
