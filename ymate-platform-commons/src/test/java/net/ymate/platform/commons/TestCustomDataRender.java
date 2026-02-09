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
package net.ymate.platform.commons;

import net.ymate.platform.commons.lang.BlurObject;

/**
 * 自定义数据渲染器实现类
 * <p>
 * 用于测试自定义数据渲染功能。
 * </p>
 */
public class TestCustomDataRender implements IExportDataRender {

    @Override
    public Object render(Object rowData, ExportColumnMeta columnMeta, String fieldName, Object value, boolean importing) throws Exception {
        if (value == null) {
            return null;
        }
        if ("username".equals(fieldName)) {
            return "用户:" + value;
        } else if ("email".equals(fieldName)) {
            return value.toString().toLowerCase();
        }
        return BlurObject.bind(value).toStringValue();
    }
}
