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
package net.ymate.platform.configuration.impl;

import net.ymate.platform.configuration.AbstractConfigFileParser;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.Properties;

/**
 * 基于Properties解析工具处理properties配置文件的读写操作
 *
 * @author 刘镇 (suninformation@163.com) on 2010-9-5 下午06:37:36
 */
public class PropertyConfigFileParser extends AbstractConfigFileParser {

    private final Properties properties = new Properties();

    public PropertyConfigFileParser(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            properties.load(reader);
        }
    }

    public PropertyConfigFileParser(InputStream inputStream) throws IOException {
        properties.load(inputStream);
    }

    public PropertyConfigFileParser(URL url) throws IOException {
        try (InputStream in = url.openStream()) {
            properties.load(in);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onLoad() {
        Enumeration<String> propNames = (Enumeration<String>) properties.propertyNames();
        while (propNames.hasMoreElements()) {
            String propName = propNames.nextElement();
            if (StringUtils.startsWith(propName, TAG_NAME_PROPERTIES)) {
                String newPropName = StringUtils.substringAfter(propName, TAG_NAME_PROPERTIES.concat("."));
                // _propArr[0] = categoryName
                // _propArr[1] = propertyName
                // _propArr[2] = attributes关键字
                // _propArr[3] = attrName
                String[] propArr = StringUtils.split(newPropName, ".");
                if (propArr.length > 1) {
                    // 若为根属性
                    if (propArr[0].equalsIgnoreCase(TAG_NAME_ATTRIBUTES)) {
                        getAttributes().put(propArr[1], new Attribute(propArr[1], properties.getProperty(propName)));
                        continue;
                    }
                    // 若为正常的category, 若category对象不存在, 则创建它
                    Category category = getCategories().get(propArr[0]);
                    if (category == null) {
                        category = new Category(propArr[0], null, null, isSorted());
                        getCategories().put(propArr[0], category);
                    }
                    //
                    switch (propArr.length) {
                        case 4:
                            if (propArr[2].equalsIgnoreCase(TAG_NAME_ATTRIBUTES)) {
                                Property prop = safeGetProperty(category, propArr[1]);
                                fixedSetAttribute(prop, propName, propArr[3]);
                            } else {
                                category.getProperties().put(propArr[3], new Property(propArr[3], properties.getProperty(propName), null));
                            }
                            break;
                        case 2:
                            fixedSetProperty(category, propName, propArr[1]);
                            break;
                        default:
                            if (propArr[1].equalsIgnoreCase(TAG_NAME_ATTRIBUTES)) {
                                category.getAttributes().put(propArr[2], new Attribute(propArr[2], properties.getProperty(propName)));
                            } else {
                                Property prop = safeGetProperty(category, propArr[1]);
                                fixedSetAttribute(prop, propName, propArr[2]);
                            }
                            break;
                    }
                }
            }
        }
        // 必须保证DEFAULT_CATEGORY_NAME配置集合存在
        if (!getCategories().containsKey(DEFAULT_CATEGORY_NAME)) {
            getCategories().put(DEFAULT_CATEGORY_NAME, new Category(DEFAULT_CATEGORY_NAME, null, null, isSorted()));
        }
    }

    private Property safeGetProperty(Category category, String newPropName) {
        Property property = category.getProperty(newPropName);
        if (property == null) {
            property = new Property(newPropName, null, null);
            category.getProperties().put(newPropName, property);
        }
        return property;
    }

    private void fixedSetAttribute(Property property, String propName, String newPropName) {
        Attribute attr = property.getAttribute(newPropName);
        String attrValue = properties.getProperty(propName);
        if (attr == null) {
            attr = new Attribute(newPropName, attrValue);
            property.getAttributes().put(newPropName, attr);
        } else {
            attr.setKey(newPropName);
            attr.setValue(attrValue);
        }
    }

    private void fixedSetProperty(Category category, String propName, String newPropName) {
        Property prop = category.getProperty(newPropName);
        String propContent = properties.getProperty(propName);
        if (prop == null) {
            prop = new Property(newPropName, propContent, null);
            category.getProperties().put(newPropName, prop);
        } else {
            prop.setName(newPropName);
            prop.setContent(propContent);
        }
    }

    @Override
    public void writeTo(File targetFile) throws IOException {
        writeTo(Files.newOutputStream(targetFile.toPath()));
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        properties.store(outputStream, StringUtils.EMPTY);
    }
}
