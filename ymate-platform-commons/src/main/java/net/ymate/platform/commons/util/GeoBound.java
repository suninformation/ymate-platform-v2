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

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * 地理坐标矩形区域
 */
public class GeoBound implements Serializable {

    /**
     * 左下(西南)角坐标点
     */
    private GeoPoint southWest;

    /**
     * 右上(东北)角坐标点
     */
    private GeoPoint northEast;

    /**
     * 空矩形
     */
    public GeoBound() {
    }

    /**
     * 取两个矩形区域的并集
     *
     * @param first 第一个矩形区域
     * @param other 另一个矩形区域
     */
    public GeoBound(GeoBound first, GeoBound other) {
        if (first == null || first.isEmpty()) {
            throw new NullArgumentException("first");
        }
        if (other == null || other.isEmpty()) {
            throw new NullArgumentException("other");
        }
        this.southWest = new GeoPoint(Math.min(first.southWest.getLongitude(), other.southWest.getLongitude()), Math.min(first.southWest.getLatitude(), other.southWest.getLatitude()));
        //
        this.northEast = new GeoPoint(Math.max(first.northEast.getLongitude(), other.northEast.getLongitude()), Math.max(first.northEast.getLatitude(), other.northEast.getLatitude()));
    }

    public GeoBound(GeoPoint southWest, GeoPoint northEast) {
        this.southWest = southWest;
        this.northEast = northEast;
    }

    public GeoPoint getSouthWest() {
        return southWest;
    }

    public void setSouthWest(GeoPoint southWest) {
        this.southWest = southWest;
    }

    public GeoPoint getNorthEast() {
        return northEast;
    }

    public void setNorthEast(GeoPoint northEast) {
        this.northEast = northEast;
    }

    /**
     * @return 返回矩形的中心点
     */
    public GeoPoint getCenter() {
        return new GeoPoint((southWest.getLongitude() + northEast.getLongitude()) / 2, (southWest.getLatitude() + northEast.getLatitude()) / 2);
    }

    /**
     * @return 矩形区域是否为空
     */
    public boolean isEmpty() {
        return southWest == null || northEast == null;
    }

    /**
     * @param point 地理坐标点
     * @return 地理坐标点是否位于此矩形内
     */
    public boolean contains(GeoPoint point) {
        return !isEmpty() && (point.getLongitude() >= southWest.getLongitude() && point.getLongitude() <= northEast.getLongitude()) && (point.getLatitude() >= southWest.getLatitude() && point.getLatitude() <= northEast.getLatitude());
    }

    /**
     * @param bounds 矩形区域
     * @return 矩形区域是否完全包含于此矩形区域中
     */
    public boolean contains(GeoBound bounds) {
        return contains(bounds.southWest) && contains(bounds.northEast);
    }

    /**
     * @param bounds 矩形区域
     * @return 计算与另一矩形的交集区域
     */
    public GeoBound intersects(GeoBound bounds) {
        if (bounds != null && !bounds.isEmpty() && !isEmpty()) {
            GeoBound merged = new GeoBound(this, bounds);
            //
            double x1 = this.southWest.getLongitude() == merged.southWest.getLongitude() ? bounds.southWest.getLongitude() : this.southWest.getLongitude();
            double y1 = this.southWest.getLatitude() == merged.southWest.getLatitude() ? bounds.southWest.getLatitude() : this.southWest.getLatitude();
            //
            double x2 = this.northEast.getLongitude() == merged.northEast.getLongitude() ? bounds.northEast.getLongitude() : this.northEast.getLongitude();
            double y2 = this.northEast.getLatitude() == merged.northEast.getLatitude() ? bounds.northEast.getLatitude() : this.northEast.getLatitude();
            //
            if (x1 < x2 && y1 < y2) {
                return new GeoBound(new GeoPoint(x1, y1), new GeoPoint(x2, y2));
            }
        }
        return new GeoBound();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeoBound bounds = (GeoBound) o;
        return new EqualsBuilder()
                .append(southWest, bounds.southWest)
                .append(northEast, bounds.northEast)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(southWest)
                .append(northEast)
                .toHashCode();
    }

    @Override
    public String toString() {
        return String.format("GeoBound{southWest=%s, northEast=%s}", southWest, northEast);
    }
}
