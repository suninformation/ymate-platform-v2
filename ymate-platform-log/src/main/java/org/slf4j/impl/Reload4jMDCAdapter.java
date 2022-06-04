/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.slf4j.impl;

import org.slf4j.spi.MDCAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Copied from org.slf4j:slf4j-reload4j:1.7.36
 */
public class Reload4jMDCAdapter implements MDCAdapter {

    public void clear() {
        @SuppressWarnings("rawtypes")
        Map map = org.apache.log4j.MDC.getContext();
        if (map != null) {
            map.clear();
        }
    }

    public String get(String key) {
        return (String) org.apache.log4j.MDC.get(key);
    }

    /**
     * Put a context value (the <code>val</code> parameter) as identified with
     * the <code>key</code> parameter into the current thread's context map. The
     * <code>key</code> parameter cannot be null. Log4j does <em>not</em>
     * support null for the <code>val</code> parameter.
     *
     * <p>
     * This method delegates all work to log4j's MDC.
     *
     * @throws IllegalArgumentException in case the "key" or <b>"val"</b> parameter is null
     */
    public void put(String key, String val) {
        org.apache.log4j.MDC.put(key, val);
    }

    public void remove(String key) {
        org.apache.log4j.MDC.remove(key);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Map getCopyOfContextMap() {
        Map old = org.apache.log4j.MDC.getContext();
        if (old != null) {
            return new HashMap(old);
        } else {
            return null;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setContextMap(Map contextMap) {
        Map old = org.apache.log4j.MDC.getContext();
        if (old == null) {
            for (Object o : contextMap.entrySet()) {
                Map.Entry mapEntry = (Map.Entry) o;
                org.apache.log4j.MDC.put((String) mapEntry.getKey(), mapEntry.getValue());
            }
        } else {
            old.clear();
            old.putAll(contextMap);
        }
    }
}
