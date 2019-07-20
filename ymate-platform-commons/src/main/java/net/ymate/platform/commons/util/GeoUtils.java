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
package net.ymate.platform.commons.util;

import net.ymate.platform.commons.MathCalcHelper;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 16/12/12 下午3:27
 */
public class GeoUtils {

    /**
     * 地球半径
     */
    private static final double EARTH_RADIUS = 6378137.0;

    private static final double EE = 0.00669342162296594323;

    /**
     * @param d 值
     * @return 弧度单位
     */
    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    /**
     * @param p1 坐标点1
     * @param p2 坐标点2
     * @return 计算两点间的距离(米)
     */
    public static double distance(Point p1, Point p2) {
        return p1.distance(p2);
    }

    /**
     * @param point    坐标点
     * @param distance 距离(米)
     * @return 返回从坐标点到直定距离的矩形范围
     */
    public static Bounds rectangle(Point point, long distance) {
        if (point == null || distance <= 0) {
            return new Bounds();
        }
        float delta = 111000;
        if (point.getLatitude() != 0 && point.getLongitude() != 0) {
            double lng1 = point.longitude - distance / Math.abs(Math.cos(Math.toRadians(point.latitude)) * delta);
            double lng2 = point.longitude + distance / Math.abs(Math.cos(Math.toRadians(point.latitude)) * delta);
            double lat1 = point.latitude - (distance / delta);
            double lat2 = point.latitude + (distance / delta);
            return new Bounds(new Point(lng1, lat1), new Point(lng2, lat2));
        } else {
            double lng1 = point.longitude - distance / delta;
            double lng2 = point.longitude + distance / delta;
            double lat1 = point.latitude - (distance / delta);
            double lat2 = point.latitude + (distance / delta);
            return new Bounds(new Point(lng1, lat1), new Point(lng2, lat2));
        }
    }

    /**
     * 判断点是否在多边形区域内
     *
     * @param polygon 多边形区域
     * @param point   待判断点
     * @return true - 多边形包含这个点, false - 多边形未包含这个点。
     */
    public static boolean contains(Polygon polygon, Point point) {
        return contains(polygon, point, false);
    }

    public static boolean contains(Polygon polygon, Point point, boolean on) {
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
    public static int contains(Circle circle, Point point) {
        return circle.contains(point);
    }

    /**
     * 地理坐标点
     * <p>坐标系转换代码参考自: https://blog.csdn.net/a13570320979/article/details/51366355</p>
     */
    public static class Point implements Serializable {

        /**
         * 经度, X
         */
        private double longitude;

        /**
         * 纬度, Y
         */
        private double latitude;

        /**
         * 坐标点类型, 默认为: WGS84
         *
         * @since 2.0.6
         */
        private PointType type;

        /**
         * 构造器
         *
         * @param longitude 经度
         * @param latitude  纬度
         * @param type      坐标点类型, 默认为WGS84
         * @since 2.0.6
         */
        public Point(double longitude, double latitude, PointType type) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.type = type == null ? PointType.WGS84 : type;
        }

        public Point(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
            this.type = PointType.WGS84;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public PointType getType() {
            return type;
        }

        public void setType(PointType type) {
            this.type = type;
        }

        public Point2D.Double toPoint2D() {
            return new Point2D.Double(longitude, latitude);
        }

        /**
         * @return 将当前坐标点转换为火星坐标
         * @since 2.0.6
         */
        public Point toGcj02() {
            Point point;
            switch (type) {
                case BD09:
                    point = bd09ToGcj02();
                    break;
                case WGS84:
                    point = toWgs84();
                    break;
                default:
                    point = transform();
            }
            return point;
        }

        /**
         * @return 将当前坐标点转换为GPS原始坐标
         * @since 2.0.6
         */
        public Point toWgs84() {
            Point point;
            switch (type) {
                case BD09:
                    point = bd09ToWgs84();
                    break;
                case GCJ02:
                    point = gcj02ToWgs84();
                    break;
                default:
                    point = new Point(longitude, latitude);
            }
            return point;
        }

        /**
         * @return 将当前坐标点转换为百度坐标
         * @since 2.0.6
         */
        public Point toBd09() {
            Point point;
            switch (type) {
                case GCJ02:
                    point = gcj02ToBd09();
                    break;
                case WGS84:
                    point = wgs84ToBd09();
                    break;
                default:
                    point = new Point(longitude, latitude, PointType.BD09);
            }
            return point;
        }

        /**
         * @return 保留小数点后六位
         * @since 2.0.6
         */
        public Point retain6() {
            return new Point(Double.valueOf(String.format("%.6f", longitude)), Double.valueOf(String.format("%.6f", latitude)), type);
        }

        /**
         * @return 是否超出中国范围
         * @since 2.0.6
         */
        public boolean notInChina() {
            if (longitude < 72.004 || longitude > 137.8347) {
                return true;
            }
            return latitude < 0.8293 || latitude > 55.8271;
        }

        private double transformLat() {
            double x = longitude - 105.0;
            double y = latitude - 35.0;
            //
            double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
            ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
            ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
            return ret;
        }

        private double transformLon() {
            double x = longitude - 105.0;
            double y = latitude - 35.0;
            //
            double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
            ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
            ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
            ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
            return ret;
        }

        /**
         * @return WGS84 --> GCJ-02
         */
        private Point transform() {
            if (notInChina()) {
                return new Point(longitude, latitude);
            }
            double dLat = transformLat();
            double dLon = transformLon();
            double radLat = rad(latitude);
            double magic = Math.sin(radLat);
            magic = 1 - EE * magic * magic;
            double sqrtMagic = Math.sqrt(magic);
            dLat = (dLat * 180.0) / ((EARTH_RADIUS * (1 - EE)) / (magic * sqrtMagic) * Math.PI);
            dLon = (dLon * 180.0) / (EARTH_RADIUS / sqrtMagic * Math.cos(radLat) * Math.PI);
            double mgLat = latitude + dLat;
            double mgLon = longitude + dLon;
            //
            return new Point(mgLon, mgLat, PointType.GCJ02);
        }

        /**
         * @return GCJ-02 --> WGS84
         */
        private Point gcj02ToWgs84() {
            Point point = transform();
            return new Point(longitude * 2 - point.longitude, latitude * 2 - point.latitude);
        }

        /**
         * @return GCJ-02 --> BD09
         */
        private Point gcj02ToBd09() {
            double z = Math.sqrt(longitude * longitude + latitude * latitude) + 0.00002 * Math.sin(latitude * Math.PI);
            double theta = Math.atan2(latitude, longitude) + 0.000003 * Math.cos(longitude * Math.PI);
            return new Point(z * Math.cos(theta) + 0.0065, z * Math.sin(theta) + 0.006, PointType.BD09);
        }

        /**
         * @return BD09 --> GCJ-02
         */
        private Point bd09ToGcj02() {
            double x = longitude - 0.0065, y = latitude - 0.006;
            double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * Math.PI);
            double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * Math.PI);
            return new Point(z * Math.cos(theta), z * Math.sin(theta), PointType.GCJ02);
        }

