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

import com.google.gson.JsonElement;
import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonNodeWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/20 4:12 下午
 * @since 2.1.0
 */
public class GsonNodeWrapper implements IJsonNodeWrapper {

    private final IJsonAdapter adapter;

    private final JsonElement jsonElement;

    public GsonNodeWrapper(IJsonAdapter adapter, JsonElement jsonElement) {
        this.adapter = adapter;
        this.jsonElement = jsonElement;
    }

    @Override
    public Object get() {
        return jsonElement;
    }

    @Override
    public boolean getBoolean() {
        return jsonElement.getAsBoolean();
    }

    @Override
    public BigInteger getBigInteger() {
        return jsonElement.getAsBigInteger();
    }

    @Override
    public BigDecimal getBigDecimal() {
        return jsonElement.getAsBigDecimal();
    }

    @Override
    public double getDouble() {
        return jsonElement.getAsDouble();
    }

    @Override
    public float getFloat() {
        return jsonElement.getAsFloat();
    }

    @Override
    public int getInt() {
        return jsonElement.getAsInt();
    }

    @Override
    public long getLong() {
        return jsonElement.getAsLong();
    }

    @Override
    public String getString() {
        return jsonElement.getAsString();
    }

    @Override
    public boolean isNull() {
        return jsonElement.isJsonNull();
    }

    @Override
    public boolean isJsonArray() {
        return jsonElement.isJsonArray();
    }

    @Override
    public boolean isJsonObject() {
        return jsonElement.isJsonObject();
    }

    @Override
    public IJsonArrayWrapper getJsonArray() {
        return jsonElement.isJsonArray() ? new GsonArrayWrapper(adapter, jsonElement.getAsJsonArray()) : null;
    }

    @Override
    public IJsonObjectWrapper getJsonObject() {
        return jsonElement.isJsonObject() ? new GsonObjectWrapper(adapter, jsonElement.getAsJsonObject()) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GsonNodeWrapper that = (GsonNodeWrapper) o;
        return new EqualsBuilder()
                .append(jsonElement, that.jsonElement)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(jsonElement)
                .toHashCode();
    }

    @Override
    public String toString() {
        return adapter.toJsonString(jsonElement, false, false);
    }
}
