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
 * 缓存提供者接口
 *
 * @author 刘镇 (suninformation@163.com) on 14/10/17
 */
@Ignored
public interface ICacheProvider extends IInitialization<ICaches>, IDestroyable {

    /**
     * 获取缓存提供者名称
     *
     * @return 返回当前缓存提供者名称
     */
    String getName();

    /**
     * 获取所属缓存管理器
     *
     * @return 返回所属缓存管理器实例
     */
    ICaches getOwner();

    /**
     * 创建缓存对象，若已存在则直接返回
     *
     * @param name     缓存名称
     * @param listener 缓存元素过期监听器接口实现
     * @return 返回缓存对象
     */
    ICache createCache(String name, ICacheEventListener listener);

    /**
     * 获取缓存对象，若不存在则创建
     *
     * @param name 缓存名称
     * @return 返回缓存对象
     */
    ICache getCache(String name);

    /**
     * 获取缓存对象，若不存在则根据create参数决定是否创建缓存对象或返回null
     *
     * @param name   缓存名称
     * @param create 是否创建缓存对象
     * @return 返回缓存对象
     */
    ICache getCache(String name, boolean create);

    /**
     * 获取缓存对象，若不存在则根据create参数决定是否创建缓存对象或返回null
     *
     * @param name     缓存名称
     * @param create   是否创建缓存对象
     * @param listener 缓存元素过期监听器接口实现
     * @return 返回缓存对象
     */
    ICache getCache(String name, boolean create, ICacheEventListener listener);
}
