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
import net.ymate.platform.persistence.jdbc.base.impl.BatchUpdateOperator;
import net.ymate.platform.persistence.jdbc.base.impl.DefaultQueryOperator;
import net.ymate.platform.persistence.jdbc.base.impl.DefaultUpdateOperator;
import net.ymate.platform.persistence.jdbc.base.impl.EntityResultSetHandler;
import net.ymate.platform.persistence.jdbc.dialect.IDialect;
import net.ymate.platform.persistence.jdbc.dialect.impl.OracleDialect;
import net.ymate.platform.persistence.jdbc.query.BatchSQL;
import net.ymate.platform.persistence.jdbc.query.EntitySQL;
import net.ymate.platform.persistence.jdbc.query.SQL;
import net.ymate.platform.persistence.jdbc.query.Where;
import net.ymate.platform.persistence.jdbc.support.BaseEntity;
import net.ymate.platform.persistence.jdbc.transaction.Transactions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * 默认数据库会话操作接口实现
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-27 下午03:09:46
 */
public class DefaultDatabaseSession extends AbstractSession<IDatabaseConnectionHolder> implements IDatabaseSession {

    private IDatabase owner;

    private IDatabaseConnectionHolder connectionHolder;

    private IDialect dialect;

    private String tablePrefix;

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

