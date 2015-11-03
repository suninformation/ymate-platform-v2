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
package net.ymate.platform.cache;

/**
 * 缓存配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 14-12-1 上午2:52
 * @version 1.0
 */
public interface ICacheModuleCfg {

    /**
     * @return 缓存提供者，可选参数，默认值为net.ymate.platform.cache.impl.DefaultCacheProvider
     */
    public ICacheProvider getCacheProvider();

    /**
     * @return 缓存Key生成器，可选参数，默认值为net.ymate.platform.cache.impl.DefaultKeyGenerator
     */
    public IKeyGenerator<?> getKeyGenerator();

    /**
     * @return 默认缓存名称，可选参数，默认值为default，对应于Ehcache配置文件中设置name="__DEFAULT__"
     */
    public String getDefaultCacheName();
}
