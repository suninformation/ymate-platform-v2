/*
 * Copyright 2007-2017 the original author or authors.
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
package net.ymate.platform.cache.impl;

import net.ymate.platform.cache.*;
import net.ymate.platform.cache.support.RedisCacheWrapper;
import net.ymate.platform.persistence.redis.IRedis;
import net.ymate.platform.persistence.redis.Redis;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/6 上午4:20
 * @version 1.0
 */
public class RedisCacheProvider extends AbstractCacheProvider {

    private IRedis __redis;

    @Override
    public String getName() {
        return ICache.REDIS;
    }

    @Override
    public void init(ICaches owner) throws CacheException {
        super.init(owner);
        //
        __redis = Redis.get(owner.getOwner());
    }

    @Override
    protected ICache __createCache(String saferName, ICacheEventListener listener) {
        return new RedisCacheWrapper(getOwner(), __redis, saferName, listener);
    }
}
