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

/**
 * @author 刘镇 (suninformation@163.com) on 2016/12/12 15:27
 */
public class GeoUtils {

    /**
     * 地球半径
     */
    public static final double EARTH_RADIUS = 6378137.0;

    public static final double EE = 0.00669342162296594323;

    /**
     * @param d 值
     * @return 弧度单位
     */
    public static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * @param p1 坐标点1
     * @param p2 坐标点2
     * @return 计算两点间的距离(米)
     */
    public static double distance(GeoPoint p1, GeoPoint p2) {
        return p1.distance(p2);
    }

    /**
     * @param point    坐标点
     * @param distance 距离(米)
     * @return 返回从坐标点到直定距离的矩形范围
     */
    public static GeoBound rectangle(GeoPoint point, long distance) {
        if (point == null || distance <= 0) {
            return new GeoBound();
        }
        float delta = 111000;
        double lng1;
        double lng2;
        double lat1 = point.getLatitude() - (distance / delta);
        double lat2 = point.getLatitude() + (distance / delta);
        if (point.getLatitude() != 0 && point.getLongitude() != 0) {
            lng1 = point.getLongitude() - distance / Math.abs(Math.cos(Math.toRadians(point.getLatitude())) * delta);
            lng2 = point.getLongitude() + distance / Math.abs(Math.cos(Math.toRadians(point.getLatitude())) * delta);
        } else {
            lng1 = point.getLongitude() - distance / delta;
            lng2 = point.getLongitude() + distance / delta;
        }
        return new GeoBound(new GeoPoint(lng1, lat1), new GeoPoint(lng2, lat2));
    }

    /**
     * 判断点是否在多边形区域内
     *
     * @param polygon 多边形区域
     * @param point   待判断点
     * @return true - 多边形包含这个点, false - 多边形未包含这个点。
     */
    public static boolean contains(GeoPolygon polygon, GeoPoint point) {
        return contains(polygon, point, false);
    }

    public static boolean contains(GeoPolygon polygon, GeoPoint point, boolean on) {
        if (on) {
            // 判断是否在多边形区域边界上
            return polygon.on(point);
        }
        // 判断点是否在多边形区域内
        return polygon.in(point);
    }

    /**
     * 判断点是否在圆形范围内
     *
     * @param circle 圆形区域
     * @param point  待判断点
     * @return -1 - 点在圆外, 0 - 点在圆上, 1 - 点在圆内
     */
    public static int contains(GeoCircle circle, GeoPoint point) {
        return circle.contains(point);
    }
}
