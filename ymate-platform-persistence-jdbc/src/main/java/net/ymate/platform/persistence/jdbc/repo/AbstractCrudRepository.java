/*
 * Copyright 2007-2024 the original author or authors.
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
package net.ymate.platform.persistence.jdbc.repo;

import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IResultSet;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.core.persistence.Params;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.support.ErrorCode;
import net.ymate.platform.persistence.jdbc.IDBLocker;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.base.impl.BatchUpdateOperator;
import net.ymate.platform.persistence.jdbc.query.*;
import net.ymate.platform.persistence.jdbc.support.EntityStateWrapper;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;

/**
 * @author 刘镇 (suninformation@163.com) on 2024/10/27 00:22
 * @since 2.1.3
 */
public abstract class AbstractCrudRepository<PK extends Serializable, ENTITY extends IEntity<PK>, QUERY_BEAN, CREATE_BEAN, UPDATE_BEAN, VERSION extends Serializable> extends AbstractRepository {

    private final Class<ENTITY> entityClass;

    protected AbstractCrudRepository(Class<ENTITY> entityClass) {
        this.entityClass = entityClass;
    }

    protected abstract ErrorCode beforeCreate(IDatabase owner, String dataSourceName, ENTITY entity, CREATE_BEAN createBean) throws Exception;

    protected PairObject<ErrorCode, ENTITY> doCreate(IDatabase owner, String dataSourceName, CREATE_BEAN createBean, Fields filter) throws Exception {
        if (createBean == null) {
            throw new NullArgumentException("createBean");
        }
        ENTITY entity = ClassUtils.wrapper(createBean).duplicate(entityClass.newInstance());
        ErrorCode errorCode = beforeCreate(owner, dataSourceName, entity, createBean);
        if (errorCode == null) {
            entity = doInsert(owner, dataSourceName, entity, filter);
            if (entity != null) {
                errorCode = ErrorCode.succeed().dataAttr("id", entity.getId());
            }
        }
        return PairObject.bind(errorCode, entity);
    }

    protected abstract ErrorCode beforeUpdate(IDatabase owner, String dataSourceName, ENTITY entity, UPDATE_BEAN updateBean, VERSION version) throws Exception;

    protected PairObject<ErrorCode, ENTITY> doUpdate(IDatabase owner, String dataSourceName, PK id, UPDATE_BEAN updateBean, VERSION version, Fields filter, boolean ignoreNull) throws Exception {
        if (id == null) {
            throw new NullArgumentException("id");
        }
        if (updateBean == null) {
            throw new NullArgumentException("updateBean");
        }
        ErrorCode errorCode = null;
        ENTITY entity = doFind(dataSourceName, EntitySQL.create(owner, entityClass).forUpdate(IDBLocker.DEFAULT), id);
        if (entity != null) {
            EntityStateWrapper<ENTITY> stateWrapper = EntityStateWrapper.bind(owner, entity, ignoreNull);
            entity = ClassUtils.wrapper(updateBean).duplicate(stateWrapper.getEntity());
            int effectCounts = 0;
            if (stateWrapper.hasChanged()) {
                errorCode = beforeUpdate(owner, dataSourceName, entity, updateBean, version);
                if (errorCode == null) {
                    entity = stateWrapper.update(filter);
                    effectCounts = entity != null ? 1 : 0;
                }
            }
            if (errorCode == null) {
                errorCode = ErrorCode.succeed().dataAttr("effectCounts", effectCounts);
            }
        }
        return PairObject.bind(errorCode, entity);
    }

    protected int doUpdate(IDatabase owner, String dataSourceName, PK[] ids, Fields fields, Params values) throws Exception {
        if (ArrayUtils.isEmpty(ids)) {
            throw new NullArgumentException("ids");
        }
        if (fields == null || fields.isEmpty()) {
            throw new NullArgumentException("fields");
        }
        if (values == null || values.isEmpty()) {
            throw new NullArgumentException("values");
        }
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        if (entityMeta.getPropertyNames().containsAll(fields.fields())) {
            Cond cond = Cond.create(owner, dataSourceName);
            entityMeta.getPrimaryKeys().forEach(primaryKey -> cond.andIfNeed().eqWrap(primaryKey));
            //
            Update update = Update.create(owner, dataSourceName, entityClass)
                    .field(fields)
                    .where(cond.buildWhere());
            BatchSQL batchSql = BatchSQL.create(update);
            for (PK id : ids) {
                Params params = Params.create(values);
                if (entityMeta.isMultiplePrimaryKey()) {
                    for (String primaryKey : entityMeta.getPrimaryKeys()) {
                        params.add(entityMeta.getPropertyByName(primaryKey).getField().get(id));
                    }
                } else {
                    params.add(id);
                }
                batchSql.addParameter(params);
            }
            return BatchUpdateOperator.parseEffectCounts(batchSql.execute(update.dataSourceName()));
        }
        return 0;
    }

    protected <VO_BEAN> VO_BEAN doQuery(IDatabase owner, String dataSourceName, Class<VO_BEAN> voBeanClass, String prefix, PK id, Fields excludedFields) throws Exception {
        if (id == null) {
            throw new NullArgumentException("id");
        }
        Cond cond = Cond.create(owner, dataSourceName);
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        if (entityMeta.isMultiplePrimaryKey()) {
            for (String primaryKey : entityMeta.getPrimaryKeys()) {
                cond.andIfNeed().eqWrap(prefix, primaryKey).param(entityMeta.getPropertyByName(primaryKey).getField().get(id));
            }
        } else {
            entityMeta.getPrimaryKeys().stream().findFirst().ifPresent(primaryKey -> cond.andIfNeed().eqWrap(prefix, primaryKey).param(id));
        }
        return Query.build(owner, dataSourceName, voBeanClass).where(cond.buildWhere(), true)
                .addExcludeField(excludedFields)
                .findFirst();
    }

    protected <VO_BEAN> IResultSet<VO_BEAN> doQuery(IDatabase owner, String dataSourceName, Class<VO_BEAN> voBeanClass, QUERY_BEAN queryBean, Cond additionalCond, OrderBy orderBy, Fields excludedFields, Page page) throws Exception {
        Cond cond = Cond.create(owner, dataSourceName, queryBean);
        if (additionalCond != null && !additionalCond.isEmpty()) {
            cond.andIfNeed(additionalCond);
        }
        return doQuery(owner, dataSourceName, voBeanClass, cond, orderBy, excludedFields, page);
    }

    protected <VO_BEAN> IResultSet<VO_BEAN> doQuery(IDatabase owner, String dataSourceName, Class<VO_BEAN> voBeanClass, Cond cond, OrderBy orderBy, Fields excludedFields, Page page) throws Exception {
        Where where = cond.buildWhere();
        if (orderBy != null && !orderBy.isEmpty()) {
            where.orderBy().orderBy(orderBy);
        }
        return Query.build(owner, dataSourceName, voBeanClass)
                .where(where, true)
                .addExcludeField(excludedFields)
                .find(page);
    }

    protected int doRemove(IDatabase owner, String dataSourceName, PK id) throws Exception {
        if (id == null) {
            throw new NullArgumentException("id");
        }
        return doDelete(owner, dataSourceName, entityClass, id);
    }

    public int doRemove(IDatabase owner, String dataSourceName, PK[] ids) throws Exception {
        if (ArrayUtils.isEmpty(ids)) {
            throw new NullArgumentException("ids");
        }
        return BatchUpdateOperator.parseEffectCounts(doDelete(owner, dataSourceName, entityClass, ids));
    }
}
