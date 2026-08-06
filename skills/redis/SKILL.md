---
name: ymp-redis
description: YMP框架Redis持久化模块，基于Jedis驱动封装，支持单节点/分片/哨兵/集群四种连接模式、多数据源、连接池、发布订阅
version: 2.1.4-dev
author: YMP Team
category: cache
tags:
  - java
  - redis
  - cache
  - nosql
  - pub-sub
trigger: 当用户需要使用Redis缓存、Jedis客户端封装、Redis发布订阅、Redis多数据源、Redis哨兵集群、Redis分片等场景时触发
tools:
  - redis
  - distributed-cache
  - pub-sub
examples:
  - Redis配置默认数据源与哨兵数据源
  - Redis openSession执行String/Hash/List/Set/ZSet操作
  - Redis subscribe/publish发布订阅消息
  - Redis多数据源切换与sharded/cluster连接判断
  - Redis缓存Key过期事件监听
---

# Redis 技能包

> AI读取指引：本模块专注Redis底层连接与命令执行。业务AOP缓存注解@Cacheable请跳转cache；关系型/NoSQL数据库跳转persistence-jdbc/mongodb；核心容器注解跳转core。

---

## 0. 快速索引
- Maven artifactId：`ymate-platform-persistence-redis`
- 静态入口类全限定名：`net.ymate.platform.persistence.redis.Redis`
- 必备注解（启动类）：`@EnableAutoScan` + `@EnableBeanProxy`
- 5行最简调用代码：

```java
Redis.get().openSession(session ->
    session.getConnectionHolder().getConnection().set("hello", "world"));
String v = Redis.get().openSession(session ->
    session.getConnectionHolder().getConnection().get("hello"));
```

## 1. 模块摘要
基于Jedis驱动封装，采用会话统一管理连接生命周期，整合default/shard/sentinel/cluster四种连接模式的API差异，提供多数据源、连接池、发布订阅与模式判断能力。

- 会话`IRedisSession` + Lambda执行器，自动归还连接到池
- 四种连接模式枚举`ConnectionType.DEFAULT/SHARD/SENTINEL/CLUSTER`
- 命令对象`IRedisCommander`合并Jedis各模式接口，String/Hash/List/Set/ZSet/HyperLogLog/Geo/Stream/Script全支持
- `subscribe()`异步线程池订阅频道；服务停止自动取消订阅
- 多数据源`openSession(dsName)`切换；`isCluster()/isSharded()/isSentinel()`判断模式取原始Jedis对象

## 2. 核心注解速查表

| 注解 | 全限定名 | 作用 | 核心参数2-5个 |
|------|----------|------|---------------|
| @RedisConf | `net.ymate.platform.persistence.redis.annotation.RedisConf` | 模块注解配置，声明数据源集合 | `dsDefaultName`默认数据源名；`value`=@RedisDataSource[] |
| @RedisDataSource | `net.ymate.platform.persistence.redis.annotation.RedisDataSource` | 单个Redis数据源定义（含连接池） | `name`数据源名；`connectionType`枚举；`servers`=@RedisServer[]；`masterServerName`（sentinel必填）；`autoConnection`；`poolMaxTotal`/`poolMaxIdle`/`poolMinIdle`等连接池参数 |
| @RedisServer | `net.ymate.platform.persistence.redis.annotation.RedisServer` | 数据源内单个Redis节点 | `name`服务名；`host`主机；`port`端口；`password`；`database`库索引；`timeout`连接超时；`weight`权重（shard用） |

## 3. 核心API速查

