---
name: ymp-serv
description: YMP框架服务通讯模块，基于NIO实现TCP/UDP客户端与服务端封装，支持消息编解码、心跳断线重连、会话管理与流量统计
version: 2.1.4-dev
author: YMP Team
category: framework
tags:
  - java
  - framework
  - serv
  - nio
  - tcp
  - udp
  - netty
  - codec
trigger: 当用户需要创建TCP/UDP服务端或客户端、配置编解码Codec(ByteArrayCodec/NioStringCodec/TextLineCodec)、心跳断线重连服务、会话管理SessionManager、NioServer/NioClient监听器时触发
tools:
  - tcp-server
  - tcp-client
  - udp-server
  - udp-client
  - codec
  - heartbeat
  - reconnect
examples:
  - TCP服务端监听端口接收消息并回写
  - TCP客户端带心跳+断线重连连接服务端
  - UDP服务端/客户端通讯
  - 自定义消息编解码器Codec
  - 会话管理器空闲检测+流量统计
---

# Serv 服务通讯技能包

> AI读取指引：本模块边界=NIO通讯(TCP/UDP)+编解码+心跳重连+会话管理；所有类路径前缀`net.ymate.platform.serv`；依赖core模块，事件相关跳转event/SKILL.md。

---

## 0. 快速索引

- Maven artifactId：`ymate-platform-serv`
- 静态入口类：`net.ymate.platform.serv.Servs`（createServer/createClient/createUdpServer/createUdpClient）
- 必备配置：`IServerCfg`(服务端) / `IClientCfg`(客户端) + `INioCodec`编解码器 + 监听器
- 5行最简TCP服务端：
```java
IServerCfg cfg = DefaultServerCfg.builder().serverName("srv").port(8281).build();
NioServer server = Servs.createServer(cfg, new TextLineCodec(), new NioServerListener() {
    public void onMessageReceived(Object msg, INioSession s) throws IOException { s.send("OK:"+msg); }
});
server.start();
```

## 1. 模块摘要

基于Java NIO的TCP/UDP通讯框架，封装服务端/客户端创建、消息编解码、连接维护（心跳/重连）、会话管理（空闲检测/流量统计），开发者仅需关注业务消息处理。

- **TCP/UDP双协议**：`NioServer`/`NioClient`(TCP) + `NioUdpServer`/`NioUdpClient`(UDP)
- **三种内置编解码器**：`ByteArrayCodec`(长度头) / `NioStringCodec`(字符串) / `TextLineCodec`(\r\n分隔)
- **内置连接服务**：`DefaultHeartbeatServiceImpl`(心跳) + `DefaultReconnectServiceImpl`(断线重连)
- **会话管理器**：`NioSessionManager`/`NioUdpSessionManager` 支持空闲检测+Speedometer流量统计
- **事件监听**：`NioServerListener`/`NioClientListener` 提供会话注册/连接/关闭/消息/异常回调

## 2. 核心注解/类速查表（全限定名）

| 类/注解 | 全限定名 | 核心作用 |
|---|---|---|
| Servs | `net.ymate.platform.serv.Servs` | 静态入口，创建Server/Client |
| IServerCfg | `net.ymate.platform.serv.IServerCfg` | 服务端配置接口 |
| DefaultServerCfg | `net.ymate.platform.serv.impl.DefaultServerCfg` | 服务端配置Builder实现 |
| IClientCfg | `net.ymate.platform.serv.IClientCfg` | 客户端配置接口 |
| DefaultClientCfg | `net.ymate.platform.serv.impl.DefaultClientCfg` | 客户端配置Builder实现 |
| INioServer | `net.ymate.platform.serv.nio.INioServer` | NIO服务端接口 |
| NioServer | `net.ymate.platform.serv.nio.server.NioServer` | TCP服务端实现 |
| NioUdpServer | `net.ymate.platform.serv.nio.datagram.NioUdpServer` | UDP服务端实现 |
| NioClient | `net.ymate.platform.serv.nio.client.NioClient` | TCP客户端实现 |
| NioUdpClient | `net.ymate.platform.serv.nio.datagram.NioUdpClient` | UDP客户端实现 |
| INioCodec | `net.ymate.platform.serv.nio.INioCodec` | 编解码器接口 |
| ByteArrayCodec | `net.ymate.platform.serv.nio.codec.ByteArrayCodec` | byte[4]长度头编解码器 |
| NioStringCodec | `net.ymate.platform.serv.nio.codec.NioStringCodec` | 字符串编解码器（继承ByteArrayCodec） |
| TextLineCodec | `net.ymate.platform.serv.nio.codec.TextLineCodec` | \r\n行分隔编解码器 |
| IHeartbeatService | `net.ymate.platform.serv.IHeartbeatService` | 心跳服务接口 |
| DefaultHeartbeatServiceImpl | `net.ymate.platform.serv.impl.DefaultHeartbeatServiceImpl` | 默认心跳实现（默认心跳消息"0"） |
| IReconnectService | `net.ymate.platform.serv.IReconnectService` | 断线重连服务接口 |
| DefaultReconnectServiceImpl | `net.ymate.platform.serv.impl.DefaultReconnectServiceImpl` | 默认断线重连实现 |
| NioServerListener | `net.ymate.platform.serv.nio.server.NioServerListener` | TCP服务端事件监听器（继承此类） |
| NioClientListener | `net.ymate.platform.serv.nio.client.NioClientListener` | TCP客户端事件监听器（继承此类） |
| AbstractNioUdpListener | `net.ymate.platform.serv.nio.datagram.AbstractNioUdpListener` | UDP监听器抽象类 |
| INioSession | `net.ymate.platform.serv.nio.INioSession` | 会话接口（send发送消息） |
| IClient | `net.ymate.platform.serv.IClient` | 客户端接口 |

