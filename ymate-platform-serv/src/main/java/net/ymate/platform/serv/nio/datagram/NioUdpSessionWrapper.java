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
package net.ymate.platform.serv.nio.datagram;

import net.ymate.platform.serv.AbstractSessionWrapper;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/15 1:47 AM
 * @version 1.0
 */
public class NioUdpSessionWrapper extends AbstractSessionWrapper<NioUdpSession, InetSocketAddress> {

    private InetSocketAddress __id;

    private NioUdpSession __session;

    private InetSocketAddress __socketAddress;

    private long __lastTouchTime;

    private final ConcurrentMap<String, Object> __attributes;

    public NioUdpSessionWrapper(NioUdpSession session, InetSocketAddress socketAddress) {
        __id = socketAddress;
        __socketAddress = socketAddress;
        __session = session;
        __lastTouchTime = System.currentTimeMillis();
        __attributes = new ConcurrentHashMap<String, Object>();
    }

    @Override
    public InetSocketAddress getId() {
        return __id;
    }

    @Override
    public NioUdpSession getSession() {
        return __session;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return __attributes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String attrKey) {
        return (T) __attributes.get(attrKey);
    }

    @Override
    public void addAttribute(String attrKey, Object attrValue) {
        __attributes.put(attrKey, attrValue);
    }

    @Override
    public void touch() {
        __lastTouchTime = System.currentTimeMillis();
    }

    @Override
    public long getLastTouchTime() {
        return __lastTouchTime;
    }

    @Override
    public String toString() {
        return "NioSessionWrapper {"
                + "__session=" + " [id=" + __id + ", remote=" + (__socketAddress.getHostName() + ":" + __socketAddress.getPort()) + ", attrs=" + __attributes + "]"
                + ", __lastTouchTime=" + getLastTouchTime() + '}';
    }
}
