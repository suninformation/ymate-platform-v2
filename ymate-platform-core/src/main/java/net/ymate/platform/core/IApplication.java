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

import net.ymate.platform.core.beans.IBeanFactory;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.beans.intercept.IInterceptor;
import net.ymate.platform.core.beans.intercept.InterceptSettings;
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.event.Events;
import net.ymate.platform.core.i18n.I18N;
import net.ymate.platform.core.module.ModuleManager;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.RecycleHelper;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * 应用容器接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 2019-04-25 17:40
 * @since 2.1.0
 */
@Ignored
public interface IApplication extends IDestroyable {

    String YMP_BASE_PACKAGE_NAME = "net.ymate.platform";

    String SYSTEM_ENV = "ymp.env";

    String SYSTEM_PACKAGES = "ymp.packages";

    String SYSTEM_CONFIG_HOME = "ymp.configHome";

    String SYSTEM_CONFIG_FILE = "ymp.configFile";

    String SYSTEM_MAIN_CLASS = "ymp.mainClass";

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
     * 获取应用容器配置器工厂
     *
     * @return 返回应用容器配置器工厂
     */
    IApplicationConfigureFactory getConfigureFactory();

    /**
     * 获取模块管理器
     *
     * @return 返回模块管理器实例
     */
    ModuleManager getModuleManager();

    /**
     * 获取对象工厂
     *
     * @return 返回对象工厂
     */
    IBeanFactory getBeanFactory();

    /**
     * 获取对象资源回收助手
     *
     * @return 返回对象资源回收助手实例
     */
    RecycleHelper getRecycleHelper();

    /**
     * 获取国际化资源管理器
     *
     * @return 返回国际化资源管理器
     */
    I18N getI18n();

    /**
     * 是否为测试环境
     *
     * @return 返回true表示是
     */
    boolean isTestEnv();

    /**
     * 是否为开发环境
     *
     * @return 返回true表示是
     */
    boolean isDevEnv();

    /**
     * 是否为生产环境
     *
     * @return 返回true表示是
     */
    boolean isProductEnv();

    /**
     * 获取当前运行环境
     *
     * @return 返回运行环境枚举值
     */
    Environment getRunEnv();

    /**
     * 获取事件管理器
     *
     * @return 返回事件管理器
     */
    Events getEvents();

    /**
     * 获取拦截器配置
     *
     * @return 返回拦截器配置
     */
    InterceptSettings getInterceptSettings();

    /**
     * 注册拦截器
     *
     * @param interceptClass 拦截器类
     */
    void registerInterceptor(Class<? extends IInterceptor> interceptClass);

    /**
     * 注册拦截器注解
     *
     * @param annotationClass 注解类
     * @param interceptClass  拦截器类
     * @param singleton       是否单例
     */
    void registerInterceptAnnotation(Class<? extends Annotation> annotationClass, Class<? extends IInterceptor> interceptClass, boolean singleton);

    /**
     * 获取框架全局参数映射
     *
     * @return 返回全局参数映射
     */
    Map<String, String> getParams();

    /**
     * 获取由name指定的全局参数值
     *
     * @param name 参数名称
     * @return 返回参数值
     */
    String getParam(String name);

    /**
     * 获取由name指定的全局参数值，若参数值为空则返回默认值
     *
     * @param name         参数名称
     * @param defaultValue 默认值
     * @return 返回参数值
     */
    String getParam(String name, String defaultValue);

    /**
     * 获取全局配置参数读取器
     *
     * @return 返回全局配置参数读取器
     */
    IConfigReader getParamConfigReader();

    /**
     * 运行模式枚举
     */
    enum Environment {
        /**
         * 测试环境
         */
        TEST,

        /**
         * 开发环境
         */
        DEV,

        /**
         * 生产环境
         */
        PRODUCT,

        /**
         * 未知(未指定)
         */
        UNKNOWN
    }
}
