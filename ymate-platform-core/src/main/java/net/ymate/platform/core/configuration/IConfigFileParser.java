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

import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.core.beans.annotation.Ignored;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author 刘镇 (suninformation@163.com) on 2017/7/31 上午12:02
 */
@Ignored
public interface IConfigFileParser {

    String DEFAULT_CATEGORY_NAME = "default";

    String TAG_NAME_PROPERTIES = "properties";

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
    IJsonObjectWrapper toJson();

    class Category {

        public static List<Category> fromJson(IJsonArrayWrapper jsonArray, boolean sorted) {
            if (jsonArray != null) {
                return IntStream.range(0, jsonArray.size()).mapToObj(idx -> fromJson(jsonArray.getJsonObject(idx), sorted)).filter(Objects::nonNull).collect(Collectors.toList());
            }
            return new ArrayList<>();
        }

        public static Category fromJson(IJsonObjectWrapper jsonObject, boolean sorted) {
            if (jsonObject != null) {
                String name = jsonObject.getString("name");
                if (StringUtils.isNotBlank(name)) {
                    return new Category(name, Attribute.fromJson(jsonObject.getJsonObject(TAG_NAME_ATTRIBUTES)), Property.fromJson(jsonObject.getJsonArray(TAG_NAME_PROPERTIES)), sorted);
                }
            }
            return null;
        }

        private final String name;

        private final Map<String, Attribute> attributes = new HashMap<>();

        private final Map<String, Property> properties;

        private final boolean sorted;

        public Category(String name, List<Attribute> attributes, List<Property> properties, boolean sorted) {
            this.name = name;
            this.sorted = sorted;
            if (this.sorted) {
                this.properties = new LinkedHashMap<>();
            } else {
                this.properties = new HashMap<>();
            }
            if (attributes != null) {
                attributes.forEach(attr -> this.attributes.put(attr.getKey(), attr));
            }
            if (properties != null) {
                properties.forEach(prop -> this.properties.put(prop.getName(), prop));
            }
        }

        public String getName() {
            return name;
        }

        public Attribute getAttribute(String key) {
            return this.attributes.get(key);
        }

        public String getAttribute(String key, String defaultValue) {
            Attribute attr = this.attributes.get(key);
            if (attr != null) {
                return attr.getValue(defaultValue);
            }
            return defaultValue;
        }

        public Map<String, Attribute> getAttributes() {
            return attributes;
        }

        public Property getProperty(String name) {
            return this.properties.get(name);
        }

        public Map<String, Property> getProperties() {
            return properties;
        }

        public IJsonObjectWrapper toJson() {
            IJsonObjectWrapper jsonO = JsonWrapper.createJsonObject(sorted)
                    .put("name", name);
            IJsonObjectWrapper jsonAttr = JsonWrapper.createJsonObject(sorted);
            attributes.values().forEach(attr -> jsonAttr.put(attr.getKey(), attr.getValue()));
            jsonO.put(TAG_NAME_ATTRIBUTES, jsonAttr);
            //
            IJsonArrayWrapper jsonArrayProp = JsonWrapper.createJsonArray();
            properties.values().stream().map(Property::toJson).forEach(jsonArrayProp::add);
            jsonO.put(TAG_NAME_PROPERTIES, jsonArrayProp);
            return jsonO;
        }
    }

    class Property {

        public static List<Property> fromJson(IJsonArrayWrapper jsonArray) {
            if (jsonArray != null) {
                return IntStream.range(0, jsonArray.size()).mapToObj(idx -> fromJson(jsonArray.getJsonObject(idx))).filter(Objects::nonNull).collect(Collectors.toList());
            }
            return new ArrayList<>();
        }

        public static Property fromJson(IJsonObjectWrapper jsonObject) {
            if (jsonObject != null) {
                String name = jsonObject.getString("name");
                if (StringUtils.isNotBlank(name)) {
                    return new Property(name, jsonObject.getString("content"), Attribute.fromJson(jsonObject.getJsonObject(TAG_NAME_ATTRIBUTES)));
                }
            }
            return null;
        }

        private String name;

        private String content;

        private final Map<String, Attribute> attributes = new HashMap<>();

        public Property(String name, String content, List<Attribute> attributes) {
            this.name = name;
            this.content = content;
            if (attributes != null) {
                attributes.forEach(attr -> this.attributes.put(attr.getKey(), attr));
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
            return this.attributes.get(key);
        }

        public String getAttribute(String key, String defaultValue) {
            Attribute attr = this.attributes.get(key);
            if (attr != null) {
                return attr.getValue(defaultValue);
            }
            return defaultValue;
        }

        public Map<String, Attribute> getAttributes() {
            return attributes;
        }

        public IJsonObjectWrapper toJson() {
            IJsonObjectWrapper jsonO = JsonWrapper.createJsonObject()
                    .put("name", name)
                    .put("content", content);
            IJsonObjectWrapper jsonAttrs = JsonWrapper.createJsonObject();
            attributes.values().forEach(attr -> attr.appendTo(jsonAttrs));
            jsonO.put(TAG_NAME_ATTRIBUTES, jsonAttrs);
            //
            return jsonO;
        }
    }

    class Attribute {

        public static List<Attribute> fromJson(IJsonObjectWrapper jsonObject) {
            if (jsonObject != null) {
                return jsonObject.keySet().stream().map(key -> new Attribute(key, jsonObject.getString(key))).collect(Collectors.toList());
            }
            return new ArrayList<>();
        }

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

        public IJsonObjectWrapper toJson() {
            return JsonWrapper.createJsonObject().put(key, value);
        }

        public void appendTo(IJsonObjectWrapper jsonObject) {
            jsonObject.put(key, value);
        }
    }
}
