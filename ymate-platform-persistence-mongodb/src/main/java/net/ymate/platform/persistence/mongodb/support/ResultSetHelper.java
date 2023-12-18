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
package net.ymate.platform.persistence.mongodb.support;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IKeyGenerator;
import net.ymate.platform.core.persistence.IValueRenderer;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.PropertyMeta;
import net.ymate.platform.persistence.mongodb.IMongo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/22 下午11:41
 */
public class ResultSetHelper {

    private static final Log LOG = LogFactory.getLog(ResultSetHelper.class);

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T extends IEntity> T toEntity(Class<T> entity, Document document) throws Exception {
        ClassUtils.BeanWrapper<T> beanWrapper = ClassUtils.wrapperClass(entity);
        if (beanWrapper != null) {
            EntityMeta entityMeta = EntityMeta.load(entity);
            Object primaryKeyObject = null;
            if (entityMeta.isMultiplePrimaryKey()) {
                primaryKeyObject = entityMeta.getPrimaryKeyClass().newInstance();
                //
                beanWrapper.getTargetObject().setId((Serializable) primaryKeyObject);
            }
            for (PropertyMeta propertyMeta : entityMeta.getProperties()) {
                Field field = propertyMeta.getField();
                Object propValue;
                if (entityMeta.isPrimaryKey(propertyMeta.getName())) {
                    propValue = document.getObjectId(propertyMeta.getName()).toString();
                } else {
                    propValue = IValueRenderer.processValueRenderer(field, BlurObject.bind(document.get(propertyMeta.getName())).toObjectValue(field.getType()));
                }
                if (propValue == null) {
                    continue;
                }
                if (entityMeta.isPrimaryKey(propertyMeta.getName()) && entityMeta.isMultiplePrimaryKey()) {
                    field.set(primaryKeyObject, propValue);
                } else {
                    field.set(beanWrapper.getTargetObject(), propValue);
                }
            }
            return beanWrapper.getTargetObject();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static <T extends IEntity> List<T> toEntities(Class<T> entity, MongoIterable<Document> iterable) throws Exception {
        try (MongoCursor<Document> documentIt = iterable.iterator()) {
            List<T> resultSet = new ArrayList<>();
            while (documentIt.hasNext()) {
                resultSet.add(toEntity(entity, documentIt.next()));
            }
            return resultSet;
        }
    }

    @SuppressWarnings("rawtypes")
    public static <T extends IEntity> Document toDocument(IMongo owner, T entity, Fields filter, boolean forUpdate) throws Exception {
        EntityMeta entityMeta = EntityMeta.load(entity.getClass());
        Document returnObj = new Document();
        for (PropertyMeta propertyMeta : entityMeta.getProperties()) {
            Object value = propertyMeta.getField().get(entity);
            if (IMongo.Opt.ID.equals(propertyMeta.getName())) {
                if (value == null && !forUpdate && !propertyMeta.isAutoincrement() && StringUtils.isNotBlank(propertyMeta.getUseKeyGenerator())) {
                    IKeyGenerator keyGenerator = IKeyGenerator.Manager.getKeyGenerator(propertyMeta.getUseKeyGenerator());
                    if (keyGenerator != null) {
                        value = keyGenerator.generate(owner, propertyMeta, entity);
                    } else if (LOG.isWarnEnabled()) {
                        LOG.warn(String.format("KeyGenerator named '%s' was not found.", propertyMeta.getUseKeyGenerator()));
                    }
                }
                if (value != null) {
                    if (forUpdate || !propertyMeta.isAutoincrement()) {
                        returnObj.put(propertyMeta.getName(), new ObjectId(value.toString()));
                    }
                }
            } else {
                if (forUpdate && propertyMeta.isReadonly()) {
                    continue;
                }
                // 若属性值为空时尝试使用默认值
                if (value == null && StringUtils.isNotBlank(propertyMeta.getDefaultValue())) {
                    value = BlurObject.bind(propertyMeta.getDefaultValue()).toObjectValue(propertyMeta.getField().getType());
                }
                if (value != null) {
                    // 若字段成员声明了@Conversion注解则执行类型转换
                    if (propertyMeta.getConversionType() != null) {
                        value = BlurObject.bind(value).toObjectValue(propertyMeta.getConversionType());
                    }
                } else if (!propertyMeta.isNullable()) {
                    boolean flag = true;
                    if (filter != null && !filter.fields().isEmpty()) {
                        if (filter.isExcluded()) {
                            Collection<String> names = entityMeta.getPropertyNames().stream().filter(name -> !filter.fields().contains(name)).collect(Collectors.toSet());
                            if (names.isEmpty() || !names.contains(propertyMeta.getName())) {
                                flag = false;
                            }
                        } else if (!filter.fields().contains(propertyMeta.getName())) {
                            flag = false;
                        }
                    }
                    if (flag) {
                        throw new IllegalArgumentException(String.format("Entity field '%s.%s' value can not be null.", entityMeta.getEntityName(), propertyMeta.getName()));
                    }
                }
                if (value == null) {
                    returnObj.append(propertyMeta.getName(), null);
                    continue;
                }
                returnObj.put(propertyMeta.getName(), value);
            }
        }
        return returnObj;
    }
}
