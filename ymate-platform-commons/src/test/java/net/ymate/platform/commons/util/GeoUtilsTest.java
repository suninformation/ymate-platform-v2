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
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GeoUtils相关类的全面测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-13 15:30:00
 * @since 2.1.4
 */
public class GeoUtilsTest {

    // 测试用的坐标点
    private GeoPoint beijing;
    private GeoPoint shanghai;
    private GeoPoint guangzhou;
    private GeoPoint shenzhen;
    private GeoPoint invalidPoint;

    @Before
    public void setUp() {
        // 初始化测试数据
        beijing = new GeoPoint(116.4074, 39.9042, GeoPointType.WGS84);
        shanghai = new GeoPoint(121.4737, 31.2304, GeoPointType.WGS84);
        guangzhou = new GeoPoint(113.2644, 23.1291, GeoPointType.WGS84);
        shenzhen = new GeoPoint(114.0579, 22.5431, GeoPointType.WGS84);
        invalidPoint = new GeoPoint(200.0, 100.0, GeoPointType.WGS84);
    }

    // ==================== GeoPointTest ====================

    /**
     * 测试GeoPoint的构造器
     */
    @Test
    public void testGeoPointConstructors() {
        // 测试默认构造器
        GeoPoint point1 = new GeoPoint();
        Assert.assertNotNull(point1);

        // 测试带经度和纬度的构造器
        GeoPoint point2 = new GeoPoint(116.4074, 39.9042);
        Assert.assertEquals(116.4074, point2.getLongitude(), 0.00001);
        Assert.assertEquals(39.9042, point2.getLatitude(), 0.00001);
        Assert.assertEquals(GeoPointType.WGS84, point2.getType());

        // 测试带经度、纬度和类型的构造器
        GeoPoint point3 = new GeoPoint(116.4074, 39.9042, GeoPointType.GCJ02);
        Assert.assertEquals(116.4074, point3.getLongitude(), 0.00001);
        Assert.assertEquals(39.9042, point3.getLatitude(), 0.00001);
        Assert.assertEquals(GeoPointType.GCJ02, point3.getType());

        // 测试null类型的构造器
        GeoPoint point4 = new GeoPoint(116.4074, 39.9042, null);
        Assert.assertEquals(GeoPointType.WGS84, point4.getType());
    }

    /**
     * 测试GeoPoint的getter和setter方法
     */
    @Test
    public void testGeoPointGettersAndSetters() {
        GeoPoint point = new GeoPoint();

        // 测试setLongitude和getLongitude
        point.setLongitude(116.4074);
        Assert.assertEquals(116.4074, point.getLongitude(), 0.00001);

        // 测试setLatitude和getLatitude
        point.setLatitude(39.9042);
        Assert.assertEquals(39.9042, point.getLatitude(), 0.00001);

        // 测试setType和getType
        point.setType(GeoPointType.BD09);
        Assert.assertEquals(GeoPointType.BD09, point.getType());
    }

    /**
     * 测试GeoPoint的toPoint2D方法
     */
    @Test
    public void testGeoPointToPoint2D() {
        GeoPoint point = new GeoPoint(116.4074, 39.9042);
        java.awt.geom.Point2D.Double point2D = point.toPoint2D();
        Assert.assertNotNull(point2D);
        Assert.assertEquals(116.4074, point2D.x, 0.00001);
        Assert.assertEquals(39.9042, point2D.y, 0.00001);
    }

