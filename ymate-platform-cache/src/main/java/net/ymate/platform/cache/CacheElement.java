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
package net.ymate.platform.cache;

import net.ymate.platform.commons.util.DateTimeUtils;

import java.io.Serializable;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/3 下午2:42
 */
public class CacheElement implements Serializable {

    private static final long serialVersionUID = 1L;

    private Object object;

    private long lastUpdateTime = System.currentTimeMillis();

    private int timeout;

    public CacheElement() {
    }

    public CacheElement(final Object object) {
        this.object = object;
    }

    public CacheElement(final Object object, final int timeout) {
        this(object);
        this.timeout = timeout;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public CacheElement touch() {
        lastUpdateTime = System.currentTimeMillis();
        return this;
    }

    public boolean isExpired() {
        return ((System.currentTimeMillis() - lastUpdateTime) >= timeout * DateTimeUtils.SECOND);
    }
}
