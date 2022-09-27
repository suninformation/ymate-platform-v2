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

import net.ymate.platform.webmvc.base.Type;

import javax.servlet.jsp.PageContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;


/**
 * @author 刘镇 (suninformation@163.com) on 2011-7-24 下午10:31:48
 */
@SuppressWarnings("rawtypes")
public class AttributeMap implements Map {

    private static final String UNSUPPORTED = "method makes no sense for a simplified map";

    private final Map context;

    public AttributeMap(Map context) {
        this.context = context;
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public boolean containsKey(Object key) {
        return (get(key) != null);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public Set entrySet() {
        return Collections.emptySet();
    }

    @Override
    public Object get(Object key) {
        PageContext pc = getPageContext();
        if (pc == null) {
            Map request = (Map) context.get("request");
            Map session = (Map) context.get("session");
            Map application = (Map) context.get("application");
            if ((request != null) && (request.get(key) != null)) {
                return request.get(key);
            } else if ((session != null) && (session.get(key) != null)) {
                return session.get(key);
            } else if ((application != null) && (application.get(key) != null)) {
                return application.get(key);
            }
        } else {
            try {
                return pc.findAttribute(key.toString());
            } catch (NullPointerException npe) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Set keySet() {
        return Collections.emptySet();
    }

    @Override
    public Object put(Object key, Object value) {
        PageContext pc = getPageContext();
        if (pc != null) {
            pc.setAttribute(key.toString(), value);
        }
        return null;
    }

    @Override
    public void putAll(Map t) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public Collection values() {
        return Collections.emptySet();
    }

    private PageContext getPageContext() {
        return (PageContext) context.get(Type.Context.PAGE_CONTEXT);
    }
}
