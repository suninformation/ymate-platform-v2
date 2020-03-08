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

import net.ymate.platform.core.beans.annotation.Ignored;

/**
 * 应用容器配置器工厂
 *
 * @author 刘镇 (suninformation@163.com) on 2019-08-12 04:10
 * @since 2.1.0
 */
@Ignored
public interface IApplicationConfigureFactory {

    /**
     * 获取启动配置类
     *
     * @return 返回启动配置类
     */
    Class<?> getMainClass();

    /**
     * 设置启动配置类
     *
     * @param mainClass 启动配置类
     */
    void setMainClass(Class<?> mainClass);

    /**
     * 获取启动参数
     *
     * @return 返回启动参数集合
     */
    String[] getArgs();

    /**
     * 设置启动参数
     *
     * @param args 启动参数集合
     */
    void setArgs(String[] args);

    /**
     * 获取应用容器配置器
     *
     * @return 返回应用容器配置器
     */
    IApplicationConfigurer getConfigurer();
}
