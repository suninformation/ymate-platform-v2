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
package net.ymate.platform.configuration.support;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;

/**
 * 基于JDK自带的解析工具处理XML配置文件的读写操作
 *
 * @author 刘镇 (suninformation@163.com) on 14-11-7 下午3:56
 * @version 1.0
 */
public class XMLConfigFileHandler {

    public static String DEFAULT_CATEGORY_NAME = "default";

    public static String TAG_NAME_ROOT = "properties";

    public static String TAG_NAME_CATEGORY = "category";

    public static String TAG_NAME_PROPERTY = "property";

    //

    private Element __rootElement;

    private Map<String, XMLAttribute> __rootAttributes;

    private Map<String, XMLCategory> __categories;

    private boolean __loaded;

    private boolean __sorted;

    public XMLConfigFileHandler(File file) throws ParserConfigurationException, IOException, SAXException {
        Document _document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
        __rootElement = _document.getDocumentElement();
    }

    public XMLConfigFileHandler(InputStream inputStream) throws ParserConfigurationException, IOException, SAXException {
        Document _document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
        __rootElement = _document.getDocumentElement();
    }

    public XMLConfigFileHandler(URL url) throws ParserConfigurationException, IOException, SAXException {
        Document _document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
        __rootElement = _document.getDocumentElement();
    }

    public XMLConfigFileHandler(Node node) {
        __rootElement = (Element) node;
    }

    public XMLConfigFileHandler load(boolean sorted) {
        if (!__loaded) {
            __sorted = sorted;
            // 判断是否保证顺序
            if (sorted) {
                __categories = new LinkedHashMap<String, XMLCategory>();
                __rootAttributes = new LinkedHashMap<String, XMLAttribute>();
            } else {
                __categories = new HashMap<String, XMLCategory>();
                __rootAttributes = new HashMap<String, XMLAttribute>();
            }
            //
            if (!__rootElement.getNodeName().equals(TAG_NAME_ROOT)) {
                throw new RuntimeException("Configuration root element not valid.");
            }
            //
            NamedNodeMap __rootAttrNodes = __rootElement.getAttributes();
            if (__rootAttrNodes != null && __rootAttrNodes.getLength() > 0) {
                // 提取root标签的所有属性
                for (int _attrIdx = 0; _attrIdx < __rootAttrNodes.getLength(); _attrIdx++) {
                    String _attrKey = __rootAttrNodes.item(_attrIdx).getNodeName();
                    String _attrValue = __rootAttrNodes.item(_attrIdx).getNodeValue();
                    if (StringUtils.isNotBlank(_attrKey) && StringUtils.isNotBlank(_attrValue)) {
                        __rootAttributes.put(_attrKey, new XMLAttribute(_attrKey, _attrValue));
                    }
                }
            }
            //
            NodeList _nodes = __rootElement.getElementsByTagName(TAG_NAME_CATEGORY);
            if (_nodes.getLength() > 0) {
                for (int _idx = 0; _idx < _nodes.getLength(); _idx++) {
                    Element _categoryElement = (Element) _nodes.item(_idx);
                    // 先处理category的属性
                    String _categoryNameAttr = null;
                    List<XMLAttribute> _categoryAttrs = new ArrayList<XMLAttribute>();
                    NamedNodeMap _categoryAttrNodes = _categoryElement.getAttributes();
                    if (_categoryAttrNodes != null && _categoryAttrNodes.getLength() > 0) {
                        // 提取category标签的所有属性
                        for (int _attrIdx = 0; _attrIdx < _categoryAttrNodes.getLength(); _attrIdx++) {
                            String _attrKey = _categoryAttrNodes.item(_attrIdx).getNodeName();
                            String _attrValue = _categoryAttrNodes.item(_attrIdx).getNodeValue();
                            if (StringUtils.isNotBlank(_attrKey) && StringUtils.isNotBlank(_attrValue)) {
                                if (_attrKey.equals("name")) {
                                    _categoryNameAttr = _attrValue;
                                } else {
                                    _categoryAttrs.add(new XMLAttribute(_attrKey, _attrValue));
                                }
                            }
                        }
                    }
                    if (_categoryNameAttr != null) {
                        // 再处理category的property标签
                        List<XMLProperty> _properties = new ArrayList<XMLProperty>();
                        //
                        NodeList _propertyNodes = _categoryElement.getElementsByTagName(TAG_NAME_PROPERTY);
                        if (_propertyNodes.getLength() > 0) {
                            for (int _idy = 0; _idy < _propertyNodes.getLength(); _idy++) {
                                Element _node = (Element) _propertyNodes.item(_idy);
                                NamedNodeMap _attrNodes = _node.getAttributes();
                                List<XMLAttribute> _attrs = new ArrayList<XMLAttribute>();
                                String _propertyNameValue = null;
                                String _propertyContent = _node.getTextContent();
                                if (_attrNodes != null && _attrNodes.getLength() > 0) {
                                    // 提取property标签的所有属性
                                    for (int _attrIdx = 0; _attrIdx < _attrNodes.getLength(); _attrIdx++) {
                                        String _attrKey = _attrNodes.item(_attrIdx).getNodeName();
                                        String _attrValue = _attrNodes.item(_attrIdx).getNodeValue();
                                        if (StringUtils.isNotBlank(_attrKey) && StringUtils.isNotBlank(_attrValue)) {
                                            if (_attrKey.equals("name")) {
                                                _propertyNameValue = _attrValue;
                                            } else if (_attrKey.equals("value")) {
                                                _propertyContent = _attrValue;
                                            } else {
                                                _attrs.add(new XMLAttribute(_attrKey, _attrValue));
                                            }
                                        }
                                    }
                                    // 只有name属性与标签体内容均为非空时才保留
                                    if (_propertyNameValue != null && StringUtils.isNotBlank(_propertyContent)) {
                                        _properties.add(new XMLProperty(_propertyNameValue, _propertyContent, _attrs));
                                    }
                                }
                            }
                        }
                        //
                        __categories.put(_categoryNameAttr, new XMLCategory(_categoryNameAttr, _categoryAttrs, _properties, sorted));
                    }
                }
            }
            // 必须保证DEFAULT_CATEGORY_NAME配置集合存在
            if (!__categories.containsKey(DEFAULT_CATEGORY_NAME)) {
                __categories.put(DEFAULT_CATEGORY_NAME, new XMLCategory(DEFAULT_CATEGORY_NAME, null, null, sorted));
            }
            //
            this.__loaded = true;
        }
        return this;
    }

