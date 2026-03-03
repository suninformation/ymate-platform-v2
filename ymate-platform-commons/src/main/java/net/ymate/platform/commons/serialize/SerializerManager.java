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
package net.ymate.platform.commons.serialize;

import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.serialize.annotation.Serializer;
import net.ymate.platform.commons.serialize.impl.*;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化器管理器，负责管理和提供各种序列化器实例。
 * <p>
 * 该管理器采用单例模式，维护一个序列化器注册表，支持动态注册和注销序列化器。
 * 默认注册 DefaultSerializer 和 JSONSerializer，FstSerializer 和 HessianSerializer 通过SPI机制动态加载。
 * </p>
 * <p>
 * 设计目的：
 * <ul>
 *   <li>集中管理所有序列化器实例</li>
 *   <li>支持动态注册和注销序列化器</li>
 *   <li>通过SPI机制自动发现扩展实现</li>
 *   <li>提供便捷的序列化器获取方法</li>
 *   <li>支持可选依赖的序列化器实现（如FST和Hessian）</li>
 * </ul>
 * </p>
 * <p>
 * 使用场景：
 * <ul>
 *   <li>获取特定类型的序列化器</li>
 *   <li>注册自定义序列化器实现</li>
 *   <li>注销不再使用的序列化器</li>
 *   <li>查询已注册的序列化器列表</li>
 * </ul>
 * </p>
 * <p>
 * 注意事项：
 * <ul>
 *   <li>序列化器名称不区分大小写</li>
 *   <li>已注册的序列化器实例会被缓存</li>
 *   <li>注销序列化器后需要重新注册才能使用</li>
 *   <li>FstSerializer 和 HessianSerializer 为可选依赖，仅在对应库存在时通过SPI加载</li>
 *   <li>如果FST或Hessian库不可用，对应的序列化器将不会被注册</li>
 * </ul>
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2017/10/3 下午3:52
 */
public class SerializerManager {

    private static final Log LOG = LogFactory.getLog(SerializerManager.class);

    /**
     * 序列化器注册表，存储所有已注册的序列化器实例。
     * Key为序列化器名称（小写），Value为序列化器实例。
     */
    private static final Map<String, ISerializer> SERIALIZERS = new ConcurrentHashMap<>();

    static {
        SERIALIZERS.put(DefaultSerializer.NAME, new DefaultSerializer());
        SERIALIZERS.put(JSONSerializer.NAME, new JSONSerializer());
        try {
            ClassUtils.ExtensionLoader<ISerializer> extensionLoader = ClassUtils.getExtensionLoader(ISerializer.class, true);
            for (Class<ISerializer> serializerClass : extensionLoader.getExtensionClasses()) {
                registerSerializer(serializerClass);
            }
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Failed to load serializer extensions", RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    /**
     * 注册序列化器。
     * <p>
     * 该方法会创建序列化器实例并注册到管理器中。
     * 如果同名序列化器已存在，则不会覆盖。
     * </p>
     * <p>
     * 序列化器名称的确定规则：
     * <ul>
     *   <li>如果类上存在 {@link Serializer} 注解，则使用注解的 value 作为名称</li>
     *   <li>如果注解的 value 为空字符串，则使用类的全限定名作为名称</li>
     *   <li>如果类上不存在 {@link Serializer} 注解，则使用类的全限定名作为名称</li>
     * </ul>
     * </p>
     *
     * @param targetClass 序列化器实现类，不能为null
     */
    public static void registerSerializer(Class<? extends ISerializer> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }
        Serializer serializerAnn = targetClass.getAnnotation(Serializer.class);
        if (serializerAnn != null) {
            registerSerializer(serializerAnn.value(), targetClass);
        } else {
            registerSerializer(null, targetClass);
        }
    }

    /**
     * 注册序列化器，使用指定的名称。
     * <p>
     * 该方法会创建序列化器实例并注册到管理器中。
     * 如果同名序列化器已存在，则不会覆盖。
     * </p>
     *
     * @param name        序列化器名称，如果为null或空则使用类的全限定名
     * @param targetClass 序列化器实现类，不能为null
     */
    public static void registerSerializer(String name, Class<? extends ISerializer> targetClass) {
        if (targetClass == null) {
            throw new IllegalArgumentException("Target class cannot be null");
        }
        String key = StringUtils.defaultIfBlank(name, targetClass.getName()).toLowerCase();
        try {
            ReentrantLockHelper.putIfAbsentAsync(SERIALIZERS, key, () -> {
                ISerializer result;
                try {
                    result = ClassUtils.impl(targetClass, ISerializer.class);
                } catch (NoClassDefFoundError e) {
                    result = null;
                }
                return result;
            });
        } catch (Exception e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                LOG.warn("Failed to load serializer extensions", RuntimeUtils.unwrapThrow(e));
            }
        }
    }

