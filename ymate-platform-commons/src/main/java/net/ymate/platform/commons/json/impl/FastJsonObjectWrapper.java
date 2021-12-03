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
package net.ymate.platform.commons.json.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonNodeWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/8 5:12 下午
 * @since 2.1.0
 */
public class FastJsonObjectWrapper implements IJsonObjectWrapper {

    private final JSONObject jsonObject;

    public FastJsonObjectWrapper() {
        this(false);
    }

    public FastJsonObjectWrapper(boolean ordered) {
        jsonObject = new JSONObject(ordered);
    }

    public FastJsonObjectWrapper(int initialCapacity) {
        this(initialCapacity, false);
    }

    public FastJsonObjectWrapper(int initialCapacity, boolean ordered) {
        jsonObject = new JSONObject(initialCapacity, ordered);
    }

    public FastJsonObjectWrapper(Map<?, ?> map) {
        if (map == null) {
            jsonObject = new JSONObject();
        } else {
            jsonObject = new JSONObject(map instanceof LinkedHashMap);
            map.forEach((key, value) -> put(String.valueOf(key), value));
        }
    }

    public FastJsonObjectWrapper(JSONObject jsonObject) {
        this.jsonObject = jsonObject != null ? jsonObject : new JSONObject();
    }

    @Override
    public IJsonNodeWrapper get(String key) {
        Object object = jsonObject.get(key);
        return object != null ? new FastJsonNodeWrapper(object) : null;
    }

    @Override
    public boolean getBoolean(String key) {
        return jsonObject.getBooleanValue(key);
    }

    @Override
    public Boolean getAsBoolean(String key) {
        return jsonObject.getBoolean(key);
    }

    @Override
    public BigInteger getBigInteger(String key) {
        return jsonObject.getBigInteger(key);
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        return jsonObject.getBigDecimal(key);
    }

    @Override
    public double getDouble(String key) {
        return jsonObject.getDoubleValue(key);
    }

    @Override
    public Double getAsDouble(String key) {
        return jsonObject.getDouble(key);
    }

    @Override
    public float getFloat(String key) {
        return jsonObject.getFloatValue(key);
    }

    @Override
    public Float getAsFloat(String key) {
        return jsonObject.getFloat(key);
    }

    @Override
    public int getInt(String key) {
        return jsonObject.getIntValue(key);
    }

    @Override
    public Integer getAsInteger(String key) {
        return jsonObject.getInteger(key);
    }

    @Override
    public IJsonArrayWrapper getJsonArray(String key) {
        JSONArray value = jsonObject.getJSONArray(key);
        return value == null ? null : new FastJsonArrayWrapper(value);
    }

    @Override
    public IJsonObjectWrapper getJsonObject(String key) {
        JSONObject value = jsonObject.getJSONObject(key);
        return value == null ? null : new FastJsonObjectWrapper(value);
    }

    @Override
    public long getLong(String key) {
        return jsonObject.getLongValue(key);
    }

    @Override
    public Long getAsLong(String key) {
        return jsonObject.getLong(key);
    }

    @Override
    public String getString(String key) {
        return jsonObject.getString(key);
    }

    @Override
    public boolean has(String key) {
        return jsonObject.containsKey(key);
    }

    @Override
    public Set<String> keySet() {
        return jsonObject.keySet();
    }

    @Override
    public int size() {
        return jsonObject.size();
    }

    @Override
    public boolean isEmpty() {
        return jsonObject.isEmpty();
    }

    @Override
    public IJsonObjectWrapper put(String key, boolean value) {
        jsonObject.put(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, Collection<?> value) {
        jsonObject.put(key, FastJsonAdapter.toJsonArray(value));
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, double value) {
        jsonObject.put(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, float value) {
        jsonObject.put(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, int value) {
        jsonObject.put(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, long value) {
        jsonObject.put(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, Map<?, ?> value) {
        jsonObject.put(key, FastJsonAdapter.toJsonObject(value));
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, Object value) {
        jsonObject.put(key, JsonWrapper.unwrap(value));
        return this;
    }

    @Override
    public Object remove(String key) {
        return jsonObject.remove(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FastJsonObjectWrapper that = (FastJsonObjectWrapper) o;
        return new EqualsBuilder()
                .append(jsonObject, that.jsonObject)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(jsonObject)
                .toHashCode();
    }

    @Override
    public String toString() {
        return this.toString(false, false);
    }

    @Override
    public String toString(boolean format, boolean keepNullValue) {
        return JsonWrapper.toJsonString(jsonObject, format, keepNullValue);
    }

    @Override
    public String toString(boolean format, boolean keepNullValue, boolean snakeCase) {
        return JsonWrapper.toJsonString(jsonObject, format, keepNullValue, snakeCase);
    }

    @Override
    public Map<String, Object> toMap() {
        return jsonObject;
    }
}
