---
name: ymp-log
description: YMP框架日志模块，基于Log4j2实现并整合JCL/Slf4j门面，支持LogFactory.getLog()(JCL)/LoggerFactory.getLogger()(Slf4j)/Logs.getLogger()三种调用方式，自定义记录器分离业务日志
version: 2.1.4-dev
author: YMP Team
category: framework
tags:
  - java
  - framework
  - log
  - log4j2
  - slf4j
  - jcl
  - logging
trigger: 当用户需要写日志、配置log4j2.xml格式输出、Logs.get().getLogger(loggerName)多记录器、@LogConf注解配置、ymp.configs.log.*参数、JCL vs Slf4j占位符区别时触发
tools:
  - log4j2
  - slf4j
  - jcl
  - logger-management
examples:
  - JCL: LogFactory.getLog(Class) + LOG.info(msg)
  - Slf4j: LoggerFactory.getLogger(Class) + LOG.info("msg:{}", arg)
  - Logs快捷方式: Logs.get().getLogger().debug()/getLogger("wechat").info()
  - @LogConf(allowConsoleOutput=true)开控制台日志
  - 自定义wechat记录器拆分日志文件
---

# Log 日志技能包

> AI读取指引：本模块边界=日志管理(Log4j2+JCL/Slf4j门面整合)；所有类路径前缀`net.ymate.platform.log`；三种调用方式(JCL/Slf4j/Logs)可并存；依赖core模块，事件/DI跳转core/SKILL.md。

---

## 0. 快速索引

- Maven artifactId：`ymate-platform-log`
- 静态入口类：`net.ymate.platform.log.Logs`（Logs.get().getLogger()）；外加JCL/Slf4j门面
- 必备配置：`ymp.configs.log.config_file`(log4j.xml路径) / `@LogConf`注解 / classpath放置log4j2.xml
- 5行最简日志（三种任选）：
```java
// JCL
private static final Log LOG = LogFactory.getLog(MyClass.class);
LOG.info("Hello");
// Slf4j
private static final Logger LOG = LoggerFactory.getLogger(MyClass.class);
LOG.info("Hello {}", "World");
// Logs快捷
Logs.get().getLogger().debug("Quick log");
```

## 1. 模块摘要

YMP日志模块基于Log4j2实现，统一管理记录器，整合JCL(Apache Commons Logging)与Slf4j两大主流门面，开发者按习惯任选其一即可。支持默认记录器与自定义命名记录器（分别写入不同日志文件），与YMP配置体系配合自动加载log4j.xml。

- **三种调用方式统一**：JCL(LogFactory.getLog) / Slf4j(LoggerFactory.getLogger) / Logs.getLogger()（无需静态变量，任意位置调用）
- **多记录器拆分**：默认default + 自定义name(如wechat/payment)，在log4j.xml中分别配置Appender与Logger，实现系统/业务日志分离
- **Log4j2配置自动生成**：${root}/cfgs/log4j.xml不存在时，模块初始化自动生成默认模板
- **注解+properties双配置**：@LogConf在启动类注解；ymp.configs.log.*在properties文件，前者优先
- **输出格式化**：allowConsoleOutput控制台开关；simplifiedPackageName简化包名；formatPaddedOutput对齐填充；printStackCount堆栈输出行数限制

## 2. 核心注解/类速查表（全限定名）

| 类/注解 | 全限定名 | 核心作用 |
|---|---|---|
| Logs | `net.ymate.platform.log.Logs` | 日志管理器单例：Logs.get()获取ILog实例，getLogger()/getLogger(name)获取记录器 |
| ILog | `net.ymate.platform.log.ILog` | 日志模块接口：getLogger()/getLogger(String) |
| ILogger | `net.ymate.platform.log.ILogger` | 记录器接口：debug/info/warn/error/fatal + isXxxEnabled + 带Throwable重载 |
| @LogConf | `net.ymate.platform.log.annotation.LogConf` | 启动类日志注解配置：configFile/outputDir/defaultLoggerName/logFormat/printStackCount/allowConsoleOutput/formatPaddedOutput/simplifiedPackageName/loggerClass |
| Log (JCL接口) | `org.apache.commons.logging.Log` | JCL门面接口：fatal/error/warn/info/debug/trace + isXxxEnabled |
| LogFactory (JCL) | `org.apache.commons.logging.LogFactory` | JCL工厂：getLog(Class/String)获取Log实例（字符串拼接参数） |
| Logger (Slf4j接口) | `org.slf4j.Logger` | Slf4j门面接口：同级别方法 + 占位符{}格式化（性能优，避免字符串拼接） |
| LoggerFactory (Slf4j) | `org.slf4j.LoggerFactory` | Slf4j工厂：getLogger(Class/String)获取Logger实例 |
| ILogConfig | `net.ymate.platform.log.ILogConfig` | 日志配置接口（YMP内部） |
| DefaultLogConfig | `net.ymate.platform.log.impl.DefaultLogConfig` | 默认配置实现（YMP内部） |
| ILogger (YMP) vs Logger (Slf4j) vs Log (JCL) | - | 三套接口独立，YMP Logs返回的是ILogger；JCL/Slf4j走门面代理到Log4j2 |

