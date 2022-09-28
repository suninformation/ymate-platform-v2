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
package net.ymate.platform.commons;

import net.ymate.platform.commons.annotation.XPathNode;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author 刘镇 (suninformation@163.com) on 16/5/22 上午6:52
 */
public class XPathHelper {

    private static final Log LOG = LogFactory.getLog(XPathHelper.class);

    public static DocumentBuilderFactory newDocumentBuilderFactory() throws ParserConfigurationException {
//        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        //
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        return documentBuilderFactory;
    }

    public static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilder builder = newDocumentBuilderFactory().newDocumentBuilder();
        builder.setEntityResolver(IGNORE_DTD_ENTITY_RESOLVER);
        //
        return builder;
    }

    public static XPathFactory newXPathFactory() {
        return XPathFactory.newInstance();
    }

    /**
     * 用于忽略所有DTD检测
     */
    public static final EntityResolver IGNORE_DTD_ENTITY_RESOLVER = (publicId, systemId) -> new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));

    public static XPathHelper create(XPathFactory xPathFactory, Document document) {
        return new XPathHelper(xPathFactory, document);
    }

    private XPath xPath;

    private Document document;

    public XPathHelper(XPathFactory xPathFactory, Document document) {
        xPath = xPathFactory.newXPath();
        this.document = document;
    }

    public XPathHelper(DocumentBuilder documentBuilder, XPath xPath, InputSource inputSource) throws IOException, SAXException {
        doInit(documentBuilder, xPath, inputSource);
    }

    public XPathHelper(InputSource inputSource) throws ParserConfigurationException, SAXException, IOException {
        this(newDocumentBuilderFactory(), newXPathFactory(), inputSource, null, null);
    }

    public XPathHelper(InputSource inputSource, EntityResolver entityResolver) throws ParserConfigurationException, SAXException, IOException {
        this(newDocumentBuilderFactory(), newXPathFactory(), inputSource, entityResolver, null);
    }

    public XPathHelper(InputSource inputSource, ErrorHandler errorHandler) throws ParserConfigurationException, SAXException, IOException {
        this(newDocumentBuilderFactory(), newXPathFactory(), inputSource, null, errorHandler);
    }

    public XPathHelper(DocumentBuilderFactory documentBuilderFactory, XPathFactory xPathFactory, InputSource inputSource, EntityResolver entityResolver, ErrorHandler errorHandler) throws IOException, SAXException, ParserConfigurationException {
        doInit(documentBuilderFactory, xPathFactory, inputSource, entityResolver, errorHandler);
    }

    public XPathHelper(DocumentBuilderFactory documentBuilderFactory, XPathFactory xPathFactory, String content) throws ParserConfigurationException, IOException, SAXException {
        try (StringReader reader = new StringReader(content)) {
            document = documentBuilderFactory.newDocumentBuilder().parse(new InputSource(reader));
            xPath = xPathFactory.newXPath();
        }
    }

    public XPathHelper(DocumentBuilderFactory documentBuilderFactory, XPathFactory xPathFactory, String content, EntityResolver entityResolver) throws ParserConfigurationException, SAXException, IOException {
        this(documentBuilderFactory, xPathFactory, content, entityResolver, null);
    }

    public XPathHelper(DocumentBuilderFactory documentBuilderFactory, XPathFactory xPathFactory, String content, ErrorHandler errorHandler) throws ParserConfigurationException, SAXException, IOException {
        this(documentBuilderFactory, xPathFactory, content, null, errorHandler);
    }

    public XPathHelper(DocumentBuilderFactory documentBuilderFactory, XPathFactory xPathFactory, String content, EntityResolver entityResolver, ErrorHandler errorHandler) throws IOException, SAXException, ParserConfigurationException {
        try (StringReader reader = new StringReader(content)) {
            doInit(documentBuilderFactory, xPathFactory, new InputSource(reader), entityResolver, errorHandler);
        }
    }

    private void doInit(DocumentBuilderFactory documentBuilderFactory, XPathFactory xPathFactory, InputSource inputSource, EntityResolver entityResolver, ErrorHandler errorHandler) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        if (entityResolver != null) {
            documentBuilder.setEntityResolver(entityResolver);
        }
        if (errorHandler != null) {
            documentBuilder.setErrorHandler(errorHandler);
        }
        doInit(documentBuilder, xPathFactory.newXPath(), inputSource);
    }

    private void doInit(DocumentBuilder documentBuilder, XPath xPath, InputSource inputSource) throws IOException, SAXException {
        document = documentBuilder.parse(inputSource);
        this.xPath = xPath;
    }

    public Document getDocument() {
        return document;
    }

    public Map<String, Object> toMap() {
        return toMap(document.getDocumentElement());
    }

    public Map<String, Object> toMap(Node parent) {
        NodeList nodes = parent.getChildNodes();
        // 多级标签不予支持
        return IntStream.range(0, nodes.getLength())
                .mapToObj(nodes::item)
                .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
                .filter(node -> node.hasChildNodes() && node.getChildNodes().getLength() == 1)
                .collect(Collectors.toMap(Node::getNodeName, Node::getTextContent, (a, b) -> b, () -> new HashMap<>(16)));
    }

    private Object doEvaluate(String expression, Object item, QName returnType) throws XPathExpressionException {
        return xPath.evaluate(expression, (null == item ? document : item), returnType);
    }

    public String getStringValue(String expression) throws XPathExpressionException {
        return (String) doEvaluate(expression, null, XPathConstants.STRING);
    }

    public Number getNumberValue(String expression) throws XPathExpressionException {
        return (Number) doEvaluate(expression, null, XPathConstants.NUMBER);
    }

    public Boolean getBooleanValue(String expression) throws XPathExpressionException {
        return (Boolean) doEvaluate(expression, null, XPathConstants.BOOLEAN);
    }

    public Node getNode(String expression) throws XPathExpressionException {
        return (Node) doEvaluate(expression, null, XPathConstants.NODE);
    }

    public NodeList getNodeList(String expression) throws XPathExpressionException {
        return (NodeList) doEvaluate(expression, null, XPathConstants.NODESET);
    }

    //

    public String getStringValue(Object item, String expression) throws XPathExpressionException {
        return (String) doEvaluate(expression, item, XPathConstants.STRING);
    }

    public Number getNumberValue(Object item, String expression) throws XPathExpressionException {
        return (Number) doEvaluate(expression, item, XPathConstants.NUMBER);
    }

    public Boolean getBooleanValue(Object item, String expression) throws XPathExpressionException {
        return (Boolean) doEvaluate(expression, item, XPathConstants.BOOLEAN);
    }

    public Node getNode(Object item, String expression) throws XPathExpressionException {
        return (Node) doEvaluate(expression, item, XPathConstants.NODE);
    }

    public NodeList getNodeList(Object item, String expression) throws XPathExpressionException {
        return (NodeList) doEvaluate(expression, item, XPathConstants.NODESET);
    }

    public <T> T toObject(Class<T> targetObject) {
        try {
            return toObject(targetObject.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    public <T> T toObject(T targetObject) {
        try {
            XPathNode rootNodeAnn = targetObject.getClass().getAnnotation(XPathNode.class);
            if (rootNodeAnn != null && StringUtils.isNotBlank(rootNodeAnn.value())) {
                Node rootNode = getNode(rootNodeAnn.value());
                if (rootNode != null) {
                    return toObject(rootNode, targetObject);
                }
            } else {
                return toObject(document, targetObject);
            }
        } catch (IllegalAccessException | XPathExpressionException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
            }
        }
        return null;
    }

    private <T> T toObject(Object parentNode, T targetObject) throws XPathExpressionException, IllegalAccessException {
        if (parentNode != null && targetObject != null) {
            return doWrapperValues(parentNode, ClassUtils.wrapper(targetObject));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> T doWrapperValues(Object parentNode, ClassUtils.BeanWrapper<T> beanWrapper) throws XPathExpressionException, IllegalAccessException {
        for (Field field : beanWrapper.getFields()) {
            if (field.isAnnotationPresent(XPathNode.class)) {
                XPathNode fieldNodeAnn = field.getAnnotation(XPathNode.class);
                if (fieldNodeAnn.child()) {
                    if (field.getType().equals(Collection.class) || field.getType().equals(List.class) || field.getType().equals(Set.class) || field.getType().isArray()) {
                        // 支持集合和数据类型
                        String expression = fieldNodeAnn.value();
                        if (StringUtils.isBlank(expression)) {
                            if (!Void.class.equals(fieldNodeAnn.implClass())) {
                                XPathNode nodeAnn = fieldNodeAnn.implClass().getAnnotation(XPathNode.class);
                                if (nodeAnn != null) {
                                    expression = nodeAnn.value();
                                }
                            }
                        }
                        NodeList childNodes = getNodeList(parentNode, expression);
                        if (childNodes != null && childNodes.getLength() > 0) {
                            Collection<Object> collection;
                            if (field.getType().equals(Set.class)) {
                                collection = new HashSet<>(childNodes.getLength());
                            } else {
                                collection = new ArrayList<>(childNodes.getLength());
                            }
                            boolean isArray = field.getType().isArray();
                            Class<?> fieldClassType = field.getType();
                            if (isArray) {
                                fieldClassType = ClassUtils.getArrayClassType(fieldClassType);
                            } else if (!Void.class.equals(fieldNodeAnn.implClass())) {
                                fieldClassType = fieldNodeAnn.implClass();
                            }
                            for (int idx = 0; idx < childNodes.getLength(); idx++) {
                                Object item = toObject(childNodes.item(idx), fieldClassType);
                                if (item != null) {
                                    collection.add(item);
                                }
                            }
                            if (isArray) {
                                beanWrapper.setValue(field, collection.toArray((Object[]) Array.newInstance(fieldClassType, 0)));
                            } else {
                                Object targetValue = beanWrapper.getValue(field);
                                if (targetValue instanceof Collection) {
                                    ((Collection<Object>) targetValue).addAll(collection);
                                } else {
                                    beanWrapper.setValue(field, collection);
                                }
                            }
                        }
                    } else {
                        Object childNode = StringUtils.isNotBlank(fieldNodeAnn.value()) ? getNode(parentNode, fieldNodeAnn.value()) : parentNode;
                        if (childNode != null) {
                            Object childObject = null;
                            Object fieldValue = beanWrapper.getValue(field);
                            //
                            if (!INodeValueParser.class.equals(fieldNodeAnn.parser())) {
                                try {
                                    INodeValueParser parser = fieldNodeAnn.parser().newInstance();
                                    childObject = parser.parse(this, parentNode, field.getType(), fieldValue);
                                } catch (InstantiationException e) {
                                    if (LOG.isWarnEnabled()) {
                                        LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                                    }
                                }
                            } else {
                                if (fieldValue != null) {
                                    childObject = toObject(childNode, fieldValue);
                                } else {
                                    try {
                                        childObject = toObject(childNode, Void.class.equals(fieldNodeAnn.implClass()) ? field.getType().newInstance() : fieldNodeAnn.implClass().newInstance());
                                    } catch (InstantiationException e) {
                                        if (LOG.isWarnEnabled()) {
                                            LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                                        }
                                    }
                                }
                            }
                            beanWrapper.setValue(field, childObject);
                        }
                    }
                } else {
                    String value = StringUtils.defaultIfBlank(StringUtils.isNotBlank(fieldNodeAnn.value()) ? getStringValue(parentNode, fieldNodeAnn.value()) : null, StringUtils.trimToNull(fieldNodeAnn.defaultValue()));
                    if (!INodeValueParser.class.equals(fieldNodeAnn.parser())) {
                        try {
                            INodeValueParser parser = fieldNodeAnn.parser().newInstance();
                            beanWrapper.setValue(field, BlurObject.bind(parser.parse(this, parentNode, field.getType(), value)).toObjectValue(field.getType()));
                        } catch (InstantiationException e) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                            }
                        }
                    } else {
                        beanWrapper.setValue(field, BlurObject.bind(value).toObjectValue(field.getType()));
                    }
                }
            }
        }
        return beanWrapper.getTargetObject();
    }

    public static class Builder {

        private DocumentBuilderFactory documentBuilderFactory;

        private XPathFactory xPathFactory;

        private EntityResolver entityResolver;

        private ErrorHandler errorHandler;

        public static Builder create() {
            return new Builder();
        }

        public Builder documentBuilderFactory(DocumentBuilderFactory documentBuilderFactory) {
            this.documentBuilderFactory = documentBuilderFactory;
            return this;
        }

        public Builder xPathFactory(XPathFactory xPathFactory) {
            this.xPathFactory = xPathFactory;
            return this;
        }

        public Builder entityResolver(EntityResolver entityResolver) {
            this.entityResolver = entityResolver;
            return this;
        }

        public Builder ignoreDtdEntityResolver() {
            entityResolver = IGNORE_DTD_ENTITY_RESOLVER;
            return this;
        }

        public Builder errorHandler(ErrorHandler errorHandler) {
            this.errorHandler = errorHandler;
            return this;
        }

        public XPathHelper build(InputSource inputSource) throws ParserConfigurationException, SAXException, IOException {
            return new XPathHelper(documentBuilderFactory != null ? documentBuilderFactory : newDocumentBuilderFactory(), xPathFactory != null ? xPathFactory : newXPathFactory(), inputSource, entityResolver, errorHandler);
        }

        public XPathHelper build(String content) throws ParserConfigurationException, SAXException, IOException {
            return new XPathHelper(documentBuilderFactory != null ? documentBuilderFactory : newDocumentBuilderFactory(), xPathFactory != null ? xPathFactory : newXPathFactory(), content, entityResolver, errorHandler);
        }

        public XPathHelper build(Document document) {
            return new XPathHelper(xPathFactory != null ? xPathFactory : newXPathFactory(), document);
        }
    }

    /**
     * 自定义节点值解析器
     */
    public interface INodeValueParser {

        /**
         * 解析节点对象
         *
         * @param helper     当前XPathHelper实例
         * @param node       当前节点对象
         * @param fieldType  当前节点值类型
         * @param fieldValue 当前节点值对象
         * @return 解析处理后的值对象
         */
        Object parse(XPathHelper helper, Object node, Class<?> fieldType, Object fieldValue);
    }
}
