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

import net.ymate.platform.cache.AbstractCacheProvider;
import net.ymate.platform.cache.ICache;
import net.ymate.platform.cache.ICacheEventListener;

/**
 * @author 刘镇 (suninformation@163.com) on 14/10/17
 */
public class DefaultCacheProvider extends AbstractCacheProvider {

    @Override
    public String getName() {
        return ICache.DEFAULT;
    }

    @Override
    protected ICache onCreateCache(String cacheName, ICacheEventListener listener) {
        return getCacheManager().createCache(cacheName, listener);
    }
}
