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
package net.ymate.platform.core.persistence;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;
import net.ymate.platform.core.support.IInitialization;

/**
 * @param <SESSION>           会话类型
 * @param <CONFIG>            配置类型
 * @param <CONNECTION_HOLDER> 连接持有者类型
 * @author 刘镇 (suninformation@163.com) on 2019-05-16 02:02
 * @since 2.1.0
 */
@Ignored
public interface IPersistence<SESSION, CONFIG, CONNECTION_HOLDER extends IConnectionHolder> extends IInitialization<IApplication>, IDestroyable {

    /**
     * 获取所属应用容器
     *
     * @return 返回所属应用容器实例
     */
    IApplication getOwner();

    /**
     * 获取持久化配置对象
     *
     * @return 返回持久化配置对象
     */
    CONFIG getConfig();

    /**
     * 获取默认数据源连接持有者对象
     *
     * @return 返回默认数据源连接持有者对象
     * @throws Exception 可能产生的异常
     */
    CONNECTION_HOLDER getDefaultConnectionHolder() throws Exception;

    /**
     * 获取由dataSourceName指定的数据源连接持有者对象
     *
     * @param dataSourceName 数据源名称
     * @return 获取由指定的数据源连接持有者对象
     * @throws Exception 可能产生的异常
     */
    CONNECTION_HOLDER getConnectionHolder(String dataSourceName) throws Exception;

    /**
     * 安全关闭数据源的连接持有者(确保非事务状态下执行关闭)
     *
     * @param connectionHolder 数据源的连接持有者对象
     * @throws Exception 可能产生的异常
     */
    void releaseConnectionHolder(CONNECTION_HOLDER connectionHolder) throws Exception;

    /**
     * 开启数据库连接会话(注意一定记得关闭会话)
     *
     * @return 返回会话对象
     * @throws Exception 可能产生的异常
     */
    SESSION openSession() throws Exception;

    /**
     * 开启数据库连接会话(注意一定记得关闭会话)
     *
     * @param dataSourceName 数据源名称
     * @return 返回会话对象
     * @throws Exception 可能产生的异常
     */
    SESSION openSession(String dataSourceName) throws Exception;

    /**
     * 开启数据库连接会话(注意一定记得关闭会话)
     *
     * @param connectionHolder 数据源连接持有者对象
     * @return 返回会话对象
     * @throws Exception 可能产生的异常
     */
    SESSION openSession(CONNECTION_HOLDER connectionHolder) throws Exception;

    /**
     * 开启数据库连接会话(注意一定记得关闭会话)
     *
     * @param dataSourceRouter 数据源路由对象
     * @return 返回会话对象
     * @throws Exception 可能产生的异常
     */
    SESSION openSession(IDataSourceRouter dataSourceRouter) throws Exception;
}
