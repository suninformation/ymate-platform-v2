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

import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.webmvc.base.Type;
import net.ymate.platform.webmvc.context.WebContext;
import net.ymate.platform.webmvc.view.AbstractView;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.OutputStream;

/**
 * JSON视图
 *
 * @author 刘镇 (suninformation@163.com) on 2011-10-23 上午11:27:16
 */
public class JsonView extends AbstractView {

    private final Object jsonObj;

    private boolean withContentType;

    private String jsonCallback;

    private boolean keepNullValue;

    private boolean snakeCase;

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
        jsonObj = JsonWrapper.toJson(obj);
    }

    /**
     * 构造器
     *
     * @param jsonStr JSON字符串
     */
    public JsonView(String jsonStr) {
        jsonObj = JsonWrapper.fromJson(jsonStr);
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

    public JsonView snakeCase() {
        snakeCase = true;
        return this;
    }

    @Override
    protected void doRenderView() throws Exception {
        render(WebContext.getResponse().getOutputStream());
    }

    @Override
    public void render(OutputStream output) throws Exception {
        StringBuilder jsonStringBuilder = new StringBuilder(JsonWrapper.toJsonString(jsonObj, false, keepNullValue, snakeCase));
        if (jsonCallback != null) {
            jsonStringBuilder.insert(0, jsonCallback + "(").append(");");
        }
        String jsonContent = jsonStringBuilder.toString();
        doWriteLog(JsonView.class, jsonContent);
        IOUtils.write(jsonContent, output, WebContext.getResponse().getCharacterEncoding());
    }
}
