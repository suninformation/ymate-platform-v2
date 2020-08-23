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
package net.ymate.platform.core.configuration;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;

import java.io.File;
import java.io.InputStream;

/**
 * 配置体系管理器接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 15-3-9 上午12:24
 */
@Ignored
public interface IConfig extends IDestroyable {

    String MODULE_NAME = "configuration";

    String YMP_CONFIG_HOME = "YMP_CONFIG_HOME";

    String PROJECTS_FOLDER_NAME = "projects";

    String MODULES_FOLDER_NAME = "modules";

    /**
     * 初始化
     *
     * @throws Exception 初始过程中产生的任何异常
     */
    void initialize() throws Exception;

    /**
     * 是否已初始化
     *
     * @return 返回true表示已初始化
     */
    boolean isInitialized();

    /**
     * 配置体系主目录路径
     *
     * @return 返回配置体系主目录路径
     */
    String getConfigHome();

    /**
     * 项目主目录路径
     *
     * @return 返回项目主目录路径
     */
    String getProjectName();

    /**
     * 模块主目录路径
     *
     * @return 返回模块主目录路径
     */
    String getModuleName();

    /**
     * 模块主目录路径
     *
     * @return 返回模块主目录路径
     */
    String getModuleHome();

    /**
     * 系统变量 user.home 值
     *
     * @return 返回系统变量 user.home 值
     */
    String getUserHome();

    /**
     * 系统变量 user.dir 值
     *
     * @return 返回系统变量 user.dir 值
     */
    String getUserDir();

    /**
     * 搜索配置文件真实资源路径，先在配置体系中查找，再到项目 CLASSPATH 路径中查找，若 cfgFile 以 "jar:" 开头则直接返回
     *
     * @param cfgFile 配置文件相对路径及名称
     * @return 配置文件真实路径
     */
    String searchAsPath(String cfgFile);

    /**
     * 按照模块路径-&gt;项目路径-&gt;主路径(CONFIG_HOME)-&gt;用户路径(user.dir)-&gt;系统用户路径(user.home)的顺序寻找指定文件
     *
     * @param cfgFile 配置文件路径及名称
     * @return 找到的文件File对象，只要找到存在的File，立即停止寻找并返回当前File实例
     */
    File searchAsFile(String cfgFile);

    /**
     * 按照模块路径的顺序寻找指定文件, 若文件存在则返回该文件输入流
     *
     * @param cfgFile 配置文件路径及名称
     * @return 返回配置文件输入流
     */
    InputStream searchAsStream(String cfgFile);

    /**
     * 根据配置文件名称自动分析(xml、properties和json等)文件类型并填充配置对象, 若未找到则返回null
     *
     * @param cfgFileName 配置所需要的装载参数
     * @param search      是否采用搜索
     * @return 返回配置对象
     */
    IConfiguration loadCfg(String cfgFileName, boolean search);

    /**
     * 根据配置文件名称自动分析(xml、properties和json等)文件类型并填充配置对象, 若未找到则返回null
     *
     * @param cfgFileName 配置所需要的装载参数
     * @return 返回配置对象
     */
    IConfiguration loadCfg(String cfgFileName);

    /**
     * 填充配置对象
     *
     * @param <T>          目标对象类型
     * @param configObject 配置对象，不可为空
     * @param cfgFileName  配置所需要的装载参数
     * @return 是否成功装载配置
     */
    <T extends IConfiguration> T fillCfg(T configObject, String cfgFileName);

    /**
     * 填充配置对象
     *
     * @param <T>          目标对象类型
     * @param configObject 配置对象，不可为空
     * @param cfgFileName  配置所需要的装载参数
     * @param search       是否采用搜索
     * @return 是否成功装载配置
     */
    <T extends IConfiguration> T fillCfg(T configObject, String cfgFileName, boolean search);

    /**
     * 填充配置对象
     *
     * @param <T>          目标对象类型
     * @param configObject 配置对象，不可为空
     * @return 是否成功装载配置
     */
    <T extends IConfiguration> T fillCfg(T configObject);

    /**
     * 装载配置，根据Configuration注解指定的配置文件进行加载，否则默认使用当前配置类对象的SimpleName作为配置文件名，即：SimpleName.CfgTagName.xml
     *
     * @param <T>          目标对象类型
     * @param configObject 配置对象，不可为空
     * @param search       是否采用搜索
     * @return 是否成功装载配置
     */
    <T extends IConfiguration> T fillCfg(T configObject, boolean search);

    /**
     * 根据自定义配置提供者填充配置对象
     *
     * @param <T>           目标对象类型
     * @param providerClass 配置提供者类对象，若为空则采用框架默认
     * @param configObject  配置对象，不可为空
     * @param cfgFileName   配置所需要的装载参数
     * @param search        是否采用搜索
     * @return 是否成功装载配置
     */
    <T extends IConfiguration> T fillCfg(Class<? extends IConfigurationProvider> providerClass, T configObject, String cfgFileName, boolean search);

    /**
     * 根据自定义配置提供者填充配置对象
     *
     * @param <T>           目标对象类型
     * @param providerClass 配置提供者类对象，若为空则采用框架默认
     * @param configObject  配置对象，不可为空
     * @param cfgFileName   配置所需要的装载参数
     * @param search        是否采用搜索
     * @param reload        是否自动重新加载
     * @return 是否成功装载配置
     */
    <T extends IConfiguration> T fillCfg(Class<? extends IConfigurationProvider> providerClass, T configObject, String cfgFileName, boolean search, boolean reload);
}
