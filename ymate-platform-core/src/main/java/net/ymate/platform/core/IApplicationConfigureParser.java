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
import net.ymate.platform.core.configuration.IConfigReader;
import net.ymate.platform.core.module.IModuleConfigurer;
import org.apache.commons.lang3.StringUtils;

/**
 * 应用容器配置分析器接口
 *
 * @author 刘镇 (suninformation@163.com) on 2019-08-07 18:19
 * @since 2.1.0
 */
@Ignored
public interface IApplicationConfigureParser {

    String PASS_PREFIX = String.format("%s(", StringUtils.defaultIfBlank(System.getProperty(IApplication.SYSTEM_PASS_PREFIX), "ENC"));

    String PASS_SUFFIX = ")";

    /**
     * 获取配置读取器
     *
     * @return 返回配置读取器
     */
    IConfigReader getConfigReader();

    /**
     * 获取指定模块名称的配置
     *
     * @param moduleName 模块名称
     * @return 返回模块配置器
     */
    IModuleConfigurer getModuleConfigurer(String moduleName);
}
