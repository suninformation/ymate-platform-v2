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
package net.ymate.platform.commons;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @param <E> 元素类型
 * @author 刘镇 (suninformation@163.com) on 2017/9/25 下午3:47
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Object ELEMENT = new Object();

    private final ConcurrentHashMap<E, Object> target;

    public ConcurrentHashSet() {
        target = new ConcurrentHashMap<>();
    }

    public ConcurrentHashSet(int capacity) {
        target = new ConcurrentHashMap<>(capacity);
    }

    @Override
    public int size() {
        return target.size();
    }

    @Override
    public boolean isEmpty() {
        return target.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return target.containsKey(o);
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<Map.Entry<E, Object>> iterator = target.entrySet().iterator();
        return new Iterator<E>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                return iterator.next().getKey();
            }

            @Override
            public void remove() {
                iterator.remove();
            }
        };
    }

    @Override
    public boolean add(E e) {
        return target.put(e, ELEMENT) == null;
    }

    @Override
    public boolean remove(Object o) {
        return target.remove(o) == ELEMENT;
    }

    @Override
    public void clear() {
        target.clear();
    }
}
