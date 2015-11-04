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
package net.ymate.platform.log.slf4j;

import org.apache.logging.log4j.ThreadContext;
import org.slf4j.spi.MDCAdapter;

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/4 下午12:02
 * @version 1.0
 */
public class Log4jMDCAdapter implements MDCAdapter {

    public void put(final String key, final String val) {
        ThreadContext.put(key, val);
    }

    public String get(final String key) {
        return ThreadContext.get(key);
    }

    public void remove(final String key) {
        ThreadContext.remove(key);
    }

    public void clear() {
        ThreadContext.clearMap();
    }

    public Map<String, String> getCopyOfContextMap() {
        return ThreadContext.getContext();
    }

    @SuppressWarnings("unchecked")
    public void setContextMap(@SuppressWarnings("rawtypes") final Map map) {
        ThreadContext.clearMap();
        for (final Map.Entry<String, String> entry : ((Map<String, String>) map).entrySet()) {
            ThreadContext.put(entry.getKey(), entry.getValue());
        }
    }
}
