/*
 * Copyright 2007-2016 the original author or authors.
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

import net.ymate.platform.configuration.IConfigurationProvider;
import net.ymate.platform.configuration.support.XMLConfigFileHandler;
import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.FileUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * 默认配置提供者接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2010-4-17 下午02:34:44
 * @version 1.0
 */
public class DefaultConfigurationProvider implements IConfigurationProvider {

    /**
     * 配置对象缓存，对于重复的文件加载会使用缓存，减少文件读写频率
     */
    private static final Map<String, XMLConfigFileHandler> __CONFIG_CACHE_MAPS = new HashMap<String, XMLConfigFileHandler>();

    /**
     * 配置对象
     */
    private XMLConfigFileHandler __config;

    /**
     * 装载配置文件参数
     */
    private String __cfgFileName;

    public void load(String cfgFileName) throws Exception {
        if (StringUtils.isBlank(cfgFileName)) {
            throw new NullArgumentException("cfgFileName");
        }
        if ((__config = __CONFIG_CACHE_MAPS.get(cfgFileName)) == null) {
            this.__cfgFileName = cfgFileName;
            __config = new XMLConfigFileHandler(FileUtils.toURL(cfgFileName)).load(true);
            __CONFIG_CACHE_MAPS.put(cfgFileName, __config);
        }
    }

    public void reload() throws Exception {
        // 移除缓存项
        __CONFIG_CACHE_MAPS.remove(this.__cfgFileName);
        // 加载配置
        load(this.__cfgFileName);
    }

    public String getCfgFileName() {
        return __cfgFileName;
    }

    public String getString(String key) {
        XMLConfigFileHandler.XMLProperty _prop = __config.getDefaultCategory().getProperty(key);
        return _prop == null ? null : _prop.getContent();
    }

    public String getString(String key, String defaultValue) {
        return StringUtils.defaultIfEmpty(getString(key), defaultValue);
    }

    public String getString(String category, String key, String defaultValue) {
        XMLConfigFileHandler.XMLCategory _category = __config.getCategory(category);
        if (_category == null) {
            return null;
        }
        XMLConfigFileHandler.XMLProperty _prop = _category.getProperty(key);
        return StringUtils.defaultIfEmpty(_prop == null ? null : _prop.getContent(), defaultValue);
    }

    public List<String> getList(String key) {
        return getList(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key);
    }

    public List<String> getList(String category, String key) {
        List<String> _returnValue = new ArrayList<String>();
        XMLConfigFileHandler.XMLProperty _prop = __config.getCategory(category).getProperty(key);
        if (_prop != null) {
            for (XMLConfigFileHandler.XMLAttribute _attr : _prop.getAttributeMap().values()) {
                if (StringUtils.isBlank(_attr.getValue())) {
                    _returnValue.add(_attr.getKey());
                }
            }
        }
        return _returnValue;
    }

    public Map<String, String> getMap(String key) {
        return getMap(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key);
    }

    public Map<String, String> getMap(String category, String key) {
        Map<String, String> _returnValue = new LinkedHashMap<String, String>();
        XMLConfigFileHandler.XMLProperty _prop = __config.getCategory(category).getProperty(key);
        if (_prop != null) {
            for (String name : _prop.getAttributeMap().keySet()) {
                String value = _prop.getAttribute(name).getValue();
                if (StringUtils.isNotBlank(value)) {
                    _returnValue.put(name, value);
                }
            }
        }
        return _returnValue;
    }

    public String[] getArray(String key) {
        List<String> _resultValue = getList(key);
        return _resultValue.toArray(new String[_resultValue.size()]);
    }

    public String[] getArray(String key, boolean zeroSize) {
        return getArray(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key, zeroSize);
    }

    public String[] getArray(String category, String key, boolean zeroSize) {
        List<String> _values = getList(category, key);
        if (_values.isEmpty() && !zeroSize) {
            return null;
        }
        return _values.toArray(new String[_values.size()]);
    }

