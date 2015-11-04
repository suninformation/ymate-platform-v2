/*
 * Copyright 2007-2016 the original author or authors.
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

import net.ymate.platform.core.YMP;

/**
 * 模块接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 2012-11-24 下午6:13:22
 * @version 1.0
 */
public interface IModule {

    /**
     * @return 返回模块名称
     */
    public String getName();

    /**
     * 模块初始化
     *
     * @param owner 加载当前模块的YMP框架核心管理器对象
     * @throws Exception 当模块初始化失败时抛出异常
     */
    public void init(YMP owner) throws Exception;

    /**
     * @return 返回模块是否已初始化
     */
    public boolean isInited();

    /**
     * @return 返回所属YMP框架管理器实例
     */
    public YMP getOwner();

    /**
     * 销毁模块
     *
     * @throws Exception 当模块销毁失败时抛出异常
     */
    public void destroy() throws Exception;
}
