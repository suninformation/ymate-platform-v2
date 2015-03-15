/*
 * Copyright 2007-2107 the original author or authors.
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
package net.ymate.platform.core;

import java.util.List;
import java.util.Map;

/**
 * YMP框架核心管理器初始化配置接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 15/3/11 下午5:41
 * @version 1.0
 */
public interface IConfig {

    /**
     * @return 返回是否为开发模式
     */
    public boolean isDevelopMode();

    /**
     * @return 返回框架自动扫描的包路径集合
     */
    public List<String> getAutoscanPackages();

    /**
     * @return 返回框架是否自动加载模块
     */
    public boolean isModuleAutoload();

    /**
     * @param moduleName 模块名称
     * @return 返回模块配置参数映射
     */
    public Map<String, String> getModuleConfigs(String moduleName);
}
