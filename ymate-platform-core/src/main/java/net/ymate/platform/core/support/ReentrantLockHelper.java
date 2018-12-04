/*
 * Copyright 2007-2016 the original author or authors.
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
package net.ymate.platform.core.support;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 刘镇 (suninformation@163.com) on 16/5/26 下午3:18
 * @version 1.0
 */
public class ReentrantLockHelper {

    public static final ReentrantLockHelper DEFAULT;

    static {
        DEFAULT = new ReentrantLockHelper();
    }

    private ConcurrentHashMap<String, ReentrantLock> __LOCK_CACHES = new ConcurrentHashMap<String, ReentrantLock>();

    public ReentrantLock getLocker(String lockKey) {
        ReentrantLock _locker = __LOCK_CACHES.get(lockKey);
        if (_locker == null) {
            _locker = new ReentrantLock();
            ReentrantLock _previous = __LOCK_CACHES.putIfAbsent(lockKey, _locker);
            if (_previous != null) {
                _locker = _previous;
            }
        }
        return _locker;
    }

    public void unlock(ReentrantLock lock) {
        if (lock != null && lock.isLocked()) {
            lock.unlock();
        }
    }

    public static <K, V> V putIfAbsent(ConcurrentMap<K, V> target, K key, V value) {
        V _value = target.get(key);
        if (_value != null) {
            V _previous = target.putIfAbsent(key, _value);
            if (_previous != null) {
                _value = _previous;
            }
        }
        return _value;
    }
}
