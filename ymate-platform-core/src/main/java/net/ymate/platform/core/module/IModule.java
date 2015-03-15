/*
 * Copyright 2007-2107 the original author or authors.
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
 * @author 刘镇 (suninformation@163.com) on 15-3-5 下午3:47
 * @version 1.0
 */
public interface IModule {

    /**
     * 模块初始化
     *
     * @param owner 加载当前模块的YMP框架核心管理器对象
     * @throws Exception
     */
    public void init(YMP owner) throws Exception;

    /**
     * @return 返回模块是否已初始化
     */
    public boolean isInited();

    /**
     * 销毁模块
     *
     * @throws Exception
     */
    public void destroy() throws Exception;
}
