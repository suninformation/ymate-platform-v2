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
package net.ymate.platform.serv.impl;

import net.ymate.platform.serv.ISessionIdleChecker;
import net.ymate.platform.serv.ISessionManager;
import net.ymate.platform.serv.ISessionWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/22 2:27 AM
 * @version 1.0
 */
public class DefaultSessionIdleChecker<SESSION_WRAPPER extends ISessionWrapper, SESSION_ID, MESSAGE_TYPE> implements ISessionIdleChecker<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> {

    private static final Log _LOG = LogFactory.getLog(DefaultSessionIdleChecker.class);

    private ISessionManager<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> __sessionManager;

    private boolean __inited;

    @Override
    public void init(ISessionManager<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> sessionManager) {
        __sessionManager = sessionManager;
        __inited = true;
    }

    @Override
    public ISessionManager<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> getSessionManager() {
        return __sessionManager;
    }

    @Override
    public boolean isInited() {
        return __inited;
    }

    @Override
    public void processIdleSession(Map<SESSION_ID, SESSION_WRAPPER> sessions, long idleTimeInMillis) {
        Iterator<Map.Entry<SESSION_ID, SESSION_WRAPPER>> _iterator = sessions.entrySet().iterator();
        while (_iterator.hasNext()) {
            Map.Entry<SESSION_ID, SESSION_WRAPPER> _entry = _iterator.next();
            if (System.currentTimeMillis() - _entry.getValue().getLastTouchTime() > idleTimeInMillis) {
                _iterator.remove();
                //
                getSessionManager().closeSessionWrapper(_entry.getValue());
                //
                if (_LOG.isDebugEnabled()) {
                    _LOG.debug(_entry.getValue() + " - Session idle removed. Session count: " + this.getSessionManager().sessionCount());
                }
                this.getSessionManager().sessionListener().onSessionIdleRemoved(_entry.getValue());
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        if (__inited) {
            __inited = false;
            __sessionManager = null;
        }
    }
}
