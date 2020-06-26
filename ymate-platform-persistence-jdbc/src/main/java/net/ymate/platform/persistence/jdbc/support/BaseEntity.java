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
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.*;
import net.ymate.platform.persistence.jdbc.base.impl.BeanResultSetHandler;
import net.ymate.platform.persistence.jdbc.impl.DefaultDatabaseSession;
import net.ymate.platform.persistence.jdbc.query.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 实体模型接口抽象实现，提供基本数据库操作方法
 *
 * @param <Entity> 实体类型
 * @param <PK>     主键类型
 * @author 刘镇 (suninformation@163.com) on 2013-7-16 下午5:22:15
 */
public abstract class BaseEntity<Entity extends IEntity, PK extends Serializable> implements IEntity<PK> {

    private transient IDatabase dbOwner;

    private final Class<Entity> entityClass;

    private transient IDatabaseConnectionHolder connectionHolder;

    private transient IShardingable shardingable;

    private String dataSourceName;

    /**
     * 构造器
     */
    @SuppressWarnings("unchecked")
    public BaseEntity() {
        this.entityClass = (Class<Entity>) ClassUtils.getParameterizedTypes(getClass()).get(0);
    }

    /**
     * 构造器
     *
     * @param dbOwner 所属JDBC数据库管理器
     */
    public BaseEntity(IDatabase dbOwner) {
        this();
        this.dbOwner = dbOwner;
    }

    public IDatabaseConnectionHolder getConnectionHolder() {
        return this.connectionHolder;
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
        return this.dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = StringUtils.trimToNull(dataSourceName);
    }

    /**
     * @return 获取实体对象类型
     */
    protected Class<Entity> getEntityClass() {
        return this.entityClass;
    }

    /**
     * 确保能够正确获取到数据库连接持有对象，即连接持有对象为null时尝试获取JDBC默认连接
     *
     * @param owner            数据库管理器
     * @param connectionHolder 当前数据库连接持有对象
     * @param dataSourceName   数据源名称
     * @return 返回数据库连接持有对象
     * @throws Exception 可能产生的异常
     * @since 2.1.0 提取为静态方法
     */
    public static IDatabaseConnectionHolder getSafeConnectionHolder(IDatabase owner, IDatabaseConnectionHolder connectionHolder, String dataSourceName) throws Exception {
        if (connectionHolder == null || connectionHolder.getConnection() == null || connectionHolder.getConnection().isClosed()) {
            if (StringUtils.isNotBlank(dataSourceName)) {
                return owner.getConnectionHolder(dataSourceName);
            } else {
                return owner.getDefaultConnectionHolder();
            }
        }
        return connectionHolder;
    }

    protected IDatabase doGetSafeOwner() {
        if (dbOwner == null) {
            return JDBC.get();
        }
        return dbOwner;
    }

