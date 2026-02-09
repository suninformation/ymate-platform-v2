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

import org.apache.commons.lang.NullArgumentException;
import org.junit.Assert;
import org.junit.Test;

/**
 * GeoBound测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-13 15:30:00
 * @since 2.1.4
 */
public class GeoBoundTest {

    private static final double BEIJING_LONGITUDE = 116.404;
    private static final double BEIJING_LATITUDE = 39.915;
    private static final double SHANGHAI_LONGITUDE = 121.4737;
    private static final double SHANGHAI_LATITUDE = 31.2304;

    @Test
    public void testConstructor() {
        // 测试无参构造函数
        GeoBound bound1 = new GeoBound();
        Assert.assertNull(bound1.getSouthWest());
        Assert.assertNull(bound1.getNorthEast());
        Assert.assertTrue(bound1.isEmpty());

        // 测试带两个点构造函数
        GeoPoint southWest = new GeoPoint(116.0, 39.0);
        GeoPoint northEast = new GeoPoint(117.0, 40.0);
        GeoBound bound2 = new GeoBound(southWest, northEast);
        Assert.assertEquals(southWest, bound2.getSouthWest());
        Assert.assertEquals(northEast, bound2.getNorthEast());
        Assert.assertFalse(bound2.isEmpty());

        // 测试带两个GeoBound构造函数
        GeoBound bound3 = new GeoBound(new GeoPoint(115.0, 38.0), new GeoPoint(116.0, 39.0));
        GeoBound bound4 = new GeoBound(new GeoPoint(116.0, 39.0), new GeoPoint(117.0, 40.0));
        GeoBound mergedBound = new GeoBound(bound3, bound4);
        Assert.assertNotNull(mergedBound.getSouthWest());
        Assert.assertNotNull(mergedBound.getNorthEast());
        // 合并后的边界应该是两个边界的并集
        Assert.assertEquals(115.0, mergedBound.getSouthWest().getLongitude(), 0.0001);
        Assert.assertEquals(38.0, mergedBound.getSouthWest().getLatitude(), 0.0001);
        Assert.assertEquals(117.0, mergedBound.getNorthEast().getLongitude(), 0.0001);
        Assert.assertEquals(40.0, mergedBound.getNorthEast().getLatitude(), 0.0001);
    }

    @Test(expected = NullArgumentException.class)
    public void testConstructorWithNullFirstBound() {
        // 测试第一个边界为null的情况
        new GeoBound(null, new GeoBound(new GeoPoint(116.0, 39.0), new GeoPoint(117.0, 40.0)));
    }

    @Test(expected = NullArgumentException.class)
    public void testConstructorWithEmptyFirstBound() {
        // 测试第一个边界为空的情况
        new GeoBound(new GeoBound(), new GeoBound(new GeoPoint(116.0, 39.0), new GeoPoint(117.0, 40.0)));
    }

    @Test(expected = NullArgumentException.class)
    public void testConstructorWithNullOtherBound() {
        // 测试第二个边界为null的情况
        new GeoBound(new GeoBound(new GeoPoint(116.0, 39.0), new GeoPoint(117.0, 40.0)), null);
    }

    @Test(expected = NullArgumentException.class)
    public void testConstructorWithEmptyOtherBound() {
        // 测试第二个边界为空的情况
        new GeoBound(new GeoBound(new GeoPoint(116.0, 39.0), new GeoPoint(117.0, 40.0)), new GeoBound());
    }

    @Test
    public void testSetterGetter() {
        GeoBound bound = new GeoBound();

        // 测试设置西南角
        GeoPoint southWest = new GeoPoint(116.0, 39.0);
        bound.setSouthWest(southWest);
        Assert.assertEquals(southWest, bound.getSouthWest());

        // 测试设置东北角
        GeoPoint northEast = new GeoPoint(117.0, 40.0);
        bound.setNorthEast(northEast);
        Assert.assertEquals(northEast, bound.getNorthEast());
    }

