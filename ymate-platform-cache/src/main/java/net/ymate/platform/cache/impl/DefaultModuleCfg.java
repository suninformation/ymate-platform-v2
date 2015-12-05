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
package net.ymate.platform.cache.impl;

import net.ymate.platform.cache.*;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.util.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * 缓存模块配置类
 *
 * @author 刘镇 (suninformation@163.com) on 14/12/25 下午5:58
 * @version 1.0
 */
public class DefaultModuleCfg implements ICacheModuleCfg {

    private ICacheProvider __cacheProvider;

    private IKeyGenerator<?> __keyGenerator;

    private ISerializer __serializer;

    private String __defaultCacheName;

    public DefaultModuleCfg(YMP owner) throws Exception {
        Map<String, String> _moduleCfgs = owner.getConfig().getModuleConfigs(ICaches.MODULE_NAME);
        //
        __cacheProvider = ClassUtils.impl(_moduleCfgs.get("cache_provider_class"), ICacheProvider.class, this.getClass());
        if (__cacheProvider == null) {
            __cacheProvider = new DefaultCacheProvider();
        }
        //
        __serializer = ClassUtils.impl(_moduleCfgs.get("serializer_class"), ISerializer.class, this.getClass());
        if (__serializer == null) {
            __serializer = new DefaultSerializer();
        }
        //
        __keyGenerator = ClassUtils.impl(_moduleCfgs.get("key_generator_class"), IKeyGenerator.class, this.getClass());
        if (__keyGenerator == null) {
            __keyGenerator = new DefaultKeyGenerator();
        }
        __keyGenerator.init(__serializer);
        //
        __defaultCacheName = StringUtils.defaultIfBlank(_moduleCfgs.get("default_cache_name"), "default");
    }

    public ICacheProvider getCacheProvider() {
        return __cacheProvider;
    }

    public IKeyGenerator<?> getKeyGenerator() {
        return __keyGenerator;
    }

    public ISerializer getSerializer() {
        return __serializer;
    }

    public String getDefaultCacheName() {
        return __defaultCacheName;
    }
}
