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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.ymate.platform.commons.json.*;
import net.ymate.platform.commons.json.support.JsonArrayFastJsonSerializer;
import net.ymate.platform.commons.json.support.JsonObjectFastJsonSerializer;
import net.ymate.platform.commons.json.support.JsonWrapperFastJsonSerializer;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/9 9:17 下午
 * @since 2.1.0
 */
public class FastJsonAdapter implements IJsonAdapter {

    public static final SerializeConfig SNAKE_CASE_SERIALIZE_CONFIG = new SerializeConfig();

    public static final ParserConfig SNAKE_CASE_PARSE_CONFIG = new ParserConfig();

    static {
        JsonWrapperFastJsonSerializer jsonWrapperFastJsonSerializer = new JsonWrapperFastJsonSerializer();
        JsonObjectFastJsonSerializer jsonObjectFastJsonSerializer = new JsonObjectFastJsonSerializer();
        JsonArrayFastJsonSerializer jsonArrayFastJsonSerializer = new JsonArrayFastJsonSerializer();
        //
        SerializeConfig serializeConfig = SerializeConfig.getGlobalInstance();
        serializeConfig.put(JsonWrapper.class, jsonWrapperFastJsonSerializer);
        serializeConfig.put(FastJsonObjectWrapper.class, jsonObjectFastJsonSerializer);
        serializeConfig.put(FastJsonArrayWrapper.class, jsonArrayFastJsonSerializer);
        serializeConfig.put(IJsonObjectWrapper.class, jsonObjectFastJsonSerializer);
        serializeConfig.put(IJsonArrayWrapper.class, jsonArrayFastJsonSerializer);
        //
        SNAKE_CASE_SERIALIZE_CONFIG.setPropertyNamingStrategy(PropertyNamingStrategy.SnakeCase);
        SNAKE_CASE_SERIALIZE_CONFIG.put(JsonWrapper.class, jsonWrapperFastJsonSerializer);
        SNAKE_CASE_SERIALIZE_CONFIG.put(FastJsonObjectWrapper.class, jsonObjectFastJsonSerializer);
        SNAKE_CASE_SERIALIZE_CONFIG.put(FastJsonArrayWrapper.class, jsonArrayFastJsonSerializer);
        SNAKE_CASE_SERIALIZE_CONFIG.put(IJsonObjectWrapper.class, jsonObjectFastJsonSerializer);
        SNAKE_CASE_SERIALIZE_CONFIG.put(IJsonArrayWrapper.class, jsonArrayFastJsonSerializer);
        //
        ParserConfig parserConfig = ParserConfig.getGlobalInstance();
        parserConfig.setSafeMode(true);
        parserConfig.putDeserializer(JsonWrapper.class, jsonWrapperFastJsonSerializer);
        parserConfig.putDeserializer(FastJsonObjectWrapper.class, jsonObjectFastJsonSerializer);
        parserConfig.putDeserializer(FastJsonArrayWrapper.class, jsonArrayFastJsonSerializer);
        parserConfig.putDeserializer(IJsonObjectWrapper.class, jsonObjectFastJsonSerializer);
        parserConfig.putDeserializer(IJsonArrayWrapper.class, jsonArrayFastJsonSerializer);
        //
        SNAKE_CASE_PARSE_CONFIG.setSafeMode(true);
        SNAKE_CASE_PARSE_CONFIG.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        SNAKE_CASE_PARSE_CONFIG.putDeserializer(JsonWrapper.class, jsonWrapperFastJsonSerializer);
        SNAKE_CASE_PARSE_CONFIG.putDeserializer(FastJsonObjectWrapper.class, jsonObjectFastJsonSerializer);
        SNAKE_CASE_PARSE_CONFIG.putDeserializer(FastJsonArrayWrapper.class, jsonArrayFastJsonSerializer);
        SNAKE_CASE_PARSE_CONFIG.putDeserializer(IJsonObjectWrapper.class, jsonObjectFastJsonSerializer);
        SNAKE_CASE_PARSE_CONFIG.putDeserializer(IJsonArrayWrapper.class, jsonArrayFastJsonSerializer);
    }

    public static JSONObject toJsonObject(Map<?, ?> value) {
        JSONObject jsonObj = new JSONObject(value.size(), value instanceof LinkedHashMap);
        value.forEach((key, v) -> jsonObj.put(String.valueOf(key), JsonWrapper.unwrap(v)));
        return jsonObj;
    }

    public static JSONArray toJsonArray(Collection<?> value) {
        JSONArray jsonArr = new JSONArray(value.size());
        value.stream().map(JsonWrapper::unwrap).forEach(jsonArr::add);
        return jsonArr;
    }

    public FastJsonAdapter() {
    }

