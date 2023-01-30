/*
 * Copyright 2007-2023 the original author or authors.
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
package net.ymate.platform.cache.support;

import net.ymate.platform.cache.ICacheLocker;
import net.ymate.platform.cache.ICaches;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.persistence.redis.IRedis;

/**
 * @author 刘镇 (suninformation@163.com) on 2023/1/30 01:41
 * @since 2.1.2
 */
public interface IRedisCacheLocker extends ICacheLocker, IDestroyable {

    void initialize(ICaches owner, IRedis redis, String cacheName) throws Exception;

    boolean isInitialized();
}