    @Test
    public void testGetCenter() {
        // 测试空边界的中心点
        GeoBound emptyBound = new GeoBound();
        try {
            emptyBound.getCenter();
            Assert.fail("应该抛出NullPointerException");
        } catch (NullPointerException e) {
            // 预期异常
        }

        // 测试非空边界的中心点
        GeoPoint southWest = new GeoPoint(116.0, 39.0);
        GeoPoint northEast = new GeoPoint(117.0, 40.0);
        GeoBound bound = new GeoBound(southWest, northEast);
        GeoPoint center = bound.getCenter();
        Assert.assertNotNull(center);
        Assert.assertEquals(116.5, center.getLongitude(), 0.0001);
        Assert.assertEquals(39.5, center.getLatitude(), 0.0001);
    }

    @Test
    public void testIsEmpty() {
        // 测试空边界
        GeoBound emptyBound = new GeoBound();
        Assert.assertTrue(emptyBound.isEmpty());

        // 测试非空边界
        GeoPoint southWest = new GeoPoint(116.0, 39.0);
        GeoPoint northEast = new GeoPoint(117.0, 40.0);
        GeoBound bound = new GeoBound(southWest, northEast);
        Assert.assertFalse(bound.isEmpty());

        // 测试只有西南角的边界
        GeoBound boundWithSouthWest = new GeoBound();
        boundWithSouthWest.setSouthWest(southWest);
        Assert.assertTrue(boundWithSouthWest.isEmpty());

        // 测试只有东北角的边界
        GeoBound boundWithNorthEast = new GeoBound();
        boundWithNorthEast.setNorthEast(northEast);
        Assert.assertTrue(boundWithNorthEast.isEmpty());
    }

    @Test
    public void testContainsPoint() {
        GeoPoint southWest = new GeoPoint(116.0, 39.0);
        GeoPoint northEast = new GeoPoint(117.0, 40.0);
        GeoBound bound = new GeoBound(southWest, northEast);

        // 测试边界内的点
        GeoPoint insidePoint = new GeoPoint(116.5, 39.5);
        Assert.assertTrue(bound.contains(insidePoint));

        // 测试边界上的点（西南角）
        Assert.assertTrue(bound.contains(southWest));

        // 测试边界上的点（东北角）
        Assert.assertTrue(bound.contains(northEast));

        // 测试边界外的点（经度小于西南角）
        GeoPoint outsideWestPoint = new GeoPoint(115.9, 39.5);
        Assert.assertFalse(bound.contains(outsideWestPoint));

        // 测试边界外的点（经度大于东北角）
        GeoPoint outsideEastPoint = new GeoPoint(117.1, 39.5);
        Assert.assertFalse(bound.contains(outsideEastPoint));

        // 测试边界外的点（纬度小于西南角）
        GeoPoint outsideSouthPoint = new GeoPoint(116.5, 38.9);
        Assert.assertFalse(bound.contains(outsideSouthPoint));

        // 测试边界外的点（纬度大于东北角）
        GeoPoint outsideNorthPoint = new GeoPoint(116.5, 40.1);
        Assert.assertFalse(bound.contains(outsideNorthPoint));

        // 测试空边界
        GeoBound emptyBound = new GeoBound();
        Assert.assertFalse(emptyBound.contains(insidePoint));
    }

    @Test
    public void testContainsBound() {
        GeoPoint southWest1 = new GeoPoint(116.0, 39.0);
        GeoPoint northEast1 = new GeoPoint(117.0, 40.0);
        GeoBound bound1 = new GeoBound(southWest1, northEast1);

        // 测试完全包含的边界
        GeoPoint southWest2 = new GeoPoint(116.2, 39.2);
        GeoPoint northEast2 = new GeoPoint(116.8, 39.8);
        GeoBound bound2 = new GeoBound(southWest2, northEast2);
        Assert.assertTrue(bound1.contains(bound2));

        // 测试部分重叠的边界
        GeoPoint southWest3 = new GeoPoint(116.5, 39.5);
        GeoPoint northEast3 = new GeoPoint(117.5, 40.5);
        GeoBound bound3 = new GeoBound(southWest3, northEast3);
        Assert.assertFalse(bound1.contains(bound3));

        // 测试完全不重叠的边界
        GeoPoint southWest4 = new GeoPoint(118.0, 41.0);
        GeoPoint northEast4 = new GeoPoint(119.0, 42.0);
        GeoBound bound4 = new GeoBound(southWest4, northEast4);
        Assert.assertFalse(bound1.contains(bound4));
    }

