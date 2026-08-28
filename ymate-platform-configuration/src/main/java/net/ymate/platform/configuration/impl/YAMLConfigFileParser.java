/*
 * Copyright 2007-present the original author or authors.
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
package net.ymate.platform.configuration.impl;

import net.ymate.platform.commons.json.IJsonArrayWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.configuration.AbstractConfigFileParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * 基于SnakeYAML解析工具处理YAML配置文件的读写操作
 * <p>
 * YAML配置文件内容格式与JSON格式结构一致，根节点为categories分类列表，每个分类包含name、attributes和properties，
 * 每个属性包含name、content和attributes；集合类型的属性值content将被序列化为JSON数组字符串，
 * 与JSON配置格式中集合属性值的表示方式保持一致。
 * <p>
 * 使用示例：
 * <pre>
 * categories:
 *   - name: default
 *     properties:
 *       - name: company_name
 *         content: Apple Inc.
 *         attributes: {}
 *       - name: products
 *         content:
 *           - iphone
 *           - ipad
 *         attributes: {}
 *       - name: product_spec
 *         content: spec.
 *         attributes:
 *           color: red
 * </pre>
 *
 * @author 刘镇 (suninformation@163.com) on 2026-08-28 14:20
 * @since 2.1.4
 */
public class YAMLConfigFileParser extends AbstractConfigFileParser {

    private static final String TAG_NAME_NAME = "name";

    private static final String TAG_NAME_CONTENT = "content";

    /**
     * YAML解析后的根节点对象，可为Map或List
     */
    private Object yamlObject;