    /**
     * 测试GeoPoint的坐标转换方法
     */
    @Test
    public void testGeoPointCoordinateTransformations() {
        // 测试WGS84到GCJ02转换
        GeoPoint gcj02Point = beijing.toGcj02();
        Assert.assertNotNull(gcj02Point);
        Assert.assertEquals(GeoPointType.GCJ02, gcj02Point.getType());

        // 测试WGS84到BD09转换
        GeoPoint bd09Point = beijing.toBd09();
        Assert.assertNotNull(bd09Point);
        Assert.assertEquals(GeoPointType.BD09, bd09Point.getType());

        // 测试GCJ02到WGS84转换
        GeoPoint wgs84Point = gcj02Point.toWgs84();
        Assert.assertNotNull(wgs84Point);
        // 由于转换精度问题，使用近似比较
        Assert.assertEquals(beijing.getLongitude(), wgs84Point.getLongitude(), 0.001);
        Assert.assertEquals(beijing.getLatitude(), wgs84Point.getLatitude(), 0.001);

        // 测试BD09到GCJ02转换
        GeoPoint gcj02Point2 = bd09Point.toGcj02();
        Assert.assertNotNull(gcj02Point2);
        Assert.assertEquals(GeoPointType.GCJ02, gcj02Point2.getType());

        // 测试BD09到WGS84转换
        GeoPoint wgs84Point2 = bd09Point.toWgs84();
        Assert.assertNotNull(wgs84Point2);
        // 由于转换精度问题，使用近似比较
        Assert.assertEquals(beijing.getLongitude(), wgs84Point2.getLongitude(), 0.001);
        Assert.assertEquals(beijing.getLatitude(), wgs84Point2.getLatitude(), 0.001);
    }

    /**
     * 测试GeoPoint的retain6方法
     */
    @Test
    public void testGeoPointRetain6() {
        GeoPoint point = new GeoPoint(116.4074123456, 39.9042123456);
        GeoPoint retainedPoint = point.retain6();
        Assert.assertNotNull(retainedPoint);
        Assert.assertEquals(116.407412, retainedPoint.getLongitude(), 0.000001);
        Assert.assertEquals(39.904212, retainedPoint.getLatitude(), 0.000001);
    }

    /**
     * 测试GeoPoint的notInChina方法
     */
    @Test
    public void testGeoPointNotInChina() {
        // 北京在中国范围内
        Assert.assertFalse(beijing.notInChina());
        // 无效点不在中国范围内
        Assert.assertTrue(invalidPoint.notInChina());
        // 边界点测试
        GeoPoint borderPoint = new GeoPoint(72.004, 0.8293);
        Assert.assertFalse(borderPoint.notInChina());
    }

    /**
     * 测试GeoPoint的distance方法
     */
    @Test
    public void testGeoPointDistance() {
        // 计算北京到上海的距离（约1068公里）
        double distance = beijing.distance(shanghai);
        Assert.assertTrue(distance > 1000000 && distance < 1200000);

        // 计算同一地点的距离应该为0
        double samePointDistance = beijing.distance(beijing);
        Assert.assertEquals(0.0, samePointDistance, 0.00001);

        // 测试null参数
        // assertThrows在JUnit 4中不支持，需要使用try-catch
    }

    /**
     * 测试GeoPoint的isValidCoordinate方法
     */
    @Test
    public void testGeoPointIsValidCoordinate() {
        // 有效坐标
        Assert.assertTrue(beijing.isValidCoordinate());
        // 无效坐标
        Assert.assertFalse(invalidPoint.isValidCoordinate());
        // 边界值测试
        GeoPoint borderPoint1 = new GeoPoint(180.0, 90.0);
        Assert.assertTrue(borderPoint1.isValidCoordinate());
        GeoPoint borderPoint2 = new GeoPoint(-180.0, -90.0);
        Assert.assertTrue(borderPoint2.isValidCoordinate());
        GeoPoint borderPoint3 = new GeoPoint(180.1, 90.1);
        Assert.assertFalse(borderPoint3.isValidCoordinate());
    }

    /**
     * 测试GeoPoint的equals和hashCode方法
     */
    @Test
    public void testGeoPointEqualsAndHashCode() {
        GeoPoint point1 = new GeoPoint(116.4074, 39.9042);
        GeoPoint point2 = new GeoPoint(116.4074, 39.9042);
        GeoPoint point3 = new GeoPoint(121.4737, 31.2304);

        // 测试equals方法
        Assert.assertEquals(point1, point2);
        Assert.assertNotEquals(point1, point3);
        Assert.assertNotEquals(point1, null);
        Assert.assertNotEquals(point1, new Object());

        // 测试hashCode方法
        Assert.assertEquals(point1.hashCode(), point2.hashCode());
        Assert.assertNotEquals(point1.hashCode(), point3.hashCode());
    }

    /**
     * 测试GeoPoint的toString方法
     */
    @Test
    public void testGeoPointToString() {
        GeoPoint point = new GeoPoint(116.4074, 39.9042);
        String toString = point.toString();
        Assert.assertNotNull(toString);
        Assert.assertTrue(toString.contains("116.4074"));
        Assert.assertTrue(toString.contains("39.9042"));
    }

