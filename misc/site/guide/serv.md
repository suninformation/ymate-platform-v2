---
sidebarDepth: 2
---

# 服务（Serv）

服务模块（Serv）是一套基于NIO实现的通讯服务框架，提供TCP、UDP协议的客户端与服务端封装，灵活的消息监听与消息内容编/解码，简约的配置使二次开发更加便捷；
同时默认提供断线重连、链路维护(心跳)等服务支持，您只需了解业务即可轻松完成开发工作。

## Maven包依赖

    <dependency>
        <groupId>net.ymate.platform</groupId>
        <artifactId>ymate-platform-serv</artifactId>
        <version><VERSION></version>
    </dependency>

> **注**：在项目的pom.xml中添加上述配置，该模块已经默认引入核心包依赖，无需重复配置。


## 基础概念

### 会话（Session）

> 用于客户端与服务端之间连接状态的维护和消息发送的对象；

### 编/解码器（Codec）

> 目前提供以下两种编/解码器，开发者可通过实现ICodec接口自行扩展；

> - NioStringCodec：采用字节`byte[4]`作为消息头，用于记录消息体长度的字符串消息编/解码器；

> - TextLineCodec：用于解析以回车换行符(`\r\n`)做为消息结束标志的字符串消息的编/解码器；

### 内置服务（Service）

> 目前提供以下两种内置服务，更多服务在不断完善中...；

> - IHeartbeatService：内置链路维护(心跳)服务，该服务将在与服务端成功建立连接后按参数配置的时间间隔向服务端发送心跳消息（心跳消息内容默认为0字符，心跳消息内容可以通过自定义参数heartbeat_message设置）；

> - IReconnectService：内置断线重连服务，当服务的连接状态异常时将尝试重新与服务端建立连接；

## 通过代码手工初始化模块示例

    // 创建YMP实例
    YMP owner = new YMP(ConfigBuilder.create(
            // 设置服务模块配置
            ModuleCfgProcessBuilder.create().putModuleCfg(
                    ServModuleConfigurable.create()
                            // 添加服务端配置
                            .addServer(ServServerConfigurable.create("default").serverHost("localhost").port(8281))
                            // 添加客户端配置
                            .addClient(ServClientConfigurable.create("default")
                                    .remoteHost("localhost")
                                    .port(8281)
                                    .connectionTimeout(60)
                                    .heartbeatInterval(30).reconnectionInterval(1))).build())
            .proxyFactory(new DefaultProxyFactory())
            .developMode(true)
            .runEnv(IConfig.Environment.PRODUCT).build());
    // 向容器注册模块
    owner.registerModule(Servs.class);
    // 执行框架初始化
    owner.init();

## 服务端（Server）

服务端初始化参数：

    #-------------------------------------
    # 服务模块--服务端初始化参数
    #-------------------------------------

    # 服务端配置列表，多个服务端名称间用'|'分隔，默认为default
    ymp.configs.serv.server.name_list=default
    
    # 绑定IP地址, 默认为0.0.0.0
    ymp.configs.serv.server.default.host=0.0.0.0
    
    # 监听端口号, 默认为8281
    ymp.configs.serv.server.default.port=8281
    
    # 编解码字符集, 默认为UTF-8
    ymp.configs.serv.server.default.charset=UTF-8
    
    # 缓冲区大小, 默认为4096
    ymp.configs.serv.server.default.buffer_size=4096
    
    # NIO选择器数量, 默认为1
    ymp.configs.serv.server.default.selector_count=1
    
    # 执行线程池大小, 默认为 Runtime.getRuntime().availableProcessors()
    ymp.configs.serv.server.default.executor_count=10
    
    # 空闲线程等待新任务的最长时间, 默认为 0
    ymp.configs.serv.server.default.keep_alive_time=0
    
    # 最大线程池大小，默认为 200
    ymp.configs.serv.server.default.thread_max_pool_size=200
    
    # 线程队列大小，默认为 1024
    ymp.configs.serv.server.default.thread_queue_size=1024
    
    # 自定义参数, 可选
    ymp.configs.serv.server.default.params.xxx=xxx

通过在监听器实现类声明`@Server`注解来表示一个服务端，该注解有如下参数：

