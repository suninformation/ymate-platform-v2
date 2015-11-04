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

import org.apache.logging.log4j.MarkerManager;
import org.slf4j.IMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.impl.StaticMarkerBinder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/4 下午12:02
 * @version 1.0
 */
public class Log4jMarker implements Marker {

    public static final long serialVersionUID = 1L;

    private final IMarkerFactory factory = StaticMarkerBinder.SINGLETON.getMarkerFactory();

    private final org.apache.logging.log4j.Marker marker;

    public Log4jMarker(final org.apache.logging.log4j.Marker marker) {
        this.marker = marker;
    }

    public org.apache.logging.log4j.Marker getLog4jMarker() {
        return marker;
    }

    public void add(final Marker marker) {
        final Marker m = factory.getMarker(marker.getName());
        this.marker.addParents(((Log4jMarker) m).getLog4jMarker());
    }

    public boolean remove(final Marker marker) {
        return this.marker.remove(MarkerManager.getMarker(marker.getName()));
    }

    public String getName() {
        return marker.getName();
    }

    public boolean hasReferences() {
        return marker.hasParents();
    }

    public boolean hasChildren() {
        return marker.hasParents();
    }

    @SuppressWarnings("rawtypes")
    public Iterator iterator() {
        final List<Marker> parents = new ArrayList<Marker>();
        for (final org.apache.logging.log4j.Marker m : this.marker.getParents()) {
            parents.add(factory.getMarker(m.getName()));
        }
        return parents.iterator();
    }

    public boolean contains(final org.slf4j.Marker marker) {
        return this.marker.isInstanceOf(marker.getName());
    }

    public boolean contains(final String s) {
        return this.marker.isInstanceOf(s);
    }
}
