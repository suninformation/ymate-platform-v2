/*
 * Copyright 2007-2025 the original author or authors.
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
package net.ymate.platform.persistence.jdbc.query;

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.IEntityPK;
import net.ymate.platform.core.persistence.base.PropertyMeta;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lambda表达式工具类，用于解析方法引用获取字段名
 *
 * @author 刘镇 (suninformation@163.com) on 2025/12/21 上午10:00
 * @since 2.1.4
 */
public final class LambdaUtils {

    private static final Map<Class<?>, Map<String, String>> FIELD_NAME_CACHE = new ConcurrentHashMap<>();

    private static final Map<Class<?>, SerializedLambda> LAMBDA_CACHE = new ConcurrentHashMap<>();

    /**
     * 序列化的函数式接口，用于支持Lambda表达式和方法引用
     *
     * @param <T> 入参类型
     * @param <R> 返回值类型
     */
    @FunctionalInterface
    public interface SFunction<T, R> extends java.util.function.Function<T, R>, Serializable {
    }

    /**
     * 实体函数式接口，用于实体类的方法引用
     *
     * @param <T> 实体类型
     * @param <R> 返回值类型
     */
    @FunctionalInterface
    public interface EntityFunction<T extends IEntity<?>, R> extends SFunction<T, R> {
    }

    /**
     * 主键函数式接口，用于复合主键类的方法引用
     *
     * @param <T> 主键类型
     * @param <R> 返回值类型
     */
    @FunctionalInterface
    public interface PkFunction<T extends IEntityPK, R> extends SFunction<T, R> {
    }

    /**
     * 序列化的供应商接口，用于支持无参方法引用
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface SSupplier<T> extends java.util.function.Supplier<T>, Serializable {
    }

    /**
     * 序列化的双函数接口，用于支持两个参数的方法引用
     *
     * @param <T> 第一个参数类型
     * @param <U> 第二个参数类型
     * @param <R> 返回值类型
     */
    @FunctionalInterface
    public interface SBinaryFunction<T, U, R> extends java.util.function.BiFunction<T, U, R>, Serializable {
    }

    private LambdaUtils() {
    }

    /**
     * 从方法引用中解析出字段名
     *
     * @param func 方法引用
     * @param <T>  类型
     * @param <R>  返回值类型
     * @return 字段名
     */
    public static <T, R> String getFieldName(SFunction<T, R> func) {
        SerializedLambda lambda = getSerializedLambda(func);
        String implMethodName = lambda.getImplMethodName();
        if (implMethodName.startsWith("get")) {
            return StringUtils.uncapitalize(implMethodName.substring(3));
        } else if (implMethodName.startsWith("is")) {
            return StringUtils.uncapitalize(implMethodName.substring(2));
        }
        throw new IllegalArgumentException("Invalid method reference, only getter methods are supported: " + implMethodName);
    }

    /**
     * 从方法引用中解析出数据库字段名
     *
     * @param func 方法引用
     * @param <T>  类型
     * @param <R>  返回值类型
     * @return 数据库字段名
     */
    public static <T, R> String getColumnName(SFunction<T, R> func) {
        SerializedLambda lambda = getSerializedLambda(func);
        Class<?> targetClass = getTargetClass(lambda);
        String fieldName = getFieldName(func);
        // 检查是否为实体类
        if (IEntity.class.isAssignableFrom(targetClass)) {
            EntityMeta entityMeta = EntityMeta.createAndGet(targetClass.asSubclass(IEntity.class));
            if (entityMeta != null) {
                PropertyMeta propertyMeta = entityMeta.getPropertyByField(fieldName);
                if (propertyMeta != null) {
                    return propertyMeta.getName();
                }
            }
        }
        // 检查是否为复合主键类
        else if (IEntityPK.class.isAssignableFrom(targetClass)) {
            // 复合主键类的字段直接映射为数据库字段
            return ClassUtils.fieldNameToPropertyName(fieldName, 0);
        }
        return ClassUtils.fieldNameToPropertyName(fieldName, 0);
    }

    public static String getEntityName(Class<? extends IEntity<?>> entityClass) {
        EntityMeta entityMeta = EntityMeta.createAndGet(entityClass.asSubclass(IEntity.class));
        if (entityMeta != null) {
            return entityMeta.getEntityName();
        }
        return ClassUtils.fieldNameToPropertyName(entityClass.getSimpleName(), 0);
    }

    /**
     * 获取序列化的Lambda对象
     *
     * @param func 函数式接口实例
     * @param <T>  类型
     * @param <R>  返回值类型
     * @return SerializedLambda对象
     */
    private static <T, R> SerializedLambda getSerializedLambda(SFunction<T, R> func) {
        Class<?> clazz = func.getClass();
        return LAMBDA_CACHE.computeIfAbsent(clazz, k -> {
            try {
                Method writeReplaceMethod = clazz.getDeclaredMethod("writeReplace");
                writeReplaceMethod.setAccessible(true);
                return (SerializedLambda) writeReplaceMethod.invoke(func);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get SerializedLambda from function reference", e);
            }
        });
    }

    /**
     * 从SerializedLambda中获取目标类
     *
     * @param lambda SerializedLambda对象
     * @return 目标类
     */
    private static Class<?> getTargetClass(SerializedLambda lambda) {
        try {
            String className = lambda.getImplClass().replace("/", ".");
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to get target class from SerializedLambda", e);
        }
    }

    /**
     * 获取字段的完整名称（包含前缀）
     *
     * @param prefix 前缀
     * @param func   方法引用
     * @param <T>    类型
     * @param <R>    返回值类型
     * @return 完整字段名
     */
    public static <T, R> String getFullFieldName(String prefix, SFunction<T, R> func) {
        String columnName = getColumnName(func);
        if (StringUtils.isNotBlank(prefix)) {
            return StringUtils.join(prefix, ".", columnName);
        }
        return columnName;
    }
}