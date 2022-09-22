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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonNodeWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/10 3:00 下午
 * @since 2.1.0
 */
public class GsonArrayWrapper implements IJsonArrayWrapper {

    private final IJsonAdapter adapter;

    private final JsonArray jsonArray;

    public GsonArrayWrapper(IJsonAdapter adapter) {
        this.adapter = adapter;
        jsonArray = new JsonArray();
    }

    public GsonArrayWrapper(IJsonAdapter adapter, int initialCapacity) {
        this.adapter = adapter;
        jsonArray = new JsonArray(initialCapacity);
    }

    public GsonArrayWrapper(IJsonAdapter adapter, Object[] array) {
        this(adapter, array.length);
        Arrays.stream(array).forEach(this::add);
    }

    public GsonArrayWrapper(IJsonAdapter adapter, Collection<?> collection) {
        this(adapter, collection.size());
        collection.forEach(this::add);
    }

    public GsonArrayWrapper(IJsonAdapter adapter, JsonArray jsonArray) {
        this.adapter = adapter;
        this.jsonArray = jsonArray != null ? jsonArray : new JsonArray();
    }

    @Override
    public IJsonNodeWrapper get(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? new GsonNodeWrapper(adapter, jsonElement) : null;
    }

    @Override
    public boolean getBoolean(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null && jsonElement.getAsBoolean();
    }

    @Override
    public Boolean getAsBoolean(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        if (jsonElement != null) {
            return jsonElement.getAsBoolean();
        }
        return null;
    }

    @Override
    public double getDouble(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsDouble() : 0d;
    }

    @Override
    public Double getAsDouble(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        if (jsonElement != null) {
            return jsonElement.getAsDouble();
        }
        return null;
    }

    @Override
    public float getFloat(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsFloat() : 0f;
    }

    @Override
    public Float getAsFloat(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        if (jsonElement != null) {
            return jsonElement.getAsFloat();
        }
        return null;
    }

    @Override
    public BigDecimal getBigDecimal(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsBigDecimal() : null;
    }

    @Override
    public BigInteger getBigInteger(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsBigInteger() : null;
    }

    @Override
    public int getInt(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsInt() : 0;
    }

    @Override
    public Integer getAsInteger(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        if (jsonElement != null) {
            return jsonElement.getAsInt();
        }
        return null;
    }

    @Override
    public IJsonArrayWrapper getJsonArray(int index) {
        JsonArray value = jsonArray.get(index).getAsJsonArray();
        return value == null ? null : new GsonArrayWrapper(adapter, value);
    }

    @Override
    public IJsonObjectWrapper getJsonObject(int index) {
        JsonObject value = jsonArray.get(index).getAsJsonObject();
        return value == null ? null : new GsonObjectWrapper(adapter, value);
    }

    @Override
    public long getLong(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsLong() : 0L;
    }

    @Override
    public Long getAsLong(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        if (jsonElement != null) {
            return jsonElement.getAsLong();
        }
        return null;
    }

    @Override
    public String getString(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsString() : null;
    }

    @Override
    public boolean isNull(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement == null || jsonElement.isJsonNull();
    }

    @Override
    public int size() {
        return jsonArray.size();
    }

    @Override
    public boolean isEmpty() {
        return jsonArray.size() == 0;
    }

    @Override
    public IJsonArrayWrapper add(boolean value) {
        jsonArray.add(value);
        return this;
    }

    @Override
    public IJsonArrayWrapper add(Collection<?> value) {
        jsonArray.add(GsonAdapter.toJsonArray(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(double value) {
        jsonArray.add(value);
        return this;
    }

    @Override
    public IJsonArrayWrapper add(float value) {
        jsonArray.add(value);
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int value) {
        jsonArray.add(value);
        return this;
    }

    @Override
    public IJsonArrayWrapper add(long value) {
        jsonArray.add(value);
        return this;
    }

    @Override
    public IJsonArrayWrapper add(Map<?, ?> value) {
        jsonArray.add(GsonAdapter.toJsonObject(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(Object value) {
        jsonArray.add(GsonAdapter.toJsonElement(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, boolean value) {
        jsonArray.set(index, GsonAdapter.toJsonElement(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, Collection<?> value) {
        jsonArray.set(index, GsonAdapter.toJsonArray(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, double value) {
        jsonArray.set(index, GsonAdapter.toJsonElement(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, float value) {
        jsonArray.set(index, GsonAdapter.toJsonElement(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, int value) {
        jsonArray.set(index, GsonAdapter.toJsonElement(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, long value) {
        jsonArray.set(index, GsonAdapter.toJsonElement(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, Map<?, ?> value) {
        jsonArray.set(index, GsonAdapter.toJsonObject(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, Object value) {
        jsonArray.set(index, GsonAdapter.toJsonElement(value));
        return this;
    }

    @Override
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
        GsonArrayWrapper that = (GsonArrayWrapper) o;
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

    @Override
    public String toString(boolean format, boolean keepNullValue) {
        return adapter.toJsonString(jsonArray, format, keepNullValue);
    }

    @Override
    public String toString(boolean format, boolean keepNullValue, boolean snakeCase) {
        return adapter.toJsonString(jsonArray, format, keepNullValue, snakeCase);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> toList() {
        return GsonAdapter.GSON.fromJson(jsonArray, List.class);
    }

    @Override
    public Object[] toArray() {
        return toList().toArray();
    }
}
