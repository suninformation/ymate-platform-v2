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
package net.ymate.platform.core.support.impl;

import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.support.IConfigReader;
import net.ymate.platform.core.util.ClassUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/1/3 下午5:24
 * @version 1.0
 */
public class MapSafeConfigReader implements IConfigReader {

    public static IConfigReader bind(Map<String, ?> cfgMap) {
        return new MapSafeConfigReader(cfgMap == null ? Collections.<String, Object>emptyMap() : cfgMap);
    }

    public static IConfigReader bind(Properties props) {
        Map<String, Object> _cfgMap = new HashMap<String, Object>();
        for (Map.Entry _entry : props.entrySet()) {
            String _key = BlurObject.bind(_entry.getKey()).toStringValue();
            _cfgMap.put(_key, _entry.getValue());
        }
        return new MapSafeConfigReader(_cfgMap);
    }

    private Map<String, String> __innerMap;

    public MapSafeConfigReader(Map<String, ?> cfgMap) {
        if (cfgMap == null) {
            throw new NullArgumentException("cfgMap");
        }
        __innerMap = new HashMap<String, String>();
        for (Map.Entry<String, ?> _entry : cfgMap.entrySet()) {
            if (_entry.getValue() instanceof String) {
                __innerMap.put(_entry.getKey(), (String) _entry.getValue());
            } else {
                __innerMap.put(_entry.getKey(), BlurObject.bind(_entry.getValue()).toStringValue());
            }
        }
    }

    @Override
    public String getString(String key) {
        return StringUtils.trimToEmpty(BlurObject.bind(__innerMap.get(key)).toStringValue());
    }

    @Override
    public String getString(String key, String defaultValue) {
        return StringUtils.defaultIfBlank(getString(key), defaultValue);
    }

    @Override
    public List<String> getList(String key) {
        return Arrays.asList(getArray(key));
    }

    @Override
    public Map<String, String> getMap(String keyHead) {
        Map<String, String> _returnValue = new HashMap<String, String>();
        if (StringUtils.isNotBlank(keyHead)) {
            for (Map.Entry<String, ?> _entry : __innerMap.entrySet()) {
                if (StringUtils.startsWith(_entry.getKey(), keyHead)) {
                    String _key = StringUtils.substringAfter(_entry.getKey(), keyHead);
                    _returnValue.put(_key, StringUtils.trimToEmpty(BlurObject.bind(_entry.getValue()).toStringValue()));
                }
            }
        }
        return _returnValue;
    }

    @Override
    public String[] getArray(String key) {
        return StringUtils.split(getString(key), "|");
    }

    @Override
    public String[] getArray(String key, boolean zeroSize) {
        String[] _array = StringUtils.split(getString(key), "|");
        return _array == null || _array.length == 0 ? (zeroSize ? new String[0] : null) : _array;
    }

    private BlurObject __doGetObject(String key, Object defaultValue) {
        Object _value = __innerMap.get(key);
        if (_value == null) {
            _value = defaultValue;
        }
        return BlurObject.bind(_value);
    }

    @Override
    public int getInt(String key) {
        return BlurObject.bind(__innerMap.get(key)).toIntValue();
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return __doGetObject(key, defaultValue).toIntValue();
    }

    @Override
    public boolean getBoolean(String key) {
        return BlurObject.bind(__innerMap.get(key)).toBooleanValue();
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return __doGetObject(key, defaultValue).toBooleanValue();
    }

    @Override
    public long getLong(String key) {
        return BlurObject.bind(__innerMap.get(key)).toLongValue();
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return __doGetObject(key, defaultValue).toLongValue();
    }

    @Override
    public float getFloat(String key) {
        return BlurObject.bind(__innerMap.get(key)).toFloatValue();
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return __doGetObject(key, defaultValue).toFloatValue();
    }

    @Override
    public double getDouble(String key) {
        return BlurObject.bind(__innerMap.get(key)).toDoubleValue();
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return __doGetObject(key, defaultValue).toDoubleValue();
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
        return Collections.unmodifiableMap(__innerMap);
    }

    @Override
    public boolean contains(String key) {
        return __innerMap.containsKey(key);
    }
}
