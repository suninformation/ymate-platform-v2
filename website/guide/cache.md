---
sidebar_position: 9
slug: cache
---

# 缓存（Cache）

缓存模块是以 EhCache 作为默认 `JVM` 进程内缓存服务，通过整合外部 Redis 服务实现多级缓存（MultiLevel）的轻量级缓存框架，并与 YMP 框架深度集成（支持针对类方法的缓存，可以根据方法参数值进行缓存），灵活的配置、易于使用和扩展。



## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-cache</artifactId>
    <version>2.1.1</version>
</dependency>
```

:::tip **注意**：

若需要启用redis作为缓存服务，需额外添加以下依赖配置：

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-persistence-redis</artifactId>
    <version>2.1.1</version>
</dependency>
```

:::



## 基础接口概念

开发者可以根据以下接口完成对缓存模块的自定义扩展实现。

- 缓存服务提供者接口：`ICacheProvider`

    + DefaultCacheProvider - 基于EhCache缓存服务的默认缓存服务提供者接口实现类；
    + RedisCacheProvider - 基于Redis数据库的缓存服务提供者接口实现类；
    + MultievelCacheProvider - 融合EhCache和Redis两者的缓存服务提供者接口实现类，通过MultilevelKey决定缓存对象的获取方式；
- 缓存Key生成器接口：`ICacheKeyGenerator`

    + DefaultCacheKeyGenerator - 根据提供的类方法和参数对象生成缓存Key，默认是将方法和参数对象进行序列化后取其MD5值；
- 序列化服务接口：`ISerializer`

    + DefaultSerializer - 默认序列化服务采用JDK自带的对象序列化技术实现；
- 缓存事件监听接口：`ICacheEventListener`

  + 用于监听被缓存对象发生变化时的事件处理，需开发者实现接口；
- 缓存作用域处理器接口：`ICacheScopeProcessor`
    - 用于处理`@Cacheable`注解的`scope`参数设置为非DEFAULT作用域的缓存对象，需开发者实现接口；



## 模块配置

### 配置文件参数说明

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

# Ehcache配置文件路径, 可选参数, 若未设置或设置的文件路径无效将被忽略, 默认值: 空
ymp.configs.cache.config_file=

# Redis是否采用Set存储缓存键名, 默认值: false
ymp.configs.cache.storage_with_set=

# 是否开启Redis订阅缓存元素过期事件, 可选参数, 默认值: false
ymp.configs.cache.enabled_subscribe_expired=

# Multilevel模式下是否自动同步Master和Slave级缓存, 可选参数, 默认值: false
ymp.configs.cache.multilevel_slave_auto_sync=
```



### 配置注解参数说明

#### @CacheConf 

| 配置项                   | 描述                                                         |
| ------------------------ | ------------------------------------------------------------ |
| defaultCacheName         | 默认缓存名称                                                 |
| defaultCacheTimeout      | 默认缓存数据超时时间(秒)                                     |
| configFile               | Ehcache配置文件路径                                          |
| storageWithSet           | 是否采用Set存储缓存键名                                      |
| subscribeExpired         | 是否开启Redis订阅缓存元素过期事件<br />*（注：Redis服务需开启 `notify-keyspace-events Ex` 配置）* |
| multilevelSlavesAutoSync | Multilevel模式下是否自动同步Master和Slave级缓存              |
| providerClass            | 缓存提供者                                                   |
| eventListenerClass       | 缓存对象事件监听器                                           |
| scopeProcessorClass      | 缓存作用域处理器                                             |
| keyGeneratorClass        | 缓存Key生成器                                                |
| serializerClass          | 对象序列化接口实现                                           |



### EhCache配置示例

以下是默认 `ehcache.xml` 文件内容，模块初始化时会检查该文件是否存在，若不存在则生成并放置在`${root}/cfgs/`路径下。

```xml
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



## 模块事件

当缓存对象事件监听器（`event_listener_class`）采用默认配置时，可以通过`CacheEvent`捕获缓存事件，事件枚举对象包括以下事件类型：

|事务类型|说明|
|---|---|
|ELEMENT_PUT|缓存元素添加|
|ELEMENT_UPDATED|缓存元素更新|
|ELEMENT_EXPIRED|缓存元素过期|
|ELEMENT_EVICTED|缓存元素被驱逐|
|ELEMENT_REMOVED|缓存元素删除|
|ELEMENT_REMOVED_ALL|缓存元素被清空|



## 模块使用

### 示例一：通过缓存模块操作缓存数据

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

:::tip **注意**：

当指定缓存名称时，请确认 `ehcache.xml` 文件中与名称对应的配置是否已存在。

:::



### 示例二：基于注解完成类方法的缓存

这里用到了 `@Cacheable` 注解，作用是标识类中方法的执行结果是否进行缓存，需要注意的是：

> 首先 `@Cacheable` 注解必须在已注册到YMP类对象管理器的类上声明，表示该类支持缓存；
> 
> 其次，在需要缓存执行结果的方法上添加 `@Cacheable` 注解；

@Cacheable注解参数说明：

> cacheName：缓存名称, 默认值为default；
> 
> key：缓存Key，若以 `#` 开头则尝试从方法参数中获取该参数值，若未设置则使用Key生成器自动生成；
> 
> generator：Key生成器接口实现类，默认为DefaultKeyGenerator.class；
> 
> scope：缓存作用域，可选值为 `APPLICATION` 、`SESSION` 和 `DEFAULT` ，默认为 `DEFAULT` ，非 `DEFAULT` 设置需要缓存作用域处理器（`ICacheScopeProcessor`）接口配合；
> 
> timeout：缓存数据超时时间（秒）， 默认值为0表示使用缓存配置的缓存数据超时时间；

示例代码：

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

执行结果：

    Not Cached
    Hi, YMP
    Hi, YMP
    --------
    Not Cached
    Hi, YMPer
    Hi, YMP
    Hi, YMPer

以上结果输出可以看出，sayHi方法相同参数首次被调用时将输出“Not Cached”字符串，说明它没有使用缓存，再次调用时直接从缓存中返回值。



## 缓存Key的自动生成规则

在`@Cacheable`注解的参数说明中已经说明，若`key`参数值以 `#` 开头则尝试从对应的方法参数中获取该参数值，若未设置则使用指定的缓存Key生成器接口实现类自动生成，该接口的默认实现类采用的自动生成规则是将当前类名、方法名和方法参数值集合进行序列化后计算其Hash值，示意代码如下：

```java
@Override
public Serializable generateKey(Method method, Object[] params) throws Exception {
    // [className:methodName:{serializeStr}]
    String className = method.getDeclaringClass().getName();
    ISerializer serializer = SerializerManager.getDefaultSerializer();
	String paramStr = Base64.encodeBase64String(serializer.serialize(method.getParams()));
	String keyStr = String.format("[%s:%s{%s}]", className, method.getName(), paramStr);
    return DigestUtils.md5Hex(keyStr);
}
```



## 缓存同步锁

示例：

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