|参数|说明|
|---|---|
|name|设置服务的名称，Serv框架将会根据该参数指定的名称加载对应的服务端参数配置，默认为default；|
|codec|设置编解码器，默认为NioStringCodec；|
|implClass|服务端实现类，默认为NioServer；|

基于TCP协议的服务端，需要继承NioServerListener监听器类，支持监听如下事件：

|事件|说明|
|---|---|
|onSessionAccepted|客户端成功接入服务端后触发该事件；|
|onBeforeSessionClosed|客户端会话被关闭之前触发该事件；|
|onAfterSessionClosed|客户端会话被关闭之后触发该事件；|
|onMessageReceived|收到客户端发送的消息时触发该事件；|
|onExceptionCaught|出现异常时触发该事件；|

基于UDP协议的服务端，需要继承NioUdpListener监听器类，支持监听如下事件：

|事件|说明|
|---|---|
|onSessionReady|客户端与服务端连接已建立并准备就绪时触发该事件；|
|onMessageReceived|收到客户端发送的消息时触发该事件；|
|onExceptionCaught|出现异常时触发该事件；|

### 示例代码

#### TCP服务端

	// 采用默认配置的TCP服务端
	@Server
	public class TcpServer extends NioServerListener {
		@Override
		public void onSessionAccepted(INioSession session) throws IOException {
		    super.onSessionAccepted(session);
		}
		
		@Override
		public void onMessageReceived(Object message, INioSession session) throws IOException {
		    super.onMessageReceived(message, session);
		    // 输出接收到的消息
		    System.out.println("Message received: " + message);
		    // 向客户端发送消息
		    session.send("Hi, guys!");
		}
		
		@Override
		public void onAfterSessionClosed(INioSession session) throws IOException {
		    super.onAfterSessionClosed(session);
		}
		
		@Override
		public void onBeforeSessionClosed(INioSession session) throws IOException {
		    super.onBeforeSessionClosed(session);
		}
	}

#### UDP服务端

    // 采用默认配置的UDP服务端，其中implClass参数必须指定为NioUpdServer.class
    @Server(implClass = NioUdpServer.class, codec = TextLineCodec.class)
    public class UdpServer extends NioUdpListener {

        public Object onSessionReady() throws IOException {
            // 此接口方法的返回值将作为消息发送至客户端
            return null;
        }

        public Object onMessageReceived(InetSocketAddress sourceAddr, Object message) throws IOException {
            // 输出接收到的消息
            System.out.println("Message received: " + message);
            // 此接口方法的返回值将作为消息发送至客户端
            return message;
        }

        public void onExceptionCaught(InetSocketAddress sourceAddr, Throwable e) throws IOException {
            System.out.println(sourceAddr + "--->" + e);
        }
    }

## 客户端（Client）：

客户端初始化参数：

    #-------------------------------------
    # 服务模块--客户端初始化参数
    #-------------------------------------

    # 客户端配置列表，多个客户端名称间用'|'分隔，默认为default
    ymp.configs.serv.client.name_list=default
    
    # 远程主机IP地址, 默认为0.0.0.0
    ymp.configs.serv.client.default.host=0.0.0.0
    
    # 远程主机端口号, 默认为8281
    ymp.configs.serv.client.default.port=8281
    
    # 编解码字符集, 默认为UTF-8
    ymp.configs.serv.client.default.charset=UTF-8
    
    # 缓冲区大小, 默认为4096
    ymp.configs.serv.client.default.buffer_size=4096
    
    # 执行线程池大小, 默认为 Runtime.getRuntime().availableProcessors()
    ymp.configs.serv.client.default.executor_count=10
    
    # 连接超时时间(秒), 默认为30
    ymp.configs.serv.client.default.connection_timeout=30
    
    # 断线重连检测间隔(秒), 默认为1
    ymp.configs.serv.client.default.reconnection_interval=1
    
    # 心跳发送时间间隔(秒), 默认为60
    ymp.configs.serv.client.default.heartbeat_interval=60
    
    # 自定义参数, 可选
    ymp.configs.serv.client.default.params.xxx=xxx

