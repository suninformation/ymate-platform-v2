---
name: ymp-redis
description: YMP框架Redis模块，基于Jedis驱动的Redis客户端封装，支持多数据源、连接池、发布订阅等
version: 2.1.4
author: YMP Team
category: cache
tags:
  - java
  - redis
  - cache
  - nosql
trigger: 当用户需要使用Redis缓存、分布式缓存、发布订阅等场景时触发
tools:
  - redis
  - distributed-cache
  - pub-sub
examples:
  - 配置Redis数据源
  - 使用Redis缓存
  - 实现发布订阅
  - 使用Redis集群
  - 使用Redis哨兵
---

# Redis 持久化模块

## 1. 模块概览

Redis 持久化模块是 YMP 框架中基于 Jedis 驱动的 Redis 客户端封装，以 JDBC 持久化模块的设计思想进行简单封装，采用会话机制，统一 API 调用，简化订阅（subscribe）和发布（publish）操作，支持多数据源及连接池配置，支持 jedis、shard、sentinel 和 cluster 等数据源连接方式。

- **统一 API 调用**：封装不同 Redis 连接模式的操作接口，提供一致的 API 调用方式
- **多数据源支持**：支持配置多个 Redis 数据源，适用于复杂的应用场景
- **连接池管理**：内置连接池配置，优化连接资源的使用
- **会话机制**：采用会话机制管理连接资源，确保资源正确释放
- **发布/订阅支持**：简化 Redis 的发布/订阅操作

## 2. 核心功能

### 2.1 多数据源配置

Redis 持久化模块支持配置多个数据源，每个数据源可以有不同的连接配置，适用于连接不同的 Redis 服务或不同的数据库。

### 2.2 多种连接方式

支持以下四种连接方式：
- **default**：单节点连接
- **shard**：分片连接
- **sentinel**：哨兵模式连接
- **cluster**：集群模式连接

### 2.3 连接池管理

内置连接池配置，可根据应用需求调整连接池参数，如最大连接数、最小空闲连接数、连接超时时间等。

### 2.4 会话机制

通过会话机制管理连接资源，确保连接正确创建和释放，避免资源泄漏。

### 2.5 发布/订阅支持

简化 Redis 的发布/订阅操作，支持订阅指定频道的消息。

## 3. 核心 API/类/函数

### 3.1 IRedis 接口

Redis 持久化模块的核心接口，提供获取连接持有者、开启会话等方法。

### 3.2 IRedisCommandHolder 接口

Redis 命令持有者，用于记录真正的 Redis 连接对象的原始状态及与数据源对应关系。

**主要方法**：
- `getConnection()`：获取命令对象（IRedisCommander），用于执行最终的 Redis 命令
- `getDataSourceConfig()`：获取数据源配置对象
- `getDataSourceAdapter()`：获取当前数据源适配器对象
- `getOwner()`：获取当前命令持有者所属 Redis 模块实例

### 3.3 IRedisCommander 接口

Redis 命令对象，整合了 Jedis 驱动包中的各种模式下的接口，用于执行 Redis 命令。

**主要方法**：
- 各种 Redis 命令方法，如 `set()`, `get()`, `hset()`, `hget()`, `lpush()`, `lpop()` 等
- `isCluster()`：判断是否为集群模式
- `isSharded()`：判断是否为分片模式
- `isNormal()`：判断是否为正常模式
- `isSentinel()`：判断是否为哨兵模式
- `getOriginJedis()`：获取原始的 Jedis 对象

### 3.4 IRedisSession 接口

Redis 会话接口，负责与 Redis 之间连接资源的创建及回收，同时提供更为高级的抽象指令接口调用。

**主要方法**：
- `getConnectionHolder()`：获取连接持有者

### 3.5 Redis 工具类

- `Redis.get()`：获取 Redis 模块实例

## 4. 技术架构与实现

### 4.1 架构层次

1. **API 层**：提供统一的 Redis 操作接口
2. **会话层**：管理连接资源的创建和释放
3. **命令层**：执行具体的 Redis 命令
4. **连接层**：管理与 Redis 服务器的连接

