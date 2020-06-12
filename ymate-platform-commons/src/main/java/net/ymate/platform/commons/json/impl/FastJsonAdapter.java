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
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/9 9:17 下午
 * @since 2.1.0
 */
public class FastJsonAdapter implements IJsonAdapter {

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
        ParserConfig.getGlobalInstance().setSafeMode(true);
    }

    @Override
    public IJsonObjectWrapper createJsonObject() {
        return new FastJsonObjectWrapper();
    }

    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity) {
        return new FastJsonObjectWrapper(initialCapacity);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(boolean ordered) {
        return new FastJsonObjectWrapper(ordered);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(int initialCapacity, boolean ordered) {
        return new FastJsonObjectWrapper(initialCapacity, ordered);
    }

    @Override
    public IJsonObjectWrapper createJsonObject(Map<?, ?> map) {
        return new FastJsonObjectWrapper(map);
    }

    @Override
    public IJsonArrayWrapper createJsonArray() {
        return new FastJsonArrayWrapper();
    }

    @Override
    public IJsonArrayWrapper createJsonArray(int initialCapacity) {
        return new FastJsonArrayWrapper(initialCapacity);
    }

    @Override
    public IJsonArrayWrapper createJsonArray(Object[] array) {
        return new FastJsonArrayWrapper(array);
    }

    @Override
    public IJsonArrayWrapper createJsonArray(Collection<?> collection) {
        return new FastJsonArrayWrapper(collection);
    }

    @Override
    public JsonWrapper fromJson(String jsonStr) {
        JsonWrapper jsonWrapper = null;
        Object obj = JSON.parse(jsonStr, Feature.OrderedField);
        if (obj instanceof JSONObject) {
            jsonWrapper = new JsonWrapper(new FastJsonObjectWrapper((JSONObject) obj));
        } else if (obj instanceof JSONArray) {
            jsonWrapper = new JsonWrapper(new FastJsonArrayWrapper((JSONArray) obj));
        }
        return jsonWrapper;
    }

    @Override
    public JsonWrapper toJson(Object object) {
        JsonWrapper jsonWrapper = null;
        Object obj = JSON.toJSON(JsonWrapper.unwrap(object));
        if (obj instanceof JSONObject) {
            jsonWrapper = new JsonWrapper(new FastJsonObjectWrapper((JSONObject) obj));
        } else if (obj instanceof JSONArray) {
            jsonWrapper = new JsonWrapper(new FastJsonArrayWrapper((JSONArray) obj));
        }
        return jsonWrapper;
    }

    @Override
    public String toJsonString(Object object, boolean format, boolean keepNullValue) {
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
        return JSON.toJSONString(JsonWrapper.unwrap(object), serializerFeatures.toArray(new SerializerFeature[0]));
    }

    @Override
    public byte[] serialize(Object object) throws Exception {
        JSONSerializer serializer = new JSONSerializer();
        serializer.config(SerializerFeature.WriteEnumUsingToString, true);
        serializer.config(SerializerFeature.WriteClassName, true);
        serializer.write(object);
        return serializer.getWriter().toBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        return JSON.parseObject(new String(bytes, StandardCharsets.UTF_8), clazz);
    }
}
