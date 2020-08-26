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
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/10 3:05 下午
 * @since 2.1.0
 */
public class GsonObjectWrapper implements IJsonObjectWrapper {

    private final JsonObject jsonObject;

    public GsonObjectWrapper() {
        jsonObject = new JsonObject();
    }

    public GsonObjectWrapper(Map<?, ?> map) {
        this();
        if (map != null && !map.isEmpty()) {
            map.forEach((key, value) -> put(String.valueOf(key), value));
        }
    }

    public GsonObjectWrapper(JsonObject jsonObject) {
        this.jsonObject = jsonObject != null ? jsonObject : new JsonObject();
    }

    @Override
    public IJsonNodeWrapper get(String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement != null ? new GsonNodeWrapper(jsonElement) : null;
    }

    @Override
    public boolean getBoolean(String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement != null && jsonElement.getAsBoolean();
    }

    @Override
    public BigInteger getBigInteger(String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement != null ? jsonElement.getAsBigInteger() : null;
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement != null ? jsonElement.getAsBigDecimal() : null;
    }

    @Override
    public double getDouble(String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement != null ? jsonElement.getAsDouble() : 0d;
    }

    @Override
    public float getFloat(String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement != null ? jsonElement.getAsFloat() : 0f;
    }

    @Override
    public int getInt(String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement != null ? jsonElement.getAsInt() : 0;
    }

    @Override
    public IJsonArrayWrapper getJsonArray(String key) {
        JsonArray value = jsonObject.getAsJsonArray(key);
        return value == null ? null : new GsonArrayWrapper(value);
    }

    @Override
    public IJsonObjectWrapper getJsonObject(String key) {
        JsonObject value = jsonObject.getAsJsonObject(key);
        return value == null ? null : new GsonObjectWrapper(value);
    }

    @Override
    public long getLong(String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement != null ? jsonElement.getAsLong() : 0L;
    }

    @Override
    public String getString(String key) {
        JsonElement jsonElement = jsonObject.get(key);
        return jsonElement != null ? jsonElement.getAsString() : null;
    }

    @Override
    public boolean has(String key) {
        return jsonObject.has(key);
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
        return jsonObject.size() == 0;
    }

    @Override
    public IJsonObjectWrapper put(String key, boolean value) {
        jsonObject.addProperty(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, Collection<?> value) {
        jsonObject.add(key, GsonAdapter.toJsonArray(value));
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, double value) {
        jsonObject.addProperty(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, float value) {
        jsonObject.addProperty(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, int value) {
        jsonObject.addProperty(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, long value) {
        jsonObject.addProperty(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, Map<?, ?> value) {
        jsonObject.add(key, GsonAdapter.toJsonObject(value));
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, Object value) {
        jsonObject.add(key, GsonAdapter.toJsonElement(value));
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
        GsonObjectWrapper that = (GsonObjectWrapper) o;
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
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap() {
        return GsonAdapter.GSON.fromJson(jsonObject, Map.class);
    }
}
