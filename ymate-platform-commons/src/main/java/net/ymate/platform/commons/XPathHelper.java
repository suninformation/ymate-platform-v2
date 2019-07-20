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

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author 刘镇 (suninformation@163.com) on 16/5/22 上午6:52
 */
public class XPathHelper {

    private static final Log LOG = LogFactory.getLog(XPathHelper.class);

    public static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY;

    public static final XPathFactory XPATH_FACTORY;

    static {
        DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_BUILDER_FACTORY.setExpandEntityReferences(false);
        //
        XPATH_FACTORY = XPathFactory.newInstance();
    }

    /**
     * 用于忽略所有DTD检测
     */
    public static final EntityResolver IGNORE_DTD_ENTITY_RESOLVER = (publicId, systemId) -> new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));

    public static XPathHelper create(Document document) {
        return new XPathHelper(document);
    }

    private XPath xPath;

    private Document document;

    public XPathHelper(Document document) {
        xPath = XPATH_FACTORY.newXPath();
        this.document = document;
    }

    public XPathHelper(InputSource inputSource) throws Exception {
        this(inputSource, null, null);
    }

    public XPathHelper(InputSource inputSource, EntityResolver entityResolver) throws Exception {
        this(inputSource, entityResolver, null);
    }

    public XPathHelper(InputSource inputSource, ErrorHandler errorHandler) throws Exception {
        this(inputSource, null, errorHandler);
    }

    public XPathHelper(InputSource inputSource, EntityResolver entityResolver, ErrorHandler errorHandler) throws Exception {
        doInit(inputSource, entityResolver, errorHandler);
    }

    public XPathHelper(String content) throws Exception {
        try (StringReader reader = new StringReader(content)) {
            document = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().parse(new InputSource(reader));
            xPath = XPATH_FACTORY.newXPath();
        }
    }

    public XPathHelper(String content, EntityResolver entityResolver) throws Exception {
        this(content, entityResolver, null);
    }

    public XPathHelper(String content, ErrorHandler errorHandler) throws Exception {
        this(content, null, errorHandler);
    }

    public XPathHelper(String content, EntityResolver entityResolver, ErrorHandler errorHandler) throws Exception {
        try (StringReader reader = new StringReader(content)) {
            doInit(new InputSource(reader), entityResolver, errorHandler);
            xPath = XPATH_FACTORY.newXPath();
        }
    }

    private void doInit(InputSource inputSource, EntityResolver entityResolver, ErrorHandler errorHandler) throws Exception {
        DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
        if (entityResolver != null) {
            builder.setEntityResolver(entityResolver);
        }
        if (errorHandler != null) {
            builder.setErrorHandler(errorHandler);
        }
        xPath = XPATH_FACTORY.newXPath();
        document = builder.parse(inputSource);
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

    public <T> T toObject(Class<T> targetClass) {
        try {
            return toObject(targetClass.newInstance());
        } catch (IllegalAccessException | InstantiationException e) {
            LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
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
            LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
        }
        return null;
    }

    private <T> T toObject(Object parentNode, Class<T> targetClass) throws XPathExpressionException, IllegalAccessException {
        if (parentNode != null && targetClass != null) {
            return doWrapperValues(parentNode, Objects.requireNonNull(ClassUtils.wrapper(targetClass)));
        }
        return null;
    }

    private <T> T toObject(Object parentNode, T targetObject) throws XPathExpressionException, IllegalAccessException {
        if (parentNode != null && targetObject != null) {
            return doWrapperValues(parentNode, ClassUtils.wrapper(targetObject));
        }
        return null;
    }

    private <T> T doWrapperValues(Object parentNode, ClassUtils.BeanWrapper<T> beanWrapper) throws XPathExpressionException, IllegalAccessException {
        for (Field field : beanWrapper.getFields()) {
            if (field.isAnnotationPresent(XPathNode.class)) {
                XPathNode fieldNodeAnn = field.getAnnotation(XPathNode.class);
                if (fieldNodeAnn.child()) {
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
                                LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
                            }
                        } else {
                            if (fieldValue != null) {
                                childObject = toObject(childNode, fieldValue);
                            } else {
                                childObject = toObject(childNode, Void.class.equals(fieldNodeAnn.implClass()) ? field.getType() : fieldNodeAnn.implClass());
                            }
                        }
                        beanWrapper.setValue(field, childObject);
                    }
                } else {
                    String value = StringUtils.defaultIfBlank(StringUtils.isNotBlank(fieldNodeAnn.value()) ? getStringValue(parentNode, fieldNodeAnn.value()) : null, StringUtils.trimToNull(fieldNodeAnn.defaultValue()));
                    if (!INodeValueParser.class.equals(fieldNodeAnn.parser())) {
                        try {
                            INodeValueParser parser = fieldNodeAnn.parser().newInstance();
                            beanWrapper.setValue(field, BlurObject.bind(parser.parse(this, parentNode, field.getType(), value)).toObjectValue(field.getType()));
                        } catch (InstantiationException e) {
                            LOG.warn(StringUtils.EMPTY, RuntimeUtils.unwrapThrow(e));
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

        private EntityResolver entityResolver;

        private ErrorHandler errorHandler;

        public static Builder create() {
            return new Builder();
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

        public XPathHelper build(InputSource inputSource) throws Exception {
            return new XPathHelper(inputSource, entityResolver, errorHandler);
        }

        public XPathHelper build(String content) throws Exception {
            return new XPathHelper(content, entityResolver, errorHandler);
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