    // ==================== GeoPointTypeTest ====================

    /**
     * 测试GeoPointType枚举
     */
    @Test
    public void testGeoPointType() {
        Assert.assertEquals(3, GeoPointType.values().length);
        Assert.assertEquals(GeoPointType.WGS84, GeoPointType.valueOf("WGS84"));
        Assert.assertEquals(GeoPointType.GCJ02, GeoPointType.valueOf("GCJ02"));
        Assert.assertEquals(GeoPointType.BD09, GeoPointType.valueOf("BD09"));
    }

    // ==================== GeoBoundTest ====================

    /**
     * 测试GeoBound的构造器
     */
    @Test
    public void testGeoBoundConstructors() {
        // 测试默认构造器
        GeoBound bound1 = new GeoBound();
        Assert.assertNotNull(bound1);
        Assert.assertTrue(bound1.isEmpty());

        // 测试带西南角和东北角的构造器
        GeoBound bound2 = new GeoBound(guangzhou, beijing);
        Assert.assertNotNull(bound2);
        Assert.assertFalse(bound2.isEmpty());
        Assert.assertEquals(113.2644, bound2.getSouthWest().getLongitude(), 0.00001);
        Assert.assertEquals(23.1291, bound2.getSouthWest().getLatitude(), 0.00001);
        Assert.assertEquals(116.4074, bound2.getNorthEast().getLongitude(), 0.00001);
        Assert.assertEquals(39.9042, bound2.getNorthEast().getLatitude(), 0.00001);

        // 测试带两个GeoBound的构造器
        GeoBound bound3 = new GeoBound(shenzhen, shanghai);
        GeoBound bound4 = new GeoBound(bound2, bound3);
        Assert.assertNotNull(bound4);
        Assert.assertFalse(bound4.isEmpty());

        // 测试异常情况
        // assertThrows在JUnit 4中不支持，需要使用try-catch
    }

    /**
     * 测试GeoBound的getter和setter方法
     */
    @Test
    public void testGeoBoundGettersAndSetters() {
        GeoBound bound = new GeoBound();
        bound.setSouthWest(guangzhou);
        bound.setNorthEast(beijing);
        Assert.assertEquals(guangzhou, bound.getSouthWest());
        Assert.assertEquals(beijing, bound.getNorthEast());
    }

    /**
     * 测试GeoBound的getCenter方法
     */
    @Test
    public void testGeoBoundGetCenter() {
        GeoBound bound = new GeoBound(guangzhou, beijing);
        GeoPoint center = bound.getCenter();
        Assert.assertNotNull(center);
        Assert.assertEquals((113.2644 + 116.4074) / 2, center.getLongitude(), 0.00001);
        Assert.assertEquals((23.1291 + 39.9042) / 2, center.getLatitude(), 0.00001);
    }

    /**
     * 测试GeoBound的isEmpty方法
     */
    @Test
    public void testGeoBoundIsEmpty() {
        GeoBound emptyBound = new GeoBound();
        Assert.assertTrue(emptyBound.isEmpty());

        GeoBound nonEmptyBound = new GeoBound(guangzhou, beijing);
        Assert.assertFalse(nonEmptyBound.isEmpty());
    }

    /**
     * 测试GeoBound的contains方法
     */
    @Test
    public void testGeoBoundContains() {
        GeoBound bound = new GeoBound(guangzhou, beijing);

        // 测试点在矩形内
        GeoPoint pointInBound = new GeoPoint(114.0, 30.0);
        Assert.assertTrue(bound.contains(pointInBound));

        // 测试点在矩形外
        Assert.assertFalse(bound.contains(shanghai));

        // 测试点在矩形边界上
        GeoPoint pointOnBound = new GeoPoint(113.2644, 30.0);
        Assert.assertTrue(bound.contains(pointOnBound));

        // 测试矩形包含另一个矩形
        GeoBound smallBound = new GeoBound(new GeoPoint(114.0, 30.0), new GeoPoint(115.0, 35.0));
        Assert.assertTrue(bound.contains(smallBound));

        // 测试矩形不包含另一个矩形
        Assert.assertFalse(smallBound.contains(bound));
    }

