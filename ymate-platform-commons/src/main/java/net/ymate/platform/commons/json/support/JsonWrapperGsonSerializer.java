/*
 * Copyright 2007-present the original author or authors.
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
package net.ymate.platform.commons.json.support;

import com.google.gson.*;
import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.json.impl.GsonAdapter;

import java.lang.reflect.Type;

/**
 * Gson JsonWrapper序列化器实现，提供JsonWrapper的序列化和反序列化功能。
 * <p>
 * 设计目的：为Gson提供JsonWrapper类型的序列化和反序列化支持。
 * <p>
 * 使用场景：
 * - 当需要使用Gson序列化JsonWrapper类型时
 * - 当需要使用Gson反序列化JSON到JsonWrapper类型时
 *
 * @author 刘镇 (suninformation@163.com) on 2021/12/27 2:46 上午
 * @since 2.1.0
 */
public class JsonWrapperGsonSerializer implements JsonSerializer<JsonWrapper>, JsonDeserializer<JsonWrapper> {

    /**
     * Gson适配器实例，用于JSON序列化和反序列化操作。
     */
    private final IJsonAdapter adapter = new GsonAdapter();

    /**
     * 将JsonWrapper类型序列化为JsonElement。
     *
     * @param jsonWrapper              要序列化的JsonWrapper实例
     * @param type                     类型
     * @param jsonSerializationContext 序列化上下文
     * @return 序列化后的JsonElement，若输入为null则返回null
     */
    @Override
    public JsonElement serialize(JsonWrapper jsonWrapper, Type type, JsonSerializationContext jsonSerializationContext) {
        return GsonAdapter.toJsonElement(jsonWrapper);
    }

    /**
     * 将JsonElement反序列化为JsonWrapper类型。
     *
     * @param jsonElement                要反序列化的JsonElement
     * @param type                       类型
     * @param jsonDeserializationContext 反序列化上下文
     * @return 反序列化后的JsonWrapper实例，若反序列化失败则返回null
     * @throws JsonParseException 当JSON解析失败时抛出
     */
    @Override
    public JsonWrapper deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return adapter.toJson(jsonElement);
    }
}
