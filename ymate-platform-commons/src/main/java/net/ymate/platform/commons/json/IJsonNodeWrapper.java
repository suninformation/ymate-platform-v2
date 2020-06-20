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
package net.ymate.platform.commons.json;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/6/20 3:30 下午
 * @since 2.1.0
 */
public interface IJsonNodeWrapper extends Serializable {

    Object get();

    boolean getBoolean();

    BigInteger getBigInteger();

    BigDecimal getBigDecimal();

    double getDouble();

    float getFloat();

    int getInt();

    long getLong();

    String getString();

    boolean isNull();

    boolean isJsonArray();

    boolean isJsonObject();

    IJsonArrayWrapper getJsonArray();

    IJsonObjectWrapper getJsonObject();
}
