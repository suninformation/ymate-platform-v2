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

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/9 3:35 下午
 * @since 2.1.0
 */
public interface IJsonObjectWrapper extends Serializable {

    IJsonNodeWrapper get(String key);

    boolean getBoolean(String key);

    BigInteger getBigInteger(String key);

    BigDecimal getBigDecimal(String key);

    double getDouble(String key);

    float getFloat(String key);

    int getInt(String key);

    IJsonArrayWrapper getJsonArray(String key);

    IJsonObjectWrapper getJsonObject(String key);

    long getLong(String key);

    String getString(String key);

    boolean has(String key);

    Set<String> keySet();

    int size();

    boolean isEmpty();

    IJsonObjectWrapper put(String key, boolean value);

    IJsonObjectWrapper put(String key, Collection<?> value);

    IJsonObjectWrapper put(String key, double value);

    IJsonObjectWrapper put(String key, float value);

    IJsonObjectWrapper put(String key, int value);

    IJsonObjectWrapper put(String key, long value);

    IJsonObjectWrapper put(String key, Map<?, ?> value);

    IJsonObjectWrapper put(String key, Object value);

    Object remove(String key);

    String toString(boolean format, boolean keepNullValue);

    String toString(boolean format, boolean keepNullValue, boolean snakeCase);

    Map<String, Object> toMap();
}
