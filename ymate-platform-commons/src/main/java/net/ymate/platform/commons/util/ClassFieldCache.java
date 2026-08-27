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
package net.ymate.platform.commons.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 类字段缓存管理器，缓存类的字段信息以避免重复反射扫描
 *
 * @author 刘镇 (suninformation@163.com) on 2026/08/25
 * @since 2.1.4
 */
public final class ClassFieldCache {

    private static final ConcurrentHashMap<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取指定类的字段列表（递归扫描父类，已过滤非正常字段）
     *
     * @param clazz 目标类
     * @return 不可修改的字段列表
     */
    public static List<Field> getFields(Class<?> clazz) {
        return FIELD_CACHE.computeIfAbsent(clazz, key -> {
            List<Field> fields = new ArrayList<>();
            for (Field field : ClassUtils.getFields(key, true)) {
                if (ClassUtils.isNormalField(field)) {
                    fields.add(field);
                }
            }
            return Collections.unmodifiableList(fields);
        });
    }

    /**
     * 清理缓存
     */
    public static void clear() {
        FIELD_CACHE.clear();
    }
}