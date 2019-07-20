/*
 * Copyright 2007-2019 the original author or authors.
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
package net.ymate.platform.core.persistence;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-08-15 03:30
 * @since 2.1.0
 */
public abstract class AbstractDataSourceConfigurable implements IDataSourceConfigurable {

    private final String dataSourceName;

    private final Map<String, String> configs = new HashMap<>();

    protected AbstractDataSourceConfigurable(String dataSourceName) {
        if (StringUtils.isBlank(dataSourceName)) {
            throw new NullArgumentException("dataSourceName");
        }
        this.dataSourceName = dataSourceName;
    }

    public void addConfig(String confKey, String confValue) {
        configs.put(String.format("ds.%s.%s", dataSourceName, confKey), confValue);
    }

    public void addConfig(Map<String, String> configs) {
        if (configs != null && !configs.isEmpty()) {
            this.configs.putAll(configs);
        }
    }

    @Override
    public String getDataSourceName() {
        return dataSourceName;
    }

    @Override
    public Map<String, String> toMap() {
        return configs;
    }
}