    public YAMLConfigFileParser(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            doInit(IOUtils.toString(reader));
        }
    }

    public YAMLConfigFileParser(InputStream inputStream) throws IOException {
        doInit(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
    }

    public YAMLConfigFileParser(URL url) throws IOException {
        doInit(IOUtils.toString(url, StandardCharsets.UTF_8));
    }

    public YAMLConfigFileParser(String yamlStr) {
        doInit(yamlStr);
    }

    private void doInit(String yamlStr) {
        if (StringUtils.isNotBlank(yamlStr)) {
            yamlObject = YamlProcessor.load(yamlStr);
        }
    }

    @Override
    protected void onLoad() {
        if (yamlObject instanceof List) {
            // 兼容根节点直接为分类列表的格式
            parseCategories(castToMapList(yamlObject));
        } else if (yamlObject instanceof Map) {
            Map<String, Object> rootMap = castToMap(yamlObject);
            parseRootAttributes(rootMap);
            parseCategories(castToMapList(rootMap.get(TAG_NAME_CATEGORIES)));
        }
        // 必须保证DEFAULT_CATEGORY_NAME配置集合存在
        if (!getCategories().containsKey(DEFAULT_CATEGORY_NAME)) {
            getCategories().put(DEFAULT_CATEGORY_NAME, new Category(DEFAULT_CATEGORY_NAME, null, null, isSorted()));
        }
    }

    /**
     * 解析分类列表
     *
     * @param categoryList 分类列表
     * @since 2.1.4
     */
    private void parseCategories(List<Map<String, Object>> categoryList) {
        if (categoryList != null) {
            categoryList.stream().map(this::parseCategory).filter(Objects::nonNull)
                    .forEach(category -> getCategories().put(category.getName(), category));
        }
    }

    /**
     * 解析单个分类
     *
     * @param categoryMap 分类节点
     * @return 返回分类对象，若分类名称为空则返回null
     * @since 2.1.4
     */
    private Category parseCategory(Map<String, Object> categoryMap) {
        String name = parseScalarValue(categoryMap.get(TAG_NAME_NAME));
        if (StringUtils.isNotBlank(name)) {
            return new Category(name,
                    parseAttributes(castToMap(categoryMap.get(TAG_NAME_ATTRIBUTES))),
                    parseProperties(castToMapList(categoryMap.get(TAG_NAME_PROPERTIES))),
                    isSorted());
        }
        return null;
    }

    /**
     * 解析根节点属性
     *
     * @param rootMap 根节点对象
     * @since 2.1.4
     */
    private void parseRootAttributes(Map<String, Object> rootMap) {
        Map<String, Object> attrMap = castToMap(rootMap.get(TAG_NAME_ATTRIBUTES));
        if (attrMap != null) {
            attrMap.forEach((key, value) -> getAttributes().put(key, new Attribute(key, parseScalarValue(value))));
        }
    }

    /**
     * 解析属性映射为属性对象集合
     *
     * @param attrMap 属性映射
     * @return 返回属性对象集合，若属性映射为空则返回null
     * @since 2.1.4
     */
    private List<Attribute> parseAttributes(Map<String, Object> attrMap) {
        if (attrMap == null) {
            return null;
        }
        List<Attribute> attributes = new ArrayList<>(attrMap.size());
        attrMap.forEach((key, value) -> attributes.add(new Attribute(key, parseScalarValue(value))));
        return attributes;
    }

    /**
     * 解析属性列表
     *
     * @param propList 属性列表
     * @return 返回属性对象集合，若属性列表为空则返回null
     * @since 2.1.4
     */
    private List<Property> parseProperties(List<Map<String, Object>> propList) {
        if (propList == null) {
            return null;
        }
        List<Property> properties = new ArrayList<>(propList.size());
        propList.forEach(propMap -> {
            String name = parseScalarValue(propMap.get(TAG_NAME_NAME));
            if (StringUtils.isNotBlank(name)) {
                properties.add(new Property(name, parseContent(propMap.get(TAG_NAME_CONTENT)), parseAttributes(castToMap(propMap.get(TAG_NAME_ATTRIBUTES)))));
            }
        });
        return properties;
    }

    /**
     * 解析属性值：集合或数组类型的值将被序列化为JSON数组字符串，与JSON配置格式中集合属性值的表示方式保持一致
     *
     * @param value 属性值对象
     * @return 返回解析后的属性值字符串
     * @since 2.1.4
     */
    private String parseContent(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Collection) {
            return JsonWrapper.toJsonString(new ArrayList<>((Collection<?>) value));
        }
        if (value instanceof Object[]) {
            return JsonWrapper.toJsonString(value);
        }
        return parseScalarValue(value);
    }

    /**
     * 解析标量值并转换为字符串
     *
     * @param value 值对象
     * @return 返回转换后的字符串值
     * @since 2.1.4
     */
    private String parseScalarValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public void writeTo(File targetFile) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(targetFile.toPath())) {
            writeTo(outputStream);
        }
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        IOUtils.write(YamlProcessor.dump(buildYamlMap()), outputStream, StandardCharsets.UTF_8);
    }

    /**
     * 将配置内容构建为YAML层级Map结构
     *
     * @return 返回YAML层级Map结构对象
     * @since 2.1.4
     */
    private Map<String, Object> buildYamlMap() {
        Map<String, Object> rootMap = new LinkedHashMap<>();
        if (!getAttributes().isEmpty()) {
            rootMap.put(TAG_NAME_ATTRIBUTES, buildAttributeMap(getAttributes()));
        }
        List<Object> categoryList = new ArrayList<>(getCategories().size());
        getCategories().values().forEach(category -> {
            Map<String, Object> categoryMap = new LinkedHashMap<>();
            categoryMap.put(TAG_NAME_NAME, category.getName());
            if (!category.getAttributes().isEmpty()) {
                categoryMap.put(TAG_NAME_ATTRIBUTES, buildAttributeMap(category.getAttributes()));
            }
            List<Object> propertyList = new ArrayList<>(category.getProperties().size());
            category.getProperties().values().forEach(property -> {
                Map<String, Object> propertyMap = new LinkedHashMap<>();
                propertyMap.put(TAG_NAME_NAME, property.getName());
                propertyMap.put(TAG_NAME_CONTENT, restoreContent(property.getContent()));
                if (!property.getAttributes().isEmpty()) {
                    propertyMap.put(TAG_NAME_ATTRIBUTES, buildAttributeMap(property.getAttributes()));
                }
                propertyList.add(propertyMap);
            });
            categoryMap.put(TAG_NAME_PROPERTIES, propertyList);
            categoryList.add(categoryMap);
        });
        rootMap.put(TAG_NAME_CATEGORIES, categoryList);
        return rootMap;
    }

    /**
     * 构建属性映射的YAML结构
     *
     * @param attributes 属性对象映射
     * @return 返回属性键值对Map对象
     * @since 2.1.4
     */
    private Map<String, Object> buildAttributeMap(Map<String, Attribute> attributes) {
        Map<String, Object> attrMap = new LinkedHashMap<>();
        attributes.values().forEach(attr -> attrMap.put(attr.getKey(), attr.getValue()));
        return attrMap;
    }

    /**
     * 还原属性值：JSON数组格式的字符串将被解析为列表，以适配YAML集合的表示方式，其它值保持原字符串
     *
     * @param content 属性值字符串
     * @return 返回还原后的属性值对象
     * @since 2.1.4
     */
    private Object restoreContent(String content) {
        if (StringUtils.isNotBlank(content) && Strings.CS.startsWith(content, "[") && Strings.CS.endsWith(content, "]")) {
            try {
                JsonWrapper jsonWrapper = JsonWrapper.fromJson(content);
                if (jsonWrapper != null && jsonWrapper.isJsonArray()) {
                    IJsonArrayWrapper jsonArray = jsonWrapper.getAsJsonArray();
                    assert jsonArray != null;
                    List<Object> values = new ArrayList<>(jsonArray.size());
                    for (int idx = 0; idx < jsonArray.size(); idx++) {
                        values.add(jsonArray.getString(idx));
                    }
                    return values;
                }
            } catch (Exception ignored) {
                // 非法JSON数组格式的属性值按原字符串处理
            }
        }
        return content;
    }

    /**
     * 将对象转换为目标泛型Map对象
     *
     * @param value 待转换对象
     * @return 返回转换后的Map对象
     * @since 2.1.4
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> castToMap(Object value) {
        return (Map<String, Object>) value;
    }

    /**
     * 将对象转换为目标泛型Map列表
     *
     * @param value 待转换对象
     * @return 返回转换后的Map列表
     * @since 2.1.4
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> castToMapList(Object value) {
        return (List<Map<String, Object>>) value;
    }

    /**
     * SnakeYAML调用隔离处理器，避免无SnakeYAML依赖环境下触发类加载错误
     *
     * @since 2.1.4
     */
    private static final class YamlProcessor {

        /**
         * 使用SnakeYAML解析YAML字符串并返回根节点对象
         *
         * @param yamlStr YAML格式字符串
         * @return 返回解析后的根节点对象，可为Map或List
         * @since 2.1.4
         */
        static Object load(String yamlStr) {
            return new Yaml().load(yamlStr);
        }

        /**
         * 使用SnakeYAML将层级Map结构序列化为YAML格式文本，采用块级风格输出且不折行
         *
         * @param sourceMap 层级Map结构对象
         * @return 返回YAML格式文本
         * @since 2.1.4
         */
        static String dump(Map<String, Object> sourceMap) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setWidth(Integer.MAX_VALUE);
            return new Yaml(options).dump(sourceMap);
        }
    }
}