## 3. 核心API速查（≤8条最常用）

- `Servs.createServer(IServerCfg, INioCodec, NioServerListener)` → `NioServer`：创建TCP服务端
- `Servs.createClient(IClientCfg, INioCodec, IReconnectService, IHeartbeatService, NioClientListener)` → `NioClient`：创建TCP客户端（含重连+心跳）
- `Servs.createUdpServer(IServerCfg, INioCodec, AbstractNioUdpListener)` → `NioUdpServer`：创建UDP服务端
- `Servs.createUdpClient(IClientCfg, INioCodec, IHeartbeatService, AbstractNioUdpListener)` → `NioUdpClient`：创建UDP客户端
- `NioServer.start()` / `NioClient.connect()`：启动服务端 / 连接客户端
- `INioSession.send(Object message)`：会话发送消息
- `DefaultServerCfg.builder().serverName().serverHost().port().build()`：构建服务端配置
- `DefaultClientCfg.builder().clientName().remoteHost().port().heartbeatInterval().reconnectionInterval().build()`：构建客户端配置

## 4. 标准代码模板

### 模板1：TCP服务端（ServerCfg+TextLineCodec+收到消息回写+start）

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
package com.example.serv;

import net.ymate.platform.serv.IServerCfg;
import net.ymate.platform.serv.Servs;
import net.ymate.platform.serv.impl.DefaultServerCfg;
import net.ymate.platform.serv.nio.INioSession;
import net.ymate.platform.serv.nio.codec.TextLineCodec;
import net.ymate.platform.serv.nio.server.NioServer;
import net.ymate.platform.serv.nio.server.NioServerListener;

import java.io.IOException;

/**
 * TCP服务端示例-接收消息并原样回写
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
public class TcpServerDemo {

    /**
     * 启动TCP服务端
     *
     * @param args 命令行参数
     * @throws Exception 启动异常
     * @since 2.1.4-dev
     */
    public static void main(String[] args) throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("demoTcpServer")
                .serverHost("0.0.0.0")
                .port(8281)
                .build();
        NioServer nioServer = Servs.createServer(serverCfg, new TextLineCodec(), new NioServerListener() {
            @Override
            public void onSessionRegistered(INioSession session) throws IOException {
                System.out.println("Session registered: " + session);
            }

            @Override
            public void onSessionAccepted(INioSession session) throws IOException {
                super.onSessionAccepted(session);
                System.out.println("Session accepted: " + session);
            }

            @Override
            public void onBeforeSessionClosed(INioSession session) throws IOException {
                System.out.println("Session closing: " + session);
            }

            @Override
            public void onAfterSessionClosed(INioSession session) throws IOException {
                System.out.println("Session closed: " + session);
            }

            @Override
            public void onMessageReceived(Object message, INioSession session) throws IOException {
                System.out.println("Received: " + message + " from " + session);
                session.send("Hi, I received: " + message);
            }

            @Override
            public void onExceptionCaught(Throwable e, INioSession session) throws IOException {
                e.printStackTrace();
            }
        });
        nioServer.start();
        System.out.println("TCP Server started on port 8281...");
    }
}
```

### 模板2：TCP客户端（ClientCfg+DefaultReconnectServiceImpl+DefaultHeartbeatServiceImpl+NioClientListener+send）

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
package com.example.serv;

import net.ymate.platform.serv.IClientCfg;
import net.ymate.platform.serv.Servs;
import net.ymate.platform.serv.impl.DefaultClientCfg;
import net.ymate.platform.serv.impl.DefaultHeartbeatServiceImpl;
import net.ymate.platform.serv.impl.DefaultReconnectServiceImpl;
import net.ymate.platform.serv.nio.INioSession;
import net.ymate.platform.serv.nio.client.NioClient;
import net.ymate.platform.serv.nio.client.NioClientListener;
import net.ymate.platform.serv.nio.codec.TextLineCodec;
import net.ymate.platform.serv.IClient;

import java.io.IOException;
import java.util.Scanner;

/**
 * TCP客户端示例-带心跳+断线重连
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
public class TcpClientDemo extends NioClientListener {

    /**
     * 启动TCP客户端
     *
     * @param args 命令行参数
     * @throws Exception 连接异常
     * @since 2.1.4-dev
     */
    public static void main(String[] args) throws Exception {
        IClientCfg clientCfg = DefaultClientCfg.builder()
                .clientName("demoTcpClient")
                .remoteHost("127.0.0.1")
                .port(8281)
                .heartbeatInterval(30)
                .reconnectionInterval(5)
                .build();
        NioClient nioClient = Servs.createClient(
                clientCfg,
                new TextLineCodec(),
                new DefaultReconnectServiceImpl(),
                new DefaultHeartbeatServiceImpl(),
                new TcpClientDemo()
        );
        nioClient.connect();
        System.out.println("TCP Client connected, type messages (quit to exit):");
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                String line = scanner.nextLine();
                if ("quit".equalsIgnoreCase(line)) {
                    break;
                }
                nioClient.send(line);
            }
        }
        nioClient.close();
    }

    @Override
    public void onSessionRegistered(INioSession session) throws IOException {
        System.out.println("Session registered: " + session);
    }

    @Override
    public void onSessionConnected(INioSession session) throws IOException {
        super.onSessionConnected(session);
        System.out.println("Session connected: " + session);
    }

    @Override
    public void onClientReconnected(IClient<?, ?> client) {
        System.out.println("Client reconnected: " + client);
    }

    @Override
    public void onBeforeSessionClosed(INioSession session) throws IOException {
        System.out.println("Session closing: " + session);
    }

    @Override
    public void onAfterSessionClosed(INioSession session) throws IOException {
        System.out.println("Session closed: " + session);
    }

    @Override
    public void onMessageReceived(Object message, INioSession session) throws IOException {
        super.onMessageReceived(message, session);
        System.out.println("Received from server: " + message);
    }

    @Override
    public void onExceptionCaught(Throwable e, INioSession session) throws IOException {
        e.printStackTrace();
    }
}
```

