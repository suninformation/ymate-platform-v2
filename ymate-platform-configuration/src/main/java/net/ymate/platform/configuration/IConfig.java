/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.configuration;

import java.io.File;

/**
 * 配置体系管理器接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-9 上午12:24
 * @version 1.0
 */
public interface IConfig {

    public static final String MODULE_NAME = "configuration";

    public static final String __PROJECTS_FOLDER_NAME = "projects";
    public static final String __MODULES_FOLDER_NAME = "modules";

    public static final String __CONFIG_HOME = "CONFIG_HOME";
    public static final String __USER_HOME = "user.home";
    public static final String __USER_DIR = "user.dir";

    /**
     * @return 返回配置体系模块配置对象
     */
    public IConfigModuleCfg getModuleCfg();

    /**
     * @return 返回配置体系主目录路径
     */
    public String getConfigHome();

    /**
     * @return 返回项目主目录路径
     */
    public String getProjectHome();

    /**
     * @return 返回模块主目录路径
     */
    public String getModuleHome();

    /**
     * @return 返回系统变量user.home值
     */
    public String getUserHome();

    /**
     * @return 返回系统变量user.dir值
     */
    public String getUserDir();

    /**
     * 搜索配置文件真实资源路径，先在配置体系中查找，再到项目 CLASSPATH 路径中查找，若 cfgFile 以 "jar:" 开头则直接返回
     *
     * @param cfgFile 配置文件相对路径及名称
     * @return 配置文件真实路径
     */
    public String searchPath(String cfgFile);

    /**
     * 按照模块路径->项目路径->主路径(CONFIG_HOME)->用户路径(user.dir)->系统用户路径(user.home)的顺序寻找指定文件
     *
     * @param cfgFile 配置文件路径及名称
     * @return 找到的文件File对象，只要找到存在的File，立即停止寻找并返回当前File实例
     */
    public File searchFile(String cfgFile);

    public boolean fillCfg(IConfiguration config, String cfgFileName);

    /**
     * 填充配置对象
     *
     * @param config      配置对象，不可为空
     * @param cfgFileName 配置所需要的装载参数
     * @param search      是否采用搜索
     * @return 是否成功装载配置
     */
    public boolean fillCfg(IConfiguration config, String cfgFileName, boolean search);

    public boolean fillCfg(IConfiguration config);

    /**
     * 装载配置，根据Configuration注解指定的配置文件进行加载，否则默认使用当前配置类对象的SimpleName作为配置文件名，即：SimpleName.CfgTagName.xml
     *
     * @param config 配置对象，不可为空
     * @param search 是否采用搜索
     * @return 是否成功装载配置
     */
    public boolean fillCfg(IConfiguration config, boolean search);

    /**
     * 根据自定义配置提供者填充配置对象
     *
     * @param providerClass 配置提供者类对象，若为空则采用框架默认
     * @param config        配置对象，不可为空
     * @param cfgFileName   配置所需要的装载参数
     * @param search        是否采用搜索
     * @return 是否成功装载配置
     */
    public boolean fillCfg(Class<? extends IConfigurationProvider> providerClass, IConfiguration config, String cfgFileName, boolean search);
}
