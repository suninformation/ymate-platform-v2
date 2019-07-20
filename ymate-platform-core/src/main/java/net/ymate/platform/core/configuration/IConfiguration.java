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
package net.ymate.platform.core.configuration;

import net.ymate.platform.core.beans.annotation.Ignored;

import java.util.List;
import java.util.Map;

/**
 * 配置对象接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-27 上午12:20:51
 */
@Ignored
public interface IConfiguration extends IConfigReader {

    /**
     * 获取指定分类中键的指定键名的属性对象
     *
     * @param category 分类名称
     * @param key      属性键
     * @return 属性对象
     */
    IConfigFileParser.Property getProperty(String category, String key);

    /**
     * 获取指定键名的属性对象
     *
     * @param key 属性键
     * @return 属性对象
     */
    IConfigFileParser.Property getProperty(String key);

    /**
     * 获取指定分类中键的对应的文字值，若为空则返回指定默认值
     *
     * @param category     分类名称
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值
     */
    String getString(String category, String key, String defaultValue);

    /**
     * 获取指定分类中键的对应的文字值列表，其中匹配以key开头的键串
     *
     * @param category 分类名称
     * @param key      属性键
     * @return 文字值列表
     */
    List<String> getList(String category, String key);

    /**
     * 获取指定分类中键的键值映射
     *
     * @param category 分类名称
     * @param keyHead  键头标识
     * @return 键值映射
     */
    Map<String, String> getMap(String category, String keyHead);

    /**
     * 获取指定分类中键的数组值
     *
     * @param category 分类名称
     * @param key      属性键
     * @param zeroSize 是否返回空元素数组
     * @return 键值数组
     */
    String[] getArray(String category, String key, boolean zeroSize);

    /**
     * 获取指定分类中键的对应的数字值，若不存在则返回指定默认值
     *
     * @param category     分类名称
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 数字值
     */
    int getInt(String category, String key, int defaultValue);

    /**
     * 获取指定分类中键的对应的布尔值，若不存在则返回指定默认值
     *
     * @param category     分类名称
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 布尔值
     */
    boolean getBoolean(String category, String key, boolean defaultValue);

    /**
     * 获取指定分类中键的配置对象内部加载的配置项映射
     *
     * @param category 分类名称
     * @return 配置项映射
     */
    Map<String, String> toMap(String category);

    /**
     * 获取分类的名称集合
     *
     * @return 分类名称集合
     */
    List<String> getCategoryNames();

    /**
     * 判断指定分类中是否包含指定的键
     *
     * @param category 分类名称
     * @param key      键名
     * @return 返回是否存在
     */
    boolean contains(String category, String key);

    /**
     * 初始化配置项，一般用来从provider中获取配置项，是实现自定义的配置项进入内存的位置
     *
     * @param provider 配置提供者
     */
    void initialize(IConfigurationProvider provider);

    /**
     * 重新加载配置文件内容
     *
     * @throws Exception 加载配置文件可能产生的异常
     */
    void reload() throws Exception;

    /**
     * 获得配置文件自定义标签名称
     *
     * @return 返回值加在配置文件名与扩展名中间，形成XXXX.YYY.xml形式，其中需要返回.YYY
     */
    String getTagName();
}