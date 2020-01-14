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
package net.ymate.platform.cache.impl;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.ymate.platform.cache.AbstractRedisCacheProvider;
import net.ymate.platform.cache.ICache;
import net.ymate.platform.cache.ICacheEventListener;
import net.ymate.platform.cache.support.MultilevelCacheWrapper;
import net.ymate.platform.persistence.redis.IRedis;

/**
 * @author 刘镇 (suninformation@163.com) on 15/12/6 上午4:58
 */
public class MultilevelCacheProvider extends AbstractRedisCacheProvider {

    private CacheManager cacheManager;

    private IRedis redis;

    @Override
    public String getName() {
        return ICache.MULTILEVEL;
    }

    @Override
    protected void onInitialize() throws Exception {
        cacheManager = doCreateCacheManager();
        redis = REDIS_CREATOR.create();
        redis.initialize(getOwner().getOwner());
    }

    @Override
    protected void onDestroy() throws Exception {
        cacheManager.shutdown();
        cacheManager = null;
        //
        redis.close();
        redis = null;
    }

    @Override
    protected ICache onCreateCache(String cacheName, ICacheEventListener listener) {
        Ehcache ehcache = cacheManager.getEhcache(cacheName);
        if (ehcache == null) {
            cacheManager.addCache(cacheName);
            ehcache = cacheManager.getCache(cacheName);
        }
        return new MultilevelCacheWrapper(getOwner(), cacheName, ehcache, redis, listener);
    }
}