    public boolean writeTo(File targetFile) {
        // TODO write file
        return false;
    }

    public boolean writeTo(OutputStream outputStream) {
        // TODO write file
        return false;
    }

    public XMLAttribute getAttribute(String key) {
        return this.__rootAttributes.get(key);
    }

    public Map<String, XMLAttribute> getAttributes() {
        return Collections.unmodifiableMap(__rootAttributes);
    }

    public XMLCategory getDefaultCategory() {
        return this.__categories.get(DEFAULT_CATEGORY_NAME);
    }

    public XMLCategory getCategory(String name) {
        return this.__categories.get(name);
    }

    public Map<String, XMLCategory> getCategories() {
        return Collections.unmodifiableMap(__categories);
    }

    public JSONObject toJSON() {
        JSONObject _jsonO = new JSONObject(__sorted);
        //
        for (XMLAttribute _attr : __rootAttributes.values()) {
            _jsonO.put(_attr.getKey(), _attr.getValue());
        }
        //
        JSONArray _jsonArrayCATEGORY = new JSONArray();
        for (XMLCategory _category : __categories.values()) {
            _jsonArrayCATEGORY.add(_category.toJSON());
        }
        _jsonO.put("categories", _jsonArrayCATEGORY);
        return _jsonO;
    }

    //////

    public static class XMLCategory {

        private String name;

        private Map<String, XMLAttribute> attributeMap;

        private Map<String, XMLProperty> propertyMap;

        private boolean __sorted;

        public XMLCategory(String name, List<XMLAttribute> attributes, List<XMLProperty> properties, boolean sorted) {
            this.name = name;
            this.attributeMap = new HashMap<String, XMLAttribute>();
            if (sorted) {
                __sorted = sorted;
                this.propertyMap = new LinkedHashMap<String, XMLProperty>();
            } else {
                this.propertyMap = new HashMap<String, XMLProperty>();
            }
            if (attributes != null) {
                for (XMLAttribute _attr : attributes) {
                    this.attributeMap.put(_attr.getKey(), _attr);
                }
            }
            if (properties != null) {
                for (XMLProperty _prop : properties) {
                    this.propertyMap.put(_prop.getName(), _prop);
                }
            }
        }

        public String getName() {
            return name;
        }

        public XMLAttribute getAttribute(String key) {
            return this.attributeMap.get(name);
        }

        public Map<String, XMLAttribute> getAttributeMap() {
            return Collections.unmodifiableMap(attributeMap);
        }

        public XMLProperty getProperty(String name) {
            return this.propertyMap.get(name);
        }

        public Map<String, XMLProperty> getPropertyMap() {
            return Collections.unmodifiableMap(propertyMap);
        }

        public JSONObject toJSON() {
            JSONObject _jsonO = new JSONObject(__sorted);
            _jsonO.put("name", name);
            for (XMLAttribute _attr : attributeMap.values()) {
                _jsonO.put(_attr.getKey(), _attr.getValue());
            }
            JSONArray _jsonArrayPROP = new JSONArray();
            for (XMLProperty _prop : propertyMap.values()) {
                _jsonArrayPROP.add(_prop.toJSON());
            }
            _jsonO.put("properties", _jsonArrayPROP);
            return _jsonO;
        }

    }

    public static class XMLProperty {

        private String name;

        private String content;

        private Map<String, XMLAttribute> attributeMap;

        public XMLProperty(String name, String content, List<XMLAttribute> attributes) {
            this.name = name;
            this.content = content;
            this.attributeMap = new HashMap<String, XMLAttribute>();
            if (attributes != null) {
                for (XMLAttribute _attr : attributes) {
                    this.attributeMap.put(_attr.getKey(), _attr);
                }
            }
        }

        public String getName() {
            return name;
        }

        public String getContent() {
            return content;
        }

        public XMLAttribute getAttribute(String key) {
            return this.attributeMap.get(key);
        }

        public Map<String, XMLAttribute> getAttributeMap() {
            return Collections.unmodifiableMap(attributeMap);
        }

        public JSONObject toJSON() {
            JSONObject _jsonO = new JSONObject();
            _jsonO.put("name", name);
            _jsonO.put("content", content);
            for (XMLAttribute _attr : attributeMap.values()) {
                _jsonO.put(_attr.getKey(), _attr.getValue());
            }
            return _jsonO;
        }

    }

    public static class XMLAttribute {

        private String key;

        private String value;

        public XMLAttribute(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public JSONObject toJSON() {
            JSONObject _jsonO = new JSONObject();
            _jsonO.put(key, value);
            return _jsonO;
        }

    }

}