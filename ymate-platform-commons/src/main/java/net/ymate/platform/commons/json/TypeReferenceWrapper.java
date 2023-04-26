/*
 * Copyright 2007-2023 the original author or authors.
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
 * 类型引用包装器
 *
 * <p>参考：com.fasterxml.jackson.core.type.TypeReference</p>
 *
 * @author 刘镇 (suninformation@163.com) on 2023/4/26 16:13
 * @since 2.1.2
 */
public abstract class TypeReferenceWrapper<T> implements Comparable<TypeReferenceWrapper<T>> {

    private final Type type;

    public TypeReferenceWrapper() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) {
            throw new IllegalArgumentException("Internal error: TypeReferenceWrapper constructed without actual type information");
        }
        type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }

    @Override
    public int compareTo(TypeReferenceWrapper<T> o) {
        return 0;
    }
}
