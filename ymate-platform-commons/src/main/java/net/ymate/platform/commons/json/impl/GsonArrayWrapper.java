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
 * @author 刘镇 (suninformation@163.com) on 2020/6/10 3:00 下午
 * @since 2.1.0
 */
public class GsonArrayWrapper implements IJsonArrayWrapper {

    private final JsonArray jsonArray;

    public GsonArrayWrapper() {
        jsonArray = new JsonArray();
    }

    public GsonArrayWrapper(int initialCapacity) {
        jsonArray = new JsonArray(initialCapacity);
    }

    public GsonArrayWrapper(Object[] array) {
        this(array.length);
        Arrays.stream(array).forEach(this::add);
    }

    public GsonArrayWrapper(Collection<?> collection) {
        this(collection.size());
        collection.forEach(this::add);
    }

    public GsonArrayWrapper(JsonArray jsonArray) {
        this.jsonArray = jsonArray != null ? jsonArray : new JsonArray();
    }

    @Override
    public IJsonNodeWrapper get(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? new GsonNodeWrapper(jsonElement) : null;
    }

    @Override
    public boolean getBoolean(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null && jsonElement.getAsBoolean();
    }

    @Override
    public double getDouble(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsDouble() : 0d;
    }

    @Override
    public float getFloat(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsFloat() : 0f;
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
    public IJsonArrayWrapper getJsonArray(int index) {
        JsonArray value = jsonArray.get(index).getAsJsonArray();
        return value == null ? null : new GsonArrayWrapper(value);
    }

    @Override
    public IJsonObjectWrapper getJsonObject(int index) {
        JsonObject value = jsonArray.get(index).getAsJsonObject();
        return value == null ? null : new GsonObjectWrapper(value);
    }

    @Override
    public long getLong(int index) {
        JsonElement jsonElement = jsonArray.get(index);
        return jsonElement != null ? jsonElement.getAsLong() : 0L;
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
        return JsonWrapper.toJsonString(jsonArray, format, keepNullValue);
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
