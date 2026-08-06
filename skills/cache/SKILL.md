---
name: ymp-cache
description: YMP框架缓存模块，提供基于EhCache和Redis的多级缓存支持，支持方法缓存、缓存事件监听、灵活配置等功能
version: 2.1.4-dev
author: YMP Team
category: cache
tags:
  - java
  - cache
  - ehcache
  - redis
  - multilevel
  - performance
trigger: 当用户需要使用缓存功能、提高应用性能、实现分布式缓存、方法级缓存@Cacheable、缓存事件监听、缓存同步锁、多级缓存Multilevel等场景时触发
examples:
  - 配置EhCache本地缓存+超时设置
  - 配置Redis分布式缓存
  - 配置Multilevel多级缓存（EhCache+Redis）
  - @Cacheable标注业务方法实现方法级缓存（scope/timeout/keyGenerator）
  - Caches.get()手动put/get/remove缓存操作
---

# 缓存（Cache）技能包

> AI读取指引：本技能处理本地EhCache缓存、Redis缓存、Multilevel多级缓存及@Cacheable方法缓存。纯Redis数据结构操作（Set/Hash/List等）请跳redis SKILL；@Cacheable依赖AOP，需配合@EnableBeanProxy生效。

---

## 0. 快速索引

- Maven artifactId：`ymate-platform-cache`（启用Redis缓存需额外依赖`ymate-platform-persistence-redis`）
- 静态入口类全限定名：`net.ymate.platform.cache.Caches`
- 必备启动注解：`@EnableAutoScan`（扫描@Bean/@Cacheable类）、`@EnableBeanProxy`（@Cacheable AOP代理生效）
- 5行最简调用代码片段：

```java
ICaches caches = Caches.get();
caches.put("user:1", userObj, 300);
User cached = (User) caches.get("user:1");
caches.remove("user:1");
caches.clear();
```

## 1. 模块摘要

缓存模块默认基于EhCache提供JVM进程内缓存，通过整合Redis服务实现Multilevel多级缓存，与YMP框架深度集成，支持@Cacheable方法级缓存（根据参数自动生成Key）、缓存事件监听、缓存同步锁、自定义Key生成器和序列化等能力。

- 三种缓存提供者（provider）：default(EhCache) / redis / multilevel（EhCache+Redis融合，MultilevelKey控制读写目标）
- @Cacheable注解支持类/方法级缓存，可按参数生成Key，支持APPLICATION/SESSION/DEFAULT作用域
- 支持ICacheEventListener监听ELEMENT_PUT/UPDATED/EXPIRED/REMOVED/EVICTED等事件
- 支持ICacheLocker读写锁，防止缓存击穿（Multilevel可指定Redis层锁）
- 可自定义ICacheKeyGenerator、ISerializer、ICacheScopeProcessor

## 2. 核心注解速查表（全限定名）

| 注解 | 全限定名 | 作用 | 核心参数 |
|---|---|---|---|
| @Cacheable | `net.ymate.platform.cache.annotation.Cacheable` | 类/方法开启结果缓存；类上声明该类支持，方法上声明该方法走缓存 | cacheName/key/#参数/generator/scope(DEFAULT/APPLICATION/SESSION)/timeout秒 |
| @CacheConf | `net.ymate.platform.cache.annotation.CacheConf` | 启动类/配置类上声明缓存模块全局配置（替代properties） | defaultCacheName/defaultCacheTimeout/configFile/storageWithSet/subscribeExpired/multilevelSlavesAutoSync/providerClass/eventListenerClass/scopeProcessorClass/keyGeneratorClass/serializerClass |

## 3. 核心API速查（入口类常用方法≤8条）

