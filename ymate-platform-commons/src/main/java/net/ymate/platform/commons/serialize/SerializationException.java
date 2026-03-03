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
package net.ymate.platform.commons.serialize;

/**
 * 序列化异常类，用于封装序列化和反序列化过程中产生的异常信息。
 * <p>
 * 该异常类提供了更详细的错误信息，包括序列化器名称、操作类型等，
 * 便于快速定位和解决问题。
 * </p>
 *
 * @author 刘镇 (suninformation@163.com) on 2025/01/06 04:18:39
 * @since 2.1.4
 */
public class SerializationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 序列化器名称，用于标识产生异常的序列化器。
     */
    private final String serializerName;

    /**
     * 操作类型，标识是在序列化还是反序列化过程中产生的异常。
     */
    private final OperationType operationType;

    /**
     * 操作类型枚举，定义了序列化操作的两个阶段。
     */
    public enum OperationType {
        /**
         * 序列化操作。
         */
        SERIALIZE,
        /**
         * 反序列化操作。
         */
        DESERIALIZE
    }

    /**
     * 构造一个只包含错误消息的序列化异常。
     *
     * @param message 错误消息，描述异常的具体信息
     */
    public SerializationException(String message) {
        super(message);
        this.serializerName = null;
        this.operationType = null;
    }

    /**
     * 构造一个包含错误消息和原因的序列化异常。
     *
     * @param message 错误消息，描述异常的具体信息
     * @param cause   导致此异常的原因，可为null
     */
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
        this.serializerName = null;
        this.operationType = null;
    }

    /**
     * 构造一个包含序列化器名称、操作类型和错误消息的序列化异常。
     *
     * @param serializerName 序列化器名称，用于标识产生异常的序列化器，可为null
     * @param operationType  操作类型，标识是序列化还是反序列化异常，可为null
     * @param message        错误消息，描述异常的具体信息
     */
    public SerializationException(String serializerName, OperationType operationType, String message) {
        super(message);
        this.serializerName = serializerName;
        this.operationType = operationType;
    }

    /**
     * 构造一个包含序列化器名称、操作类型、错误消息和原因的序列化异常。
     *
     * @param serializerName 序列化器名称，用于标识产生异常的序列化器，可为null
     * @param operationType  操作类型，标识是序列化还是反序列化异常，可为null
     * @param message        错误消息，描述异常的具体信息
     * @param cause          导致此异常的原因，可为null
     */
    public SerializationException(String serializerName, OperationType operationType, String message, Throwable cause) {
        super(message, cause);
        this.serializerName = serializerName;
        this.operationType = operationType;
    }

    /**
     * 获取序列化器名称。
     *
     * @return 序列化器名称，如果未设置则返回null
     */
    public String getSerializerName() {
        return serializerName;
    }

    /**
     * 获取操作类型。
     *
     * @return 操作类型，如果未设置则返回null
     */
    public OperationType getOperationType() {
        return operationType;
    }

    /**
     * 获取完整的错误消息。
     * <p>
     * 该方法将序列化器名称、操作类型和原始错误消息组合成一个完整的错误描述。
     * </p>
     *
     * @return 完整的错误消息字符串
     */
    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        if (serializerName != null) {
            sb.append("Serializer[").append(serializerName).append("] ");
        }
        if (operationType != null) {
            sb.append(operationType.name()).append(" error: ");
        }
        sb.append(super.getMessage());
        return sb.toString();
    }
}
