/*
 * Copyright 2007-2019 the original author or authors.
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
package net.ymate.platform.core.beans.support;

import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.beans.annotation.PropertyState;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.beans.proxy.impl.DefaultProxyFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @param <T> 元素类型
 * @author 刘镇 (suninformation@163.com) on 16/7/3 上午2:05
 */
public class PropertyStateSupport<T> {

    private IProxyFactory proxyFactory;

    private final T source;

    private T bound;

    private final Class<?> targetClass;

    private final boolean ignoreNull;

    private final Set<PropertyStateMeta> stateMetas = new HashSet<>();

    private final Map<String, PropertyStateMeta> propertyStates = new HashMap<>();

    public static <T> PropertyStateSupport<T> create(T source) throws Exception {
        return new PropertyStateSupport<>(null, source, false);
    }

    public static <T> PropertyStateSupport<T> create(T source, boolean ignoreNull) throws Exception {
        return new PropertyStateSupport<>(null, source, ignoreNull);
    }

    public static <T> PropertyStateSupport<T> create(IProxyFactory proxyFactory, T source, boolean ignoreNull) throws Exception {
        return new PropertyStateSupport<>(proxyFactory, source, ignoreNull);
    }

    public PropertyStateSupport(T source) throws Exception {
        this(null, source, false);
    }

    public PropertyStateSupport(T source, boolean ignoreNull) throws Exception {
        this(null, source, ignoreNull);
    }

    public PropertyStateSupport(IProxyFactory proxyFactory, T source, boolean ignoreNull) throws Exception {
        this.proxyFactory = proxyFactory;
        this.source = source;
        targetClass = source.getClass();
        this.ignoreNull = ignoreNull;
        //
        ClassUtils.BeanWrapper<T> wrapper = ClassUtils.wrapper(source);
        for (Map.Entry<String, Field> entry : wrapper.getFieldMap().entrySet()) {
            PropertyState state = entry.getValue().getAnnotation(PropertyState.class);
            if (state != null) {
                PropertyStateMeta stateMeta = new PropertyStateMeta(StringUtils.defaultIfBlank(state.propertyName(), entry.getKey()), state.aliasName(), wrapper.getValue(entry.getValue()));
                stateMetas.add(stateMeta);
                propertyStates.put("set" + StringUtils.capitalize(entry.getKey()), stateMeta);
                if (StringUtils.isNotBlank(state.setterName())) {
                    propertyStates.put(state.setterName(), stateMeta);
                }
            }
        }
    }

    public IProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public PropertyStateSupport<T> setProxyFactory(IProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
        return this;
    }

    private T doBind(IProxyFactory proxyFactory) {
        return ClassUtils.wrapper(source).duplicate(proxyFactory.createProxy(targetClass, (targetObject, targetMethod, methodParams) -> {
            PropertyStateMeta stateMeta = propertyStates.get(targetMethod.getName());
            if (stateMeta != null && ArrayUtils.isNotEmpty(methodParams) && !Objects.equals(stateMeta.getOriginalValue(), methodParams[0])) {
                if (ignoreNull && methodParams[0] == null) {
                    methodParams[0] = stateMeta.getOriginalValue();
                }
                stateMeta.setNewValue(methodParams[0]);
            }
            return methodParams;
        }));
    }

    public T bind() {
        if (bound == null) {
            if (proxyFactory == null) {
                try (IProxyFactory f = new DefaultProxyFactory()) {
                    bound = doBind(f);
                } catch (Exception ignored) {
                }
            } else {
                bound = doBind(proxyFactory);
            }
        }
        return bound;
    }

    public T unbind() {
        return ClassUtils.wrapper(bound).duplicate(source);
    }

    public T duplicate(Object source) {
        return duplicate(source, false);
    }

    public T duplicate(Object source, boolean ignoreNull) {
        ClassUtils.BeanWrapper<Object> wrapperSource = ClassUtils.wrapper(source);
        wrapperSource.getFieldMap().forEach((key, field) -> {
            try {
                Object value = wrapperSource.getValue(field);
                if (ignoreNull && value == null) {
                    return;
                }
                Method method = bound.getClass().getMethod("set" + StringUtils.capitalize(key), field.getType());
                method.invoke(bound, value);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException |
                     InvocationTargetException ignored) {
            }
        });
        return bound;
    }

    public String[] getChangedPropertyNames() {
        return stateMetas.stream().filter(PropertyStateMeta::isChanged).map(PropertyStateMeta::getPropertyName).distinct().toArray(String[]::new);
    }

    public Set<PropertyStateMeta> getChangedProperties() {
        Set<PropertyStateMeta> states = new HashSet<>();
        stateMetas.stream().filter(PropertyStateMeta::isChanged).forEachOrdered(states::add);
        return states;
    }

    /**
     * @return 返回true表示某属性值已发生变化
     * @since 2.1.2
     */
    public boolean hasChanged() {
        return stateMetas.stream().anyMatch(PropertyStateMeta::isChanged);
    }

    /**
     * @param propertyOrAliasName 属性名或别名
     * @return 返回true表示该属性值已发生变化
     * @since 2.1.3
     */
    public boolean isChanged(String propertyOrAliasName) {
        return StringUtils.isNotBlank(propertyOrAliasName) && stateMetas.stream()
                .filter(stateMeta -> StringUtils.equalsAny(propertyOrAliasName, stateMeta.getPropertyName(), stateMeta.getAliasName()))
                .findFirst()
                .filter(PropertyStateMeta::isChanged)
                .isPresent();
    }

    /**
     * @return 返回全部属性状态对象集合
     * @since 2.1.2
     */
    public Set<PropertyStateMeta> getProperties() {
        return Collections.unmodifiableSet(stateMetas);
    }

    /**
     * @param properties    属性状态对象集合
     * @param format        是否格式化
     * @param keepNullValue 是否保留空属性
     * @return 以 JSON 格式输出属性名称和值
     * @since 2.1.2
     */
    public static String toString(Collection<PropertyStateMeta> properties, boolean format, boolean keepNullValue) {
        IJsonObjectWrapper objectWrapper = JsonWrapper.createJsonObject();
        for (PropertyStateMeta stateMeta : properties) {
            objectWrapper.put(StringUtils.defaultIfBlank(stateMeta.getAliasName(), stateMeta.getPropertyName()), stateMeta.getNewValue() != null ? stateMeta.getNewValue() : stateMeta.getOriginalValue());
        }
        return objectWrapper.toString(format, keepNullValue);
    }

    /**
     * @param format        是否格式化
     * @param keepNullValue 是否保留空属性
     * @return 以 JSON 格式输出属性名称和值
     * @since 2.1.2
     */
    public String toString(boolean format, boolean keepNullValue) {
        return toString(stateMetas, format, keepNullValue);
    }

    /**
     * @return 以 JSON 格式输出属性名称和值
     * @since 2.1.2
     */
    @Override
    public String toString() {
        return toString(false, false);
    }
}
