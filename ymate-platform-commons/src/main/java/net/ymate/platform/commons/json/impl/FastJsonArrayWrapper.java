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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/8 5:13 下午
 * @since 2.1.0
 */
public class FastJsonArrayWrapper implements IJsonArrayWrapper {

    private final JSONArray jsonArray;

    public FastJsonArrayWrapper() {
        jsonArray = new JSONArray();
    }

    public FastJsonArrayWrapper(int initialCapacity) {
        jsonArray = new JSONArray(initialCapacity);
    }

    public FastJsonArrayWrapper(Object[] array) {
        this(array.length);
        Arrays.stream(array).forEach(this::add);
    }

    public FastJsonArrayWrapper(Collection<?> collection) {
        this(collection.size());
        collection.forEach(this::add);
    }

    public FastJsonArrayWrapper(JSONArray jsonArray) {
        this.jsonArray = jsonArray != null ? jsonArray : new JSONArray();
    }

    public IJsonNodeWrapper get(int index) {
        Object object = jsonArray.get(index);
        return object != null ? new FastJsonNodeWrapper(object) : null;
    }

    public boolean getBoolean(int index) {
        return jsonArray.getBooleanValue(index);
    }

    public double getDouble(int index) {
        return jsonArray.getDoubleValue(index);
    }

    public float getFloat(int index) {
        return jsonArray.getFloatValue(index);
    }

    public BigDecimal getBigDecimal(int index) {
        return jsonArray.getBigDecimal(index);
    }

    public BigInteger getBigInteger(int index) {
        return jsonArray.getBigInteger(index);
    }

    public int getInt(int index) {
        return jsonArray.getIntValue(index);
    }

    public IJsonArrayWrapper getJsonArray(int index) {
        JSONArray value = jsonArray.getJSONArray(index);
        return value == null ? null : new FastJsonArrayWrapper(value);
    }

    public IJsonObjectWrapper getJsonObject(int index) {
        JSONObject value = jsonArray.getJSONObject(index);
        return value == null ? null : new FastJsonObjectWrapper(value);
    }

    public long getLong(int index) {
        return jsonArray.getLongValue(index);
    }

    public String getString(int index) {
        return jsonArray.getString(index);
    }

    public boolean isNull(int index) {
        return jsonArray.get(index) == null;
    }

    public int size() {
        return jsonArray.size();
    }

    public boolean isEmpty() {
        return jsonArray.isEmpty();
    }

    public IJsonArrayWrapper add(boolean value) {
        jsonArray.add(value);
        return this;
    }

    public IJsonArrayWrapper add(Collection<?> value) {
        jsonArray.add(FastJsonAdapter.toJsonArray(value));
        return this;
    }

    public IJsonArrayWrapper add(double value) {
        jsonArray.add(value);
        return this;
    }

    public IJsonArrayWrapper add(float value) {
        jsonArray.add(value);
        return this;
    }

    public IJsonArrayWrapper add(int value) {
        jsonArray.add(value);
        return this;
    }

    public IJsonArrayWrapper add(long value) {
        jsonArray.add(value);
        return this;
    }

    public IJsonArrayWrapper add(Map<?, ?> value) {
        jsonArray.add(FastJsonAdapter.toJsonObject(value));
        return this;
    }

    public IJsonArrayWrapper add(Object value) {
        jsonArray.add(JsonWrapper.unwrap(value));
        return this;
    }

    public IJsonArrayWrapper add(int index, boolean value) {
        jsonArray.add(index, value);
        return this;
    }

    public IJsonArrayWrapper add(int index, Collection<?> value) {
        jsonArray.add(index, value);
        return this;
    }

    public IJsonArrayWrapper add(int index, double value) {
        jsonArray.add(index, value);
        return this;
    }

    public IJsonArrayWrapper add(int index, float value) {
        jsonArray.add(index, value);
        return this;
    }

    public IJsonArrayWrapper add(int index, int value) {
        jsonArray.add(index, value);
        return this;
    }

    public IJsonArrayWrapper add(int index, long value) {
        jsonArray.add(index, value);
        return this;
    }

    public IJsonArrayWrapper add(int index, Map<?, ?> value) {
        jsonArray.add(index, FastJsonAdapter.toJsonObject(value));
        return this;
    }

    public IJsonArrayWrapper add(int index, Object value) {
        jsonArray.add(index, JsonWrapper.unwrap(value));
        return this;
    }

    public Object remove(int index) {
        return jsonArray.remove(index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FastJsonArrayWrapper that = (FastJsonArrayWrapper) o;
        return new EqualsBuilder()
                .append(jsonArray, that.jsonArray)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(jsonArray)
                .toHashCode();
    }

    @Override
    public String toString() {
        return this.toString(false, false);
    }

    public String toString(boolean format, boolean keepNullValue) {
        return JsonWrapper.toJsonString(jsonArray, format, keepNullValue);
    }

    public List<Object> toList() {
        return jsonArray;
    }

    @Override
    public Object[] toArray() {
        return jsonArray.toArray();
    }
}
