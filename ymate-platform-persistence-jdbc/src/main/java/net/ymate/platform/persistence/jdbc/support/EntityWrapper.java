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

import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IResultSet;
import net.ymate.platform.core.persistence.IShardingable;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.*;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseSession;
import net.ymate.platform.persistence.jdbc.query.*;
import org.apache.commons.lang3.StringUtils;

/**
 * 主要作用是为没有继承BaseEntity的实体类提供基本数据库操作方法
 *
 * @author 刘镇 (suninformation@163.com) on 2019-11-13 09:19
 * @since 2.1.0
 */
public final class EntityWrapper<Entity extends IEntity> {

    private IDatabase owner;

    private final Entity entity;

    private final Class<Entity> entityClass;

    private IDatabaseConnectionHolder connectionHolder;

    private IShardingable shardingable;

    private String dataSourceName;

    public static <Entity extends IEntity> EntityWrapper<Entity> bind(Entity entity) {
        return new EntityWrapper<>(entity);
    }

    public static <Entity extends IEntity> EntityWrapper<Entity> bind(IDatabase owner, Entity entity) {
        return new EntityWrapper<>(owner, entity);
    }

    @SuppressWarnings("unchecked")
    private EntityWrapper(Entity entity) {
        this.entity = entity;
        entityClass = (Class<Entity>) ClassUtils.getParameterizedTypes(getClass()).get(0);
    }

    private EntityWrapper(IDatabase owner, Entity entity) {
        this(entity);
        this.owner = owner;
    }

    public IDatabaseConnectionHolder getConnectionHolder() {
        return connectionHolder;
    }

    public void setConnectionHolder(IDatabaseConnectionHolder connectionHolder) {
        this.connectionHolder = connectionHolder;
        // 每次设置将记录数据源名称，若设置为空将重置
        if (this.connectionHolder != null) {
            this.dataSourceName = this.connectionHolder.getDataSourceConfig().getName();
        } else {
            this.dataSourceName = null;
        }
    }

    public IShardingable getShardingable() {
        return this.shardingable;
    }

    public void setShardingable(IShardingable shardingable) {
        this.shardingable = shardingable;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = StringUtils.trimToNull(dataSourceName);
    }

    protected Class<Entity> getEntityClass() {
        return entityClass;
    }

    private IDatabase doGetSafeOwner() {
        if (owner == null) {
            return JDBC.get();
        }
        return owner;
    }

    private IDatabaseConnectionHolder doGetSafeConnectionHolder() throws Exception {
        return BaseEntity.getSafeConnectionHolder(doGetSafeOwner(), connectionHolder, dataSourceName);
    }

