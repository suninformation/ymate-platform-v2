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
package net.ymate.platform.configuration.impl;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.configuration.IConfigFileParser;
import net.ymate.platform.core.configuration.IConfiguration;
import net.ymate.platform.core.configuration.IConfigurationProvider;

import java.util.List;
import java.util.Map;

/**
 * 默认配置对象接口实现, 方便扩展实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-27 上午01:57:05
 */
public class DefaultConfiguration implements IConfiguration {

    private IConfigurationProvider provider;

    @Override
    public IConfigFileParser.Property getProperty(String category, String key) {
        IConfigFileParser.Category categoryObj = provider.getConfigFileParser().getDefaultCategory();
        if (categoryObj == null) {
            return null;
        }
        return categoryObj.getProperty(key);
    }

    @Override
    public IConfigFileParser.Property getProperty(String key) {
        return provider.getConfigFileParser().getDefaultCategory().getProperty(key);
    }

    @Override
    public String getString(String key) {
        return provider.getString(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return provider.getString(key, defaultValue);
    }

    @Override
    public String getString(String category, String key, String defaultValue) {
        return provider.getString(category, key, defaultValue);
    }

    @Override
    public List<String> getList(String key) {
        return provider.getList(key);
    }

    @Override
    public List<String> getList(String category, String key) {
        return provider.getList(category, key);
    }

    @Override
    public Map<String, String> getMap(String keyHead) {
        return provider.getMap(keyHead);
    }

    @Override
    public Map<String, String> getMap(String category, String keyHead) {
        return provider.getMap(category, keyHead);
    }

    @Override
    public String[] getArray(String key) {
        return provider.getArray(key);
    }

    @Override
    public String[] getArray(String key, boolean zeroSize) {
        return provider.getArray(key, zeroSize);
    }

    @Override
    public String[] getArray(String category, String key, boolean zeroSize) {
        return provider.getArray(category, key, zeroSize);
    }

    @Override
    public int getInt(String key) {
        return provider.getInt(key);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return provider.getInt(key, defaultValue);
    }

    @Override
    public int getInt(String category, String key, int defaultValue) {
        return provider.getInt(category, key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key) {
        return provider.getBoolean(key);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return provider.getBoolean(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String category, String key, boolean defaultValue) {
        return provider.getBoolean(category, key, defaultValue);
    }

    @Override
    public long getLong(String key) {
        return provider.getLong(key);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return provider.getLong(key, defaultValue);
    }

    @Override
    public float getFloat(String key) {
        return provider.getFloat(key);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return provider.getFloat(key, defaultValue);
    }

    @Override
    public double getDouble(String key) {
        return provider.getDouble(key);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return provider.getDouble(key, defaultValue);
    }

    @Override
    public <T> T getClassImpl(String key, Class<T> interfaceClass) {
        return ClassUtils.impl(getString(key), interfaceClass, getClass());
    }

    @Override
    public <T> T getClassImpl(String key, String defaultValue, Class<T> interfaceClass) {
        return ClassUtils.impl(getString(key, defaultValue), interfaceClass, getClass());
    }

    @Override
    public Map<String, String> toMap() {
        return provider.toMap();
    }

    @Override
    public Map<String, String> toMap(String category) {
        return provider.toMap(category);
    }

    @Override
    public List<String> getCategoryNames() {
        return provider.getCategoryNames();
    }

    @Override
    public void initialize(IConfigurationProvider provider) {
        this.provider = provider;
    }

    @Override
    public void reload() throws Exception {
        this.provider.reload();
    }

    @Override
    public String getTagName() {
        return ".cfg";
    }

    @Override
    public boolean contains(String key) {
        return provider.contains(key);
    }

    @Override
    public boolean contains(String category, String key) {
        return provider.contains(category, key);
    }

}