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

import net.ymate.platform.core.event.AbstractEventContext;
import net.ymate.platform.core.event.IEvent;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/12/13 上午11:50
 */
public class DatabaseEvent extends AbstractEventContext<IDatabase, DatabaseEvent.EVENT> implements IEvent {

    /**
     * Database事件枚举
     */
    public enum EVENT {

        /**
         * 执行查询操作后
         */
        QUERY_AFTER,

        /**
         * 执行插入操作后
         */
        INSERT_AFTER,

        /**
         * 执行更新操作后
         */
        UPDATE_AFTER,

        /**
         * 执行删除操作后
         */
        REMOVE_AFTER
    }

    public DatabaseEvent(IDatabase owner, EVENT eventName) {
        super(owner, DatabaseEvent.class, eventName);
    }
}
