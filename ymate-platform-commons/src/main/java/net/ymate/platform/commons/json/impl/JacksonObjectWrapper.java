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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonNodeWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/11 3:40 下午
 * @since 2.1.0
 */
public class JacksonObjectWrapper implements IJsonObjectWrapper {

    private final ObjectNode objectNode;

    public JacksonObjectWrapper() {
        this.objectNode = JacksonAdapter.OBJECT_MAPPER.createObjectNode();
    }

    public JacksonObjectWrapper(Map<?, ?> map) {
        this();
        if (map != null && !map.isEmpty()) {
            map.forEach((key, value) -> put(String.valueOf(key), value));
        }
    }

    public JacksonObjectWrapper(ObjectNode objectNode) {
        this.objectNode = objectNode != null ? objectNode : JacksonAdapter.OBJECT_MAPPER.createObjectNode();
    }

    @Override
    public IJsonNodeWrapper get(String key) {
        JsonNode jsonNode = objectNode.get(key);
        return jsonNode != null ? new JacksonNodeWrapper(jsonNode) : null;
    }

    @Override
    public boolean getBoolean(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null && jsonNode.getBoolean();
    }

    @Override
    public Boolean getAsBoolean(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        if (jsonNode != null) {
            return jsonNode.getBoolean();
        }
        return null;
    }

    @Override
    public BigInteger getBigInteger(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getBigInteger() : null;
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getBigDecimal() : null;
    }

    @Override
    public double getDouble(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getDouble() : 0d;
    }

    @Override
    public Double getAsDouble(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        if (jsonNode != null) {
            return jsonNode.getDouble();
        }
        return null;
    }

    @Override
    public float getFloat(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getFloat() : 0f;
    }

    @Override
    public Float getAsFloat(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        if (jsonNode != null) {
            return jsonNode.getFloat();
        }
        return null;
    }

    @Override
    public int getInt(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getInt() : 0;
    }

    @Override
    public Integer getAsInteger(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        if (jsonNode != null) {
            return jsonNode.getInt();
        }
        return null;
    }

    @Override
    public IJsonArrayWrapper getJsonArray(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getJsonArray() : null;
    }

    @Override
    public IJsonObjectWrapper getJsonObject(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getJsonObject() : null;
    }

    @Override
    public long getLong(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getLong() : 0L;
    }

    @Override
    public Long getAsLong(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        if (jsonNode != null) {
            return jsonNode.getLong();
        }
        return null;
    }

    @Override
    public String getString(String key) {
        IJsonNodeWrapper jsonNode = get(key);
        return jsonNode != null ? jsonNode.getString() : null;
    }

    @Override
    public boolean has(String key) {
        return objectNode.has(key);
    }

    @Override
    public Set<String> keySet() {
        Set<String> keySet = new LinkedHashSet<>(objectNode.size());
        Iterator<String> iterator = objectNode.fieldNames();
        while (iterator.hasNext()) {
            keySet.add(iterator.next());
        }
        return keySet;
    }

    @Override
    public int size() {
        return objectNode.size();
    }

    @Override
    public boolean isEmpty() {
        return objectNode.isEmpty();
    }

    @Override
    public IJsonObjectWrapper put(String key, boolean value) {
        objectNode.put(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, Collection<?> value) {
        objectNode.set(key, JacksonAdapter.toArrayNode(value));
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, double value) {
        objectNode.put(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, float value) {
        objectNode.put(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, int value) {
        objectNode.put(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, long value) {
        objectNode.put(key, value);
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, Map<?, ?> value) {
        objectNode.set(key, JacksonAdapter.toObjectNode(value));
        return this;
    }

    @Override
    public IJsonObjectWrapper put(String key, Object value) {
        objectNode.set(key, JacksonAdapter.toJsonNode(value));
        return this;
    }

    @Override
    public Object remove(String key) {
        return objectNode.remove(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JacksonObjectWrapper that = (JacksonObjectWrapper) o;
        return new EqualsBuilder()
                .append(objectNode, that.objectNode)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(objectNode)
                .toHashCode();
    }

    @Override
    public String toString() {
        return this.toString(false, false);
    }

    @Override
    public String toString(boolean format, boolean keepNullValue) {
        return JsonWrapper.toJsonString(objectNode, format, keepNullValue);
    }

    @Override
    public String toString(boolean format, boolean keepNullValue, boolean snakeCase) {
        return JsonWrapper.toJsonString(objectNode, format, keepNullValue, snakeCase);
    }

    @Override
    public Map<String, Object> toMap() {
        return JacksonAdapter.OBJECT_MAPPER.convertValue(objectNode, new TypeReference<Map<String, Object>>() {
        });
    }
}