| API | 说明 |
|---|---|
| `Caches.get()` | 获取缓存模块静态入口（ICaches实例） |
| `Caches.get().put(key, value)` / `Caches.get().put(cacheName, key, value)` / `Caches.get().put(key, value, timeout秒)` | 默认/指定缓存写入，可带超时（单位秒，0=配置默认） |
| `Caches.get().get(key)` / `Caches.get().get(cacheName, key)` | 从默认/指定缓存读取 |
| `Caches.get().update(key, value, timeout)` | 更新缓存值（区别于put的语义由底层实现） |
| `Caches.get().remove(key)` / `Caches.get().remove(cacheName, key)` / `Caches.get().removeAll(List keys)` | 删除默认/指定缓存单条或批量 |
| `Caches.get().clear()` / `Caches.get().clear(cacheName)` | 清空默认/指定缓存 |
| `Caches.get().contains(key)` / `Caches.get().keys()` / `Caches.get().keys(cacheName)` | 判断键存在/列出所有键 |
| `Caches.get().getConfig().getCacheProvider().getCache(name).acquireCacheLocker()` → `writeLock(key)` / `releaseWriteLock(key)` | 获取缓存同步锁，防止缓存击穿（Multilevel可通过MultilevelKey指定层级） |

## 4. 标准代码模板（最少可运行）

### 模板1：@Cacheable标记在业务方法上（scope/timeout/keyGenerator核心参数）

```java
/*
 * Copyright 2007-2026 the original author or authors.
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
package net.ymate.demo.service;

import net.ymate.platform.cache.ICacheKeyGenerator;
import net.ymate.platform.cache.ICaches;
import net.ymate.platform.cache.annotation.Cacheable;
import net.ymate.platform.core.beans.annotation.Bean;
import net.ymate.platform.demo.persistence.entity.UserEntity;

/**
 * 用户缓存服务示例
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@Bean
@Cacheable(cacheName = "default")
public class UserCacheService {

    /**
     * 根据用户ID查询，使用默认生成器根据参数生成Key，超时300秒
     *
     * @param userId 用户ID
     * @return 用户实体
     * @since 2.1.4-dev
     */
    @Cacheable(cacheName = "user_cache", timeout = 300)
    public UserEntity findUserById(String userId) throws Exception {
        return UserEntity.builder().id(userId).build().load();
    }

    /**
     * 指定key="#userId"从方法参数取userId值作为Key；作用域APPLICATION（全局共享）
     *
     * @param userId 用户ID
     * @return 用户名
     * @since 2.1.4-dev
     */
    @Cacheable(cacheName = "user_cache", key = "#userId", scope = ICaches.Scope.APPLICATION, timeout = 600)
    public String findUsernameById(String userId) throws Exception {
        UserEntity u = UserEntity.builder().id(userId).build()
            .load(Fields.create(UserEntity.FIELDS.USERNAME));
        return u == null ? null : u.getUsername();
    }

    /**
     * 使用自定义Key生成器（需实现ICacheKeyGenerator并注册）
     *
     * @since 2.1.4-dev
     */
    @Cacheable(cacheName = "user_cache", generator = ICacheKeyGenerator.class, timeout = 180)
    public java.util.List<UserEntity> listUsersByStatus(Integer status) throws Exception {
        return UserEntity.builder().status(status).build().find()
            .toList();
    }
}
```

启动类示例：
```java
/*
 * Copyright 2007-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 */
package net.ymate.demo;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;

/**
 * 启动类
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@EnableAutoScan
@EnableBeanProxy
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    public static void main(String[] args) throws Exception {
        try (IApplication app = YMP.run(args)) {
            UserCacheService svc = app.getBeanFactory().getBean(UserCacheService.class);
            System.out.println(svc.findUsernameById("user_001"));
            System.out.println(svc.findUsernameById("user_001")); // 第二次命中缓存
        }
    }
}
```

### 模板2：Caches.get()手动操作缓存（put/get/remove）

