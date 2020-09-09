/*
 * Copyright 2007-2020 the original author or authors.
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

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.core.support.ErrorCode;
import net.ymate.platform.webmvc.IWebResult;
import net.ymate.platform.webmvc.IWebResultBuilder;
import net.ymate.platform.webmvc.util.WebResult;

import java.io.Serializable;
import java.util.Map;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/09/08 22:31
 * @since 2.1.0
 */
public class DefaultWebResultBuilder implements IWebResultBuilder {

    private final WebResult result = new WebResult();

    @Override
    public IWebResultBuilder succeed() {
        result.code(ErrorCode.SUCCEED);
        return this;
    }

    @Override
    public IWebResultBuilder code(Serializable code) {
        result.code(BlurObject.bind(code).toIntValue());
        return this;
    }

    @Override
    public IWebResultBuilder msg(String msg) {
        result.msg(msg);
        return this;
    }

    @Override
    public IWebResultBuilder data(Object data) {
        result.data(data);
        return this;
    }

    @Override
    public IWebResultBuilder attrs(Map<String, Object> attrs) {
        result.attrs(attrs);
        return this;
    }

    @Override
    public IWebResultBuilder dataAttr(String dataKey, Object dataValue) {
        result.dataAttr(dataKey, dataValue);
        return this;
    }

    @Override
    public IWebResultBuilder attr(String attrKey, Object attrValue) {
        result.attr(attrKey, attrValue);
        return this;
    }

    @Override
    public IWebResult<?> build() {
        return result;
    }
}
