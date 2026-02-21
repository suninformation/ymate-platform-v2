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
package net.ymate.platform.core.persistence.impl;

import net.ymate.platform.commons.serialize.ISerializer;
import net.ymate.platform.commons.serialize.SerializerManager;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.persistence.IValueRenderer;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * 序列化值渲染器
 * <p>
 * 用于将Base64编码的序列化数据反序列化为对象，支持通过参数指定序列化器类型
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2026/2/21 22:14
 * @since 2.1.4
 */
public class SerializedValueRenderer implements IValueRenderer {

    /**
     * 渲染方法，将Base64编码的序列化字符串反序列化为指定类型的对象
     *
     * @param targetWrapper 目标对象包装器
     * @param field         目标字段
     * @param originValue   原始值（Base64编码的序列化字符串）
     * @param params        渲染参数，支持 "serializer=xxx" 格式指定序列化器名称，若未指定则使用默认序列化器
     * @return 反序列化后的对象，如果失败则返回 null
     * @since 2.1.4
     */
    @Override
    public Object render(ClassUtils.BeanWrapper<?> targetWrapper, Field field, Object originValue, String[] params) {
        if (originValue instanceof String && StringUtils.isNotBlank((String) originValue)) {
            try {
                ISerializer serializer = null;
                if (ArrayUtils.isNotEmpty(params)) {
                    for (String param : params) {
                        if (StringUtils.isNotBlank(param) && param.startsWith("serializer=")) {
                            String serializerName = StringUtils.substringAfter(param, "serializer=");
                            if (StringUtils.isNotBlank(serializerName)) {
                                serializer = SerializerManager.getSerializer(serializerName);
                                if (serializer != null) {
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    serializer = SerializerManager.getDefaultSerializer();
                }
                if (serializer != null) {
                    return serializer.deserialize(Base64.decodeBase64((String) originValue), field.getType());
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