```java
/*
 * Copyright 2007-2026 the original author or authors.
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
package net.ymate.demo.cache;

import net.ymate.platform.cache.Caches;
import net.ymate.platform.cache.ICache;
import net.ymate.platform.cache.ICacheLocker;
import net.ymate.platform.cache.ICaches;
import net.ymate.platform.cache.support.MultilevelKey;
import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.demo.persistence.entity.UserEntity;

/**
 * 手动缓存操作示例
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@EnableAutoScan
public class ManualCacheDemo {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, ManualCacheDemo.class.getName());
    }

    public static void main(String[] args) throws Exception {
        try (IApplication app = YMP.run(args)) {
            ICaches caches = Caches.get();
            String cacheName = "default";

            // ---- 1. 默认缓存 put/get/remove ----
            caches.put("user:1", UserEntity.builder().id("1").username("ymp").build());
            UserEntity u1 = (UserEntity) caches.get("user:1");
            caches.remove("user:1");

            // ---- 2. 指定缓存名 + 超时（秒） ----
            caches.put(cacheName, "token:abc", "token-value-xyz", 1800);
            String token = (String) caches.get(cacheName, "token:abc");
            caches.update(cacheName, "token:abc", "new-token", 3600);

            // ---- 3. 检查存在 / 列出键 / 清空 ----
            boolean exist = caches.contains(cacheName, "token:abc");
            java.util.List<?> keys = caches.keys(cacheName);
            // caches.clear(cacheName);

            // ---- 4. 缓存锁（防止缓存击穿） ----
            ICache cache = caches.getConfig().getCacheProvider().getCache(cacheName);
            ICacheLocker locker = cache.acquireCacheLocker();
            String lockKey = "lock:user:10086";
            locker.writeLock(lockKey);
            try {
                // 查DB写缓存...
            } finally {
                locker.releaseWriteLock(lockKey);
            }

            // ---- 5. 多级缓存模式下指定操作目标 ----
            if (caches.isMultilevel()) {
                // MultilevelKey(String key, boolean masterOnly):
                //   true  = 仅Master层（EhCache）
                //   false = 仅Slave层（Redis）
                //   不传 = 按Multilevel配置自动走Master→Slave或全写
                MultilevelKey mk = new MultilevelKey("onlyInRedis:1", false);
                caches.put(mk, "只在redis里的值", 60);
                Object val = caches.get(mk);
            }
        }
    }
}
```

### 模板3：MultilevelCache多级缓存配置简述

```properties
#-------------------------------------
# 缓存模块 - Multilevel多级缓存配置示例
#-------------------------------------
# 缓存提供者：default(EhCache) | redis | multilevel
ymp.configs.cache.provider_class=multilevel

# 默认缓存名（对应ehcache.xml的name="__DEFAULT__"）
ymp.configs.cache.default_cache_name=default
# 默认超时（秒，0=按ehcache配置）
ymp.configs.cache.default_cache_timeout=0
# EhCache XML路径，默认${root}/cfgs/ehcache.xml
ymp.configs.cache.config_file=cfgs/ehcache.xml

# --- Redis相关（provider=redis/multilevel时使用redis模块配置，请同时配置Redis SKILL中的ymp.configs.persistence.redis.*） ---
# Redis侧是否用Set维护键名集合（列出keys依赖它，keys命令不建议生产开启）
ymp.configs.cache.storage_with_set=false
# 是否订阅Redis key过期事件（需Redis开启notify-keyspace-events Ex）
ymp.configs.cache.enabled_subscribe_expired=false

# Multilevel特有：主从缓存自动同步（EhCache<->Redis）
ymp.configs.cache.multilevel_slave_auto_sync=true

# 自定义扩展（类全限定名，可选）
ymp.configs.cache.event_listener_class=
ymp.configs.cache.scope_processor_class=
ymp.configs.cache.key_generator_class=
ymp.configs.cache.serializer_class=
```

## 5. 配置速查

### 5.1 ymp-conf.properties/ymp.properties最常改项≤15条

| 配置key | 默认值 | 说明 |
|---|---|---|
| `ymp.configs.cache.provider_class` | default | 缓存提供者：default(EhCache) / redis / multilevel 或类全限定名 |
| `ymp.configs.cache.default_cache_name` | default | 默认缓存名称，对应ehcache.xml的`name="__DEFAULT__"` |
| `ymp.configs.cache.default_cache_timeout` | 0 | 默认超时秒数，0=使用各cache自己的配置（如ehcache.xml的TTL） |
| `ymp.configs.cache.config_file` | ${root}/cfgs/ehcache.xml | EhCache XML配置路径 |
| `ymp.configs.cache.storage_with_set` | false | Redis/ML模式下是否用Set维护缓存键名集合（影响keys()性能和正确性） |
| `ymp.configs.cache.enabled_subscribe_expired` | false | 是否订阅Redis过期key事件（用于清理EhCache侧过期，Redis需配置notify-keyspace-events Ex） |
| `ymp.configs.cache.multilevel_slave_auto_sync` | false | Multilevel下Master(SLAVE)自动双向同步 |
| `ymp.configs.cache.event_listener_class` | 空 | ICacheEventListener实现类全限定名，监听PUT/UPDATE/EXPIRE/REMOVE等 |
| `ymp.configs.cache.scope_processor_class` | 空 | ICacheScopeProcessor实现，处理@Cacheable scope非DEFAULT时的Key作用域 |
| `ymp.configs.cache.key_generator_class` | DefaultCacheKeyGenerator | ICacheKeyGenerator，默认是类名+方法名+参数序列化后MD5 |
| `ymp.configs.cache.serializer_class` | SerializerManager默认 | ISerializer，缓存对象进Redis或非堆存储时使用 |

