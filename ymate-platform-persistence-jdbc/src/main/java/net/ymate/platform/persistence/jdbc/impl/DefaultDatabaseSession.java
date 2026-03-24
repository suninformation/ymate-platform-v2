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
package net.ymate.platform.persistence.jdbc.impl;

import net.ymate.platform.commons.lang.BlurObject;
import net.ymate.platform.commons.lang.PairObject;
import net.ymate.platform.commons.util.ExpressionUtils;
import net.ymate.platform.core.persistence.*;
import net.ymate.platform.core.persistence.base.*;
import net.ymate.platform.core.persistence.impl.DefaultResultSet;
import net.ymate.platform.persistence.jdbc.*;
import net.ymate.platform.persistence.jdbc.base.*;
import net.ymate.platform.persistence.jdbc.base.impl.*;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import net.ymate.platform.persistence.jdbc.dialect.impl.OracleDialect;
import net.ymate.platform.persistence.jdbc.query.*;
import net.ymate.platform.persistence.jdbc.support.BaseEntity;
import net.ymate.platform.persistence.jdbc.transaction.Transactions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * 默认数据库会话操作接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-27 下午03:09:46
 */
@SuppressWarnings("rawtypes")
public class DefaultDatabaseSession extends AbstractSession<IDatabaseConnectionHolder, IDatabaseSessionEventListener> implements IDatabaseSession {

    private static final Log LOG = LogFactory.getLog(DefaultDatabaseSession.class);

    private final IDatabase owner;

    private final IDatabaseConnectionHolder connectionHolder;

    private final IDialect dialect;

    private final String tablePrefix;

    public DefaultDatabaseSession(IDatabase owner) throws Exception {
        this(owner, owner.getDefaultConnectionHolder());
    }

    public DefaultDatabaseSession(IDatabase owner, IDatabaseConnectionHolder connectionHolder) {
        this.owner = owner;
        this.connectionHolder = connectionHolder;
        //
        dialect = connectionHolder.getDialect();
        tablePrefix = connectionHolder.getDataSourceConfig().getTablePrefix();
    }

    public IDatabase getOwner() {
        return owner;
    }

    @Override
    public IDatabaseConnectionHolder getConnectionHolder() {
        return connectionHolder;
    }

    @Override
    public void close() throws Exception {
        // 同时需要判断当前连接是否参与事务，若存在事务则不进行关闭操作
        if (connectionHolder != null) {
            if (Transactions.get() == null) {
                connectionHolder.close();
            }
        }
    }

