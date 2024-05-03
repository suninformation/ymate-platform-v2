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

import net.ymate.platform.core.persistence.*;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.base.IResultSetHandler;
import net.ymate.platform.persistence.jdbc.query.BatchSQL;
import net.ymate.platform.persistence.jdbc.query.EntitySQL;
import net.ymate.platform.persistence.jdbc.query.SQL;
import net.ymate.platform.persistence.jdbc.query.Where;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 2024/5/2 00:56
 * @since 2.1.3
 */
@SuppressWarnings("rawtypes")
public class AbstractRepository implements IRepository {

    protected String dataSourceNameSafety(IDatabase owner, String dataSourceName) {
        return StringUtils.isBlank(dataSourceName) ? owner.getConfig().getDefaultDataSourceName() : dataSourceName;
    }

    protected <T> IResultSet<T> find(SQL sql, IResultSetHandler<T> handler) throws Exception {
        return sql.find(handler);
    }

    protected <T> IResultSet<T> find(String dataSourceName, SQL sql, IResultSetHandler<T> handler) throws Exception {
        return sql.find(dataSourceNameSafety(sql.owner(), dataSourceName), handler);
    }

    protected <T> IResultSet<T> find(SQL sql, IResultSetHandler<T> handler, Page page) throws Exception {
        return sql.find(handler, page);
    }

