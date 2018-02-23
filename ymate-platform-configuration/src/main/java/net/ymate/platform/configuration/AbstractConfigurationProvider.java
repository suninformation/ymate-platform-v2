/*
 * Copyright 2007-2017 the original author or authors.
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
package net.ymate.platform.configuration;

import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.util.FileUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/7/31 上午12:39
 * @version 1.0
 */
public abstract class AbstractConfigurationProvider implements IConfigurationProvider {

    /**
     * 配置对象缓存，对于重复的文件加载会使用缓存，减少文件读写频率
     */
    private static final Map<String, IConfigFileParser> __CONFIG_CACHE_MAPS = new ConcurrentHashMap<String, IConfigFileParser>();

    private static final ConcurrentHashMap<String, ReentrantLock> __LOCK_CACHES = new ConcurrentHashMap<String, ReentrantLock>();

    /**
     * 配置对象
     */
    private IConfigFileParser __configFileParser;

    /**
     * 装载配置文件参数
     */
    private String __cfgFileName;

    @Override
    public void load(String cfgFileName) throws Exception {
        if (StringUtils.isBlank(cfgFileName)) {
            throw new NullArgumentException("cfgFileName");
        }
        __cfgFileName = cfgFileName;
        //
        ReentrantLock _locker = __doGetLocker();
        try {
            _locker.lock();
            __doLoad();
        } finally {
            if (_locker.isLocked()) {
                _locker.unlock();
            }
        }
    }

    private ReentrantLock __doGetLocker() {
        ReentrantLock _locker = __LOCK_CACHES.get(__cfgFileName);
        if (_locker == null) {
            _locker = new ReentrantLock();
            ReentrantLock _previous = __LOCK_CACHES.putIfAbsent(__cfgFileName, _locker);
            if (_previous != null) {
                _locker = _previous;
            }
        }
        return _locker;
    }

    private void __doLoad() throws Exception {
        if ((__configFileParser = __CONFIG_CACHE_MAPS.get(__cfgFileName)) == null) {
            __configFileParser = __buildConfigFileParser(FileUtils.toURL(__cfgFileName)).load(true);
            __CONFIG_CACHE_MAPS.put(__cfgFileName, __configFileParser);
        }
    }

    protected abstract IConfigFileParser __buildConfigFileParser(URL cfgFileName) throws Exception;

    protected IConfigFileParser __getConfigFileParser() {
        return __configFileParser;
    }

    @Override
    public void reload() throws Exception {
        ReentrantLock _locker = __doGetLocker();
        try {
            _locker.lock();
            // 移除缓存项
            __CONFIG_CACHE_MAPS.remove(__cfgFileName);
            // 加载配置
            __doLoad();
        } finally {
            if (_locker.isLocked()) {
                _locker.unlock();
            }
        }
    }

    @Override
    public String getCfgFileName() {
        return __cfgFileName;
    }

    @Override
    public String getString(String key) {
        IConfigFileParser.Property _prop = __configFileParser.getDefaultCategory().getProperty(key);
        return _prop == null ? null : _prop.getContent();
    }

    @Override
    public String getString(String key, String defaultValue) {
        return StringUtils.defaultIfEmpty(getString(key), defaultValue);
    }

    @Override
    public String getString(String category, String key, String defaultValue) {
        IConfigFileParser.Category _category = __configFileParser.getCategory(category);
        if (_category == null) {
            return null;
        }
        IConfigFileParser.Property _prop = _category.getProperty(key);
        return StringUtils.defaultIfEmpty(_prop == null ? null : _prop.getContent(), defaultValue);
    }

    @Override
    public List<String> getList(String key) {
        return getList(IConfigFileParser.DEFAULT_CATEGORY_NAME, key);
    }

    @Override
    public List<String> getList(String category, String key) {
        List<String> _returnValue = new ArrayList<String>();
        IConfigFileParser.Property _prop = __configFileParser.getCategory(category).getProperty(key);
        if (_prop != null) {
            for (IConfigFileParser.Attribute _attr : _prop.getAttributeMap().values()) {
                if (StringUtils.isBlank(_attr.getValue())) {
                    _returnValue.add(_attr.getKey());
                }
            }
        }
        return _returnValue;
    }

    @Override
    public Map<String, String> getMap(String key) {
        return getMap(IConfigFileParser.DEFAULT_CATEGORY_NAME, key);
    }

