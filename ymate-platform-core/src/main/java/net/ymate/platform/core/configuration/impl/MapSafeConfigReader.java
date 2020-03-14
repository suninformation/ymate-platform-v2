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
package net.ymate.platform.core.configuration.impl;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.configuration.IConfigReader;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/1/3 下午5:24
 */
public class MapSafeConfigReader implements IConfigReader {

    private final Map<String, String> innerMap;

    public static Map<String, String> keyStartsWith(Map<String, String> map, String keyPrefix) {
        Map<String, String> returnValues = new HashMap<>(16);
        if (StringUtils.isNotBlank(keyPrefix)) {
            map.forEach((key, value) -> {
                if (StringUtils.startsWith(key, keyPrefix)) {
                    String keyStr = StringUtils.substringAfter(key, keyPrefix);
                    if (StringUtils.isNotBlank(keyStr) && StringUtils.isNotBlank(value)) {
                        returnValues.put(keyStr, value);
                    }
                }
            });
        }
        return returnValues;
    }

    public static IConfigReader bind(Map<?, ?> cfgMap) {
        return new MapSafeConfigReader(cfgMap == null ? Collections.emptyMap() : cfgMap);
    }

    public static IConfigReader bind(Properties props) {
        Map<String, Object> cfgMap = new HashMap<>(16);
        props.forEach((key1, value) -> {
            String key = BlurObject.bind(key1).toStringValue();
            cfgMap.put(key, value);
        });
        return new MapSafeConfigReader(cfgMap);
    }

    private MapSafeConfigReader(Map<?, ?> cfgMap) {
        if (cfgMap == null) {
            throw new NullArgumentException("cfgMap");
        }
        innerMap = new HashMap<>();
        cfgMap.forEach((key, value) -> {
            String keyStr = BlurObject.bind(key).toStringValue();
            if (value instanceof String) {
                innerMap.put(keyStr, (String) value);
            } else {
                innerMap.put(keyStr, BlurObject.bind(value).toStringValue());
            }
        });
    }

    @Override
    public String getString(String key) {
        return BlurObject.bind(innerMap.get(key)).toStringValue();
    }

    @Override
    public String getString(String key, String defaultValue) {
        return StringUtils.defaultIfBlank(getString(key), defaultValue);
    }

    @Override
    public List<String> getList(String key) {
        String[] array = getArray(key);
        if (array == null) {
            return new ArrayList<>();
        }
        return Arrays.asList(array);
    }

    @Override
    public Map<String, String> getMap(String keyHead) {
        return keyStartsWith(innerMap, keyHead);
    }

    @Override
    public String[] getArray(String key) {
        return StringUtils.split(getString(key), "|");
    }

    @Override
    public String[] getArray(String key, String[] defaultValue) {
        String[] array = getArray(key);
        return array == null || array.length == 0 ? defaultValue : array;
    }

    @Override
    public String[] getArray(String key, boolean zeroSize) {
        String[] array = StringUtils.split(getString(key), "|");
        return array == null || array.length == 0 ? (zeroSize ? new String[0] : null) : array;
    }

    private BlurObject getObject(String key, Object defaultValue) {
        Object value = innerMap.get(key);
        if (StringUtils.isBlank((CharSequence) value)) {
            value = defaultValue;
        }
        return BlurObject.bind(value);
    }

    @Override
    public int getInt(String key) {
        return BlurObject.bind(innerMap.get(key)).toIntValue();
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return getObject(key, defaultValue).toIntValue();
    }

    @Override
    public boolean getBoolean(String key) {
        return BlurObject.bind(innerMap.get(key)).toBooleanValue();
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return getObject(key, defaultValue).toBooleanValue();
    }

    @Override
    public long getLong(String key) {
        return BlurObject.bind(innerMap.get(key)).toLongValue();
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return getObject(key, defaultValue).toLongValue();
    }

    @Override
    public float getFloat(String key) {
        return BlurObject.bind(innerMap.get(key)).toFloatValue();
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return getObject(key, defaultValue).toFloatValue();
    }

    @Override
    public double getDouble(String key) {
        return BlurObject.bind(innerMap.get(key)).toDoubleValue();
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return getObject(key, defaultValue).toDoubleValue();
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
        return Collections.unmodifiableMap(innerMap);
    }

    @Override
    public boolean contains(String key) {
        return innerMap.containsKey(key);
    }
}
