# YMP框架缓存模块技能文档

## 1. 模块概述

缓存模块是以 EhCache 作为默认 JVM 进程内缓存服务，通过整合外部 Redis 服务实现多级缓存（MultiLevel）的轻量级缓存框架，并与 YMP 框架深度集成（支持针对类方法的缓存，可以根据方法参数值进行缓存），提供灵活的配置、易于使用和扩展的特性。

## 2. 核心功能

### 2.1 多级缓存支持
- **EhCache缓存**：基于内存的本地缓存，适合快速访问的热点数据
- **Redis缓存**：基于Redis的分布式缓存，适合跨应用共享数据
- **多级缓存**：融合EhCache和Redis两者的缓存服务，通过MultilevelKey决定缓存对象的获取方式

### 2.2 方法缓存
- 支持通过 `@Cacheable` 注解标记类方法，自动缓存方法执行结果
- 支持根据方法参数值生成缓存键
- 支持缓存作用域设置

### 2.3 缓存事件监听
- 提供缓存事件监听接口，可监听缓存元素的添加、更新、过期、删除等事件
- 支持通过事件机制处理缓存变化

### 2.4 灵活的配置
- 支持多种缓存提供者配置
- 支持自定义缓存键生成器
- 支持自定义序列化服务
- 支持缓存作用域处理器

## 3. 技术架构

### 3.1 核心接口

| 接口名称 | 描述 | 实现类 |
|---------|------|-------|
| `ICacheProvider` | 缓存服务提供者接口 | `DefaultCacheProvider`, `RedisCacheProvider`, `MultilevelCacheProvider` |
| `ICacheKeyGenerator` | 缓存Key生成器接口 | `DefaultCacheKeyGenerator` |
| `ISerializer` | 序列化服务接口 | `DefaultSerializer` |
| `ICacheEventListener` | 缓存事件监听接口 | 需开发者实现 |
| `ICacheScopeProcessor` | 缓存作用域处理器接口 | 需开发者实现 |

### 3.2 架构层次

1. **API层**：提供 `Caches` 静态工具类，简化缓存操作
2. **核心层**：包含缓存管理、缓存提供者、缓存键生成器等核心组件
3. **实现层**：包含不同缓存提供者的具体实现
4. **集成层**：与YMP框架集成，支持方法缓存注解

## 4. API接口

### 4.1 核心接口

#### ICaches 接口

```java
// 获取缓存实例
ICache getCache(String name);

// 向默认缓存中写入数据
void put(String key, Object value);

// 向指定缓存中写入数据
void put(String cacheName, String key, Object value);

// 向默认缓存中写入数据并设置超时时间
void put(String key, Object value, long timeout);

// 向指定缓存中写入数据并设置超时时间
void put(String cacheName, String key, Object value, long timeout);

// 从默认缓存中获取数据
Object get(String key);

// 从指定缓存中获取数据
Object get(String cacheName, String key);

// 从默认缓存中移除数据
void remove(String key);

// 从指定缓存中移除数据
void remove(String cacheName, String key);

// 清空默认缓存
void clear();

// 清空指定缓存
void clear(String cacheName);

// 检查默认缓存中是否存在指定键
boolean contains(String key);

// 检查指定缓存中是否存在指定键
boolean contains(String cacheName, String key);

// 检查是否为多级缓存模式
boolean isMultilevel();
```

#### ICache 接口

```java
// 获取缓存名称
String getName();

// 向缓存中写入数据
void put(String key, Object value);

// 向缓存中写入数据并设置超时时间
void put(String key, Object value, long timeout);

// 从缓存中获取数据
Object get(String key);

// 从缓存中移除数据
void remove(String key);

// 清空缓存
void clear();

// 检查缓存中是否存在指定键
boolean contains(String key);

// 获取缓存元素数量
int size();

// 检查缓存是否为空
boolean isEmpty();

// 获取缓存事件监听器
ICacheEventListener getCacheEventListener();

// 获取缓存锁
ICacheLocker acquireCacheLocker();
```

### 4.2 注解接口

#### @Cacheable 注解

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cacheable {
    /**
     * 缓存名称, 默认值为default
     */
    String cacheName() default "default";

    /**
     * 缓存Key，若以 # 开头则尝试从方法参数中获取该参数值，若未设置则使用Key生成器自动生成
     */
    String key() default "";

    /**
     * Key生成器接口实现类，默认为DefaultKeyGenerator.class
     */
    Class<? extends ICacheKeyGenerator> generator() default DefaultCacheKeyGenerator.class;

    /**
     * 缓存作用域，可选值为 APPLICATION 、SESSION 和 DEFAULT，默认为 DEFAULT
     */
    Scope scope() default Scope.DEFAULT;

    /**
     * 缓存数据超时时间（秒）， 默认值为0表示使用缓存配置的缓存数据超时时间
     */
    int timeout() default 0;
}
```

#### @CacheConf 注解

```java
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConfigurationConf
public @interface CacheConf {
    /**
     * 默认缓存名称
     */
    String defaultCacheName() default "default";

