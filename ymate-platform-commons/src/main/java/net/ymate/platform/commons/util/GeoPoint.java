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

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * 地理坐标点
 * <p>
 * 坐标系转换代码参考自:
 * <p>
 * https://blog.csdn.net/a13570320979/article/details/51366355
 * <p>
 * https://blog.csdn.net/m0_37738114/article/details/80452485
 *
 * @author 刘镇 (suninformation@163.com) on 2016/12/12 15:27
 */
public class GeoPoint implements Serializable {

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
    private GeoPointType type;

    public GeoPoint() {
    }

    /**
     * 构造器
     *
     * @param longitude 经度
     * @param latitude  纬度
     * @param type      坐标点类型, 默认为WGS84
     * @since 2.0.6
     */
    public GeoPoint(double longitude, double latitude, GeoPointType type) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.type = type == null ? GeoPointType.WGS84 : type;
    }

    public GeoPoint(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.type = GeoPointType.WGS84;
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

    public GeoPointType getType() {
        return type;
    }

    public void setType(GeoPointType type) {
        this.type = type;
    }

    public Point2D.Double toPoint2D() {
        return new Point2D.Double(longitude, latitude);
    }

    /**
     * @return 将当前坐标点转换为火星坐标
     * @since 2.0.6
     */
    public GeoPoint toGcj02() {
        GeoPoint point;
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
    public GeoPoint toWgs84() {
        GeoPoint point;
        switch (type) {
            case BD09:
                point = bd09ToWgs84();
                break;
            case GCJ02:
                point = gcj02ToWgs84();
                break;
            default:
                point = new GeoPoint(longitude, latitude);
        }
        return point;
    }

    /**
     * @return 将当前坐标点转换为百度坐标
     * @since 2.0.6
     */
    public GeoPoint toBd09() {
        GeoPoint point;
        switch (type) {
            case GCJ02:
                point = gcj02ToBd09();
                break;
            case WGS84:
                point = wgs84ToBd09();
                break;
            default:
                point = new GeoPoint(longitude, latitude, GeoPointType.BD09);
        }
        return point;
    }

    /**
     * @return 保留小数点后六位
     * @since 2.0.6
     */
    public GeoPoint retain6() {
        return new GeoPoint(Double.parseDouble(String.format("%.6f", longitude)), Double.parseDouble(String.format("%.6f", latitude)), type);
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
    private GeoPoint transform() {
        if (notInChina()) {
            return new GeoPoint(longitude, latitude);
        }
        double dLat = transformLat();
        double dLon = transformLon();
        double radLat = GeoUtils.rad(latitude);
        double magic = Math.sin(radLat);
        magic = 1 - GeoUtils.EE * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((GeoUtils.EARTH_RADIUS * (1 - GeoUtils.EE)) / (magic * sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / (GeoUtils.EARTH_RADIUS / sqrtMagic * Math.cos(radLat) * Math.PI);
        double mgLat = latitude + dLat;
        double mgLon = longitude + dLon;
        //
        return new GeoPoint(mgLon, mgLat, GeoPointType.GCJ02);
    }

    /**
     * @return GCJ-02 --> WGS84
     */
    private GeoPoint gcj02ToWgs84() {
        GeoPoint point = transform();
        return new GeoPoint(longitude * 2 - point.longitude, latitude * 2 - point.latitude);
    }

    /**
     * @return GCJ-02 --> BD09
     */
    private GeoPoint gcj02ToBd09() {
        double z = Math.sqrt(longitude * longitude + latitude * latitude) + 0.00002 * Math.sin(latitude * Math.PI);
        double theta = Math.atan2(latitude, longitude) + 0.000003 * Math.cos(longitude * Math.PI);
        return new GeoPoint(z * Math.cos(theta) + 0.0065, z * Math.sin(theta) + 0.006, GeoPointType.BD09);
    }

    /**
     * @return BD09 --> GCJ-02
     */
    private GeoPoint bd09ToGcj02() {
        double x = longitude - 0.0065, y = latitude - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * Math.PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * Math.PI);
        return new GeoPoint(z * Math.cos(theta), z * Math.sin(theta), GeoPointType.GCJ02);
    }

    /**
     * @return WGS84 --> BD09
     */
    private GeoPoint wgs84ToBd09() {
        return transform().toBd09();
    }

    /**
     * @return BD09 --> WGS84
     */
    private GeoPoint bd09ToWgs84() {
        return bd09ToGcj02().toWgs84();
    }

    /**
     * 算法代码参考自：https://blog.csdn.net/xiejm2333/article/details/73297004
     *
     * @param point 坐标点
     * @return 计算两点间的距离(米)
     */
    public double distance(GeoPoint point) {
        double lat1 = GeoUtils.rad(latitude);
        double lat2 = GeoUtils.rad(point.latitude);
        double diff = GeoUtils.rad(longitude) - GeoUtils.rad(point.longitude);
        return Math.round((2 * Math.asin(Math.sqrt(Math.pow(Math.sin((lat1 - lat2) / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(diff / 2), 2)))) * GeoUtils.EARTH_RADIUS * 10000d) / 10000d;
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
        GeoPoint point = (GeoPoint) o;
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
        return String.format("GeoPoint{longitude=%s, latitude=%s, type=%s}", longitude, latitude, type);
    }
}