    public int getInt(String key) {
        return getInt(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return getInt(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    public int getInt(String category, String key, int defaultValue) {
        XMLConfigFileHandler.XMLCategory _category = __config.getCategory(category);
        if (_category != null) {
            XMLConfigFileHandler.XMLProperty _prop = _category.getProperty(key);
            if (_prop != null) {
                return new BlurObject(_prop.getContent()).toIntValue();
            }
        }
        return defaultValue;
    }

    public boolean getBoolean(String key) {
        return getBoolean(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    public boolean getBoolean(String category, String key, boolean defaultValue) {
        XMLConfigFileHandler.XMLCategory _category = __config.getCategory(category);
        if (_category != null) {
            XMLConfigFileHandler.XMLProperty _prop = _category.getProperty(key);
            if (_prop != null) {
                return new BlurObject(_prop.getContent()).toBooleanValue();
            }
        }
        return defaultValue;
    }

    public long getLong(String key) {
        return getLong(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key, 0l);
    }

    public long getLong(String key, long defaultValue) {
        return getLong(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    public long getLong(String category, String key, long defaultValue) {
        XMLConfigFileHandler.XMLCategory _category = __config.getCategory(category);
        if (_category != null) {
            XMLConfigFileHandler.XMLProperty _prop = _category.getProperty(key);
            if (_prop != null) {
                return new BlurObject(_prop.getContent()).toLongValue();
            }
        }
        return defaultValue;
    }

    public float getFloat(String key) {
        return getFloat(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key, 0f);
    }

    public float getFloat(String key, float defaultValue) {
        return getFloat(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    public float getFloat(String category, String key, float defaultValue) {
        XMLConfigFileHandler.XMLCategory _category = __config.getCategory(category);
        if (_category != null) {
            XMLConfigFileHandler.XMLProperty _prop = _category.getProperty(key);
            if (_prop != null) {
                return new BlurObject(_prop.getContent()).toFloatValue();
            }
        }
        return defaultValue;
    }

    public double getDouble(String key) {
        return getDouble(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key, 0d);
    }

    public double getDouble(String key, double defaultValue) {
        return getDouble(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    public double getDouble(String category, String key, double defaultValue) {
        XMLConfigFileHandler.XMLCategory _category = __config.getCategory(category);
        if (_category != null) {
            XMLConfigFileHandler.XMLProperty _prop = _category.getProperty(key);
            if (_prop != null) {
                return new BlurObject(_prop.getContent()).toDoubleValue();
            }
        }
        return defaultValue;
    }

    public Map<String, String> toMap() {
        return toMap(XMLConfigFileHandler.DEFAULT_CATEGORY_NAME);
    }

    public Map<String, String> toMap(String category) {
        XMLConfigFileHandler.XMLCategory _category = __config.getCategory(category);
        if (_category == null) {
            return Collections.emptyMap();
        }
        Collection<XMLConfigFileHandler.XMLProperty> _properties = _category.getPropertyMap().values();
        Map<String, String> _returnValue = new LinkedHashMap<String, String>(_properties.size());
        for (XMLConfigFileHandler.XMLProperty _prop : _properties) {
            _returnValue.put(_prop.getName(), _prop.getContent());
            for (XMLConfigFileHandler.XMLAttribute _attr : _prop.getAttributeMap().values()) {
                _returnValue.put(_prop.getName().concat(".").concat(_attr.getKey()), _attr.getValue());
            }
        }
        return _returnValue;
    }

    public List<String> getCategoryNames() {
        return new ArrayList<String>(__config.getCategories().keySet());
    }

    public boolean contains(String key) {
        return __config.getDefaultCategory().getProperty(key) != null;
    }

    public boolean contains(String category, String key) {
        XMLConfigFileHandler.XMLCategory _category = __config.getCategory(category);
        return _category != null && _category.getProperty(key) != null;
    }
}