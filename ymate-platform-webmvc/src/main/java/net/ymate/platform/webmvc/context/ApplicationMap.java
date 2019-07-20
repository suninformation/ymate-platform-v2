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

import javax.servlet.ServletContext;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

/**
 * @author 刘镇 (suninformation@163.com) on 2011-7-24 下午10:31:48
 */
@SuppressWarnings("rawtypes")
public class ApplicationMap extends AbstractMap implements Serializable {

    private static final long serialVersionUID = 9136809763083228202L;

    private final ServletContext context;

    private Set<Object> entries;

    public ApplicationMap(ServletContext ctx) {
        this.context = ctx;
    }

    @Override
    public void clear() {
        entries = null;
        Enumeration e = context.getAttributeNames();
        while (e.hasMoreElements()) {
            context.removeAttribute(e.nextElement().toString());
        }
    }

    private void addEntry(Enumeration enumeration) {
        while (enumeration.hasMoreElements()) {
            final String key = enumeration.nextElement().toString();
            final Object value = context.getAttribute(key);
            entries.add(new AbstractEntry<String, Object>(key, value) {
                @Override
                public Object setValue(Object value) {
                    context.setAttribute(key, value);
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

    @Override
    public Set entrySet() {
        if (entries == null) {
            entries = new HashSet<>();
            addEntry(context.getAttributeNames());
            addEntry(context.getInitParameterNames());
        }
        return entries;
    }

    @Override
    public Object get(Object key) {
        String keyString = key.toString();
        Object value = context.getAttribute(keyString);
        return (value == null) ? context.getInitParameter(keyString) : value;
    }

    @Override
    public Object put(Object key, Object value) {
        Object oldValue = get(key);
        entries = null;
        context.setAttribute(key.toString(), value);
        return oldValue;
    }

    @Override
    public Object remove(Object key) {
        entries = null;
        Object value = get(key);
        context.removeAttribute(key.toString());
        return value;
    }
}
