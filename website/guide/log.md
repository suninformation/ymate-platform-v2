---
sidebar_position: 4
slug: log
---

# 日志（Log）

基于开源日志框架 Log4j 2 实现，提供日志记录器对象的统一管理，并整合了 JCL、Slf4j 等优秀的日志系统，可以在任意位置调用任意日志记录器输出日志，实现了系统与业务日志的分离，同时与 YMP 配置体系模块配合使用，效果更佳。



## Maven包依赖

```xml
<dependency>
    <groupId>net.ymate.platform</groupId>
    <artifactId>ymate-platform-log</artifactId>
    <version>2.1.2</version>
</dependency>
```



## 模块配置

### 配置文件参数说明

```properties
#-------------------------------------
# 日志模块初始化参数
#-------------------------------------

# 日志记录器配置文件, 默认值: ${root}/cfgs/log4j.xml
ymp.configs.log.config_file=${user.dir}/cfgs/log4j.xml

# 日志文件输出路径, 默认值: ${root}/logs/
ymp.configs.log.output_dir=${user.dir}/logs/

# 日志记录器默认名称, 默认值: default
ymp.configs.log.logger_name=

# 日志记录器接口实现类, 默认值: net.ymate.platform.log.impl.DefaultLogger
ymp.configs.log.logger_class=

# 日志格式模板, 默认值: "${dateTime} ${level} [${hostName}] [${threadName}] [${threadId}:${callerInfo}] ${logContent}"
ymp.configs.log.log_format=

# 打印堆栈数量, 超过这个数量会省略输出, 默认值: 5
ymp.configs.log.print_stack_count=

# 否允许控制台输出, 默认值: false
ymp.configs.log.allow_output_console=true

# 日志记录器是否采用简化包名输出, 默认值: false
ymp.configs.log.simplified_package_name=true

# 日志记录器是否采用格式化填充输出, 默认值: false
ymp.configs.log.format_padded_output=true
```

:::tip **注意**：

建议在开发阶段将 `allow_output_console` 参数设置为 `true`，这样可以通过控制台直接查看日志输出。

:::



### 配置注解参数说明

#### @LogConf

| 配置项                | 描述                 |
| --------------------- | -------------------- |
| configFile            | 日志记录器配置文件   |
| outputDir             | 日志文件输出路径     |
| defaultLoggerName     | 默认日志记录器名称   |
| logFormat             | 日志格式模板         |
| printStackCount       | 打印堆栈数量         |
| allowConsoleOutput    | 否允许控制台输出     |
| formatPaddedOutput    | 否采用简化包名输出   |
| simplifiedPackageName | 否采用格式化填充输出 |
| loggerClass           | 日志记录器接口实现类 |



### Log4j配置示例

以下是默认 `log4j.xml` 文件内容，该文件应放置在由配置参数 `config_file` 指定的位置，同时请根据实际情况调整其内容，模块初始化时会检查该文件是否存在，若不存在则生成并放置在 `${root}/cfgs/` 路径下。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration>
    <Appenders>
        <RollingRandomAccessFile name="default" 
                                 fileName="${sys:LOG_OUT_DIR}/default.log"
                                 filePattern="${sys:LOG_OUT_DIR}/$${date:yyyyMMdd}/default-%d{yyMMddHH}-%i.log">
            <PatternLayout pattern="%m %n" charset="UTF-8"/>
            <Policies>
                <TimeBasedTriggeringPolicy modulate="true" interval="1"/>
                <SizeBasedTriggeringPolicy size="200 MB"/>
            </Policies>
            <DefaultRolloverStrategy max="100"/>
        </RollingRandomAccessFile>
        <!--
        <RollingRandomAccessFile name="custom-logname"
                                 fileName="${sys:LOG_OUT_DIR}/custom-logname.log"
                                 filePattern="${sys:LOG_OUT_DIR}/$${date:yyyyMMdd}/custom-logname-%d{yyMMddHH}-%i.log">
            <PatternLayout pattern="%m %n" charset="UTF-8"/>
            <Policies>
                <TimeBasedTriggeringPolicy modulate="true" interval="1"/>
                <SizeBasedTriggeringPolicy size="200 MB"/>
            </Policies>
            <DefaultRolloverStrategy max="100"/>
        </RollingRandomAccessFile>
        -->
    </Appenders>
    <Loggers>
        <!--
        <Logger name="custom-logname" level="debug" additivity="false">
            <AppenderRef ref="custom-logname"/>
        </Logger>
        -->
        <Root level="debug">
            <AppenderRef ref="default"/>
        </Root>
    </Loggers>
