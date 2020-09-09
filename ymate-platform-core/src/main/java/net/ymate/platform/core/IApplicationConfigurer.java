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
package net.ymate.platform.core;

import net.ymate.platform.commons.IPasswordProcessor;
import net.ymate.platform.core.beans.IBeanLoadFactory;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.beans.intercept.InterceptSettings;
import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.i18n.II18nEventHandler;
import net.ymate.platform.core.module.IModuleConfigurer;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 应用容器配置器接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 2019-07-04 16:34
 * @since 2.1.0
 */
@Ignored
public interface IApplicationConfigurer {

    /**
     * 获取运行模式
     *
     * @return 返回运行模式
     */
    IApplication.Environment getRunEnv();

    /**
     * 获取代理工厂
     *
     * @return 返回代理工厂
     */
    IProxyFactory getProxyFactory();

    /**
     * 获取对象加载器工厂
     *
     * @return 返回对象加载器工厂
     */
    IBeanLoadFactory getBeanLoadFactory();

    /**
     * 获取应用容器配置分析器
     *
     * @return 返回应用容器配置分析器
     */
    IApplicationConfigureParser getConfigureParser();

    /**
     * 获取默认密码处理器
     *
     * @return 返回密码处理器
     */
    IPasswordProcessor getPasswordProcessor();

    /**
     * 获取自动扫描的包名称集合
     *
     * @return 返回自动扫描的包名称集合
     */
    List<String> getPackageNames();

    /**
     * 获取排除包名称集合，多个包名之间用'|'分隔，被包含在包路径下的类文件在扫描过程中将被忽略
     *
     * @return 返回排除包名称集合
     */
    List<String> getExcludedPackageNames();

    /**
     * 获取排除包文件名称集合，被包含的JAR或ZIP文件在扫描过程中将被忽略
     *
     * @return 返回排除包文件名称集合
     */
    List<String> getExcludedFiles();

    /**
     * 获取排除模块类名集合，被包含的模块在加载过程中将被忽略
     *
     * @return 返回排除模块类名集合
     */
    List<String> getExcludedModules();

    /**
     * 获取包含模块名集合，若设置该参数则框架初始化时仅加载被包含的模块
     *
     * @return 返回包含模块名集合
     */
    List<String> getIncludedModules();

    /**
     * 获取默认语言，若为空则采用JVM默认语言
     *
     * @return 返回语言
     */
    Locale getDefaultLocale();

    /**
     * 获取国际化资源事件监听处理器
     *
     * @return 返回国际化资源事件监听处理器
     */
    II18nEventHandler getI18nEventHandler();

    /**
     * 获取拦截器全局规则设置
     *
     * @return 返回拦截器全局规则设置
     */
    InterceptSettings getInterceptSettings();

    /**
     * 获取扩展参数映射
     *
     * @return 返回扩展参数映射
     */
    Map<String, String> getParameters();

    /**
     * 获取模块配置加载器
     *
     * @param moduleName 模块名称
     * @return 返回配置加载器对象
     */
    IModuleConfigurer getModuleConfigurer(String moduleName);
}
