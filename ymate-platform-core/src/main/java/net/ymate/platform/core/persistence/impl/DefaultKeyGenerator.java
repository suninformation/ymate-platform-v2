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
package net.ymate.platform.core.persistence.impl;

import net.ymate.platform.commons.util.UUIDUtils;
import net.ymate.platform.core.persistence.IKeyGenerator;
import net.ymate.platform.core.persistence.IPersistence;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.PropertyMeta;

/**
 * 默认键值生成器，采用UUID策略
 *
 * @author 刘镇 (suninformation@163.com) on 2021/5/10 5:11 下午
 * @since 2.1.0
 */
public class DefaultKeyGenerator implements IKeyGenerator {

    @Override
    public Object generate(IPersistence<?, ?, ?, ?> owner, PropertyMeta propertyMeta, IEntity<?> entity) {
        if (propertyMeta.getField().getType().equals(String.class)) {
            return UUIDUtils.UUID();
        }
        return null;
    }
}
