---
sidebar_position: 3
slug: redis
---

# Redis

基于 Jedis 驱动，以 JDBC 持久化模块的设计思想进行简单封装，采用会话机制，统一 API 调用，简化订阅（subscribe）和发布（publish）操作，支持多数据源及连接池配置，支持 jedis、shard、sentinel 和 cluster 等数据源连接方式。



## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-persistence-redis</artifactId>
    <version>2.1.1</version>
</dependency>
```



## 模块配置

### 配置文件参数说明

```properties
#-------------------------------------
# Redis持久化模块初始化参数
#-------------------------------------

# 默认数据源名称，默认值为default
ymp.configs.persistence.redis.ds_default_name=

# 数据源列表，多个数据源名称间用'|'分隔，默认为default
ymp.configs.persistence.redis.ds_name_list=

# 数据源连接方式, 默认为default，目前支持[default|shard|sentinel|cluster]
ymp.configs.persistence.redis.ds.default.connection_type=

# Redis服务端名称列表, 多个服务端名称间用'|'分隔, 默认为default
ymp.configs.persistence.redis.ds.default.server_name_list=

# 当connection_type=sentinel时, 参数master_server_name必须提供, 默认为default
ymp.configs.persistence.redis.ds.default.master_server_name=

# 服务端--主机地址, 默认为localhost
ymp.configs.persistence.redis.ds.default.server.default.host=

# 服务端--主机端口, 默认为6379
ymp.configs.persistence.redis.ds.default.server.default.port=

# 服务端--连接超时时间(毫秒), 默认为2000
ymp.configs.persistence.redis.ds.default.server.default.timeout=

# 服务端--超时时间(毫秒), 默认为2000
ymp.configs.persistence.redis.ds.default.server.default.socket_timeout=

# 服务端--最大尝试次数, 默认为3
ymp.configs.persistence.redis.ds.default.server.default.max_attempts=

# 服务端--连接权重, 默认为1
ymp.configs.persistence.redis.ds.default.server.default.weight=

# 服务端--数据库索引, 默认为0
ymp.configs.persistence.redis.ds.default.server.default.database=

# 服务端--客户端名称, 默认为空
ymp.configs.persistence.redis.ds.default.server.default.client_name=

# 服务端--身份认证密码, 选填, 默认为空
ymp.configs.persistence.redis.ds.default.server.default.password=

# 服务端--身份认证密码是否已加密，默认为false
ymp.configs.persistence.redis.ds.default.server.default.password_encrypted=

# 服务端--密码处理器，可选参数，用于对已加密访问密码进行解密，默认为空
ymp.configs.persistence.redis.ds.default.server.default.password_class=

#-------------------------------------
# 连接池相关配置参数
#-------------------------------------

# 连接池--最大空闲连接数, 默认为8
ymp.configs.persistence.redis.ds.default.pool.max_idle=

# 连接池--最大连接数, 默认为8
ymp.configs.persistence.redis.ds.default.pool.max_total=

# 连接池--最小空闲连接数, 默认为0
ymp.configs.persistence.redis.ds.default.pool.min_idle=

# 连接池--连接耗尽时是否阻塞, false报异常, ture阻塞直到超时, 默认为true
ymp.configs.persistence.redis.ds.default.pool.block_when_exhausted=

# 连接池--当从池中获取资源或者将资源还回池中时是否使用ReentrantLock公平锁机制, 默认为false
ymp.configs.persistence.redis.ds.default.pool.fairness=

# 连接池--设置逐出策略类名, 默认为DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
ymp.configs.persistence.redis.ds.default.pool.eviction_policy_class_name=

# 连接池--是否启用JMX管理功能, 默认为true
ymp.configs.persistence.redis.ds.default.pool.jmx_enabled=

# 连接池--设置JMX基础名
ymp.configs.persistence.redis.ds.default.pool.jmx_name_base=

# 连接池--设置JMX前缀名,默认值pool
ymp.configs.persistence.redis.ds.default.pool.jmx_name_prefix=

# 连接池--是否启用后进先出, 默认为true
ymp.configs.persistence.redis.ds.default.pool.lifo=

# 连接池--获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted), 如果超时就抛异常, 小于零:阻塞不确定的时间, 默认为-1
ymp.configs.persistence.redis.ds.default.pool.max_wait_millis=

# 连接池--设置连接最小的逐出间隔时间，默认1800000毫秒
ymp.configs.persistence.redis.ds.default.pool.min_evictable_idle_time_millis=

# 连接池--对象空闲多久后逐出, 当空闲时间>该值且空闲连接>最大空闲数时直接逐出, 不再根据MinEvictableIdleTimeMillis判断(默认逐出策略)
ymp.configs.persistence.redis.ds.default.pool.soft_min_evictable_idle_time_millis=

# 连接池--在获取连接的时候检查有效性, 默认为false
ymp.configs.persistence.redis.ds.default.pool.test_on_borrow=

# 连接池--在创建连接时测试连接对象的有效性, 默认为false
ymp.configs.persistence.redis.ds.default.pool.test_on_create=