    private <T extends IOperator> T doOperator(DatabaseEvent.EVENT event, DatabaseSessionEventContext eventContext, IOperatorBuilder<T> operatorBuilder) throws Exception {
        DatabaseSessionEventListener sessionEventListener = new DatabaseSessionEventListener(owner.getGlobalSessionEventListener(), getSessionEventListener());
        switch (eventContext.getOperationType()) {
            case QUERY:
                sessionEventListener.onQueryBefore(eventContext);
                break;
            case INSERT:
            case BATCH_INSERT:
                sessionEventListener.onInsertBefore(eventContext);
                break;
            case INSERT_IF_NOT_EXIST:
            case BATCH_INSERT_IF_NOT_EXIST:
                sessionEventListener.onInsertIfNotExistBefore(eventContext);
                break;
            case UPDATE:
            case BATCH_UPDATE:
                sessionEventListener.onUpdateBefore(eventContext);
                break;
            case UPSERT:
            case BATCH_UPSERT:
                sessionEventListener.onUpsertBefore(eventContext);
                break;
            case DELETE:
            case BATCH_DELETE:
                sessionEventListener.onRemoveBefore(eventContext);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + eventContext.getSql());
        }
        T operator = operatorBuilder.build(eventContext);
        //
        if (eventContext.getParams() != null) {
            eventContext.getParams().params().forEach(operator::addParameter);
        }
        operator.execute();
        eventContext.putAttribute(IOperator.class.getName(), operator);
        switch (eventContext.getOperationType()) {
            case QUERY:
                sessionEventListener.onQueryAfter(eventContext);
                break;
            case INSERT:
            case BATCH_INSERT:
                sessionEventListener.onInsertAfter(eventContext);
                break;
            case INSERT_IF_NOT_EXIST:
            case BATCH_INSERT_IF_NOT_EXIST:
                sessionEventListener.onInsertIfNotExistAfter(eventContext);
                break;
            case UPDATE:
            case BATCH_UPDATE:
                sessionEventListener.onUpdateAfter(eventContext);
                break;
            case UPSERT:
            case BATCH_UPSERT:
                sessionEventListener.onUpsertAfter(eventContext);
                break;
            case DELETE:
            case BATCH_DELETE:
                sessionEventListener.onRemoveAfter(eventContext);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + eventContext.getOperationType());
        }
        owner.getOwner().getEvents().fireEvent(new DatabaseEvent(owner, event).setEventSource(eventContext));
        return operator;
    }

    private String doForUpdateIfNeed(String sqlStr, IDBLocker dbLocker) {
        if (dbLocker != null) {
            return String.format("%s %s", sqlStr, dbLocker.toSQL());
        }
        return sqlStr;
    }

    @Override
    public <T> IResultSet<T> find(SQL sql, IResultSetHandler<T> handler) throws Exception {
        IQueryOperator<T> queryOperator = doOperator(DatabaseEvent.EVENT.QUERY_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.QUERY)
                .sql(sql)
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> new DefaultQueryOperator<>(sessionEventContext.getSql(), connectionHolder, handler));
        //
        return new DefaultResultSet<>(queryOperator.getResultSet());
    }

    @Override
    public <T> IResultSet<T> find(SQL sql, IResultSetHandler<T> handler, Page page) throws Exception {
        String sqlStr = sql.toString();
        //
        long count = 0;
        if (page != null) {
            sqlStr = dialect.buildPagedQuerySql(sql.toString(), page.page(), page.pageSize());
            if (page.isCount()) {
                count = this.count(sql);
                if (count == 0) {
                    return new DefaultResultSet<>(Collections.emptyList(), page.page(), page.pageSize(), count);
                }
            }
        }
        //
        IQueryOperator<T> queryOperator = doOperator(DatabaseEvent.EVENT.QUERY_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.QUERY)
                .sql(sqlStr)
                .params(sql.params())
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> new DefaultQueryOperator<>(sessionEventContext.getSql(), this.connectionHolder, handler));
        //
        if (page != null) {
            return new DefaultResultSet<>(queryOperator.getResultSet(), page.page(), page.pageSize(), count);
        }
        return new DefaultResultSet<>(queryOperator.getResultSet());
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(T entity) throws Exception {
        return find(entity, Fields.create(), null, entity instanceof IShardingable ? (IShardingable) entity : null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(T entity, IShardingable shardingable) throws Exception {
        return find(entity, Fields.create(), null, shardingable);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(T entity, Page page) throws Exception {
        return find(entity, Fields.create(), page, entity instanceof IShardingable ? (IShardingable) entity : null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(T entity, Page page, IShardingable shardingable) throws Exception {
        return find(entity, Fields.create(), page, shardingable);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(T entity, Fields filter) throws Exception {
        return find(entity, filter, null, entity instanceof IShardingable ? (IShardingable) entity : null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(T entity, Fields filter, IShardingable shardingable) throws Exception {
        return find(entity, filter, null, shardingable);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(T entity, Fields filter, Page page) throws Exception {
        return find(entity, filter, page, entity instanceof IShardingable ? (IShardingable) entity : null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IEntity> IResultSet<T> find(T entity, Fields filter, Page page, IShardingable shardingable) throws Exception {
        return (IResultSet<T>) this.find(EntitySQL.create(entity.getClass()).field(filter), Where.create(BaseEntity.buildCond(owner, entity)), page, shardingable);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity) throws Exception {
        return find(entity, null, null, null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, IShardingable shardingable) throws Exception {
        return this.find(entity, null, null, shardingable);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Page page) throws Exception {
        return find(entity, null, page, null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Page page, IShardingable shardingable) throws Exception {
        return this.find(entity, null, page, shardingable);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Where where) throws Exception {
        return find(entity, where, null, null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Where where, IShardingable shardingable) throws Exception {
        return this.find(entity, where, null, shardingable);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Where where, Page page) throws Exception {
        return find(entity, where, page, null);
    }

    @Override
    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Where where, Page page, IShardingable shardingable) throws Exception {
        String sqlStr = dialect.buildSelectSql(entity.entityClass(), tablePrefix, entity.shardingable() != null ? entity.shardingable() : shardingable, doGetNotExcludedFields(EntityMeta.load(entity.entityClass()), entity.fields(), false, true));
        if (where != null) {
            sqlStr = sqlStr.concat(StringUtils.SPACE).concat(where.toString());
        }
        long count = 0;
        if (page != null) {
            sqlStr = dialect.buildPagedQuerySql(sqlStr, page.page(), page.pageSize());
            if (page.isCount()) {
                count = this.count(entity.entityClass(), where);
                if (count == 0) {
                    return new DefaultResultSet<>(Collections.emptyList(), page.page(), page.pageSize(), count);
                }
            }
        }
        //
        IQueryOperator<T> queryOperator = doOperator(DatabaseEvent.EVENT.QUERY_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.QUERY)
                .sql(sqlStr)
                .params(where != null ? where.params() : null)
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> new DefaultQueryOperator<>(doForUpdateIfNeed(sessionEventContext.getSql(), entity.forUpdate()), this.connectionHolder, new EntityResultSetHandler<>(entity.entityClass())));
        //
        if (page != null) {
            return new DefaultResultSet<>(queryOperator.getResultSet(), page.page(), page.pageSize(), count);
        }
        return new DefaultResultSet<>(queryOperator.getResultSet());
    }

    @Override
    public <T extends IEntity> T find(EntitySQL<T> entity, Serializable id) throws Exception {
        return find(entity, id, null);
    }

    @Override
    public <T extends IEntity> T find(EntitySQL<T> entity, Serializable id, IShardingable shardingable) throws Exception {
        EntityMeta entityMeta = EntityMeta.load(entity.entityClass());
        PairObject<Fields, Params> entityPrimaryKeyValues = doGetPrimaryKeyFieldAndValues(entityMeta, id, null);
        String sqlStr = dialect.buildSelectByPkSql(entity.entityClass(), tablePrefix, entity.shardingable() != null ? entity.shardingable() : shardingable, entityPrimaryKeyValues.getKey(), doGetNotExcludedFields(entityMeta, entity.fields(), false, true));
        Params params;
        if (entityMeta.isMultiplePrimaryKey()) {
            params = entityPrimaryKeyValues.getValue();
        } else {
            params = Params.create(id);
        }
        //
        IQueryOperator<T> queryOperator = doOperator(DatabaseEvent.EVENT.QUERY_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.QUERY)
                .sql(sqlStr)
                .params(params)
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> new DefaultQueryOperator<>(doForUpdateIfNeed(sessionEventContext.getSql(), entity.forUpdate()), this.connectionHolder, new EntityResultSetHandler<>(entity.entityClass())));
        //
        return queryOperator.getResultSet().isEmpty() ? null : queryOperator.getResultSet().get(0);
    }

    @Override
    public <T> T findFirst(SQL sql, IResultSetHandler<T> handler) throws Exception {
        String sqlStr = dialect.buildPagedQuerySql(sql.toString(), 1, 1);
        IQueryOperator<T> queryOperator = doOperator(DatabaseEvent.EVENT.QUERY_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.QUERY)
                .sql(sqlStr)
                .params(sql.params())
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> new DefaultQueryOperator<>(sessionEventContext.getSql(), this.connectionHolder, handler));
        //
        return queryOperator.getResultSet().isEmpty() ? null : queryOperator.getResultSet().get(0);
    }

    @Override
    public <T extends IEntity> T findFirst(EntitySQL<T> entity) throws Exception {
        return findFirst(entity, (Where) null);
    }

    @Override
    public <T extends IEntity> T findFirst(EntitySQL<T> entity, IShardingable shardingable) throws Exception {
        if (entity.shardingable() == null && shardingable != null) {
            entity.shardingable(shardingable);
        }
        return findFirst(entity, (Where) null);
    }

    @Override
    public <T extends IEntity> T findFirst(EntitySQL<T> entity, Where where) throws Exception {
        IResultSet<T> resultSet = find(entity, where, Page.limitOne());
        return !resultSet.isResultsAvailable() ? null : resultSet.getResultData().get(0);
    }

    @Override
    public <T extends IEntity> T findFirst(EntitySQL<T> entity, Where where, IShardingable shardingable) throws Exception {
        if (entity.shardingable() == null && shardingable != null) {
            entity.shardingable(shardingable);
        }
        return findFirst(entity, (Where) null);
    }

    @Override
    public int executeForUpdate(SQL sql) throws Exception {
        IUpdateOperator updateOperator = doOperator(DatabaseEvent.EVENT.UPDATE_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.UPDATE)
                .sql(sql)
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> new DefaultUpdateOperator(sessionEventContext.getSql(), this.getConnectionHolder()));
        //
        return updateOperator.getEffectCounts();
    }

    @Override
    public int[] executeForUpdate(BatchSQL sql) throws Exception {
        IBatchUpdateOperator updateOperator = doOperator(DatabaseEvent.EVENT.UPDATE_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.BATCH_UPDATE)
                .sql(sql)
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> {
            IBatchUpdateOperator operator;
            BatchSQL batchSQL = sessionEventContext.getBatchSQL();
            if (StringUtils.isNotBlank(batchSQL.getSQL())) {
                operator = new BatchUpdateOperator(batchSQL.getSQL(), this.getConnectionHolder());
                batchSQL.params().forEach(param -> {
                    SQLBatchParameter batchParam = SQLBatchParameter.create();
                    param.params().forEach(batchParam::addParameter);
                    operator.addBatchParameter(batchParam);
                });
            } else {
                operator = new BatchUpdateOperator(this.getConnectionHolder());
            }
            batchSQL.getSQLs().forEach(operator::addBatchSQL);
            return operator;
        });
        //
        return updateOperator.getEffectCounts();
    }

    @Override
    public <T extends IEntity> T update(T entity) throws Exception {
        return update(entity, null, entity instanceof IShardingable ? (IShardingable) entity : null);
    }

    @Override
    public <T extends IEntity> T update(T entity, Fields filter) throws Exception {
        return update(entity, filter, entity instanceof IShardingable ? (IShardingable) entity : null);
    }

    @Override
    public <T extends IEntity> T update(T entity, Fields filter, IShardingable shardingable) throws Exception {
        EntityMeta entityMeta = EntityMeta.load(entity.getClass()).unsupportedIfView();
        PairObject<Fields, Params> entityPrimaryKeyValues = doGetPrimaryKeyFieldAndValues(entityMeta, entity, null);
        filter = doGetNotExcludedFields(entityMeta, filter, true, false);
        String sqlStr = dialect.buildUpdateByPkSql(entity.getClass(), tablePrefix, shardingable, entityPrimaryKeyValues.getKey(), filter);
        // 先获取并添加需要更新的字段值
        Params params = doGetEntityFieldAndValues(entityMeta, entity, filter, false, false).getValue().add(entityPrimaryKeyValues.getValue());
        IUpdateOperator updateOperator = doOperator(DatabaseEvent.EVENT.UPDATE_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.UPDATE)
                .sql(sqlStr)
                .params(params)
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> new DefaultUpdateOperator(sessionEventContext.getSql(), this.connectionHolder));
        //
        if (updateOperator.getEffectCounts() > 0) {
            return entity;
        }
        return null;
    }

    @Override
    public <T extends IEntity> List<T> update(List<T> entities, Fields filter) throws Exception {
        if (!entities.isEmpty()) {
            T element = entities.get(0);
            EntityMeta entityMeta = EntityMeta.load(element.getClass()).unsupportedIfView();
            PairObject<Fields, Params> entityPrimaryKeyValues = doGetPrimaryKeyFieldAndValues(entityMeta, element, null);
            filter = doGetNotExcludedFields(entityMeta, filter, true, false);
            String sqlStr = dialect.buildUpdateByPkSql(element.getClass(), tablePrefix, null, entityPrimaryKeyValues.getKey(), filter);
            BatchSQL batchSQL = BatchSQL.create(owner, sqlStr);
            for (T entity : entities) {
                // 先获取并添加需要更新的字段值
                Params params = doGetEntityFieldAndValues(entityMeta, entity, filter, false, false).getValue();
                // 再获取并添加主键条件字段值
                params.add(doGetPrimaryKeyFieldAndValues(entityMeta, entity, null).getValue());
                //
                batchSQL.addParameter(params);
            }
            doOperator(DatabaseEvent.EVENT.UPDATE_AFTER, DatabaseSessionEventContext.builder()
                    .operationType(Type.OPT.BATCH_UPDATE)
                    .sql(batchSQL)
                    .build(this), this::doCreateBatchUpdateOperator);
        }
        return entities;
    }

    /**
     * 创建批处理更新操作器
     *
     * @param sessionEventContext 数据库会话事件上下文
     * @return 批处理更新操作器
     */
    private IBatchUpdateOperator doCreateBatchUpdateOperator(DatabaseSessionEventContext sessionEventContext) {
        IBatchUpdateOperator operator = new BatchUpdateOperator(sessionEventContext.getBatchSQL().getSQL(), this.connectionHolder);
        sessionEventContext.getBatchSQL().params().forEach(param -> {
            SQLBatchParameter batchParameter = SQLBatchParameter.create();
            param.params().forEach(batchParameter::addParameter);
            operator.addBatchParameter(batchParameter);
        });
        return operator;
    }

    @Override
    public <T extends IEntity> List<T> update(ShardingList<T> entities, Fields filter) throws Exception {
        List<T> results = new ArrayList<>();
        for (ShardingElement<T> element : entities) {
            T entity = this.update(element.getElement(), filter, element);
            if (entity != null) {
                results.add(entity);
            }
        }
        return results;
    }

    @Override
    public <T extends IEntity> T insert(T entity) throws Exception {
        return insert(entity, null, (entity instanceof IShardingable ? (IShardingable) entity : null));
    }

    @Override
    public <T extends IEntity> T insert(T entity, IShardingable shardingable) throws Exception {
        return insert(entity, null, shardingable);
    }

    @Override
    public <T extends IEntity> T insert(T entity, Fields filter) throws Exception {
        return insert(entity, filter, (entity instanceof IShardingable ? (IShardingable) entity : null));
    }

    @Override
    public <T extends IEntity> T insert(T entity, Fields filter, IShardingable shardingable) throws Exception {
        EntityMeta entityMeta = EntityMeta.load(entity.getClass()).unsupportedIfView();
        PairObject<Fields, Params> entityFieldAndValues = doGetEntityFieldAndValues(entityMeta, entity, filter, true, false);
        String sqlStr = dialect.buildInsertSql(entity.getClass(), tablePrefix, shardingable, entityFieldAndValues.getKey());
        return doExecuteUpdate(entity, sqlStr, entityFieldAndValues.getValue(), DatabaseEvent.EVENT.INSERT_AFTER, Type.OPT.INSERT, entityMeta);
    }

    @Override
    public <T extends IEntity> List<T> insert(List<T> entities) throws Exception {
        return insert(entities, null);
    }

    @Override
    public <T extends IEntity> List<T> insert(ShardingList<T> entities) throws Exception {
        return this.insert(entities, null);
    }

    @Override
    public <T extends IEntity> List<T> insert(List<T> entities, Fields filter) throws Exception {
        return doBatchUpdate(entities, filter, (entityClass, fields) -> dialect.buildInsertSql(entityClass, tablePrefix, null, fields), DatabaseEvent.EVENT.INSERT_AFTER, Type.OPT.BATCH_INSERT);
    }

    @Override
    public <T extends IEntity> List<T> insert(ShardingList<T> entities, Fields filter) throws Exception {
        List<T> results = new ArrayList<>();
        for (ShardingElement<T> element : entities) {
            T entity = this.insert(element.getElement(), filter, element);
            if (entity != null) {
                results.add(entity);
            }
        }
        return results;
    }

    @Override
    public <T extends IEntity> T upsert(T entity) throws Exception {
        return upsert(entity, null, entity instanceof IShardingable ? (IShardingable) entity : null);
    }

    @Override
    public <T extends IEntity> T upsert(T entity, Fields filter) throws Exception {
        return upsert(entity, filter, entity instanceof IShardingable ? (IShardingable) entity : null);
    }

    @Override
    public <T extends IEntity> T upsert(T entity, Fields filter, IShardingable shardingable) throws Exception {
        EntityMeta entityMeta = EntityMeta.load(entity.getClass()).unsupportedIfView();
        // 获取实体字段和值（用于INSERT部分）
        PairObject<Fields, Params> entityFieldAndValues = doGetEntityFieldAndValues(entityMeta, entity, filter, true, true);
        String sqlStr = dialect.buildUpsertSql(entity.getClass(), tablePrefix, shardingable, entityFieldAndValues.getKey());
        return doExecuteUpdate(entity, sqlStr, entityFieldAndValues.getValue(), DatabaseEvent.EVENT.UPSERT_AFTER, Type.OPT.UPSERT, entityMeta);
    }

    @Override
    public <T extends IEntity> List<T> upsert(List<T> entities) throws Exception {
        return upsert(entities, null);
    }

    @Override
    public <T extends IEntity> List<T> upsert(ShardingList<T> entities) throws Exception {
        return upsert(entities, null);
    }

    @Override
    public <T extends IEntity> List<T> upsert(List<T> entities, Fields filter) throws Exception {
        return doBatchUpdate(entities, filter, (entityClass, fields) -> dialect.buildUpsertSql(entityClass, tablePrefix, null, fields), DatabaseEvent.EVENT.UPSERT_AFTER, Type.OPT.BATCH_UPSERT);
    }

    @Override
    public <T extends IEntity> List<T> upsert(ShardingList<T> entities, Fields filter) throws Exception {
        List<T> results = new ArrayList<>();
        for (ShardingElement<T> element : entities) {
            T entity = this.upsert(element.getElement(), filter, element);
            if (entity != null) {
                results.add(entity);
            }
        }
        return results;
    }

    @Override
    public <T extends IEntity> T insertIfNotExist(T entity) throws Exception {
        return insertIfNotExist(entity, null, entity instanceof IShardingable ? (IShardingable) entity : null);
    }

    @Override
    public <T extends IEntity> T insertIfNotExist(T entity, Fields filter) throws Exception {
        return insertIfNotExist(entity, filter, entity instanceof IShardingable ? (IShardingable) entity : null);
    }

    @Override
    public <T extends IEntity> T insertIfNotExist(T entity, Fields filter, IShardingable shardingable) throws Exception {
        EntityMeta entityMeta = EntityMeta.load(entity.getClass()).unsupportedIfView();
        PairObject<Fields, Params> entityFieldAndValues = doGetEntityFieldAndValues(entityMeta, entity, filter, true, true);
        String sqlStr = dialect.buildInsertIfNotExistSql(entity.getClass(), tablePrefix, shardingable, entityFieldAndValues.getKey());
        return doExecuteUpdate(entity, sqlStr, entityFieldAndValues.getValue(), DatabaseEvent.EVENT.INSERT_IF_NOT_EXIST_AFTER, Type.OPT.INSERT_IF_NOT_EXIST, entityMeta);
    }

    @Override
    public <T extends IEntity> List<T> insertIfNotExist(List<T> entities) throws Exception {
        return insertIfNotExist(entities, null);
    }

    @Override
    public <T extends IEntity> List<T> insertIfNotExist(ShardingList<T> entities) throws Exception {
        return insertIfNotExist(entities, null);
    }

    @Override
    public <T extends IEntity> List<T> insertIfNotExist(List<T> entities, Fields filter) throws Exception {
        return doBatchUpdate(entities, filter, (entityClass, fields) -> dialect.buildInsertIfNotExistSql(entityClass, tablePrefix, null, fields), DatabaseEvent.EVENT.INSERT_IF_NOT_EXIST_AFTER, Type.OPT.BATCH_INSERT_IF_NOT_EXIST);
    }

    @Override
    public <T extends IEntity> List<T> insertIfNotExist(ShardingList<T> entities, Fields filter) throws Exception {
        List<T> results = new ArrayList<>();
        for (ShardingElement<T> element : entities) {
            T entity = this.insertIfNotExist(element.getElement(), filter, element);
            if (entity != null) {
                results.add(entity);
            }
        }
        return results;
    }

    @Override
    public <T extends IEntity> T delete(T entity) throws Exception {
        return delete(entity, (entity instanceof IShardingable ? (IShardingable) entity : null));
    }

    @Override
    public <T extends IEntity> T delete(T entity, IShardingable shardingable) throws Exception {
        if (this.delete(entity.getClass(), entity.getId(), shardingable) > 0) {
            return entity;
        }
        return null;
    }

    @Override
    public <T extends IEntity> int delete(Class<T> entityClass, Serializable id) throws Exception {
        return delete(entityClass, id, null);
    }

    @Override
    public <T extends IEntity> int delete(Class<T> entityClass, Serializable id, IShardingable shardingable) throws Exception {
        EntityMeta entityMeta = EntityMeta.load(entityClass).unsupportedIfView();
        PairObject<Fields, Params> entityPrimaryKeyValues = doGetPrimaryKeyFieldAndValues(entityMeta, id, null);
        String sqlStr = dialect.buildDeleteByPkSql(entityClass, tablePrefix, shardingable, entityPrimaryKeyValues.getKey());
        IUpdateOperator updateOperator = doOperator(DatabaseEvent.EVENT.REMOVE_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.DELETE)
                .sql(sqlStr)
                .params(entityPrimaryKeyValues.getValue())
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> new DefaultUpdateOperator(sessionEventContext.getSql(), this.connectionHolder));
        //
        return updateOperator.getEffectCounts();
    }

    @Override
    public <T extends IEntity> List<T> delete(List<T> entities) throws Exception {
        EntityMeta entityMeta = EntityMeta.load(entities.get(0).getClass()).unsupportedIfView();
        PairObject<Fields, Params> entityPrimaryKeyValues = doGetPrimaryKeyFieldAndValues(entityMeta, entities.get(0), null);
        String sqlStr = dialect.buildDeleteByPkSql(entities.get(0).getClass(), tablePrefix, null, entityPrimaryKeyValues.getKey());
        BatchSQL batchSQL = BatchSQL.create(owner, sqlStr);
        for (T entity : entities) {
            batchSQL.addParameter(doGetPrimaryKeyFieldAndValues(entityMeta, entity, null).getValue());
        }
        doOperator(DatabaseEvent.EVENT.REMOVE_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.BATCH_DELETE)
                .sql(batchSQL)
                .build(this), this::doCreateBatchUpdateOperator);
        //
        return entities;
    }

    @Override
    public <T extends IEntity> List<T> delete(ShardingList<T> entities) throws Exception {
        List<T> results = new ArrayList<>();
        for (ShardingElement<T> element : entities) {
            T entity = this.delete(element.getElement(), element);
            if (entity != null) {
                results.add(entity);
            }
        }
        return results;
    }

    @Override
    public <T extends IEntity> int[] delete(Class<T> entityClass, Serializable[] ids) throws Exception {
        EntityMeta entityMeta = EntityMeta.load(entityClass).unsupportedIfView();
        PairObject<Fields, Params> entityPrimaryKeyValues = doGetPrimaryKeyFieldAndValues(entityMeta, ids[0], null);
        String sqlStr = dialect.buildDeleteByPkSql(entityClass, tablePrefix, null, entityPrimaryKeyValues.getKey());
        BatchSQL batchSQL = BatchSQL.create(owner, sqlStr);
        for (Serializable id : ids) {
            batchSQL.addParameter(doGetPrimaryKeyFieldAndValues(entityMeta, id, null).getValue());
        }
        IBatchUpdateOperator updateOperator = doOperator(DatabaseEvent.EVENT.REMOVE_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.BATCH_DELETE)
                .sql(batchSQL)
                .build(this), this::doCreateBatchUpdateOperator);
        //
        return updateOperator.getEffectCounts();
    }

    @Override
    public <T extends IEntity> int[] delete(Class<T> entityClass, ShardingList<Serializable> ids) throws Exception {
        List<Integer> results = new ArrayList<>();
        for (ShardingElement<Serializable> element : ids) {
            results.add(this.delete(entityClass, element.getElement(), element));
        }
        return ArrayUtils.toPrimitive(results.toArray(new Integer[0]));
    }

    @Override
    public <T extends IEntity> long count(Class<T> entityClass, Where where) throws Exception {
        return count(entityClass, where, null);
    }

    @Override
    public <T extends IEntity> long count(Class<T> entityClass) throws Exception {
        return count(entityClass, null, null);
    }

    @Override
    public <T extends IEntity> long count(Class<T> entityClass, Where where, IShardingable shardingable) throws Exception {
        EntityMeta entityMeta = EntityMeta.load(entityClass);
        ExpressionUtils exp = ExpressionUtils.bind("SELECT count(*) FROM ${table_name} ${where}")
                .set("table_name", dialect.buildTableName(tablePrefix, entityMeta, shardingable))
                .set("where", where == null ? StringUtils.EMPTY : where.toSQL());
        IQueryOperator<Object[]> queryOperator = doOperator(DatabaseEvent.EVENT.QUERY_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.QUERY)
                .sql(exp.getResult())
                .params(where != null ? where.params() : null)
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> new DefaultQueryOperator<>(sessionEventContext.getSql(), this.getConnectionHolder(), new ArrayResultSetHandler()));
        //
        return BlurObject.bind(((Object[]) queryOperator.getResultSet().get(0)[0])[1]).toLongValue();
    }

    @Override
    public long count(SQL sql) throws Exception {
        String sqlStr = dialect.buildCountSQL(sql.toString());
        IQueryOperator<Object[]> queryOperator = doOperator(DatabaseEvent.EVENT.QUERY_AFTER, DatabaseSessionEventContext.builder()
                .operationType(Type.OPT.QUERY)
                .sql(sqlStr)
                .params(sql.params())
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> new DefaultQueryOperator<>(sessionEventContext.getSql(), this.getConnectionHolder(), new ArrayResultSetHandler()));
        //
        return BlurObject.bind(((Object[]) queryOperator.getResultSet().get(0)[0])[1]).toLongValue();
    }

    /**
     * @param entityMeta 实体元描述对象
     * @param targetObj  目标实体对象
     * @param filter     字段名称过滤集合
     * @return 获取主键对象的所有字段和值
     * @throws Exception 可能产生的异常
     */
    private PairObject<Fields, Params> doGetPrimaryKeyFieldAndValues(EntityMeta entityMeta, Object targetObj, Fields filter) throws Exception {
        Fields fields = Fields.create();
        Params values = Params.create();
        if (targetObj instanceof IEntityPK) {
            if (entityMeta.isMultiplePrimaryKey()) {
                for (String pkFieldName : entityMeta.getPrimaryKeys()) {
                    Object value = entityMeta.getPropertyByName(pkFieldName).getField().get(targetObj);
                    if (value != null) {
                        if (doCheckField(filter, pkFieldName)) {
                            fields.add(pkFieldName);
                            values.add(value);
                        }
                    }
                }
            } else {
                String fieldName = entityMeta.getPrimaryKeys().get(0);
                if (doCheckField(filter, fieldName)) {
                    fields.add(fieldName);
                    values.add(targetObj);
                }
            }
        } else if (targetObj instanceof IEntity) {
            if (entityMeta.isMultiplePrimaryKey()) {
                PairObject<Fields, Params> fieldAndValues = doGetPrimaryKeyFieldAndValues(entityMeta, ((IEntity) targetObj).getId(), filter);
                fields.add(fieldAndValues.getKey());
                values.add(fieldAndValues.getValue());
            } else {
                String fieldName = entityMeta.getPrimaryKeys().get(0);
                if (doCheckField(filter, fieldName)) {
                    fields.add(fieldName);
                    values.add(((IEntity) targetObj).getId());
                }
            }
        } else {
            String fieldName = entityMeta.getPrimaryKeys().get(0);
            if (doCheckField(filter, fieldName)) {
                fields.add(fieldName);
                values.add(targetObj);
            }
        }
        return new PairObject<>(fields, values);
    }

    /**
     * @param entityMeta        实体元描述对象
     * @param targetObj         目标实体对象
     * @param filter            进滤的字段名称集合
     * @param includePrimaryKey 是否提取主键对象的值
     * @return 获取实体的所有字段和值
     * @throws Exception 可能产生的异常
     */
    private PairObject<Fields, Params> doGetEntityFieldAndValues(EntityMeta entityMeta, IEntity targetObj, Fields filter, boolean includePrimaryKey, boolean forUpsertOrNotExist) throws Exception {
        Fields fields = Fields.create();
        Params values = Params.create();
        for (String fieldName : entityMeta.getPropertyNames()) {
            PropertyMeta propertyMeta = entityMeta.getPropertyByName(fieldName);
            Object value = null;
            boolean isPrimaryKey = entityMeta.isPrimaryKey(fieldName);
            boolean isAutoincrementField = entityMeta.isAutoincrement(fieldName);
            // 判断是否需要处理该字段
            boolean shouldProcessField = true;
            // 检查是否被过滤
            if (!doCheckField(filter, fieldName)) {
                // 当forUpsertOrNotExist=true时，不管是否被过滤都必须获取主键属性值
                if (!isPrimaryKey || !forUpsertOrNotExist) {
                    // 当includePrimaryKey=true且是主键且非自增时，即使被过滤也要获取属性值
                    if (!(includePrimaryKey && isPrimaryKey && !isAutoincrementField)) {
                        shouldProcessField = false;
                    }
                }
            }
            if (shouldProcessField) {
                if (isPrimaryKey) {
                    if (includePrimaryKey || forUpsertOrNotExist) {
                        if (isAutoincrementField) {
                            if (StringUtils.isNotBlank(propertyMeta.getSequenceName()) || forUpsertOrNotExist) {
                                // 尝试调用序列, 若当前数据库不支持序列将会抛出异常以示警告
                                if (StringUtils.isNotBlank(propertyMeta.getSequenceName())) {
                                    dialect.getSequenceNextValSql(propertyMeta.getSequenceName());
                                }
                                if (entityMeta.isMultiplePrimaryKey()) {
                                    value = propertyMeta.getField().get(targetObj.getId());
                                } else {
                                    value = targetObj.getId();
                                }
                            }
                        } else {
                            if (entityMeta.isMultiplePrimaryKey()) {
                                value = propertyMeta.getField().get(targetObj.getId());
                            } else {
                                value = targetObj.getId();
                            }
                        }
                    }
                } else {
                    value = propertyMeta.getField().get(targetObj);
                }
                // 尝试为非自增长字段执行键值生成器
                if (!isAutoincrementField && value == null && StringUtils.isNotBlank(propertyMeta.getUseKeyGenerator())) {
                    IKeyGenerator keyGenerator = IKeyGenerator.Manager.getKeyGenerator(propertyMeta.getUseKeyGenerator());
                    if (keyGenerator != null) {
                        value = keyGenerator.generate(owner, propertyMeta, targetObj);
                    } else if (LOG.isWarnEnabled()) {
                        LOG.warn(String.format("KeyGenerator named '%s' was not found.", propertyMeta.getUseKeyGenerator()));
                    }
                }
                // 以下操作是为了使@Default起效果的同时也保证数据库中的字段默认值不被null值替代
                if (!isPrimaryKey && value == null && !propertyMeta.isDefaultValueIgnored() && StringUtils.isNotBlank(propertyMeta.getDefaultValue())) {
                    // 如果value为空则尝试提取默认值
                    value = BlurObject.bind(propertyMeta.getDefaultValue()).toObjectValue(propertyMeta.getField().getType());
                }
                if (value != null || propertyMeta.isNullable()) {
                    if (includePrimaryKey && isPrimaryKey && isAutoincrementField && !forUpsertOrNotExist) {
                        continue;
                    }
                    fields.add(fieldName);
                    // 若字段成员声明了@Conversion注解则执行类型转换
                    if (value != null && propertyMeta.getConversionType() != null) {
                        values.add(BlurObject.bind(value).toObjectValue(propertyMeta.getConversionType()));
                    } else {
                        values.add(value);
                    }
                } else if (!isAutoincrementField && !propertyMeta.isNullable() && !Table.isSpecialDefaultValue(propertyMeta.getDefaultValue())) {
                    throw new IllegalArgumentException(String.format("Entity field '%s.%s' value can not be null.", entityMeta.getEntityName(), propertyMeta.getName()));
                }
            }
        }
        return new PairObject<>(fields, values);
    }

    /**
     * @param filter    字段过滤对象
     * @param fieldName 数据表字段名称
     * @return 返回字段是否被过滤
     */
    private boolean doCheckField(Fields filter, String fieldName) {
        if (filter != null && !filter.fields().isEmpty()) {
            if (filter.isExcluded()) {
                return !filter.fields().contains(fieldName);
            } else {
                return filter.fields().contains(fieldName);
            }
        }
        return true;
    }

    /**
     * @param entityMeta        目标数据实体属性描述对象
     * @param filter            字段过滤对象
     * @param forUpdate         若是更新操作则需要过滤掉声明了@Readonly的字段
     * @param includePrimaryKey 是否包含主键
     * @return 返回目标实体中所有未被过滤的字段名称集合
     */
    private Fields doGetNotExcludedFields(EntityMeta entityMeta, Fields filter, boolean forUpdate, boolean includePrimaryKey) {
        Fields returnValue = Fields.create();
        entityMeta.getPropertyNames().stream().filter((field) -> (doCheckField(filter, field))).filter((field) -> !(!includePrimaryKey && entityMeta.isPrimaryKey(field))).filter((field) -> !(forUpdate && entityMeta.isReadonly(field))).forEachOrdered(returnValue::add);
        return returnValue;
    }

    /**
     * 执行批量更新操作（插入或Upsert）
     *
     * @param <T>        实体类型
     * @param entities   实体列表
     * @param filter     字段过滤
     * @param sqlBuilder SQL构建函数
     * @param event      数据库事件类型
     * @param opt        操作类型
     * @return 操作后的实体列表
     * @throws Exception 可能产生的异常
     */
    @SuppressWarnings("unchecked")
    private <T extends IEntity> List<T> doBatchUpdate(List<T> entities, Fields filter, BatchSqlBuilder sqlBuilder, DatabaseEvent.EVENT event, Type.OPT opt) throws Exception {
        T element = entities.get(0);
        EntityMeta entityMeta = EntityMeta.load(element.getClass()).unsupportedIfView();
        boolean forUpsertOrNotExist = opt == Type.OPT.BATCH_UPSERT || opt == Type.OPT.BATCH_INSERT_IF_NOT_EXIST;
        PairObject<Fields, Params> entityFieldAndValues = doGetEntityFieldAndValues(entityMeta, element, filter, true, forUpsertOrNotExist);
        String sqlStr = sqlBuilder.buildSql(element.getClass(), entityFieldAndValues.getKey());
        BatchSQL batchSQL = BatchSQL.create(sqlStr);
        for (T entity : entities) {
            batchSQL.addParameter(doGetEntityFieldAndValues(entityMeta, entity, filter, true, forUpsertOrNotExist).getValue());
        }
        IBatchUpdateOperator updateOperator = doOperator(event, DatabaseSessionEventContext.builder()
                .operationType(opt)
                .sql(batchSQL)
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> {
            IBatchUpdateOperator operator = new BatchUpdateOperator(sessionEventContext.getBatchSQL().getSQL(), this.connectionHolder);
            sessionEventContext.getBatchSQL().params().forEach(params -> {
                SQLBatchParameter batchParameter = SQLBatchParameter.create();
                params.params().forEach(batchParameter::addParameter);
                operator.addBatchParameter(batchParameter);
            });
            // 只对插入操作设置自增主键访问器，因为只有插入操作需要获取自增主键
            if (entityMeta.hasAutoincrement() && (opt == Type.OPT.BATCH_INSERT || opt == Type.OPT.BATCH_UPSERT || opt == Type.OPT.INSERT_IF_NOT_EXIST)) {
                // 配置自增主键访问器
                doConfigureAutoincrement(operator, entityMeta, (List<IEntity<?>>) entities);
            }
            return operator;
        });
        // 对于 insertIfNotExist 操作，只返回实际插入的实体
        if (opt == Type.OPT.BATCH_INSERT_IF_NOT_EXIST) {
            int[] effectCounts = updateOperator.getEffectCounts();
            List<T> result = new ArrayList<>();
            for (int i = 0; i < entities.size() && i < effectCounts.length; i++) {
                if (effectCounts[i] > 0) {
                    result.add(entities.get(i));
                }
            }
            return result;
        }
        return entities;
    }

    /**
     * 批量SQL构建器接口
     */
    private interface BatchSqlBuilder {
        String buildSql(Class<? extends IEntity> entityClass, Fields fields) throws Exception;
    }

    /**
     * 操作器构建器接口
     *
     * @param <T> 构建器类型
     */
    private interface IOperatorBuilder<T extends IOperator> {
        T build(DatabaseSessionEventContext sessionEventContext);
    }

    /**
     * 为单条更新操作配置自增主键访问器
     *
     * @param operator   更新操作器
     * @param entityMeta 实体元描述对象
     * @param entities   实体对象列表
     */
    private void doConfigureAutoincrement(IOperator operator, EntityMeta entityMeta, List<IEntity<?>> entities) {
        if (entityMeta.hasAutoincrement()) {
            // 兼容Oracle无法直接获取生成的主键问题
            if (connectionHolder.getDialect() instanceof OracleDialect) {
                final String[] ids = entityMeta.getAutoincrementKeys().toArray(new String[0]);
                operator.setAccessorConfig(new EntityAccessorConfig(entityMeta, connectionHolder, entities) {
                    @Override
                    public PreparedStatement getPreparedStatement(Connection conn, String sql) throws SQLException {
                        if (conn != null && !conn.isClosed()) {
                            return conn.prepareStatement(sql, ids);
                        }
                        return accessorConnHolder.getConnection().prepareStatement(sql, ids);
                    }
                });
            } else {
                operator.setAccessorConfig(new EntityAccessorConfig(entityMeta, connectionHolder, entities));
            }
        }
    }

    /**
     * 执行单条更新操作
     *
     * @param <T>        实体类型
     * @param entity     实体对象
     * @param sqlStr     SQL语句
     * @param params     参数对象
     * @param event      数据库事件类型
     * @param opt        操作类型
     * @param entityMeta 实体元描述对象
     * @return 若影响记录数大于0返回实体对象，否则返回null
     * @throws Exception 可能产生的异常
     */
    private <T extends IEntity> T doExecuteUpdate(T entity, String sqlStr, Params params, DatabaseEvent.EVENT event, Type.OPT opt, EntityMeta entityMeta) throws Exception {
        IUpdateOperator updateOperator = doOperator(event, DatabaseSessionEventContext.builder()
                .operationType(opt)
                .sql(sqlStr)
                .params(params)
                .build(this), (DatabaseSessionEventContext sessionEventContext) -> {
            IUpdateOperator operator = new DefaultUpdateOperator(sessionEventContext.getSql(), this.connectionHolder);
            // 只对插入操作设置自增主键访问器，因为只有插入操作需要获取自增主键
            if (entityMeta.hasAutoincrement() && (opt == Type.OPT.INSERT || opt == Type.OPT.UPSERT || opt == Type.OPT.INSERT_IF_NOT_EXIST)) {
                doConfigureAutoincrement(operator, entityMeta, Arrays.asList(entity));
            }
            return operator;
        });
        if (updateOperator.getEffectCounts() > 0) {
            return entity;
        }
        return null;
    }

    /**
     * 访问器配置接口私有实现，只为DefaultSession提供扩展服务
     */
    private class EntityAccessorConfig implements IAccessorConfig {

        EntityMeta accessorEntityMeta;
        final IDatabaseConnectionHolder accessorConnHolder;
        List<IEntity<?>> accessorEntities;

        EntityAccessorConfig(EntityMeta entityMeta, IDatabaseConnectionHolder connectionHolder, List<IEntity<?>> entities) {
            accessorEntityMeta = entityMeta;
            this.accessorConnHolder = connectionHolder;
            accessorEntities = entities;
        }

        @Override
        public Statement getStatement(Connection conn) throws Exception {
            if (conn != null && !conn.isClosed()) {
                return conn.createStatement();
            }
            return accessorConnHolder.getConnection().createStatement();
        }

        @Override
        public CallableStatement getCallableStatement(Connection conn, String sql) throws Exception {
            if (conn != null && !conn.isClosed()) {
                return conn.prepareCall(sql);
            }
            return accessorConnHolder.getConnection().prepareCall(sql);
        }

        @Override
        public PreparedStatement getPreparedStatement(Connection conn, String sql) throws Exception {
            if (conn != null && !conn.isClosed()) {
                return conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }
            return accessorConnHolder.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        }

        @Override
        public void beforeStatementExecution(AccessorEventContext context) throws Exception {
        }

        @Override
        public void afterStatementExecution(AccessorEventContext context) throws Exception {
            if (accessorEntities != null && accessorEntityMeta.hasAutoincrement()) {
                // 注: 数据表最多一个自动生成主键
                // 获取返回的自动生成主键集合
                List<Map<String, Object>> keysList = dialect.getGeneratedKeys(context.getStatement(), accessorEntityMeta.getAutoincrementKeys());
                if (!keysList.isEmpty()) {
                    for (int i = 0; i < accessorEntities.size() && i < keysList.size(); i++) {
                        IEntity<?> entity = accessorEntities.get(i);
                        Map<String, Object> keyValues = keysList.get(i);
                        for (Map.Entry<String, Object> autoField : keyValues.entrySet()) {
                            PropertyMeta propertyMeta = accessorEntityMeta.getPropertyByName(autoField.getKey());
                            if (propertyMeta != null) {
                                Field field = propertyMeta.getField();
                                // 为自生成主键赋值, 自动填充
                                if (autoField.getValue() != null) {
                                    if (accessorEntityMeta.isMultiplePrimaryKey()) {
                                        field.set(entity.getId(), BlurObject.bind(autoField.getValue()).toObjectValue(field.getType()));
                                    } else {
                                        field.set(entity, BlurObject.bind(autoField.getValue()).toObjectValue(field.getType()));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public int getFetchDirection() {
            return 0;
        }

        @Override
        public int getFetchSize() {
            return 10000;
        }

        @Override
        public int getMaxFieldSize() {
            return 0;
        }

        @Override
        public int getMaxRows() {
            return 1000;
        }

        @Override
        public int getQueryTimeout() {
            return 0;
        }
    }
}
