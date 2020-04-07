---
sidebarDepth: 2
---

# 日志（Log）

基于开源日志框架Log4J 2实现，提供对日志记录器对象的统一管理，可以在任意位置调用任意日志记录器输出日志，实现系统与业务日志的分离；与YMP配置体系模块配合使用，效果更佳:)

日志框架同时提供扩展支持的两个子项目：

> - log-jcl：用于整合apache-commons-logging日志框架；
> - log-slf4j：针对slf4j日志系统提供支持；

## Maven包依赖

- Log依赖配置

        <dependency>
            <groupId>net.ymate.platform</groupId>
            <artifactId>ymate-platform-log</artifactId>
            <version><VERSION></version>
        </dependency>

- log-jcl依赖配置

        <dependency>
            <groupId>net.ymate.platform</groupId>
            <artifactId>ymate-platform-log-jcl</artifactId>
            <version><VERSION></version>
        </dependency>

- log-slf4j依赖配置

        <dependency>
            <groupId>net.ymate.platform</groupId>
            <artifactId>ymate-platform-log-slf4j</artifactId>
            <version><VERSION></version>
        </dependency>

> **注**：
> - 请根据您项目的实际情况，在项目的pom.xml中添加相应配置，该模块已经默认引入核心包依赖，无需重复配置。
> - 在使用中需要注意`log-jcl`和`log-slf4j`内部使用的YMP对象是全局实例(采用`YMP.get()`方式获取的YMP实例对象称为全局实例)。

## 模块事件

LogEvent事件枚举对象包括以下事件类型：

|事务类型|说明|
|---|---|
|LOG_WRITE_IN|日志写入时触发该事件|

## 模块配置

日志模块初始化参数, 将下列配置项按需添加到ymp-conf.properties文件中, 否则模块将使用默认配置进行初始化:

	  #-------------------------------------
	  # 日志模块初始化参数
	  #-------------------------------------
	
	  # 日志记录器配置文件，默认为${root}/cfgs/log4j.xml，变量${user.dir}的取值结果将受配置体系模块影响
	  ymp.configs.log.config_file=
	  
	  # 日志文件输出路径，默认为${root}/logs/
	  ymp.configs.log.output_dir=
	  
	  # 日志记录器默认名称，默认为default
	  ymp.configs.log.logger_name=
	  
	  # 日志记录器接口实现类，默认为net.ymate.platform.log.impl.DefaultLogger
	  ymp.configs.log.logger_class=
	  
	  # 日志记录器是否允许控制台输出，默认为false
	  ymp.configs.log.allow_output_console=
	  
	  # 日志记录器是否采用简化包名输出，默认为false
      ymp.configs.log.simplified_package_name=
      
      # 日志记录器是否采用格式化填充输出，默认为false
      ymp.configs.log.format_padded_output=

   > **注**：需要注意`config_file`配置的log4j.xml文件是否存在，以及`output_dir`指定的输出路径是否正确有效，这两项配置会影响YMP框架启动时异常；
   > 
   > 此外，建议在开发阶段将`allow_output_console`参数设置为true，这样可以通过控制台直接查看日志输出；


Log4J配置文件，内容如下：

        <?xml version="1.0" encoding="UTF-8"?>
        <Configuration>
            <Appenders>
                <RollingFile name="default" fileName="${sys:LOG_OUT_DIR}/default.log"
                             filePattern="${sys:LOG_OUT_DIR}/$${date:yyyy-MM}/default-%d{MM-dd-yyyy}-%i.log.gz">
                    <PatternLayout pattern="%m %n" charset="UTF-8"/>
                    <SizeBasedTriggeringPolicy size="500 MB"/>
                </RollingFile>
                <!--
                <RollingFile name="custom-logname" fileName="${sys:LOG_OUT_DIR}/custom-logname.log"
                             filePattern="${sys:LOG_OUT_DIR}/$${date:yyyy-MM}/custom-logname-%d{MM-dd-yyyy}-%i.log.gz">
                    <PatternLayout pattern="%m %n" charset="UTF-8"/>
                    <SizeBasedTriggeringPolicy size="500 MB"/>
                </RollingFile>
                -->
            </Appenders>
            <Loggers>
                <!--
                <Logger name="custom-logname" level="debug">
                    <AppenderRef ref="custom-logname"/>
                </Logger>
                -->
                <!-- 配置记录器级别 -->
                <Root level="debug">
                    <!-- 输出设置 -->
                    <AppenderRef ref="default"/>
                </Root>
            </Loggers>
        </Configuration>

    **注**：该文件应根据ymp.configs.log.config_file指定的位置，其内容请根据实际情况调整。

