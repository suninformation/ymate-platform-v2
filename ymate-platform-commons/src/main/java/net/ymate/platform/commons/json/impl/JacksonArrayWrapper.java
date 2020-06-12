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
 * @author 刘镇 (suninformation@163.com) on 2020/6/11 3:41 下午
 * @since 2.1.0
 */
public class JacksonArrayWrapper implements IJsonArrayWrapper {

    private final ArrayNode arrayNode;

    public JacksonArrayWrapper() {
        this.arrayNode = JacksonAdapter.OBJECT_MAPPER.createArrayNode();
    }

    public JacksonArrayWrapper(Object[] array) {
        this();
        Arrays.stream(array).forEach(this::add);
    }

    public JacksonArrayWrapper(Collection<?> collection) {
        this();
        collection.forEach(this::add);
    }

    public JacksonArrayWrapper(ArrayNode arrayNode) {
        this.arrayNode = arrayNode != null ? arrayNode : JacksonAdapter.OBJECT_MAPPER.createArrayNode();
    }

    @Override
    public Object get(int index) {
        return arrayNode.get(index);
    }

    @Override
    public boolean getBoolean(int index) {
        JsonNode jsonNode = arrayNode.get(index);
        return jsonNode != null && jsonNode.booleanValue();
    }

    @Override
    public double getDouble(int index) {
        JsonNode jsonNode = arrayNode.get(index);
        return jsonNode != null ? jsonNode.doubleValue() : 0d;
    }

    @Override
    public float getFloat(int index) {
        JsonNode jsonNode = arrayNode.get(index);
        return jsonNode != null ? jsonNode.floatValue() : 0f;
    }

    @Override
    public BigDecimal getBigDecimal(int index) {
        JsonNode jsonNode = arrayNode.get(index);
        return jsonNode != null ? jsonNode.decimalValue() : null;
    }

    @Override
    public BigInteger getBigInteger(int index) {
        JsonNode jsonNode = arrayNode.get(index);
        return jsonNode != null ? jsonNode.bigIntegerValue() : null;
    }

    @Override
    public int getInt(int index) {
        JsonNode jsonNode = arrayNode.get(index);
        return jsonNode != null ? jsonNode.intValue() : 0;
    }

    @Override
    public IJsonArrayWrapper getJsonArray(int index) {
        JsonNode jsonNode = arrayNode.get(index);
        return jsonNode != null && jsonNode.isArray() ? new JacksonArrayWrapper((ArrayNode) jsonNode) : null;
    }

    @Override
    public IJsonObjectWrapper getJsonObject(int index) {
        JsonNode jsonNode = arrayNode.get(index);
        return jsonNode != null && jsonNode.isObject() ? new JacksonObjectWrapper((ObjectNode) jsonNode) : null;
    }

    @Override
    public long getLong(int index) {
        JsonNode jsonNode = arrayNode.get(index);
        return jsonNode != null ? jsonNode.longValue() : 0L;
    }

    @Override
    public String getString(int index) {
        JsonNode jsonNode = arrayNode.get(index);
        return jsonNode != null ? jsonNode.toString() : null;
    }

    @Override
    public boolean isNull(int index) {
        JsonNode jsonNode = arrayNode.get(index);
        return jsonNode == null || jsonNode.isNull();
    }

    @Override
    public int size() {
        return arrayNode.size();
    }

    @Override
    public boolean isEmpty() {
        return arrayNode.isEmpty();
    }

    @Override
    public IJsonArrayWrapper add(boolean value) {
        arrayNode.add(value);
        return this;
    }

    @Override
    public IJsonArrayWrapper add(Collection<?> value) {
        arrayNode.add(JacksonAdapter.toArrayNode(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(double value) {
        arrayNode.add(value);
        return this;
    }

    @Override
    public IJsonArrayWrapper add(float value) {
        arrayNode.add(value);
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int value) {
        arrayNode.add(value);
        return this;
    }

    @Override
    public IJsonArrayWrapper add(long value) {
        arrayNode.add(value);
        return this;
    }

    @Override
    public IJsonArrayWrapper add(Map<?, ?> value) {
        arrayNode.add(JacksonAdapter.toObjectNode(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(Object value) {
        arrayNode.add(JacksonAdapter.toJsonNode(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, boolean value) {
        arrayNode.set(index, JacksonAdapter.toJsonNode(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, Collection<?> value) {
        arrayNode.set(index, JacksonAdapter.toArrayNode(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, double value) {
        arrayNode.set(index, JacksonAdapter.toJsonNode(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, float value) {
        arrayNode.set(index, JacksonAdapter.toJsonNode(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, int value) {
        arrayNode.set(index, JacksonAdapter.toJsonNode(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, long value) {
        arrayNode.set(index, JacksonAdapter.toJsonNode(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, Map<?, ?> value) {
        arrayNode.set(index, JacksonAdapter.toObjectNode(value));
        return this;
    }

    @Override
    public IJsonArrayWrapper add(int index, Object value) {
        arrayNode.set(index, JacksonAdapter.toJsonNode(value));
        return this;
    }

    @Override
    public Object remove(int index) {
        return arrayNode.remove(index);
    }

    @Override
    public String toString() {
        return this.toString(false, false);
    }

    @Override
    public String toString(boolean format, boolean keepNullValue) {
        return JsonWrapper.toJsonString(arrayNode, format, keepNullValue);
    }

    @Override
    public List<Object> toList() {
        return JacksonAdapter.OBJECT_MAPPER.convertValue(arrayNode, new TypeReference<ArrayList<Object>>() {
        });
    }

    @Override
    public Object[] toArray() {
        return toList().toArray();
    }
}
