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
package net.ymate.platform.log.jcl;

import net.ymate.platform.commons.ReentrantLockHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 刘镇 (suninformation@163.com) on 15/9/28 21:59
 */
public class JCLogFactory extends LogFactory {

    private static final Map<String, Log> LOGGER_CACHE = new ConcurrentHashMap<>();

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    @Override
    public Log getInstance(final String name) throws LogConfigurationException {
        try {
            return ReentrantLockHelper.putIfAbsentAsync(LOGGER_CACHE, name, () -> new JCLogger(name));
        } catch (Exception e) {
            throw new LogConfigurationException(e);
        }
    }

    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }

    @Override
    public String[] getAttributeNames() {
        return attributes.keySet().toArray(new String[0]);
    }

    @Override
    public Log getInstance(@SuppressWarnings("rawtypes") final Class clazz) throws LogConfigurationException {
        return getInstance(clazz.getName());
    }

    @Override
    public void release() {
        LOGGER_CACHE.clear();
    }

    @Override
    public void removeAttribute(final String name) {
        attributes.remove(name);
    }

    @Override
    public void setAttribute(final String name, final Object value) {
        if (value != null) {
            attributes.put(name, value);
        } else {
            removeAttribute(name);
        }
    }
}
