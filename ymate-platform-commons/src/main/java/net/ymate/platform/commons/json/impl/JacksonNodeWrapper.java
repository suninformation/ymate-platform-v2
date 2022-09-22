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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonNodeWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/20 3:52 下午
 * @since 2.1.0
 */
public class JacksonNodeWrapper implements IJsonNodeWrapper {

    private final IJsonAdapter adapter;

    private final JsonNode jsonNode;

    public JacksonNodeWrapper(IJsonAdapter adapter, JsonNode jsonNode) {
        this.adapter = adapter;
        this.jsonNode = jsonNode;
    }

    @Override
    public Object get() {
        return jsonNode;
    }

    @Override
    public boolean getBoolean() {
        return jsonNode.asBoolean();
    }

    @Override
    public BigInteger getBigInteger() {
        return BigInteger.valueOf(jsonNode.asLong());
    }

    @Override
    public BigDecimal getBigDecimal() {
        return BigDecimal.valueOf(jsonNode.asDouble());
    }

    @Override
    public double getDouble() {
        return jsonNode.asDouble();
    }

    @Override
    public float getFloat() {
        return Double.valueOf(jsonNode.asDouble()).floatValue();
    }

    @Override
    public int getInt() {
        return jsonNode.asInt();
    }

    @Override
    public long getLong() {
        return jsonNode.asLong();
    }

    @Override
    public String getString() {
        return jsonNode.asText();
    }

    @Override
    public boolean isNull() {
        return jsonNode.isNull();
    }

    @Override
    public boolean isJsonArray() {
        return jsonNode.isArray();
    }

    @Override
    public boolean isJsonObject() {
        return jsonNode.isObject();
    }

    @Override
    public IJsonArrayWrapper getJsonArray() {
        return jsonNode.isArray() ? new JacksonArrayWrapper(adapter, (ArrayNode) jsonNode) : null;
    }

    @Override
    public IJsonObjectWrapper getJsonObject() {
        return jsonNode.isObject() ? new JacksonObjectWrapper(adapter, (ObjectNode) jsonNode) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JacksonNodeWrapper that = (JacksonNodeWrapper) o;
        return new EqualsBuilder()
                .append(jsonNode, that.jsonNode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(jsonNode)
                .toHashCode();
    }

    @Override
    public String toString() {
        return adapter.toJsonString(jsonNode, false, false);
    }
}
