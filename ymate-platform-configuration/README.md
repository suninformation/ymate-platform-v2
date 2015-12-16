#ymate-platform-configuration

配置体系模块，是通过简单的目录结构实现在项目开发以及维护过程中，对配置等各种文件资源的统一管理，为模块化开发和部署提供灵活的、简单有效的解决方案；

###特点:

- 从开发角度规范了模块化开发流程、统一资源文件的生命周期管理；
- 从可维护角度将全部资源集成在整个体系中，具备有效的资源重用和灵活的系统集成构建、部署和数据备份与迁移等优势；

- 简单的配置文件检索、加载及管理模式；
- 模块间资源共享，模块(modules)可以共用所属项目(projects)的配置、类和jar包等资源文件；
- 默认支持XML和Property配置文件解析，可以通过IConfigurationProvider接口自定义文件格式，支持缓存，避免重复加载；
- 配置对象支持@Configuration注解方式声明，无需编码即可自动加载并填充配置内容到类对象；
- 集成模块的构建（编译）与分发、服务的启动与停止，以及清晰的资源文件分类结构可快速定位；



###配置体系目录结构:

按优先级由低到高的顺序依次是：全局(configHome) -> 项目(projects) -> 模块(modules)：


    CONFIG_HOME\
        |--bin\
        |--cfgs\
        |--classes\
        |--dist\
        |--lib\
        |--logs\
        |--plugins\
        |--projects\
        |   |--<project_xxx>
        |   |   |--cfgs\
        |   |   |--classes\
        |   |   |--lib\
        |   |   |--logs\
        |   |   |--modules\
        |   |   |   |--<module_xxx>
        |   |   |   |   |--cfgs\
        |   |   |   |   |--classes\
        |   |   |   |   |--lib\
        |   |   |   |   |--logs\
        |   |   |   |   |--plugins\
        |   |   |   |   |--<......>
        |   |   |   |--<......>
        |   |   |--plugins\
        |   |--<......>
        |--temp\
        |--......

###模块配置及使用:

- 配置体系模块初始化参数, 将下列配置项按需添加到ymp-conf.properties文件中, 否则模块将使用默认配置进行初始化:


        #-------------------------------------
        # 配置体系模块初始化参数
        #-------------------------------------
        
        # 配置体系根路径，必须决对路径，前缀支持${root}、${user.home}和${user.dir}变量，默认为${root}
        ymp.configs.configuration.config_home=
        
        # 项目名称，做为根路径下级子目录，对现实项目起分类作用，默认为空
        ymp.configs.configuration.project_name=
        
        # 模块名称，此模块一般指现实项目中分拆的若干子项目的名称，默认为空
        ymp.configs.configuration.module_name=
        
        # 指定配置体系下的默认配置文件分析器，默认为net.ymate.platform.configuration.impl.DefaultConfigurationProvider
        ymp.configs.configuration.provider_class=


- 基于XML文件的基础配置格式如下, 为了配合测试代码, 请将该文件命名为configuration.xml并放置在config_home路径下的cfgs目录里:


        <?xml version="1.0" encoding="UTF-8"?>
        <!-- XML根节点为properties -->
        <properties abc="xyz">
        
            <!-- 分类节点为category, 默认分类名称为default -->
            <category name="default">
            
                <!-- 属性标签为property, name代表属性名称, value代表属性值(也可以用property标签包裹) -->
                <property name="company_name" value="Apple Inc."/>
                
                <!-- 用属性标签表示一个数组或集合数据类型的方法 -->
                <property name="products">
                    <!-- 集合元素必须用value标签包裹, 且value标签不要包括任何扩展属性 -->
                    <value>iphone</value>
                    <value>ipad</value>
                    <value>imac</value>
                    <value>itouch</value>
                </property>
                
                <!-- 用属性标签表示一个MAP数据类型的方法, abc代表扩展属性key, xyz代表扩展属性值, 扩展属性与item将被合并处理  -->
                <property name="product_spec" abc="xzy">
                    <!-- MAP元素用item标签包裹, 且item标签必须包含name扩展属性(其它扩展属性将被忽略), 元素值由item标签包裹 -->
                    <item name="color">red</item>
                    <item name="weight">120g</item>
                    <item name="size">small</item>
                    <item name="age">2015</item>
                </property>
            </category>
        </properties>


- 新建配置类, 通过@Configuration注解指定配置文件相对路径


        @Configuration("cfgs/configuration.xml")
        public class DemoConfig extends DefaultConfiguration {
        }


- 测试代码, 完成模块初始化并加载配置文件内容:


        public static void main(String[] args) throws Exception {
            YMP.get().init();
            try {
                DemoConfig _cfg = new DemoConfig();
                if (Cfgs.get().fillCfg(_cfg)) {
                    System.out.println(_cfg.getString("company_name"));
                    System.out.println(_cfg.getMap("product_spec"));
                    System.out.println(_cfg.getList("products"));
                }
            } finally {
                YMP.get().destroy();
            }
        }

- 执行结果:


        Apple Inc.
        {abc=xzy, color=red, size=small, weight=120g, age=2015}
        [itouch, imac, ipad, iphone]
