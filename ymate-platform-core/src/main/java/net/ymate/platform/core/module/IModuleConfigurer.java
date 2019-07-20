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
package net.ymate.platform.core.module;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.configuration.IConfigReader;

/**
 * 模块配置器接口
 *
 * @author 刘镇 (suninformation@163.com) on 2019-07-24 18:02
 * @since 2.1.0
 */
@Ignored
public interface IModuleConfigurer {

    /**
     * 获取当前配置对应的模块名称
     *
     * @return 返回所属模块名称
     */
    String getModuleName();

    /**
     * 获取模块配置读取器
     *
     * @return 返回配置读取器实例
     */
    IConfigReader getConfigReader();
}
