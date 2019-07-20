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
package net.ymate.platform.commons;

import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/12/25 下午2:42
 */
public interface IExportDataProcessor {

    /**
     * 处理导出数据
     *
     * @param index 索引
     * @return 返回导出数据映射
     * @throws Exception 可能产生的任何异常
     */
    Map<String, Object> getData(int index) throws Exception;
}
