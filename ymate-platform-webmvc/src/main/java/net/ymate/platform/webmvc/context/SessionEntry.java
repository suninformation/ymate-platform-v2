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
package net.ymate.platform.webmvc.context;

import javax.servlet.http.HttpSession;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-07-07 19:45
 */
class SessionEntry extends AbstractEntry<String, Object> {

    private final HttpSession session;

    SessionEntry(HttpSession session, String key, Object value) {
        super(key, value);
        this.session = session;
    }

    @Override
    public Object setValue(Object value) {
        session.setAttribute(getKey(), value);
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
