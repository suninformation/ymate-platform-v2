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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/9 9:26 下午
 * @since 2.1.0
 */
public final class JsonWrapper implements Serializable {

    private static final Log LOG = LogFactory.getLog(JsonWrapper.class);

    private static IJsonAdapter jsonAdapter;

    static {
        try {
            String jsonAdapterClass = System.getProperty("ymp.jsonAdapterClass");
            jsonAdapter = ClassUtils.impl(jsonAdapterClass, IJsonAdapter.class, JsonWrapper.class);
            if (jsonAdapter == null) {
                ClassUtils.ExtensionLoader<IJsonAdapter> extensionLoader = ClassUtils.getExtensionLoader(IJsonAdapter.class);
                for (Class<IJsonAdapter> adapterClass : extensionLoader.getExtensionClasses()) {
                    try {
                        jsonAdapter = ClassUtils.impl(adapterClass, IJsonAdapter.class);
                        if (jsonAdapter != null) {
                            if (LOG.isInfoEnabled()) {
                                LOG.info(String.format("Using JsonAdapter class [%s].", adapterClass.getName()));
                            }
                            break;
                        }
                    } catch (NoClassDefFoundError | Exception ignored) {
                    }
                }
            } else if (LOG.isInfoEnabled()) {
                LOG.info(String.format("Using JsonAdapter class [%s].", jsonAdapterClass));
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    public static IJsonAdapter getJsonAdapter() {
        return jsonAdapter;
    }

    public static Object unwrap(Object value) {
        if (value instanceof JsonWrapper) {
            if (((JsonWrapper) value).isJsonObject()) {
                value = unwrap(((JsonWrapper) value).getAsJsonObject());
            } else if (((JsonWrapper) value).isJsonArray()) {
                value = unwrap(((JsonWrapper) value).getAsJsonArray());
            }
        }
        if (value instanceof IJsonArrayWrapper) {
            value = ((IJsonArrayWrapper) value).toList();
        } else if (value instanceof IJsonObjectWrapper) {
            value = ((IJsonObjectWrapper) value).toMap();
        } else if (value instanceof IJsonNodeWrapper) {
            value = ((IJsonNodeWrapper) value).get();
        } else if (value instanceof Collection) {
            Collection<?> collection = (Collection<?>) value;
            value = collection.stream().map(JsonWrapper::unwrap).collect(Collectors.toCollection(() -> new ArrayList<>(collection.size())));
        } else if (value instanceof Map) {
            Map<String, Object> newMap = new HashMap<>();
            ((Map<?, ?>) value).forEach((key, v) -> newMap.put(String.valueOf(key), unwrap(v)));
            value = newMap;
        }
        return value;
    }

    public static IJsonObjectWrapper createJsonObject() {
        return jsonAdapter.createJsonObject();
    }

    public static IJsonObjectWrapper createJsonObject(int initialCapacity) {
        return jsonAdapter.createJsonObject(initialCapacity);
    }

    public static IJsonObjectWrapper createJsonObject(boolean ordered) {
        return jsonAdapter.createJsonObject(ordered);
    }

    public static IJsonObjectWrapper createJsonObject(int initialCapacity, boolean ordered) {
        return jsonAdapter.createJsonObject(initialCapacity, ordered);
    }

    public static IJsonObjectWrapper createJsonObject(Map<?, ?> map) {
        return jsonAdapter.createJsonObject(map);
    }

    public static IJsonArrayWrapper createJsonArray() {
        return jsonAdapter.createJsonArray();
    }

    public static IJsonArrayWrapper createJsonArray(int initialCapacity) {
        return jsonAdapter.createJsonArray(initialCapacity);
    }

    public static IJsonArrayWrapper createJsonArray(Object[] array) {
        return jsonAdapter.createJsonArray(array);
    }

    public static IJsonArrayWrapper createJsonArray(Collection<?> collection) {
        return jsonAdapter.createJsonArray(collection);
    }

    public static JsonWrapper fromJson(String jsonStr) {
        return jsonAdapter.fromJson(jsonStr);
    }

    public static JsonWrapper toJson(Object object) {
        return jsonAdapter.toJson(object);
    }

    public static String toJsonString(Object object, boolean format, boolean keepNullValue) {
        return jsonAdapter.toJsonString(object, format, keepNullValue);
    }

    public static byte[] serialize(Object object) throws Exception {
        return jsonAdapter.serialize(object);
    }

    public static <T> T deserialize(String jsonStr, Class<T> clazz) throws Exception {
        return jsonAdapter.deserialize(jsonStr, clazz);
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) throws Exception {
        return jsonAdapter.deserialize(bytes, clazz);
    }

    private final Object object;

    public JsonWrapper(IJsonObjectWrapper jsonObjectWrapper) {
        if (jsonObjectWrapper == null) {
            throw new NullArgumentException("jsonObjectWrapper");
        }
        this.object = jsonObjectWrapper;
    }

    public JsonWrapper(IJsonArrayWrapper jsonArrayWrapper) {
        if (jsonArrayWrapper == null) {
            throw new NullArgumentException("jsonArrayWrapper");
        }
        this.object = jsonArrayWrapper;
    }

    public boolean isJsonObject() {
        return object instanceof IJsonObjectWrapper;
    }

    public boolean isJsonArray() {
        return object instanceof IJsonArrayWrapper;
    }

    public IJsonObjectWrapper getAsJsonObject() {
        if (isJsonObject()) {
            return (IJsonObjectWrapper) object;
        }
        return null;
    }

    public IJsonArrayWrapper getAsJsonArray() {
        if (isJsonArray()) {
            return (IJsonArrayWrapper) object;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JsonWrapper that = (JsonWrapper) o;
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
        return object.toString();
    }
}
