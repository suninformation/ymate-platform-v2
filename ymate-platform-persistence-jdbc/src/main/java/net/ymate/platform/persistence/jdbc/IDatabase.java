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
package net.ymate.platform.persistence.jdbc;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.IDataSourceRouter;
import net.ymate.platform.core.persistence.IPersistence;

/**
 * JDBC数据库管理器接口
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/29 下午7:06
 */
@Ignored
public interface IDatabase extends IPersistence<IDatabaseSession, IDatabaseConfig, IDatabaseConnectionHolder> {

    String MODULE_NAME = "persistence.jdbc";

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param executor 会话执行器
     * @param <T>      执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openSession(IDatabaseSessionExecutor<T> executor) throws Exception;

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param dataSourceName 数据源名称
     * @param executor       会话执行器
     * @param <T>            执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openSession(String dataSourceName, IDatabaseSessionExecutor<T> executor) throws Exception;

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param connectionHolder 数据源连接持有者对象
     * @param executor         会话执行器
     * @param <T>              执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openSession(IDatabaseConnectionHolder connectionHolder, IDatabaseSessionExecutor<T> executor) throws Exception;

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param dataSourceRouter 数据源路由对象
     * @param executor         会话执行器
     * @param <T>              执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openSession(IDataSourceRouter dataSourceRouter, IDatabaseSessionExecutor<T> executor) throws Exception;
}
