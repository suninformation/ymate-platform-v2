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

import org.apache.commons.lang.NullArgumentException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 刘镇 (suninformation@163.com) on 16/5/26 下午3:18
 */
public class ReentrantLockHelper {

    public static final ReentrantLockHelper DEFAULT = new ReentrantLockHelper();

    public static <K, V> V putIfAbsent(Map<K, V> target, K key, V value) {
        if (target == null) {
            throw new NullArgumentException("target");
        }
        if (key == null) {
            throw new NullArgumentException("key");
        }
        V v = target.get(key);
        if (v == null) {
            if (value != null) {
                v = value;
                V previous = target.putIfAbsent(key, v);
                if (previous != null) {
                    v = previous;
                }
            }
        }
        return v;
    }

    /**
     * @param target      目标映射
     * @param key         键名
     * @param valueGetter 键值回调接口
     * @param <K>         键名类型
     * @param <V>         键值类型
     * @return 返回键值对象
     * @throws Exception 可能产生的任何异常
     * @since 2.0.7
     */
    public static <K, V> V putIfAbsentAsync(Map<K, V> target, K key, ValueGetter<V> valueGetter) throws Exception {
        if (target == null) {
            throw new NullArgumentException("target");
        }
        if (key == null) {
            throw new NullArgumentException("key");
        }
        V v = target.get(key);
        if (v == null) {
            v = valueGetter.getValue();
            if (v != null) {
                V previous = target.putIfAbsent(key, v);
                if (previous != null) {
                    v = previous;
                }
            }
        }
        return v;
    }

    private final Map<String, ReentrantLock> lockCaches = new ConcurrentHashMap<>();

    public ReentrantLock getLocker(String lockKey) throws Exception {
        return putIfAbsentAsync(lockCaches, lockKey, ReentrantLock::new);
    }

    public static void unlock(ReentrantLock lock) {
        if (lock != null && lock.isLocked()) {
            lock.unlock();
        }
    }

    public interface ValueGetter<V> {

        /**
         * 获取新值对象
         *
         * @return 返回新值对象
         * @throws Exception 初始化时可能产生的异常
         */
        V getValue() throws Exception;
    }
}
