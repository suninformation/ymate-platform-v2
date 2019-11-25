package org.slf4j.impl;

import org.apache.log4j.MDC;
import org.apache.log4j.MDCFriend;
import org.slf4j.spi.MDCAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Copied from org.slf4j:slf4j-log4j12-1.7.29
 *
 * @author QOS.ch
 */
public class Log4jMDCAdapter implements MDCAdapter {

    static {
        if (VersionUtil.getJavaMajorVersion() >= 9) {
            MDCFriend.fixForJava9();
        }
    }

    @Override
    public void clear() {
        @SuppressWarnings("rawtypes")
        Map map = org.apache.log4j.MDC.getContext();
        if (map != null) {
            map.clear();
        }
    }

    @Override
    public String get(String key) {
        return (String) org.apache.log4j.MDC.get(key);
    }

    @Override
    public void put(String key, String val) {
        org.apache.log4j.MDC.put(key, val);
    }

    @Override
    public void remove(String key) {
        org.apache.log4j.MDC.remove(key);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map getCopyOfContextMap() {
        Map old = org.apache.log4j.MDC.getContext();
        if (old != null) {
            return new HashMap(old);
        } else {
            return null;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void setContextMap(Map contextMap) {
        Map old = org.apache.log4j.MDC.getContext();
        if (old == null) {
            for (Object o : contextMap.entrySet()) {
                Map.Entry mapEntry = (Map.Entry) o;
                MDC.put((String) mapEntry.getKey(), mapEntry.getValue());
            }
        } else {
            old.clear();
            old.putAll(contextMap);
        }
    }
}
