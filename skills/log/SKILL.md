# Log 日志模块

## 1. 模块概览

Log 日志模块是 YMP 框架中基于开源日志框架 Log4j 2 实现的日志管理模块，提供日志记录器对象的统一管理，并整合了 JCL、Slf4j 等优秀的日志系统，可以在任意位置调用任意日志记录器输出日志，实现了系统与业务日志的分离，同时与 YMP 配置体系模块配合使用，效果更佳。

- **统一日志管理**：提供日志记录器对象的统一管理
- **多日志系统整合**：整合了 JCL、Slf4j 等优秀的日志系统
- **灵活的配置**：支持通过配置文件和注解进行配置
- **系统与业务日志分离**：可以根据需要将不同类型的日志输出到不同的文件
- **与配置体系集成**：与 YMP 配置体系模块配合使用，配置更加灵活

## 2. 核心功能

### 2.1 统一日志管理

Log 日志模块提供日志记录器对象的统一管理，开发者可以通过统一的 API 获取和使用日志记录器。

### 2.2 多日志系统整合

整合了 JCL、Slf4j 等优秀的日志系统，开发者可以根据自己的习惯选择使用不同的日志门面接口。

### 2.3 灵活的配置

支持通过配置文件和注解进行配置，可以根据需要调整日志的输出方式、输出格式、日志级别等。

### 2.4 系统与业务日志分离

可以根据需要将不同类型的日志输出到不同的文件，实现系统与业务日志的分离，便于日志的管理和分析。

### 2.5 日志格式定制

支持自定义日志格式模板，可以根据需要调整日志的输出格式。

## 3. 核心 API/类/函数

### 3.1 ILogger 接口

日志记录器接口，提供了各种级别的日志记录方法。

**主要方法**：
- `debug(Object message)`：输出 DEBUG 级别的日志
- `info(Object message)`：输出 INFO 级别的日志
- `warn(Object message)`：输出 WARN 级别的日志
- `error(Object message)`：输出 ERROR 级别的日志
- `fatal(Object message)`：输出 FATAL 级别的日志
- `isDebugEnabled()`：检查是否启用了 DEBUG 级别的日志
- `isInfoEnabled()`：检查是否启用了 INFO 级别的日志
- `isWarnEnabled()`：检查是否启用了 WARN 级别的日志
- `isErrorEnabled()`：检查是否启用了 ERROR 级别的日志
- `isFatalEnabled()`：检查是否启用了 FATAL 级别的日志

### 3.2 Logs 工具类

日志模块的工具类，提供了获取日志记录器的静态方法。

**主要方法**：
- `get()`：获取 Logs 实例
- `getLogger()`：获取默认名称的日志记录器
- `getLogger(String name)`：获取指定名称的日志记录器
- `getLogger(Class<?> clazz)`：获取指定类的日志记录器

### 3.3 LogFactory 类

日志工厂类，用于创建日志记录器。

**主要方法**：
- `getLog(Class<?> clazz)`：获取指定类的日志记录器
- `getLog(String name)`：获取指定名称的日志记录器

## 4. 技术架构与实现

### 4.1 架构层次

1. **API 层**：提供统一的日志记录器接口
2. **适配层**：整合 JCL、Slf4j 等日志系统
3. **实现层**：基于 Log4j 2 实现日志记录功能
4. **配置层**：处理日志配置

### 4.2 核心组件

- **ILogger**：日志记录器接口
- **DefaultLogger**：基于 Log4j 2 的默认日志记录器实现
- **Logs**：日志模块的工具类
- **LogFactory**：日志工厂类

### 4.3 工作流程

1. 应用程序通过 Logs 工具类获取日志记录器
2. 调用日志记录器的方法输出日志
3. 日志记录器将日志传递给 Log4j 2 进行处理
4. Log4j 2 根据配置将日志输出到相应的目标（如文件、控制台等）

## 5. 使用指南与典型场景

### 5.1 基本配置

**配置文件方式**：

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

**注解方式**：

```java
@LogConf(
    configFile = "${user.dir}/cfgs/log4j.xml",
    outputDir = "${user.dir}/logs/",
    defaultLoggerName = "default",
    logFormat = "${dateTime} ${level} [${hostName}] [${threadName}] [${threadId}:${callerInfo}] ${logContent}",
    printStackCount = 5,
    allowConsoleOutput = true,
    simplifiedPackageName = true,
    formatPaddedOutput = true
)
```

### 5.2 Log4j 配置示例

以下是默认 `log4j.xml` 文件内容，该文件应放置在由配置参数 `config_file` 指定的位置：

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

### 5.3 基本使用

**示例 1：基于 JCL 接口调用**

```java
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LogDemo {

    private static final Log LOG = LogFactory.getLog(LogDemo.class);

    public static void main(String[] args) throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Everything depends on ability!  -- YMP :)");
        }
    }
}
```

