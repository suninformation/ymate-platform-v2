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
package net.ymate.platform.commons.lang.converter;

import net.ymate.platform.commons.annotation.Converter;
import net.ymate.platform.commons.lang.IConverter;

import java.sql.Timestamp;

/**
 * @author 刘镇 (suninformation@163.com) on 2019-11-18 08:48
 * @since 2.1.0
 */
@Converter(from = {Long.class, Integer.class, String.class}, to = Timestamp.class)
public class TimestampConverter implements IConverter<Timestamp> {

    @Override
    public Timestamp convert(Object target) {
        if (target instanceof Number) {
            return new Timestamp(((Number) target).longValue());
        } else if (target instanceof String) {
            return Timestamp.valueOf((String) target);
        }
        return null;
    }
}
