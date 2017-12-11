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
package net.ymate.platform.webmvc;

import net.ymate.platform.webmvc.view.IView;

/**
 * 控制器异常自定义处理过程
 *
 * @author 刘镇 (suninformation@163.com) on 2017/12/11 下午1:11
 * @version 1.0
 */
public interface IResponseErrorProcessor {

    /**
     * 处理异常
     *
     * @param owner 所属YMP框架管理器实例
     * @param e     异常对象
     * @return 返回响应视图对象, 若为null将交由框架默认处理
     */
    IView processError(IWebMvc owner, Throwable e);
}
