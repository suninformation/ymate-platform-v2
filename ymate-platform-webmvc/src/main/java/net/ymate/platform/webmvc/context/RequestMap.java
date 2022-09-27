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
package net.ymate.platform.webmvc.context;

import javax.servlet.http.HttpServletRequest;
import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;


/**
 * @author 刘镇 (suninformation@163.com) on 2011-7-24 下午10:31:48
 */
@SuppressWarnings("rawtypes")
public class RequestMap extends AbstractMap {

    private Set<Object> entries;

    private final HttpServletRequest request;

    public RequestMap(final HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public void clear() {
        entries = null;
        Enumeration keys = request.getAttributeNames();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            request.removeAttribute(key);
        }
    }

    @Override
    public Set entrySet() {
        if (entries == null) {
            entries = new HashSet<>();
            Enumeration enumeration = request.getAttributeNames();
            while (enumeration.hasMoreElements()) {
                final String key = enumeration.nextElement().toString();
                entries.add(new AbstractEntry<String, Object>(key, request.getAttribute(key)) {
                    @Override
                    public Object setValue(Object value) {
                        request.setAttribute(key, value);
                        return value;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return super.equals(obj);
                    }

                    @Override
                    public int hashCode() {
                        return super.hashCode();
                    }
                });
            }
        }
        return entries;
    }

    @Override
    public Object get(Object key) {
        return request.getAttribute(key.toString());
    }

    @Override
    public Object put(Object key, Object value) {
        Object oldValue = get(key);
        entries = null;
        request.setAttribute(key.toString(), value);
        return oldValue;
    }

    @Override
    public Object remove(Object key) {
        entries = null;
        Object value = get(key);
        request.removeAttribute(key.toString());
        return value;
    }
}