### 4.2 核心组件

- **IRedis**：Redis 模块的核心接口
- **IRedisCommandHolder**：命令持有者，管理连接对象
- **IRedisCommander**：命令执行器，执行 Redis 命令
- **IRedisSession**：会话管理，管理连接资源
- **IRedisDataSourceAdapter**：数据源适配器，适配不同的连接模式

### 4.3 工作流程

1. 通过 `Redis.get()` 获取 Redis 模块实例
2. 调用 `openSession()` 开启会话
3. 在会话中通过 `getConnectionHolder().getConnection()` 获取命令对象
4. 使用命令对象执行 Redis 命令
5. 会话结束时，连接资源自动释放

## 5. 使用指南与典型场景

### 5.1 基本配置

**配置文件方式**：

```properties
# 默认数据源名称，默认值为default
ymp.configs.persistence.redis.ds_default_name=default

# 数据源列表，多个数据源名称间用'|'分隔，默认为default
ymp.configs.persistence.redis.ds_name_list=default|otherredis

# 是否自动连接, 即模块初始化时完成连接动作, 默认值: false
ymp.configs.persistence.redis.ds.default.auto_connection=true

# 数据源连接方式, 默认为default，目前支持[default|shard|sentinel|cluster]
ymp.configs.persistence.redis.ds.default.connection_type=default

# Redis服务端名称列表, 多个服务端名称间用'|'分隔, 默认为default
ymp.configs.persistence.redis.ds.default.server_name_list=default

# 当connection_type=sentinel时, 参数master_server_name必须提供, 默认为default
ymp.configs.persistence.redis.ds.default.master_server_name=default

# 服务端--主机地址, 默认为localhost
ymp.configs.persistence.redis.ds.default.server.default.host=localhost

# 服务端--主机端口, 默认为6379
ymp.configs.persistence.redis.ds.default.server.default.port=6379

# 服务端--连接超时时间(毫秒), 默认为2000
ymp.configs.persistence.redis.ds.default.server.default.timeout=2000

# 服务端--身份认证密码, 选填, 默认为空
ymp.configs.persistence.redis.ds.default.server.default.password=

# 连接池--最大空闲连接数, 默认为8
ymp.configs.persistence.redis.ds.default.pool.max_idle=8

# 连接池--最大连接数, 默认为8
ymp.configs.persistence.redis.ds.default.pool.max_total=8

# 连接池--最小空闲连接数, 默认为0
ymp.configs.persistence.redis.ds.default.pool.min_idle=0

# 连接池--获取连接时的最大等待毫秒数, 默认为-1
ymp.configs.persistence.redis.ds.default.pool.max_wait_millis=-1
```

**注解方式**：

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

### 5.2 基本使用

**示例 1：使用默认数据源开启会话**

```java
Redis.get().openSession(new IRedisSessionExecutor<Object>() {
    @Override
    public Object execute(IRedisSession session) throws Exception {
        return session.getConnectionHolder().getConnection().set("key", "value");
    }
});
```

**示例 2：使用指定的数据源开启会话**

```java
String value = Redis.get().openSession("otherredis", new IRedisSessionExecutor<String>() {
    @Override
    public String execute(IRedisSession session) throws Exception {
        return session.getConnectionHolder().getConnection().get("key");
    }
});
```

**示例 3：手动开启与关闭会话**

```java
// 一定要确保连接使用完毕后关闭会话以释放连接
try (IRedisSession session = Redis.get().openSession()) {
    IRedisCommandHolder holder = session.getConnectionHolder();
    IRedisCommander commander = holder.getConnection();
    commander.set("key", "value");
}
```

### 5.3 连接模式判断

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

### 5.4 消息订阅

**示例：订阅缓存 Key 过期通知**

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
if (jedisPubSub.isSubscribed()) {
    jedisPubSub.unsubscribe();
}
```

## 6. 配置、部署与开发

### 6.1 依赖配置

在 Maven 项目中添加以下依赖：

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-persistence-redis</artifactId>
    <version>2.1.4-dev</version>
</dependency>
```

