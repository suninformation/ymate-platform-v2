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
package net.ymate.platform.configuration;

import java.util.List;
import java.util.Map;

/**
 * 配置对象接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 14-8-17
 * @version 1.0
 */
public interface IConfiguration {

    /**
     * @param key
     * @return 获得对应的文字值
     */
    public String getString(String key);

    public String getString(String key, String defaultValue);

    public String getString(String category, String key, String defaultValue);

    /**
     * @param key
     * @return 获得对应的文字值列表，其中匹配以key开头的键串
     */
    public List<String> getList(String key);

    public List<String> getList(String category, String key);

    /**
     * @param keyHead 键头标识
     * @return 获取键值映射
     */
    public Map<String, String> getMap(String keyHead);

    public Map<String, String> getMap(String category, String keyHead);

    /**
     * @param key
     * @return 获取键值数组值
     */
    public String[] getArray(String key);

    public String[] getArray(String key, boolean zeroSize);

    public String[] getArray(String category, String key, boolean zeroSize);

    /**
     * @param key
     * @return 获得对应的数字值
     */
    public int getInt(String key);

    public int getInt(String key, int defaultValue);

    public int getInt(String category, String key, int defaultValue);

    /**
     * @param key
     * @return 获得对应的布尔值
     */
    public boolean getBoolean(String key);

    public boolean getBoolean(String key, boolean defaultValue);

    public boolean getBoolean(String category, String key, boolean defaultValue);

    /**
     * @param key
     * @return 获取长整数
     */
    public long getLong(String key);

    public long getLong(String key, long defaultValue);

    /**
     * @param key
     * @return 获取浮点数
     */
    public float getFloat(String key);

    public float getFloat(String key, float defaultValue);

    /**
     * @param key
     * @return 获取双精度浮点数
     */
    public double getDouble(String key);

    public double getDouble(String key, double defaultValue);

    /**
     * @return 获得配置对象内部加载的配置项映射
     */
    public Map<String, String> toMap();

    public Map<String, String> toMap(String category);

    /**
     * @return 获取分类的名称集合
     */
    public List<String> getCategoryNames();

    /**
     * 判断键key的配置项是否存在
     *
     * @param key
     * @return 如果存在配置项那么返回true，否则返回false
     */
    public boolean contains(String key);

    public boolean contains(String category, String key);

    /**
     * 初始化配置项，一般用来从provider中获取配置项，是实现自定义的配置项进入内存的位置
     *
     * @param provider 配置提供者
     */
    public void initialize(IConfigurationProvider provider);

    /**
     * @return 获得配置文件自定义标签名称，即返回值加在配置文件名与扩展名中间，形成XXXX.YYY.xml形式，其中需要返回.YYY
     */
    public String getTagName();

}