## 3. 核心API速查（≤8条最常用）

- `LogFactory.getLog(Class<?> clazz)` → `org.apache.commons.logging.Log`：JCL方式获取记录器（最常用，参数拼接）
- `LoggerFactory.getLogger(Class<?> clazz)` → `org.slf4j.Logger`：Slf4j方式获取记录器（占位符{}，性能优先）
- `Logs.get()` → `ILog`：获取YMP日志管理器单例
- `Logs.get().getLogger()` → `ILogger`：获取默认记录器（ymp.configs.log.logger_name指定，默认default）
- `Logs.get().getLogger(String loggerName)` → `ILogger`：获取自定义命名记录器（需在log4j.xml配置同名Logger+Appender）
- `LOG.debug/info/warn/error(Object message)` / `LOG.error(Object msg, Throwable t)`：各级别输出方法（三种接口签名一致）
- `LOG.isDebugEnabled()/isInfoEnabled()` → `boolean`：高并发场景下先判断避免字符串拼接开销
- `Slf4j占位符：LOG.info("User {} login at {}", uid, time)` → 自动填充{}，性能优于JCL"+"拼接 |

## 4. 标准代码模板

### 模板1：三种调用方式（JCL/Slf4j/Logs）在同一类演示 + 自定义记录器wechat

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
package com.example.log;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.YMP;
import net.ymate.platform.core.annotation.EnableAutoScan;
import net.ymate.platform.core.annotation.EnableBeanProxy;
import net.ymate.platform.log.ILogger;
import net.ymate.platform.log.Logs;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志三种调用方式演示-JCL/Slf4j/Logs快捷方式，对比参数拼接vs占位符性能差异
 *
 * @author YMP Team
 * @since 2.1.4-dev
 */
@EnableAutoScan
@EnableBeanProxy
public class LogDemo {

    static {
        System.setProperty(IApplication.SYSTEM_MAIN_CLASS, LogDemo.class.getName());
    }

    // ==================== 方式一：JCL (Apache Commons Logging) ====================
    // 依赖：org.apache.commons.logging.Log / LogFactory
    // 传参：字符串拼接（+），isXxxEnabled()判断避免无效拼接
    private static final Log JCL_LOG = LogFactory.getLog(LogDemo.class);

    // ==================== 方式二：Slf4j (Simple Logging Facade 4 Java) ====================
    // 依赖：org.slf4j.Logger / LoggerFactory
    // 传参：占位符 {}，底层延迟拼接（推荐，性能好）；多个参数按顺序填充，异常写最后一个参数
    private static final Logger SLF_LOG = LoggerFactory.getLogger(LogDemo.class);

    /**
     * 启动入口：演示三种日志调用+自定义wechat记录器
     *
     * @param args CLI
     * @throws Exception 启动异常
     * @since 2.1.4-dev
     */
    public static void main(String[] args) throws Exception {
        try (IApplication application = YMP.run(args)) {
            String uid = "U1001";
            int score = 95;

            // -------- JCL 使用演示（字符串拼接） --------
            if (JCL_LOG.isInfoEnabled()) {
                JCL_LOG.info("User " + uid + " scored " + score + " points by JCL");
            }
            try {
                throw new RuntimeException("Simulated error for JCL");
            } catch (Exception e) {
                JCL_LOG.error("JCL error with uid=" + uid, e);
            }

            // -------- Slf4j 使用演示（占位符 {}，推荐） --------
            // 单参数/多参数按顺序匹配，异常对象写最后一位无需占位符
            if (SLF_LOG.isInfoEnabled()) {
                SLF_LOG.info("User {} scored {} points by Slf4j", uid, score);
            }
            try {
                throw new RuntimeException("Simulated error for Slf4j");
            } catch (Exception e) {
                SLF_LOG.error("Slf4j error with uid={}", uid, e);
            }

            // ==================== 方式三：YMP Logs 快捷方式（无需static变量，任意位置调用） ====================
            // 默认记录器(default)
            ILogger defaultLog = Logs.get().getLogger();
            defaultLog.debug("Quick default debug log via Logs.get().getLogger()");
            if (defaultLog.isDebugEnabled()) {
                defaultLog.info("User " + uid + " via YMP default logger");
            }
            // 自定义命名记录器(wechat)：需在log4j.xml中定义<Logger name="wechat"> + 对应Appender
            ILogger wechatLog = Logs.get().getLogger("wechat");
            wechatLog.info("Wechat module specific log - this line goes to wechat.log file");
            try {
                throw new RuntimeException("Simulated wechat error");
            } catch (Exception e) {
                wechatLog.error("Wechat log error", e);
            }
        }
    }
}

