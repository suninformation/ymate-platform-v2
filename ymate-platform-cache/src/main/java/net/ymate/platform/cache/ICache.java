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
import net.ymate.platform.core.support.IDestroyable;

import java.util.Collection;
import java.util.List;

/**
 * 缓存接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 14/10/17
 */
@Ignored
public interface ICache extends IDestroyable {

    String DEFAULT = "default";

    String REDIS = "redis";

    String MULTILEVEL = "multilevel";

    /**
     * 从缓存中获取对象
     *
     * @param key 缓存key
     * @return 返回缓存的对象，若不存在则返回null
     * @throws CacheException 可能产生的异常
     */
    Object get(Object key) throws CacheException;

    /**
     * 添加对象到缓存
     *
     * @param key   缓存key
     * @param value 预缓存的元素对象
     * @throws CacheException 可能产生的异常
     */
    void put(Object key, Object value) throws CacheException;

    /**
     * 添加对象到缓存
     *
     * @param key     缓存key
     * @param value   预缓存的元素对象
     * @param timeout 自定义超时时间（秒），若为0则使用默认值
     * @throws CacheException 可能产生的异常
     */
    void put(Object key, Object value, int timeout) throws CacheException;

    /**
     * 更新对象到缓存
     *
     * @param key   缓存key
     * @param value 预缓存的元素对象
     * @throws CacheException 可能产生的异常
     */
    void update(Object key, Object value) throws CacheException;

    /**
     * 更新对象到缓存
     *
     * @param key     缓存key
     * @param value   预缓存的元素对象
     * @param timeout 自定义超时时间（秒），若为0则使用默认值
     * @throws CacheException 可能产生的异常
     */
    void update(Object key, Object value, int timeout) throws CacheException;

    /**
     * 获取当前所有缓存对象key的集合
     *
     * @return 返回当前所有缓存对象key的集合
     * @throws CacheException 可能产生的异常
     */
    List<?> keys() throws CacheException;

    /**
     * 从缓存中移除对象
     *
     * @param key 缓存key
     * @throws CacheException 可能产生的异常
     */
    void remove(Object key) throws CacheException;

    /**
     * 批量从缓存中移除对象
     *
     * @param keys 缓存key集合
     * @throws CacheException 可能产生的异常
     */
    void removeAll(Collection<?> keys) throws CacheException;

    /**
     * 清理缓存
     *
     * @throws CacheException 可能产生的异常
     */
    void clear() throws CacheException;

    /**
     * 获取缓存锁对象，若缓存实例不支持则返回null
     *
     * @return 返回缓存锁对象
     */
    ICacheLocker acquireCacheLocker();
}
