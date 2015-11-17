/*
 * Copyright 2007-2016 the original author or authors.
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
package net.ymate.platform.serv.nio.datagram;

import net.ymate.platform.serv.AbstractListener;
import net.ymate.platform.serv.nio.INioSession;

import java.io.IOException;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/17 下午4:44
 * @version 1.0
 */
public class NioUdpListener extends AbstractListener<INioSession> {

    public void onSessionRegisted(INioSession session) throws IOException {
    }

    public final void onSessionConnected(INioSession session) throws IOException {
    }

    public final void onSessionAccepted(INioSession session) throws IOException {
    }

    public void onBeforeSessionClosed(INioSession session) throws IOException {
    }

    public void onAfterSessionClosed(INioSession session) throws IOException {
    }

    public void onMessageReceived(Object message, INioSession session) throws IOException {
    }
}
