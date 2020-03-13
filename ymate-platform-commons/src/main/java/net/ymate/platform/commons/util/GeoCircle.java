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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * 地理坐标圆形
 */
public class GeoCircle implements Serializable {

    /**
     * 圆心
     */
    private GeoPoint center;

    /**
     * 半径
     */
    private double r;

    /**
     * 构造器
     *
     * @param center 圆心
     * @param r      半径
     */
    public GeoCircle(GeoPoint center, double r) {
        this.center = center;
        this.r = r;
    }

    public GeoPoint getCenter() {
        return center;
    }

    public void setCenter(GeoPoint center) {
        this.center = center;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    /**
     * 判断点是否在圆形范围内
     *
     * @param point 点
     * @return -1 - 点在圆外, 0 - 点在圆上, 1 - 点在圆内
     */
    public int contains(GeoPoint point) {
        double value = Math.hypot((point.getLongitude() - center.getLongitude()), (point.getLatitude() - center.getLatitude()));
        if (value > r) {
            // 点在圆外
            return -1;
        } else if (value < r) {
            // 点在圆内
            return 1;
        }
        // 点在圆上
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GeoCircle circle = (GeoCircle) o;
        return new EqualsBuilder()
                .append(r, circle.r)
                .append(center, circle.center)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(center)
                .append(r)
                .toHashCode();
    }

    @Override
    public String toString() {
        return String.format("GeoCircle{center=%s, r=%s}", center, r);
    }
}