### 5.2 启动注解核心参数

- `@EnableAutoScan`：无必选参数，必填。扫描@Bean/@Service中的@Cacheable类。
- `@EnableBeanProxy`：无必选参数，@Cacheable必填。启用AOP代理才能拦截方法调用做缓存。
- `@CacheConf`（可选，替代properties）参数：
  - `defaultCacheName` / `defaultCacheTimeout`（秒）
  - `configFile`：ehcache.xml路径
  - `storageWithSet`：Redis用Set存键名
  - `subscribeExpired`：订阅Redis key过期
  - `multilevelSlavesAutoSync`：ML主从自动同步
  - `providerClass` / `eventListenerClass` / `scopeProcessorClass` / `keyGeneratorClass` / `serializerClass`

## 6. 常见坑点（3-6条）：现象 | 原因 | 解决

| 现象 | 原因 | 解决 |
|---|---|---|
| @Cacheable注解标注的方法调用时每次都执行，缓存完全不生效 | 1. 类未交给容器（缺少@Bean）；2. 启动类未加@EnableBeanProxy；3. 同类内部this调用（绕过代理对象）；4. 方法非public/静态 | 1. 类上加@Bean；2. 启动类必须@EnableAutoScan + @EnableBeanProxy；3. 自调用改为通过application.getBeanFactory().getBean(Xxx.class)取代理对象调用，或拆分为两个Bean；4. 缓存方法必须public非静态 |
| provider=redis或multilevel时启动报错ClassNotFound/NoClassDefFound | 没有引入`ymate-platform-persistence-redis`依赖，或Redis模块未正确配置数据源 | pom.xml追加ymate-platform-persistence-redis依赖，并在ymp-conf.properties配置`ymp.configs.persistence.redis.*`（Redis SKILL）确保Redis模块能连通 |
| 切换到multilevel后Caches.get().keys()返回空/少了Redis侧的key | storage_with_set=false时Redis层不维护键名集合，只查EhCache侧的keys | 1. 若需要跨层keys()，设置ymp.configs.cache.storage_with_set=true（性能代价）；2. 业务代码不依赖keys()遍历，改用业务侧维护索引键 |
| Redis模式下缓存对象反序列化失败（ClassCastException/InvalidClassException） | 1. 对象未实现Serializable；2. 自定义serializer_class配置不一致；3. 类结构变化（字段增删）导致旧缓存二进制不兼容 | 1. 所有缓存对象implements Serializable并显式声明serialVersionUID；2. 多实例部署统一serializer_class（默认用JDK序列化，可改为JSON/Kryo）；3. 升级类后清理对应缓存，或在Key前缀加版本号如 v2:user:1 |
| 缓存击穿：高并发查同一条热点数据全部落到DB | 没有加缓存同步锁；或锁粒度太大 | 使用ICacheLocker对该key加writeLock后做DB查询+回填缓存（见模板2第4段）；Multilevel下用MultilevelKey(true/false)灵活控制锁所在层 |
| @Cacheable key="#xxx"取值为null或和预期不符 | 参数名xxx在编译后丢失（字节码里是arg0），或参数对象根本不存在 | 1. 编译开启`-parameters`参数（Maven插件maven-compiler-plugin配置`<parameters>true</parameters>`）；2. 确保方法签名参数名和#xxx一致；3. 拿不准就用默认generator自动生成Key，不要手写# |
