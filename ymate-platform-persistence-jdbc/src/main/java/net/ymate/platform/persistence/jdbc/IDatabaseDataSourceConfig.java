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
import net.ymate.platform.core.persistence.IDataSourceConfig;

import java.io.File;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-05-16 01:56
 * @since 2.1.0
 */
@Ignored
public interface IDatabaseDataSourceConfig extends IDataSourceConfig<IDatabase> {

    /**
     * 是否显示执行的SQL语句，默认为false
     *
     * @return 返回true表示显示
     */
    boolean isShowSql();

    /**
     * 是否开启堆栈跟踪，默认为false
     *
     * @return 返回true表示开启
     */
    boolean isStackTraces();

    /**
     * 堆栈跟踪层级深度，默认为0(即全部)
     *
     * @return 返回层级深度值
     */
    int getStackTraceDepth();

    /**
     * 堆栈跟踪包名前缀过滤，多个包名之间用'|'分隔，默认为空
     *
     * @return 返回包名称
     */
    String getStackTracePackages();

    /**
     * 数据库表前缀名称，默认为空
     *
     * @return 返回表前缀名称
     */
    String getTablePrefix();

    /**
     * 自定义引用标识符
     *
     * @return 返回自定义引用标识符
     */
    String getIdentifierQuote();

    /**
     * 数据源适配器
     *
     * @return 返回数据源适配器类型
     */
    Class<? extends IDatabaseDataSourceAdapter> getAdapterClass();

    /**
     * 获取数据源适配器配置文件，可选参数，若未设置或设置的文件路径无效将被忽略，默认值为空
     *
     * @return 返回数据源适配器配置文件
     */
    File getConfigFile();

    /**
     * 数据库类型，可选参数，默认值将通过连接字符串分析获得
     *
     * @return 返回数据库类型
     */
    String getType();

    /**
     * 数据库方言，可选参数，自定义方言将覆盖默认配置
     *
     * @return 返回数据库方言
     */
    String getDialectClass();

    /**
     * 数据库连接驱动，可选参数，框架默认将根据数据库类型进行自动匹配
     *
     * @return 返回数据库连接驱动类名称
     */
    String getDriverClass();

    /**
     * 数据库连接字符串，必填参数
     *
     * @return 返回数据库连接串
     */
    String getConnectionUrl();
}
