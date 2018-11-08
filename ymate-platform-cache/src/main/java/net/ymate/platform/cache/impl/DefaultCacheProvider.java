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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.ymate.platform.cache.AbstractCacheProvider;
import net.ymate.platform.cache.ICache;
import net.ymate.platform.cache.ICacheEventListener;
import net.ymate.platform.cache.ICaches;
import net.ymate.platform.cache.support.EhCacheWrapper;

/**
 * @author 刘镇 (suninformation@163.com) on 14/10/17
 * @version 1.0
 */
public class DefaultCacheProvider extends AbstractCacheProvider {

    private CacheManager __cacheManager;

    @Override
    public String getName() {
        return ICache.DEFAULT;
    }

    @Override
    public void init(ICaches owner) {
        super.init(owner);
        //
        __cacheManager = CacheManager.create();
    }

    @Override
    protected ICache __createCache(String saferName, ICacheEventListener listener) {
        Ehcache _ehcache = __cacheManager.getEhcache(saferName);
        if (_ehcache == null) {
            __cacheManager.addCache(saferName);
            _ehcache = __cacheManager.getCache(saferName);
        }
        return new EhCacheWrapper(getOwner(), _ehcache, listener);
    }

    @Override
    public void destroy() {
        super.destroy();
        //
        __cacheManager.shutdown();
        __cacheManager = null;
    }
}
