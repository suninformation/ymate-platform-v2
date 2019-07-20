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
package net.ymate.platform.persistence.jdbc.base;

import net.ymate.platform.core.persistence.base.Type;

import java.sql.Statement;
import java.util.EventObject;

/**
 * 访问器配置事件处理上下文
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-30 上午10:05:44
 */
public class AccessorEventContext extends EventObject {

    private static final long serialVersionUID = 1L;

    private final Statement statement;

    private final Type.OPT operationType;

    public AccessorEventContext(Statement statement, Type.OPT operationType) {
        super(statement);
        this.statement = statement;
        this.operationType = operationType;
    }

    public Statement getStatement() {
        return statement;
    }

    public Type.OPT getOperationType() {
        return operationType;
    }
}
