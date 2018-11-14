/*
 * Copyright 2007-2017 the original author or authors.
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
 * <p>
 * SessionMap
 * </p>
 * <p>
 * A simple implementation of the {@link Map} interface to handle a collection of HTTP session
 * attributes. The {@link #entrySet()} method enumerates over all session attributes and creates a Set of entries.
 * Note, this will occur lazily - only when the entry set is asked for.
 * </p>
 */
public class SessionMap extends AbstractMap<String, Object> implements Serializable {

    private static final long serialVersionUID = 4678843241638046854L;

    protected HttpSession session;
    protected Set<Entry<String, Object>> entries;
    protected HttpServletRequest request;

    /**
     * Creates a new session map given a http servlet request. Note, ths enumeration of request
     * attributes will occur when the map entries are asked for.
     *
     * @param request the http servlet request object.
     */
    public SessionMap(HttpServletRequest request) {
        // note, holding on to this request and relying on lazy session initalization will not work
        // if you are running your action invocation in a background task, such as using the "execAndWait" interceptor
        this.request = request;
        this.session = request.getSession(false);
    }

    /**
     * Invalidate the http session.
     */
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

    /**
     * Removes all attributes from the session as well as clears entries in this
     * map.
     */
    @Override
    @SuppressWarnings("unchecked")
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

    /**
     * Returns a Set of attributes from the http session.
     *
     * @return a Set of attributes from the http session.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Set<Entry<String, Object>> entrySet() {
        if (session == null) {
            return Collections.emptySet();
        }
        synchronized (session) {
            if (entries == null) {
                entries = new HashSet<Entry<String, Object>>();
                Enumeration enumeration = session.getAttributeNames();
                while (enumeration.hasMoreElements()) {
                    final String key = enumeration.nextElement().toString();
                    final Object value = session.getAttribute(key);
                    entries.add(new WebContext.AbstractEntry<String, Object>(key, value) {
                        @Override
                        public Object setValue(Object value) {
                            session.setAttribute(key, value);
                            return value;
                        }
                    });
                }
            }
        }
        return entries;
    }

    /**
     * Returns the session attribute associated with the given key or <tt>null</tt> if it doesn't exist.
     *
     * @param key the name of the session attribute.
     * @return the session attribute or <tt>null</tt> if it doesn't exist.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object get(Object key) {
        if (session == null) {
            return null;
        }
        synchronized (session) {
            return session.getAttribute(key.toString());
        }
    }

    /**
     * Saves an attribute in the session.
     *
     * @param key   the name of the session attribute.
     * @param value the value to set.
     * @return the object that was just set.
     */
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

    /**
     * Removes the specified session attribute.
     *
     * @param key the name of the attribute to remove.
     * @return the value that was removed or <tt>null</tt> if the value was not found (and hence, not removed).
     */
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


    /**
     * Checks if the specified session attribute with the given key exists.
     *
     * @param key the name of the session attribute.
     * @return <tt>true</tt> if the session attribute exits or <tt>false</tt> if it doesn't exist.
     */
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
