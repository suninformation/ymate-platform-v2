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
package net.ymate.platform.commons.lang;

import java.io.Serializable;

/**
 * 结对对象类型
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author 刘镇 (suninformation@163.com) on 2010-4-17 上午12:07:42
 */
public class PairObject<K, V> implements Serializable, Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = -6239279408656130276L;

    public static <K, V> PairObject<K, V> bind(K key) {
        return new PairObject<>(key);
    }

    public static <K, V> PairObject<K, V> bind(K key, V value) {
        return new PairObject<>(key, value);
    }

    /**
     * 对数据中的键
     */
    private K key;

    /**
     * 对数据中的值
     */
    private V value;

    public PairObject() {
    }

    public PairObject(K key) {
        this.key = key;
    }

    public PairObject(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public PairObject<K, V> setKey(K key) {
        this.key = key;
        return this;
    }

    public V getValue() {
        return value;
    }

    public PairObject<K, V> setValue(V value) {
        this.value = value;
        return this;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PairObject) {
            PairObject<?, ?> o = (PairObject<?, ?>) obj;
            return this.getKey().equals(o.getKey()) && this.getValue().equals(o.getValue());
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("{key : '%s', value : '%s'}", this.key, this.value);
    }

}