# 连接池--在归还到池中前进行检验, 默认为false
ymp.configs.persistence.redis.ds.default.pool.test_on_return=

# 连接池--在空闲时检查有效性, 默认为false
ymp.configs.persistence.redis.ds.default.pool.test_while_idle=

# 连接池--每次逐出检查时逐出的最大数目, 如果为负数就是1/abs(n), 默认为3
ymp.configs.persistence.redis.ds.default.pool.num_tests_per_eviction_run=

# 连接池--逐出扫描的时间间隔(毫秒), 如果为负数则不运行逐出线程, 默认为-1
ymp.configs.persistence.redis.ds.default.pool.time_between_eviction_runs_millis=
```



### 配置注解参数说明

:::tip **特别说明：** 

当 Redis 持久化模块初始化时，若在配置文件中存在数据源相关配置，则基于注解的数据源配置将全部失效。

:::



#### @RedisConf

| 配置项        | 描述           |
| ------------- | -------------- |
| dsDefaultName | 默认数据源名称 |
| value         | 数据源配置     |



#### @RedisServer

| 配置项        | 描述               |
| ------------- | ------------------ |
| name          | 服务器名称         |
| host          | 主机地址           |
| port          | 主机端口           |
| timeout       | 连接超时时间(毫秒) |
| socketTimeout | 超时时间(毫秒)     |
| maxAttempts   | 最大尝试次数       |
| weight        | 连接权重           |
| database      | 数据库索引         |
| clientName    | 客户端名称         |
| password      | 身份认证密码       |



#### @RedisDataSource

| 配置项                             | 描述                                 |
| ---------------------------------- | ------------------------------------ |
| name                               | 数据源名称                           |
| connectionType                     | 数据源连接方式                       |
| masterServerName                   | 主服务端名称                         |
| servers                            | 服务端 `@RedisServer` 集合           |
| passwordEncrypted                  | 身份认证密码是否已加密               |
| passwordClass                      | 密码处理器                           |
| poolMinIdle                        | 连接池--最小空闲连接数               |
| poolMaxIdle                        | 连接池--最大空闲连接数               |
| poolMaxTotal                       | 连接池--最大连接数                   |
| poolBlockWhenExhausted             | 连接池--连接耗尽时是否阻塞           |
| poolFairness                       |                                      |
| poolJmxEnabled                     | 连接池--是否启用 JMX 管理功能        |
| poolJmxNameBase                    |                                      |
| poolJmxNamePrefix                  |                                      |
| poolEvictionPolicyClassName        | 连接池--设置逐出策略类名             |
| poolLifo                           | 连接池--是否启用后进先出             |
| poolMaxWaitMillis                  | 连接池--获取连接时的最大等待毫秒数   |
| poolMinEvictableIdleTimeMillis     |                                      |
| poolSoftMinEvictableIdleTimeMillis | 连接池--对象空闲多久后逐出           |
| poolTestOnBorrow                   | 连接池--在获取连接的时候检查有效性   |
| poolTestOnReturn                   | 连接池--在归还到池中前进行检验       |
| poolTestOnCreate                   |                                      |
| poolTestWhileIdle                  | 连接池--在空闲时检查有效性           |
| poolNumTestsPerEvictionRun         | 连接池--每次逐出检查时逐出的最大数目 |
| poolTimeBetweenEvictionRunsMillis  | 连接池--逐出扫描的时间间隔(毫秒)     |



## 数据源（DataSource）

### 多数据源连接

Redis 持久化模块默认支持多数据源配置，下面通过简单的配置来展示如何连接多个服务：

```properties
# 定义两个数据源分别用于连接本地和另一台IP地址两个Redis服务
ymp.configs.persistence.redis.ds_default_name=default
ymp.configs.persistence.redis.ds_name_list=default|otherredis

# 默认数据源连接本地默认端口Redis服务
ymp.configs.persistence.redis.ds.default.connection_type=default
ymp.configs.persistence.redis.ds.default.server.default.password=123456

