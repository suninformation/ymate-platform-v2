/*
 * Copyright 2007-present the original author or authors.
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

import net.ymate.platform.core.persistence.AbstractSessionEventListener;

/**
 * 数据库会话事件监听器实现
 *
 * @author 刘镇 (suninformation@163.com) on 2026/3/24 10:34
 * @since 2.1.4
 */
public final class DatabaseSessionEventListener extends AbstractSessionEventListener<IDatabaseSessionEventListener, DatabaseSessionEventContext> implements IDatabaseSessionEventListener {

    public DatabaseSessionEventListener(IDatabaseSessionEventListener... listeners) {
        super(listeners);
    }

    public DatabaseSessionEventListener addListener(IDatabaseSessionEventListener... listeners) {
        super.addListener(listeners);
        return this;
    }

    public DatabaseSessionEventListener addListener(Iterable<IDatabaseSessionEventListener> listeners) {
        super.addListener(listeners);
        return this;
    }
}