---
sidebarDepth: 2
---

# Redis

基于Jedis驱动封装，以JDBC模块的设计思想进行简单封装，采用会话机制，简化订阅(`subscribe`)和发布(`publish`)处理，支持多数据源及连接池配置，支持`jedis`、`shard`、`sentinel`和`cluster`等数据源连接方式；

## Maven包依赖

    <dependency>
        <groupId>net.ymate.platform</groupId>
        <artifactId>ymate-platform-persistence-redis</artifactId>
        <version><VERSION></version>
    </dependency>

## 模块初始化配置

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

    # 连接池--
    ymp.configs.persistence.redis.ds.default.pool.fairness=false

    # 连接池--设置逐出策略类名, 默认为DefaultEvictionPolicy(当连接超过最大空闲时间,或连接数超过最大空闲连接数)
    ymp.configs.persistence.redis.ds.default.pool.eviction_policy_class_name=

    # 连接池--是否启用JMX管理功能, 默认为true
    ymp.configs.persistence.redis.ds.default.pool.jmx_enabled=

    # 连接池--
    ymp.configs.persistence.redis.ds.default.pool.jmx_name_base=pool

    # 连接池--
    ymp.configs.persistence.redis.ds.default.pool.jmx_name_prefix=pool

    # 连接池--是否启用后进先出, 默认为true
    ymp.configs.persistence.redis.ds.default.pool.lifo=true

    # 连接池--获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted), 如果超时就抛异常, 小于零:阻塞不确定的时间, 默认为-1
    ymp.configs.persistence.redis.ds.default.pool.max_wait_millis=-1

    # 连接池--
    ymp.configs.persistence.redis.ds.default.pool.min_evictable_idle_time_millis=

    # 连接池--对象空闲多久后逐出, 当空闲时间>该值且空闲连接>最大空闲数时直接逐出, 不再根据MinEvictableIdleTimeMillis判断(默认逐出策略)
    ymp.configs.persistence.redis.ds.default.pool.soft_min_evictable_idle_time_millis=

    # 连接池--在获取连接的时候检查有效性, 默认为false
    ymp.configs.persistence.redis.ds.default.pool.test_on_borrow=

    # 连接池--, 默认为false
    ymp.configs.persistence.redis.ds.default.pool.test_on_create=

    # 连接池--在归还到池中前进行检验, 默认为false
    ymp.configs.persistence.redis.ds.default.pool.test_on_return=

    # 连接池--在空闲时检查有效性, 默认为false
    ymp.configs.persistence.redis.ds.default.pool.test_while_idle=

    # 连接池--每次逐出检查时逐出的最大数目, 如果为负数就是1/abs(n), 默认为3
    ymp.configs.persistence.redis.ds.default.pool.num_tests_per_eviction_run=

    # 连接池--逐出扫描的时间间隔(毫秒), 如果为负数则不运行逐出线程, 默认为-1
    ymp.configs.persistence.redis.ds.default.pool.time_between_eviction_runs_millis=

## 多数据源连接

Redis持久化模块默认支持多数据源配置，下面通过简单的配置来展示如何连接多个服务：

	# 定义两个数据源分别用于连接本地和另一台IP地址两个Redis服务
    ymp.configs.persistence.redis.ds_default_name=default
    ymp.configs.persistence.redis.ds_name_list=default|otherredis

    # 默认数据源连接本地默认端口Redis服务
    ymp.configs.persistence.redis.ds.default.connection_type=default
    ymp.configs.persistence.redis.ds.default.server.default.password=123456

	# 名称otherredis数据源连接指定IP地址和端口的Redis服务
    ymp.configs.persistence.redis.ds.otherredis.connection_type=default
    ymp.configs.persistence.redis.ds.otherredis.server.default.host=192.168.10.110
    ymp.configs.persistence.redis.ds.otherredis.server.default.port=86379
    ymp.configs.persistence.redis.ds.otherredis.server.default.database=1
    ymp.configs.persistence.redis.ds.otherredis.server.default.password=654321

## 通过代码手工初始化模块示例

    // 创建YMP实例
    YMP owner = new YMP(ConfigBuilder.create(
            // 设置Redis模块配置
            ModuleCfgProcessBuilder.create().putModuleCfg(
                    RedisModuleConfigurable.create().addDataSource(
                            RedisDataSourceConfigurable.create("default").addServer(
                                    // 添加Redis服务器节点
                                    RedisServerConfigurable.create("default").host("localhost").port(6379)))).build())
            .proxyFactory(new DefaultProxyFactory())
            .developMode(true)
            .runEnv(IConfig.Environment.PRODUCT).build());
    // 向容器注册模块
    owner.registerModule(Redis.class);
    // 执行框架初始化
    owner.init();

## 示例代码

- 示例一：开启会话并手动关闭

        // 方式一：开启默认Redis服务连接会话
        IRedisSession _session = Redis.get().openSession();
        // 方式二：开启指定数据源连接会话
        _session = Redis.get().openSession("otherredis");
        try {
            // 通过会话接口可以获取Redis命令持有者接口对象实例
            IRedisCommandsHolder _holder = _session.getCommandHolder();
            // 可以通过命令持有者对象获取Jdeis对象和JedisCommands对象
            Jedis _jedis = _holder.getJedis();
            JedisCommands _commands = _holder.getCommands();
            // 示范写入key和value值
            _commands.set("key", "value");
        } finally {
            // 一定要确保连接使用完毕后关闭会话以释放连接
            _session.close();
        }

- 示例二：开启会话并在使用后自动关闭

        // 方式一：开启默认数据源连接会话
        Redis.get().openSession(new IRedisSessionExecutor<Object>() {
            @Override
            public Object execute(IRedisSession session) throws Exception {
                return session.getCommandHolder().getCommands().set("key", "value");
            }
        });

        // 方式二：开启指定数据源连接会话
        String _value = Redis.get().openSession("otherredis", new IRedisSessionExecutor<String>() {
            @Override
            public String execute(IRedisSession session) throws Exception {
                return session.getCommandHolder().getCommands().get("key");
            }
        });

- 示例三：消息订阅

        // 订阅缓存key过期通知
        Redis.get().subscribe(new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println("channel: " + channel + ", message: " + message);
            }
        }, "__keyevent@0__:expired");

        // 订阅指定数据源...通知
        Redis.get().subscribe("otherredis", new JedisPubSub() {
            ....
        }, "__keyevent@0__:expired");

    **说明：** 订阅对象`JedisPubSub`将在服务停止时被自动取消订阅。