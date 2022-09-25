/*
 * Copyright 2007-2021 the original author or authors.
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
package net.ymate.platform.commons.json.support;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import net.ymate.platform.commons.json.JsonWrapper;

import java.lang.reflect.Type;

/**
 * @author 刘镇 (suninformation@163.com) on 2021/12/25 7:19 PM
 * @since 2.1.0
 */
public class JsonArrayFastJsonSerializer extends AbstractFastJsonSerializer {

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JsonWrapper jsonWrapper = adapter.toJson(parser.parse());
        if (jsonWrapper == null) {
            return null;
        }
        return (T) jsonWrapper.getAsJsonArray();
    }
}
