---
title: 快速上手
---



import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';



# 快速上手（Quickstart）

本文档主要阐述如何快速搭建基于 [YMP - 轻量级 Java 应用开发框架](https://ymate.net/) 的 Web 工程、如何通过 IDE 环境配置 Tomcat 服务及工程的运行和测试的完整过程。

为了能够帮助你快速搭建各种类型的 Java 工程，YMP 框架特别提供的一系列 Maven Archetypes 模板。

目前支持构建以下四种项目类型：

| 类型         | 描述                                                         |
| ------------ | ------------------------------------------------------------ |
| Quickstart   | 标准 Java 工程，已集成 YMP 核心依赖和参数配置                |
| Webapp       | 标准 Web 工程，已集成 WebMVC 相关依赖和参数配置              |
| Module       | 标准 Maven 多模块工程，已集成 Assembly 插件的自定义打包规则和命令行启动脚本 |
| Microservice | 基于 YMP 框架的微服务多模块工程（***暂仅内部使用***）        |



## 安装与配置

本项目依赖于 [Maven](http://maven.apache.org/) 环境，假设你已具备，编写本文所使用的 Maven 版本为：Apache Maven 3.1.1

首次使用时需要下载源码并安装到本地 Maven 仓库中，操作步骤如下：



### 下载最新源码



<Tabs
defaultValue="github"
values={[
{ label: 'GitHub', value: 'github', },
{ label: '码云', value: 'gitee', },
]
}>
<TabItem value="github">

```shell
git clone https://github.com/suninformation/ymate-maven-archetypes.git
```

</TabItem>
<TabItem value="gitee">

```shell
git clone https://gitee.com/suninformation/ymate-maven-archetypes.git
```

</TabItem>
</Tabs>



### 编译安装到本地仓库



```shell
cd ymate-maven-archetypes
mvn clean install
```



:::tip **小提示！**
为了保持代码是最新的，一定要记得经常使用 `git pull` 命令进行更新！
:::



## 操作说明



### 运行工程构建向导



```shell
mvn archetype:generate -DarchetypeCatalog=local
```



:::tip **小技巧！**
为了方便书写，可以通过 `alias` 命令为其创建一个别名（如：`createprj`），设置方法如下：

- 编辑 `~/.bash_profile` 文件并添加以下内容：

```shell
alias createprj="mvn archetype:generate -DarchetypeCatalog=local"
```

- 使配置生效

```shell
source ~/.bash_profile
```

- 测试命令

```shell
createprj
```

*（注：该方法适用于 Linux 和 Mac OS）*
:::



**控制台输出：**

```shell
......
1: local -> net.ymate.maven.archetypes:ymate-archetype-microservice (microservice)
2: local -> net.ymate.maven.archetypes:ymate-archetype-quickstart (quickstart)
3: local -> net.ymate.maven.archetypes:ymate-archetype-module (module)
4: local -> net.ymate.maven.archetypes:ymate-archetype-webapp (webapp)
Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): :
```



:::tip **小提示！**
若命令执行后没有显示上述内容，请执行 `mvn archetype:crawl` 命令后再试！
:::



### 选择模板并按提示设置

本例演示如何创建和运行 Web 工程，所以此处按屏幕提示应选择 `4` 号模板类型，输入数字并按屏幕提示设置坐标系等信息，如下所示：

```shell
Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): : 4
Define value for property 'groupId': net.ymate.platform.examples
Define value for property 'artifactId': ymp-examples-webapp
Define value for property 'version' 1.0-SNAPSHOT: :
Define value for property 'package' net.ymate.platform.examples: :
Confirm properties configuration:
groupId: net.ymate.platform.examples
artifactId: ymp-examples-webapp
version: 1.0-SNAPSHOT
package: net.ymate.platform.examples
 Y: :
```

若以上信息确认无误，按回车键确认并开始生成工程文件。

至此，基于 Maven Archetypes 模板快速搭建 YMP 工程构建完毕！



### 使用 IDE 打开工程

:::tip **提示：** 

请使用你最常用的 IDE 开发工具（如：[Apache NetBeans](http://netbeans.apache.org/)、[Eclipse](https://www.eclipse.org/downloads/) 等）导入刚刚创建的 Maven 工程，本文使用的是 [IntelliJ IDEA](https://www.jetbrains.com/idea/) 做为演示。

:::



如下图所示，通过模板构建的 Web 工程中除了生成常规配置和资源文件之外，还默认提供了一个名称为 `HelloController` 的类文件简单且清晰地展示如何编写控制器，更多内容请阅读 [WebMVC](/guide/webmvc) 文档。



![open-using-idea](/img/quickstart/quickstart_open_with_idea.png)



### 配置 Tomcat 服务器

首先，点击上图红框 ① 处按钮，弹出 `运行/调试配置` 对话框，从左侧选择目标 Tomcat 服务，若未添加任何 Tomcat 服务，请点击红框 ② 处按钮进行添加，如下图所示：



![quickstart_tomcat_config_1](/img/quickstart/quickstart_tomcat_config_1.png)



此处，需要将当前的 Web 工程部署到 Tomcat 服务，并根据情况调整应用上下文访问路径，如下图所示：




![quickstart_tomcat_config_2](/img/quickstart/quickstart_tomcat_config_2.png)



最后，确认以上配置无误后点击右下角的 `OK` 按钮使其生效。



### 编译并启动 Web 服务

如下图所示，点击红框 ③ 处按钮可以运行或调式模式启动 Web 服务，查看控制台输出内容并确认无异常表示服务启动成功。

![quickstart_tomcat_run](/img/quickstart/quickstart_tomcat_run.png)




### 浏览器访问测试

打开浏览器并在地址栏中输入如下 URL 地址：

```shell
http://localhost:8080/hello
```



![quickstart_web_result](/img/quickstart/quickstart_access_controller.png)



恭喜你！已经成功地完成了基于 YMP 框架的项目从创建、编译到运行等一系列操作，亲自动手尝试一下吧！



:::tip One More Thing

YMP 不仅提供便捷的 Web 及其它 Java 项目的快速开发体验，也将不断提供更多丰富的项目实践经验。

感兴趣的小伙伴儿们可以加入官方 QQ 群：480374360，一起交流学习，帮助 YMP 成长！

如果喜欢 YMP，希望得到你的支持和鼓励！

<Tabs
defaultValue="donationCode"
values={[
{label: '请喝一杯咖啡', value: 'donationCode'},
{label: '微信支付', value: 'wepay'},
{label: '支付宝', value: 'alipay'},
]}>
<TabItem value="donationCode"><img src="/img/donation_code.png" alt="donationCode"/></TabItem>
<TabItem value="wepay"><img src="/img/wepay.png" alt="WePay"/></TabItem>
<TabItem value="alipay"><img src="/img/alipay.jpeg" alt="AliPay"/></TabItem>
</Tabs>

了解更多有关 YMP 框架的内容，请访问官网：[https://ymate.net](https://ymate.net)

:::