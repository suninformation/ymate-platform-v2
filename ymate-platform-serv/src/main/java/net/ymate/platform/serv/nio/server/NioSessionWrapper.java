/*
 * Copyright 2007-2018 the original author or authors.
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
package net.ymate.platform.serv.nio.server;

import net.ymate.platform.serv.AbstractSessionWrapper;
import net.ymate.platform.serv.nio.INioSession;

import java.util.Map;

/**
 * 会话包装器类
 *
 * @author 刘镇 (suninformation@163.com) on 2018/11/13 12:05 AM
 * @version 1.0
 */
public class NioSessionWrapper extends AbstractSessionWrapper<INioSession, String> {

    private INioSession __session;

    public NioSessionWrapper(INioSession session) {
        __session = session;
    }

    @Override
    public String getId() {
        return __session.id();
    }

    @Override
    public INioSession getSession() {
        return __session;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return __session.attrs();
    }

    @Override
    public <T> T getAttribute(String attrKey) {
        return __session.attr(attrKey);
    }

    @Override
    public void addAttribute(String attrKey, Object attrValue) {
        __session.attr(attrKey, attrValue);
    }

    @Override
    public void touch() {
        __session.touch();
    }

    @Override
    public long getLastTouchTime() {
        return __session.lastTouchTime();
    }

    @Override
    public String toString() {
        return "NioSessionWrapper {" + "__session=" + __session + ", __lastTouchTime=" + getLastTouchTime() + '}';
    }
}