        /**
         * @return WGS84 --> BD09
         */
        private Point wgs84ToBd09() {
            return transform().toBd09();
        }

        /**
         * @return BD09 --> WGS84
         */
        private Point bd09ToWgs84() {
            return bd09ToGcj02().toWgs84();
        }

        /**
         * @param point 坐标点
         * @return 计算两点间的距离(米)
         */
        public double distance(Point point) {
            double lat1 = rad(latitude);
            double lat2 = rad(point.latitude);
            double diff = rad(longitude) - rad(point.longitude);
            return Math.round(2 * Math.asin(Math.sqrt(Math.pow(Math.sin((lat1 - lat2) / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(diff / 2), 2))) * EARTH_RADIUS * 10000) / 10000;
        }

        /**
         * @return 验证是否为合法有效的经纬度
         */
        public boolean isValidCoordinate() {
            // 经度: 180° >= x >= 0°
            if (0.0 > longitude || 180.0 < longitude) {
                return false;
            }
            // 纬度: 90° >= y >= 0°
            return !(0.0 > latitude || 90.0 < latitude);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Point point = (Point) o;
            return new EqualsBuilder()
                    .append(longitude, point.longitude)
                    .append(latitude, point.latitude)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(longitude)
                    .append(latitude)
                    .toHashCode();
        }

        @Override
        public String toString() {
            return "Point{" + "longitude=" + longitude + ", latitude=" + latitude + ", type=" + type + '}';
        }
    }

    /**
     * 坐标点类型
     *
     * @since 2.0.6
     */
    public enum PointType {

        /**
         * GPS
         */
        WGS84,

        /**
         * 火星
         */
        GCJ02,

        /**
         * 百度
         */
        BD09
    }

    /**
     * 地理坐标矩形区域
     */
    public static class Bounds implements Serializable {

        /**
         * 左下(西南)角坐标点
         */
        private Point southWest;

        /**
         * 右上(东北)角坐标点
         */
        private Point northEast;

        /**
         * 空矩形
         */
        public Bounds() {
        }

        /**
         * 取两个矩形区域的并集
         *
         * @param first 第一个矩形区域
         * @param other 另一个矩形区域
         */
        public Bounds(Bounds first, Bounds other) {
            if (first == null || first.isEmpty() || other == null || other.isEmpty()) {
                throw new NullArgumentException("bounds");
            }
            this.southWest = new Point(Math.min(first.southWest.getLongitude(), other.southWest.getLongitude()), Math.min(first.southWest.getLatitude(), other.southWest.getLatitude()));
            //
            this.northEast = new Point(Math.max(first.northEast.getLongitude(), other.northEast.getLongitude()), Math.max(first.northEast.getLatitude(), other.northEast.getLatitude()));
        }

        public Bounds(Point southWest, Point northEast) {
            this.southWest = southWest;
            this.northEast = northEast;
        }

        public Point getSouthWest() {
            return southWest;
        }

        public void setSouthWest(Point southWest) {
            this.southWest = southWest;
        }

        public Point getNorthEast() {
            return northEast;
        }