    /**
     * 默认缓存数据超时时间(秒)
     */
    int defaultCacheTimeout() default 0;

    /**
     * Ehcache配置文件路径
     */
    String configFile() default "";

    /**
     * 是否采用Set存储缓存键名
     */
    boolean storageWithSet() default false;

    /**
     * 是否开启Redis订阅缓存元素过期事件
     */
    boolean subscribeExpired() default false;

    /**
     * Multilevel模式下是否自动同步Master和Slave级缓存
     */
    boolean multilevelSlavesAutoSync() default false;

    /**
     * 缓存提供者
     */
    String providerClass() default "";

    /**
     * 缓存对象事件监听器
     */
    String eventListenerClass() default "";

    /**
     * 缓存作用域处理器
     */
    String scopeProcessorClass() default "";

    /**
     * 缓存Key生成器
     */
    String keyGeneratorClass() default "";

    /**
     * 对象序列化接口实现
     */
    String serializerClass() default "";
}
```

## 5. 配置方式

### 5.1 配置文件参数

```properties
#-------------------------------------
# 缓存模块初始化参数
#-------------------------------------

# 缓存提供者, 可选参数, 默认值: default, 目前支持[default|redis|multilevel]或自定义类名称
ymp.configs.cache.provider_class=

# 缓存对象事件监听器, 可选参数, 此类需实现net.ymate.platform.cache.ICacheEventListener接口, 默认值: 空
ymp.configs.cache.event_listener_class=

# 缓存作用域处理器, 可选参数, 此类需实现net.ymate.platform.cache.ICacheScopeProcessor接口, 默认值: 空
ymp.configs.cache.scope_processor_class=

# 缓存Key生成器, 可选参数, 此类需实现net.ymate.platform.cache.ICacheKeyGenerator接口, 默认值: net.ymate.platform.cache.impl.DefaultCacheKeyGenerator
ymp.configs.cache.key_generator_class=

# 对象序列化接口实现, 可选参数, 默认值: SerializerManager.getDefaultSerializer()
ymp.configs.cache.serializer_class=

# 默认缓存名称, 可选参数, 默认值: default, 对应于Ehcache配置文件中设置name="__DEFAULT__"
ymp.configs.cache.default_cache_name=

# 默认缓存数据超时时间(秒), 可选参数, 数值必须大于等于0, 默认值: 0
ymp.configs.cache.default_cache_timeout=

# 可选参数, 若未设置或设置的文件路径无效将使用默认值: ${root}/cfgs/ehcache.xml
ymp.configs.cache.config_file=

# Redis是否采用Set存储缓存键名, 默认值: false
ymp.configs.cache.storage_with_set=

# 是否开启Redis订阅缓存元素过期事件, 可选参数, 默认值: false
ymp.configs.cache.enabled_subscribe_expired=

# Multilevel模式下是否自动同步Master和Slave级缓存, 可选参数, 默认值: false
ymp.configs.cache.multilevel_slave_auto_sync=
```

### 5.2 EhCache配置示例

```xml
<?xml version="1.0" encoding="UTF-8"?>
<ehcache updateCheck="false" dynamicConfig="false">

    <diskStore path="java.io.tmpdir"/>

    <defaultCache
            maxElementsInMemory="10000"
            eternal="false"
            timeToIdleSeconds="300"
            timeToLiveSeconds="300"
            maxElementsOnDisk="10000000"
            diskExpiryThreadIntervalSeconds="300"
            memoryStoreEvictionPolicy="LRU">
        <persistence strategy="localTempSwap"/>
    </defaultCache>

    <cache name="__DEFAULT__"
           maxElementsInMemory="10000"
           eternal="false"
           timeToIdleSeconds="300"
           timeToLiveSeconds="300"
           maxElementsOnDisk="10000000"
           diskExpiryThreadIntervalSeconds="300"
           memoryStoreEvictionPolicy="LRU"
           overflowToDisk="true"/>
