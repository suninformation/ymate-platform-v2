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
 * GeoCircle测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-13 15:30:00
 * @since 2.1.4
 */
public class GeoCircleTest {

    private static final double BEIJING_LONGITUDE = 116.404;
    private static final double BEIJING_LATITUDE = 39.915;

    @Test
    public void testConstructor() {
        // 测试带中心点构造函数
        GeoPoint center = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE);
        GeoCircle circle = new GeoCircle(center, 1000);
        Assert.assertNotNull(circle);
        Assert.assertEquals(center, circle.getCenter());
        Assert.assertEquals(1000, circle.getR(), 0.0001);
    }

    @Test
    public void testSetterGetter() {
        GeoCircle circle = new GeoCircle(new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE), 1000);

        // 测试设置圆心
        GeoPoint newCenter = new GeoPoint(121.4737, 31.2304);
        circle.setCenter(newCenter);
        Assert.assertEquals(newCenter, circle.getCenter());

        // 测试设置半径
        circle.setR(2000);
        Assert.assertEquals(2000, circle.getR(), 0.0001);
    }

    @Test
    public void testContains() {
        GeoCircle circle = new GeoCircle(new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE), 1000);

        // 测试圆心点是否在圆内
        GeoPoint centerPoint = new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE);
        int centerResult = circle.contains(centerPoint);
        Assert.assertEquals(1, centerResult); // 点在圆内

        // 测试圆外的点
        GeoPoint outsidePoint = new GeoPoint(BEIJING_LONGITUDE + 2000 / 111000.0, BEIJING_LATITUDE);
        int outsideResult = circle.contains(outsidePoint);
        Assert.assertEquals(-1, outsideResult); // 点在圆外
    }

    @Test
    public void testEqualsAndHashCode() {
        GeoCircle circle1 = new GeoCircle(new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE), 1000);
        GeoCircle circle2 = new GeoCircle(new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE), 1000);
        GeoCircle circle3 = new GeoCircle(new GeoPoint(121.4737, 31.2304), 2000);

        // 测试相等性
        Assert.assertEquals(circle1, circle2);
        Assert.assertNotEquals(circle1, circle3);
        Assert.assertNotEquals(circle1, null);
        Assert.assertNotEquals(circle1, new Object());

        // 测试哈希码
        Assert.assertEquals(circle1.hashCode(), circle2.hashCode());
        Assert.assertNotEquals(circle1.hashCode(), circle3.hashCode());
    }

    @Test
    public void testToString() {
        GeoCircle circle = new GeoCircle(new GeoPoint(BEIJING_LONGITUDE, BEIJING_LATITUDE), 1000);
        String toString = circle.toString();
        Assert.assertNotNull(toString);
        Assert.assertTrue(toString.contains("center="));
        Assert.assertTrue(toString.contains("r="));
    }
}
