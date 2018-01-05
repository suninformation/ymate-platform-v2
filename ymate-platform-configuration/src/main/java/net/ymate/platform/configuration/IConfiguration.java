/*
 * Copyright 2007-2017 the original author or authors.
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
package net.ymate.platform.configuration;

import net.ymate.platform.core.support.IConfigReader;

import java.util.List;
import java.util.Map;

/**
 * 配置对象接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-27 上午12:20:51
 * @version 1.0
 */
public interface IConfiguration extends IConfigReader {

    String getString(String category, String key, String defaultValue);

    List<String> getList(String category, String key);

    Map<String, String> getMap(String category, String keyHead);

    String[] getArray(String category, String key, boolean zeroSize);

    int getInt(String category, String key, int defaultValue);

    boolean getBoolean(String category, String key, boolean defaultValue);

    Map<String, String> toMap(String category);

    /**
     * @return 获取分类的名称集合
     */
    List<String> getCategoryNames();

    boolean contains(String category, String key);

    /**
     * 初始化配置项，一般用来从provider中获取配置项，是实现自定义的配置项进入内存的位置
     *
     * @param provider 配置提供者
     */
    void initialize(IConfigurationProvider provider);

    /**
     * @return 获得配置文件自定义标签名称，即返回值加在配置文件名与扩展名中间，形成XXXX.YYY.xml形式，其中需要返回.YYY
     */
    String getTagName();

}