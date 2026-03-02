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
package net.ymate.platform.commons.json;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 类型引用包装器，用于捕获和保存泛型类型信息，解决Java泛型擦除问题。
 * <p>
 * 设计目的：提供一种方式来获取完整的泛型类型信息，用于JSON反序列化等场景。
 * <p>
 * 使用场景：
 * - 当需要反序列化到具有复杂泛型类型的对象时（如List&lt;Map&lt;String, Object&gt;&gt;）
 * - 当需要在运行时获取泛型类型的具体信息时
 * - 当需要传递泛型类型信息给其他方法时
 * <p>
 * 参考：com.fasterxml.jackson.core.type.TypeReference
 *
 * @param <T> 泛型类型参数，用于指定实际的泛型类型
 * @author 刘镇 (suninformation@163.com) on 2023/4/26 16:13
 * @since 2.1.2
 */
public abstract class TypeReferenceWrapper<T> implements Comparable<TypeReferenceWrapper<T>> {

    private final Type type;

    /**
     * 构造函数，用于创建类型引用包装器实例。
     * <p>
     * 该构造函数通过反射获取泛型类型信息，必须通过匿名内部类方式使用，如：
     * {@code new TypeReferenceWrapper<List<String>>() {}}
     *
     * @throws IllegalArgumentException 如果构造时没有提供实际的泛型类型信息
     */
    public TypeReferenceWrapper() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) {
            throw new IllegalArgumentException("Internal error: TypeReferenceWrapper constructed without actual type information");
        }
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    /**
     * 获取保存的泛型类型信息。
     *
     * @return 泛型类型信息，不为null
     */
    public Type getType() {
        return type;
    }

    /**
     * 比较当前类型引用包装器与另一个实例。
     * <p>
     * 该方法始终返回0，表示所有TypeReferenceWrapper实例都被视为相等。
     *
     * @param o 要比较的另一个TypeReferenceWrapper实例
     * @return 始终返回0
     */
    @Override
    public int compareTo(TypeReferenceWrapper<T> o) {
        return 0;
    }
}
