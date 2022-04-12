---
sidebar_position: 7
slug: serv
---

# 服务（Serv）

服务（Serv）是一套基于 NIO 实现的通讯服务框架，提供 TCP、UDP 协议的客户端与服务端封装，灵活的消息监听与消息内容编/解码，简约的配置使二次开发更加便捷。同时针对客户端提供默认的断线重连、链路维护（心跳）等服务支持，您只需了解业务即可轻松完成开发工作。

## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-serv</artifactId>
    <version>2.1.1</version>
</dependency>
```




## 基础概念

### 会话（Session）

用于客户端与服务端之间连接状态的维护和消息发送的对象。

### 编/解码器（Codec）

目前提供以下三种编/解码器，开发者可通过实现 `ICodec` 接口自行扩展。

- ByteArrayCodec：采用字节 `byte[4]` 作为消息头，用于记录消息体长度字节数组消息编/解码器。
- NioStringCodec：通过继承 `ByteArrayCodec` 类实现的字符串消息编/解码器。
- TextLineCodec：用于解析以回车换行符 `\r\n` 做为消息结束标志的字符串消息的编/解码器。

### 内置服务（Service）

目前提供以下两种内置服务，更多服务在不断完善中...

- IHeartbeatService：内置链路维护（心跳）服务，该服务将在与服务端成功建立连接后按参数配置的时间间隔向服务端发送心跳消息（心跳消息内容默认为 `0` 字符，心跳消息内容可以通过自定义参数 `heartbeat_message` 设置）。

- IReconnectService：内置断线重连服务，当服务的连接状态异常时将尝试重新与服务端建立连接。



## 服务端（Server）



### 配置参数说明

|配置项|描述|
|---|---|
| serverName        | 服务名称                                                     |
| serverHost        | 主机名称或IP地址                                             |
| port              | 服务监听端口                                                 |
| charset           | 编解码字符集，默认为 `UTF-8`                                 |
| bufferSize        | 缓冲区大小，默认为 `4096`                                    |
| executorCount     | 执行线程数量，默认为 `Runtime.getRuntime().availableProcessors()` |
| keepAliveTime     | 空闲线程等待新任务的最长时间，默认为 `0`                     |
| threadMaxPoolSize | 最大线程池大小，默认为 `200`                                 |
| threadQueueSize   | 线程队列大小，默认为 `1024`                                  |
| selectorCount     | 选择器数量，默认为 `1`                                       |
| params            | 自定义参数映射                                               |



### 配置示例代码

```java
IServerCfg serverCfg = DefaultServerCfg.builder()
        .serverName("demoServer")
        .serverHost("0.0.0.0")
        .port(8281)
        .build();
```



### 事件监听器

基于 TCP 协议的服务端，需要继承 `NioServerListener` 监听器类，支持监听如下事件：

|事件|说明|
|---|---|
|onSessionRegistered|客户端会话注册成功后触发该事件|
|onSessionAccepted|客户端成功接入服务端后触发该事件|
|onBeforeSessionClosed|客户端会话被关闭之前触发该事件|
|onAfterSessionClosed|客户端会话被关闭之后触发该事件|
|onMessageReceived|收到客户端发送的消息时触发该事件|
|onExceptionCaught|出现异常时触发该事件|

基于 UDP 协议的服务端，需要继承 `AbstractNioUdpListener` 监听器类，支持监听如下事件：

|事件|说明|
|---|---|
|onSessionReady|客户端与服务端连接已建立并准备就绪时触发该事件|
|onMessageReceived|收到客户端发送的消息时触发该事件|
|onExceptionCaught|出现异常时触发该事件|



### 示例代码



#### TCP服务端

```java
public class TcpServer {

