/*
 * Copyright 2007-2020 the original author or authors.
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
package net.ymate.platform.cache.annotation;

import net.ymate.platform.cache.ICacheEventListener;
import net.ymate.platform.cache.ICacheKeyGenerator;
import net.ymate.platform.cache.ICacheProvider;
import net.ymate.platform.cache.ICacheScopeProcessor;
import net.ymate.platform.commons.serialize.ISerializer;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.*;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/03/09 17:06
 * @since 2.1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheConf {

    /**
     * @return 默认缓存名称
     */
    String defaultCacheName() default StringUtils.EMPTY;

    /**
     * @return 默认缓存数据超时时间(秒)
     */
    int defaultCacheTimeout() default 0;

    /**
     * @return Ehcache配置文件
     */
    String configFile() default StringUtils.EMPTY;

    /**
     * @return 是否采用Set存储缓存键名
     */
    boolean storageWithSet() default false;

    /**
     * @return 是否开启Redis订阅缓存元素过期事件
     */
    boolean subscribeExpired() default false;

    /**
     * @return Multilevel模式下是否自动同步Master和Slave级缓存
     */
    boolean multilevelSlavesAutoSync() default false;

    /**
     * @return 缓存提供者
     */
    Class<? extends ICacheProvider> providerClass() default ICacheProvider.class;

    /**
     * @return 缓存对象事件监听器
     */
    Class<? extends ICacheEventListener> eventListenerClass() default ICacheEventListener.class;

    /**
     * @return 缓存作用域处理器
     */
    Class<? extends ICacheScopeProcessor> scopeProcessorClass() default ICacheScopeProcessor.class;

    /**
     * @return 缓存Key生成器
     */
    Class<? extends ICacheKeyGenerator> keyGeneratorClass() default ICacheKeyGenerator.class;

    /**
     * @return 对象序列化接口实现
     */
    Class<? extends ISerializer> serializerClass() default ISerializer.class;
}
