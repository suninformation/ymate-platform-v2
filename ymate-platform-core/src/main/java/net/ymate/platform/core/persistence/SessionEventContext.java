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

import net.ymate.platform.core.persistence.base.Type;

import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * 会话事件处理上下文
 *
 * @author 刘镇 (suninformation@163.com) on 2014年3月12日 下午4:17:32
 */
public class SessionEventContext extends EventObject {

    private static final long serialVersionUID = 1L;

    private final Type.OPT operationType;

    private final Map<String, Object> attributes = new HashMap<>();

    public SessionEventContext(ISession<?, ?> source, Type.OPT operationType) {
        super(source);
        this.operationType = operationType;
    }

    public Type.OPT getOperationType() {
        return operationType;
    }

    @Override
    public ISession<?, ?> getSource() {
        return (ISession<?, ?>) super.getSource();
    }

    /**
     * @since 2.1.4
     */
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * @since 2.1.4
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * @since 2.1.4
     */
    public SessionEventContext putAttribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }
}