    /**
     * 注销指定名称的序列化器。
     * <p>
     * 该方法会从注册表中移除指定名称的序列化器。
     * </p>
     *
     * @param name 序列化器名称，不区分大小写
     * @since 2.1.4
     */
    public static void unregisterSerializer(String name) {
        if (StringUtils.isNotBlank(name)) {
            SERIALIZERS.remove(name.toLowerCase());
        }
    }

    /**
     * 注销指定类型的序列化器。
     * <p>
     * 该方法会从注册表中移除指定类型的序列化器。
     * </p>
     * <p>
     * 序列化器名称的确定规则：
     * <ul>
     *   <li>如果类上存在 {@link Serializer} 注解，则使用注解的 value 作为名称</li>
     *   <li>如果注解的 value 为空字符串，则使用类的全限定名作为名称</li>
     *   <li>如果类上不存在 {@link Serializer} 注解，则使用类的全限定名作为名称</li>
     * </ul>
     * </p>
     *
     * @param clazz 序列化器类，不能为null
     * @since 2.1.4
     */
    public static void unregisterSerializer(Class<? extends ISerializer> clazz) {
        if (clazz != null) {
            Serializer serializerAnn = clazz.getAnnotation(Serializer.class);
            if (serializerAnn != null) {
                unregisterSerializer(serializerAnn.value());
            } else {
                SERIALIZERS.remove(clazz.getName().toLowerCase());
            }
        }
    }

    /**
     * 注销所有已注册的序列化器。
     * <p>
     * 该方法会清空注册表，移除所有序列化器。
     * </p>
     *
     * @since 2.1.4
     */
    public static void unregisterAll() {
        SERIALIZERS.clear();
    }

    /**
     * 获取默认序列化器。
     * <p>
     * 默认序列化器为 DefaultSerializer。
     * </p>
     *
     * @return 默认序列化器实例，如果不存在则返回null
     */
    public static ISerializer getDefaultSerializer() {
        return getSerializer(DefaultSerializer.NAME);
    }

    /**
     * 获取JSON序列化器。
     * <p>
     * JSON序列化器为 JSONSerializer。
     * </p>
     *
     * @return JSON序列化器实例，如果不存在则返回null
     */
    public static ISerializer getJsonSerializer() {
        return getSerializer(JSONSerializer.NAME);
    }

    /**
     * 获取FST序列化器。
     * <p>
     * FST序列化器为 FstSerializer。
     * 该序列化器为可选依赖，仅在 FST 库存在时通过 SPI 机制自动加载。
     * 如果 FST 库不可用，则返回 null。
     * </p>
     *
     * @return FST序列化器实例，如果FST库不可用则返回null
     * @since 2.1.4
     */
    public static ISerializer getFstSerializer() {
        return getSerializer(FstSerializer.NAME);
    }

    /**
     * 获取Hessian序列化器。
     * <p>
     * Hessian序列化器为 HessianSerializer。
     * 该序列化器为可选依赖，仅在 Hessian 库存在时通过 SPI 机制自动加载。
     * 如果 Hessian 库不可用，则返回 null。
     * </p>
     *
     * @return Hessian序列化器实例，如果Hessian库不可用则返回null
     * @since 2.1.4
     */
    public static ISerializer getHessianSerializer() {
        return getSerializer(HessianSerializer.NAME);
    }

    /**
     * 获取Protobuf序列化器。
     * <p>
     * Protobuf序列化器为 ProtobufSerializer。
     * 该序列化器为可选依赖，仅在 Protobuf 库存在时通过 SPI 机制自动加载。
     * 如果 Protobuf 库不可用，则返回 null。
     * </p>
     *
     * @return Protobuf序列化器实例，如果Protobuf库不可用则返回null
     * @since 2.1.4
     */
    public static ISerializer getProtobufSerializer() {
        return getSerializer(ProtobufSerializer.NAME);
    }

