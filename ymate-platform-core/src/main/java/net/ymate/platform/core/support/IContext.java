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
package net.ymate.platform.core.support;

import net.ymate.platform.core.IApplication;
import net.ymate.platform.core.beans.annotation.Ignored;

import java.io.Serializable;
import java.util.Map;

/**
 * 环境上下文接口
 *
 * @author 刘镇 (suninformation@163.com) on 2018/8/30 上午11:44
 * @since 2.0.6
 */
@Ignored
public interface IContext extends Serializable {

    /**
     * 获取所属应用管理器
     *
     * @return 返回所属应用管理器实例
     */
    IApplication getOwner();

    /**
     * 获取上下文参数映射
     *
     * @return 返回上下文参数映射
     */
    Map<String, String> getContextParams();
}
