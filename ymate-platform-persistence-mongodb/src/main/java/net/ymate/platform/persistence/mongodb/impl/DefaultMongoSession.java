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
package net.ymate.platform.persistence.mongodb.impl;

import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import net.ymate.platform.core.persistence.*;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.core.persistence.impl.DefaultResultSet;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IMongoConnectionHolder;
import net.ymate.platform.persistence.mongodb.IMongoSession;
import net.ymate.platform.persistence.mongodb.expression.ComparisonExp;
import net.ymate.platform.persistence.mongodb.expression.UpdateExp;
import net.ymate.platform.persistence.mongodb.support.Aggregation;
import net.ymate.platform.persistence.mongodb.support.OrderBy;
import net.ymate.platform.persistence.mongodb.support.Query;
import net.ymate.platform.persistence.mongodb.support.ResultSetHelper;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/22 下午10:35
 */
public class DefaultMongoSession extends AbstractSession<IMongoConnectionHolder> implements IMongoSession {

    private IMongo owner;

    private IMongoConnectionHolder connectionHolder;

    private String collectionPrefix;

    public DefaultMongoSession(IMongo owner) throws Exception {
        this(owner, owner.getDefaultConnectionHolder());
    }

    public DefaultMongoSession(IMongo owner, IMongoConnectionHolder connectionHolder) {
        this.owner = owner;
        this.connectionHolder = connectionHolder;
        this.collectionPrefix = StringUtils.trimToEmpty(connectionHolder.getDataSourceConfig().getCollectionPrefix());
    }

    public IMongo getOwner() {
        return owner;
    }

    @Override
    public IMongoConnectionHolder getConnectionHolder() {
        return connectionHolder;
    }

    @Override
    public void close() throws Exception {
        connectionHolder.close();
    }

    private <T extends IEntity> MongoCollection<Document> doGetCollection(Class<T> entity) {
        return connectionHolder.getConnection().getCollection(collectionPrefix.concat(EntityMeta.load(entity).getEntityName()));
    }