/*
配套 log4j.xml 片段（放在${user.dir}/cfgs/log4j.xml或ymp.configs.log.config_file指定路径）：
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
        <!-- 自定义wechat记录器Appender -->
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
    </Appenders>
    <Loggers>
        <Logger name="wechat" level="debug" additivity="false">
            <AppenderRef ref="wechat"/>
        </Logger>
        <Root level="debug">
            <AppenderRef ref="default"/>
        </Root>
    </Loggers>
</Configuration>
*/
```

## 5. 配置速查

### 5.1 配置文件最常改项（≤12条 key|默认值|说明）

| 配置项（ymp.configs.log.*） | 默认值 | 说明 |
|---|---|---|
| config_file | ${root}/cfgs/log4j.xml | Log4j2 XML配置文件路径（不存在则自动生成默认） |
| output_dir | ${root}/logs/ | 日志文件输出根目录（系统属性LOG_OUT_DIR注入到log4j.xml） |
| logger_name | default | 默认记录器名称，对应log4j.xml中<Logger name="xxx">的name |
| logger_class | net.ymate.platform.log.impl.DefaultLogger | ILogger接口实现类（一般无需改） |
| log_format | "${dateTime} ${level} [${hostName}] [${threadName}] [${threadId}:${callerInfo}] ${logContent}" | YMP自定义输出模板（DefaultLogger专用，Log4j2优先PatternLayout） |
| print_stack_count | 5 | 异常堆栈打印行数上限，超出省略 |
| allow_output_console | false | 是否允许控制台打印stdout（开发阶段建议true） |
| simplified_package_name | false | 包名简化输出：net.ymate.platform -> n.y.p.l（日志体积更小） |
| format_padded_output | false | 各字段空格对齐填充（人眼易读，略增日志长度） |

### 5.2 注解配置核心参数

| 注解参数 | 类型 | 说明 |
|---|---|---|
| @LogConf.configFile / outputDir / defaultLoggerName | String | 对应properties的config_file/output_dir/logger_name |
| @LogConf.logFormat / printStackCount | String / int | 对应properties的log_format/print_stack_count |
| @LogConf.allowConsoleOutput / formatPaddedOutput / simplifiedPackageName | boolean | 对应properties三个开关 |
| @LogConf.loggerClass | Class<? extends ILogger> | 自定义ILogger实现（高级） |
| JCL: LOG.info("a="+a+" b="+b) | - | 字符串拼接，需isInfoEnabled判断 |
| Slf4j: LOG.info("a={} b={}", a, b) | - | 占位符{}按顺序，异常对象写最后无需占位符（自动识别） |
| Logs.get().getLogger("wechat") | String loggerName | 名称需与log4j.xml中<Logger name="xxx">严格匹配大小写 |

## 6. 常见坑点排查

| 现象 | 可能原因 | 排查/修复 |
|---|---|---|
| 日志文件不生成/目录为空 | output_dir路径权限/不存在；log4j.xml中Appender fileName写错；LOG_OUT_DIR未被设置 | 检查ymp.configs.log.output_dir绝对路径；给进程写权限；确保log4j.xml使用${sys:LOG_OUT_DIR}变量（YMP初始化自动注入系统属性） |
| 控制台无输出 | allow_output_console=false；开发忘记开启 | @LogConf(allowConsoleOutput=true) 或 ymp.configs.log.allow_output_console=true；若仍无，检查log4j2是否加了Console Appender（默认生成的XML不含Console，仅文件输出） |
| Slf4j占位符打印出字面"{}"不解析 | 参数数量与{}数量不匹配；异常对象放错位置 | 检查参数顺序个数：LOG.info("a={} b={}", a) 缺第二个参数；异常必须最后一位且无需{}：LOG.error("msg", e)正确，LOG.error("msg {}", e)错 |
| JCL字符串拼接性能差、CPU高 | 大量info/debug在关闭级别时仍拼接字符串 | 统一用if (LOG.isDebugEnabled()) { LOG.debug(...) } 包裹；新项目直接选Slf4j占位符写法免判断 |
| 自定义"wechat"记录器日志仍进default.log | log4j.xml中<Logger>未设additivity="false"；名称大小写不匹配 | <Logger name="wechat" level="debug" additivity="false">（additivity=false表示不向上冒泡到Root）；Logs.getLogger("WeChat")和name="wechat"大小写不同=两个不同记录器 |
| 日志打印包名/调用者信息不准确/为空 | formatPaddedOutput/simplifiedPackageName影响；YMP DefaultLogger模式vs Log4j2原生PatternLayout冲突 | 若使用log4j.xml PatternLayout（推荐）：pattern里用%c{1.} %M %L输出包/方法/行号，忽略ymp.configs.log.log_format（后者仅YMP自己的DefaultLogger流式拼接有效） |
| 多线程日志乱序/丢失 | Log4j2异步Appender配置不当；RollingRandomAccessFile buffer满 | 默认RollingRandomAccessFile已高效；极端高并发下<AsyncLogger>提升吞吐；检查磁盘IO是否瓶颈 |