        public void setNorthEast(Point northEast) {
            this.northEast = northEast;
        }

        /**
         * @return 返回矩形的中心点
         */
        public Point getCenter() {
            return new Point((southWest.longitude + northEast.longitude) / 2, (southWest.latitude + northEast.latitude) / 2);
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
        public boolean contains(Point point) {
            return !isEmpty() && (point.longitude >= southWest.longitude && point.longitude <= northEast.longitude) && (point.latitude >= southWest.latitude && point.latitude <= northEast.latitude);
        }

        /**
         * @param bounds 矩形区域
         * @return 矩形区域是否完全包含于此矩形区域中
         */
        public boolean contains(Bounds bounds) {
            return contains(bounds.southWest) && contains(bounds.northEast);
        }

        /**
         * @param bounds 矩形区域
         * @return 计算与另一矩形的交集区域
         */
        public Bounds intersects(Bounds bounds) {
            if (bounds != null && !bounds.isEmpty() && !isEmpty()) {
                Bounds merged = new Bounds(this, bounds);
                //
                double x1 = this.southWest.longitude == merged.southWest.longitude ? bounds.southWest.longitude : this.southWest.longitude;
                double y1 = this.southWest.latitude == merged.southWest.latitude ? bounds.southWest.latitude : this.southWest.latitude;
                //
                double x2 = this.northEast.longitude == merged.northEast.longitude ? bounds.northEast.longitude : this.northEast.longitude;
                double y2 = this.northEast.latitude == merged.northEast.latitude ? bounds.northEast.latitude : this.northEast.latitude;
                //
                if (x1 < x2 && y1 < y2) {
                    return new Bounds(new Point(x1, y1), new Point(x2, y2));
                }
            }
            return new Bounds();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Bounds bounds = (Bounds) o;
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
            return "Bounds{southWest=" + southWest + ", northEast=" + northEast + '}';
        }
    }

    /**
     * 地理坐标圆形
     */
    public static class Circle implements Serializable {

        /**
         * 圆心
         */
        private Point center;

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
        public Circle(Point center, double r) {
            this.center = center;
            this.r = r;
        }

        public Point getCenter() {
            return center;
        }

        public void setCenter(Point center) {
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
        public int contains(Point point) {
            double value = Math.hypot((point.longitude - center.longitude), (point.latitude - center.latitude));
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

            Circle circle = (Circle) o;

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
            return "Circle{" + "center=" + center + ", r=" + r + '}';
        }
    }

    /**
     * 地理坐标多边形区域
     */
    public static class Polygon implements Serializable {

        /**
         * 多边形坐标点列表
         */
        private final List<Point> points = new ArrayList<>();

        public Polygon() {
        }

        public Polygon(Point[] points) {
            if (ArrayUtils.isNotEmpty(points)) {
                this.points.addAll(Arrays.asList(points));
            }
        }

        public Polygon(Collection<Point> points) {
            if (points != null && !points.isEmpty()) {
                this.points.addAll(points);
            }
        }

        public boolean isEmpty() {
            return this.points.isEmpty();
        }

        public Polygon add(Point point) {
            if (point != null) {
                this.points.add(point);
            }
            return this;
        }

        public Polygon add(double longitude, double latitude) {
            this.points.add(new Point(longitude, latitude));
            return this;
        }

        public List<Point> getPoints() {
            return points;
        }

        public boolean in(Point point) {
            int nCross = 0;
            for (int i = 0; i < points.size(); i++) {
                Point p1 = points.get(i);
                Point p2 = points.get((i + 1) % points.size());
                if (MathCalcHelper.eq(p1.latitude, p2.latitude)) {
                    continue;
                }
                if (point.latitude < Math.min(p1.latitude, p2.latitude)) {
                    continue;
                }
                if (point.latitude >= Math.max(p1.latitude, p2.latitude)) {
                    continue;
                }
                double x = (point.latitude - p1.latitude) * (p2.longitude - p1.longitude) / (p2.latitude - p1.latitude) + p1.longitude;
                if (x > point.longitude) {
                    nCross++;
                }
            }
            return (nCross % 2 == 1);
        }

        public boolean on(Point point) {
            for (int i = 0; i < points.size(); i++) {
                Point p1 = points.get(i);
                Point p2 = points.get((i + 1) % points.size());
                if (point.latitude < Math.min(p1.latitude, p2.latitude)) {
                    continue;
                }
                if (point.latitude > Math.max(p1.latitude, p2.latitude)) {
                    continue;
                }
                if (MathCalcHelper.eq(p1.latitude, p2.latitude)) {
                    double minLon = Math.min(p1.longitude, p2.longitude);
                    double maxLon = Math.max(p1.longitude, p2.longitude);
                    if (MathCalcHelper.eq(point.latitude, p1.latitude) && point.longitude >= minLon && point.longitude <= maxLon) {
                        return true;
                    }
                } else {
                    double x = (point.latitude - p1.latitude) * (p2.longitude - p1.longitude) / (p2.latitude - p1.latitude) + p1.longitude;
                    if (MathCalcHelper.eq(x, point.longitude)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
