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
package net.ymate.platform.webmvc.impl;

import net.ymate.platform.commons.XPathHelper;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.commons.util.RuntimeUtils;
import net.ymate.platform.webmvc.IRequestContext;
import net.ymate.platform.webmvc.IUploadFileWrapper;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.ParameterMeta;
import net.ymate.platform.webmvc.context.WebContext;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 基于XML作为协议格式控制器请求处理器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 上午11:52
 */
public class XMLRequestProcessor extends DefaultRequestProcessor {

    private static final Log LOG = LogFactory.getLog(XMLRequestProcessor.class);

    private XMLProtocol doGetProtocol(IWebMvc owner) {
        IRequestContext requestContext = WebContext.getRequestContext();
        XMLProtocol protocol = requestContext.getAttribute(XMLRequestProcessor.class.getName());
        if (protocol == null) {
            try (InputStream inputStream = WebContext.getRequest().getInputStream()) {
                String charsetEncoding = owner.getConfig().getDefaultCharsetEncoding();
                if (owner.getOwner().isDevEnv() && LOG.isDebugEnabled() && owner.getOwner().getParamConfigReader().getBoolean(REQUEST_PROTOCOL_LOG_ENABLED_KEY)) {
                    String content = IOUtils.toString(inputStream, charsetEncoding);
                    try (InputStream contentStream = IOUtils.toInputStream(content, charsetEncoding)) {
                        protocol = new XMLProtocol(contentStream, charsetEncoding);
                        LOG.debug(String.format("Protocol content: %s", content));
                    }
                } else {
                    protocol = new XMLProtocol(inputStream, charsetEncoding);
                }
            } catch (Exception e) {
                protocol = new XMLProtocol();
                //
                if (owner.getOwner().isDevEnv() && LOG.isWarnEnabled()) {
                    LOG.warn("Invalid protocol.", RuntimeUtils.unwrapThrow(e));
                }
            }
            requestContext.addAttribute(XMLRequestProcessor.class.getName(), protocol);
        }
        return protocol;
    }

    @Override
    protected Object doParseRequestParam(IWebMvc owner, ParameterMeta paramMeta, String paramName, String defaultValue, boolean fullScope) {
        Object returnValue = null;
        XMLProtocol protocol = doGetProtocol(owner);
        String[] paramNameArr = StringUtils.split(paramName, ".");
        if (paramMeta.isArray()) {
            if (!paramMeta.getParamType().equals(IUploadFileWrapper[].class)) {
                String[] values;
                String valueStr;
                if (paramNameArr.length > 1) {
                    valueStr = protocol.getSubProperty(paramNameArr[0], paramNameArr[1], defaultValue);
                } else {
                    valueStr = protocol.getProperty(paramName, defaultValue);
                }
                values = StringUtils.split(valueStr, StringUtils.defaultIfBlank(paramMeta.getSplitArraySeparator(), ","));
                if (values != null && values.length > 0) {
                    returnValue = doSafeGetParamValueArray(owner, paramName, ClassUtils.getArrayClassType(paramMeta.getParamType()), values);
                }
            }
        } else if (!paramMeta.getParamType().equals(IUploadFileWrapper.class)) {
            if (paramNameArr.length > 1) {
                returnValue = doSafeGetParamValue(owner, paramName, paramMeta.getParamType(), protocol.getSubProperty(paramNameArr[0], paramNameArr[1], defaultValue), defaultValue, fullScope);
            } else {
                returnValue = doSafeGetParamValue(owner, paramName, paramMeta.getParamType(), protocol.getProperty(paramName, defaultValue), defaultValue, fullScope);
            }
        }
        return returnValue;
    }

    /**
     * 简单XML协议处理类
     *
     * @author 刘镇 (suninformation@163.com) on 15/7/25 下午6:16
     */
    private static class XMLProtocol {

        private Element rootElement;

        private boolean initialized;

        XMLProtocol() {
        }

        XMLProtocol(InputStream inputStream, String charsetName) throws ParserConfigurationException, IOException, SAXException {
            rootElement = XPathHelper.newDocumentBuilder()
                    .parse(new InputSource(new InputStreamReader(inputStream, charsetName)))
                    .getDocumentElement();
            initialized = true;
        }

        public String getProperty(String tagName) {
            return getProperty(tagName, null);
        }

        public String getProperty(String tagName, String defaultValue) {
            String returnValue = null;
            if (initialized) {
                NodeList nodeList = rootElement.getElementsByTagName(tagName);
                if (nodeList.getLength() > 0) {
                    Element node = (Element) nodeList.item(0);
                    returnValue = node.getTextContent();
                }
            }
            return StringUtils.defaultIfBlank(returnValue, defaultValue);
        }

        public String getSubProperty(String subTagName, String tagName) {
            return getSubProperty(subTagName, tagName, null);
        }

        public String getSubProperty(String subTagName, String tagName, String defaultValue) {
            String returnValue = null;
            if (initialized) {
                NodeList nodeList = rootElement.getElementsByTagName(subTagName);
                if (nodeList.getLength() > 0) {
                    Element node = (Element) nodeList.item(0);
                    //
                    NodeList subNodeList = node.getElementsByTagName(tagName);
                    if (subNodeList.getLength() > 0) {
                        Element subNode = (Element) subNodeList.item(0);
                        returnValue = subNode.getTextContent();
                    }
                }
            }
            return StringUtils.defaultIfBlank(returnValue, defaultValue);
        }
    }
}
