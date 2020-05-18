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
package net.ymate.platform.persistence.jdbc.support;

import net.ymate.platform.core.beans.support.PropertyStateSupport;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.base.IEntity;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * @param <Entity> 数据实体类型
 * @author 刘镇 (suninformation@163.com) on 2019-11-09 11:43
 * @since 2.1.0
 */
public final class EntityStateWrapper<Entity extends IEntity> {

    private final PropertyStateSupport<Entity> stateSupport;

    public static <Entity extends IEntity> EntityStateWrapper<Entity> bind(Entity entity, boolean ignoreNull) throws Exception {
        return new EntityStateWrapper<>(entity, ignoreNull);
    }

    private EntityStateWrapper(Entity entity, boolean ignoreNull) throws Exception {
        this.stateSupport = PropertyStateSupport.create(entity, ignoreNull);
    }

    public PropertyStateSupport<Entity> getStateSupport() {
        return stateSupport;
    }

    public Entity getEntity() {
        return stateSupport.bind();
    }

    public Entity updateNotIncluded(Fields fields) throws Exception {
        return update(propertyNames -> !Arrays.asList(propertyNames).containsAll(fields.fields()));
    }

    public Entity update() throws Exception {
        return update(null);
    }

    @SuppressWarnings("unchecked")
    public Entity update(IUpdatePropertyChecker propertyChecker) throws Exception {
        String[] propNames = stateSupport.getChangedPropertyNames();
        if (ArrayUtils.isNotEmpty(propNames)) {
            if (propertyChecker == null || propertyChecker.check(propNames)) {
                Entity entity = stateSupport.unbind();
                if (entity instanceof BaseEntity) {
                    return (Entity) ((BaseEntity) entity).update(Fields.create(propNames));
                } else {
                    return EntityWrapper.bind(entity).update(Fields.create(propNames));
                }
            }
        }
        return null;
    }

    /**
     * 待更新字段属性检测器接口
     */
    public interface IUpdatePropertyChecker {

        /**
         * 执行检查操作
         *
         * @param propertyNames 待更新字段名称集合
         * @return 返回true表示允许执行更新操作
         */
        boolean check(String[] propertyNames);
    }
}