通过在监听器实现类声明@Client注解来表示一个客户端，该注解有如下参数：

|事件|说明|
|---|---|
|name|设置客户端名称，Serv框架将会根据该参数指定的名称加载对应的客户端参数配置，默认为default；|
|codec|设置编解码器，默认为NioStringCodec；|
|implClass|客户端实现类，默认为NioClient；|
|reconnectClass|短线重连服务实现类，默认为NONE；|
|heartbeatClass|链路维护(心跳)服务实现类，默认为NONE；|

基于TCP协议的客户端，需要继承NioClientListener监听器类，支持监听如下事件：

|事件|说明|
|---|---|
|onSessionConnected|客户端成功接入服务端后触发该事件；|
|onBeforeSessionClosed|客户端会话被关闭之前触发该事件；|
|onAfterSessionClosed|客户端会话被关闭之后触发该事件；|
|onMessageReceived|收到服务端发送的消息时触发该事件；|
|onExceptionCaught|出现异常时触发该事件；|

基于UDP协议的客户端，需要继承NioUdpListener监听器类，支持监听如下事件：

|事件|说明|
|---|---|
|onSessionReady|客户端与服务端连接已建立并准备就绪时触发该事件；|
|onMessageReceived|收到服务端发送的消息时触发该事件；|
|onExceptionCaught|出现异常时触发该事件；|

### 示例代码

#### TCP客户端

    @Client(reconnectClass = DefaultReconnectService.class,
            heartbeatClass = DefaultHeartbeatService.class, codec = TextLineCodec.class)
    public class TcpClient extends NioClientListener {

        @Override
        public void onSessionConnected(INioSession session) throws IOException {
            super.onSessionConnected(session);
            //
            session.send("Hello from client.");
        }

        @Override
        public void onMessageReceived(Object message, INioSession session) throws IOException {
            super.onMessageReceived(message, session);
            //
            System.out.println(session + "--->" + message);
        }

        @Override
        public void onExceptionCaught(Throwable e, INioSession session) throws IOException {
            System.out.println(session + "--->" + e);
        }
    }

##### UDP客户端

    @Client(implClass = NioUdpClient.class, codec = TextLineCodec.class)
    public class UdpClient extends NioUdpListener {

        public Object onSessionReady() throws IOException {
            return "Hello from client.";
        }

        public Object onMessageReceived(InetSocketAddress sourceAddr, Object message) throws IOException {
            System.out.println(sourceAddr + "--->" + message);
            return null;
        }

        public void onExceptionCaught(InetSocketAddress sourceAddr, Throwable e) throws IOException {
            System.out.println(sourceAddr + "--->" + e);
        }
    }

## 客户端和服务端对象的使用

YMP框架启动时将自动扫描并加载声明了`@Server`和`@Client`注解的类，并根据注解设置和对应的参数配置进行客户端或服务端对象的初始化，但此时的客户端和服务端程序并没有直正执行，需要手动完成启动动作，代码如下：

- 示例一：启动所有已加载的客户端、服务端服务

        public static void main(String[] args) throws Exception {
            YMP.get().init();
            //
            Servs.get().startup();
        }

- 示例二：获取指定的客户端或服务端服务，启动服务并向服务端发送消息

        public static void main(String[] args) throws Exception {
            YMP.get().init();

            // 获取服务端实例对象
            NioUdpServer _serv = Servs.get().getServer(UdpServer.class);
            // 启动服务
            _serv.start();

            // 获取客户端实例对象
            NioUdpClient _c = Servs.get().getClient(UdpClient.class);
            // 连接到远程服务
            _c.connect();
            // 通过客户端对象向服务端发送消息
            _c.send("Message from Client.");
        }

## 通过代码创建并配置客户端和服务端对象

上述内容主要说明如果基于框架配置文件初始化、启动和调用TCP、UDP服务端与客户端对象，下面阐述的是通过手工编码方式完成客户端或服务端的配置、启动和调用过程，此方法不需要配置文件支持：

### 示例代码

