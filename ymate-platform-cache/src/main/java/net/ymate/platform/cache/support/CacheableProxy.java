/*
 * Copyright 2007-2016 the original author or authors.
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

import net.ymate.platform.cache.CacheElement;
import net.ymate.platform.cache.Caches;
import net.ymate.platform.cache.ICaches;
import net.ymate.platform.cache.annotation.Cacheable;
import net.ymate.platform.core.beans.annotation.Order;
import net.ymate.platform.core.beans.annotation.Proxy;
import net.ymate.platform.core.beans.proxy.IProxy;
import net.ymate.platform.core.beans.proxy.IProxyChain;
import org.apache.commons.lang.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/3 下午6:20
 * @version 1.0
 */
@Proxy(annotation = Cacheable.class, order = @Order(-89))
public class CacheableProxy implements IProxy {


    public Object doProxy(IProxyChain proxyChain) throws Throwable {
        ICaches _caches = Caches.get(proxyChain.getProxyFactory().getOwner());
        //
        Cacheable _anno = proxyChain.getTargetMethod().getAnnotation(Cacheable.class);
        Object _cacheKey = StringUtils.trimToNull(_anno.key());
        if (_cacheKey == null) {
            _cacheKey = _caches.getModuleCfg().getKeyGenerator().generateKey(proxyChain.getTargetMethod(), proxyChain.getMethodParams());
        }
        CacheElement _result = (CacheElement) _caches.get(_anno.cacheName(), _cacheKey);
        if (_result == null || (System.currentTimeMillis() - _result.getLastUpdateTime()) > _anno.timeout()) {
            _result = new CacheElement(proxyChain.doProxyChain());
            if (_result.getObject() != null) {
                _caches.put(_anno.cacheName(), _cacheKey, _result);
            }
        }
        return _result.getObject();
    }
}
