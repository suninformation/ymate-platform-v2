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

import java.util.ArrayList;
import java.util.List;

/**
 * GeoPolygon测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-13 15:30:00
 * @since 2.1.4
 */
public class GeoPolygonTest {

    @Test
    public void testConstructor() {
        // 测试无参构造函数
        GeoPolygon polygon1 = new GeoPolygon();
        Assert.assertTrue(polygon1.isEmpty());

        // 测试带GeoPoint数组构造函数
        GeoPoint[] pointsArray = new GeoPoint[]{
                new GeoPoint(116.0, 39.0),
                new GeoPoint(117.0, 39.0),
                new GeoPoint(117.0, 40.0),
                new GeoPoint(116.0, 40.0)
        };
        GeoPolygon polygon2 = new GeoPolygon(pointsArray);
        Assert.assertFalse(polygon2.isEmpty());
        Assert.assertEquals(4, polygon2.getPoints().size());

        // 测试带List构造函数
        List<GeoPoint> pointsList = new ArrayList<>();
        pointsList.add(new GeoPoint(116.0, 39.0));
        pointsList.add(new GeoPoint(117.0, 39.0));
        pointsList.add(new GeoPoint(117.0, 40.0));
        pointsList.add(new GeoPoint(116.0, 40.0));
        GeoPolygon polygon3 = new GeoPolygon(pointsList);
        Assert.assertFalse(polygon3.isEmpty());
        Assert.assertEquals(4, polygon3.getPoints().size());

        // 测试带空数组构造函数
        GeoPolygon polygon4 = new GeoPolygon(new GeoPoint[0]);
        Assert.assertTrue(polygon4.isEmpty());

        // 测试带空List构造函数
        GeoPolygon polygon5 = new GeoPolygon(new ArrayList<>());
        Assert.assertTrue(polygon5.isEmpty());
    }

    @Test
    public void testAdd() {
        GeoPolygon polygon = new GeoPolygon();
        Assert.assertTrue(polygon.isEmpty());

        // 测试添加GeoPoint
        GeoPoint point1 = new GeoPoint(116.0, 39.0);
        polygon.add(point1);
        Assert.assertFalse(polygon.isEmpty());
        Assert.assertEquals(1, polygon.getPoints().size());
        Assert.assertEquals(point1, polygon.getPoints().get(0));

        // 测试添加经纬度
        polygon.add(117.0, 39.0);
        Assert.assertEquals(2, polygon.getPoints().size());
        Assert.assertEquals(117.0, polygon.getPoints().get(1).getLongitude(), 0.0001);
        Assert.assertEquals(39.0, polygon.getPoints().get(1).getLatitude(), 0.0001);

        // 测试添加null点
        polygon.add(null);
        Assert.assertEquals(2, polygon.getPoints().size()); // 应该不会增加
    }

    @Test
    public void testIsEmpty() {
        // 测试空多边形
        GeoPolygon emptyPolygon = new GeoPolygon();
        Assert.assertTrue(emptyPolygon.isEmpty());

        // 测试非空多边形
        GeoPolygon nonEmptyPolygon = new GeoPolygon();
        nonEmptyPolygon.add(new GeoPoint(116.0, 39.0));
        Assert.assertFalse(nonEmptyPolygon.isEmpty());
    }

    @Test
    public void testIn() {
        // 创建一个矩形多边形（北京市中心区域）
        GeoPolygon polygon = new GeoPolygon();
        polygon.add(116.0, 39.0);
        polygon.add(117.0, 39.0);
        polygon.add(117.0, 40.0);
        polygon.add(116.0, 40.0);

        // 测试多边形内部的点
        GeoPoint insidePoint = new GeoPoint(116.5, 39.5);
        Assert.assertTrue(polygon.in(insidePoint));

        // 测试多边形外部的点
        GeoPoint outsidePoint = new GeoPoint(115.5, 39.5);
        Assert.assertFalse(polygon.in(outsidePoint));

        // 测试多边形顶点
        GeoPoint vertexPoint = new GeoPoint(116.0, 39.0);
        Assert.assertFalse(polygon.in(vertexPoint)); // 顶点不在内部

        // 测试多边形边上的点
        GeoPoint edgePoint = new GeoPoint(116.5, 39.0);
        Assert.assertFalse(polygon.in(edgePoint)); // 边上的点不在内部
    }

    @Test
    public void testOn() {
        // 创建一个矩形多边形（北京市中心区域）
        GeoPolygon polygon = new GeoPolygon();
        polygon.add(116.0, 39.0);
        polygon.add(117.0, 39.0);
        polygon.add(117.0, 40.0);
        polygon.add(116.0, 40.0);

        // 测试多边形顶点
        GeoPoint vertexPoint = new GeoPoint(116.0, 39.0);
        Assert.assertTrue(polygon.on(vertexPoint)); // 顶点在边上

        // 测试多边形边上的点
        GeoPoint edgePoint = new GeoPoint(116.5, 39.0);
        Assert.assertTrue(polygon.on(edgePoint)); // 边上的点在边上

        // 测试多边形内部的点
        GeoPoint insidePoint = new GeoPoint(116.5, 39.5);
        Assert.assertFalse(polygon.on(insidePoint)); // 内部点不在边上

        // 测试多边形外部的点
        GeoPoint outsidePoint = new GeoPoint(115.5, 39.5);
        Assert.assertFalse(polygon.on(outsidePoint)); // 外部点不在边上
    }

    @Test
    public void testEqualsAndHashCode() {
        // 创建两个相同的多边形
        GeoPolygon polygon1 = new GeoPolygon();
        polygon1.add(116.0, 39.0);
        polygon1.add(117.0, 39.0);
        polygon1.add(117.0, 40.0);
        polygon1.add(116.0, 40.0);

        GeoPolygon polygon2 = new GeoPolygon();
        polygon2.add(116.0, 39.0);
        polygon2.add(117.0, 39.0);
        polygon2.add(117.0, 40.0);
        polygon2.add(116.0, 40.0);

        // 创建一个不同的多边形
        GeoPolygon polygon3 = new GeoPolygon();
        polygon3.add(116.0, 39.0);
        polygon3.add(117.0, 39.0);
        polygon3.add(117.0, 40.0);

        // 测试相等性
        Assert.assertEquals(polygon1, polygon2);
        Assert.assertNotEquals(polygon1, polygon3);
        Assert.assertNotEquals(polygon1, null);
        Assert.assertNotEquals(polygon1, new Object());

        // 测试哈希码
        Assert.assertEquals(polygon1.hashCode(), polygon2.hashCode());
        Assert.assertNotEquals(polygon1.hashCode(), polygon3.hashCode());
    }

    @Test
    public void testToString() {
        GeoPolygon polygon = new GeoPolygon();
        polygon.add(116.0, 39.0);
        polygon.add(117.0, 39.0);
        String toString = polygon.toString();
        Assert.assertNotNull(toString);
        Assert.assertTrue(toString.contains("GeoPolygon{points="));
        Assert.assertTrue(toString.contains("116.0"));
        Assert.assertTrue(toString.contains("39.0"));
        Assert.assertTrue(toString.contains("117.0"));
    }
}