| API签名 | 作用 | 所在类/接口 |
|---------|------|------------|
| `Redis.get()` | 获取模块单例 | `Redis`静态入口 |
| `<T> T openSession(IRedisSessionExecutor<T>)` | Lambda开启默认数据源会话自动关闭 | `IRedis` |
| `<T> T openSession(String dsName, IRedisSessionExecutor<T>)` | 指定数据源开启会话 | `IRedis` |
| `IRedisSession openSession()` | 手动开启（需try-with-resources） | `IRedis` |
| `IRedisCommandHolder session.getConnectionHolder()` | 获取命令持有者（与数据源绑定） | `IRedisSession` |
| `IRedisCommander holder.getConnection()` | 获取命令对象执行Jedis API | `IRedisCommandHolder` |
| `commander.set/get/incr/decr/expire/del` | String键基础操作 | `IRedisCommander` |
| `commander.hset/hget/hgetAll/hdel/hlen/hmget` | Hash操作 | `IRedisCommander` |
| `commander.lpush/rpush/lpop/rpop/lrange/llen` | List操作 | `IRedisCommander` |
| `commander.sadd/srem/smembers/scard/sismember` | Set操作 | `IRedisCommander` |
| `commander.zadd/zrange/zrevrange/zscore/zrem/zcard` | 有序Set操作 | `IRedisCommander` |
| `IRedisCommandHolder getDefaultConnectionHolder()` / `getConnectionHolder(ds)` | 直接获取连接持有者（不经过session，需close） | `IRedis` |
| `boolean isCluster()/isSharded()/isSentinel()/isNormal()` | 判断连接模式 | `IRedisCommander` |
| `Object getOriginJedis()` | 取原始Jedis/JedisCluster/ShardedJedis | `IRedisCommander` |
| `void subscribe(JedisPubSub, String... channels)` | 订阅频道（异步线程池） | `IRedis` |
| `void subscribe(String dsName, JedisPubSub, String... channels)` | 指定数据源订阅 | `IRedis` |

## 4. 标准代码模板

### 模板1：ymp-conf.properties配置（默认+一个sentinel数据源）

```properties
#-------------------------------------
# Redis持久化模块
#-------------------------------------
ymp.configs.persistence.redis.ds_default_name=default
ymp.configs.persistence.redis.ds_name_list=default|sentinel_ds

#---- 默认数据源：单节点 default ----
ymp.configs.persistence.redis.ds.default.connection_type=default
ymp.configs.persistence.redis.ds.default.auto_connection=true
ymp.configs.persistence.redis.ds.default.server_name_list=default

ymp.configs.persistence.redis.ds.default.server.default.host=127.0.0.1
ymp.configs.persistence.redis.ds.default.server.default.port=6379
ymp.configs.persistence.redis.ds.default.server.default.password=123456
ymp.configs.persistence.redis.ds.default.server.default.database=0
ymp.configs.persistence.redis.ds.default.server.default.timeout=3000
ymp.configs.persistence.redis.ds.default.server.default.socket_timeout=3000

ymp.configs.persistence.redis.ds.default.pool.max_total=32
ymp.configs.persistence.redis.ds.default.pool.max_idle=16
ymp.configs.persistence.redis.ds.default.pool.min_idle=4
ymp.configs.persistence.redis.ds.default.pool.max_wait_millis=3000
ymp.configs.persistence.redis.ds.default.pool.test_on_borrow=false
ymp.configs.persistence.redis.ds.default.pool.test_while_idle=true
ymp.configs.persistence.redis.ds.default.pool.time_between_eviction_runs_millis=60000

#---- 第二个数据源：哨兵模式 sentinel_ds ----
ymp.configs.persistence.redis.ds.sentinel_ds.connection_type=sentinel
ymp.configs.persistence.redis.ds.sentinel_ds.auto_connection=true
ymp.configs.persistence.redis.ds.sentinel_ds.master_server_name=mymaster
ymp.configs.persistence.redis.ds.sentinel_ds.server_name_list=s1|s2|s3
# 密码设置到每个server节点上
ymp.configs.persistence.redis.ds.sentinel_ds.server.s1.host=sentinel-1.local
ymp.configs.persistence.redis.ds.sentinel_ds.server.s1.port=26379
ymp.configs.persistence.redis.ds.sentinel_ds.server.s2.host=sentinel-2.local
ymp.configs.persistence.redis.ds.sentinel_ds.server.s2.port=26379
ymp.configs.persistence.redis.ds.sentinel_ds.server.s3.host=sentinel-3.local
ymp.configs.persistence.redis.ds.sentinel_ds.server.s3.port=26379
ymp.configs.persistence.redis.ds.sentinel_ds.server.default.password=sentinelpass
ymp.configs.persistence.redis.ds.sentinel_ds.server.default.database=1
ymp.configs.persistence.redis.ds.sentinel_ds.server.default.timeout=5000
```

