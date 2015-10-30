/*
 * Copyright 2007-2016 the original author or authors.
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

import net.ymate.platform.core.lang.BlurObject;
import net.ymate.platform.core.lang.PairObject;
import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.core.util.RuntimeUtils;
import net.ymate.platform.webmvc.IRequestProcessor;
import net.ymate.platform.webmvc.IUploadFileWrapper;
import net.ymate.platform.webmvc.IWebMvc;
import net.ymate.platform.webmvc.RequestMeta;
import net.ymate.platform.webmvc.annotation.*;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.util.CookieHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 基于XML作为协议格式控制器请求处理器接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 15/5/28 上午11:52
 * @version 1.0
 */
public class XMLRequestProcessor implements IRequestProcessor {

    private static final Log _LOG = LogFactory.getLog(XMLRequestProcessor.class);

    public Map<String, ParameterMeta> processRequestParams(IWebMvc owner, RequestMeta requestMeta, String[] methodParamNames) throws Exception {
        XMLProtocol _protocol = null;
        try {
            _protocol = new XMLProtocol(WebContext.getRequest().getInputStream(), owner.getModuleCfg().getDefaultCharsetEncoding());
        } catch (Exception e) {
            _protocol = new XMLProtocol();
            //
            if (WebContext.getContext().getOwner().getOwner().getConfig().isDevelopMode()) {
                _LOG.warn("", RuntimeUtils.unwrapThrow(e));
            }
        }
        //
        Map<String, ParameterMeta> _returnValues = new LinkedHashMap<String, ParameterMeta>();
        //
        ClassUtils.BeanWrapper<?> _wrapper = ClassUtils.wrapper(requestMeta.getTargetClass());
        if (_wrapper != null) {
            for (String _fieldName : _wrapper.getFieldNames()) {
                PairObject<String, Object> _result = __doGetParamValue(owner, "", _fieldName, _wrapper.getFieldType(_fieldName), _wrapper.getFieldAnnotations(_fieldName), _protocol);
                if (_result != null) {
                    ParameterMeta _pMeta = new ParameterMeta(_result.getKey(), _fieldName, _result.getValue());
                    _returnValues.put(_pMeta.getParamName(), _pMeta);
                    _returnValues.put(_pMeta.getFieldName(), _pMeta);
                }
            }
        }
        //
        Class<?>[] _paramTypes = requestMeta.getMethod().getParameterTypes();
        if (methodParamNames.length > 0) {
            Annotation[][] _paramAnnotations = requestMeta.getMethod().getParameterAnnotations();
            for (int _idx = 0; _idx < methodParamNames.length; _idx++) {
                PairObject<String, Object> _result = __doGetParamValue(owner, "", methodParamNames[_idx], _paramTypes[_idx], _paramAnnotations[_idx], _protocol);
                if (_result != null) {
                    ParameterMeta _pMeta = new ParameterMeta(_result.getKey(), methodParamNames[_idx], _result.getValue());
                    _returnValues.put(_pMeta.getParamName(), _pMeta);
                    _returnValues.put(_pMeta.getFieldName(), _pMeta);
                }
            }
        }
        return _returnValues;
    }

    private PairObject<String, Object> __doGetParamValue(IWebMvc owner, String prefix, String paramName, Class<?> paramType, Annotation[] annotations, XMLProtocol protocol) throws Exception {
        String _pName = null;
        Object _pValue = null;
        for (Annotation _annotation : annotations) {
            if (_annotation instanceof CookieVariable) {
                CookieVariable _anno = (CookieVariable) _annotation;
                _pName = __doGetParamName(_anno.prefix(), _anno.value(), paramName);
                _pValue = BlurObject.bind(StringUtils.defaultIfBlank(CookieHelper.bind(owner).getCookie(_pName).toStringValue(), StringUtils.trimToNull(_anno.defaultValue()))).toObjectValue(paramType);
                break;
            } else if (_annotation instanceof PathVariable) {
                PathVariable _anno = (PathVariable) _annotation;
                _pName = __doGetParamName("", _anno.value(), paramName);
                _pValue = BlurObject.bind(WebContext.getRequestContext().getAttribute(_pName)).toObjectValue(paramType);
                break;
            } else if (_annotation instanceof RequestHeader) {
                RequestHeader _anno = (RequestHeader) _annotation;
                _pName = __doGetParamName(_anno.prefix(), _anno.value(), paramName);
                _pValue = BlurObject.bind(StringUtils.defaultIfBlank(WebContext.getRequest().getHeader(_pName), StringUtils.trimToNull(_anno.defaultValue()))).toObjectValue(paramType);
                break;
            } else if (_annotation instanceof RequestParam) {
                RequestParam _anno = (RequestParam) _annotation;
                _pName = __doGetParamName("", _anno.value(), paramName);
                _pValue = this.__doParseRequestParam(_pName, StringUtils.defaultIfBlank(_anno.prefix(), prefix), StringUtils.trimToNull(_anno.defaultValue()), paramType, protocol);
                break;
            } else if (_annotation instanceof ModelBind) {
                ModelBind _mBind = (ModelBind) _annotation;
                _pName = paramName;
                _pValue = __doParseModelBind(owner, StringUtils.defaultIfBlank(_mBind.prefix(), prefix), paramType, protocol);
                break;
            }
        }
        if (_pName != null && _pValue != null) {
            return new PairObject<String, Object>(_pName, _pValue);
        }
        return null;
    }

