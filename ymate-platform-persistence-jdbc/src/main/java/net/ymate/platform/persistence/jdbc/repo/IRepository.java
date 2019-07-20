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
package net.ymate.platform.persistence.jdbc.repo;

import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.configuration.IConfiguration;

/**
 * @author 刘镇 (suninformation@163.com) on 16/4/24 上午12:18
 */
@Ignored
public interface IRepository {

    /**
     * 获取配置
     *
     * @return 返回配置接口对象
     */
    IConfiguration getConfig();
}
