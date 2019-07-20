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
package net.ymate.platform.cache;

import net.ymate.platform.core.beans.annotation.Ignored;

/**
 * 缓存同步锁能力接口
 *
 * @author 刘镇 (suninformation@163.com) on 17/2/18 上午12:35
 */
@Ignored
public interface ICacheLocker {

    /**
     * 设置读锁
     *
     * @param key 缓存锁Key值
     */
    void readLock(Object key);

    /**
     * 设置写锁
     *
     * @param key 缓存锁Key值
     */
    void writeLock(Object key);

    /**
     * 尝试设置读锁，等待timeout毫秒时间
     *
     * @param key     缓存锁Key值
     * @param timeout 超时时间(毫秒)
     * @return 若锁定成功则返回true
     * @throws CacheException 可能产生缓存异常
     */
    boolean tryReadLock(Object key, long timeout) throws CacheException;

    /**
     * 尝试设置写锁，等待timeout毫秒时间
     *
     * @param key     缓存锁Key值
     * @param timeout 超时时间(毫秒)
     * @return 若锁定成功则返回true
     * @throws CacheException 可能产生缓存异常
     */
    boolean tryWriteLock(Object key, long timeout) throws CacheException;

    /**
     * 释放读锁
     *
     * @param key 缓存锁Key值
     */
    void releaseReadLock(Object key);

    /**
     * 释放写锁
     *
     * @param key 缓存锁Key值
     */
    void releaseWriteLock(Object key);
}