## 通过代码手工初始化模块示例

    // 创建YMP实例
    YMP owner = new YMP(ConfigBuilder.create(
            // 设置日志模块配置
            ModuleCfgProcessBuilder.create().putModuleCfg(
                    LogModuleConfigurable.create()
                            .configFile("${root}/cfgs/log4j.xml")
                            .outputDir("${root}/logs/")
                            .loggerName("default")
                            .allowOutputConsole(true)).build())
            .proxyFactory(new DefaultProxyFactory())
            .developMode(true)
            .runEnv(IConfig.Environment.PRODUCT).build());
    // 向容器注册模块
    owner.registerModule(Logs.class);
    // 执行框架初始化
    owner.init();

## 使用示例

首先，为了配合演示多个日志记录器的使用方法，修改log4j.xml配置内容如下：

	  <?xml version="1.0" encoding="UTF-8"?>
	  <Configuration>
	      <Appenders>
	          <RollingFile name="default" fileName="${sys:LOG_OUT_DIR}/default.log"
	                       filePattern="${sys:LOG_OUT_DIR}/$${date:yyyy-MM}/default-%d{MM-dd-yyyy}-%i.log.gz">
	              <PatternLayout pattern="%m %n" charset="UTF-8"/>
	              <SizeBasedTriggeringPolicy size="500 MB"/>
	          </RollingFile>
	
	          <RollingFile name="wechat" fileName="${sys:LOG_OUT_DIR}/wechat.log"
	                       filePattern="${sys:LOG_OUT_DIR}/$${date:yyyy-MM}/wechat-%d{MM-dd-yyyy}-%i.log.gz">
	              <PatternLayout pattern="%m %n" charset="UTF-8"/>
	              <SizeBasedTriggeringPolicy size="500 MB"/>
	          </RollingFile>
	
	      </Appenders>
	      <Loggers>
	
	          <Logger name="wechat" level="debug">
	              <AppenderRef ref="wechat"/>
	          </Logger>
	
	          <!-- 配置记录器级别 -->
	          <Root level="debug">
	              <!-- 输出设置 -->
	              <AppenderRef ref="default"/>
	          </Root>
	      </Loggers>
	  </Configuration>
	
> 上面的配置文件中共配置两个日志记录器：
> 
> - default：默认根日志记录器，它将记录所有日志内容；
> - wechat：自定义的日志记录器；

示例代码：

- 使用默认日志记录器输出：

		Logs.get().getLogger().debug("日志将被输出到default.log文件...");
		Logs.get().getLogger().debug("日志内容", e);

	>  **注**：默认日志记录器是由`logger_name`参数指定的，默认值为default；

- 输出日志到wechat.log文件中：

		ILogger _wechat = Logs.get().getLogger("wechat");
		_wechat.debug("日志将被分别输出到wechat.log和default.log文件中");
		//
		if (_wechat.isDebugEnabled()) {
			_wechat.debug("日志内容", e);
		}
		
		// 或者
		Logs.get().getLogger("wechat").info("日志内容");

## 怀旧版业务日志记录工具使用示例

通过`@Loggable`注解与`Logoo`类配合来记录完整业务逻辑处理过程；

- @Loggable: 声明一个类对业务日志记录工具的支持或声明一个方法开始业务日志记录器：

    > value[]：设定输出到日志记录器名称集合，可选；
    >
    > flag：自定义标识；
    >
    > action：自定义动作标识；
    >
    > level：日志输出级别, 默认为: INFO；
    >
    > merge：是否日志合并输出, 默认为: false；
    >
    > adapterClass：自定义日志适配器类；

- 示例代码：：

        public class DemoLogooAdapter extends DefaultLogooAdapter {
        
            @Override
            public void onLogWritten(String flag, String action, Map<String, Object> attributes) {
                // 自定义业务逻辑处理过程中传递的参数, 如: 写入数据库等
                System.out.println("业务逻辑处理完毕...");
            }
        }
    
        @Controller
        @RequestMapping("/hello")
        @Loggable(value = "custom", flag = "示例:")
        public class HelloController {
        
            @RequestMapping("/")
            @Loggable(flag = "逻辑处理", merge = true, adapterClass = DemoLogooAdapter.class)
            public IView hello() throws Exception {
                // 输出日志
                Logoo.log("日志输出...");
                // 传递参数
                Logoo.addAttribute("key1", "value1);
                //
                Logoo.finished();
                //
                return View.textView("Hello YMP world!");
            }
        }
