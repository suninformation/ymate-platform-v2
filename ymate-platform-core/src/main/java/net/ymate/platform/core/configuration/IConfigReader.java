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
 * @author 刘镇 (suninformation@163.com) on 2018/1/1 下午9:09
 */
@Ignored
public interface IConfigReader {

    /**
     * 获得对应的文字值
     *
     * @param key 属性键
     * @return 属性值
     */
    String getString(String key);

    /**
     * 获得对应的文字值，若为空则返回指定默认值
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 属性值
     */
    String getString(String key, String defaultValue);

    /**
     * 获得对应的文字值列表，其中匹配以key开头的键串
     *
     * @param key 属性键
     * @return 文字值列表
     */
    List<String> getList(String key);

    /**
     * 获取键值映射
     *
     * @param keyHead 键头标识
     * @return 键值映射
     */
    Map<String, String> getMap(String keyHead);

    /**
     * 获取键值数组值
     *
     * @param key 属性键
     * @return 键值数组
     */
    String[] getArray(String key);

    /**
     * 获取键值数组值
     *
     * @param key      属性键
     * @param zeroSize 是否返回空元素数组
     * @return 键值数组
     */
    String[] getArray(String key, boolean zeroSize);

    /**
     * 获得对应的数字值
     *
     * @param key 属性键
     * @return 数字值
     */
    int getInt(String key);

    /**
     * 获得对应的数字值，若不存在则返回指定默认值
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 数字值
     */
    int getInt(String key, int defaultValue);

    /**
     * 获得对应的布尔值
     *
     * @param key 属性键
     * @return 布尔值
     */
    boolean getBoolean(String key);

    /**
     * 获得对应的布尔值，若不存在则返回指定默认值
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 布尔值
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * 获取长整数
     *
     * @param key 属性键
     * @return 长整数
     */
    long getLong(String key);

    /**
     * 获取长整数，若不存在则返回指定默认值
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 长整数
     */
    long getLong(String key, long defaultValue);

    /**
     * 获取浮点数
     *
     * @param key 属性键
     * @return 浮点数
     */
    float getFloat(String key);

    /**
     * 获取浮点数，若不存在则返回指定默认值
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 浮点数
     */
    float getFloat(String key, float defaultValue);

    /**
     * 获取双精度浮点数
     *
     * @param key 属性键
     * @return 双精度浮点数
     */
    double getDouble(String key);

    /**
     * 获取双精度浮点数，若不存在则返回指定默认值
     *
     * @param key          属性键
     * @param defaultValue 默认值
     * @return 双精度浮点数
     */
    double getDouble(String key, double defaultValue);

    /**
     * 获取接口实例类型
     *
     * @param key            属性键
     * @param interfaceClass 接口类型
     * @param <T>            类型
     * @return 接口实例对象
     */
    <T> T getClassImpl(String key, Class<T> interfaceClass);

    /**
     * 获取接口实例类型，若不存在则返回指定默认值
     *
     * @param key            属性键
     * @param defaultValue   默认值
     * @param interfaceClass 接口类型
     * @param <T>            类型
     * @return 接口实例对象
     */
    <T> T getClassImpl(String key, String defaultValue, Class<T> interfaceClass);

    /**
     * 获得配置对象内部加载的配置项映射
     *
     * @return 配置项映射
     */
    Map<String, String> toMap();

    /**
     * 判断键key的配置项是否存在
     *
     * @param key 属性键
     * @return 如果存在配置项那么返回true，否则返回false
     */
    boolean contains(String key);
}
