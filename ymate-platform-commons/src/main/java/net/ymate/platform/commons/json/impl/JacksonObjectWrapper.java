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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;

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
    public Object get(String key) {
        return objectNode.get(key);
    }

    @Override
    public boolean getBoolean(String key) {
        JsonNode jsonNode = objectNode.get(key);
        return jsonNode != null && jsonNode.booleanValue();
    }

    @Override
    public BigInteger getBigInteger(String key) {
        JsonNode jsonNode = objectNode.get(key);
        return jsonNode != null ? jsonNode.bigIntegerValue() : null;
    }

    @Override
    public BigDecimal getBigDecimal(String key) {
        JsonNode jsonNode = objectNode.get(key);
        return jsonNode != null ? jsonNode.decimalValue() : null;
    }

    @Override
    public double getDouble(String key) {
        JsonNode jsonNode = objectNode.get(key);
        return jsonNode != null ? jsonNode.doubleValue() : 0d;
    }

    @Override
    public float getFloat(String key) {
        JsonNode jsonNode = objectNode.get(key);
        return jsonNode != null ? jsonNode.floatValue() : 0f;
    }

    @Override
    public int getInt(String key) {
        JsonNode jsonNode = objectNode.get(key);
        return jsonNode != null ? jsonNode.intValue() : 0;
    }

    @Override
    public IJsonArrayWrapper getJsonArray(String key) {
        JsonNode jsonNode = objectNode.get(key);
        return jsonNode != null && jsonNode.isArray() ? new JacksonArrayWrapper((ArrayNode) jsonNode) : null;
    }

    @Override
    public IJsonObjectWrapper getJsonObject(String key) {
        JsonNode jsonNode = objectNode.get(key);
        return jsonNode != null && jsonNode.isObject() ? new JacksonObjectWrapper((ObjectNode) jsonNode) : null;
    }

    @Override
    public long getLong(String key) {
        JsonNode jsonNode = objectNode.get(key);
        return jsonNode != null ? jsonNode.longValue() : 0L;
    }

    @Override
    public String getString(String key) {
        JsonNode jsonNode = objectNode.get(key);
        return jsonNode != null ? jsonNode.toString() : null;
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
    public String toString() {
        return this.toString(false, false);
    }

    @Override
    public String toString(boolean format, boolean keepNullValue) {
        return JsonWrapper.toJsonString(objectNode, format, keepNullValue);
    }

    @Override
    public Map<String, Object> toMap() {
        return JacksonAdapter.OBJECT_MAPPER.convertValue(objectNode, new TypeReference<Map<String, Object>>() {
        });
    }
}
