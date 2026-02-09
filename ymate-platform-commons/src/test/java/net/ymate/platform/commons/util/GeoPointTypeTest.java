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
 * GeoPointType测试类
 *
 * @author 刘镇 (suninformation@163.com) on 2026-01-13 15:30:00
 * @since 2.1.4
 */
public class GeoPointTypeTest {

    @Test
    public void testEnumValues() {
        // 验证枚举值数量
        GeoPointType[] values = GeoPointType.values();
        Assert.assertEquals(3, values.length);

        // 验证枚举值顺序和名称
        Assert.assertEquals(GeoPointType.WGS84, values[0]);
        Assert.assertEquals("WGS84", values[0].name());

        Assert.assertEquals(GeoPointType.GCJ02, values[1]);
        Assert.assertEquals("GCJ02", values[1].name());

        Assert.assertEquals(GeoPointType.BD09, values[2]);
        Assert.assertEquals("BD09", values[2].name());

        // 验证枚举值通过名称获取
        Assert.assertEquals(GeoPointType.WGS84, GeoPointType.valueOf("WGS84"));
        Assert.assertEquals(GeoPointType.GCJ02, GeoPointType.valueOf("GCJ02"));
        Assert.assertEquals(GeoPointType.BD09, GeoPointType.valueOf("BD09"));
    }
}
