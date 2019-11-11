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
 * 应用容器构建者接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 2019-07-04 09:52
 * @since 2.1.0
 */
@Ignored
public interface IApplicationCreator {

    /**
     * 构建应用容器接口实现对象
     *
     * @param mainClass               启动配置类(用于解析初始化配置注解)
     * @param applicationInitializers 扩展初始化处理器
     * @return 返回应用容器对象
     * @throws Exception 可能产生的任何异常
     */
    IApplication create(Class<?> mainClass, IApplicationInitializer... applicationInitializers) throws Exception;
}