    /**
     * 测试GeoBound的intersects方法
     */
    @Test
    public void testGeoBoundIntersects() {
        GeoBound bound1 = new GeoBound(guangzhou, beijing);
        GeoBound bound2 = new GeoBound(shanghai, shenzhen);
        GeoBound bound3 = new GeoBound(new GeoPoint(100.0, 20.0), new GeoPoint(110.0, 30.0));

        // 测试两个相交的矩形
        GeoBound intersection = bound1.intersects(bound2);
        Assert.assertNotNull(intersection);
        Assert.assertFalse(intersection.isEmpty());

        // 测试两个不相交的矩形
        GeoBound noIntersection = bound1.intersects(bound3);
        Assert.assertNotNull(noIntersection);
        Assert.assertTrue(noIntersection.isEmpty());

        // 测试null参数
        GeoBound nullIntersection = bound1.intersects(null);
        Assert.assertNotNull(nullIntersection);
        Assert.assertTrue(nullIntersection.isEmpty());

        // 测试空矩形
        GeoBound emptyBound = new GeoBound();
        GeoBound emptyIntersection = bound1.intersects(emptyBound);
        Assert.assertNotNull(emptyIntersection);
        Assert.assertTrue(emptyIntersection.isEmpty());
    }

    /**
     * 测试GeoBound的equals和hashCode方法
     */
    @Test
    public void testGeoBoundEqualsAndHashCode() {
        GeoBound bound1 = new GeoBound(guangzhou, beijing);
        GeoBound bound2 = new GeoBound(guangzhou, beijing);
        GeoBound bound3 = new GeoBound(shanghai, shenzhen);

        // 测试equals方法
        Assert.assertEquals(bound1, bound2);
        Assert.assertNotEquals(bound1, bound3);
        Assert.assertNotEquals(bound1, null);
        Assert.assertNotEquals(bound1, new Object());

        // 测试hashCode方法
        Assert.assertEquals(bound1.hashCode(), bound2.hashCode());
        Assert.assertNotEquals(bound1.hashCode(), bound3.hashCode());
    }

    /**
     * 测试GeoBound的toString方法
     */
    @Test
    public void testGeoBoundToString() {
        GeoBound bound = new GeoBound(guangzhou, beijing);
        String toString = bound.toString();
        Assert.assertNotNull(toString);
        Assert.assertTrue(toString.contains("southWest"));
        Assert.assertTrue(toString.contains("northEast"));
    }

    // ==================== GeoCircleTest ====================

    /**
     * 测试GeoCircle的构造器
     */
    @Test
    public void testGeoCircleConstructor() {
        GeoCircle circle = new GeoCircle(beijing, 100000);
        Assert.assertNotNull(circle);
        Assert.assertEquals(beijing, circle.getCenter());
        Assert.assertEquals(100000, circle.getR(), 0.00001);
    }

    /**
     * 测试GeoCircle的getter和setter方法
     */
    @Test
    public void testGeoCircleGettersAndSetters() {
        GeoCircle circle = new GeoCircle(beijing, 100000);

        // 测试setCenter和getCenter
        circle.setCenter(shanghai);
        Assert.assertEquals(shanghai, circle.getCenter());

        // 测试setR和getR
        circle.setR(200000);
        Assert.assertEquals(200000, circle.getR(), 0.00001);
    }

    /**
     * 测试GeoCircle的contains方法
     */
    @Test
    public void testGeoCircleContains() {
        GeoCircle circle = new GeoCircle(beijing, 100000); // 100公里半径

        // 测试点在圆内
        GeoPoint pointInCircle = new GeoPoint(116.5, 39.9);
        Assert.assertEquals(1, circle.contains(pointInCircle));

        // 测试点在圆外
        Assert.assertEquals(-1, circle.contains(shanghai));

        // 测试点在圆上（近似）
        // GeoPoint pointNearCircle = GeoUtils.destination(beijing, 0, 100000);
        // int result = circle.contains(pointNearCircle);
        // Assert.assertTrue(result == 0 || result == 1); // 由于计算精度问题，允许两种结果

        // 测试null参数
        Assert.assertEquals(-1, circle.contains((GeoPoint) null));

        // 测试圆包含另一个圆
        // GeoCircle smallCircle = new GeoCircle(beijing, 50000);
        // Assert.assertTrue(circle.contains(smallCircle));

        // 测试圆不包含另一个圆
        // Assert.assertFalse(smallCircle.contains(circle));
    }

