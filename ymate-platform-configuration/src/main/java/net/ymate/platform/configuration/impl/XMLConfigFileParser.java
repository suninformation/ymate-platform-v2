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

import net.ymate.platform.commons.XPathHelper;
import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.configuration.AbstractConfigFileParser;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于JDK自带的解析工具处理XML配置文件的读写操作
 *
 * @author 刘镇 (suninformation@163.com) on 14-11-7 下午3:56
 */
public class XMLConfigFileParser extends AbstractConfigFileParser {

    private final Element rootElement;

    public XMLConfigFileParser(File file) throws ParserConfigurationException, IOException, SAXException {
        rootElement = XPathHelper.newDocumentBuilder().parse(file).getDocumentElement();
    }

    public XMLConfigFileParser(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        rootElement = XPathHelper.newDocumentBuilder().parse(inputStream).getDocumentElement();
    }

    public XMLConfigFileParser(URL url) throws ParserConfigurationException, IOException, SAXException {
        rootElement = XPathHelper.newDocumentBuilder().parse(url.openStream()).getDocumentElement();
    }

    public XMLConfigFileParser(Node node) {
        rootElement = (Element) node;
    }

    @Override
    public void onLoad() {
        NamedNodeMap rootAttrNodes = rootElement.getAttributes();
        if (rootAttrNodes != null && rootAttrNodes.getLength() > 0) {
            // 提取root标签的所有属性
            for (int attrIdx = 0; attrIdx < rootAttrNodes.getLength(); attrIdx++) {
                String attrKey = rootAttrNodes.item(attrIdx).getNodeName();
                String attrValue = rootAttrNodes.item(attrIdx).getNodeValue();
                if (StringUtils.isNotBlank(attrKey) && StringUtils.isNotBlank(attrValue)) {
                    getAttributes().put(attrKey, new Attribute(attrKey, attrValue));
                }
            }
        }
        //
        NodeList nodes = rootElement.getElementsByTagName(TAG_NAME_CATEGORY);
        if (nodes.getLength() > 0) {
            for (int idx = 0; idx < nodes.getLength(); idx++) {
                Element categoryElement = (Element) nodes.item(idx);
                // 1. 处理category的属性
                List<Attribute> categoryAttrs = new ArrayList<>();
                PairObject<String, String> category = parseNodeAttributes(categoryAttrs, categoryElement, false, false);
                //
                if (category != null) {
                    // 2. 处理category的property标签
                    List<Property> properties = new ArrayList<>();
                    //
                    NodeList propertyNodes = categoryElement.getElementsByTagName(TAG_NAME_PROPERTY);
                    if (propertyNodes.getLength() > 0) {
                        for (int idy = 0; idy < propertyNodes.getLength(); idy++) {
                            Element node = (Element) propertyNodes.item(idy);
                            // 3. 处理property的属性
                            List<Attribute> propertyAttrs = new ArrayList<>();
                            PairObject<String, String> property = parseNodeAttributes(propertyAttrs, node, false, false);
                            if (property != null) {
                                // 是否有子标签
                                boolean hasSubTag = false;
                                // 4.1 处理property->value标签
                                NodeList childNodes = node.getElementsByTagName(TAG_NAME_VALUE);
                                if (childNodes.getLength() > 0) {
                                    if (childNodes.getLength() == 1) {
                                        property.setValue(childNodes.item(0).getTextContent());
                                    } else {
                                        hasSubTag = true;
                                        for (int idxItem = 0; idxItem < childNodes.getLength(); idxItem++) {
                                            Element nodeItem = (Element) childNodes.item(idxItem);
                                            String value = nodeItem.getTextContent();
                                            if (StringUtils.isNotBlank(value)) {
                                                propertyAttrs.add(new Attribute(value, ""));
                                            }
                                        }
                                    }
                                } else {
                                    // 4.2 处理property->item标签
                                    childNodes = node.getElementsByTagName(TAG_NAME_ITEM);
                                    if (childNodes.getLength() > 0) {
                                        hasSubTag = true;
                                        for (int idxItem = 0; idxItem < childNodes.getLength(); idxItem++) {
                                            parseNodeAttributes(propertyAttrs, (Element) childNodes.item(idxItem), true, true);
                                        }
                                    }
                                }
                                //
                                if (!hasSubTag) {
                                    if (StringUtils.isNotBlank(property.getValue())) {
                                        properties.add(new Property(property.getKey(), property.getValue(), propertyAttrs));
                                    }
                                } else {
                                    properties.add(new Property(property.getKey(), null, propertyAttrs));
                                }
                            }
                        }
                    }
                    //
                    getCategories().put(category.getKey(), new Category(category.getKey(), categoryAttrs, properties, isSorted()));
                }
            }
        }
        // 必须保证DEFAULT_CATEGORY_NAME配置集合存在
        if (!getCategories().containsKey(DEFAULT_CATEGORY_NAME)) {
            getCategories().put(DEFAULT_CATEGORY_NAME, new Category(DEFAULT_CATEGORY_NAME, null, null, isSorted()));
        }
    }

    private PairObject<String, String> parseNodeAttributes(List<Attribute> attributes, Element node, boolean collections, boolean textContent) {
        String propertyName = null;
        String propertyContent = null;
        //
        NamedNodeMap attrNodes = node.getAttributes();
        if (attrNodes != null && attrNodes.getLength() > 0) {
            for (int idy = 0; idy < attrNodes.getLength(); idy++) {
                String attrKey = attrNodes.item(idy).getNodeName();
                String attrValue = attrNodes.item(idy).getNodeValue();
                if (collections) {
                    if ("name".equals(attrKey)) {
                        attributes.add(new Attribute(attrValue, node.getTextContent()));
                    }
                } else {
                    if (textContent && StringUtils.isNotBlank(attrValue)) {
                        attrValue = node.getTextContent();
                    }
                    switch (attrKey) {
                        case "name":
                            propertyName = attrValue;
                            break;
                        case "value":
                            propertyContent = attrValue;
                            break;
                        default:
                            attributes.add(new Attribute(attrKey, attrValue));
                    }
                }
            }
        }
        if (!collections && StringUtils.isNotBlank(propertyName)) {
            return new PairObject<>(propertyName, propertyContent);
        }
        return null;
    }

    @Override
    public void writeTo(File targetFile) throws IOException {
        // TODO write file
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        // TODO write file
    }
}