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
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.*;


/**
 * @author 刘镇 (suninformation@163.com) on 2011-7-24 下午10:31:48
 */
public class SessionMap extends AbstractMap<String, Object> implements Serializable {

    private static final long serialVersionUID = 4678843241638046854L;

    protected HttpSession session;
    private Set<Entry<String, Object>> entries;
    protected HttpServletRequest request;

    public SessionMap(HttpServletRequest request) {
        this.request = request;
        this.session = request.getSession(false);
    }

    public void invalidate() {
        if (session == null) {
            return;
        }
        synchronized (session) {
            session.invalidate();
            session = null;
            entries = null;
        }
    }

    @Override
    public void clear() {
        if (session == null) {
            return;
        }
        synchronized (session) {
            entries = null;
            Enumeration<String> attributeNamesEnum = session.getAttributeNames();
            while (attributeNamesEnum.hasMoreElements()) {
                session.removeAttribute(attributeNamesEnum.nextElement());
            }
        }
    }


    @Override
    public Set<Entry<String, Object>> entrySet() {
        if (session == null) {
            return Collections.emptySet();
        }
        synchronized (session) {
            if (entries == null) {
                entries = new HashSet<>();
                Enumeration enumeration = session.getAttributeNames();
                while (enumeration.hasMoreElements()) {
                    final String key = enumeration.nextElement().toString();
                    entries.add(new SessionEntry(session, key, session.getAttribute(key)));
                }
            }
        }
        return entries;
    }

    @Override
    public Object get(Object key) {
        if (session == null) {
            return null;
        }
        synchronized (session) {
            return session.getAttribute(key.toString());
        }
    }

    @Override
    public Object put(String key, Object value) {
        synchronized (this) {
            if (session == null) {
                session = request.getSession(true);
            }
        }
        synchronized (session) {
            Object oldValue = get(key);
            entries = null;
            session.setAttribute(key, value);
            return oldValue;
        }
    }

    @Override
    public Object remove(Object key) {
        if (session == null) {
            return null;
        }
        synchronized (session) {
            entries = null;
            Object value = get(key);
            session.removeAttribute(key.toString());
            return value;
        }
    }

    @Override
    public boolean containsKey(Object key) {
        if (session == null) {
            return false;
        }
        synchronized (session) {
            return (session.getAttribute(key.toString()) != null);
        }
    }
}
