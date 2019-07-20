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

import net.ymate.platform.core.support.IInitialization;

/**
 * 配置体系配置接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 2012-11-28 下午9:04:52
 */
public interface IConfigurationConfig extends IInitialization<IConfig> {

    String CONFIG_HOME = "config_home";

    String PROJECT_NAME = "project_name";

    String MODULE_NAME = "module_name";

    String CONFIG_CHECK_TIME_INTERVAL = "config_check_time_interval";

    String PROVIDER_CLASS = "provider_class";

    /**
     * 配置体系根路径，必须绝对路径，前缀支持${root}、${user.home}和${user.dir}变量，默认值为${root}
     *
     * @return 返回配置体系根路径
     */
    String getConfigHome();

    /**
     * 项目名称，做为根路径下级子目录，对现实项目起分类作用，默认值为空
     *
     * @return 返回项目名称
     */
    String getProjectName();

    /**
     * 模块名称，此模块一般指现实项目中分拆的若干子项目的名称，默认值为空
     *
     * @return 返回模块名称
     */
    String getModuleName();

    /**
     * 配置文件检查时间间隔(毫秒)，默认值为0表示不开启
     *
     * @return 返回时间间隔
     */
    long getConfigCheckTimeInterval();

    /**
     * 指定配置体系下的默认配置文件分析器，默认值为net.ymate.platform.configuration.impl.DefaultConfigurationProvider
     *
     * @return 返回配置提供者接口类型
     */
    Class<? extends IConfigurationProvider> getConfigurationProviderClass();
}
