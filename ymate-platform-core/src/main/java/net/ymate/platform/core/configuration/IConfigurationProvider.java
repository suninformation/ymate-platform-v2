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
 * 配置提供者接口，通过配置提供者来获取配置文件内容
 *
 * @author 刘镇 (suninformation@163.com) on 2011-8-27 上午12:23:13
 */
@Ignored
public interface IConfigurationProvider extends IConfigReader {

    /**
     * 返回当前配置文件分析器
     *
     * @return 配置文件分析器
     */
    IConfigFileParser getConfigFileParser();

    /**
     * 根据配置文件绝对路径加载配置
     *
     * @param cfgFileName 配置文件路径及名称
     * @throws Exception 加载配置文件可能产生的异常
     */
    void load(String cfgFileName) throws Exception;

    /**
     * 重新加载配置文件内容
     *
     * @throws Exception 加载配置文件可能产生的异常
     */
    void reload() throws Exception;

    /**
     * 获取当前加载的配置文件路径
     *
     * @return 返回文件路径名称
     */
    String getCfgFileName();

    /**
     * 获取指定分类中键对应的文字值，若为空则返回指定默认值
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
     * @return 属性集合
     */
    List<String> getList(String category, String key);

    /**
     * 获取指定分类中键的键值映射
     *
     * @param category 分类名称
     * @param key      键标识
     * @return 键值映射
     */
    Map<String, String> getMap(String category, String key);

    /**
     * 获取指定分类中键的键值数组
     *
     * @param category 分类名称
     * @param key      属性键
     * @param zeroSize 是否返回空元素数组
     * @return 键值数组
     */
    String[] getArray(String category, String key, boolean zeroSize);

    /**
     * 获取指定分类中键的对应的数字值，若为空则返回指定默认值
     *
     * @param category     分类名称
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 数字值
     */
    int getInt(String category, String key, int defaultValue);

    /**
     * 获取指定分类中键的布尔值，若为空则返回指定默认值
     *
     * @param category     分类名称
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 布尔值
     */
    boolean getBoolean(String category, String key, boolean defaultValue);

    /**
     * 获取指定分类中键的长整数，若为空则返回指定默认值
     *
     * @param category     分类名称
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 长整数
     */
    long getLong(String category, String key, long defaultValue);

    /**
     * 获取指定分类中键的浮点数，若为空则返回指定默认值
     *
     * @param category     分类名称
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 浮点数
     */
    float getFloat(String category, String key, float defaultValue);

    /**
     * 获取指定分类中键的双精度浮点数，若为空则返回指定默认值
     *
     * @param category     分类名称
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 双精度浮点数
     */
    double getDouble(String category, String key, double defaultValue);

    /**
     * 获取指定分类中键的接口实例类型，若不存在则返回指定默认值
     *
     * @param category       分类名称
     * @param key            属性键ø
     * @param defaultValue   默认值
     * @param interfaceClass 接口类型
     * @param <T>            类型
     * @return 接口实例对象
     */
    <T> T getClassImpl(String category, String key, String defaultValue, Class<T> interfaceClass);

    /**
     * 获得配置对象内部加载的配置项映射
     *
     * @param category 分类名称
     * @return 配置项映射
     */
    Map<String, String> toMap(String category);

    /**
     * 获取分类的名称集合
     *
     * @return 名称集合
     */
    List<String> getCategoryNames();

    /**
     * 判断键key的配置项是否存在
     *
     * @param category 集合名称
     * @param key      属性键
     * @return 如果存在配置项那么返回true，否则返回false
     */
    boolean contains(String category, String key);
}
