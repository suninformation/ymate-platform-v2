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
package net.ymate.platform.persistence.redis.support;

import net.ymate.platform.core.support.IModuleConfigurable;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.IRedisModuleCfg;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018-12-14 16:09
 * @version 1.0
 * @since 2.0.6
 */
public class RedisModuleConfigurable implements IModuleConfigurable {

    public static RedisModuleConfigurable create() {
        return new RedisModuleConfigurable();
    }

    private Map<String, String> __configs = new HashMap<String, String>();

    private Map<String, RedisDataSourceConfigurable> __dataSources = new HashMap<String, RedisDataSourceConfigurable>();

    public RedisModuleConfigurable defaultDataSourceName(String defaultDataSourceName) {
        __configs.put(IRedisModuleCfg.DS_DEFAULT_NAME, StringUtils.trimToEmpty(defaultDataSourceName));
        return this;
    }

    public RedisModuleConfigurable addDataSource(RedisDataSourceConfigurable dataSourceConfig) {
        __dataSources.put(dataSourceConfig.getName(), dataSourceConfig);
        return this;
    }

    @Override
    public String getModuleName() {
        return IRedis.MODULE_NAME;
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> _configs = new HashMap<String, String>(__configs);
        if (!__dataSources.isEmpty()) {
            _configs.put(IRedisModuleCfg.DS_NAME_LIST, StringUtils.join(__dataSources.keySet(), "|"));
            for (RedisDataSourceConfigurable _dsConfig : __dataSources.values()) {
                _configs.putAll(_dsConfig.toMap());
            }
        }
        return _configs;
    }
}