    private String __doGetParamName(String prefix, String pName, String defaultName) {
        String _name = StringUtils.defaultIfBlank(pName, defaultName);
        if (StringUtils.isNotBlank(prefix)) {
            _name = prefix.trim().concat("_").concat(_name);
        }
        return _name;
    }

    private Object __doParseRequestParam(String paramName, String prefix, String defaultValue, Class<?> paramType, XMLProtocol protocol) {
        if (paramType.isArray()) {
            if (!paramType.equals(IUploadFileWrapper[].class)) {
                Object[] _values = null;
                if (StringUtils.isNotBlank(prefix)) {
                    String _valueStr = protocol.getSubProperty(prefix, paramName, defaultValue);
                    _values = StringUtils.split(_valueStr, "|");
                } else {
                    String _valueStr = protocol.getProperty(paramName, defaultValue);
                    _values = StringUtils.split(_valueStr, "|");
                }
                if (_values != null && _values.length > 0) {
                    Class<?> _arrayClassType = ClassUtils.getArrayClassType(paramType);
                    Object[] _tempParams = (Object[]) Array.newInstance(_arrayClassType, _values.length);
                    for (int _tempIdx = 0; _tempIdx < _values.length; _tempIdx++) {
                        _tempParams[_tempIdx] = new BlurObject(_values[_tempIdx]).toObjectValue(_arrayClassType);
                    }
                    return _tempParams;
                }
            }
            return null;
        } else if (paramType.equals(IUploadFileWrapper.class)) {
            return null;
        }
        String _value = null;
        if (StringUtils.isNotBlank(prefix)) {
            _value = protocol.getSubProperty(prefix, paramName, defaultValue);
        } else {
            _value = protocol.getProperty(paramName, defaultValue);
        }
        return new BlurObject(_value).toObjectValue(paramType);
    }

    private Object __doParseModelBind(IWebMvc owner, String prefix, Class<?> paramType, XMLProtocol protocol) throws Exception {
        ClassUtils.BeanWrapper<?> _wrapper = ClassUtils.wrapper(paramType);
        if (_wrapper != null) {
            for (String _fName : _wrapper.getFieldNames()) {
                Annotation[] _fieldAnnotations = _wrapper.getFieldAnnotations(_fName);
                PairObject<String, Object> _result = __doGetParamValue(owner, prefix, _fName, _wrapper.getFieldType(_fName), _fieldAnnotations, protocol);
                if (_result != null) {
                    _wrapper.setValue(_fName, _result.getValue());
                }
            }
            return _wrapper.getTargetObject();
        }
        return null;
    }

    /**
     * 简单XML协议处理类
     *
     * @author 刘镇 (suninformation@163.com) on 15/7/25 下午6:16
     * @version 1.0
     */
    private class XMLProtocol {
        private Element __rootElement;

        private boolean __inited;

        public XMLProtocol() {
        }

        public XMLProtocol(InputStream inputStream, String charsetName) throws ParserConfigurationException, IOException, SAXException {
            InputSource _source = new InputSource(new InputStreamReader(inputStream, charsetName));
            __rootElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(_source).getDocumentElement();
            __inited = true;
        }

        public String getProperty(String tagName) {
            return getProperty(tagName, null);
        }

        public String getProperty(String tagName, String defaultValue) {
            String _returnValue = null;
            if (__inited) {
                NodeList _nodes = __rootElement.getElementsByTagName(tagName);
                if (_nodes.getLength() > 0) {
                    Element _node = (Element) _nodes.item(0);
                    _returnValue = _node.getTextContent();
                }
            }
            return StringUtils.defaultIfBlank(_returnValue, defaultValue);
        }

        public String getSubProperty(String subTagName, String tagName) {
            return getSubProperty(subTagName, tagName, null);
        }

        public String getSubProperty(String subTagName, String tagName, String defaultValue) {
            String _returnValue = null;
            if (__inited) {
                NodeList _nodes = __rootElement.getElementsByTagName(subTagName);
                if (_nodes.getLength() > 0) {
                    Element _node = (Element) _nodes.item(0);
                    //
                    NodeList _subNodes = _node.getElementsByTagName(tagName);
                    if (_subNodes.getLength() > 0) {
                        Element _subNode = (Element) _subNodes.item(0);
                        _returnValue = _subNode.getTextContent();
                    }
                }
            }
            return StringUtils.defaultIfBlank(_returnValue, defaultValue);
        }
    }
}
