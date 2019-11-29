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

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.support.IDestroyable;

/**
 * 基准数据源适配器接口
 *
 * @param <OWNER>             拥有者对象类型
 * @param <DATASOURCE_CONFIG> 数据源配置对象类型
 * @param <CONNECTION>        连接对象类型
 * @author 刘镇 (suninformation@163.com) on 2019-05-16 01:26
 * @since 2.1.0
 */
@Ignored
public interface IDataSourceAdapter<OWNER, DATASOURCE_CONFIG extends IDataSourceConfig, CONNECTION> extends IDestroyable {

    /**
     * 数据源适配器初始化
     *
     * @param owner            所属拥有者对象
     * @param dataSourceConfig 数据源配置参数
     * @throws Exception 可能产生的异常
     */
    void initialize(OWNER owner, DATASOURCE_CONFIG dataSourceConfig) throws Exception;

    /**
     * 判断是否已初始化, 若尚未执行则尝试初始化
     *
     * @return 返回true表示已初始化
     * @throws Exception 可能产生的异常
     */
    boolean initializeIfNeed() throws Exception;

    /**
     * 获取所属持久化模块
     *
     * @return 返回所属持久化模块对象
     */
    OWNER getOwner();

    /**
     * 获取数据源配置
     *
     * @return 返回数据源配置
     */
    DATASOURCE_CONFIG getDataSourceConfig();

    /**
     * 获取连接对象
     *
     * @return 返回连接对象
     * @throws Exception 可能产生的异常
     */
    CONNECTION getConnection() throws Exception;
}
