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
package net.ymate.platform.commons.lang.converter;

import net.ymate.platform.commons.annotation.Converter;
import net.ymate.platform.commons.json.JsonWrapper;
import net.ymate.platform.commons.lang.IConverter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 刘镇 (suninformation@163.com) on 2020/08/23 16:50
 * @since 2.1.0
 */
@Converter(from = String.class, to = JsonWrapper.class)
public class JsonWrapperConverter implements IConverter<JsonWrapper> {

    @Override
    public JsonWrapper convert(Object target) {
        if (target instanceof String && StringUtils.isNotBlank((CharSequence) target)) {
            return JsonWrapper.fromJson((String) target);
        }
        return null;
    }
}