    public static void main(String[] args) throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("tcpServer")
                .serverHost("0.0.0.0")
                .port(8281)
                .build();
        NioServer nioServer = Servs.createServer(serverCfg, new TextLineCodec(), new NioServerListener() {
            @Override
            public void onSessionRegistered(INioSession session) throws IOException {
                System.out.println("Session registered: " + session);
            }

            @Override
            public void onClientReconnected(IClient<?, ?> client) {
                System.out.println("Client reconnected: " + client);
            }

            @Override
            public void onExceptionCaught(Throwable e, INioSession session) throws IOException {
                super.onExceptionCaught(e, session);
            }

            @Override
            public void onSessionAccepted(INioSession session) throws IOException {
                super.onSessionAccepted(session);
                System.out.println("Session accepted: " + session);
            }

            @Override
            public void onMessageReceived(Object message, INioSession session) throws IOException {
                session.send("Hi, guys! I received a message: " + message);
            }

            @Override
            public void onAfterSessionClosed(INioSession session) throws IOException {
                System.out.println("Session closed: " + session);
            }

            @Override
            public void onBeforeSessionClosed(INioSession session) throws IOException {
                System.out.println("Session closing: " + session);
            }
        });
        nioServer.start();
    }
}
```

**测试：**

```shell
# 通过终端执行nc命令与服务端连接
nc 0.0.0.0 8281
# 连接成功后，在控制台输入文字并按回车键
Hello
# 将收到服务端响应
Hi, guys! I received a message: Hello
```



#### UDP服务端

```java
public class UdpServer {

    public static void main(String[] args) throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("udpServer")
                .serverHost("0.0.0.0")
                .port(8281)
                .build();
        NioUdpServer nioUdpServer = Servs.createUdpServer(serverCfg, new TextLineCodec(), new AbstractNioUdpListener() {

            @Override
            public Object onMessageReceived(InetSocketAddress sourceAddress, Object message) throws IOException {
                return "Hi, guys! I received a message: " + message + ", from " + sourceAddress;
            }

            @Override
            public void onExceptionCaught(InetSocketAddress sourceAddress, Throwable e) throws IOException {
                System.out.println(sourceAddress + "--->" + e);
            }
        });
        nioUdpServer.start();
    }
}
```

**测试：**

```shell
# 通过终端执行nc命令向服务端发送一条消息
echo -n "Hello\r\n" | nc -4u -w1 0.0.0.0 8281
# 将收到服务端响应
Hi, guys! I received a message: Hello, from /127.0.0.1:53653
```



## 客户端（Client）



### 配置参数说明

| 配置项               | 描述                               |
| -------------------- | ---------------------------------- |
| clientName           | 客户端名称                         |
| remoteHost           | 远程主机名称或IP地址               |
| port                 | 远程服务监听端口                   |
| charset              | 编解码字符集，默认为`UTF-8`        |
| executorCount        | 执行线程数量，默认为`1`            |
| connectionTimeout    | 连接超时时间（秒）， 默认为`30`    |
| bufferSize           | 最大线程池大小，默认为`200`        |
| reconnectionInterval | 断线重连检测间隔（秒），默认为`1`  |
| heartbeatInterval    | 心跳发送时间间隔（秒），默认为`60` |
| params               | 自定义参数映射                     |



### 配置示例代码

```java
IClientCfg clientCfg = DefaultClientCfg.builder()
        .clientName("demoClient")
        .remoteHost("0.0.0.0")
        .port(8281)
        .build();
```



### 事件监听器

基于 TCP 协议的客户端，需要继承 `NioClientListener` 监听器类，支持监听如下事件：

|事件|说明|
|---|---|
|onSessionRegistered|客户端会话注册成功后触发该事件|
|onSessionConnected|客户端成功接入服务端后触发该事件|
|onBeforeSessionClosed|客户端会话被关闭之前触发该事件|
|onAfterSessionClosed|客户端会话被关闭之后触发该事件|
|onMessageReceived|收到服务端发送的消息时触发该事件|
|onClientReconnected|客户端断线重连成功后触发该事件事件|
|onExceptionCaught|出现异常时触发该事件|

基于 UDP 协议的客户端，需要继承 `NioUdpListener` 监听器类，支持监听如下事件：

|事件|说明|
|---|---|
|onSessionReady|客户端与服务端连接已建立并准备就绪时触发该事件|
|onMessageReceived|收到服务端发送的消息时触发该事件|
|onExceptionCaught|出现异常时触发该事件|



### 示例代码



#### TCP客户端

```java
public class TcpClientListener extends NioClientListener {

    private static final Log LOG = LogFactory.getLog(TcpClientListener.class);

