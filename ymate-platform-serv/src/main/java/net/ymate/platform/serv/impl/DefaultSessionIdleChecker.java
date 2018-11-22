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

import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.serv.AbstractSessionIdleChecker;
import net.ymate.platform.serv.ISessionWrapper;
import net.ymate.platform.serv.nio.INioSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/22 2:27 AM
 * @version 1.0
 */
public class DefaultSessionIdleChecker<SESSION_WRAPPER extends ISessionWrapper, SESSION_ID, MESSAGE_TYPE> extends AbstractSessionIdleChecker<SESSION_WRAPPER, SESSION_ID, MESSAGE_TYPE> {

    private static final Log _LOG = LogFactory.getLog(DefaultSessionIdleChecker.class);

    @Override
    protected void __doCloseSession(SESSION_WRAPPER sessionWrapper) {
        if (sessionWrapper.getSession() instanceof INioSession) {
            INioSession _session = (INioSession) sessionWrapper.getSession();
            if (!_session.isUdp()) {
                try {
                    _session.closeNow();
                } catch (IOException e) {
                    if (_LOG.isDebugEnabled()) {
                        _LOG.debug("Session close exception: ", RuntimeUtils.unwrapThrow(e));
                    }
                }
            }
        }
    }
}
