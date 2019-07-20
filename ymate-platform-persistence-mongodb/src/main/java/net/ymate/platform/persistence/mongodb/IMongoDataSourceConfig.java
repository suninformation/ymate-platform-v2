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
package net.ymate.platform.persistence.mongodb;

import com.mongodb.ServerAddress;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.IDataSourceConfig;

import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-07-07 00:46
 * @since 2.1.0
 */
@Ignored
public interface IMongoDataSourceConfig extends IDataSourceConfig<IMongo> {

    /**
     * 集合前缀名称，可选参数，默认为空
     *
     * @return 返回前缀名称
     */
    String getCollectionPrefix();

    /**
     * 服务器主机连接字符串，可选参数，若提供此参数则下面的servers等参数就不在需要提供
     *
     * @return 返回连接字符串
     */
    String getConnectionUrl();

    /**
     * 服务器主机集合
     *
     * @return 返回主机集合
     */
    List<ServerAddress> getServerAddresses();

    /**
     * 默认数据库名称，必填参数
     *
     * @return 返回数据库名称
     */
    String getDatabaseName();

    /**
     * 自定义MongoDB客户端参数配置处理器
     *
     * @return 返回客户端参数配置处理器类型
     */
    Class<? extends IMongoClientOptionsHandler> getClientOptionsHandlerClass();
}
