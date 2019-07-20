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
import net.ymate.platform.core.support.IInitialization;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/6 上午7:23
 */
@Ignored
public interface ICacheEventListener extends IInitialization<ICaches>, IDestroyable {

    String CACHE_NAME = "cacheName";

    String CACHE_KEY = "key";

    String CACHE_VALUE = "value";

    /**
     * 获取所属缓存管理器
     *
     * @return 返回所属缓存管理器实例
     */
    ICaches getOwner();

    /**
     * 缓存元素被移除事件
     *
     * @param cacheName 缓存名称
     * @param key       缓存Key值
     */
    void notifyElementRemoved(String cacheName, Object key);

    /**
     * 缓存元素被添加事件
     *
     * @param cacheName 缓存名称
     * @param key       缓存Key值
     * @param value     缓存值对象
     */
    void notifyElementPut(String cacheName, Object key, Object value);

    /**
     * 缓存元素被更新事件
     *
     * @param cacheName 缓存名称
     * @param key       缓存Key值
     * @param value     缓存值对象
     */
    void notifyElementUpdated(String cacheName, Object key, Object value);

    /**
     * 缓存元素过期事件
     *
     * @param cacheName 缓存名称
     * @param key       缓存Key值
     */
    void notifyElementExpired(String cacheName, Object key);

    /**
     * 缓存元素被驱逐事件
     *
     * @param cacheName 缓存名称
     * @param key       缓存Key值
     */
    void notifyElementEvicted(String cacheName, Object key);

    /**
     * 缓存元素被清空事件
     *
     * @param cacheName 缓存名称
     */
    void notifyRemoveAll(String cacheName);
}
