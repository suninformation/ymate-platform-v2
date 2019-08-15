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
package net.ymate.platform.webmvc;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MVC框架管理器接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/17 下午9:52
 */
@Ignored
public interface IWebMvc extends IInitialization<IApplication>, IDestroyable {

    String MODULE_NAME = "webmvc";

    /**
     * 获取所属应用容器实例
     *
     * @return 返回所属应用容器实例
     */
    IApplication getOwner();

    /**
     * 获取WebMVC模块配置对象
     *
     * @return 返回WebMVC模块配置对象
     */
    IWebMvcConfig getConfig();

    /**
     * 注册并分析控制器
     *
     * @param targetClass 目标类型
     * @return 返回是否有效注册
     * @throws Exception 可能产生的异常
     */
    boolean registerController(Class<?> targetClass) throws Exception;

    /**
     * 注册拦截器规则配置
     *
     * @param targetClass 目标类型
     * @return 返回是否有效注册
     * @throws Exception 可能产生的异常
     */
    boolean registerInterceptorRule(Class<? extends IInterceptorRule> targetClass) throws Exception;

    /**
     * 处理控制器请求
     *
     * @param context        请求上下文
     * @param servletContext ServletContext对象
     * @param request        HttpServletRequest对象
     * @param response       HttpServletResponse对象
     * @throws Exception 可能产生的异常
     */
    void processRequest(IRequestContext context,
                        ServletContext servletContext,
                        HttpServletRequest request,
                        HttpServletResponse response) throws Exception;
}