    @Override
    public IJsonObjectWrapper createJsonObject() {
        return new FastJsonObjectWrapper(this);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity) {
        return new FastJsonObjectWrapper(this, initialCapacity);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(boolean ordered) {
        return new FastJsonObjectWrapper(this, ordered);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity, boolean ordered) {
        return new FastJsonObjectWrapper(this, initialCapacity, ordered);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(Map<?, ?> map) {
        return new FastJsonObjectWrapper(this, map);
    }

    @Override
    public IJsonArrayWrapper createJsonArray() {
        return new FastJsonArrayWrapper(this);
    }

    @Override
    public IJsonArrayWrapper createJsonArray(int initialCapacity) {
        return new FastJsonArrayWrapper(this, initialCapacity);
    }

    @Override
    public IJsonArrayWrapper createJsonArray(Object[] array) {
        return new FastJsonArrayWrapper(this, array);
    }

    @Override
    public IJsonArrayWrapper createJsonArray(Collection<?> collection) {
        return new FastJsonArrayWrapper(this, collection);
    }

    @Override
    public JsonWrapper fromJson(String jsonStr) {
        JsonWrapper jsonWrapper = null;
        Object obj = JSON.parse(jsonStr, ParserConfig.getGlobalInstance(), Feature.OrderedField);
        if (obj instanceof JSONObject) {
            jsonWrapper = new JsonWrapper(new FastJsonObjectWrapper(this, (JSONObject) obj));
        } else if (obj instanceof JSONArray) {
            jsonWrapper = new JsonWrapper(new FastJsonArrayWrapper(this, (JSONArray) obj));
        }
        return jsonWrapper;
    }

    @Override
    public JsonWrapper toJson(Object object) {
        return toJson(object, false);
    }

    @Override
    public JsonWrapper toJson(Object object, boolean snakeCase) {
        JsonWrapper jsonWrapper = null;
        Object obj = JSON.toJSON(JsonWrapper.unwrap(object), snakeCase ? SNAKE_CASE_SERIALIZE_CONFIG : SerializeConfig.globalInstance);
        if (obj instanceof JSONObject) {
            jsonWrapper = new JsonWrapper(new FastJsonObjectWrapper(this, (JSONObject) obj));
        } else if (obj instanceof JSONArray) {
            jsonWrapper = new JsonWrapper(new FastJsonArrayWrapper(this, (JSONArray) obj));
        }
        return jsonWrapper;
    }

    @Override
    public String toJsonString(Object object) {
        return toJsonString(object, false, false, false);
    }

    @Override
    public String toJsonString(Object object, boolean format) {
        return toJsonString(object, format, false, false);
    }

    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue) {
        return toJsonString(object, format, keepNullValue, false);
    }

    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue, boolean snakeCase) {
        List<SerializerFeature> serializerFeatures = new ArrayList<>();
        if (format) {
            serializerFeatures.add(SerializerFeature.PrettyFormat);
        }
        if (keepNullValue) {
            serializerFeatures.addAll(Arrays.asList(
                    SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteNullBooleanAsFalse,
                    SerializerFeature.WriteNullListAsEmpty,
                    SerializerFeature.WriteNullNumberAsZero,
                    SerializerFeature.WriteNullStringAsEmpty,
                    SerializerFeature.WriteNullNumberAsZero));
        }
        return JSON.toJSONString(JsonWrapper.unwrap(object), snakeCase ? SNAKE_CASE_SERIALIZE_CONFIG : SerializeConfig.getGlobalInstance(), serializerFeatures.toArray(new SerializerFeature[0]));
    }

    @Override
    public byte[] serialize(Object object) throws Exception {
        return serialize(object, false);
    }

    @Override
    public byte[] serialize(Object object, boolean snakeCase) throws Exception {
        JSONSerializer serializer = new JSONSerializer(snakeCase ? SNAKE_CASE_SERIALIZE_CONFIG : SerializeConfig.getGlobalInstance());
        serializer.config(SerializerFeature.WriteEnumUsingToString, true);
//        serializer.config(SerializerFeature.WriteClassName, true);
        serializer.write(JsonWrapper.unwrap(object));
        return serializer.getWriter().toBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(String jsonStr, Class<T> clazz) throws Exception {
        return JSON.parseObject(jsonStr, clazz);
    }

    @Override
    public <T> T deserialize(String jsonStr, boolean snakeCase, Class<T> clazz) throws Exception {
        return JSON.parseObject(jsonStr, clazz, snakeCase ? SNAKE_CASE_PARSE_CONFIG : ParserConfig.getGlobalInstance());
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        return deserialize(bytes, false, clazz);
    }

    @Override
    public <T> T deserialize(byte[] bytes, boolean snakeCase, Class<T> clazz) throws Exception {
        return deserialize(new String(bytes, StandardCharsets.UTF_8), snakeCase, clazz);
    }

    @Override
    public <T> T deserialize(String jsonStr, TypeReferenceWrapper<T> typeRef) throws Exception {
        return deserialize(jsonStr, false, typeRef);
    }

    @Override
    public <T> T deserialize(String jsonStr, boolean snakeCase, TypeReferenceWrapper<T> typeRef) throws Exception {
        return JSON.parseObject(jsonStr, typeRef.getType(), snakeCase ? SNAKE_CASE_PARSE_CONFIG : ParserConfig.getGlobalInstance());
    }

    @Override
    public <T> T deserialize(byte[] bytes, TypeReferenceWrapper<T> typeRef) throws Exception {
        return deserialize(new String(bytes, StandardCharsets.UTF_8), false, typeRef);
    }

    @Override
    public <T> T deserialize(byte[] bytes, boolean snakeCase, TypeReferenceWrapper<T> typeRef) throws Exception {
        return deserialize(new String(bytes, StandardCharsets.UTF_8), snakeCase, typeRef);
    }
}
