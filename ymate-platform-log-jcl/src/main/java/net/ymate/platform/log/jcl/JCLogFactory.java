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
package net.ymate.platform.log.jcl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.spi.LoggerAdapter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 刘镇 (suninformation@163.com) on 15/9/28 21:59
 * @version 1.0
 */
public class JCLogFactory extends LogFactory {

    private final LoggerAdapter<Log> adapter = new JCLoggerAdapter();

    private final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    @Override
    public Log getInstance(final String name) throws LogConfigurationException {
        return adapter.getLogger(name);
    }

    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }

    @Override
    public String[] getAttributeNames() {
        return attributes.keySet().toArray(new String[attributes.size()]);
    }

    @Override
    public Log getInstance(@SuppressWarnings("rawtypes") final Class clazz) throws LogConfigurationException {
        return getInstance(clazz.getName());
    }

    /**
     * This method is supposed to clear all loggers. In this implementation it will clear all the logger
     * wrappers but the loggers managed by the underlying logger context will not be.
     */
    @Override
    public void release() {
        try {
            adapter.close();
        } catch (final IOException ignored) {
        }
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