    public static void main(String[] args) throws Exception {
        IClientCfg clientCfg = DefaultClientCfg.builder()
                .clientName("tcpClient")
                .remoteHost("0.0.0.0")
                .port(8281)
                .build();
        NioClient nioClient = Servs.createClient(clientCfg, new TextLineCodec(), new DefaultReconnectServiceImpl(), new DefaultHeartbeatServiceImpl(), new TcpClientListener());
        nioClient.connect();
    }

    @Override
    public void onClientReconnected(IClient<?, ?> client) {
        LOG.info("onClientReconnected: " + client);
    }

    @Override
    public void onSessionRegistered(INioSession session) throws IOException {
        LOG.info("onSessionRegistered: " + session);
    }

    @Override
    public void onSessionConnected(INioSession session) throws IOException {
        super.onSessionConnected(session);
        LOG.info("onSessionConnected: " + session);
    }

    @Override
    public void onBeforeSessionClosed(INioSession session) throws IOException {
        LOG.info("onBeforeSessionClosed: " + session);
    }

    @Override
    public void onAfterSessionClosed(INioSession session) throws IOException {
        LOG.info("onAfterSessionClosed: " + session);
    }

    @Override
    public void onMessageReceived(Object message, INioSession session) throws IOException {
        super.onMessageReceived(message, session);
        LOG.info("onMessageReceived: " + message + " --> " + session);
    }
}
```



#### UDP客户端

```java
public class UdpClientListener extends AbstractNioUdpListener {

    private static final Log LOG = LogFactory.getLog(UdpClientListener.class);

    public static void main(String[] args) throws Exception {
        IClientCfg clientCfg = DefaultClientCfg.builder()
                .clientName("udpClient")
                .remoteHost("0.0.0.0")
                .port(8281)
                .build();
        NioUdpClient nioUdpClient = Servs.createUdpClient(clientCfg, new TextLineCodec(), new DefaultHeartbeatServiceImpl(), new UdpClientListener());
        nioUdpClient.connect();
    }

    @Override
    public Object onSessionReady() throws IOException {
        return "Hello!";
    }

    @Override
    public Object onMessageReceived(InetSocketAddress sourceAddress, Object message) throws IOException {
        LOG.info("onMessageReceived: " + message + ", from " + sourceAddress);
        return null;
    }

    @Override
    public void onExceptionCaught(InetSocketAddress sourceAddress, Throwable e) throws IOException {
        System.out.println(sourceAddress + "--->" + e);
    }
}
```



## 会话管理器（SessionManager）

会话管理器的作用是帮助 TCP、UDP 服务端管理已连接的客户端会话，目前主要功能包括：

- 空闲会话检查：当会话在设定的时间内与服务器之间无任何通讯时，此会话将被关闭并从会话管理器中移除。

- 流量速度统计：通过记录客户端与服务端之间消息收发数量，计算消息处理的实时速度、平均速度、最大及最小速度值。

- 向客户端主动发送消息：通过调用会话管理器实例对象方法，可以根据业务需要主动向指定会话发送消息。

- 移除客户端会话：通过调用会话管理器实例对象方法，可以将指定标识的会话关闭并将其移除。



### TCP会话管理器

```java
public class TcpSessionListener implements INioSessionListener<NioSessionWrapper, String> {

    private static final Log LOG = LogFactory.getLog(TcpSessionListener.class);

    public static void main(String[] args) throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("tcpServer")
                .serverHost("localhost")
                .port(8281)
                .keepAliveTime(60000).build();
        // 通过会话管理器创建服务端并设置会话空闲时间为30秒
        NioSessionManager<NioSessionWrapper, String> sessionManager = new NioSessionManager<>(serverCfg, new TextLineCodec(), new TcpSessionListener(), 30000L);
        // 设置空闲会话检查服务
        sessionManager.idleChecker(new DefaultSessionIdleChecker<>());
        // 设置并启动流量速度计数器
        Speedometer speedometer = new Speedometer("tcpServerSpeedometer").interval(10000);
        // 注：此处为自定义流量速度监听，若仅日志输出以下两行代码可忽略
        Speedometer.IListener speedListener = new DefaultSpeedListener(speedometer);
        speedometer.start(speedListener);
        //
        sessionManager.speedometer(speedometer);
        // 初始化并启动服务
        sessionManager.initialize();

        // -------------------

