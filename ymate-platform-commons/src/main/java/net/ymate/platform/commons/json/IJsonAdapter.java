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
package net.ymate.platform.commons.json;

import java.util.Collection;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/8 5:14 下午
 * @since 2.1.0
 */
public interface IJsonAdapter {

    IJsonObjectWrapper createJsonObject();

    IJsonObjectWrapper createJsonObject(int initialCapacity);

    IJsonObjectWrapper createJsonObject(boolean ordered);

    IJsonObjectWrapper createJsonObject(int initialCapacity, boolean ordered);

    IJsonObjectWrapper createJsonObject(Map<?, ?> map);

    IJsonArrayWrapper createJsonArray();

    IJsonArrayWrapper createJsonArray(int initialCapacity);

    IJsonArrayWrapper createJsonArray(Object[] array);

    IJsonArrayWrapper createJsonArray(Collection<?> collection);

    JsonWrapper fromJson(String jsonStr);

    JsonWrapper fromJson(String jsonStr, boolean snakeCase);

    JsonWrapper toJson(Object object);

    String toJsonString(Object object, boolean format, boolean keepNullValue);

    String toJsonString(Object object, boolean format, boolean keepNullValue, boolean snakeCase);

    byte[] serialize(Object object) throws Exception;

    byte[] serialize(Object object, boolean snakeCase) throws Exception;

    <T> T deserialize(String jsonStr, Class<T> clazz) throws Exception;

    <T> T deserialize(String jsonStr, boolean snakeCase, Class<T> clazz) throws Exception;

    <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception;

    <T> T deserialize(byte[] bytes, boolean snakeCase, Class<T> clazz) throws Exception;
}