    /**
     * 获取Kryo序列化器。
     * <p>
     * Kryo序列化器为 KryoSerializer。
     * 该序列化器为可选依赖，仅在 Kryo 库存在时通过 SPI 机制自动加载。
     * 如果 Kryo 库不可用，则返回 null。
     * </p>
     *
     * @return Kryo序列化器实例，如果Kryo库不可用则返回null
     * @since 2.1.4
     */
    public static ISerializer getKryoSerializer() {
        return getSerializer(KryoSerializer.NAME);
    }

    /**
     * 获取指定类型的序列化器。
     * <p>
     * 该方法根据类上的 {@link Serializer} 注解或类的全限定名查找序列化器。
     * </p>
     * <p>
     * 序列化器名称的确定规则：
     * <ul>
     *   <li>如果类上存在 {@link Serializer} 注解，则使用注解的 value 作为名称</li>
     *   <li>如果注解的 value 为空字符串，则使用类的全限定名作为名称</li>
     *   <li>如果类上不存在 {@link Serializer} 注解，则使用类的全限定名作为名称</li>
     * </ul>
     * </p>
     *
     * @param clazz 序列化器类，不能为null
     * @return 序列化器实例，如果不存在则返回null
     */
    public static ISerializer getSerializer(Class<? extends ISerializer> clazz) {
        if (clazz == null) {
            return null;
        }
        Serializer serializerAnn = clazz.getAnnotation(Serializer.class);
        if (serializerAnn != null) {
            return getSerializer(serializerAnn.value());
        }
        return SERIALIZERS.get(clazz.getName().toLowerCase());
    }

    /**
     * 获取指定名称的序列化器。
     * <p>
     * 该方法使用名称查找序列化器，名称不区分大小写。
     * </p>
     *
     * @param name 序列化器名称，不区分大小写
     * @return 序列化器实例，如果不存在则返回null
     */
    public static ISerializer getSerializer(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        return SERIALIZERS.get(name.toLowerCase());
    }

    /**
     * 检查是否存在指定名称的序列化器。
     *
     * @param name 序列化器名称，不区分大小写
     * @return 如果存在则返回true，否则返回false
     * @since 2.1.4
     */
    public static boolean containsSerializer(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        return SERIALIZERS.containsKey(name.toLowerCase());
    }

    /**
     * 检查是否存在指定类型的序列化器。
     * <p>
     * 该方法根据类上的 {@link Serializer} 注解或类的全限定名检查序列化器是否存在。
     * </p>
     * <p>
     * 序列化器名称的确定规则：
     * <ul>
     *   <li>如果类上存在 {@link Serializer} 注解，则使用注解的 value 作为名称</li>
     *   <li>如果注解的 value 为空字符串，则使用类的全限定名作为名称</li>
     *   <li>如果类上不存在 {@link Serializer} 注解，则使用类的全限定名作为名称</li>
     * </ul>
     * </p>
     *
     * @param clazz 序列化器类，不能为null
     * @return 如果存在则返回true，否则返回false
     * @since 2.1.4
     */
    public static boolean containsSerializer(Class<? extends ISerializer> clazz) {
        if (clazz == null) {
            return false;
        }
        Serializer serializerAnn = clazz.getAnnotation(Serializer.class);
        if (serializerAnn != null) {
            return containsSerializer(serializerAnn.value());
        }
        return SERIALIZERS.containsKey(clazz.getName().toLowerCase());
    }

    /**
     * 获取所有已注册的序列化器名称集合。
     *
     * @return 序列化器名称集合，返回的集合不可修改
     * @since 2.1.4
     */
    public static Set<String> getRegisteredNames() {
        return SERIALIZERS.keySet();
    }

    /**
     * 获取所有已注册的序列化器实例集合。
     *
     * @return 序列化器实例集合，返回的集合不可修改
     * @since 2.1.4
     */
    public static Collection<ISerializer> getRegisteredSerializers() {
        return SERIALIZERS.values();
    }

    /**
     * 获取已注册的序列化器数量。
     *
     * @return 序列化器数量
     * @since 2.1.4
     */
    public static int getSerializerCount() {
        return SERIALIZERS.size();
    }
}
