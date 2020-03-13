/*
 * Copyright 2007-2020 the original author or authors.
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
package net.ymate.platform.commons.util;

import net.ymate.platform.commons.MathCalcHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 地理坐标多边形区域
 */
public class GeoPolygon implements Serializable {

    /**
     * 多边形坐标点列表
     */
    private final List<GeoPoint> points = new ArrayList<>();

    public GeoPolygon() {
    }

    public GeoPolygon(GeoPoint[] points) {
        if (ArrayUtils.isNotEmpty(points)) {
            this.points.addAll(Arrays.asList(points));
        }
    }

    public GeoPolygon(Collection<GeoPoint> points) {
        if (points != null && !points.isEmpty()) {
            this.points.addAll(points);
        }
    }

    public boolean isEmpty() {
        return this.points.isEmpty();
    }

    public GeoPolygon add(GeoPoint point) {
        if (point != null) {
            this.points.add(point);
        }
        return this;
    }

    public GeoPolygon add(double longitude, double latitude) {
        this.points.add(new GeoPoint(longitude, latitude));
        return this;
    }

    public List<GeoPoint> getPoints() {
        return points;
    }

    public boolean in(GeoPoint point) {
        int nCross = 0;
        for (int i = 0; i < points.size(); i++) {
            GeoPoint p1 = points.get(i);
            GeoPoint p2 = points.get((i + 1) % points.size());
            if (MathCalcHelper.eq(p1.getLatitude(), p2.getLatitude())) {
                continue;
            }
            if (point.getLatitude() < Math.min(p1.getLatitude(), p2.getLatitude())) {
                continue;
            }
            if (point.getLatitude() >= Math.max(p1.getLatitude(), p2.getLatitude())) {
                continue;
            }
            double x = (point.getLatitude() - p1.getLatitude()) * (p2.getLongitude() - p1.getLongitude()) / (p2.getLatitude() - p1.getLatitude()) + p1.getLongitude();
            if (x > point.getLongitude()) {
                nCross++;
            }
        }
        return (nCross % 2 == 1);
    }

    public boolean on(GeoPoint point) {
        for (int i = 0; i < points.size(); i++) {
            GeoPoint p1 = points.get(i);
            GeoPoint p2 = points.get((i + 1) % points.size());
            if (point.getLatitude() < Math.min(p1.getLatitude(), p2.getLatitude())) {
                continue;
            }
            if (point.getLatitude() > Math.max(p1.getLatitude(), p2.getLatitude())) {
                continue;
            }
            if (MathCalcHelper.eq(p1.getLatitude(), p2.getLatitude())) {
                double minLon = Math.min(p1.getLongitude(), p2.getLongitude());
                double maxLon = Math.max(p1.getLongitude(), p2.getLongitude());
                if (MathCalcHelper.eq(point.getLatitude(), p1.getLatitude()) && point.getLongitude() >= minLon && point.getLongitude() <= maxLon) {
                    return true;
                }
            } else {
                double x = (point.getLatitude() - p1.getLatitude()) * (p2.getLongitude() - p1.getLongitude()) / (p2.getLatitude() - p1.getLatitude()) + p1.getLongitude();
                if (MathCalcHelper.eq(x, point.getLongitude())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeoPolygon that = (GeoPolygon) o;
        return new EqualsBuilder()
                .append(points, that.points)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(points)
                .toHashCode();
    }

    @Override
    public String toString() {
        return String.format("GeoPolygon{points=%s}", points);
    }
}