    protected IDatabaseConnectionHolder doGetSafeConnectionHolder() throws Exception {
        return getSafeConnectionHolder(doGetSafeOwner(), connectionHolder, dataSourceName);
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
        IDatabase owner = doGetSafeOwner();
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            EntitySQL<Entity> entitySql = EntitySQL.create(owner, this.getEntityClass());
            if (fields != null) {
                entitySql.field(fields);
            }
            if (dbLocker != null) {
                entitySql.forUpdate(dbLocker);
            }
            return session.find(entitySql, this.getId(), this.getShardingable());
        }
    }

    @SuppressWarnings("unchecked")
    public Entity save() throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            return session.insert((Entity) this, this.getShardingable());
        }
    }

    @SuppressWarnings("unchecked")
    public Entity save(Fields fields) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            return session.insert((Entity) this, fields, this.getShardingable());
        }
    }

    public boolean saveIfNotExist() throws Exception {
        return saveIfNotExist(false);
    }

    @SuppressWarnings("unchecked")
    public boolean saveIfNotExist(boolean useLocker) throws Exception {
        IDatabase owner = doGetSafeOwner();
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            EntitySQL<Entity> entitySql = EntitySQL.create(owner, this.getEntityClass());
            if (useLocker) {
                entitySql.forUpdate(IDBLocker.DEFAULT);
            }
            Entity entity = session.find(entitySql, this.getId(), this.getShardingable());
            if (entity == null) {
                return session.insert((Entity) this, this.getShardingable()) != null;
            }
            return false;
        }
    }

    public Entity saveOrUpdate() throws Exception {
        return saveOrUpdate(null);
    }

    @SuppressWarnings("unchecked")
    public Entity saveOrUpdate(Fields fields) throws Exception {
        IDatabase owner = doGetSafeOwner();
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            EntitySQL<Entity> entitySql = EntitySQL.create(owner, this.getEntityClass()).forUpdate(IDBLocker.DEFAULT);
            if (fields != null) {
                entitySql.field(fields);
            }
            Entity entity = session.find(entitySql, this.getId(), this.getShardingable());
            if (entity == null) {
                return session.insert((Entity) this, this.getShardingable());
            }
            return session.update((Entity) this, fields, this.getShardingable());
        }
    }

    public Entity update() throws Exception {
        return update(null);
    }

    @SuppressWarnings("unchecked")
    public Entity update(Fields fields) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            return session.update((Entity) this, fields, this.getShardingable());
        }
    }

    @SuppressWarnings("unchecked")
    public Entity delete() throws Exception {
        IDatabase owner = doGetSafeOwner();
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            if (null != this.getId()) {
                if (session.delete(this.getEntityClass(), this.getId(), this.getShardingable()) > 0) {
                    return (Entity) this;
                }
            } else {
                Cond cond = buildCond(owner, this);
                if (StringUtils.isNotBlank(cond.toString())) {
                    if (session.executeForUpdate(Delete.create(owner)
                            .shardingable(this.getShardingable())
                            .from(this.getEntityClass())
                            .where(Where.create(cond)).toSQL()) > 0) {
                        return (Entity) this;
                    }
                }
            }
            return null;
        }
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass) throws Exception {
        return find(beanClass, Where.create(buildCond(doGetSafeOwner(), this)), null, null, null);
    }

    public IResultSet<Entity> find() throws Exception {
        return find(Where.create(buildCond(doGetSafeOwner(), this)), null, null, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, OrderBy orderBy) throws Exception {
        return find(beanClass, null, orderBy, null, null, null);
    }

    public IResultSet<Entity> find(OrderBy orderBy) throws Exception {
        return find((Fields) null, orderBy, null, null, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, OrderBy orderBy, GroupBy groupBy) throws Exception {
        return find(beanClass, null, orderBy, groupBy, null, null);
    }

    public IResultSet<Entity> find(OrderBy orderBy, GroupBy groupBy) throws Exception {
        return find((Fields) null, orderBy, groupBy, null, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, IDBLocker dbLocker) throws Exception {
        return find(beanClass, Where.create(buildCond(doGetSafeOwner(), this)), null, null, dbLocker);
    }

    public IResultSet<Entity> find(IDBLocker dbLocker) throws Exception {
        return find(Where.create(buildCond(doGetSafeOwner(), this)), null, null, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, OrderBy orderBy, IDBLocker dbLocker) throws Exception {
        return find(beanClass, null, orderBy, null, null, dbLocker);
    }

    public IResultSet<Entity> find(OrderBy orderBy, IDBLocker dbLocker) throws Exception {
        return find((Fields) null, orderBy, null, null, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, OrderBy orderBy, GroupBy groupBy, IDBLocker dbLocker) throws Exception {
        return find(beanClass, null, orderBy, groupBy, null, dbLocker);
    }

    public IResultSet<Entity> find(OrderBy orderBy, GroupBy groupBy, IDBLocker dbLocker) throws Exception {
        return find((Fields) null, orderBy, groupBy, null, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Page page) throws Exception {
        return find(beanClass, Where.create(buildCond(doGetSafeOwner(), this)), null, page, null);
    }

    public IResultSet<Entity> find(Page page) throws Exception {
        return find(Where.create(buildCond(doGetSafeOwner(), this)), null, page, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, OrderBy orderBy, Page page) throws Exception {
        return find(beanClass, null, orderBy, page, null);
    }

    public IResultSet<Entity> find(OrderBy orderBy, Page page) throws Exception {
        return find((Fields) null, orderBy, page, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, OrderBy orderBy, GroupBy groupBy, Page page) throws Exception {
        return find(beanClass, null, orderBy, groupBy, page, null);
    }

    public IResultSet<Entity> find(OrderBy orderBy, GroupBy groupBy, Page page) throws Exception {
        return find((Fields) null, orderBy, groupBy, page, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Page page, IDBLocker dbLocker) throws Exception {
        return find(beanClass, Where.create(buildCond(doGetSafeOwner(), this)), null, page, dbLocker);
    }

    public IResultSet<Entity> find(Page page, IDBLocker dbLocker) throws Exception {
        return find(Where.create(buildCond(doGetSafeOwner(), this)), null, page, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, OrderBy orderBy, Page page, IDBLocker dbLocker) throws Exception {
        return find(beanClass, null, orderBy, page, dbLocker);
    }

    public IResultSet<Entity> find(OrderBy orderBy, Page page, IDBLocker dbLocker) throws Exception {
        return find((Fields) null, orderBy, page, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, OrderBy orderBy, GroupBy groupBy, Page page, IDBLocker dbLocker) throws Exception {
        return find(beanClass, null, orderBy, groupBy, page, dbLocker);
    }

    public IResultSet<Entity> find(OrderBy orderBy, GroupBy groupBy, Page page, IDBLocker dbLocker) throws Exception {
        return find((Fields) null, orderBy, groupBy, page, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Fields fields) throws Exception {
        return find(beanClass, Where.create(buildCond(doGetSafeOwner(), this)), fields, null, null);
    }

    public IResultSet<Entity> find(Fields fields) throws Exception {
        return find(Where.create(buildCond(doGetSafeOwner(), this)), fields, null, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Fields fields, OrderBy orderBy) throws Exception {
        return find(beanClass, fields, orderBy, null, null, null);
    }

    public IResultSet<Entity> find(Fields fields, OrderBy orderBy) throws Exception {
        return find(fields, orderBy, null, null, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Fields fields, OrderBy orderBy, GroupBy groupBy) throws Exception {
        return find(beanClass, fields, orderBy, groupBy, null, null);
    }

    public IResultSet<Entity> find(Fields fields, OrderBy orderBy, GroupBy groupBy) throws Exception {
        return find(fields, orderBy, groupBy, null, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Fields fields, IDBLocker dbLocker) throws Exception {
        return find(beanClass, Where.create(buildCond(doGetSafeOwner(), this)), fields, null, dbLocker);
    }

    public IResultSet<Entity> find(Fields fields, IDBLocker dbLocker) throws Exception {
        return find(Where.create(buildCond(doGetSafeOwner(), this)), fields, null, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Fields fields, OrderBy orderBy, IDBLocker dbLocker) throws Exception {
        return find(beanClass, fields, orderBy, null, null, dbLocker);
    }

    public IResultSet<Entity> find(Fields fields, OrderBy orderBy, IDBLocker dbLocker) throws Exception {
        return find(fields, orderBy, null, null, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Fields fields, OrderBy orderBy, GroupBy groupBy, IDBLocker dbLocker) throws Exception {
        return find(beanClass, fields, orderBy, groupBy, null, dbLocker);
    }

    public IResultSet<Entity> find(Fields fields, OrderBy orderBy, GroupBy groupBy, IDBLocker dbLocker) throws Exception {
        return find(fields, orderBy, groupBy, null, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Fields fields, Page page) throws Exception {
        return find(beanClass, Where.create(buildCond(doGetSafeOwner(), this)), fields, page, null);
    }

    public IResultSet<Entity> find(Fields fields, Page page) throws Exception {
        return find(Where.create(buildCond(doGetSafeOwner(), this)), fields, page, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Fields fields, OrderBy orderBy, Page page) throws Exception {
        return find(beanClass, fields, orderBy, page, null);
    }

    public IResultSet<Entity> find(Fields fields, OrderBy orderBy, Page page) throws Exception {
        return find(fields, orderBy, page, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Fields fields, OrderBy orderBy, GroupBy groupBy, Page page) throws Exception {
        return find(beanClass, fields, orderBy, groupBy, page, null);
    }

    public IResultSet<Entity> find(Fields fields, OrderBy orderBy, GroupBy groupBy, Page page) throws Exception {
        return find(fields, orderBy, groupBy, page, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Fields fields, Page page, IDBLocker dbLocker) throws Exception {
        return find(beanClass, Where.create(buildCond(doGetSafeOwner(), this)), fields, page, dbLocker);
    }

    public IResultSet<Entity> find(Fields fields, Page page, IDBLocker dbLocker) throws Exception {
        return find(Where.create(buildCond(doGetSafeOwner(), this)), fields, page, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Fields fields, OrderBy orderBy, Page page, IDBLocker dbLocker) throws Exception {
        return find(beanClass, fields, orderBy, null, page, dbLocker);
    }

    public IResultSet<Entity> find(Fields fields, OrderBy orderBy, Page page, IDBLocker dbLocker) throws Exception {
        return find(fields, orderBy, null, page, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Fields fields, OrderBy orderBy, GroupBy groupBy, Page page, IDBLocker dbLocker) throws Exception {
        Where where = Where.create(buildCond(doGetSafeOwner(), this));
        if (orderBy != null) {
            where.orderBy().orderBy(orderBy);
        }
        return find(beanClass, where.groupBy(groupBy), fields, page, dbLocker);
    }

    public IResultSet<Entity> find(Fields fields, OrderBy orderBy, GroupBy groupBy, Page page, IDBLocker dbLocker) throws Exception {
        Where where = Where.create(buildCond(doGetSafeOwner(), this));
        if (orderBy != null) {
            where.orderBy().orderBy(orderBy);
        }
        return find(where.groupBy(groupBy), fields, page, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Where where) throws Exception {
        return find(beanClass, where, null, null, null);
    }

    public IResultSet<Entity> find(Where where) throws Exception {
        return find(where, null, null, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Where where, IDBLocker dbLocker) throws Exception {
        return find(beanClass, where, null, null, dbLocker);
    }

    public IResultSet<Entity> find(Where where, IDBLocker dbLocker) throws Exception {
        return find(where, null, null, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Where where, Fields fields) throws Exception {
        return find(beanClass, where, fields, null, null);
    }

    public IResultSet<Entity> find(Where where, Fields fields) throws Exception {
        return find(where, fields, null, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Where where, Fields fields, IDBLocker dbLocker) throws Exception {
        return find(beanClass, where, fields, null, dbLocker);
    }

    public IResultSet<Entity> find(Where where, Fields fields, IDBLocker dbLocker) throws Exception {
        return find(where, fields, null, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Where where, Fields fields, Page page) throws Exception {
        return find(beanClass, where, fields, page, null);
    }

    public IResultSet<Entity> find(Where where, Fields fields, Page page) throws Exception {
        return find(where, fields, page, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Where where, Page page) throws Exception {
        return find(beanClass, where, null, page, null);
    }

    public IResultSet<Entity> find(Where where, Page page) throws Exception {
        return find(where, null, page, null);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Where where, Page page, IDBLocker dbLocker) throws Exception {
        return find(beanClass, where, null, page, dbLocker);
    }

    public IResultSet<Entity> find(Where where, Page page, IDBLocker dbLocker) throws Exception {
        return find(where, null, page, dbLocker);
    }

    public static <Entity extends IEntity> IResultSet<Entity> find(IDatabase owner, IDatabaseConnectionHolder connectionHolder, String dataSourceName, IShardingable shardingable, Class<Entity> entityClass, Where where, Fields fields, Page page, IDBLocker dbLocker) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, getSafeConnectionHolder(owner, connectionHolder, dataSourceName))) {
            EntitySQL<Entity> entitySql = EntitySQL.create(owner, entityClass);
            if (fields != null) {
                entitySql.field(fields);
            }
            if (dbLocker != null) {
                entitySql.forUpdate(dbLocker);
            }
            return session.find(entitySql, where, page, shardingable);
        }
    }

    private static <Entity extends IEntity> Select buildSelect(IDatabase owner, String dataSourceName, IShardingable shardingable, Class<Entity> entityClass, Where where, Fields fields, IDBLocker dbLocker) {
        Select select = Select.create(owner, dataSourceName, entityClass)
                .shardingable(shardingable);
        if (fields != null && !fields.isEmpty()) {
            select.field(fields);
        }
        if (where != null) {
            select.where(where);
        }
        if (dbLocker != null) {
            select.forUpdate(dbLocker);
        }
        return select;
    }

    public static <Entity extends IEntity, T extends Serializable> IResultSet<T> find(IDatabase owner, String dataSourceName, IShardingable shardingable, Class<Entity> entityClass, Class<T> beanClass, Where where, Fields fields, Page page, IDBLocker dbLocker) throws Exception {
        return buildSelect(owner, dataSourceName, shardingable, entityClass, where, fields, dbLocker).find(new BeanResultSetHandler<>(beanClass), page);
    }

    public <T extends Serializable> IResultSet<T> find(Class<T> beanClass, Where where, Fields fields, Page page, IDBLocker dbLocker) throws Exception {
        return find(doGetSafeOwner(), getDataSourceName(), getShardingable(), getEntityClass(), beanClass, where, fields, page, dbLocker);
    }

    public IResultSet<Entity> find(Where where, Fields fields, Page page, IDBLocker dbLocker) throws Exception {
        return find(doGetSafeOwner(), getConnectionHolder(), getDataSourceName(), getShardingable(), getEntityClass(), where, fields, page, dbLocker);
    }

    public <T extends Serializable> IResultSet<T> findAll(Class<T> beanClass) throws Exception {
        return find(beanClass, (Where) null, null, null, null);
    }

    public IResultSet<Entity> findAll() throws Exception {
        return find((Where) null, null, null, null);
    }

    public <T extends Serializable> IResultSet<T> findAll(Class<T> beanClass, OrderBy orderBy) throws Exception {
        return findAll(beanClass, null, orderBy, null);
    }

    public IResultSet<Entity> findAll(OrderBy orderBy) throws Exception {
        return findAll(null, orderBy, null);
    }

    public <T extends Serializable> IResultSet<T> findAll(Class<T> beanClass, Fields fields, Page page) throws Exception {
        return find(beanClass, null, fields, page, null);
    }

    public IResultSet<Entity> findAll(Fields fields, Page page) throws Exception {
        return find((Where) null, fields, page, null);
    }

    public <T extends Serializable> IResultSet<T> findAll(Class<T> beanClass, Fields fields, OrderBy orderBy, Page page) throws Exception {
        Where where = null;
        if (orderBy != null) {
            where = Where.create(doGetSafeOwner());
            where.orderBy().orderBy(orderBy);
        }
        return find(beanClass, where, fields, page, null);
    }

    public IResultSet<Entity> findAll(Fields fields, OrderBy orderBy, Page page) throws Exception {
        Where where = null;
        if (orderBy != null) {
            where = Where.create(doGetSafeOwner());
            where.orderBy().orderBy(orderBy);
        }
        return find(where, fields, page, null);
    }

    public IResultSet<Entity> findAll(Fields fields) throws Exception {
        return find((Where) null, fields, null, null);
    }

    public IResultSet<Entity> findAll(Fields fields, OrderBy orderBy) throws Exception {
        return findAll(fields, orderBy, null);
    }

    public IResultSet<Entity> findAll(Page page) throws Exception {
        return find((Where) null, null, page, null);
    }

    public IResultSet<Entity> findAll(OrderBy orderBy, Page page) throws Exception {
        return findAll(null, orderBy, page);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass) throws Exception {
        return findFirst(beanClass, Where.create(buildCond(doGetSafeOwner(), this)), null, null);
    }

    public Entity findFirst() throws Exception {
        return findFirst(Where.create(buildCond(doGetSafeOwner(), this)), null, null);
    }

    public Entity findFirst(OrderBy orderBy) throws Exception {
        return findFirst((Fields) null, orderBy, null, null);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, OrderBy orderBy, GroupBy groupBy) throws Exception {
        return findFirst(beanClass, null, orderBy, groupBy, null);
    }

    public Entity findFirst(OrderBy orderBy, GroupBy groupBy) throws Exception {
        return findFirst((Fields) null, orderBy, groupBy, null);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, IDBLocker dbLocker) throws Exception {
        return findFirst(beanClass, Where.create(buildCond(doGetSafeOwner(), this)), null, dbLocker);
    }

    public Entity findFirst(IDBLocker dbLocker) throws Exception {
        return findFirst(Where.create(buildCond(doGetSafeOwner(), this)), null, dbLocker);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, OrderBy orderBy, IDBLocker dbLocker) throws Exception {
        return findFirst(beanClass, null, orderBy, null, dbLocker);
    }

    public Entity findFirst(OrderBy orderBy, IDBLocker dbLocker) throws Exception {
        return findFirst((Fields) null, orderBy, null, dbLocker);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, OrderBy orderBy, GroupBy groupBy, IDBLocker dbLocker) throws Exception {
        return findFirst(beanClass, null, orderBy, groupBy, dbLocker);
    }

    public Entity findFirst(OrderBy orderBy, GroupBy groupBy, IDBLocker dbLocker) throws Exception {
        return findFirst((Fields) null, orderBy, groupBy, dbLocker);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, Fields fields) throws Exception {
        return findFirst(beanClass, Where.create(buildCond(doGetSafeOwner(), this)), fields, null);
    }

    public Entity findFirst(Fields fields) throws Exception {
        return findFirst(Where.create(buildCond(doGetSafeOwner(), this)), fields, null);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, Fields fields, OrderBy orderBy) throws Exception {
        return findFirst(beanClass, fields, orderBy, null, null);
    }

    public Entity findFirst(Fields fields, OrderBy orderBy) throws Exception {
        return findFirst(fields, orderBy, null, null);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, Fields fields, OrderBy orderBy, GroupBy groupBy) throws Exception {
        return findFirst(beanClass, fields, orderBy, groupBy, null);
    }

    public Entity findFirst(Fields fields, OrderBy orderBy, GroupBy groupBy) throws Exception {
        return findFirst(fields, orderBy, groupBy, null);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, Fields fields, IDBLocker dbLocker) throws Exception {
        return findFirst(beanClass, Where.create(buildCond(doGetSafeOwner(), this)), fields, dbLocker);
    }

    public Entity findFirst(Fields fields, IDBLocker dbLocker) throws Exception {
        return findFirst(Where.create(buildCond(doGetSafeOwner(), this)), fields, dbLocker);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, Fields fields, OrderBy orderBy, IDBLocker dbLocker) throws Exception {
        return findFirst(beanClass, fields, orderBy, null, dbLocker);
    }

    public Entity findFirst(Fields fields, OrderBy orderBy, IDBLocker dbLocker) throws Exception {
        return findFirst(fields, orderBy, null, dbLocker);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, Fields fields, OrderBy orderBy, GroupBy groupBy, IDBLocker dbLocker) throws Exception {
        Where where = Where.create(buildCond(doGetSafeOwner(), this));
        if (orderBy != null) {
            where.orderBy().orderBy(orderBy);
        }
        return findFirst(beanClass, where.groupBy(groupBy), fields, dbLocker);
    }

    public Entity findFirst(Fields fields, OrderBy orderBy, GroupBy groupBy, IDBLocker dbLocker) throws Exception {
        Where where = Where.create(buildCond(doGetSafeOwner(), this));
        if (orderBy != null) {
            where.orderBy().orderBy(orderBy);
        }
        return findFirst(where.groupBy(groupBy), fields, dbLocker);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, Where where) throws Exception {
        return findFirst(beanClass, where, null, null);
    }

    public Entity findFirst(Where where) throws Exception {
        return findFirst(where, null, null);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, Where where, IDBLocker dbLocker) throws Exception {
        return findFirst(beanClass, where, null, dbLocker);
    }

    public Entity findFirst(Where where, IDBLocker dbLocker) throws Exception {
        return findFirst(where, null, dbLocker);
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, Where where, Fields fields) throws Exception {
        return findFirst(beanClass, where, fields, null);
    }

    public Entity findFirst(Where where, Fields fields) throws Exception {
        return findFirst(where, fields, null);
    }

    public static <Entity extends IEntity> Entity findFirst(IDatabase owner, IDatabaseConnectionHolder connectionHolder, String dataSourceName, IShardingable shardingable, Class<Entity> entityClass, Where where, Fields fields, IDBLocker dbLocker) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, getSafeConnectionHolder(owner, connectionHolder, dataSourceName))) {
            EntitySQL<Entity> entitySql = EntitySQL.create(owner, entityClass);
            if (fields != null) {
                entitySql.field(fields);
            }
            if (dbLocker != null) {
                entitySql.forUpdate(dbLocker);
            }
            return session.findFirst(entitySql, where, shardingable);
        }
    }

    public static <Entity extends IEntity, T extends Serializable> T findFirst(IDatabase owner, String dataSourceName, IShardingable shardingable, Class<Entity> entityClass, Class<T> beanClass, Where where, Fields fields, IDBLocker dbLocker) throws Exception {
        return buildSelect(owner, dataSourceName, shardingable, entityClass, where, fields, dbLocker).findFirst(new BeanResultSetHandler<>(beanClass));
    }

    public <T extends Serializable> T findFirst(Class<T> beanClass, Where where, Fields fields, IDBLocker dbLocker) throws Exception {
        return findFirst(doGetSafeOwner(), getDataSourceName(), getShardingable(), getEntityClass(), beanClass, where, fields, dbLocker);
    }

    public Entity findFirst(Where where, Fields fields, IDBLocker dbLocker) throws Exception {
        return findFirst(doGetSafeOwner(), getConnectionHolder(), getDataSourceName(), getShardingable(), getEntityClass(), where, fields, dbLocker);
    }

    public long count() throws Exception {
        return count(Where.create(buildCond(doGetSafeOwner(), this)));
    }

    public long count(Where where) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(doGetSafeOwner(), doGetSafeConnectionHolder())) {
            return session.count(this.getEntityClass(), where, this.getShardingable());
        }
    }

    public <T extends BaseEntity> EntityStateWrapper<T> stateWrapper() throws Exception {
        return stateWrapper(true);
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEntity> EntityStateWrapper<T> stateWrapper(boolean ignoreNull) throws Exception {
        return EntityStateWrapper.bind(dbOwner, (T) this, ignoreNull);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseEntity that = (BaseEntity) o;
        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }

    public static <T extends IEntity> Cond buildCond(IDatabase owner, T entity) throws Exception {
        return buildCond(owner, entity, false);
    }

    public static <T extends IEntity> Cond buildCond(IDatabase owner, T entity, boolean or) throws Exception {
        Cond cond = Cond.create(owner);
        EntityMeta entityMeta = EntityMeta.load(entity.getClass());
        ClassUtils.BeanWrapper<T> wrapper = ClassUtils.wrapper(entity);
        boolean flag = false;
        for (String field : entityMeta.getPropertyNames()) {
            Object value;
            if (entityMeta.isMultiplePrimaryKey() && entityMeta.isPrimaryKey(field)) {
                value = entityMeta.getPropertyByName(field).getField().get(entity.getId());
            } else {
                value = wrapper.getValue(entityMeta.getPropertyByName(field).getField().getName());
            }
            if (value != null) {
                if (flag) {
                    if (or) {
                        cond.or();
                    } else {
                        cond.and();
                    }
                } else {
                    flag = true;
                }
                cond.eq(field).param(value);
            }
        }
        return cond;
    }
}