</Configuration>
```



## 模块使用

在日常编写代码过程中，不需要开发者关心日志框架，只需要引入 JCL、Slf4j 等日志门面系统的接口方法使用即可，可以根据团队或个人习惯自行选择。



### 示例一：基于JCL接口调用

```java
package demo;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@EnableAutoScan
@EnableBeanProxy
public class LogDemo {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, LogDemo.class.getName());
    }

    private static final Log LOG = LogFactory.getLog(LogDemo.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Everything depends on ability!  -- YMP :)");
            }
        }
    }
}
```



### 示例二：基于Slf4j接口调用

```java
package demo;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EnableAutoScan
@EnableBeanProxy
public class LogDemo {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, LogDemo.class.getName());
    }

    private static final Logger LOG = LoggerFactory.getLogger(LogDemo.class);

    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Everything depends on ability!  -- {} :)", "YMP");
            }
        }
    }
}
```



### 示例三：任意日志输出

在前面两个示例中，需要在使用日志输出时需要预先声明日志记录器静态变量，接下来，在本例中将展示如何在不声明日志记录器的情况下，在任意位置调用并输出日志内容，代码如下：

```java
Logs.get().getLogger().debug("Everything depends on ability!  -- YMP :)");
```



### 示例四：自定义日志记录器

在一些特殊情况下，我们需要将特定的日志内容记录在不同的日志文件中，在本例中将展示如何自定义日志记录器及如何调用。

首先，需要在配置文件中增加名称为 `wechat` 的记录器配置，配置中明确了日志文件的存储路径、文件名格式及日志文件拆分规则等。

```xml
<RollingRandomAccessFile name="wechat"
                         fileName="${sys:LOG_OUT_DIR}/wechat.log"
                         filePattern="${sys:LOG_OUT_DIR}/$${date:yyyyMMdd}/wechat-%d{yyMMddHH}-%i.log">
	<PatternLayout pattern="%m %n" charset="UTF-8"/>
	<Policies>
		<TimeBasedTriggeringPolicy modulate="true" interval="1"/>
		<SizeBasedTriggeringPolicy size="200 MB"/>
	</Policies>
	<DefaultRolloverStrategy max="100"/>
</RollingRandomAccessFile>
```

然后，再定义一个新的日志记录器并设置其日志输出级别和是否输出到根日志中，同时指定该记录器配置，这里我们为了方便而采用了相同的名称命名。

```xml
<Logger name="wechat" level="debug" additivity="false">
	<AppenderRef ref="wechat"/>
</Logger>
```

好了，通过以上的配置后，接下来就可以在业务代码中调用了，示例代码如下：

- 使用默认日志记录器输出：

  ```java
  ILogger defaultLog = Logs.get().getLogger();
  defaultLog.debug("日志内容将被输出到default.log文件中");
  defaultLog.debug("日志内容", e);
  ```

  :::tip **注意**：

  默认日志记录器是由 `logger_name` 参数指定的，默认值为 `default`。

  :::

- 使用自定义日志记录器输出：

  ```java
  ILogger wechatLog = Logs.get().getLogger("wechat");
  wechatLog.debug("日志内容将被输出到wechat.log文件中");
  if (wechatLog.isDebugEnabled()) {
  	wechatLog.debug("日志内容", e);
  }
  // 或者
  Logs.get().getLogger("wechat").info("日志内容");
  ```