        // 遍历会话并向其发送消息
        for (NioSessionWrapper sessionWrapper : sessionManager.sessionWrappers()) {
            sessionManager.sendTo(sessionWrapper.getId(), "Send message from server.");
        }
        // 获取当前会话总数
        LOG.info("Current session count: " + sessionManager.sessionCount());
        // 将已连接的客户端会话从管理器中移除
        for (NioSessionWrapper sessionWrapper : sessionManager.sessionWrappers()) {
            sessionManager.closeSessionWrapper(sessionWrapper);
        }
        // 销毁会话管理器
        sessionManager.close();
    }

    @Override
    public void onSessionRegistered(NioSessionWrapper session) throws IOException {
        LOG.info("onSessionRegistered: " + session.getId());
    }

    @Override
    public void onSessionAccepted(NioSessionWrapper session) throws IOException {
        LOG.info("onSessionAccepted: " + session.getId());
    }

    @Override
    public void onBeforeSessionClosed(NioSessionWrapper session) throws IOException {
        LOG.info("onBeforeSessionClosed: " + session.getId());
    }

    @Override
    public void onAfterSessionClosed(NioSessionWrapper session) throws IOException {
        LOG.info("onAfterSessionClosed: " + session.getId());
    }

    @Override
    public void onMessageReceived(String message, NioSessionWrapper session) throws IOException {
        LOG.info("onMessageReceived: " + message + " from " + session.getId());
    }

    @Override
    public void onExceptionCaught(Throwable e, NioSessionWrapper session) throws IOException {
        LOG.info("onExceptionCaught: " + e.getMessage() + " -- " + session.getId());
    }

    @Override
    public void onSessionIdleRemoved(NioSessionWrapper sessionWrapper) {
        LOG.info("onSessionIdleRemoved: " + sessionWrapper.getId());
    }
}
```



### UDP会话管理器

```java
public class UdpSessionListener implements INioUdpSessionListener<NioUdpSessionWrapper, String> {

    private static final Log LOG = LogFactory.getLog(UdpSessionListener.class);

    public static void main(String[] args) throws Exception {
        IServerCfg serverCfg = DefaultServerCfg.builder()
                .serverName("udpServer")
                .serverHost("localhost")
                .port(8281)
                .keepAliveTime(60000).build();
        // 通过会话管理器创建服务端并设置会话空闲时间为30秒
        NioUdpSessionManager<NioUdpSessionWrapper, String> sessionManager = new NioUdpSessionManager<>(serverCfg, new TextLineCodec(), new UdpSessionListener(), 30000L);
        // 设置空闲会话检查服务
        sessionManager.idleChecker(new DefaultSessionIdleChecker<>());
        // 设置流量速度计数器
        sessionManager.speedometer(new Speedometer("udpServerSpeedometer"));
        // 初始化并启动服务
        sessionManager.initialize();

        // -------------------

        // 遍历会话并向其发送消息
        sessionManager.sessionWrappers().forEach(nioUdpSessionWrapper -> {
            try {
                sessionManager.sendTo(nioUdpSessionWrapper.getId(), "Send message from server.");
            } catch (IOException e) {
                LOG.warn(e.getMessage(), RuntimeUtils.unwrapThrow(e));
            }
        });
        // 当前会话总数
        LOG.info("Current session count: " + sessionManager.sessionCount());
        // 将已连接的客户端会话从管理器中移除
        sessionManager.sessionWrappers().forEach(sessionManager::closeSessionWrapper);
        // 销毁会话管理器
        sessionManager.close();
    }

    @Override
    public Object onMessageReceived(NioUdpSessionWrapper sessionWrapper, String message) throws IOException {
        LOG.info("onMessageReceived: " + message + " from " + sessionWrapper.getId());
        // 当收到消息后，可以直接向客户端回复消息
        return "Hi, " + sessionWrapper.getId();
    }

    @Override
    public void onExceptionCaught(NioUdpSessionWrapper sessionWrapper, Throwable e) throws IOException {
        LOG.info("onExceptionCaught: " + e.getMessage() + " -- " + sessionWrapper.getId());
    }

    @Override
    public void onSessionIdleRemoved(NioUdpSessionWrapper sessionWrapper) {
        LOG.info("onSessionIdleRemoved: " + sessionWrapper.getId());
    }
}
```