假设，我们通过手工方法创建YMP实例对象，注册`Serv`模块(也可以直接通过`YMP.get()`获取实例)并完成初始化，代码如下：

    YMP owner = new YMP(ConfigBuilder.create()
            .proxyFactory(new NoOpProxyFactory())
            .beanLoader(new AbstractBeanLoader() {
                @Override
                public void load(IBeanFactory beanFactory, IBeanFilter filter) throws Exception {
                    // Nothing...
                }
            }).developMode(true).runEnv(IConfig.Environment.PRODUCT).build());
     owner.registerModule(new Servs());
     owner.init();

- 服务端

        NioServer server = Servs.get().buildServer(DefaultServerCfg.create().serverHost("localhost").port(8281).build(), new TextLineCodec(), new NioServerListener() {
            @Override
            public void onSessionAccepted(INioSession session) throws IOException {
                super.onSessionAccepted(session);
                //
                System.out.println("Session accepted: " + session);
            }
        
            @Override
            public void onMessageReceived(Object message, INioSession session) throws IOException {
                // 输出接收到的消息
                System.out.println("Message received: " + message);
                // 向客户端发送消息
                session.send("Hi, guys!");
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
        server.start();

- 客户端

        IClient client = Servs.get(owner)
                .buildClient(DefaultClientCfg.create()
                        .remoteHost("localhost").port(8281).build(), new TextLineCodec(), new DefaultReconnectService(), new DefaultHeartbeatService(), new NioClientListener() {
            @Override
            public void onSessionConnected(INioSession session) throws IOException {
                super.onSessionConnected(session);
                //
                session.send("Hello!");
            }
        
            @Override
            public void onMessageReceived(Object message, INioSession session) throws IOException {
                super.onMessageReceived(message, session);
                //
                System.out.println(session + "--->" + message);
            }
        
            @Override
            public void onExceptionCaught(Throwable e, INioSession session) throws IOException {
                System.out.println(session + "--->" + e);
            }
            
        });
        client.connect();

## 会话管理器

会话管理器的作用是帮助TCP、UDP服务端管理已连接的客户端会话，目前主要功能包括：

- 空闲会话检查：当会话在设定的时间内与服务器之间无任何通讯时，此会话将被关闭并从会话管理器中移除；

- 流量速度统计：通过记录客户端与服务端的消息收发数量，能够计算出消息处理的实时速度、平均速度、最大及最小速度值；

- 向客户端主动发送消息：通过调用会话管理器实例对象方法，可以根据业务需要主动向指定会话发送消息；

- 移除客户端会话：通过调用会话管理器实例对象方法，可以将指定标识的会话关闭并将其移除；


### 示例程序：

- TCP会话管理器示例：

    ```
    public class TcpSessionListener implements INioSessionListener<NioSessionWrapper, String> {
    
        private static final Log _LOG = LogFactory.getLog(TcpSessionListener.class);
    
        public static void main(String[] args) throws Exception {
            // 初始化YMP框架
            YMP.get().init();
            // 创建服务端配置
            IServerCfg _serverCfg = DefaultServerCfg.create()
                    .selectorCount(10)
                    .serverHost("localhost")
                    .port(8281)
                    .keepAliveTime(60000).build();
            // 通过会话管理器创建服务 (设置会话空闲时间为30秒)
            NioSessionManager<NioSessionWrapper, String> _manager = new NioSessionManager<NioSessionWrapper, String>(_serverCfg, new NioStringCodec(), new TcpSessionListener(), 30000L);
            // 设置空闲会话检查服务
            _manager.idleChecker(new DefaultSessionIdleChecker<NioSessionWrapper, String, String>());
            // 设置流量速度计数器
            _manager.speedometer(new Speedometer());
            // 初始化并启动服务
            _manager.init(Servs.get());
    
            // -------------------
    
            // 遍历会话并向其发送消息
            for (NioSessionWrapper _session : _manager.sessionWrappers()) {
                _manager.sendTo(_session.getId(), "Send message from server.");
            }
            // 当前会话总数
            System.out.println("Current session count: " + _manager.sessionCount());
            // 将已连接的客户端会话从管理器中移除
            for (NioSessionWrapper _session : _manager.sessionWrappers()) {
                _manager.closeSessionWrapper(_session);
            }
            // 销毁会话管理器
            _manager.destroy();
        }
    
        @Override
        public void onSessionRegistered(NioSessionWrapper session) throws IOException {
            _LOG.info("onSessionRegistered: " + session.getId());
        }
    
        @Override
        public void onSessionAccepted(NioSessionWrapper session) throws IOException {
            _LOG.info("onSessionAccepted: " + session.getId());
        }
    
        @Override
        public void onBeforeSessionClosed(NioSessionWrapper session) throws IOException {
            _LOG.info("onBeforeSessionClosed: " + session.getId());
        }
    
        @Override
        public void onAfterSessionClosed(NioSessionWrapper session) throws IOException {
            _LOG.info("onAfterSessionClosed: " + session.getId());
        }
    
        @Override
        public void onMessageReceived(String message, NioSessionWrapper session) throws IOException {
            _LOG.info("onMessageReceived: " + message + " from " + session.getId());
        }
    
        @Override
        public void onExceptionCaught(Throwable e, NioSessionWrapper session) throws IOException {
            _LOG.info("onExceptionCaught: " + e.getMessage() + " -- " + session.getId());
        }
    
        @Override
        public void onSessionIdleRemoved(NioSessionWrapper sessionWrapper) {
            _LOG.info("onSessionIdleRemoved: " + sessionWrapper.getId());
        }
    }
    ```

- UDP会话管理器示例：

    ```
    public class UdpSessionListener implements INioUdpSessionListener<NioUdpSessionWrapper, String> {
    
        private static final Log _LOG = LogFactory.getLog(UdpSessionListener.class);
    
        public static void main(String[] args) throws Exception {
            // 初始化YMP框架
            YMP.get().init();
            // 创建服务端配置
            IServerCfg _serverCfg = DefaultServerCfg.create()
                    .selectorCount(10)
                    .serverHost("localhost")
                    .port(8281).build();
            // 通过会话管理器创建服务 (设置会话空闲时间为30秒)
            NioUdpSessionManager<NioUdpSessionWrapper, String> _manager = new NioUdpSessionManager<NioUdpSessionWrapper, String>(_serverCfg, new NioStringCodec(), new UdpSessionListener(), 30000L);
            // 设置空闲会话检查服务
            _manager.idleChecker(new DefaultSessionIdleChecker<NioUdpSessionWrapper, InetSocketAddress, String>());
            // 设置流量速度计数器
            _manager.speedometer(new Speedometer());
            // 初始化并启动服务
            _manager.init(Servs.get());
    
            // -------------------
    
            // 遍历会话并向其发送消息
            for (NioUdpSessionWrapper _session : _manager.sessionWrappers()) {
                _manager.sendTo(_session.getId(), "Send message from server.");
            }
            // 当前会话总数
            System.out.println("Current session count: " + _manager.sessionCount());
            // 将已连接的客户端会话从管理器中移除
            for (NioUdpSessionWrapper _session : _manager.sessionWrappers()) {
                _manager.closeSessionWrapper(_session);
            }
            // 销毁会话管理器
            _manager.destroy();
        }
    
        @Override
        public Object onMessageReceived(NioUdpSessionWrapper sessionWrapper, String message) throws IOException {
            _LOG.info("onMessageReceived: " + message + " from " + sessionWrapper.getId());
            // 当收到消息后，可以直接向客户端回复消息
            return "Hi, " + sessionWrapper.getId();
        }
    
        @Override
        public void onExceptionCaught(NioUdpSessionWrapper sessionWrapper, Throwable e) throws IOException {
            _LOG.info("onExceptionCaught: " + e.getMessage() + " -- " + sessionWrapper.getId());
        }
    
        @Override
        public void onSessionIdleRemoved(NioUdpSessionWrapper sessionWrapper) {
            _LOG.info("onSessionIdleRemoved: " + sessionWrapper.getId());
        }
    }
    ```

> **注意**：
>
> - 通过手工编码方式创建的服务端或客户端实例对象将不被框架管理，需要开发者手动调用关闭方法(如：`server.close()`或`client.close()`)来释放资源。
> - YMP框架初始化后，若使用`try...finally`执行`YMP.get().destroy()`销毁动作，则服务启动后将立即被停止。
