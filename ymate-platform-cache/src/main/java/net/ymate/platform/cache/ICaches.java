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

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;

import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/2 下午2:24
 */
@Ignored
public interface ICaches extends IInitialization<IApplication>, IDestroyable {

    String MODULE_NAME = "cache";

    /**
     * 缓存作用域
     */
    enum Scope {

        /**
         * 应用级
         */
        APPLICATION,

        /**
         * 会话级
         */
        SESSION,

        /**
         * 默认
         */
        DEFAULT
    }

    /**
     * 获取所属应用容器
     *
     * @return 返回所属应用容器实例
     */
    IApplication getOwner();

    /**
     * 获取缓存配置
     *
     * @return 返回缓存配置对象
     */
    ICacheConfig getConfig();

    /**
     * 从指定名称的缓存中获取key对应的对象
     *
     * @param cacheName 缓存名称
     * @param key       缓存Key
     * @return 返回缓存的对象，若不存在则返回null
     * @throws CacheException 可能产生的异常
     */
    Object get(String cacheName, Object key) throws CacheException;

    /**
     * 从默认缓存中获取key对应的对象
     *
     * @param key 缓存Key
     * @return 返回缓存的对象，若不存在则返回null
     * @throws CacheException 可能产生的异常
     */
    Object get(Object key) throws CacheException;

    /**
     * 从指定名称的缓存中获取所有缓存对象映射
     *
     * @param cacheName 缓存名称
     * @return 返回缓存内对象映射
     * @throws CacheException 可能产生的异常
     */
    Map<Object, Object> getAll(String cacheName) throws CacheException;

    /**
     * 从默认缓存中获取所有缓存对象映射
     *
     * @return 返回缓存内对象映射
     * @throws CacheException 可能产生的异常
     */
    Map<Object, Object> getAll() throws CacheException;

    /**
     * 添加对象到指定名称的缓存中
     *
     * @param cacheName 缓存名称
     * @param key       缓存Key
     * @param value     预缓存的元素对象
     * @throws CacheException 可能产生的异常
     */
    void put(String cacheName, Object key, Object value) throws CacheException;

    /**
     * 添加对象到默认缓存中
     *
     * @param key   缓存Key
     * @param value 预缓存的元素对象
     * @throws CacheException 可能产生的异常
     */
    void put(Object key, Object value) throws CacheException;

    /**
     * 更新对象到指定名称的缓存中
     *
     * @param cacheName 缓存名称
     * @param key       缓存Key
     * @param value     缓存元素对象
     * @throws CacheException 可能产生的异常
     */
    void update(String cacheName, Object key, Object value) throws CacheException;

    /**
     * 更新对象到默认缓存中
     *
     * @param key   缓存Key
     * @param value 缓存元素对象
     * @throws CacheException 可能产生的异常
     */
    void update(Object key, Object value) throws CacheException;

    /**
     * 获取指定名称的缓存中所有key值集合
     *
     * @param cacheName 缓存名称
     * @return 返回缓存中的key值集合
     * @throws CacheException 可能产生的异常
     */
    List<?> keys(String cacheName) throws CacheException;

    /**
     * 获取默认缓存中所有key值集合
     *
     * @return 返回缓存中的key值集合
     * @throws CacheException 可能产生的异常
     */
    List<?> keys() throws CacheException;

    /**
     * 从指定名称的缓存中移除对象
     *
     * @param cacheName 缓存名称
     * @param key       缓存Key
     * @throws CacheException 可能产生的异常
     */
    void remove(String cacheName, Object key) throws CacheException;

    /**
     * 从默认缓存中移除对象
     *
     * @param key 缓存Key
     * @throws CacheException 可能产生的异常
     */
    void remove(Object key) throws CacheException;

    /**
     * 批量从指定名称的缓存中移除对象
     *
     * @param cacheName 缓存名称
     * @param keys      缓存Key
     * @throws CacheException 可能产生的异常
     */
    void removeAll(String cacheName, List<?> keys) throws CacheException;

    /**
     * 批量从默认缓存中移除对象
     *
     * @param keys 缓存Key
     * @throws CacheException 可能产生的异常
     */
    void removeAll(List<?> keys) throws CacheException;

    /**
     * 清理指定名称的缓存
     *
     * @param cacheName 缓存名称
     * @throws CacheException 可能产生的异常
     */
    void clear(String cacheName) throws CacheException;

    /**
     * 清理默认缓存
     *
     * @throws CacheException 可能产生的异常
     */
    void clear() throws CacheException;
}