## 5. 配置速查

### 5.1 配置文件最常改项（≤12条 key|默认值|说明）

| 配置项 | 默认值 | 说明 |
|---|---|---|
| IServerCfg.serverHost | 0.0.0.0 | 服务端绑定主机IP |
| IServerCfg.port | - | 服务端监听端口（必填） |
| IServerCfg.charset | UTF-8 | 编解码字符集 |
| IServerCfg.bufferSize | 4096 | 缓冲区大小 |
| IServerCfg.selectorCount | 1 | NIO选择器数量 |
| IServerCfg.executorCount | CPU核数 | 执行线程数量 |
| IClientCfg.remoteHost | - | 远程服务主机IP（必填） |
| IClientCfg.port | - | 远程服务端口（必填） |
| IClientCfg.connectionTimeout | 30 | 连接超时时间（秒） |
| IClientCfg.heartbeatInterval | 60 | 心跳发送间隔（秒），设0禁用 |
| IClientCfg.reconnectionInterval | 1 | 断线重连检测间隔（秒），设0禁用 |
| IClientCfg.charset | UTF-8 | 编解码字符集 |

### 5.2 注解配置核心参数

| 类/Builder参数 | 类型 | 说明 |
|---|---|---|
| DefaultServerCfg.builder().serverName(String) | String | 服务名称（标识用） |
| DefaultServerCfg.builder().keepAliveTime(long) | long | 空闲线程等待新任务最长时间(ms) |
| DefaultServerCfg.builder().threadMaxPoolSize(int) | int | 最大线程池大小，默认200 |
| DefaultServerCfg.builder().threadQueueSize(int) | int | 线程队列大小，默认1024 |
| DefaultServerCfg.builder().params(Map) | Map<String,String> | 自定义参数（如heartbeat_message） |
| DefaultClientCfg.builder().clientName(String) | String | 客户端名称（标识用） |
| DefaultClientCfg.builder().executorCount(int) | int | 执行线程数量，默认1 |
| DefaultClientCfg.builder().params(Map) | Map<String,String> | 自定义参数（heartbeat_message覆盖心跳内容） |

## 6. 常见坑点排查

| 现象 | 可能原因 | 排查/修复 |
|---|---|---|
| 客户端心跳丢失/频繁断线 | heartbeatInterval设置过大/过小；网络不稳定 | 检查心跳间隔是否合理(建议10-60s)；确认服务端无主动超时；params.heartbeat_message自定义内容与服务端一致 |
| 断线重连不生效或重连风暴 | reconnectionInterval=0(禁用)；间隔太小(如<1s)；未传入DefaultReconnectServiceImpl实例 | 检查createClient是否传入了非null的IReconnectService；间隔建议≥5s；重连成功后配合onClientReconnected事件记录日志 |
| 编解码不一致导致半包/粘包 | 服务端与客户端Codec类型不同；自定义Codec未正确处理长度头 | 确认两端使用相同Codec(ByteArrayCodec/NioStringCodec/TextLineCodec)；TextLineCodec要求消息必须以\r\n结尾；自定义Codec需严格实现encode/decode处理TCP流 |
| 会话send抛出ClosedChannelException | 会话已被关闭/空闲超时被SessionManager移除 | send前检查session状态；配合NioSessionManager.idleChecker()调整空闲时间；捕获IOException并重试 |
| UDP消息收不到 | 未绑定正确端口；防火墙拦截；TextLineCodec缺\r\n | UDP客户端用nc -u测试连通性；确认TextLineCodec发送时追加\r\n；检查params自定义参数是否覆盖默认配置 |
