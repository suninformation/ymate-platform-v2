/*
 * Copyright 2007-2107 the original author or authors.
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

import net.ymate.platform.core.util.ClassUtils;
import net.ymate.platform.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.IConnectionHolder;
import net.ymate.platform.persistence.jdbc.ISession;
import net.ymate.platform.persistence.jdbc.JDBC;
import net.ymate.platform.persistence.jdbc.base.IResultSet;
import net.ymate.platform.persistence.jdbc.impl.DefaultSession;
import net.ymate.platform.persistence.jdbc.query.EntitySQL;
import net.ymate.platform.persistence.jdbc.query.Fields;
import net.ymate.platform.persistence.jdbc.query.Page;
import net.ymate.platform.persistence.jdbc.query.Where;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 实体模型接口抽象实现，提供基本数据库操作方法
 *
 * @author 刘镇 (suninformation@163.com) on 2013-7-16 下午5:22:15
 * @version 1.0
 */
public abstract class BaseEntity<Entity extends IEntity, PK extends Serializable> implements IEntity<PK> {

    private Class<Entity> __entityClass;

    private IConnectionHolder __connectionHolder;

    /**
     * 构造器
     */
    @SuppressWarnings("unchecked")
    public BaseEntity() {
        __entityClass = (Class<Entity>) ClassUtils.getParameterizedTypes(getClass()).get(0);
    }

    public IConnectionHolder getConnectionHolder() {
        return this.__connectionHolder;
    }

    public void setConnectionHolder(IConnectionHolder connectionHolder) {
        this.__connectionHolder = connectionHolder;
    }

    /**
     * @return 获取实体对象类型
     */
    protected Class<Entity> getEntityClass() {
        return this.__entityClass;
    }

    /**
     * @return 确保能够正确获取到数据库连接持有对象，即连接持有对象为null时尝试获取JDBC默认连接
     * @throws Exception
     */
    protected IConnectionHolder __doGetConnectionHolderSafed() throws Exception {
        if (this.__connectionHolder == null) {
            return JDBC.get().getDefaultConnectionHolder();
        }
        return this.__connectionHolder;
    }

    public Entity load() throws Exception {
        return load(Fields.create());
    }

    public Entity load(Fields fields) throws Exception {
        ISession _session = new DefaultSession(__doGetConnectionHolderSafed());
        try {
            return _session.find(EntitySQL.create(this.getEntityClass()).field(fields), this.getId());
        } finally {
            _session.close();
        }
    }

    @SuppressWarnings("unchecked")
    public Entity save() throws Exception {
        ISession _session = new DefaultSession(__doGetConnectionHolderSafed());
        try {
            return _session.insert((Entity) this);
        } finally {
            _session.close();
        }
    }

    public Entity saveOrUpdate() throws Exception {
        return saveOrUpdate(Fields.create());
    }

    @SuppressWarnings("unchecked")
    public Entity saveOrUpdate(Fields fields) throws Exception {
        ISession _session = new DefaultSession(__doGetConnectionHolderSafed());
        try {
            Entity _t = _session.find(EntitySQL.create(this.getEntityClass()).field(fields), this.getId());
            if (_t == null) {
                return _session.insert((Entity) this);
            }
            return _session.update((Entity) this, fields);
        } finally {
            _session.close();
        }
    }

    public Entity update() throws Exception {
        return update(null);
    }

    @SuppressWarnings("unchecked")
    public Entity update(Fields fields) throws Exception {
        ISession _session = new DefaultSession(__doGetConnectionHolderSafed());
        try {
            return _session.update((Entity) this, fields);
        } finally {
            _session.close();
        }
    }

    @SuppressWarnings("unchecked")
    public Entity delete() throws Exception {
        ISession _session = new DefaultSession(__doGetConnectionHolderSafed());
        try {
            _session.delete(this.getEntityClass(), this.getId());
            return (Entity) this;
        } finally {
            _session.close();
        }
    }

    public IResultSet<Entity> find(Where where) throws Exception {
        return find(where, Fields.create());
    }

    public IResultSet<Entity> find(Where where, Fields fields) throws Exception {
        ISession _session = new DefaultSession(__doGetConnectionHolderSafed());
        try {
            return _session.find(EntitySQL.create(this.getEntityClass()).field(fields), where);
        } finally {
            _session.close();
        }
    }

    public IResultSet<Entity> find(Where where, Fields fields, Page page) throws Exception {
        ISession _session = new DefaultSession(__doGetConnectionHolderSafed());
        try {
            return _session.find(EntitySQL.create(this.getEntityClass()).field(fields), where, page);
        } finally {
            _session.close();
        }
    }

    public IResultSet<Entity> findAll() throws Exception {
        return find(null, Fields.create());
    }

    public IResultSet<Entity> findAll(Fields fields, Page page) throws Exception {
        return find(null, fields, page);
    }

    public IResultSet<Entity> findAll(Page page) throws Exception {
        return find(null, Fields.create(), page);
    }

    public Entity findFirst() throws Exception {
        return findFirst(null, Fields.create());
    }

    public Entity findFirst(Fields fields) throws Exception {
        return findFirst(null, fields);
    }

    public Entity findFirst(Where where, Fields fields) throws Exception {
        ISession _session = new DefaultSession(__doGetConnectionHolderSafed());
        try {
            return _session.findFirst(EntitySQL.create(this.getEntityClass()).field(fields), where);
        } finally {
            _session.close();
        }
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
    }

}