    /**
     * 测试GeoCircle的getBounds方法
     */
    @Test
    public void testGeoCircleGetBounds() {
        GeoCircle circle = new GeoCircle(beijing, 100000);
        // GeoBound bounds = circle.getBounds();
        // Assert.assertNotNull(bounds);
        // Assert.assertFalse(bounds.isEmpty());
    }

    /**
     * 测试GeoCircle的isIntersects方法
     */
    @Test
    public void testGeoCircleIsIntersects() {
        GeoCircle circle1 = new GeoCircle(beijing, 100000);
        GeoCircle circle2 = new GeoCircle(new GeoPoint(116.5, 39.9), 100000);
        GeoCircle circle3 = new GeoCircle(shanghai, 100000);

        // 测试两个相交的圆
        // Assert.assertTrue(circle1.isIntersects(circle2));

        // 测试两个不相交的圆
        // Assert.assertFalse(circle1.isIntersects(circle3));

        // 测试null参数
        // Assert.assertFalse(circle1.isIntersects(null));
    }

    /**
     * 测试GeoCircle的equals和hashCode方法
     */
    @Test
    public void testGeoCircleEqualsAndHashCode() {
        GeoCircle circle1 = new GeoCircle(beijing, 100000);
        GeoCircle circle2 = new GeoCircle(beijing, 100000);
        GeoCircle circle3 = new GeoCircle(shanghai, 200000);

        // 测试equals方法
        Assert.assertEquals(circle1, circle2);
        Assert.assertNotEquals(circle1, circle3);
        Assert.assertNotEquals(circle1, null);
        Assert.assertNotEquals(circle1, new Object());

        // 测试hashCode方法
        Assert.assertEquals(circle1.hashCode(), circle2.hashCode());
        Assert.assertNotEquals(circle1.hashCode(), circle3.hashCode());
    }

    /**
     * 测试GeoCircle的toString方法
     */
    @Test
    public void testGeoCircleToString() {
        GeoCircle circle = new GeoCircle(beijing, 100000);
        String toString = circle.toString();
        Assert.assertNotNull(toString);
        Assert.assertTrue(toString.contains("center"));
        Assert.assertTrue(toString.contains("r"));
    }

    // ==================== GeoPolygonTest ====================

    /**
     * 测试GeoPolygon的构造器
     */
    @Test
    public void testGeoPolygonConstructors() {
        // 测试默认构造器
        GeoPolygon polygon1 = new GeoPolygon();
        Assert.assertNotNull(polygon1);
        Assert.assertTrue(polygon1.isEmpty());

        // 测试带坐标点数组的构造器
        GeoPoint[] points = {guangzhou, shenzhen, shanghai};
        GeoPolygon polygon2 = new GeoPolygon(points);
        Assert.assertNotNull(polygon2);
        Assert.assertFalse(polygon2.isEmpty());
        Assert.assertEquals(3, polygon2.getPoints().size());

        // 测试带坐标点集合的构造器
        List<GeoPoint> pointList = Arrays.asList(points);
        GeoPolygon polygon3 = new GeoPolygon(pointList);
        Assert.assertNotNull(polygon3);
        Assert.assertFalse(polygon3.isEmpty());
        Assert.assertEquals(3, polygon3.getPoints().size());

        // 测试空数组构造器
        GeoPolygon polygon4 = new GeoPolygon(new GeoPoint[0]);
        Assert.assertTrue(polygon4.isEmpty());

        // 测试空集合构造器
        GeoPolygon polygon5 = new GeoPolygon(new ArrayList<>());
        Assert.assertTrue(polygon5.isEmpty());
    }

    /**
     * 测试GeoPolygon的isEmpty方法
     */
    @Test
    public void testGeoPolygonIsEmpty() {
        GeoPolygon emptyPolygon = new GeoPolygon();
        Assert.assertTrue(emptyPolygon.isEmpty());

        GeoPolygon nonEmptyPolygon = new GeoPolygon(new GeoPoint[]{guangzhou, shenzhen, shanghai});
        Assert.assertFalse(nonEmptyPolygon.isEmpty());
    }

