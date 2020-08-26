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
 * 连接对象持有者接口，用于记录连接的原始状态及与数据源对应关系
 *
 * @param <CONNECTION>        连接对象类型
 * @param <DATASOURCE_CONFIG> 数据源配置类型
 * @author 刘镇 (suninformation@163.com) on 2019-05-16 01:06
 * @since 2.1.0
 */
@Ignored
@SuppressWarnings("rawtypes")
public interface IConnectionHolder<OWNER extends IPersistence, CONNECTION, DATASOURCE_CONFIG extends IDataSourceConfig> extends IDestroyable {

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
     */
    CONNECTION getConnection();
}
