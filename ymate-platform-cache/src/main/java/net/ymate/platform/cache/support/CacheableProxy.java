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
package net.ymate.platform.cache.support;

import net.ymate.platform.cache.*;
import net.ymate.platform.cache.annotation.Cacheable;
import net.ymate.platform.commons.ReentrantLockHelper;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.beans.annotation.Order;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyChain;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/3 下午6:20
 */
@Order(-60000)
public class CacheableProxy implements IProxy {

    private static final ReentrantLockHelper LOCKER = new ReentrantLockHelper();

    @Override
    public Object doProxy(IProxyChain proxyChain) throws Throwable {
        Cacheable cacheable = proxyChain.getTargetMethod().getAnnotation(Cacheable.class);
        if (cacheable == null || !ClassUtils.isNormalMethod(proxyChain.getTargetMethod())) {
            return proxyChain.doProxyChain();
        }
        ICaches caches = proxyChain.getProxyFactory().getOwner().getModuleManager().getModule(Caches.class);
        Object cacheKey = null;
        // 若缓存key以'#'开头则尝试从方法参数中获取该参数值
        if (StringUtils.startsWith(cacheable.key(), "#")) {
            String paramName = StringUtils.substringAfter(cacheable.key(), "#");
            String[] paramNames = ClassUtils.getMethodParamNames(proxyChain.getTargetMethod());
            for (int idx = 0; idx < paramNames.length; idx++) {
                if (StringUtils.equals(paramNames[idx], paramName)) {
                    cacheKey = proxyChain.getMethodParams()[idx];
                    break;
                }
            }
        } else {
            cacheKey = StringUtils.trimToNull(cacheable.key());
        }
        // 若缓存key为空则通过默认key生成器创建
        if (cacheKey == null) {
            cacheKey = caches.getConfig().getKeyGenerator().generateKey(proxyChain.getTargetMethod(), proxyChain.getMethodParams());
        }
        //
        CacheElement cacheElement;
        ReentrantLock locker = LOCKER.getLocker(cacheKey.toString());
        locker.lock();
        try {
            String cacheName = StringUtils.defaultIfBlank(cacheable.cacheName(), ICacheConfig.DEFAULT_STR);
            ICacheScopeProcessor cacheScopeProcessor = caches.getConfig().getCacheScopeProcessor();
            if (!cacheable.scope().equals(ICaches.Scope.DEFAULT) && cacheScopeProcessor != null) {
                cacheElement = cacheScopeProcessor.getFromCache(caches, cacheable.scope(), cacheName, cacheKey.toString());
            } else {
                cacheElement = (CacheElement) caches.get(cacheName, cacheKey);
            }
            boolean flag = true;
            if (cacheElement != null && !cacheElement.isExpired()) {
                flag = false;
            }
            if (flag) {
                Object cacheTarget = proxyChain.doProxyChain();
                if (cacheTarget != null) {
                    cacheElement = new CacheElement(cacheTarget);
                    int timeout = cacheable.timeout() > 0 ? cacheable.timeout() : caches.getConfig().getDefaultCacheTimeout();
                    if (timeout > 0) {
                        cacheElement.setTimeout(timeout);
                    }
                    if (!cacheable.scope().equals(ICaches.Scope.DEFAULT) && cacheScopeProcessor != null) {
                        cacheScopeProcessor.putInCache(caches, cacheable.scope(), cacheName, cacheKey.toString(), cacheElement);
                    } else {
                        caches.put(cacheName, cacheKey, cacheElement);
                    }
                }
            }
        } finally {
            locker.unlock();
        }
        return cacheElement != null ? cacheElement.getObject() : null;
    }
}
