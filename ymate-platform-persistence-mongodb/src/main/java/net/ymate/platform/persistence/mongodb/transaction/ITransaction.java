/*
 * Copyright 2007-2021 the original author or authors.
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
package net.ymate.platform.persistence.mongodb.transaction;

import com.mongodb.client.ClientSession;
import net.ymate.platform.persistence.mongodb.IMongoConnectionHolder;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/10/16 11:11 下午
 * @since 2.1.0
 */
public interface ITransaction {

    /**
     * 获取事务唯一标识
     *
     * @return 返回事务唯一标识
     */
    String getId();

    /**
     * 提交事务
     */
    void commit();

    /**
     * 回滚事务
     */
    void rollback();

    /**
     * 关闭事务（连接）
     */
    void close();

    /**
     * 获取客户端会话对象
     *
     * @param dsName 数据源名称
     * @return 返回客户端会话对象
     */
    ClientSession getClientSession(String dsName);

    /**
     * 获取数据库连接持有者对象
     *
     * @param dsName 数据源名称
     * @return 返回数据库连接持有者对象
     */
    IMongoConnectionHolder getConnectionHolder(String dsName);
}
