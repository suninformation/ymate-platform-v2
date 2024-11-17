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

import net.ymate.platform.core.beans.proxy.IProxyFactory;
import net.ymate.platform.core.beans.support.PropertyStateSupport;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.IDatabase;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

/**
 * @param <Entity> 数据实体类型
 * @author 刘镇 (suninformation@163.com) on 2019-11-09 11:43
 * @since 2.1.0
 */
@SuppressWarnings("rawtypes")
public final class EntityStateWrapper<Entity extends IEntity> {

    private final IDatabase owner;

    private final PropertyStateSupport<Entity> stateSupport;

    public static <Entity extends IEntity> EntityStateWrapper<Entity> bind(Entity entity, boolean ignoreNull) throws Exception {
        return new EntityStateWrapper<>(null, entity, ignoreNull);
    }

    public static <Entity extends IEntity> EntityStateWrapper<Entity> bind(IDatabase owner, Entity entity, boolean ignoreNull) throws Exception {
        return new EntityStateWrapper<>(owner, entity, ignoreNull);
    }

    private EntityStateWrapper(IDatabase owner, Entity entity, boolean ignoreNull) throws Exception {
        this.owner = owner;
        this.stateSupport = PropertyStateSupport.create(owner != null ? owner.getOwner().getConfigureFactory().getConfigurer().getProxyFactory() : null, entity, ignoreNull);
    }

    public PropertyStateSupport<Entity> getStateSupport() {
        return stateSupport;
    }

    /**
     * @return 返回true表示某属性值已发生变化
     * @since 2.1.2
     */
    public boolean hasChanged() {
        return stateSupport.hasChanged();
    }

    /**
     * @param propertyOrAliasName 属性名或别名
     * @return 返回true表示该属性值已发生变化
     * @since 2.1.3
     */
    public boolean isChanged(String propertyOrAliasName) {
        return stateSupport.isChanged(propertyOrAliasName);
    }

    public IProxyFactory getProxyFactory() {
        return stateSupport.getProxyFactory();
    }

    public EntityStateWrapper<Entity> setProxyFactory(IProxyFactory proxyFactory) {
        stateSupport.setProxyFactory(proxyFactory);
        return this;
    }

    public Entity getEntity() {
        return stateSupport.bind();
    }

    /**
     * @see EntityStateWrapper#update(Fields)
     */
    @Deprecated
    public Entity updateNotIncluded(Fields fields) throws Exception {
        return update(fields.excluded());
    }

    /**
     * @param fields 字段过滤对象
     * @return 执行更新并返回实体对象
     * @throws Exception 可能产生的任何异常
     */
    public Entity update(Fields fields) throws Exception {
        if (fields == null || fields.isEmpty()) {
            return update((IUpdatePropertyFilter) null);
        } else {
            return update(propertyNames -> {
                Fields filtered = Fields.create();
                Arrays.stream(propertyNames)
                        .filter(propertyName -> fields.isExcluded() != fields.fields().contains(propertyName))
                        .forEach(filtered::add);
                return filtered;
            });
        }
    }

    public Entity update() throws Exception {
        return update((IUpdatePropertyFilter) null);
    }

    @SuppressWarnings("unchecked")
    public Entity update(IUpdatePropertyFilter propertyFilter) throws Exception {
        String[] propNames = stateSupport.getChangedPropertyNames();
        if (ArrayUtils.isNotEmpty(propNames)) {
            Fields fields;
            if (propertyFilter != null) {
                fields = propertyFilter.filter(propNames);
            } else {
                fields = Fields.create(propNames);
            }
            if (!fields.isEmpty()) {
                Entity entity = stateSupport.unbind();
                if (entity instanceof BaseEntity) {
                    return (Entity) ((BaseEntity) entity).update(fields);
                } else {
                    return EntityWrapper.bind(owner, entity).update(fields);
                }
            }
        }
        return null;
    }

    /**
     * 待更新字段属性过滤器接口
     */
    public interface IUpdatePropertyFilter {

        /**
         * 执行字段过滤操作
         *
         * @param propertyNames 待更新字段名称集合
         * @return 返回最终参与更新的字段集合，返回空集合将终止更新操作
         */
        Fields filter(String[] propertyNames);
    }
}