</ehcache>
```

## 6. 使用示例

### 6.1 基本缓存操作

```java
@EnableAutoScan
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            ICaches caches = Caches.get();
            // 1. 将键名为key1的值写入默认缓存
            caches.put("key1", "value1");
            // 2. 从默认缓存中读取键名为key1的值
            System.out.println(caches.get("key1"));
            // 3. 将键名为key2的值写入指定名称的缓存（默认缓存的名称为：default）
            caches.put("default", "key2", "value2");
            // 4. 从指定名称的缓存中读取键名为key2的值
            System.out.println(caches.get("default", "key2"));
            // 5. 写入缓存时指定其超时时间（秒）
            caches.put("key3", "value3", 30);
            // 6. 更新指定缓存对象
            caches.update("key3", "Value updated.", 50);
            //
            // 7. 当采用多级缓存（multilevel）时，可以通过MultilevelKey对象设置操作缓存目标：
            if (caches.isMultilevel()) {
                MultilevelKey key = new MultilevelKey("key4", false);
                caches.put(key, "Value4", 50);
                // 8. 读取多级缓存对象
                caches.get(key);
            }
        }
    }
}
```

### 6.2 基于注解的方法缓存

```java
@Bean
@Cacheable
public class CacheDemo {

    @Cacheable
    public String sayHi(String name) {
        System.out.println("Not Cached");
        return "Hi, " + name;
    }
}

@EnableAutoScan
@EnableBeanProxy
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            CacheDemo demo = application.getBeanFactory().getBean(CacheDemo.class);
            System.out.println(demo.sayHi("YMP"));
            System.out.println(demo.sayHi("YMP"));
            //
            System.out.println("--------");
            //
            System.out.println(demo.sayHi("YMPer"));
            System.out.println(demo.sayHi("YMP"));
            System.out.println(demo.sayHi("YMPer"));
        }
    }
}
```

### 6.3 缓存同步锁

```java
@EnableAutoScan
@EnableBeanProxy
public class Starter {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, Starter.class.getName());
    }

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            ICache cache = Caches.get().getConfig().getCacheProvider().getCache("default");
            ICacheLocker cacheLocker = cache.acquireCacheLocker();
            // 设置锁的键名
            String cacheKey = "cacheLockKey";
            // 当采用多级缓存（multilevel）时，可以通过MultilevelKey对象设置操作缓存目标：
            // MultilevelKey cacheKey = new MultilevelKey("cacheLockKey", true);
            cacheLocker.writeLock(cacheKey);
            try {
                // ...
            } finally {
                cacheLocker.releaseWriteLock(cacheKey);
            }
        }
    }
}
```

## 7. 注意事项

1. **缓存键生成**：默认的缓存Key生成器会将类名、方法名和参数值进行序列化后计算其Hash值，确保唯一性

2. **多级缓存配置**：在使用多级缓存时，需要确保Redis服务可用，否则会降级为仅使用EhCache

3. **缓存作用域**：当使用非DEFAULT作用域时，需要实现ICacheScopeProcessor接口

4. **Redis配置**：使用Redis缓存时，需要确保Redis服务配置正确，包括连接信息、密码等

5. **序列化问题**：缓存的对象需要实现Serializable接口，否则可能导致序列化失败

6. **缓存过期**：设置合理的缓存过期时间，避免缓存数据过期导致的性能问题

7. **内存管理**：合理配置EhCache的内存使用上限，避免内存溢出

8. **锁竞争**：在高并发场景下，注意缓存锁的使用，避免锁竞争影响性能

## 8. 最佳实践

1. **缓存策略选择**：
   - 本地热点数据：使用EhCache
   - 分布式共享数据：使用Redis
   - 混合场景：使用多级缓存

2. **缓存键设计**：
   - 使用有意义的缓存键前缀
   - 包含业务标识和参数信息
   - 避免过长的缓存键

3. **缓存粒度控制**：
   - 合理控制缓存粒度，避免缓存过大
   - 对于复杂对象，考虑缓存部分字段而非整个对象

4. **缓存更新策略**：
   - 采用主动更新或过期策略
   - 对于频繁变化的数据，使用较短的过期时间
   - 对于不常变化的数据，使用较长的过期时间

5. **缓存监控**：
   - 实现ICacheEventListener接口，监控缓存操作
   - 记录缓存命中率和性能指标

6. **缓存预热**：
   - 在应用启动时预热热点数据
   - 避免首次访问时的性能抖动

7. **异常处理**：
   - 缓存操作失败时应有降级策略
   - 避免缓存异常影响业务流程

8. **性能优化**：
   - 合理设置缓存大小和过期时间
   - 避免缓存穿透和缓存雪崩
   - 使用批量操作减少缓存访问次数

## 9. 总结

缓存模块是YMP框架中一个重要的性能优化组件，通过提供多种缓存实现和灵活的配置选项，帮助开发者构建高性能的应用系统。合理使用缓存模块可以显著提高应用的响应速度和吞吐量，特别是在处理频繁访问的数据时。

开发者应该根据具体的业务场景选择合适的缓存策略，并遵循最佳实践，以充分发挥缓存模块的性能优势。同时，需要注意缓存的一致性、可靠性和安全性，确保系统的稳定运行。
