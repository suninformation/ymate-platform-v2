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

import com.alibaba.fastjson.annotation.JSONField;
import net.ymate.platform.commons.util.ClassUtils;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IResultSet;
import net.ymate.platform.core.persistence.IShardingable;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.core.persistence.base.EntityMeta;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.IDBLocker;
import net.ymate.platform.persistence.jdbc.IDatabase;
import net.ymate.platform.persistence.jdbc.IDatabaseConnectionHolder;
import net.ymate.platform.persistence.jdbc.IDatabaseSession;
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

    private final IDatabase owner;

    private final Class<Entity> entityClass;

    private IDatabaseConnectionHolder connectionHolder;

    private IShardingable shardingable;

    private String dataSourceName;

    /**
     * 构造器
     *
     * @param owner 所属JDBC数据库管理器
     */
    @SuppressWarnings("unchecked")
    public BaseEntity(IDatabase owner) {
        this.owner = owner;
        entityClass = (Class<Entity>) ClassUtils.getParameterizedTypes(getClass()).get(0);
    }

    @JSONField(serialize = false)
    public IDatabaseConnectionHolder getConnectionHolder() {
        return this.connectionHolder;
    }

    @JSONField(deserialize = false)
    public void setConnectionHolder(IDatabaseConnectionHolder connectionHolder) {
        this.connectionHolder = connectionHolder;
        // 每次设置将记录数据源名称，若设置为空将重置
        if (this.connectionHolder != null) {
            this.dataSourceName = this.connectionHolder.getDataSourceConfig().getName();
        } else {
            this.dataSourceName = null;
        }
    }

    @JSONField(serialize = false)
    public IShardingable getShardingable() {
        return this.shardingable;
    }

    @JSONField(deserialize = false)
    public void setShardingable(IShardingable shardingable) {
        this.shardingable = shardingable;
    }

    @JSONField(serialize = false)
    public String getDataSourceName() {
        return this.dataSourceName;
    }

    @JSONField(deserialize = false)
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
     * @return 确保能够正确获取到数据库连接持有对象，即连接持有对象为null时尝试获取JDBC默认连接
     * @throws Exception 可能产生的异常
     */
    protected IDatabaseConnectionHolder doGetSafeConnectionHolder() throws Exception {
        if (this.connectionHolder == null || this.connectionHolder.getConnection() == null || this.connectionHolder.getConnection().isClosed()) {
            if (StringUtils.isNotBlank(this.dataSourceName)) {
                this.connectionHolder = owner.getConnectionHolder(this.dataSourceName);
            } else {
                this.connectionHolder = owner.getDefaultConnectionHolder();
            }
        }
        return this.connectionHolder;
    }

    public void entityCreate() throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            session.executeForUpdate(SQL.create(session.getConnectionHolder().getDialect().buildCreateSql(this.entityClass, session.getConnectionHolder().getDataSourceConfig().getTablePrefix(), this.getShardingable())));
        }
    }

    public void entityDrop() throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
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
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            EntitySQL<Entity> entitySql = EntitySQL.create(this.getEntityClass());
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
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            return session.insert((Entity) this, this.getShardingable());
        }
    }

    @SuppressWarnings("unchecked")
    public Entity save(Fields fields) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            return session.insert((Entity) this, fields, this.getShardingable());
        }
    }

    public Entity saveOrUpdate() throws Exception {
        return saveOrUpdate(null);
    }

    @SuppressWarnings("unchecked")
    public Entity saveOrUpdate(Fields fields) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            EntitySQL<Entity> entitySql = EntitySQL.create(this.getEntityClass());
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
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            return session.update((Entity) this, fields, this.getShardingable());
        }
    }

    @SuppressWarnings("unchecked")
    public Entity delete() throws Exception {
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

    public IResultSet<Entity> find() throws Exception {
        return find(Where.create(buildCond(owner, this)), null, null, null);
    }

    public IResultSet<Entity> find(IDBLocker dbLocker) throws Exception {
        return find(Where.create(buildCond(owner, this)), null, null, dbLocker);
    }

    public IResultSet<Entity> find(Page page) throws Exception {
        return find(Where.create(buildCond(owner, this)), null, page, null);
    }

    public IResultSet<Entity> find(Page page, IDBLocker dbLocker) throws Exception {
        return find(Where.create(buildCond(owner, this)), null, page, dbLocker);
    }

    public IResultSet<Entity> find(Fields fields) throws Exception {
        return find(Where.create(buildCond(owner, this)), fields, null, null);
    }

    public IResultSet<Entity> find(Fields fields, IDBLocker dbLocker) throws Exception {
        return find(Where.create(buildCond(owner, this)), fields, null, dbLocker);
    }

    public IResultSet<Entity> find(Fields fields, Page page) throws Exception {
        return find(Where.create(buildCond(owner, this)), fields, page, null);
    }

    public IResultSet<Entity> find(Fields fields, Page page, IDBLocker dbLocker) throws Exception {
        return find(Where.create(buildCond(owner, this)), fields, page, dbLocker);
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
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            EntitySQL<Entity> entitySql = EntitySQL.create(this.getEntityClass());
            if (fields != null) {
                entitySql.field(fields);
            }
            if (dbLocker != null) {
                entitySql.forUpdate(dbLocker);
            }
            return session.find(entitySql, where, page, this.getShardingable());
        }
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
        return findFirst(Where.create(buildCond(owner, this)), null, null);
    }

    public Entity findFirst(IDBLocker dbLocker) throws Exception {
        return findFirst(Where.create(buildCond(owner, this)), null, dbLocker);
    }

    public Entity findFirst(Fields fields) throws Exception {
        return findFirst(Where.create(buildCond(owner, this)), fields, null);
    }

    public Entity findFirst(Fields fields, IDBLocker dbLocker) throws Exception {
        return findFirst(Where.create(buildCond(owner, this)), fields, dbLocker);
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
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            EntitySQL<Entity> entitySql = EntitySQL.create(this.getEntityClass());
            if (fields != null) {
                entitySql.field(fields);
            }
            if (dbLocker != null) {
                entitySql.forUpdate(dbLocker);
            }
            return session.findFirst(entitySql, where, this.getShardingable());
        }
    }

    public long count() throws Exception {
        return count(Where.create(buildCond(owner, this)));
    }

    public long count(Where where) throws Exception {
        try (IDatabaseSession session = new DefaultDatabaseSession(owner, doGetSafeConnectionHolder())) {
            return session.count(this.getEntityClass(), where, this.getShardingable());
        }
    }

    public <T extends BaseEntity> EntityStateWrapper<T> stateWrapper() throws Exception {
        return stateWrapper(true);
    }

    @SuppressWarnings("unchecked")
    public <T extends BaseEntity> EntityStateWrapper<T> stateWrapper(boolean ignoreNull) throws Exception {
        return new EntityStateWrapper<>((T) this, ignoreNull);
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
