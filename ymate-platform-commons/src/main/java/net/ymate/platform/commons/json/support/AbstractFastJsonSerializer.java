/*
 * Copyright 2007-present the original author or authors.
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

import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import net.ymate.platform.commons.json.IJsonAdapter;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.json.impl.FastJsonAdapter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * 抽象FastJSON序列化器实现，提供基本的JSON序列化功能。
 * <p>
 * 设计目的：为FastJSON提供统一的序列化处理基础，处理JsonWrapper和相关类型的序列化。
 * <p>
 * 使用场景：
 * - 当需要自定义FastJSON序列化器时
 * - 当需要处理JsonWrapper及相关类型的序列化时
 * - 当需要统一的序列化基础实现时
 *
 * @author 刘镇 (suninformation@163.com) on 2021/12/27 2:42 下午
 * @since 2.1.0
 */
public abstract class AbstractFastJsonSerializer implements ObjectSerializer, ObjectDeserializer {

    protected final IJsonAdapter adapter = new FastJsonAdapter();

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (object == null) {
            serializer.writeNull();
        } else {
            serializer.write(JsonWrapper.unwrap(object));
        }
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