### 模板2：openSession执行String/Hash/List/Set/ZSet操作

```java
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
package com.example.redis;

import net.ymate.platform.persistence.redis.IRedisCommander;
import net.ymate.platform.persistence.redis.Redis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis五种数据结构操作示例
 *
 * @author Example
 * @since 2.1.4-dev
 */
public class RedisDataTypeExample {

    /**
     * String：写入带过期的Key并读取
     *
     * @param key     键
     * @param value   值
     * @param seconds 过期秒数
     * @return 读取到的值
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public String setAndGet(String key, String value, int seconds) throws Exception {
        return Redis.get().openSession(session -> {
            IRedisCommander cmd = session.getConnectionHolder().getConnection();
            cmd.setex(key, seconds, value);
            return cmd.get(key);
        });
    }

    /**
     * Hash：保存用户字段
     *
     * @param userId 用户ID
     * @param name   姓名
     * @param age    年龄
     * @return Map形式读取到的全部字段
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public Map<String, String> saveUserHash(String userId, String name, int age) throws Exception {
        return Redis.get().openSession(session -> {
            IRedisCommander cmd = session.getConnectionHolder().getConnection();
            String hkey = "user:" + userId;
            Map<String, String> m = new HashMap<>();
            m.put("name", name);
            m.put("age", String.valueOf(age));
            cmd.hmset(hkey, m);
            cmd.expire(hkey, 3600);
            return cmd.hgetAll(hkey);
        });
    }

    /**
     * List：消息队列左进右出
     *
     * @param queue 队列名
     * @param msg   消息内容
     * @return 弹出的一条消息
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public String queuePushPop(String queue, String msg) throws Exception {
        return Redis.get().openSession(session -> {
            IRedisCommander cmd = session.getConnectionHolder().getConnection();
            cmd.lpush(queue, msg);
            return cmd.rpop(queue);
        });
    }

    /**
     * Set：标签集合添加与全部读取
     *
     * @param userId 用户ID
     * @param tags   标签数组
     * @return 标签集合
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public Set<String> addTags(String userId, String... tags) throws Exception {
        return Redis.get().openSession(session -> {
            IRedisCommander cmd = session.getConnectionHolder().getConnection();
            String key = "tags:" + userId;
            cmd.sadd(key, tags);
            return cmd.smembers(key);
        });
    }

    /**
     * ZSet：排行榜前N名（倒序）
     *
     * @param leaderboard 排行榜Key
     * @param member      成员
     * @param score       分数
     * @param topN        取前多少名
     * @return 前N名成员列表
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public Set<String> leaderboardTopN(String leaderboard, String member, double score, int topN) throws Exception {
        return Redis.get().openSession(session -> {
            IRedisCommander cmd = session.getConnectionHolder().getConnection();
            cmd.zadd(leaderboard, score, member);
            return cmd.zrevrange(leaderboard, 0, topN - 1);
        });
    }
}
```

### 模板3：subscribe/publish发布订阅

```java
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
package com.example.redis;

import net.ymate.platform.persistence.redis.IRedisCommander;
import net.ymate.platform.persistence.redis.Redis;
import redis.clients.jedis.JedisPubSub;

/**
 * Redis发布订阅示例
 *
 * @author Example
 * @since 2.1.4-dev
 */
public class RedisPubSubExample {

    /**
     * 订阅多个频道（异步线程池执行，阻塞监听）
     *
     * @param onMessage 收到消息回调接口
     * @param channels  频道列表
     * @return 订阅对象（可用于后续unsubscribe）
     * @since 2.1.4-dev
     */
    public JedisPubSub subscribeChannels(MessageHandler onMessage, String... channels) {
        JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                onMessage.handle(channel, message);
            }
        };
        Redis.get().subscribe(pubSub, channels);
        return pubSub;
    }

    /**
     * 订阅缓存Key过期通知（需Redis开启notify-keyspace-events）
     *
     * @param database 数据库索引（示例中为0）
     * @return 订阅对象
     * @since 2.1.4-dev
     */
    public JedisPubSub subscribeKeyExpired(int database, MessageHandler onExpired) {
        JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                onExpired.handle(channel, message);
            }
        };
        String pattern = "__keyevent@" + database + "__:expired";
        Redis.get().subscribe(pubSub, pattern);
        return pubSub;
    }

    /**
     * 发布消息到指定频道
     *
     * @param channel 频道
     * @param message 消息内容
     * @return 收到消息的客户端数
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public long publishMessage(String channel, String message) throws Exception {
        return Redis.get().openSession(session -> {
            IRedisCommander cmd = session.getConnectionHolder().getConnection();
            return cmd.publish(channel, message);
        });
    }

    /**
     * 手动取消订阅（注意订阅是另一线程，确保subscribe命令已成功发出再调用）
     *
     * @param pubSub 订阅对象
     * @since 2.1.4-dev
     */
    public void unsubscribe(JedisPubSub pubSub) {
        if (pubSub.isSubscribed()) {
            pubSub.unsubscribe();
        }
    }

    /**
     * 消息处理函数式接口
     */
    @FunctionalInterface
    public interface MessageHandler {
        /**
         * 处理收到的消息
         *
         * @param channel 频道
         * @param message 消息体
         */
        void handle(String channel, String message);
    }
}
```

