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
package net.ymate.platform.webmvc.support;

import net.ymate.platform.cache.CacheElement;
import net.ymate.platform.cache.CacheException;
import net.ymate.platform.cache.ICacheScopeProcessor;
import net.ymate.platform.cache.ICaches;
import net.ymate.platform.webmvc.context.WebContext;

/**
 * @author 刘镇 (suninformation@163.com) on 16/1/17 下午6:10
 */
public class WebCacheScopeProcessor implements ICacheScopeProcessor {

    private String buildSessionCacheKey(String cacheKey) {
        String sessionId = WebContext.getRequest().getSession().getId();
        return sessionId + "|" + cacheKey;
    }

    @Override
    public CacheElement getFromCache(ICaches caches, ICaches.Scope scope, String cacheName, String cacheKey) throws CacheException {
        CacheElement cacheElement;
        switch (scope) {
            case SESSION:
                cacheElement = (CacheElement) caches.get(scope.name(), buildSessionCacheKey(cacheKey));
                break;
            case APPLICATION:
            default:
                cacheElement = (CacheElement) caches.get(ICaches.Scope.APPLICATION.name(), cacheKey);
        }
        return cacheElement;
    }

    @Override
    public void putInCache(ICaches caches, ICaches.Scope scope, String cacheName, String cacheKey, CacheElement cacheElement) throws CacheException {
        switch (scope) {
            case SESSION:
                caches.put(scope.name(), buildSessionCacheKey(cacheKey), cacheElement);
                break;
            case APPLICATION:
            default:
                caches.put(ICaches.Scope.APPLICATION.name(), cacheKey, cacheElement);
        }
    }
}
