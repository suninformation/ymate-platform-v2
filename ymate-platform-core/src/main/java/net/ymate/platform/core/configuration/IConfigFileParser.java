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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import net.ymate.platform.core.beans.annotation.Ignored;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/7/31 上午12:02
 */
@Ignored
public interface IConfigFileParser {

    String DEFAULT_CATEGORY_NAME = "default";

    String TAG_NAME_ROOT = "properties";

    String TAG_NAME_CATEGORY = "category";

    String TAG_NAME_PROPERTY = "property";

    String TAG_NAME_ITEM = "item";

    String TAG_NAME_VALUE = "value";

    String TAG_NAME_CATEGORIES = "categories";

    String TAG_NAME_ATTRIBUTES = "attributes";

    /**
     * 开始分析配置文件
     *
     * @param sorted 是否保证顺序
     * @return 返回当前分析器对象
     */
    IConfigFileParser load(boolean sorted);

    /**
     * 将配置文件内容写入到目标文件
     *
     * @param targetFile 目标文件
     * @throws IOException 可能产生的IO异常
     */
    void writeTo(File targetFile) throws IOException;

    /**
     * 将配置文件内容写入到输出流
     *
     * @param outputStream 目标输出流
     * @throws IOException 可能产生的IO异常
     */
    void writeTo(OutputStream outputStream) throws IOException;

    /**
     * 获取指定键的属性对象
     *
     * @param key 属性键
     * @return 属性对象
     */
    Attribute getAttribute(String key);

    /**
     * 获取全部属性映射
     *
     * @return 返回属性映射
     */
    Map<String, Attribute> getAttributes();

    /**
     * 获取默认分类对象
     *
     * @return 返回分类对象
     */
    Category getDefaultCategory();

    /**
     * 获取指定名称的分类
     *
     * @param name 分类名称
     * @return 返回分类对象
     */
    Category getCategory(String name);

    /**
     * 获取全部分类映射
     *
     * @return 返回分类对象映射
     */
    Map<String, Category> getCategories();

    /**
     * 输出为JSON对象
     *
     * @return 返回JSON对象
     */
    JSONObject toJSON();

    class Category {

        private String name;

        private Map<String, Attribute> attributeMap;

        private Map<String, Property> propertyMap;

        private boolean sorted;

        public Category(String name, List<Attribute> attributes, List<Property> properties, boolean sorted) {
            this.name = name;
            this.sorted = sorted;
            this.attributeMap = new HashMap<>();
            if (this.sorted) {
                this.propertyMap = new LinkedHashMap<>();
            } else {
                this.propertyMap = new HashMap<>();
            }
            if (attributes != null) {
                for (Attribute attr : attributes) {
                    this.attributeMap.put(attr.getKey(), attr);
                }
            }
            if (properties != null) {
                for (Property prop : properties) {
                    this.propertyMap.put(prop.getName(), prop);
                }
            }
        }

        public String getName() {
            return name;
        }

        public Attribute getAttribute(String key) {
            return this.attributeMap.get(key);
        }

        public String getAttribute(String key, String defaultValue) {
            Attribute attr = this.attributeMap.get(key);
            if (attr != null) {
                return attr.getValue(defaultValue);
            }
            return defaultValue;
        }

        public Map<String, Attribute> getAttributeMap() {
            return attributeMap;
        }

        public Property getProperty(String name) {
            return this.propertyMap.get(name);
        }

        public Map<String, Property> getPropertyMap() {
            return propertyMap;
        }

        public JSONObject toJSON() {
            JSONObject jsonO = new JSONObject(sorted);
            jsonO.put("name", name);

            JSONObject jsonAttr = new JSONObject();
            for (Attribute attr : attributeMap.values()) {
                jsonAttr.put(attr.getKey(), attr.getValue());
            }
            jsonO.put("attributes", jsonAttr);

            JSONArray jsonArrayProp = new JSONArray();
            for (Property prop : propertyMap.values()) {
                jsonArrayProp.add(prop.toJSON());
            }
            jsonO.put("properties", jsonArrayProp);
            return jsonO;
        }
    }

    class Property {

        private String name;

        private String content;

        private Map<String, Attribute> attributeMap;

        public Property(String name, String content, List<Attribute> attributes) {
            this.name = name;
            this.content = content;
            this.attributeMap = new HashMap<>();
            if (attributes != null) {
                for (Attribute attr : attributes) {
                    this.attributeMap.put(attr.getKey(), attr);
                }
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public Attribute getAttribute(String key) {
            return this.attributeMap.get(key);
        }

        public String getAttribute(String key, String defaultValue) {
            Attribute attr = this.attributeMap.get(key);
            if (attr != null) {
                return attr.getValue(defaultValue);
            }
            return defaultValue;
        }

        public Map<String, Attribute> getAttributeMap() {
            return attributeMap;
        }

        public JSONObject toJSON() {
            JSONObject jsonO = new JSONObject();
            jsonO.put("name", name);
            jsonO.put("content", content);
            //
            JSONObject jsonAttrs = new JSONObject();
            for (Attribute attr : attributeMap.values()) {
                attr.appendTo(jsonAttrs);
            }
            jsonO.put(TAG_NAME_ATTRIBUTES, jsonAttrs);
            return jsonO;
        }
    }

    class Attribute {

        private String key;

        private String value;

        public Attribute(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getValue(String defaultValue) {
            return StringUtils.defaultIfBlank(value, defaultValue);
        }

        public JSONObject toJSON() {
            JSONObject jsonO = new JSONObject();
            jsonO.put(key, value);
            return jsonO;
        }

        public void appendTo(JSONObject jsonObject) {
            jsonObject.put(key, value);
        }
    }
}
