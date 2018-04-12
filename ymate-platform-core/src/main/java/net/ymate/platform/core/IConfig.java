/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.core.beans.intercept.InterceptSettings;
import net.ymate.platform.core.i18n.II18NEventHandler;
import net.ymate.platform.core.support.IPasswordProcessor;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * YMP框架核心管理器初始化配置接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/11 下午5:41
 * @version 1.0
 */
public interface IConfig {

    /**
     * @return 返回是否为开发模式
     */
    boolean isDevelopMode();

    /**
     * @return 返回是否为测试环境
     */
    boolean isTestEnv();

    /**
     * @return 返回是否为开发环境
     */
    boolean isDevEnv();

    /**
     * @return 返回是否为生产环境
     */
    boolean isProductEnv();

    /**
     * @return 返回当前运行环境
     */
    String getRunEnv();

    /**
     * @return 返回框架自动扫描的包路径集合
     */
    List<String> getAutoscanPackages();

    /**
     * @return 返回包文件排除列表，被包含的JAR或ZIP文件在扫描过程中将被忽略
     */
    List<String> getExcludedFiles();

    /**
     * @return 返回模块类排除列表，被包含的模块在加载过程中将被忽略
     */
    List<String> getExcludedModules();

    /**
     * @return 国际化资源默认语言设置，可选参数，默认采用系统环境语言
     */
    Locale getDefaultLocale();

    /**
     * @return 国际化资源管理器事件监听处理器，可选参数
     */
    II18NEventHandler getI18NEventHandlerClass();

    /**
     * @return 默认密码处理器，可选参数，用于对已加密参数值进行解密，默认为net.ymate.platform.core.support.impl.DefaultPasswordProcessor
     */
    Class<? extends IPasswordProcessor> getDefaultPasswordClass();

    /**
     * @return 返回框架全局参数映射
     */
    Map<String, String> getParams();

    /**
     * @param name 参数名称
     * @return 返回由name指定的全局参数值
     */
    String getParam(String name);

    /**
     * @param moduleName 模块名称
     * @return 返回模块配置参数映射
     */
    Map<String, String> getModuleConfigs(String moduleName);

    /**
     * @return 返回框架事件配置参数映射
     */
    Map<String, String> getEventConfigs();

    /**
     * @return 是否开启拦截器全局规则设置, 默认为false
     */
    boolean isInterceptSettingsEnabled();

    /**
     * @return 返回拦截器全局规则设置
     */
    InterceptSettings getInterceptSettings();
}