**示例 2：基于 Slf4j 接口调用**

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogDemo {

    private static final Logger LOG = LoggerFactory.getLogger(LogDemo.class);

    public static void main(String[] args) throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Everything depends on ability!  -- {} :)", "YMP");
        }
    }
}
```

**示例 3：任意日志输出**

在不声明日志记录器的情况下，在任意位置调用并输出日志内容：

```java
Logs.get().getLogger().debug("Everything depends on ability!  -- YMP :)");
```

### 5.4 自定义日志记录器

在一些特殊情况下，需要将特定的日志内容记录在不同的日志文件中，可以通过以下步骤实现：

**步骤 1：在 log4j.xml 中添加自定义 appender**

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

**步骤 2：在 log4j.xml 中添加自定义 logger**

```xml
<Logger name="wechat" level="debug" additivity="false">
    <AppenderRef ref="wechat"/>
</Logger>
```

**步骤 3：在代码中使用自定义日志记录器**

```java
// 使用默认日志记录器输出
ILogger defaultLog = Logs.get().getLogger();
defaultLog.debug("日志内容将被输出到default.log文件中");

// 使用自定义日志记录器输出
ILogger wechatLog = Logs.get().getLogger("wechat");
wechatLog.debug("日志内容将被输出到wechat.log文件中");
if (wechatLog.isDebugEnabled()) {
    wechatLog.debug("日志内容", e);
}
// 或者
Logs.get().getLogger("wechat").info("日志内容");
```

## 6. 配置、部署与开发

### 6.1 配置项说明

| 配置项 | 描述 | 默认值 |
|-------|------|--------|
| config_file | 日志记录器配置文件 | ${root}/cfgs/log4j.xml |
| output_dir | 日志文件输出路径 | ${root}/logs/ |
| logger_name | 日志记录器默认名称 | default |
| logger_class | 日志记录器接口实现类 | net.ymate.platform.log.impl.DefaultLogger |
| log_format | 日志格式模板 | "${dateTime} ${level} [${hostName}] [${threadName}] [${threadId}:${callerInfo}] ${logContent}" |
| print_stack_count | 打印堆栈数量 | 5 |
| allow_output_console | 是否允许控制台输出 | false |
| simplified_package_name | 是否采用简化包名输出 | false |
| format_padded_output | 是否采用格式化填充输出 | false |

### 6.2 注解配置

可以使用 `@LogConf` 注解进行配置：

| 配置项 | 描述 |
|-------|------|
| configFile | 日志记录器配置文件 |
| outputDir | 日志文件输出路径 |
| defaultLoggerName | 默认日志记录器名称 |
| logFormat | 日志格式模板 |
| printStackCount | 打印堆栈数量 |
| allowConsoleOutput | 是否允许控制台输出 |
| formatPaddedOutput | 是否采用格式化填充输出 |
| simplifiedPackageName | 是否采用简化包名输出 |
| loggerClass | 日志记录器接口实现类 |

### 6.3 开发建议

1. **在开发阶段**：将 `allow_output_console` 参数设置为 `true`，这样可以通过控制台直接查看日志输出
2. **在生产环境**：将 `allow_output_console` 参数设置为 `false`，只将日志输出到文件
3. **合理设置日志级别**：根据不同的环境和场景设置合适的日志级别
4. **使用不同的日志文件**：将不同类型的日志输出到不同的文件，便于管理和分析

## 7. 监控与维护

### 7.1 日志管理

- **日志文件轮转**：Log4j 2 支持基于时间和大小的日志文件轮转
- **日志归档**：可以配置将过期的日志文件归档
- **日志清理**：定期清理过期的日志文件，避免占用过多磁盘空间

### 7.2 常见问题与解决方案

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 日志文件不生成 | 配置文件路径不正确 | 检查配置文件路径是否正确 |
| 控制台无日志输出 | allow_output_console 为 false | 将 allow_output_console 设置为 true |
| 日志级别不生效 | Log4j 配置文件中的级别设置不正确 | 检查 Log4j 配置文件中的级别设置 |
| 日志文件过大 | 日志轮转配置不当 | 调整日志轮转配置，减小单个日志文件大小 |

## 8. 总结与亮点回顾

### 8.1 核心优势

- **统一的日志管理**：提供统一的日志记录器接口，简化日志使用
- **多日志系统整合**：整合了 JCL、Slf4j 等优秀的日志系统，兼容性强
- **灵活的配置**：支持通过配置文件和注解进行配置，适应不同的场景
- **系统与业务日志分离**：可以根据需要将不同类型的日志输出到不同的文件
- **与配置体系集成**：与 YMP 配置体系模块配合使用，配置更加灵活

### 8.2 技术亮点

- **基于 Log4j 2**：采用性能优异的 Log4j 2 作为底层实现
- **统一的 API**：提供统一的日志记录器接口，屏蔽底层实现细节
- **多日志系统整合**：整合了多种日志系统，开发者可以根据习惯选择使用
- **灵活的配置**：支持多种配置方式，适应不同的场景
- **易于扩展**：提供了扩展点，可以自定义日志记录器实现

### 8.3 应用场景

- **开发阶段**：通过控制台输出日志，方便调试
- **测试阶段**：详细记录日志，便于问题定位
- **生产环境**：将日志输出到文件，便于管理和分析
- **多模块应用**：不同模块可以使用不同的日志记录器，便于日志管理
- **分布式应用**：可以将日志输出到集中的日志系统，便于统一管理

Log 日志模块作为 YMP 框架的基础模块之一，为应用程序提供了统一、灵活、高效的日志管理能力，是应用程序开发和运维的重要工具。