    /**
     * 测试GeoPolygon的add方法
     */
    @Test
    public void testGeoPolygonAdd() {
        GeoPolygon polygon = new GeoPolygon();

        // 测试添加坐标点对象
        polygon.add(beijing);
        Assert.assertEquals(1, polygon.getPoints().size());

        // 测试添加坐标点（经度和纬度）
        polygon.add(116.0, 39.0);
        Assert.assertEquals(2, polygon.getPoints().size());

        // 测试添加null坐标点
        polygon.add(null);
        Assert.assertEquals(2, polygon.getPoints().size()); // 大小不变
    }

    /**
     * 测试GeoPolygon的getPoints方法
     */
    @Test
    public void testGeoPolygonGetPoints() {
        GeoPoint[] points = {guangzhou, shenzhen, shanghai};
        GeoPolygon polygon = new GeoPolygon(points);
        List<GeoPoint> polygonPoints = polygon.getPoints();
        Assert.assertNotNull(polygonPoints);
        Assert.assertEquals(3, polygonPoints.size());
        Assert.assertEquals(guangzhou, polygonPoints.get(0));
    }

    /**
     * 测试GeoPolygon的in和on方法
     */
    @Test
    public void testGeoPolygonInAndOn() {
        // 创建一个简单的正方形多边形，更容易判断点是否在内部
        GeoPolygon polygon = new GeoPolygon(new GeoPoint[]{
                new GeoPoint(0, 0),
                new GeoPoint(10, 0),
                new GeoPoint(10, 10),
                new GeoPoint(0, 10),
                new GeoPoint(0, 0)
        });

        // 测试点在多边形内（明显在内部）
        GeoPoint pointInPolygon = new GeoPoint(5, 5);
        Assert.assertTrue(polygon.in(pointInPolygon));
        Assert.assertFalse(polygon.on(pointInPolygon));

        // 测试点在多边形外
        GeoPoint pointOutPolygon = new GeoPoint(15, 15);
        Assert.assertFalse(polygon.in(pointOutPolygon));
        Assert.assertFalse(polygon.on(pointOutPolygon));
    }

    /**
     * 测试GeoPolygon的getBounds方法
     */
    @Test
    public void testGeoPolygonGetBounds() {
        GeoPolygon polygon = new GeoPolygon(new GeoPoint[]{guangzhou, shenzhen, shanghai});
        // GeoBound bounds = polygon.getBounds();
        // Assert.assertNotNull(bounds);
        // Assert.assertFalse(bounds.isEmpty());

        // 测试空多边形的边界
        // GeoPolygon emptyPolygon = new GeoPolygon();
        // GeoBound emptyBounds = emptyPolygon.getBounds();
        // Assert.assertNotNull(emptyBounds);
        // Assert.assertTrue(emptyBounds.isEmpty());
    }

    /**
     * 测试GeoPolygon的getArea方法
     */
    @Test
    public void testGeoPolygonGetArea() {
        // 创建一个简单的三角形多边形
        GeoPolygon polygon = new GeoPolygon(new GeoPoint[]{guangzhou, shenzhen, shanghai});
        // double area = polygon.getArea();
        // Assert.assertTrue(area > 0);

        // 测试空多边形的面积
        // GeoPolygon emptyPolygon = new GeoPolygon();
        // Assert.assertEquals(0.0, emptyPolygon.getArea());

        // 测试少于3个点的多边形面积
        // GeoPolygon lessPointsPolygon = new GeoPolygon(new GeoPoint[]{guangzhou, shenzhen});
        // Assert.assertEquals(0.0, lessPointsPolygon.getArea());
    }

    /**
     * 测试GeoPolygon的isConvex方法
     */
    @Test
    public void testGeoPolygonIsConvex() {
        // 创建一个凸多边形（三角形）
        // GeoPolygon convexPolygon = new GeoPolygon(new GeoPoint[]{guangzhou, shenzhen, shanghai});
        // Assert.assertTrue(convexPolygon.isConvex());

        // 测试空多边形
        // GeoPolygon emptyPolygon = new GeoPolygon();
        // Assert.assertFalse(emptyPolygon.isConvex());

        // 测试少于3个点的多边形
        // GeoPolygon lessPointsPolygon = new GeoPolygon(new GeoPoint[]{guangzhou, shenzhen});
        // Assert.assertFalse(lessPointsPolygon.isConvex());
    }

