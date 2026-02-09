/*
 * Copyright 2007-present the original author or authors.
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

import org.junit.Assert;
import org.junit.Test;

/**
 * GeoPoint测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-13 15:30:00
 * @since 2.1.4
 */
public class GeoPointTest {

    private static final double BEIJING_LONGITUDE = 116.404;
    private static final double BEIJING_LATITUDE = 39.915;
    private static final double SHANGHAI_LONGITUDE = 121.4737;
    private static final double SHANGHAI_LATITUDE = 31.2304;

    @Test
    public void testConstructor() {
        // 测试无参构造函数
        GeoPoint point1 = new GeoPoint();
        Assert.assertEquals(0.0, point1.getLongitude(), 0.0001);
        Assert.assertEquals(0.0, point1.getLatitude(), 0.0001);
        Assert.assertEquals(GeoPointType.WGS84, point1.getType());

        // 测试带经纬度构造函数
        GeoPoint point2 = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE);
        Assert.assertEquals(BEIJING_LONGITUDE, point2.getLongitude(), 0.0001);
        Assert.assertEquals(BEIJING_LATITUDE, point2.getLatitude(), 0.0001);
        Assert.assertEquals(GeoPointType.WGS84, point2.getType());

        // 测试带经纬度和类型构造函数
        GeoPoint point3 = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE, GeoPointType.GCJ02);
        Assert.assertEquals(BEIJING_LONGITUDE, point3.getLongitude(), 0.0001);
        Assert.assertEquals(BEIJING_LATITUDE, point3.getLatitude(), 0.0001);
        Assert.assertEquals(GeoPointType.GCJ02, point3.getType());

        // 测试类型为null的情况
        GeoPoint point4 = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE, null);
        Assert.assertEquals(BEIJING_LONGITUDE, point4.getLongitude(), 0.0001);
        Assert.assertEquals(BEIJING_LATITUDE, point4.getLatitude(), 0.0001);
        Assert.assertEquals(GeoPointType.WGS84, point4.getType());
    }

    @Test
    public void testSetterGetter() {
        GeoPoint point = new GeoPoint();

        // 测试设置经度
        point.setLongitude(BEIJING_LONGITUDE);
        Assert.assertEquals(BEIJING_LONGITUDE, point.getLongitude(), 0.0001);

        // 测试设置纬度
        point.setLatitude(BEIJING_LATITUDE);
        Assert.assertEquals(BEIJING_LATITUDE, point.getLatitude(), 0.0001);

        // 测试设置类型
        point.setType(GeoPointType.BD09);
        Assert.assertEquals(GeoPointType.BD09, point.getType());
    }

    @Test
    public void testToPoint2D() {
        GeoPoint point = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE);
        java.awt.geom.Point2D.Double point2D = point.toPoint2D();
        Assert.assertNotNull(point2D);
        Assert.assertEquals(BEIJING_LONGITUDE, point2D.x, 0.0001);
        Assert.assertEquals(BEIJING_LATITUDE, point2D.y, 0.0001);
    }

    @Test
    public void testCoordinateTransformation() {
        GeoPoint wgs84Point = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE, GeoPointType.WGS84);

        // 测试WGS84转GCJ02
        GeoPoint gcj02Point = wgs84Point.toGcj02();
        Assert.assertNotNull(gcj02Point);
        Assert.assertEquals(GeoPointType.GCJ02, gcj02Point.getType());
        // 转换后坐标应该不同
        Assert.assertNotEquals(wgs84Point.getLongitude(), gcj02Point.getLongitude(), 0.0001);
        Assert.assertNotEquals(wgs84Point.getLatitude(), gcj02Point.getLatitude(), 0.0001);

        // 测试GCJ02转WGS84
        GeoPoint wgs84ConvertedPoint = gcj02Point.toWgs84();
        Assert.assertNotNull(wgs84ConvertedPoint);
        Assert.assertEquals(GeoPointType.WGS84, wgs84ConvertedPoint.getType());
        // 转换后坐标应该接近原始值
        Assert.assertEquals(wgs84Point.getLongitude(), wgs84ConvertedPoint.getLongitude(), 0.001);
        Assert.assertEquals(wgs84Point.getLatitude(), wgs84ConvertedPoint.getLatitude(), 0.001);

        // 测试GCJ02转BD09
        GeoPoint bd09Point = gcj02Point.toBd09();
        Assert.assertNotNull(bd09Point);
        Assert.assertEquals(GeoPointType.BD09, bd09Point.getType());
        // 转换后坐标应该不同
        Assert.assertNotEquals(gcj02Point.getLongitude(), bd09Point.getLongitude(), 0.0001);
        Assert.assertNotEquals(gcj02Point.getLatitude(), bd09Point.getLatitude(), 0.0001);

        // 测试BD09转GCJ02
        GeoPoint gcj02ConvertedPoint = bd09Point.toGcj02();
        Assert.assertNotNull(gcj02ConvertedPoint);
        Assert.assertEquals(GeoPointType.GCJ02, gcj02ConvertedPoint.getType());
        // 转换后坐标应该接近原始值
        Assert.assertEquals(gcj02Point.getLongitude(), gcj02ConvertedPoint.getLongitude(), 0.001);
        Assert.assertEquals(gcj02Point.getLatitude(), gcj02ConvertedPoint.getLatitude(), 0.001);

        // 测试BD09转WGS84
        GeoPoint wgs84FromBd09Point = bd09Point.toWgs84();
        Assert.assertNotNull(wgs84FromBd09Point);
        Assert.assertEquals(GeoPointType.WGS84, wgs84FromBd09Point.getType());

        // 测试WGS84转BD09
        GeoPoint bd09FromWgs84Point = wgs84Point.toBd09();
        Assert.assertNotNull(bd09FromWgs84Point);
        Assert.assertEquals(GeoPointType.BD09, bd09FromWgs84Point.getType());
    }

    @Test
    public void testRetain6() {
        GeoPoint point = new GeoPoint(116.404123456, 39.915123456);
        GeoPoint retainedPoint = point.retain6();
        Assert.assertNotNull(retainedPoint);
        Assert.assertEquals(116.404123, retainedPoint.getLongitude(), 0.000001);
        Assert.assertEquals(39.915123, retainedPoint.getLatitude(), 0.000001);
        Assert.assertEquals(point.getType(), retainedPoint.getType());
    }

    @Test
    public void testNotInChina() {
        // 测试中国境内点
        GeoPoint beijingPoint = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE);
        Assert.assertFalse(beijingPoint.notInChina());

        // 测试中国境外点（经度超出范围）
        GeoPoint outsideLongitudePoint = new GeoPoint(70.0, 39.915);
        Assert.assertTrue(outsideLongitudePoint.notInChina());

        // 测试中国境外点（纬度超出范围）
        GeoPoint outsideLatitudePoint = new GeoPoint(116.404, 56.0);
        Assert.assertTrue(outsideLatitudePoint.notInChina());
    }

    @Test
    public void testDistance() {
        GeoPoint beijingPoint = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE);
        GeoPoint shanghaiPoint = new GeoPoint(SHANGHAI_LONGITUDE, SHANGHAI_LATITUDE);

        // 测试两点间距离计算
        double distance = beijingPoint.distance(shanghaiPoint);
        Assert.assertTrue(distance > 0); // 距离应该大于0
    }

    @Test
    public void testIsValidCoordinate() {
        // 测试有效坐标
        GeoPoint validPoint = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE);
        Assert.assertTrue(validPoint.isValidCoordinate());

        // 测试无效经度（小于-180）
        GeoPoint invalidLongitude1 = new GeoPoint(-181.0, BEIJING_LATITUDE);
        Assert.assertFalse(invalidLongitude1.isValidCoordinate());

        // 测试无效经度（大于180）
        GeoPoint invalidLongitude2 = new GeoPoint(181.0, BEIJING_LATITUDE);
        Assert.assertFalse(invalidLongitude2.isValidCoordinate());

        // 测试无效纬度（小于-90）
        GeoPoint invalidLatitude1 = new GeoPoint(BEIJING_LONGITUDE, -91.0);
        Assert.assertFalse(invalidLatitude1.isValidCoordinate());

        // 测试无效纬度（大于90）
        GeoPoint invalidLatitude2 = new GeoPoint(BEIJING_LONGITUDE, 91.0);
        Assert.assertFalse(invalidLatitude2.isValidCoordinate());
    }

    @Test
    public void testEqualsAndHashCode() {
        GeoPoint point1 = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE);
        GeoPoint point2 = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE);
        GeoPoint point3 = new GeoPoint(SHANGHAI_LONGITUDE, SHANGHAI_LATITUDE);

        // 测试相等性
        Assert.assertEquals(point1, point2);
        Assert.assertNotEquals(point1, point3);
        Assert.assertNotEquals(point1, null);
        Assert.assertNotEquals(point1, new Object());

        // 测试哈希码
        Assert.assertEquals(point1.hashCode(), point2.hashCode());
        Assert.assertNotEquals(point1.hashCode(), point3.hashCode());
    }

    @Test
    public void testToString() {
        GeoPoint point = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE, GeoPointType.WGS84);
        String toString = point.toString();
        Assert.assertNotNull(toString);
        Assert.assertTrue(toString.contains("longitude=" + BEIJING_LONGITUDE));
        Assert.assertTrue(toString.contains("latitude=" + BEIJING_LATITUDE));
        Assert.assertTrue(toString.contains("type=WGS84"));
    }
}
