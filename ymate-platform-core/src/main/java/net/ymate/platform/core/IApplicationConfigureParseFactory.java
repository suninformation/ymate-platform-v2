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
 * @author 刘镇 (suninformation@163.com) on 2019-08-13 15:24
 * @since 2.1.0
 */
@Ignored
public interface IApplicationConfigureParseFactory {

    /**
     * 获取应用容器配置分析器
     *
     * @return 返回应用容器配置分析器
     */
    IApplicationConfigureParser getConfigureParser();
}