### 6.2 配置参数

**主要配置参数**：

| 配置项 | 描述 | 默认值 |
| ------ | ---- | ------ |
| ds_default_name | 默认数据源名称 | default |
| ds_name_list | 数据源列表，多个数据源名称间用'|'分隔 | default |
| ds.default.auto_connection | 是否自动连接 | false |
| ds.default.connection_type | 数据源连接方式，支持[default|shard|sentinel|cluster] | default |
| ds.default.server_name_list | Redis服务端名称列表 | default |
| ds.default.master_server_name | 当connection_type=sentinel时的主服务端名称 | default |
| ds.default.server.default.host | 服务端主机地址 | localhost |
| ds.default.server.default.port | 服务端主机端口 | 6379 |
| ds.default.server.default.timeout | 连接超时时间(毫秒) | 2000 |
| ds.default.server.default.password | 身份认证密码 | 空 |
| ds.default.pool.max_idle | 连接池最大空闲连接数 | 8 |
| ds.default.pool.max_total | 连接池最大连接数 | 8 |
| ds.default.pool.min_idle | 连接池最小空闲连接数 | 0 |
| ds.default.pool.max_wait_millis | 获取连接时的最大等待毫秒数 | -1 |

### 6.3 开发建议

1. **使用会话管理连接**：始终使用 `openSession()` 方法开启会话，并在会话中执行 Redis 操作，确保连接资源正确释放。

2. **合理配置连接池**：根据应用的并发量和 Redis 服务器的性能，合理配置连接池参数，避免连接数过多或过少。

3. **选择合适的连接方式**：根据 Redis 部署方式选择合适的连接方式，如单机部署使用 default 方式，集群部署使用 cluster 方式。

4. **处理连接异常**：在执行 Redis 操作时，捕获并处理可能的连接异常，如连接超时、连接被拒绝等。

5. **使用管道和事务**：对于批量操作，使用 Redis 的管道（Pipeline）或事务（Transaction）来提高性能。

## 7. 监控与维护

### 7.1 连接状态监控

- **连接池状态**：监控连接池的使用情况，包括活跃连接数、空闲连接数等。
- **连接异常**：记录连接异常，如连接超时、连接被拒绝等。

### 7.2 性能监控

- **命令执行时间**：监控 Redis 命令的执行时间，识别慢命令。
- **命令执行频率**：监控命令的执行频率，识别高频命令。

### 7.3 常见问题与解决方案

| 问题 | 原因 | 解决方案 |
| ---- | ---- | ------ |
| 连接超时 | Redis 服务器响应慢或网络问题 | 检查网络连接，增加超时时间，优化 Redis 服务器性能 |
| 连接被拒绝 | Redis 服务器未启动或配置了访问控制 | 检查 Redis 服务器状态，确保网络可达，检查访问控制配置 |
| 内存不足 | Redis 服务器内存不足 | 增加 Redis 服务器内存，设置合理的内存淘汰策略 |
| 命令执行失败 | 命令参数错误或 Redis 服务器不支持该命令 | 检查命令参数，确保 Redis 服务器版本支持该命令 |

## 8. 总结与亮点回顾

Redis 持久化模块是 YMP 框架中一个功能强大、使用简便的 Redis 客户端封装，它的主要亮点包括：

- **统一 API**：整合了不同 Redis 连接模式的接口，提供一致的 API 调用方式，简化了开发。
- **多数据源支持**：支持配置多个 Redis 数据源，适用于复杂的应用场景。
- **会话机制**：采用会话机制管理连接资源，确保资源正确释放，避免资源泄漏。
- **连接池管理**：内置连接池配置，优化连接资源的使用，提高性能。
- **发布/订阅支持**：简化 Redis 的发布/订阅操作，方便实现消息通知等功能。

通过 Redis 持久化模块，开发者可以更加便捷地在 YMP 框架中使用 Redis，实现缓存、消息队列、计数器等功能，提高应用的性能和可靠性。
