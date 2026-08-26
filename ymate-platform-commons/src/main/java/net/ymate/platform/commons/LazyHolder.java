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
package net.ymate.platform.commons;

import java.util.function.Supplier;

/**
 * 延迟初始化持有器，基于双检锁模式(DCL)实现线程安全的延迟初始化
 *
 * @param <T> 持有对象类型
 * @author 刘镇 (suninformation@163.com) on 2025-08-24 16:38
 * @since 2.1.4
 */
public final class LazyHolder<T> {

    private volatile T value;

    private final Supplier<T> supplier;

    private LazyHolder(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * 创建延迟初始化持有器
     *
     * @param supplier 初始化供应器
     * @param <T>      持有对象类型
     * @return 延迟初始化持有器实例
     */
    public static <T> LazyHolder<T> of(Supplier<T> supplier) {
        return new LazyHolder<>(supplier);
    }

    /**
     * 获取持有对象，若尚未初始化则先初始化
     *
     * @return 持有对象
     */
    public T get() {
        T v = value;
        if (v == null) {
            synchronized (this) {
                v = value;
                if (v == null) {
                    value = v = supplier.get();
                }
            }
        }
        return v;
    }
}
