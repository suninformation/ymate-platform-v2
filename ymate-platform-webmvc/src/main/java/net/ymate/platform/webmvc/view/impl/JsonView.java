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
package net.ymate.platform.webmvc.view.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.AbstractView;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * JSON视图
 *
 * @author 刘镇 (suninformation@163.com) on 2011-10-23 上午11:27:16
 */
public class JsonView extends AbstractView {

    private final List<SerializerFeature> serializerFeatures = new ArrayList<>();

    private final Object jsonObj;

    private boolean withContentType;

    private String jsonCallback;

    private boolean keepNullValue;

    private boolean quoteFieldNames;

    private boolean useSingleQuotes;

    public static JsonView bind(Object obj) {
        if (obj instanceof String) {
            return new JsonView((String) obj);
        } else {
            return new JsonView(obj);
        }
    }

    /**
     * 构造器
     *
     * @param obj 任意对象
     */
    public JsonView(Object obj) {
        jsonObj = JSON.toJSON(obj);
    }

    /**
     * 构造器
     *
     * @param jsonStr JSON字符串
     */
    public JsonView(String jsonStr) {
        jsonObj = JSON.parseObject(jsonStr, new TypeReference<LinkedHashMap<String, Object>>() {
        }, Feature.OrderedField);
    }

    /**
     * @return 设置ContentType为"application/json"或"text/javascript"，默认为空
     */
    public JsonView withContentType() {
        withContentType = true;
        return doSetContentType();
    }

    private JsonView doSetContentType() {
        if (withContentType) {
            if (jsonCallback == null) {
                setContentType(Type.ContentType.JSON.getContentType());
            } else {
                setContentType(Type.ContentType.JAVASCRIPT.getContentType());
            }
        }
        return this;
    }

    /**
     * @param callback 回调方法名称
     * @return 设置并采用JSONP方式输出，回调方法名称由参数callback指定，若callback参数无效则不启用
     */
    public JsonView withJsonCallback(String callback) {
        jsonCallback = StringUtils.trimToNull(callback);
        return doSetContentType();
    }

    /**
     * @return 设置是否保留空值属性
     */
    public JsonView keepNullValue() {
        keepNullValue = true;
        return this;
    }

    /**
     * @return 设置JSON属性KEY使用引号
     */
    public JsonView quoteFieldNames() {
        quoteFieldNames = true;
        return this;
    }

    /**
     * @return 设置JSON属性KEY使用单引号
     */
    public JsonView useSingleQuotes() {
        useSingleQuotes = true;
        return this;
    }

    /**
     * 自定义序列化配置
     *
     * @param serialFeatures 序列化配置
     * @return 返回当前视图对象
     */
    public JsonView addSerializerFeatures(SerializerFeature... serialFeatures) {
        if (ArrayUtils.isNotEmpty(serialFeatures)) {
            serializerFeatures.addAll(Arrays.asList(serialFeatures));
        }
        return this;
    }

    /**
     * @return 将视图数据对象转换为JSON字符串
     */
    private String doObjectToJsonString() {
        if (quoteFieldNames) {
            serializerFeatures.add(SerializerFeature.QuoteFieldNames);
            if (useSingleQuotes) {
                serializerFeatures.add(SerializerFeature.UseSingleQuotes);
            }
        }
        if (keepNullValue) {
            serializerFeatures.addAll(Arrays.asList(
                    SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteNullBooleanAsFalse,
                    SerializerFeature.WriteNullListAsEmpty,
                    SerializerFeature.WriteNullNumberAsZero,
                    SerializerFeature.WriteNullStringAsEmpty,
                    SerializerFeature.WriteNullNumberAsZero));
        }
        return JSON.toJSONString(jsonObj, serializerFeatures.toArray(new SerializerFeature[0]));
    }

    @Override
    protected void doRenderView() throws Exception {
        StringBuilder jsonStringBuilder = new StringBuilder(doObjectToJsonString());
        if (jsonCallback != null) {
            jsonStringBuilder.insert(0, jsonCallback + "(").append(");");
        }
        HttpServletResponse httpServletResponse = WebContext.getResponse();
        IOUtils.write(jsonStringBuilder.toString(), httpServletResponse.getOutputStream(), httpServletResponse.getCharacterEncoding());
    }

    @Override
    public void render(OutputStream output) throws Exception {
        StringBuilder jsonStringBuilder = new StringBuilder(doObjectToJsonString());
        if (jsonCallback != null) {
            jsonStringBuilder.insert(0, jsonCallback + "(").append(");");
        }
        IOUtils.write(jsonStringBuilder, output, WebContext.getResponse().getCharacterEncoding());
    }
}
