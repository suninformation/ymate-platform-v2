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
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.base.PropertyMeta;
import net.ymate.platform.persistence.mongodb.IMongo;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/22 下午11:41
 */
public class ResultSetHelper {

    @SuppressWarnings("unchecked")
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
                Object propValue;
                if (entityMeta.isPrimaryKey(propertyMeta.getName())) {
                    propValue = document.getObjectId(propertyMeta.getName()).toString();
                } else {
                    propValue = document.get(propertyMeta.getName());
                }
                if (propValue == null) {
                    if (StringUtils.trimToNull(propertyMeta.getDefaultValue()) != null) {
                        propValue = BlurObject.bind(propertyMeta.getDefaultValue()).toObjectValue(propertyMeta.getField().getType());
                    } else {
                        continue;
                    }
                }
                if (entityMeta.isPrimaryKey(propertyMeta.getName()) && entityMeta.isMultiplePrimaryKey()) {
                    propertyMeta.getField().set(primaryKeyObject, propValue);
                } else {
                    propertyMeta.getField().set(beanWrapper.getTargetObject(), propValue);
                }
            }
            return beanWrapper.getTargetObject();
        }
        return null;
    }

    public static <T extends IEntity> List<T> toEntities(Class<T> entity, MongoIterable<Document> iterable) throws Exception {
        MongoCursor<Document> documentIt = iterable.iterator();
        List<T> resultSet = new ArrayList<>();
        while (documentIt.hasNext()) {
            resultSet.add(toEntity(entity, documentIt.next()));
        }
        return resultSet;
    }

    public static <T extends IEntity> Document toDocument(T entity) throws Exception {
        EntityMeta entityMeta = EntityMeta.load(entity.getClass());
        Document returnObj = new Document();
        for (PropertyMeta propertyMeta : entityMeta.getProperties()) {
            Object value = propertyMeta.getField().get(entity);
            if (value == null) {
                returnObj.append(propertyMeta.getName(), null);
                continue;
            }
            if (IMongo.Opt.ID.equals(propertyMeta.getName())) {
                returnObj.put(propertyMeta.getName(), new ObjectId(value.toString()));
            } else {
                returnObj.put(propertyMeta.getName(), value);
            }
        }
        return returnObj;
    }
}