    @Override
    public Map<String, String> getMap(String category, String key) {
        Map<String, String> _returnValue = new LinkedHashMap<String, String>();
        IConfigFileParser.Property _prop = __configFileParser.getCategory(category).getProperty(key);
        if (_prop != null) {
            for (IConfigFileParser.Attribute _attr : _prop.getAttributeMap().values()) {
                if (StringUtils.isNotBlank(_attr.getValue())) {
                    _returnValue.put(_attr.getKey(), _attr.getValue());
                }
            }
        }
        return _returnValue;
    }

    @Override
    public String[] getArray(String key) {
        List<String> _resultValue = getList(key);
        return _resultValue.toArray(new String[_resultValue.size()]);
    }

    @Override
    public String[] getArray(String key, boolean zeroSize) {
        return getArray(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, zeroSize);
    }

    @Override
    public String[] getArray(String category, String key, boolean zeroSize) {
        List<String> _values = getList(category, key);
        if (_values.isEmpty() && !zeroSize) {
            return null;
        }
        return _values.toArray(new String[_values.size()]);
    }

    @Override
    public int getInt(String key) {
        return getInt(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, 0);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return getInt(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    @Override
    public int getInt(String category, String key, int defaultValue) {
        IConfigFileParser.Category _category = __configFileParser.getCategory(category);
        if (_category != null) {
            IConfigFileParser.Property _prop = _category.getProperty(key);
            if (_prop != null) {
                return new BlurObject(_prop.getContent()).toIntValue();
            }
        }
        return defaultValue;
    }

    @Override
    public boolean getBoolean(String key) {
        return getBoolean(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, false);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return getBoolean(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    @Override
    public boolean getBoolean(String category, String key, boolean defaultValue) {
        IConfigFileParser.Category _category = __configFileParser.getCategory(category);
        if (_category != null) {
            IConfigFileParser.Property _prop = _category.getProperty(key);
            if (_prop != null) {
                return new BlurObject(_prop.getContent()).toBooleanValue();
            }
        }
        return defaultValue;
    }

    @Override
    public long getLong(String key) {
        return getLong(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, 0L);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return getLong(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    @Override
    public long getLong(String category, String key, long defaultValue) {
        IConfigFileParser.Category _category = __configFileParser.getCategory(category);
        if (_category != null) {
            IConfigFileParser.Property _prop = _category.getProperty(key);
            if (_prop != null) {
                return new BlurObject(_prop.getContent()).toLongValue();
            }
        }
        return defaultValue;
    }

    @Override
    public float getFloat(String key) {
        return getFloat(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, 0f);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return getFloat(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    @Override
    public float getFloat(String category, String key, float defaultValue) {
        IConfigFileParser.Category _category = __configFileParser.getCategory(category);
        if (_category != null) {
            IConfigFileParser.Property _prop = _category.getProperty(key);
            if (_prop != null) {
                return new BlurObject(_prop.getContent()).toFloatValue();
            }
        }
        return defaultValue;
    }

    @Override
    public double getDouble(String key) {
        return getDouble(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, 0d);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return getDouble(IConfigFileParser.DEFAULT_CATEGORY_NAME, key, defaultValue);
    }

    @Override
    public double getDouble(String category, String key, double defaultValue) {
        IConfigFileParser.Category _category = __configFileParser.getCategory(category);
        if (_category != null) {
            IConfigFileParser.Property _prop = _category.getProperty(key);
            if (_prop != null) {
                return new BlurObject(_prop.getContent()).toDoubleValue();
            }
        }
        return defaultValue;
    }

    @Override
    public Map<String, String> toMap() {
        return toMap(IConfigFileParser.DEFAULT_CATEGORY_NAME);
    }

    @Override
    public Map<String, String> toMap(String category) {
        IConfigFileParser.Category _category = __configFileParser.getCategory(category);
        if (_category == null) {
            return Collections.emptyMap();
        }
        Collection<IConfigFileParser.Property> _properties = _category.getPropertyMap().values();
        Map<String, String> _returnValue = new LinkedHashMap<String, String>(_properties.size());
        for (IConfigFileParser.Property _prop : _properties) {
            _returnValue.put(_prop.getName(), _prop.getContent());
            for (IConfigFileParser.Attribute _attr : _prop.getAttributeMap().values()) {
                _returnValue.put(_prop.getName().concat(".").concat(_attr.getKey()), _attr.getValue());
            }
        }
        return _returnValue;
    }

    @Override
    public List<String> getCategoryNames() {
        return new ArrayList<String>(__configFileParser.getCategories().keySet());
    }

    @Override
    public boolean contains(String key) {
        return __configFileParser.getDefaultCategory().getProperty(key) != null;
    }

    @Override
    public boolean contains(String category, String key) {
        IConfigFileParser.Category _category = __configFileParser.getCategory(category);
        return _category != null && _category.getProperty(key) != null;
    }
}
