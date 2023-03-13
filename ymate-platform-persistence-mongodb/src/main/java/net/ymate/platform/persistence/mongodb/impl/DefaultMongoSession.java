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
import net.ymate.platform.persistence.mongodb.support.*;
import net.ymate.platform.persistence.mongodb.transaction.Transactions;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/22 下午10:35
 */
@SuppressWarnings("rawtypes")
public class DefaultMongoSession extends AbstractSession<IMongoConnectionHolder> implements IMongoSession {

    private final IMongo owner;

    private final IMongoConnectionHolder connectionHolder;

    private final String collectionPrefix;

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
        if (connectionHolder != null) {
            if (Transactions.get() == null) {
                connectionHolder.close();
            }
        }
    }

    @Override
    public <T extends IEntity> MongoCollection<Document> getCollection(Class<T> entity) {
        return connectionHolder.getConnection().getCollection(collectionPrefix.concat(EntityMeta.load(entity).getEntityName()));
    }

    private <T extends IEntity> FindIterable<Document> doFindIterable(MongoCollection<Document> collection, Query filter, OrderBy orderBy) {
        FindIterable<Document> findIterable;
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (filter != null) {
            if (clientSession == null) {
                findIterable = collection.find(filter.toBson());
            } else {
                findIterable = collection.find(clientSession, filter.toBson());
            }
        } else {
            if (clientSession == null) {
                findIterable = collection.find();
            } else {
                findIterable = collection.find(clientSession);
            }
        }
        if (orderBy != null) {
            findIterable.sort(orderBy.toBson());
        }
        return findIterable;
    }

    private boolean doPageInit(FindIterable<Document> findIterable, Page page) {
        if (page != null && page.page() > 0 && page.pageSize() > 0) {
            findIterable.skip((page.page() - 1) * page.pageSize()).limit(page.pageSize());
            return true;
        }
        return false;
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(final Class<T> entity, Query filter) throws Exception {
        return find(entity, filter, null, null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(Class<T> entity) throws Exception {
        return find(entity, (Query) null, null, null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(Class<T> entity, QueryBuilder filter) throws Exception {
        return find(entity, filter.build(), null, null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(Class<T> entity, Query filter, OrderBy orderBy) throws Exception {
        return find(entity, filter, orderBy, null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(Class<T> entity, QueryBuilder filter, OrderBy orderBy) throws Exception {
        return find(entity, filter.build(), orderBy, null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(Class<T> entity, Query filter, Page page) throws Exception {
        return find(entity, filter, null, page);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(Class<T> entity, QueryBuilder filter, Page page) throws Exception {
        return find(entity, filter.build(), null, page);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(Class<T> entity, Query filter, OrderBy orderBy, Page page) throws Exception {
        MongoCollection<Document> collection = getCollection(entity);
        FindIterable<Document> findIterable = doFindIterable(collection, filter, orderBy);
        if (doPageInit(findIterable, page)) {
            return new DefaultResultSet<>(ResultSetHelper.toEntities(entity, findIterable), page.page(), page.pageSize(), page.isCount() ? doCount(collection, filter) : 0);
        }
        return new DefaultResultSet<>(ResultSetHelper.toEntities(entity, findIterable));
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(Class<T> entity, QueryBuilder filter, OrderBy orderBy, Page page) throws Exception {
        return find(entity, filter.build(), orderBy, page);
    }

    @Override
    public <T extends IEntity> T findFirst(Class<T> entity, Query filter) throws Exception {
        return findFirst(entity, filter, null);
    }

    @Override
    public <T extends IEntity> T findFirst(Class<T> entity, QueryBuilder filter) throws Exception {
        return findFirst(entity, filter.build(), null);
    }

    @Override
    public <T extends IEntity> T findFirst(Class<T> entity, Query filter, OrderBy orderBy) throws Exception {
        MongoCollection<Document> collection = getCollection(entity);
        return ResultSetHelper.toEntity(entity, doFindIterable(collection, filter, orderBy).first());
    }

    @Override
    public <T extends IEntity> T findFirst(Class<T> entity, QueryBuilder filter, OrderBy orderBy) throws Exception {
        return findFirst(entity, filter.build(), orderBy);
    }

    @Override
    public <T extends IEntity> T find(Class<T> entity, Serializable id) throws Exception {
        return findFirst(entity, Query.create(IMongo.Opt.ID, ComparisonExp.eq(new ObjectId(id.toString()))));
    }

    @Override
    public <T extends IEntity> long count(Class<T> entity) throws Exception {
        return count(entity, (Query) null);
    }

    private long doCount(MongoCollection<Document> collection, Query filter) {
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (filter != null) {
            if (clientSession == null) {
                return collection.countDocuments(filter.toBson());
            } else {
                return collection.countDocuments(clientSession, filter.toBson());
            }
        }
        if (clientSession == null) {
            return collection.countDocuments();
        } else {
            return collection.countDocuments(clientSession);
        }
    }

    @Override
    public <T extends IEntity> long count(Class<T> entity, Query filter) throws Exception {
        MongoCollection<Document> collection = getCollection(entity);
        return doCount(collection, filter);
    }

    @Override
    public <T extends IEntity> long count(Class<T> entity, QueryBuilder filter) throws Exception {
        return count(entity, filter.build());
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
    public <T extends IEntity> boolean exists(Class<T> entity, QueryBuilder filter) throws Exception {
        return exists(entity, filter.build());
    }

    @Override
    public <T extends IEntity, RESULT> AggregateIterable<RESULT> aggregate(Class<T> entity, Class<RESULT> resultClass, Aggregation... aggregations) throws Exception {
        List<Bson> pipeline = new ArrayList<>(aggregations.length);
        for (Aggregation aggregation : aggregations) {
            pipeline.add(aggregation.toBson());
        }
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            return getCollection(entity).aggregate(pipeline, resultClass);
        }
        return getCollection(entity).aggregate(clientSession, pipeline, resultClass);
    }

    @Override
    public <T extends IEntity, RESULT> DistinctIterable<RESULT> distinct(Class<T> entity, Class<RESULT> resultClass, String fieldName) throws Exception {
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            return getCollection(entity).distinct(fieldName, resultClass);
        }
        return getCollection(entity).distinct(clientSession, fieldName, resultClass);
    }

    @Override
    public <T extends IEntity, RESULT> DistinctIterable<RESULT> distinct(Class<T> entity, Class<RESULT> resultClass, String fieldName, Query query) throws Exception {
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            return getCollection(entity).distinct(fieldName, query.toBson(), resultClass);
        }
        return getCollection(entity).distinct(clientSession, fieldName, query.toBson(), resultClass);
    }

    @Override
    public <T extends IEntity, RESULT> DistinctIterable<RESULT> distinct(Class<T> entity, Class<RESULT> resultClass, String fieldName, QueryBuilder query) throws Exception {
        return distinct(entity, resultClass, fieldName, query.build());
    }

    @Override
    public <T extends IEntity, RESULT> MapReduceIterable<RESULT> mapReduce(Class<T> entity, Class<RESULT> resultClass, String mapFunction, String reduceFunction) throws Exception {
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            return getCollection(entity).mapReduce(mapFunction, reduceFunction, resultClass);
        }
        return getCollection(entity).mapReduce(clientSession, mapFunction, reduceFunction, resultClass);
    }

    @Override
    public <T extends IEntity> MapReduceIterable<Document> mapReduce(Class<T> entity, String mapFunction, String reduceFunction) throws Exception {
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            return getCollection(entity).mapReduce(mapFunction, reduceFunction);
        }
        return getCollection(entity).mapReduce(clientSession, mapFunction, reduceFunction);
    }

    @Override
    public <T extends IEntity> T update(T entity) throws Exception {
        return update(entity, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IEntity> T update(T entity, Fields filter) throws Exception {
        Document document = ResultSetHelper.toDocument(owner, entity, filter, true);
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
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            document = getCollection(entity.getClass()).findOneAndUpdate(query.toBson(), updateExp.toBson());
        } else {
            document = getCollection(entity.getClass()).findOneAndUpdate(clientSession, query.toBson(), updateExp.toBson());
        }
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
        Document document = ResultSetHelper.toDocument(owner, entity, null, false);
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            getCollection(entity.getClass()).insertOne(document);
        } else {
            getCollection(entity.getClass()).insertOne(clientSession, document);
        }
        return (T) ResultSetHelper.toEntity(entity.getClass(), document);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IEntity> List<T> insert(List<T> entities) throws Exception {
        Class<T> entityClass = (Class<T>) entities.get(0).getClass();
        List<Document> documents = new ArrayList<>();
        for (T entity : entities) {
            documents.add(ResultSetHelper.toDocument(owner, entity, null, false));
        }
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            getCollection(entityClass).insertMany(documents);
        } else {
            getCollection(entityClass).insertMany(clientSession, documents);
        }
        List<T> returnValue = new ArrayList<>();
        for (Document document : documents) {
            returnValue.add(ResultSetHelper.toEntity(entityClass, document));
        }
        return returnValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IEntity> T delete(T entity) throws Exception {
        return (T) delete(entity.getClass(), entity.getId());
    }

    @Override
    public <T extends IEntity> T delete(Class<T> entity, Serializable id) throws Exception {
        Document document;
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            document = getCollection(entity).findOneAndDelete(Query.create(IMongo.Opt.ID, ComparisonExp.eq(new ObjectId(id.toString()))).toBson());
        } else {
            document = getCollection(entity).findOneAndDelete(clientSession, Query.create(IMongo.Opt.ID, ComparisonExp.eq(new ObjectId(id.toString()))).toBson());
        }
        return ResultSetHelper.toEntity(entity, document);
    }

    @Override
    public <T extends IEntity> long delete(List<T> entities) throws Exception {
        List<Serializable> ids = entities.stream()
                .map(IEntity::getId)
                .filter(id -> id != null && StringUtils.isNotBlank(id.toString()))
                .collect(Collectors.toList());
        return delete(entities.get(0).getClass(), ids);
    }

    @Override
    public <T extends IEntity> long delete(Class<T> entity, Collection<Serializable> ids) throws Exception {
        Params objectIds = Params.create();
        ids.stream().map(id -> new ObjectId(id.toString())).forEach(objectIds::add);
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        DeleteResult result;
        if (clientSession == null) {
            result = getCollection(entity).deleteMany(Query.create(IMongo.Opt.ID, ComparisonExp.in(objectIds)).toBson());
        } else {
            result = getCollection(entity).deleteMany(clientSession, Query.create(IMongo.Opt.ID, ComparisonExp.in(objectIds)).toBson());
        }
        return result.getDeletedCount();
    }
}
