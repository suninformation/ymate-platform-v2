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
package net.ymate.platform.cache;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.serialize.ISerializer;
import net.ymate.platform.core.support.IInitialization;

import java.io.File;

/**
 * 缓存配置接口
 *
 * @author 刘镇 (suninformation@163.com) on 14-12-1 上午2:52
 */
@Ignored
public interface ICacheConfig extends IInitialization<ICaches> {

    String DEFAULT_STR = "default";

    String PROVIDER_CLASS = "provider_class";

    String EVENT_LISTENER_CLASS = "event_listener_class";

    String SCOPE_PROCESSOR_CLASS = "scope_processor_class";

    String SERIALIZER_CLASS = "serializer_class";

    String KEY_GENERATOR_CLASS = "key_generator_class";

    String DEFAULT_CACHE_NAME = "default_cache_name";

    String DEFAULT_CACHE_TIMEOUT = "default_cache_timeout";

    String DEFAULT_CONFIG_FILE = "${root}/cfgs/ehcache.xml";

    String CONFIG_FILE = "config_file";

    String STORAGE_WITH_SET = "storage_with_set";

    String ENABLED_SUBSCRIBE_EXPIRED = "enabled_subscribe_expired";

    String MULTILEVEL_SLAVE_AUTO_SYNC = "multilevel_slave_auto_sync";

    /**
     * 缓存提供者，可选参数，默认值为 net.ymate.platform.cache.impl.DefaultCacheProvider
     *
     * @return 返回缓存提供者实例
     */
    ICacheProvider getCacheProvider();

    /**
     * 缓存对象事件监听器，可选参数，默认值为空
     *
     * @return 返回缓存对象事件监听器实例
     */
    ICacheEventListener getCacheEventListener();

    /**
     * 缓存作用域处理器，可选参数，默认值为空
     *
     * @return 返回缓存作用域处理器实例
     */
    ICacheScopeProcessor getCacheScopeProcessor();

    /**
     * 缓存Key生成器，可选参数，默认值为 net.ymate.platform.cache.impl.DefaultCacheKeyGenerator
     *
     * @return 返回缓存Key生成器实例
     */
    ICacheKeyGenerator<?> getKeyGenerator();

    /**
     * 对象序列化接口实现，可选参数，默认值为 SerializerManager.getDefaultSerializer()
     *
     * @return 返回对象序列化接口实现
     */
    ISerializer getSerializer();

    /**
     * 默认缓存名称，可选参数，默认值为default，对应于Ehcache配置文件中设置name="__DEFAULT__"
     *
     * @return 返回默认缓存名称
     */
    String getDefaultCacheName();

    /**
     * 默认缓存数据超时时间(秒)，可选参数，数值必须大于等于0，默认值为0
     *
     * @return 返回默认缓存数据超时时间(秒)
     */
    int getDefaultCacheTimeout();

    /**
     * 获取Ehcache配置文件，可选参数，若未设置或设置的文件路径无效将被忽略，默认值为空
     *
     * @return 返回Ehcache配置文件
     */
    File getConfigFile();

    /**
     * 是否采用Set存储缓存键名
     *
     * @return 返回true表示采用
     */
    boolean isStorageWithSet();

    /**
     * 是否开启Redis订阅缓存元素过期事件，可选参数，默认值为false
     *
     * @return 返回true表示已开启
     */
    boolean isEnabledSubscribeExpired();

    /**
     * Multilevel模式下是否自动同步Master和Slave级缓存，可选参数，默认值为false
     *
     * @return 返回true表示同步
     */
    boolean isMultilevelSlavesAutoSync();
}
