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
package net.ymate.platform.serv.impl;

import net.ymate.platform.serv.ISessionIdleChecker;
import net.ymate.platform.serv.ISessionManager;
import net.ymate.platform.serv.ISessionWrapper;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * @param <SESSION_WRAPPER> 会话包装类型
 * @param <SESSION_ID>      会话标识类型
 * @param <MESSAGE_TYPE>    消息类型
 * @author 刘镇 (suninformation@163.com) on 2018/11/22 2:27 AM
 */
public class DefaultSessionIdleChecker<SESSION_WRAPPER extends ISessionWrapper<?, ?>, SESSION_ID extends Serializable, MESSAGE_TYPE> implements ISessionIdleChecker<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> {

    private ISessionManager<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> sessionManager;

    private boolean initialized;

    @Override
    public void initialize(ISessionManager<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> sessionManager) {
        this.sessionManager = sessionManager;
        initialized = true;
    }

    @Override
    public ISessionManager<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> getSessionManager() {
        return sessionManager;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void processIdleSession(Map<SESSION_ID, SESSION_WRAPPER> sessions, long idleTimeInMillis) {
        Iterator<Map.Entry<SESSION_ID, SESSION_WRAPPER>> iterator = sessions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<SESSION_ID, SESSION_WRAPPER> entry = iterator.next();
            if (System.currentTimeMillis() - entry.getValue().getLastTouchTime() > idleTimeInMillis) {
                iterator.remove();
                //
                sessionManager.closeSessionWrapper(entry.getValue());
                sessionManager.getSessionListener().onSessionIdleRemoved(entry.getValue());
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            initialized = false;
            sessionManager = null;
        }
    }
}