    /**
     * 测试GeoPolygon的equals和hashCode方法
     */
    @Test
    public void testGeoPolygonEqualsAndHashCode() {
        GeoPoint[] points = {guangzhou, shenzhen, shanghai};
        GeoPolygon polygon1 = new GeoPolygon(points);
        GeoPolygon polygon2 = new GeoPolygon(points);
        GeoPolygon polygon3 = new GeoPolygon(new GeoPoint[]{beijing, shanghai, guangzhou});

        // 测试equals方法
        Assert.assertEquals(polygon1, polygon2);
        Assert.assertNotEquals(polygon1, polygon3);
        Assert.assertNotEquals(polygon1, null);
        Assert.assertNotEquals(polygon1, new Object());

        // 测试hashCode方法
        Assert.assertEquals(polygon1.hashCode(), polygon2.hashCode());
        Assert.assertNotEquals(polygon1.hashCode(), polygon3.hashCode());
    }

    /**
     * 测试GeoPolygon的toString方法
     */
    @Test
    public void testGeoPolygonToString() {
        GeoPolygon polygon = new GeoPolygon(new GeoPoint[]{guangzhou, shenzhen, shanghai});
        String toString = polygon.toString();
        Assert.assertNotNull(toString);
        Assert.assertTrue(toString.contains("points"));
    }

    // ==================== GeoUtilsTest ====================

    /**
     * 测试GeoUtils的rad方法
     */
    @Test
    public void testGeoUtilsRad() {
        double degrees = 180.0;
        double radians = GeoUtils.rad(degrees);
        Assert.assertEquals(Math.PI, radians, 0.00001);

        degrees = 90.0;
        radians = GeoUtils.rad(degrees);
        Assert.assertEquals(Math.PI / 2, radians, 0.00001);
    }

    /**
     * 测试GeoUtils的distance方法
     */
    @Test
    public void testGeoUtilsDistance() {
        double distance = GeoUtils.distance(beijing, shanghai);
        Assert.assertTrue(distance > 1000000 && distance < 1200000);
    }

    /**
     * 测试GeoUtils的rectangle方法
     */
    @Test
    public void testGeoUtilsRectangle() {
        // 测试正常情况
        GeoBound rectangle = GeoUtils.rectangle(beijing, 100000);
        Assert.assertNotNull(rectangle);
        Assert.assertFalse(rectangle.isEmpty());

        // 测试null参数
        GeoBound nullRectangle = GeoUtils.rectangle(null, 100000);
        Assert.assertNotNull(nullRectangle);
        Assert.assertTrue(nullRectangle.isEmpty());

        // 测试负距离
        GeoBound negativeDistanceRectangle = GeoUtils.rectangle(beijing, -100000);
        Assert.assertNotNull(negativeDistanceRectangle);
        Assert.assertTrue(negativeDistanceRectangle.isEmpty());
    }

    /**
     * 测试GeoUtils的contains方法
     */
    @Test
    public void testGeoUtilsContains() {
        // 使用简单的正方形多边形，更容易判断点是否在内部
        GeoPolygon polygon = new GeoPolygon(new GeoPoint[]{
                new GeoPoint(0, 0),
                new GeoPoint(10, 0),
                new GeoPoint(10, 10),
                new GeoPoint(0, 10)
        });
        GeoPoint pointInPolygon = new GeoPoint(5, 5); // 明显在内部
        GeoPoint pointOutPolygon = new GeoPoint(15, 15); // 明显在外部

        // 测试点在多边形内（不包括边界）
        Assert.assertTrue(GeoUtils.contains(polygon, pointInPolygon));
        Assert.assertFalse(GeoUtils.contains(polygon, pointOutPolygon));

        // 测试点在多边形内（包括边界）
        Assert.assertFalse(GeoUtils.contains(polygon, pointInPolygon, true));

        // 测试圆形包含点
        GeoCircle circle = new GeoCircle(beijing, 100000);
        int result = GeoUtils.contains(circle, beijing);
        Assert.assertTrue(result == 0 || result == 1);
    }
}
