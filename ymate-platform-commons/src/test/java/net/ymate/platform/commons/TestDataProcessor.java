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

import java.util.ArrayList;
import java.util.List;

/**
 * 测试用的数据处理器实现类
 * <p>
 * 用于测试分批导出功能。
 * </p>
 */
public class TestDataProcessor implements IExportDataProcessor {

    private final List<?> data;

    private final int batchSize;

    public TestDataProcessor(List<?> data, int batchSize) {
        this.data = data;
        this.batchSize = batchSize;
    }

    @Override
    public List<?> getData(int index) throws Exception {
        int start = (index - 1) * batchSize;
        if (start >= data.size()) {
            return null;
        }
        int end = Math.min(start + batchSize, data.size());
        return new ArrayList<>(data.subList(start, end));
    }
}
