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
package net.ymate.platform.serv.nio.datagram;

import net.ymate.platform.serv.AbstractSessionWrapper;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/11/15 1:47 AM
 */
public class NioUdpSessionWrapper extends AbstractSessionWrapper<NioUdpSession, InetSocketAddress> {

    private static final long serialVersionUID = 1L;

    private final InetSocketAddress id;

    private final NioUdpSession session;

    private final InetSocketAddress socketAddress;

    private long lastTouchTime;

    private final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();

    public NioUdpSessionWrapper(NioUdpSession session, InetSocketAddress socketAddress) {
        id = socketAddress;
        this.socketAddress = socketAddress;
        this.session = session;
        lastTouchTime = System.currentTimeMillis();
    }

    @Override
    public InetSocketAddress getId() {
        return id;
    }

    @Override
    public NioUdpSession getSession() {
        return session;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String attrKey) {
        return (T) attributes.get(attrKey);
    }

    @Override
    public void addAttribute(String attrKey, Object attrValue) {
        attributes.put(attrKey, attrValue);
    }

    @Override
    public void touch() {
        lastTouchTime = System.currentTimeMillis();
    }

    @Override
    public long getLastTouchTime() {
        return lastTouchTime;
    }

    @Override
    public String toString() {
        return String.format("NioSessionWrapper {session= [id=%s, remote=%s, attrs=%s], lastTouchTime=%d}", id, socketAddress.getHostName() + ":" + socketAddress.getPort(), attributes, getLastTouchTime());
    }
}