# 名称为otherredis数据源连接指定IP地址和端口的Redis服务
ymp.configs.persistence.redis.ds.otherredis.connection_type=default
ymp.configs.persistence.redis.ds.otherredis.server.default.host=192.168.10.110
ymp.configs.persistence.redis.ds.otherredis.server.default.port=86379
ymp.configs.persistence.redis.ds.otherredis.server.default.database=1
ymp.configs.persistence.redis.ds.otherredis.server.default.password=654321
```

通过注解方式配置多数据源，如下所示：

```java
@RedisConf(dsDefaultName = "default", value = {
        @RedisDataSource(
                name = "default",
                connectionType = IRedis.ConnectionType.DEFAULT,
                servers = {
                        @RedisServer(name = "default", password = "123456")
                }),
        @RedisDataSource(
                name = "otherredis",
                servers = @RedisServer(
                        name = "default",
                        host = "192.168.10.110",
                        port = 86397,
                        database = 1,
                        password = "654321"))
})
```



### Redis 命令持有者（IRedisCommandHolder）

在 Redis 持久化模块中获取的连接对象被称之为命令持有者，命令持有者同样用于记录真正的 Redis 连接对象的原始状态及与数据源对应关系，而通过命令持有者获取的连接对象被称之为命令对象（IRedisCommander）用于执行最终的 Redis 命令。

:::tip **特别说明：** 

Redis 持久化模块的命令对象（IRedisCommander）已将 Jedis 驱动包中的各种模式下的接口进行整合。因此在大版本升级 Jedis 驱动时可能存在运行时错误，此时则需要重新适配最新版本的驱动并重新编译模块。

:::



**示例：**

```java
public class Main {

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (application.isInitialized()) {
                // 获取当前容器内Redis模块实例
                IRedis redis = application.getModuleManager().getModule(Redis.class);
                // 获取默认数据源的命令持有者实例，等同于 redis.getConnectionHolder("default");
                IRedisCommandHolder commandHolder = redis.getDefaultConnectionHolder();
                // 获取指定名称数据源命令持有者实例
                commandHolder = redis.getConnectionHolder("otherredis");
                // 获取命令对象
                IRedisCommander connection = commandHolder.getConnection();
                // 获取数据源配置对象
                IRedisDataSourceConfig dataSourceConfig = commandHolder.getDataSourceConfig();
                // 获取当前数据源适配器对象
                IRedisDataSourceAdapter dataSourceAdapter = commandHolder.getDataSourceAdapter();
                // 获取当前命令持有者所属Redis模块实例
                IRedis owner = commandHolder.getOwner();
            }
        }
    }
}
```



## 会话（Session）

会话是对应用中具体业务操作触发的一系列与 Redis 之间的交互过程的封装，通过建立一个临时通道，负责与 Redis 之间连接资源的创建及回收，同时提供更为高级的抽象指令接口调用，基于会话的优点：

- 开发人员不需要担心连接资源是否正确释放。
- 严格的编码规范更利于维护和理解。
- 更好的业务封装性。



### 如何开启会话

**示例：** 使用默认数据源开启会话

```java
Redis.get().openSession(new IRedisSessionExecutor<Object>() {
    @Override
    public Object execute(IRedisSession session) throws Exception {
        return session.getConnectionHolder().getConnection().set("key", "value");
    }
});
```



**示例：** 使用指定的数据源开启会话

```java
String value = Redis.get().openSession("otherredis", new IRedisSessionExecutor<String>() {
    @Override
    public String execute(IRedisSession session) throws Exception {
        return session.getConnectionHolder().getConnection().get("key");
    }
});
```



**示例：** 手动开启与关闭会话

```java
// 一定要确保连接使用完毕后关闭会话以释放连接
try (IRedisSession session = Redis.get().openSession()) {
    IRedisCommandHolder holder = session.getConnectionHolder();
    IRedisCommander commander = holder.getConnection();
    commander.set("key", "value");
}
```



## 连接模式判断

由于 Jedis 驱动程序会根据连接模式的不同，分别使用 JedisCluster、ShardedJedis 和 Jedis 对象进行初始化，这将导致在不同模式下的部份接口方法并不支持，而在 Redis 持久化模块中为了使接口调用方式统一，对其进行了整合，所以开发人员需要在实际工作中判断并根据当前 Redis 的连接模式的不同编写对应的代码。



**示例：** 根据连接模式获取对应的原始 Jedis 对象

```java
try (IRedisCommandHolder holder = Redis.get().getDefaultConnectionHolder()) {
    IRedisCommander commander = holder.getConnection();
    if (commander.isCluster()) {
        // 集群模式
        JedisCluster jedisCluster = (JedisCluster) commander.getOriginJedis();
        // ......
    } else if (commander.isSharded()) {
        // 分片模式
        ShardedJedis shardedJedis = (ShardedJedis) commander.getOriginJedis();
        // ......
    } else if (commander.isNormal() || commander.isSentinel()) {
        // 正常模式或哨兵模式
        Jedis jedis = (Jedis) commander.getOriginJedis();
        // ......
    }
}
```



## 消息订阅



**示例：** 订阅缓存 Key 过期通知

```java
JedisPubSub jedisPubSub = new JedisPubSub() {
    @Override
    public void onMessage(String channel, String message) {
        System.out.printf("channel: %s, message: %s%n", channel, message);
    }
};
Redis.get().subscribe(jedisPubSub, "__keyevent@0__:expired");
// 或指定数据源
Redis.get().subscribe("otherredis", jedisPubSub, "__keyevent@0__:expired");

// 手动取消订阅（注意：消息订阅是由另一个线程处理的，手动取消之前需要确保消息订阅命令已成功执行）
if (jedisPubSub,isSubscribed() {
    jedisPubSub.unsubscribe();
}
```

:::tip **说明：** 

若订阅对象 `JedisPubSub` 未手动取消订阅，将在服务停止时被自动取消订阅。分片模式不支持消息订阅，因此当在分片模式下调用订阅命令时，该命令将被发送至第一个分片服务。

:::