    private boolean doPageInit(FindIterable<Document> findIterable, Page page) {
        if (page != null && page.page() > 0 && page.pageSize() > 0) {
            findIterable.skip((page.page() - 1) * page.pageSize()).limit(page.pageSize());
            return true;
        }
        return false;
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(final Class<T> entity) throws Exception {
        if (getSessionEventListener() != null) {
            getSessionEventListener().onQueryBefore(new SessionEventContext(entity));
        }
        return new DefaultResultSet<>(ResultSetHelper.toEntities(entity, doGetCollection(entity).find()));
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(Class<T> entity, OrderBy orderBy) throws Exception {
        return find(entity, orderBy, null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(Class<T> entity, Page page) throws Exception {
        return find(entity, null, page);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(Class<T> entity, OrderBy orderBy, Page page) throws Exception {
        MongoCollection<Document> collection = doGetCollection(entity);
        FindIterable<Document> findIterable = collection.find();
        if (orderBy != null) {
            findIterable.sort(orderBy.toBson());
        }
        if (doPageInit(findIterable, page)) {
            return new DefaultResultSet<>(ResultSetHelper.toEntities(entity, findIterable), page.page(), page.pageSize(), page.isCount() ? collection.countDocuments() : 0);
        }
        return new DefaultResultSet<>(ResultSetHelper.toEntities(entity, findIterable));
    }

    @Override
    public <T extends IEntity> T findFirst(Class<T> entity, Query filter) throws Exception {
        return ResultSetHelper.toEntity(entity, doGetCollection(entity).find(filter.toBson()).first());
    }

    @Override
    public <T extends IEntity> T find(Class<T> entity, Serializable id) throws Exception {
        return findFirst(entity, Query.create(IMongo.Opt.ID, ComparisonExp.eq(new ObjectId(id.toString()))));
    }

    @Override
    public <T extends IEntity> long count(Class<T> entity) throws Exception {
        return doGetCollection(entity).countDocuments();
    }

    @Override
    public <T extends IEntity> long count(Class<T> entity, Query filter) throws Exception {
        return doGetCollection(entity).countDocuments(filter.toBson());
    }

    @Override
    public <T extends IEntity> boolean exists(Class<T> entity, Serializable id) throws Exception {
        return find(entity, id) != null;
    }

    @Override
    public <T extends IEntity> boolean exists(Class<T> entity, Query filter) throws Exception {
        return findFirst(entity, filter) != null;
    }

    @Override
    public <T extends IEntity, RESULT> AggregateIterable<RESULT> aggregate(Class<T> entity, Class<RESULT> resultClass, Aggregation... aggregations) throws Exception {
        List<Bson> pipeline = new ArrayList<>(aggregations.length);
        for (Aggregation aggregation : aggregations) {
            pipeline.add(aggregation.toBson());
        }
        return doGetCollection(entity).aggregate(pipeline, resultClass);
    }

    @Override
    public <T extends IEntity, RESULT> DistinctIterable<RESULT> distinct(Class<T> entity, Class<RESULT> resultClass, String fieldName) throws Exception {
        return doGetCollection(entity).distinct(fieldName, resultClass);
    }

    @Override
    public <T extends IEntity, RESULT> DistinctIterable<RESULT> distinct(Class<T> entity, Class<RESULT> resultClass, String fieldName, Query query) throws Exception {
        return doGetCollection(entity).distinct(fieldName, query.toBson(), resultClass);
    }

    @Override
    public <T extends IEntity, RESULT> MapReduceIterable<RESULT> mapReduce(Class<T> entity, Class<RESULT> resultClass, String mapFunction, String reduceFunction) throws Exception {
        return doGetCollection(entity).mapReduce(mapFunction, reduceFunction, resultClass);
    }

    @Override
    public <T extends IEntity> MapReduceIterable<Document> mapReduce(Class<T> entity, String mapFunction, String reduceFunction) throws Exception {
        return doGetCollection(entity).mapReduce(mapFunction, reduceFunction);
    }

    @Override
    public <T extends IEntity> T update(T entity) throws Exception {
        return update(entity, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IEntity> T update(T entity, Fields filter) throws Exception {
        Document document = ResultSetHelper.toDocument(entity);
        Query query = Query.create(IMongo.Opt.ID, ComparisonExp.eq(document.remove(IMongo.Opt.ID)));
        UpdateExp updateExp = new UpdateExp();
        if (filter != null && !filter.fields().isEmpty()) {
            if (filter.isExcluded()) {
                updateExp.add(UpdateExp.unset(filter));
            } else {
                for (String key : filter.fields()) {
                    updateExp.add(UpdateExp.set(key, document.get(key)));
                }
            }
        } else {
            updateExp.add(UpdateExp.set(document));
        }
        document = doGetCollection(entity.getClass()).findOneAndUpdate(query.toBson(), updateExp.toBson());
        return (T) ResultSetHelper.toEntity(entity.getClass(), document);
    }

    @Override
    public <T extends IEntity> List<T> update(List<T> entities) throws Exception {
        return update(entities, null);
    }

    @Override
    public <T extends IEntity> List<T> update(List<T> entities, Fields filter) throws Exception {
        List<T> results = new ArrayList<>();
        for (T entity : entities) {
            results.add(update(entity, filter));
        }
        return results;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IEntity> T insert(T entity) throws Exception {
        Document document = ResultSetHelper.toDocument(entity);
        if (entity.getId() != null && !StringUtils.isBlank(String.valueOf(entity.getId()))) {
            document.remove(IMongo.Opt.ID);
        }
        doGetCollection(entity.getClass()).insertOne(document);
        entity.setId(String.valueOf(document.get(IMongo.Opt.ID)));
        return entity;
    }

    @Override
    public <T extends IEntity> List<T> insert(List<T> entities) throws Exception {
        for (T entity : entities) {
            insert(entity);
        }
        return entities;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IEntity> T delete(T entity) throws Exception {
        return (T) delete(entity.getClass(), entity.getId());
    }

    @Override
    public <T extends IEntity> T delete(Class<T> entity, Serializable id) throws Exception {
        Document document = doGetCollection(entity).findOneAndDelete(Query.create(IMongo.Opt.ID, ComparisonExp.eq(new ObjectId(id.toString()))).toBson());
        return ResultSetHelper.toEntity(entity, document);
    }

    @Override
    public <T extends IEntity> List<T> delete(List<T> entities) throws Exception {
        List<T> results = new ArrayList<>();
        for (T entity : entities) {
            results.add(delete(entity));
        }
        return results;
    }

    @Override
    public <T extends IEntity> long delete(Class<T> entity, Collection<Serializable> ids) throws Exception {
        DeleteResult result = doGetCollection(entity).deleteMany(Query.create(IMongo.Opt.ID, ComparisonExp.in(Params.create(ids))).toBson());
        return result.getDeletedCount();
    }
}
