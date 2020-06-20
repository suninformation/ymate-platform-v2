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
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/9 4:26 下午
 * @since 2.1.0
 */
public interface IJsonArrayWrapper extends Serializable {

    IJsonNodeWrapper get(int index);

    boolean getBoolean(int index);

    double getDouble(int index);

    float getFloat(int index);

    BigDecimal getBigDecimal(int index);

    BigInteger getBigInteger(int index);

    int getInt(int index);

    IJsonArrayWrapper getJsonArray(int index);

    IJsonObjectWrapper getJsonObject(int index);

    long getLong(int index);

    String getString(int index);

    boolean isNull(int index);

    int size();

    boolean isEmpty();

    IJsonArrayWrapper add(boolean value);

    IJsonArrayWrapper add(Collection<?> value);

    IJsonArrayWrapper add(double value);

    IJsonArrayWrapper add(float value);

    IJsonArrayWrapper add(int value);

    IJsonArrayWrapper add(long value);

    IJsonArrayWrapper add(Map<?, ?> value);

    IJsonArrayWrapper add(Object value);

    IJsonArrayWrapper add(int index, boolean value);

    IJsonArrayWrapper add(int index, Collection<?> value);

    IJsonArrayWrapper add(int index, double value);

    IJsonArrayWrapper add(int index, float value);

    IJsonArrayWrapper add(int index, int value);

    IJsonArrayWrapper add(int index, long value);

    IJsonArrayWrapper add(int index, Map<?, ?> value);

    IJsonArrayWrapper add(int index, Object value);

    Object remove(int index);

    String toString(boolean format, boolean keepNullValue);

    List<Object> toList();

    Object[] toArray();
}
