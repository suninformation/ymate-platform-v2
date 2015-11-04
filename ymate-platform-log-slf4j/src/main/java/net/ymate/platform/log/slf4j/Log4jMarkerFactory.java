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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.status.StatusLogger;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/4 上午11:30
 * @version 1.0
 */
public class Log4jMarkerFactory implements IMarkerFactory {

    private static final Logger LOGGER = StatusLogger.getLogger();

    private final ConcurrentMap<String, Marker> markerMap = new ConcurrentHashMap<String, Marker>();

    public Marker getMarker(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Marker name must not be null");
        }
        final Marker marker = markerMap.get(name);
        if (marker != null) {
            return marker;
        }
        final org.apache.logging.log4j.Marker log4jMarker = MarkerManager.getMarker(name);
        return addMarkerIfAbsent(name, log4jMarker);
    }

    private Marker addMarkerIfAbsent(final String name, final org.apache.logging.log4j.Marker log4jMarker) {
        final Marker marker = new Log4jMarker(log4jMarker);
        final Marker existing = markerMap.putIfAbsent(name, marker);
        return existing == null ? marker : existing;
    }

    public Marker getMarker(final Marker marker) {
        if (marker == null) {
            throw new IllegalArgumentException("Marker must not be null");
        }
        final Marker m = markerMap.get(marker.getName());
        if (m != null) {
            return m;
        }
        return addMarkerIfAbsent(marker.getName(), convertMarker(marker));
    }

    private static org.apache.logging.log4j.Marker convertMarker(final Marker original) {
        if (original == null) {
            throw new IllegalArgumentException("Marker must not be null");
        }
        return convertMarker(original, new ArrayList<Marker>());
    }

    private static org.apache.logging.log4j.Marker convertMarker(final Marker original,
                                                                 final Collection<Marker> visited) {
        final org.apache.logging.log4j.Marker marker = MarkerManager.getMarker(original.getName());
        if (original.hasReferences()) {
            final Iterator it = original.iterator();
            while (it.hasNext()) {
                final Marker next = (Marker) it.next();
                if (visited.contains(next)) {
                    LOGGER.warn("Found a cycle in Marker [{}]. Cycle will be broken.", next.getName());
                } else {
                    visited.add(next);
                    marker.addParents(convertMarker(next, visited));
                }
            }
        }
        return marker;
    }

    public boolean exists(final String name) {
        return markerMap.containsKey(name);
    }

    public boolean detachMarker(final String name) {
        return false;
    }

    public Marker getDetachedMarker(final String name) {
        LOGGER.warn("Log4j does not support detached Markers. Returned Marker [{}] will be unchanged.", name);
        return getMarker(name);
    }
}