    private void doOperator(Type.OPT opt, DatabaseEvent.EVENT event, Params params, IOperator operator) throws Exception {
        if (params != null && !params.params().isEmpty()) {
            params.params().forEach(operator::addParameter);
        }
        SessionEventContext eventContext = new SessionEventContext(operator, opt);
        if (getSessionEventListener() != null) {
            switch (opt) {
                case QUERY:
                    getSessionEventListener().onQueryBefore(eventContext);
                    break;
                case UPDATE:
                case BATCH_UPDATE:
                    getSessionEventListener().onUpdateBefore(eventContext);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + opt);
            }
        }
        operator.execute();
        if (getSessionEventListener() != null) {
            switch (opt) {
                case QUERY:
                    getSessionEventListener().onQueryAfter(eventContext);
                    break;
                case UPDATE:
                case BATCH_UPDATE:
                    getSessionEventListener().onUpdateAfter(eventContext);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + opt);
            }
        }
        //
        owner.getOwner().getEvents().fireEvent(new DatabaseEvent(owner, event).setEventSource(eventContext));
    }

    private String doForUpdateIfNeed(String sqlStr, IDBLocker dbLocker) {
        if (dbLocker != null) {
            return String.format("%s %s", sqlStr, dbLocker.toSQL());
        }
        return sqlStr;
    }

    @Override
    public <T> IResultSet<T> find(SQL sql, IResultSetHandler<T> handler) throws Exception {
        IQueryOperator<T> queryOperator = new DefaultQueryOperator<>(sql.getSQL(), connectionHolder, handler);
        doOperator(Type.OPT.QUERY, DatabaseEvent.EVENT.QUERY_AFTER, sql.params(), queryOperator);
        //
        return new DefaultResultSet<>(queryOperator.getResultSet());
    }

    @Override
    public <T> IResultSet<T> find(SQL sql, IResultSetHandler<T> handler, Page page) throws Exception {
        String sqlStr = sql.getSQL();
        //
        long count = 0;
        if (page != null) {
            sqlStr = dialect.buildPagedQuerySql(sql.getSQL(), page.page(), page.pageSize());
            if (page.isCount()) {
                count = this.count(sql);
                if (count == 0) {
                    return new DefaultResultSet<>(Collections.emptyList(), page.page(), page.pageSize(), count);
                }
            }
        }
        //
        IQueryOperator<T> queryOperator = new DefaultQueryOperator<>(sqlStr, this.connectionHolder, handler);
        doOperator(Type.OPT.QUERY, DatabaseEvent.EVENT.QUERY_AFTER, sql.params(), queryOperator);
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
        String sqlStr = dialect.buildSelectSql(entity.getEntityClass(), tablePrefix, shardingable, doGetNotExcludedFields(EntityMeta.load(entity.getEntityClass()), entity.fields(), false, true));
        if (where != null) {
            sqlStr = sqlStr.concat(StringUtils.SPACE).concat(where.toString());
        }
        long count = 0;
        if (page != null) {
            sqlStr = dialect.buildPagedQuerySql(sqlStr, page.page(), page.pageSize());
            if (page.isCount()) {
                count = this.count(entity.getEntityClass(), where);
                if (count == 0) {
                    return new DefaultResultSet<>(Collections.emptyList(), page.page(), page.pageSize(), count);
                }
            }
        }
        //
        IQueryOperator<T> queryOperator = new DefaultQueryOperator<>(doForUpdateIfNeed(sqlStr, entity.forUpdate()), this.connectionHolder, new EntityResultSetHandler<>(entity.getEntityClass()));
        doOperator(Type.OPT.QUERY, DatabaseEvent.EVENT.QUERY_AFTER, where != null ? where.getParams() : null, queryOperator);
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
        EntityMeta entityMeta = EntityMeta.load(entity.getEntityClass());
        PairObject<Fields, Params> entityPrimaryKeyValues = doGetPrimaryKeyFieldAndValues(entityMeta, id, null);
        String sqlStr = dialect.buildSelectByPkSql(entity.getEntityClass(), tablePrefix, shardingable, entityPrimaryKeyValues.getKey(), doGetNotExcludedFields(entityMeta, entity.fields(), false, true));
        //
        IQueryOperator<T> queryOperator = new DefaultQueryOperator<>(doForUpdateIfNeed(sqlStr, entity.forUpdate()), this.connectionHolder, new EntityResultSetHandler<>(entity.getEntityClass()));
        if (entityMeta.isMultiplePrimaryKey()) {
            entityPrimaryKeyValues.getValue().params().forEach(queryOperator::addParameter);
        } else {
            queryOperator.addParameter(id);
        }
        doOperator(Type.OPT.QUERY, DatabaseEvent.EVENT.QUERY_AFTER, null, queryOperator);
        //
        return queryOperator.getResultSet().isEmpty() ? null : queryOperator.getResultSet().get(0);
    }

    @Override
    public <T> T findFirst(SQL sql, IResultSetHandler<T> handler) throws Exception {
        String sqlStr = dialect.buildPagedQuerySql(sql.getSQL(), 1, 1);
        IQueryOperator<T> queryOperator = new DefaultQueryOperator<>(sqlStr, this.connectionHolder, handler);
        doOperator(Type.OPT.QUERY, DatabaseEvent.EVENT.QUERY_AFTER, sql.params(), queryOperator);
        //
        return queryOperator.getResultSet().isEmpty() ? null : queryOperator.getResultSet().get(0);
    }

    @Override
    public <T extends IEntity> T findFirst(EntitySQL<T> entity) throws Exception {
        return findFirst(entity, null, null);
    }

    @Override
    public <T extends IEntity> T findFirst(EntitySQL<T> entity, IShardingable shardingable) throws Exception {
        return findFirst(entity, null, shardingable);
    }

    @Override
    public <T extends IEntity> T findFirst(EntitySQL<T> entity, Where where) throws Exception {
        return findFirst(entity, where, null);
    }

    @Override
    public <T extends IEntity> T findFirst(EntitySQL<T> entity, Where where, IShardingable shardingable) throws Exception {
        String sqlStr = dialect.buildSelectSql(entity.getEntityClass(), tablePrefix, shardingable, doGetNotExcludedFields(EntityMeta.load(entity.getEntityClass()), entity.fields(), false, true));
        if (where != null) {
            sqlStr = sqlStr.concat(StringUtils.SPACE).concat(where.toString());
        }
        sqlStr = dialect.buildPagedQuerySql(sqlStr, 1, 1);
        //
        IQueryOperator<T> queryOperator = new DefaultQueryOperator<>(doForUpdateIfNeed(sqlStr, entity.forUpdate()), this.connectionHolder, new EntityResultSetHandler<>(entity.getEntityClass()));
        doOperator(Type.OPT.QUERY, DatabaseEvent.EVENT.QUERY_AFTER, where != null ? where.getParams() : null, queryOperator);
        //
        return queryOperator.getResultSet().isEmpty() ? null : queryOperator.getResultSet().get(0);
    }

    @Override
    public int executeForUpdate(SQL sql) throws Exception {
        IUpdateOperator updateOperator = new DefaultUpdateOperator(sql.getSQL(), this.getConnectionHolder());
        doOperator(Type.OPT.UPDATE, DatabaseEvent.EVENT.UPDATE_AFTER, sql.params(), updateOperator);
        //
        return updateOperator.getEffectCounts();
    }

    @Override
    public int[] executeForUpdate(BatchSQL sql) throws Exception {
        IBatchUpdateOperator updateOperator;
        if (sql.getSQL() != null) {
            updateOperator = new BatchUpdateOperator(sql.getSQL(), this.getConnectionHolder());
            sql.params().forEach(param -> {
                SQLBatchParameter batchParam = SQLBatchParameter.create();
                param.params().forEach(batchParam::addParameter);
                updateOperator.addBatchParameter(batchParam);
            });
        } else {
            updateOperator = new BatchUpdateOperator(this.getConnectionHolder());
        }
        sql.getSQLs().forEach(updateOperator::addBatchSQL);
        doOperator(Type.OPT.BATCH_UPDATE, DatabaseEvent.EVENT.UPDATE_AFTER, null, updateOperator);
        //
        return updateOperator.getEffectCounts();
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
        IUpdateOperator updateOperator = new DefaultUpdateOperator(sqlStr, this.connectionHolder);
        // 先获取并添加需要更新的字段值
        doGetEntityFieldAndValues(entityMeta, entity, filter, false).getValue().params().forEach(updateOperator::addParameter);
        doOperator(Type.OPT.UPDATE, DatabaseEvent.EVENT.UPDATE_AFTER, entityPrimaryKeyValues.getValue(), updateOperator);
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
            IBatchUpdateOperator updateOperator = new BatchUpdateOperator(sqlStr, this.connectionHolder);
            for (T entity : entities) {
                SQLBatchParameter batchParameter = SQLBatchParameter.create();
                // 先获取并添加需要更新的字段值
                doGetEntityFieldAndValues(entityMeta, entity, filter, false).getValue().params().forEach(batchParameter::addParameter);
                // 再获取并添加主键条件字段值
                doGetPrimaryKeyFieldAndValues(entityMeta, entity, null).getValue().params().forEach(batchParameter::addParameter);
                updateOperator.addBatchParameter(batchParameter);
            }
            doOperator(Type.OPT.BATCH_UPDATE, DatabaseEvent.EVENT.UPDATE_AFTER, null, updateOperator);
        }
        return entities;
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
        PairObject<Fields, Params> entityFieldAndValues = doGetEntityFieldAndValues(entityMeta, entity, filter, true);
        String sqlStr = dialect.buildInsertSql(entity.getClass(), tablePrefix, shardingable, entityFieldAndValues.getKey());
        IUpdateOperator updateOperator = new DefaultUpdateOperator(sqlStr, this.connectionHolder);
        if (entityMeta.hasAutoincrement()) {
            // 兼容Oracle无法直接获取生成的主键问题
            if (connectionHolder.getDialect() instanceof OracleDialect) {
                final String[] ids = entityMeta.getAutoincrementKeys().toArray(new String[0]);
                updateOperator.setAccessorConfig(new EntityAccessorConfig(entityMeta, connectionHolder, entity) {
                    @Override
                    public PreparedStatement getPreparedStatement(Connection conn, String sql) throws SQLException {
                        if (conn != null && !conn.isClosed()) {
                            return conn.prepareStatement(sql, ids);
                        }
                        return accessorConnHolder.getConnection().prepareStatement(sql, ids);
                    }
                });
            } else {
                updateOperator.setAccessorConfig(new EntityAccessorConfig(entityMeta, connectionHolder, entity));
            }
        }
        doOperator(Type.OPT.UPDATE, DatabaseEvent.EVENT.INSERT_AFTER, entityFieldAndValues.getValue(), updateOperator);
        //
        if (updateOperator.getEffectCounts() > 0) {
            return entity;
        }
        return null;
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
    @SuppressWarnings("unchecked")
    public <T extends IEntity> List<T> insert(List<T> entities, Fields filter) throws Exception {
        T element = entities.get(0);
        EntityMeta entityMeta = EntityMeta.load(element.getClass()).unsupportedIfView();
        PairObject<Fields, Params> entityFieldAndValues = doGetEntityFieldAndValues(entityMeta, element, filter, true);
        String sqlStr = dialect.buildInsertSql(element.getClass(), tablePrefix, null, entityFieldAndValues.getKey());
        IBatchUpdateOperator updateOperator = new BatchUpdateOperator(sqlStr, this.connectionHolder);
        if (entityMeta.hasAutoincrement()) {
            // 兼容Oracle无法直接获取生成的主键问题
            if (connectionHolder.getDialect() instanceof OracleDialect) {
                final String[] ids = entityMeta.getAutoincrementKeys().toArray(new String[0]);
                updateOperator.setAccessorConfig(new EntityAccessorConfig(entityMeta, connectionHolder, (List<IEntity<?>>) entities) {
                    @Override
                    public PreparedStatement getPreparedStatement(Connection conn, String sql) throws SQLException {
                        if (conn != null && !conn.isClosed()) {
                            return conn.prepareStatement(sql, ids);
                        }
                        return accessorConnHolder.getConnection().prepareStatement(sql, ids);
                    }
                });
            } else {
                updateOperator.setAccessorConfig(new EntityAccessorConfig(entityMeta, connectionHolder, (List<IEntity<?>>) entities));
            }
        }
        for (T entity : entities) {
            SQLBatchParameter batchParameter = SQLBatchParameter.create();
            doGetEntityFieldAndValues(entityMeta, entity, filter, true).getValue().params().forEach(batchParameter::addParameter);
            updateOperator.addBatchParameter(batchParameter);
        }
        doOperator(Type.OPT.BATCH_UPDATE, DatabaseEvent.EVENT.INSERT_AFTER, null, updateOperator);
        //
        return entities;
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
        IUpdateOperator updateOperator = new DefaultUpdateOperator(sqlStr, this.connectionHolder);
        doOperator(Type.OPT.UPDATE, DatabaseEvent.EVENT.REMOVE_AFTER, entityPrimaryKeyValues.getValue(), updateOperator);
        //
        return updateOperator.getEffectCounts();
    }

    @Override
    public <T extends IEntity> List<T> delete(List<T> entities) throws Exception {
        EntityMeta entityMeta = EntityMeta.load(entities.get(0).getClass()).unsupportedIfView();
        PairObject<Fields, Params> entityPrimaryKeyValues = doGetPrimaryKeyFieldAndValues(entityMeta, entities.get(0), null);
        String sqlStr = dialect.buildDeleteByPkSql(entities.get(0).getClass(), tablePrefix, null, entityPrimaryKeyValues.getKey());
        IBatchUpdateOperator updateOperator = new BatchUpdateOperator(sqlStr, this.connectionHolder);
        for (T entity : entities) {
            SQLBatchParameter batchParameter = SQLBatchParameter.create();
            doGetPrimaryKeyFieldAndValues(entityMeta, entity, null).getValue().params().forEach(batchParameter::addParameter);
            updateOperator.addBatchParameter(batchParameter);
        }
        doOperator(Type.OPT.BATCH_UPDATE, DatabaseEvent.EVENT.REMOVE_AFTER, null, updateOperator);
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
        IBatchUpdateOperator updateOperator = new BatchUpdateOperator(sqlStr, this.connectionHolder);
        for (Serializable id : ids) {
            SQLBatchParameter batchParameter = SQLBatchParameter.create();
            doGetPrimaryKeyFieldAndValues(entityMeta, id, null).getValue().params().forEach(batchParameter::addParameter);
            updateOperator.addBatchParameter(batchParameter);
        }
        doOperator(Type.OPT.BATCH_UPDATE, DatabaseEvent.EVENT.REMOVE_AFTER, null, updateOperator);
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
        IQueryOperator<Object[]> queryOperator = new DefaultQueryOperator<>(exp.getResult(), this.getConnectionHolder(), IResultSetHandler.ARRAY);
        doOperator(Type.OPT.QUERY, DatabaseEvent.EVENT.QUERY_AFTER, where != null ? where.getParams() : null, queryOperator);
        //
        return BlurObject.bind(((Object[]) queryOperator.getResultSet().get(0)[0])[1]).toLongValue();
    }

    @Override
    public long count(SQL sql) throws Exception {
        String sqlStr = ExpressionUtils.bind("SELECT count(*) FROM (${sql}) c_t")
                .set("sql", sql.getSQL()).getResult();
        IQueryOperator<Object[]> queryOperator = new DefaultQueryOperator<>(sqlStr, this.getConnectionHolder(), IResultSetHandler.ARRAY);
        doOperator(Type.OPT.QUERY, DatabaseEvent.EVENT.QUERY_AFTER, sql.params(), queryOperator);
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
    private PairObject<Fields, Params> doGetEntityFieldAndValues(EntityMeta entityMeta, IEntity targetObj, Fields filter, boolean includePrimaryKey) throws Exception {
        Fields fields = Fields.create();
        Params values = Params.create();
        for (String fieldName : entityMeta.getPropertyNames()) {
            if (doCheckField(filter, fieldName)) {
                PropertyMeta propertyMeta = entityMeta.getPropertyByName(fieldName);
                Object value = null;
                if (entityMeta.isPrimaryKey(fieldName)) {
                    if (includePrimaryKey) {
                        // 自增字段将被忽略, 指定序列的除外
                        if (propertyMeta.isAutoincrement()) {
                            if (StringUtils.isNotBlank(propertyMeta.getSequenceName())) {
                                fields.add(fieldName);
                                // 尝试调用序列, 若当前数据库不支持序列将会抛出异常以示警告
                                dialect.getSequenceNextValSql(propertyMeta.getSequenceName());
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
                // 以下操作是为了使@Default起效果的同时也保证数据库中的字段默认值不被null值替代
                if (value == null) {
                    // 如果value为空则尝试提取默认值
                    value = BlurObject.bind(propertyMeta.getDefaultValue()).toObjectValue(propertyMeta.getField().getType());
                }
                if (value != null || propertyMeta.isNullable()) {
                    if (includePrimaryKey && entityMeta.isPrimaryKey(fieldName) && entityMeta.isAutoincrement(fieldName)) {
                        continue;
                    }
                    // 若value不为空则添加至返回对象中
                    fields.add(fieldName);
                    values.add(value);
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
     * 访问器配置接口私有实现，只为DefaultSession提供扩展服务
     */
    private class EntityAccessorConfig implements IAccessorConfig {

        EntityMeta accessorEntityMeta;
        final IDatabaseConnectionHolder accessorConnHolder;
        List<IEntity<?>> accessorEntities;

        EntityAccessorConfig(EntityMeta entityMeta, IDatabaseConnectionHolder connectionHolder, IEntity<?>... entity) {
            accessorEntityMeta = entityMeta;
            this.accessorConnHolder = connectionHolder;
            accessorEntities = Arrays.asList(entity);
        }

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
                Map<String, Object> keyValues = dialect.getGeneratedKey(context.getStatement(), accessorEntityMeta.getAutoincrementKeys());
                if (!keyValues.isEmpty()) {
                    for (IEntity<?> entity : this.accessorEntities) {
                        for (Map.Entry<String, Object> autoField : keyValues.entrySet()) {
                            Field field = accessorEntityMeta.getPropertyByField(autoField.getKey()).getField();
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
