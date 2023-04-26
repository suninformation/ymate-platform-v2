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

import com.google.gson.*;
import net.ymate.platform.commons.json.*;
import net.ymate.platform.commons.json.support.JsonArrayGsonSerializer;
import net.ymate.platform.commons.json.support.JsonObjectGsonSerializer;
import net.ymate.platform.commons.json.support.JsonWrapperGsonSerializer;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/10 2:58 下午
 * @since 2.1.0
 */
public class GsonAdapter implements IJsonAdapter {

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(JsonWrapper.class, new JsonWrapperGsonSerializer())
            .registerTypeAdapter(IJsonObjectWrapper.class, new JsonObjectGsonSerializer())
            .registerTypeAdapter(IJsonArrayWrapper.class, new JsonArrayGsonSerializer())
            .create();

    public static JsonElement toJsonElement(Object value) {
        return GSON.toJsonTree(JsonWrapper.unwrap(value));
    }

    public static JsonObject toJsonObject(Map<?, ?> value) {
        JsonObject jsonObj = new JsonObject();
        value.forEach((key, v) -> jsonObj.add(String.valueOf(key), toJsonElement(v)));
        return jsonObj;
    }

    public static JsonArray toJsonArray(Collection<?> value) {
        JsonArray jsonArr = new JsonArray(value.size());
        value.stream().map(GsonAdapter::toJsonElement).forEach(jsonArr::add);
        return jsonArr;
    }

    @Override
    public IJsonObjectWrapper createJsonObject() {
        return new GsonObjectWrapper(this);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity) {
        return new GsonObjectWrapper(this);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(boolean ordered) {
        return new GsonObjectWrapper(this);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity, boolean ordered) {
        return new GsonObjectWrapper(this);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(Map<?, ?> map) {
        return new GsonObjectWrapper(this, map);
    }

    @Override
    public IJsonArrayWrapper createJsonArray() {
        return new GsonArrayWrapper(this);
    }

    @Override
    public IJsonArrayWrapper createJsonArray(int initialCapacity) {
        return new GsonArrayWrapper(this, initialCapacity);
    }

    @Override
    public IJsonArrayWrapper createJsonArray(Object[] array) {
        return new GsonArrayWrapper(this, array);
    }

    @Override
    public IJsonArrayWrapper createJsonArray(Collection<?> collection) {
        return new GsonArrayWrapper(this, collection);
    }

    @Override
    public JsonWrapper fromJson(String jsonStr) {
        JsonWrapper jsonWrapper = null;
        Object obj = GSON.fromJson(jsonStr, JsonElement.class);
        if (obj instanceof JsonObject) {
            jsonWrapper = new JsonWrapper(new GsonObjectWrapper(this, (JsonObject) obj));
        } else if (obj instanceof JsonArray) {
            jsonWrapper = new JsonWrapper(new GsonArrayWrapper(this, (JsonArray) obj));
        }
        return jsonWrapper;
    }

    @Override
    public JsonWrapper toJson(Object object) {
        return fromJson(toJsonString(object, false, false));
    }

    @Override
    public JsonWrapper toJson(Object object, boolean snakeCase) {
        return fromJson(toJsonString(object, false, false, snakeCase));
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
        GsonBuilder gsonBuilder = GSON.newBuilder();
        if (format) {
            gsonBuilder.setPrettyPrinting();
        }
        if (snakeCase) {
            gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        }
        if (keepNullValue) {
            gsonBuilder.serializeNulls();
        }
        return gsonBuilder.create().toJson(JsonWrapper.unwrap(object));
    }

    @Override
    public byte[] serialize(Object object) throws Exception {
        return serialize(object, false);
    }

    @Override
    public byte[] serialize(Object object, boolean snakeCase) throws Exception {
        return toJsonString(object, false, false, snakeCase).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(String jsonStr, Class<T> clazz) throws Exception {
        return deserialize(jsonStr, false, clazz);
    }

    @Override
    public <T> T deserialize(String jsonStr, boolean snakeCase, Class<T> clazz) throws Exception {
        if (snakeCase) {
            return GSON.newBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create().fromJson(jsonStr, clazz);
        }
        return GSON.fromJson(jsonStr, clazz);
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
        if (snakeCase) {
            return GSON.newBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create().fromJson(jsonStr, typeRef.getType());
        }
        return GSON.fromJson(jsonStr, typeRef.getType());
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