    public void entityCreate() throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            session.executeForUpdate(SQL.create(session.getConnectionHolder().getDialect().buildCreateSql(this.entityClass, session.getConnectionHolder().getDataSourceConfig().getTablePrefix(), this.getShardingable())));
        }
    }

    public void entityDrop() throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            session.executeForUpdate(SQL.create(session.getConnectionHolder().getDialect().buildDropSql(this.entityClass, session.getConnectionHolder().getDataSourceConfig().getTablePrefix(), this.getShardingable())));
        }
    }

    public Entity load() throws Exception {
        return load(null, null);
    }

    public Entity load(Fields fields) throws Exception {
        return load(fields, null);
    }

    public Entity load(IDBLocker dbLocker) throws Exception {
        return load(null, dbLocker);
    }

    public Entity load(Fields fields, IDBLocker dbLocker) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            EntitySQL<Entity> entitySql = EntitySQL.create(this.getEntityClass());
            if (fields != null) {
                entitySql.field(fields);
            }
            if (dbLocker != null) {
                entitySql.forUpdate(dbLocker);
            }
            return session.find(entitySql, entity.getId(), this.getShardingable());
        }
    }

    public Entity save() throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            return session.insert(entity, this.getShardingable());
        }
    }

    public Entity save(Fields fields) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            return session.insert(entity, fields, this.getShardingable());
        }
    }

    public boolean saveIfNotExist() throws Exception {
        return saveIfNotExist(false);
    }

    public boolean saveIfNotExist(boolean useLocker) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            EntitySQL<Entity> entitySql = EntitySQL.create(this.getEntityClass());
            if (useLocker) {
                entitySql.forUpdate(IDBLocker.DEFAULT);
            }
            Entity result = session.find(entitySql, entity.getId(), this.getShardingable());
            if (result == null) {
                return session.insert(entity, this.getShardingable()) != null;
            }
            return false;
        }
    }

    public Entity saveOrUpdate() throws Exception {
        return saveOrUpdate(null);
    }

    public Entity saveOrUpdate(Fields fields) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            EntitySQL<Entity> entitySql = EntitySQL.create(this.getEntityClass()).forUpdate(IDBLocker.DEFAULT);
            if (fields != null) {
                entitySql.field(fields);
            }
            Entity result = session.find(entitySql, entity.getId(), this.getShardingable());
            if (result == null) {
                return session.insert(entity, this.getShardingable());
            }
            return session.update(entity, fields, this.getShardingable());
        }
    }

    public Entity update() throws Exception {
        return update(null);
    }

    public Entity update(Fields fields) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            return session.update(entity, fields, this.getShardingable());
        }
    }

    public Entity delete() throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            if (null != entity.getId()) {
                if (session.delete(this.getEntityClass(), entity.getId(), this.getShardingable()) > 0) {
                    return entity;
                }
            } else {
                Cond cond = BaseEntity.buildCond(doGetSafeOwner(), entity);
                if (StringUtils.isNotBlank(cond.toString())) {
                    if (session.executeForUpdate(Delete.create(doGetSafeOwner())
                            .shardingable(this.getShardingable())
                            .from(this.getEntityClass())
                            .where(Where.create(cond)).toSQL()) > 0) {
                        return entity;
                    }
                }
            }
            return null;
        }
    }

    public IResultSet<Entity> find() throws Exception {
        return find(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)), null, null, null);
    }

    public IResultSet<Entity> find(IDBLocker dbLocker) throws Exception {
        return find(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)), null, null, dbLocker);
    }

    public IResultSet<Entity> find(Page page) throws Exception {
        return find(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)), null, page, null);
    }

    public IResultSet<Entity> find(Page page, IDBLocker dbLocker) throws Exception {
        return find(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)), null, page, dbLocker);
    }

    public IResultSet<Entity> find(Fields fields) throws Exception {
        return find(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)), fields, null, null);
    }

    public IResultSet<Entity> find(Fields fields, IDBLocker dbLocker) throws Exception {
        return find(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)), fields, null, dbLocker);
    }

    public IResultSet<Entity> find(Fields fields, Page page) throws Exception {
        return find(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)), fields, page, null);
    }

    public IResultSet<Entity> find(Fields fields, Page page, IDBLocker dbLocker) throws Exception {
        return find(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)), fields, page, dbLocker);
    }

    public IResultSet<Entity> find(Where where) throws Exception {
        return find(where, null, null, null);
    }

    public IResultSet<Entity> find(Where where, IDBLocker dbLocker) throws Exception {
        return find(where, null, null, dbLocker);
    }

    public IResultSet<Entity> find(Where where, Fields fields) throws Exception {
        return find(where, fields, null, null);
    }

    public IResultSet<Entity> find(Where where, Fields fields, IDBLocker dbLocker) throws Exception {
        return find(where, fields, null, dbLocker);
    }

    public IResultSet<Entity> find(Where where, Fields fields, Page page) throws Exception {
        return find(where, fields, page, null);
    }

    public IResultSet<Entity> find(Where where, Page page) throws Exception {
        return find(where, null, page, null);
    }

    public IResultSet<Entity> find(Where where, Page page, IDBLocker dbLocker) throws Exception {
        return find(where, null, page, dbLocker);
    }

    public IResultSet<Entity> find(Where where, Fields fields, Page page, IDBLocker dbLocker) throws Exception {
        return BaseEntity.find(doGetSafeOwner(), getConnectionHolder(), getDataSourceName(), getShardingable(), getEntityClass(), where, fields, page, dbLocker);
    }

    public IResultSet<Entity> findAll() throws Exception {
        return find(null, null, null, null);
    }

    public IResultSet<Entity> findAll(Fields fields, Page page) throws Exception {
        return find(null, fields, page, null);
    }

    public IResultSet<Entity> findAll(Fields fields) throws Exception {
        return find(null, fields, null, null);
    }

    public IResultSet<Entity> findAll(Page page) throws Exception {
        return find(null, null, page, null);
    }

    public Entity findFirst() throws Exception {
        return findFirst(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)), null, null);
    }

    public Entity findFirst(IDBLocker dbLocker) throws Exception {
        return findFirst(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)), null, dbLocker);
    }

    public Entity findFirst(Fields fields) throws Exception {
        return findFirst(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)), fields, null);
    }

    public Entity findFirst(Fields fields, IDBLocker dbLocker) throws Exception {
        return findFirst(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)), fields, dbLocker);
    }

    public Entity findFirst(Where where) throws Exception {
        return findFirst(where, null, null);
    }

    public Entity findFirst(Where where, IDBLocker dbLocker) throws Exception {
        return findFirst(where, null, dbLocker);
    }

    public Entity findFirst(Where where, Fields fields) throws Exception {
        return findFirst(where, fields, null);
    }

    public Entity findFirst(Where where, Fields fields, IDBLocker dbLocker) throws Exception {
        return BaseEntity.findFirst(doGetSafeOwner(), getConnectionHolder(), getDataSourceName(), getShardingable(), getEntityClass(), where, fields, dbLocker);
    }

    public long count() throws Exception {
        return count(Where.create(BaseEntity.buildCond(doGetSafeOwner(), entity)));
    }

    public long count(Where where) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            return session.count(this.getEntityClass(), where, this.getShardingable());
        }
    }

    public EntityStateWrapper<Entity> stateWrapper() throws Exception {
        return stateWrapper(true);
    }

    public EntityStateWrapper<Entity> stateWrapper(boolean ignoreNull) throws Exception {
        return EntityStateWrapper.bind(entity, ignoreNull);
    }
}