    protected <T> IResultSet<T> find(String dataSourceName, SQL sql, IResultSetHandler<T> handler, Page page) throws Exception {
        return sql.find(dataSourceNameSafety(sql.owner(), dataSourceName), handler, page);
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, T entity) throws Exception {
        return owner.openSession(session -> session.find(entity));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, String dataSourceName, T entity) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.find(entity));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, T entity, IShardingable shardingable) throws Exception {
        return owner.openSession(session -> session.find(entity, shardingable));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, String dataSourceName, T entity, IShardingable shardingable) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.find(entity, shardingable));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, T entity, Page page) throws Exception {
        return owner.openSession(session -> session.find(entity, page));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, String dataSourceName, T entity, Page page) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.find(entity, page));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, T entity, Page page, IShardingable shardingable) throws Exception {
        return owner.openSession(session -> session.find(entity, page, shardingable));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, String dataSourceName, T entity, Page page, IShardingable shardingable) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.find(entity, page, shardingable));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, T entity, Fields filter) throws Exception {
        return owner.openSession(session -> session.find(entity, filter));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, String dataSourceName, T entity, Fields filter) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.find(entity, filter));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, T entity, Fields filter, IShardingable shardingable) throws Exception {
        return owner.openSession(session -> session.find(entity, filter, shardingable));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, String dataSourceName, T entity, Fields filter, IShardingable shardingable) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.find(entity, filter, shardingable));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, T entity, Fields filter, Page page) throws Exception {
        return owner.openSession(session -> session.find(entity, filter, page));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, String dataSourceName, T entity, Fields filter, Page page) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.find(entity, filter, page));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, T entity, Fields filter, Page page, IShardingable shardingable) throws Exception {
        return owner.openSession(session -> session.find(entity, filter, page, shardingable));
    }

    protected <T extends IEntity> IResultSet<T> find(IDatabase owner, String dataSourceName, T entity, Fields filter, Page page, IShardingable shardingable) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.find(entity, filter, page, shardingable));
    }

    protected <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity) throws Exception {
        return entity.find((Where) null);
    }

    protected <T extends IEntity> IResultSet<T> find(String dataSourceName, EntitySQL<T> entity) throws Exception {
        return entity.find(dataSourceNameSafety(entity.owner(), dataSourceName), (Where) null);
    }

    protected <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Page page) throws Exception {
        return entity.find(null, page);
    }

    protected <T extends IEntity> IResultSet<T> find(String dataSourceName, EntitySQL<T> entity, Page page) throws Exception {
        return entity.find(dataSourceNameSafety(entity.owner(), dataSourceName), null, page);
    }

    protected <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Where where) throws Exception {
        return entity.find(where);
    }

    protected <T extends IEntity> IResultSet<T> find(String dataSourceName, EntitySQL<T> entity, Where where) throws Exception {
        return entity.find(dataSourceNameSafety(entity.owner(), dataSourceName), where);
    }

    protected <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Where where, Page page) throws Exception {
        return entity.find(where, page);
    }

    protected <T extends IEntity> IResultSet<T> find(String dataSourceName, EntitySQL<T> entity, Where where, Page page) throws Exception {
        return entity.find(dataSourceNameSafety(entity.owner(), dataSourceName), where, page);
    }

    protected <T extends IEntity> T find(EntitySQL<T> entity, Serializable id) throws Exception {
        return entity.find(id);
    }

    protected <T extends IEntity> T find(String dataSourceName, EntitySQL<T> entity, Serializable id) throws Exception {
        return entity.find(dataSourceNameSafety(entity.owner(), dataSourceName), id);
    }

    protected <T> T findFirst(SQL sql, IResultSetHandler<T> handler) throws Exception {
        return sql.findFirst(handler);
    }

    protected <T> T findFirst(String dataSourceName, SQL sql, IResultSetHandler<T> handler) throws Exception {
        return sql.findFirst(dataSourceNameSafety(sql.owner(), dataSourceName), handler);
    }

    protected <T extends IEntity> T findFirst(EntitySQL<T> entity, Where where) throws Exception {
        return entity.findFirst(where);
    }

    protected <T extends IEntity> T findFirst(String dataSourceName, EntitySQL<T> entity, Where where) throws Exception {
        return entity.findFirst(dataSourceNameSafety(entity.owner(), dataSourceName), where);
    }

    protected <T extends IEntity> T findFirst(EntitySQL<T> entity) throws Exception {
        return entity.findFirst(null);
    }

    protected <T extends IEntity> T findFirst(String dataSourceName, EntitySQL<T> entity) throws Exception {
        return entity.findFirst(dataSourceNameSafety(entity.owner(), dataSourceName), null);
    }

    protected int executeForUpdate(SQL sql) throws Exception {
        return sql.execute();
    }

    protected int executeForUpdate(String dataSourceName, SQL sql) throws Exception {
        return sql.execute(dataSourceNameSafety(sql.owner(), dataSourceName));
    }

    protected int[] executeForUpdate(BatchSQL sql) throws Exception {
        return sql.execute();
    }

    protected int[] executeForUpdate(String dataSourceName, BatchSQL sql) throws Exception {
        return sql.execute(dataSourceNameSafety(sql.owner(), dataSourceName));
    }

    protected <T extends IEntity> T update(IDatabase owner, T entity) throws Exception {
        return owner.openSession(session -> session.update(entity));
    }

    protected <T extends IEntity> T update(IDatabase owner, String dataSourceName, T entity) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.update(entity));
    }

    protected <T extends IEntity> T update(IDatabase owner, T entity, Fields filter) throws Exception {
        return owner.openSession(session -> session.update(entity, filter));
    }

    protected <T extends IEntity> T update(IDatabase owner, String dataSourceName, T entity, Fields filter) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.update(entity, filter));
    }

    protected <T extends IEntity> T update(IDatabase owner, T entity, Fields filter, IShardingable shardingable) throws Exception {
        return owner.openSession(session -> session.update(entity, filter, shardingable));
    }

    protected <T extends IEntity> T update(IDatabase owner, String dataSourceName, T entity, Fields filter, IShardingable shardingable) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.update(entity, filter, shardingable));
    }

    protected <T extends IEntity> List<T> update(IDatabase owner, List<T> entities, Fields filter) throws Exception {
        return owner.openSession(session -> session.update(entities, filter));
    }

    protected <T extends IEntity> List<T> update(IDatabase owner, String dataSourceName, List<T> entities, Fields filter) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.update(entities, filter));
    }

    protected <T extends IEntity> List<T> update(IDatabase owner, ShardingList<T> entities, Fields filter) throws Exception {
        return owner.openSession(session -> session.update(entities, filter));
    }

    protected <T extends IEntity> List<T> update(IDatabase owner, String dataSourceName, ShardingList<T> entities, Fields filter) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.update(entities, filter));
    }

    protected <T extends IEntity> T insert(IDatabase owner, T entity) throws Exception {
        return owner.openSession(session -> session.insert(entity));
    }

    protected <T extends IEntity> T insert(IDatabase owner, String dataSourceName, T entity) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.insert(entity));
    }

    protected <T extends IEntity> T insert(IDatabase owner, T entity, IShardingable shardingable) throws Exception {
        return owner.openSession(session -> session.insert(entity, shardingable));
    }

    protected <T extends IEntity> T insert(IDatabase owner, String dataSourceName, T entity, IShardingable shardingable) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.insert(entity, shardingable));
    }

    protected <T extends IEntity> T insert(IDatabase owner, T entity, Fields filter) throws Exception {
        return owner.openSession(session -> session.insert(entity, filter));
    }

    protected <T extends IEntity> T insert(IDatabase owner, String dataSourceName, T entity, Fields filter) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.insert(entity, filter));
    }

    protected <T extends IEntity> T insert(IDatabase owner, T entity, Fields filter, IShardingable shardingable) throws Exception {
        return owner.openSession(session -> session.insert(entity, filter, shardingable));
    }

    protected <T extends IEntity> T insert(IDatabase owner, String dataSourceName, T entity, Fields filter, IShardingable shardingable) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.insert(entity, filter, shardingable));
    }

    protected <T extends IEntity> List<T> insert(IDatabase owner, List<T> entities) throws Exception {
        return owner.openSession(session -> session.insert(entities));
    }

    protected <T extends IEntity> List<T> insert(IDatabase owner, String dataSourceName, List<T> entities) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.insert(entities));
    }

    protected <T extends IEntity> List<T> insert(IDatabase owner, ShardingList<T> entities) throws Exception {
        return owner.openSession(session -> session.insert(entities));
    }

    protected <T extends IEntity> List<T> insert(IDatabase owner, String dataSourceName, ShardingList<T> entities) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.insert(entities));
    }

    protected <T extends IEntity> List<T> insert(IDatabase owner, List<T> entities, Fields filter) throws Exception {
        return owner.openSession(session -> session.insert(entities, filter));
    }

    protected <T extends IEntity> List<T> insert(IDatabase owner, String dataSourceName, List<T> entities, Fields filter) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.insert(entities, filter));
    }

    protected <T extends IEntity> List<T> insert(IDatabase owner, ShardingList<T> entities, Fields filter) throws Exception {
        return owner.openSession(session -> session.insert(entities, filter));
    }

    protected <T extends IEntity> List<T> insert(IDatabase owner, String dataSourceName, ShardingList<T> entities, Fields filter) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.insert(entities, filter));
    }

    protected <T extends IEntity> T delete(IDatabase owner, T entity) throws Exception {
        return owner.openSession(session -> session.delete(entity));
    }

    protected <T extends IEntity> T delete(IDatabase owner, String dataSourceName, T entity) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.delete(entity));
    }

    protected <T extends IEntity> T delete(IDatabase owner, T entity, IShardingable shardingable) throws Exception {
        return owner.openSession(session -> session.delete(entity, shardingable));
    }

    protected <T extends IEntity> T delete(IDatabase owner, String dataSourceName, T entity, IShardingable shardingable) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.delete(entity, shardingable));
    }

    protected <T extends IEntity> int delete(IDatabase owner, Class<T> entityClass, Serializable id) throws Exception {
        return owner.openSession(session -> session.delete(entityClass, id));
    }

    protected <T extends IEntity> int delete(IDatabase owner, String dataSourceName, Class<T> entityClass, Serializable id) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.delete(entityClass, id));
    }

    protected <T extends IEntity> int delete(IDatabase owner, Class<T> entityClass, Serializable id, IShardingable shardingable) throws Exception {
        return owner.openSession(session -> session.delete(entityClass, id, shardingable));
    }

    protected <T extends IEntity> int delete(IDatabase owner, String dataSourceName, Class<T> entityClass, Serializable id, IShardingable shardingable) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.delete(entityClass, id, shardingable));
    }

    protected <T extends IEntity> List<T> delete(IDatabase owner, List<T> entities) throws Exception {
        return owner.openSession(session -> session.delete(entities));
    }

    protected <T extends IEntity> List<T> delete(IDatabase owner, String dataSourceName, List<T> entities) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.delete(entities));
    }

    protected <T extends IEntity> List<T> delete(IDatabase owner, ShardingList<T> entities) throws Exception {
        return owner.openSession(session -> session.delete(entities));
    }

    protected <T extends IEntity> List<T> delete(IDatabase owner, String dataSourceName, ShardingList<T> entities) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.delete(entities));
    }

    protected <T extends IEntity> int[] delete(IDatabase owner, Class<T> entityClass, Serializable[] ids) throws Exception {
        return owner.openSession(session -> session.delete(entityClass, ids));
    }

    protected <T extends IEntity> int[] delete(IDatabase owner, String dataSourceName, Class<T> entityClass, Serializable[] ids) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.delete(entityClass, ids));
    }

    protected <T extends IEntity> int[] delete(IDatabase owner, Class<T> entityClass, ShardingList<Serializable> ids) throws Exception {
        return owner.openSession(session -> session.delete(entityClass, ids));
    }

    protected <T extends IEntity> int[] delete(IDatabase owner, String dataSourceName, Class<T> entityClass, ShardingList<Serializable> ids) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.delete(entityClass, ids));
    }

    protected <T extends IEntity> long count(IDatabase owner, Class<T> entityClass, Where where) throws Exception {
        return owner.openSession(session -> session.count(entityClass, where));
    }

    protected <T extends IEntity> long count(IDatabase owner, String dataSourceName, Class<T> entityClass, Where where) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.count(entityClass, where));
    }

    protected <T extends IEntity> long count(IDatabase owner, Class<T> entityClass) throws Exception {
        return owner.openSession(session -> session.count(entityClass));
    }

    protected <T extends IEntity> long count(IDatabase owner, String dataSourceName, Class<T> entityClass) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.count(entityClass));
    }

    protected <T extends IEntity> long count(IDatabase owner, Class<T> entityClass, Where where, IShardingable shardingable) throws Exception {
        return owner.openSession(session -> session.count(entityClass, where, shardingable));
    }

    protected <T extends IEntity> long count(IDatabase owner, String dataSourceName, Class<T> entityClass, Where where, IShardingable shardingable) throws Exception {
        return owner.openSession(dataSourceNameSafety(owner, dataSourceName), session -> session.count(entityClass, where, shardingable));
    }

    protected long count(SQL sql) throws Exception {
        return sql.count();
    }

    protected long count(String dataSourceName, SQL sql) throws Exception {
        return sql.count(dataSourceNameSafety(sql.owner(), dataSourceName));
    }
}
