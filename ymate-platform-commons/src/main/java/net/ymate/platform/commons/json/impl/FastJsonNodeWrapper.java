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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonNodeWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.alibaba.fastjson.util.TypeUtils.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/20 4:44 下午
 * @since 2.1.0
 */
public class FastJsonNodeWrapper implements IJsonNodeWrapper {

    private final Object object;

    public FastJsonNodeWrapper(Object object) {
        this.object = object;
    }

    @Override
    public Object get() {
        return object;
    }

    @Override
    public boolean getBoolean() {
        return object != null && TypeUtils.castToBoolean(object);
    }

    @Override
    public BigInteger getBigInteger() {
        return TypeUtils.castToBigInteger(object);
    }

    @Override
    public BigDecimal getBigDecimal() {
        return TypeUtils.castToBigDecimal(object);
    }

    @Override
    public double getDouble() {
        Double doubleValue = castToDouble(object);
        if (doubleValue == null) {
            return 0d;
        }
        return doubleValue;
    }

    @Override
    public float getFloat() {
        Float floatValue = castToFloat(object);
        if (floatValue == null) {
            return 0f;
        }
        return floatValue;
    }

    @Override
    public int getInt() {
        Integer intVal = castToInt(object);
        if (intVal == null) {
            return 0;
        }
        return intVal;
    }

    @Override
    public long getLong() {
        Long longVal = castToLong(object);
        if (longVal == null) {
            return 0L;
        }
        return longVal;
    }

    @Override
    public String getString() {
        return TypeUtils.castToString(object);
    }

    @Override
    public boolean isNull() {
        return object == null;
    }

    @Override
    public boolean isJsonArray() {
        return object instanceof JSONArray;
    }

    @Override
    public boolean isJsonObject() {
        return object instanceof JSONObject;
    }

    @Override
    public IJsonArrayWrapper getJsonArray() {
        return isJsonArray() ? new FastJsonArrayWrapper((JSONArray) object) : null;
    }

    @Override
    public IJsonObjectWrapper getJsonObject() {
        return isJsonObject() ? new FastJsonObjectWrapper((JSONObject) object) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FastJsonNodeWrapper that = (FastJsonNodeWrapper) o;
        return new EqualsBuilder()
                .append(object, that.object)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(object)
                .toHashCode();
    }

    @Override
    public String toString() {
        return JsonWrapper.toJsonString(object, false, false);
    }
}
