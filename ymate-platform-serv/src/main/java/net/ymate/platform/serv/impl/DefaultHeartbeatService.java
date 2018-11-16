/*
 * Copyright 2007-2017 the original author or authors.
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

import net.ymate.platform.serv.AbstractHeartbeatService;
import org.apache.commons.lang.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/19 下午3:07
 * @version 1.0
 */
public class DefaultHeartbeatService extends AbstractHeartbeatService<String> {

    private String __heartbeatMessage;

    @Override
    protected boolean __doStart() {
        __heartbeatMessage = StringUtils.defaultIfBlank(getClient().clientCfg().getParam("heartbeat_message"), "0");
        return super.__doStart();
    }

    @Override
    public String getHeartbeatPacket() {
        return __heartbeatMessage;
    }
}
