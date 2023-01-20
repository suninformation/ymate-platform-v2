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
package net.ymate.platform.webmvc;

import net.ymate.platform.commons.json.IJsonObjectWrapper;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.core.beans.annotation.Ignored;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/09/08 22:20
 * @since 2.1.0
 */
@Ignored
public interface IWebResultBuilder {

    default IWebResultBuilder fromJson(String jsonStr) {
        return fromJson(JsonWrapper.fromJson(jsonStr));
    }

    default IWebResultBuilder fromJson(JsonWrapper jsonWrapper) {
        if (jsonWrapper != null && jsonWrapper.isJsonObject()) {
            return fromJson(Objects.requireNonNull(jsonWrapper.getAsJsonObject()));
        }
        return this;
    }

    IWebResultBuilder fromJson(IJsonObjectWrapper jsonObject);

    IWebResultBuilder succeed();

    IWebResultBuilder code(Serializable code);

    IWebResultBuilder msg(String msg);

    IWebResultBuilder data(Object data);

    IWebResultBuilder attrs(Map<String, Object> attrs);

    IWebResultBuilder dataAttr(String dataKey, Object dataValue);

    IWebResultBuilder attr(String attrKey, Object attrValue);

    IWebResult<?> build();
}