    @Test
    public void testIntersects() {
        GeoPoint southWest1 = new GeoPoint(116.0, 39.0);
        GeoPoint northEast1 = new GeoPoint(117.0, 40.0);
        GeoBound bound1 = new GeoBound(southWest1, northEast1);

        // 测试与完全包含的边界的交集
        GeoPoint southWest2 = new GeoPoint(116.2, 39.2);
        GeoPoint northEast2 = new GeoPoint(116.8, 39.8);
        GeoBound bound2 = new GeoBound(southWest2, northEast2);
        GeoBound intersection1 = bound1.intersects(bound2);
        Assert.assertNotNull(intersection1.getSouthWest());
        Assert.assertNotNull(intersection1.getNorthEast());
        Assert.assertEquals(bound2.getSouthWest(), intersection1.getSouthWest());
        Assert.assertEquals(bound2.getNorthEast(), intersection1.getNorthEast());

        // 测试与部分重叠的边界的交集
        GeoPoint southWest3 = new GeoPoint(116.5, 39.5);
        GeoPoint northEast3 = new GeoPoint(117.5, 40.5);
        GeoBound bound3 = new GeoBound(southWest3, northEast3);
        GeoBound intersection2 = bound1.intersects(bound3);
        Assert.assertNotNull(intersection2.getSouthWest());
        Assert.assertNotNull(intersection2.getNorthEast());
        // 交集应该是两个边界的重叠部分
        Assert.assertEquals(116.5, intersection2.getSouthWest().getLongitude(), 0.0001);
        Assert.assertEquals(39.5, intersection2.getSouthWest().getLatitude(), 0.0001);
        Assert.assertEquals(117.0, intersection2.getNorthEast().getLongitude(), 0.0001);
        Assert.assertEquals(40.0, intersection2.getNorthEast().getLatitude(), 0.0001);

        // 测试与完全不重叠的边界的交集
        GeoPoint southWest4 = new GeoPoint(118.0, 41.0);
        GeoPoint northEast4 = new GeoPoint(119.0, 42.0);
        GeoBound bound4 = new GeoBound(southWest4, northEast4);
        GeoBound intersection3 = bound1.intersects(bound4);
        Assert.assertTrue(intersection3.isEmpty());

        // 测试与null边界的交集
        GeoBound intersection4 = bound1.intersects(null);
        Assert.assertTrue(intersection4.isEmpty());

        // 测试与空边界的交集
        GeoBound intersection5 = bound1.intersects(new GeoBound());
        Assert.assertTrue(intersection5.isEmpty());

        // 测试空边界与其他边界的交集
        GeoBound intersection6 = new GeoBound().intersects(bound1);
        Assert.assertTrue(intersection6.isEmpty());
    }

    @Test
    public void testEqualsAndHashCode() {
        GeoPoint southWest = new GeoPoint(116.0, 39.0);
        GeoPoint northEast = new GeoPoint(117.0, 40.0);
        GeoBound bound1 = new GeoBound(southWest, northEast);
        GeoBound bound2 = new GeoBound(southWest, northEast);
        GeoBound bound3 = new GeoBound(new GeoPoint(116.1, 39.1), new GeoPoint(117.1, 40.1));

        // 测试相等性
        Assert.assertEquals(bound1, bound2);
        Assert.assertNotEquals(bound1, bound3);
        Assert.assertNotEquals(bound1, null);
        Assert.assertNotEquals(bound1, new Object());

        // 测试哈希码
        Assert.assertEquals(bound1.hashCode(), bound2.hashCode());
        Assert.assertNotEquals(bound1.hashCode(), bound3.hashCode());
    }

    @Test
    public void testToString() {
        GeoPoint southWest = new GeoPoint(116.0, 39.0);
        GeoPoint northEast = new GeoPoint(117.0, 40.0);
        GeoBound bound = new GeoBound(southWest, northEast);
        String toString = bound.toString();
        Assert.assertNotNull(toString);
        Assert.assertTrue(toString.contains("southWest="));
        Assert.assertTrue(toString.contains("northEast="));
        Assert.assertTrue(toString.contains(southWest.toString()));
        Assert.assertTrue(toString.contains(northEast.toString()));
    }
}
