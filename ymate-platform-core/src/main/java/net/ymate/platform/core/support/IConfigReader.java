/*
 * Copyright 2007-2018 the original author or authors.
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

import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2018/1/1 下午9:09
 * @version 1.0
 */
public interface IConfigReader {

    /**
     * @param key 属性键
     * @return 获得对应的文字值
     */
    String getString(String key);

    String getString(String key, String defaultValue);

    /**
     * @param key 属性键
     * @return 获得对应的文字值列表，其中匹配以key开头的键串
     */
    List<String> getList(String key);

    /**
     * @param keyHead 键头标识
     * @return 获取键值映射
     */
    Map<String, String> getMap(String keyHead);

    /**
     * @param key 属性键
     * @return 获取键值数组值
     */
    String[] getArray(String key);

    String[] getArray(String key, boolean zeroSize);

    /**
     * @param key 属性键
     * @return 获得对应的数字值
     */
    int getInt(String key);

    int getInt(String key, int defaultValue);

    /**
     * @param key 属性键
     * @return 获得对应的布尔值
     */
    boolean getBoolean(String key);

    boolean getBoolean(String key, boolean defaultValue);

    /**
     * @param key 属性键
     * @return 获取长整数
     */
    long getLong(String key);

    long getLong(String key, long defaultValue);

    /**
     * @param key 属性键
     * @return 获取浮点数
     */
    float getFloat(String key);

    float getFloat(String key, float defaultValue);

    /**
     * @param key 属性键
     * @return 获取双精度浮点数
     */
    double getDouble(String key);

    double getDouble(String key, double defaultValue);

    /**
     * @return 获得配置对象内部加载的配置项映射
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