### 模板4：多数据源切换 + sharded/cluster连接方式简述

```java
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
package com.example.redis;

import net.ymate.platform.persistence.redis.IRedisCommander;
import net.ymate.platform.persistence.redis.IRedisCommandHolder;
import net.ymate.platform.persistence.redis.Redis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.ShardedJedis;

/**
 * 多数据源切换与连接模式判断示例
 *
 * @author Example
 * @since 2.1.4-dev
 */
public class RedisMultiDataSourceExample {

    /**
     * 跨数据源：从src读取、写入dst，返回两者值
     *
     * @param srcDs 源数据源名
     * @param dstDs 目标数据源名
     * @param key   键
     * @return 长度为2数组：[源值, 目标写入后的值]
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public String[] copyBetweenDataSources(String srcDs, String dstDs, String key) throws Exception {
        String srcVal = Redis.get().openSession(srcDs, session ->
                session.getConnectionHolder().getConnection().get(key));
        String dstVal = Redis.get().openSession(dstDs, session -> {
            IRedisCommander cmd = session.getConnectionHolder().getConnection();
            if (srcVal != null) {
                cmd.set(key, srcVal);
            }
            return cmd.get(key);
        });
        return new String[]{srcVal, dstVal};
    }

    /**
     * 根据连接模式取原始Jedis对象做定制操作（如pipeline、lua、transaction）
     *
     * 连接模式与对应原始对象：
     * - DEFAULT/SENTINEL => Jedis（单连接）
     * - SHARD => ShardedJedis（一致性哈希分片）
     * - CLUSTER => JedisCluster（Redis Cluster协议）
     *
     * 注意：SHARD模式部分接口（publish/subscribe/事务）有限制，仅第一个分片生效
     *
     * @param dsName 数据源名
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public void useOriginJedisByMode(String dsName) throws Exception {
        try (IRedisCommandHolder holder = Redis.get().getConnectionHolder(dsName)) {
            IRedisCommander cmd = holder.getConnection();
            if (cmd.isCluster()) {
                JedisCluster cluster = (JedisCluster) cmd.getOriginJedis();
                // cluster.mset / cluster.hmget 等集群多键操作（哈希槽需同节点）
            } else if (cmd.isSharded()) {
                ShardedJedis sharded = (ShardedJedis) cmd.getOriginJedis();
                // ShardedJedis仅支持单键命令；多键命令如mget/mset/sunionstore不可用
            } else if (cmd.isSentinel() || cmd.isNormal()) {
                Jedis jedis = (Jedis) cmd.getOriginJedis();
                // Jedis全功能：Pipelining / Transaction / 服务器管理命令
                try (redis.clients.jedis.Pipeline p = jedis.pipelined()) {
                    p.incr("counter");
                    p.expire("counter", 60);
                    p.sync();
                }
            }
        }
    }
}
```

## 5. 配置速查

### 5.1 配置文件最常改项

| Key | 默认值 | 说明 |
|-----|--------|------|
| `ymp.configs.persistence.redis.ds_default_name` | default | 默认数据源 |
| `ymp.configs.persistence.redis.ds_name_list` | default | 多数据源列表，竖线分隔 |
| `ymp.configs.persistence.redis.ds.<ds>.connection_type` | default | default/shard/sentinel/cluster四选一 |
| `ymp.configs.persistence.redis.ds.<ds>.auto_connection` | false | 初始化时完成连接 |
| `ymp.configs.persistence.redis.ds.<ds>.master_server_name` | default | sentinel模式必填，主节点名 |
| `ymp.configs.persistence.redis.ds.<ds>.server_name_list` | default | 该数据源节点列表，竖线分隔 |
| `ymp.configs.persistence.redis.ds.<ds>.server.<sv>.host` | localhost | 节点主机 |
| `ymp.configs.persistence.redis.ds.<ds>.server.<sv>.port` | 6379 | 节点端口 |
| `ymp.configs.persistence.redis.ds.<ds>.server.<sv>.password` | - | Redis认证密码 |
| `ymp.configs.persistence.redis.ds.<ds>.server.<sv>.database` | 0 | 数据库索引（0~15） |
| `ymp.configs.persistence.redis.ds.<ds>.server.<sv>.timeout` | 2000 | 连接超时(ms) |
| `ymp.configs.persistence.redis.ds.<ds>.server.<sv>.socket_timeout` | 2000 | socket读写超时(ms) |
| `ymp.configs.persistence.redis.ds.<ds>.pool.max_total` | 8 | 连接池最大连接数 |
| `ymp.configs.persistence.redis.ds.<ds>.pool.max_idle` | 8 | 连接池最大空闲 |
| `ymp.configs.persistence.redis.ds.<ds>.pool.max_wait_millis` | -1 | 取连接最大等待毫秒（-1无限） |

### 5.2 注解配置核心参数

**@RedisDataSource核心参数：**
- `name`：数据源名（必填）
- `connectionType`：`IRedis.ConnectionType`枚举（DEFAULT/SHARD/SENTINEL/CLUSTER）
- `servers`：`@RedisServer[]`节点数组（必填）
- `masterServerName`：SENTINEL模式必填
- `autoConnection`：是否启动即连
- 连接池参数：`poolMaxTotal`、`poolMaxIdle`、`poolMinIdle`、`poolMaxWaitMillis`、`poolTestOnBorrow`、`poolTestWhileIdle`、`poolTimeBetweenEvictionRunsMillis`
- `passwordEncrypted`/`passwordClass`

**@RedisServer核心参数：**
- `name`/`host`/`port`
- `password`/`database`
- `timeout`/`socketTimeout`/`maxAttempts`
- `weight`（分片权重）、`clientName`

## 6. 常见坑点排查

| 现象 | 原因 | 解决 |
|------|------|------|
| `subscribe`后无回调且未报错 | 订阅在独立线程，主线程提前退出；或频道/模式名写错 | 示例代码Main线程用`CountDownLatch`等保活；打印订阅前后状态`pubSub.isSubscribed()` |
| SENTINEL模式连不上报`No reachable node in cluster` | `master_server_name`配置错误或与哨兵真实master名不一致 | `redis-cli -p 26379 sentinel masters` 查看真实master名称，完全匹配 |
| `isCluster()` true但用`(Jedis) getOriginJedis()`强转抛ClassCast | 四种模式原始对象不同 | 先`isCluster()/isSharded()/isSentinel()/isNormal()`条件分支，再转JedisCluster/ShardedJedis/Jedis |
| SHARD模式`publish`仅第一个分片能收到订阅 | ShardedJedis不支持跨分片发布订阅 | 订阅/发布需求不用shard，改用default/sentinel/cluster；或单节点shard也可 |
| 连接池耗尽`Could not get a resource from the pool` | 漏关IRedisSession/IRedisCommandHolder；或池太小 | 统一用`openSession(executor)` Lambda或try-with-resources；按并发调大`max_total`；加`test_while_idle`清理死连接 |
| 多库`database`参数写在`server.<sv>`而非数据源级别 | cluster模式不支持select database，其余模式server级database生效 | default/shard/sentinel：每个server节点database相同即可；cluster一律用database=0 |
