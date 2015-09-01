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
package net.ymate.platform.persistence.jdbc;

import net.ymate.platform.persistence.base.IEntity;
import net.ymate.platform.persistence.jdbc.base.IResultSet;
import net.ymate.platform.persistence.jdbc.base.IResultSetHandler;
import net.ymate.platform.persistence.jdbc.query.*;

import java.io.Serializable;
import java.util.List;

/**
 * 数据库会话操作接口定义
 *
 * @author 刘镇 (suninformation@163.com) on 2011-9-21 下午01:45:36
 * @version 1.0
 */
public interface ISession {

    /**
     * @return 获取会话对象唯一标识ID
     */
    public String getId();

    /**
     * @return 获取数据连接对象
     */
    public IConnectionHolder getConnectionHolder();

    /**
     * 设置会话事件处理器
     *
     * @param event 事件处理器接口
     * @return 返回当前会话对象
     */
    public ISession setSessionEvent(ISessionEvent event);

    /**
     * 关闭并释放会话
     */
    public void close();

    /**
     * @param <T>     指定结果集数据类型
     * @param sql     SQL语句对象
     * @param handler 结果集数据处理器
     * @return 执行SQL查询，返回全部结果数据
     * @throws Exception
     */
    public <T> IResultSet<T> find(SQL sql, IResultSetHandler<T> handler) throws Exception;

    /**
     * @param <T>     指定结果集数据类型
     * @param sql     SQL语句对象
     * @param handler 结果集数据处理器
     * @param page    分页参数对象
     * @return 执行SQL分页查询（执行总记录数统计）
     * @throws Exception
     */
    public <T> IResultSet<T> find(SQL sql, IResultSetHandler<T> handler, Page page) throws Exception;

    /**
     * @param entity 实体对象
     * @param <T>    指定结果集数据类型
     * @return 根据实体属性值执行SQL查询，返回全部结果数据
     * @throws Exception
     */
    public <T extends IEntity> IResultSet<T> find(T entity) throws Exception;

    /**
     * @param <T>    指定结果集数据类型
     * @param entity 实体查询对象
     * @return 根据实体执行SQL查询，返回全部结果数据
     * @throws Exception
     */
    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity) throws Exception;

    /**
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @param where  查询条件对象
     * @return 根据实体执行SQL查询，返回全部结果数据
     * @throws Exception
     */
    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Where where) throws Exception;

    /**
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @param where  查询条件对象
     * @param page   分页参数对象
     * @return 根据实体执行SQL分页查询
     * @throws Exception
     */
    public <T extends IEntity> IResultSet<T> find(EntitySQL<T> entity, Where where, Page page) throws Exception;

    /**
     * @param <T>    指定结果集数据类型
     * @param entity 实体查询对象
     * @param id     记录Id
     * @return 通过ID查找指定的实体对象
     * @throws Exception
     */
    public <T extends IEntity> T find(EntitySQL<T> entity, Serializable id) throws Exception;

    /**
     * @param <T>     指定结果集数据类型
     * @param sql     SQL语句对象
     * @param handler 结果集数据处理器
     * @return 执行SQL查询，返回结果集中第一条数据
     * @throws Exception
     */
    public <T> T findFirst(SQL sql, IResultSetHandler<T> handler) throws Exception;

    /**
     * @param <T>    指定结果集数据类型
     * @param entity 实体查询对象
     * @param where  查询条件对象
     * @return 根据实体执行SQL查询，返回结果集中第一条数据
     * @throws Exception
     */
    public <T extends IEntity> T findFirst(EntitySQL<T> entity, Where where) throws Exception;

    /**
     * @param sql SQL语句对象
     * @return 执行SQL更新（如更新、插入和删除），返回此次更新影响的记录数
     * @throws Exception
     */
    public int executeForUpdate(SQL sql) throws Exception;

    /**
     * @param sql 批量SQL更新语句
     * @return 执行SQL批量更新（如批更新、插入和删除），返回此次更新影响的记录数
     * @throws Exception
     */
    public int[] executeForUpdate(BatchSQL sql) throws Exception;

    /**
     * @param <T>    指定结果集数据类型
     * @param entity 实体查询对象
     * @param filter 显示字段过滤集合
     * @return 根据实体执行SQL更新，返回更新后的实体对象
     * @throws Exception
     */
    public <T extends IEntity> T update(T entity, Fields filter) throws Exception;

    /**
     * @param <T>      指定结果集数据类型
     * @param entities 实体查询对象集合
     * @param filter   显示字段过滤集合
     * @return 根据实体执行SQL批量更新，返回更新后的实体对象集合
     * @throws Exception
     */
    public <T extends IEntity> List<T> update(List<T> entities, Fields filter) throws Exception;

    /**
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @return 根据实体执行记录插入，返回插入后的实体对象
     * @throws Exception
     */
    public <T extends IEntity> T insert(T entity) throws Exception;

    public <T extends IEntity> T insert(T entity, Fields filter) throws Exception;

    /**
     * @param <T>      指定结果集数据类型
     * @param entities 实体对象集合
     * @return 根据实体执行记录批量插入，返回插入后的实体对象集合
     * @throws Exception
     */
    public <T extends IEntity> List<T> insert(List<T> entities) throws Exception;

    public <T extends IEntity> List<T> insert(List<T> entities, Fields filter) throws Exception;

    /**
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @return 根据实体执行记录删除，返回删除后的实体对象
     * @throws Exception
     */
    public <T extends IEntity> T delete(T entity) throws Exception;

    public <T extends IEntity> int delete(Class<T> entityClass, Serializable id) throws Exception;

    /**
     * @param <T>      指定结果集数据类型
     * @param entities 实体对象集合
     * @return 根据实体执行记录批量删除，返回删除后的实体对象集合
     * @throws Exception
     */
    public <T extends IEntity> List<T> delete(List<T> entities) throws Exception;

    public <T extends IEntity> int[] delete(Class<T> entityClass, Serializable[] ids) throws Exception;

    /**
     * @param <T>         指定实体类型
     * @param entityClass 实体类对象
     * @param where       查询条件对象
     * @return 计算查询结果总记录数量
     * @throws Exception
     */
    public <T extends IEntity> long count(Class<T> entityClass, Where where) throws Exception;

    /**
     * @param sql SQL语句对象
     * @return 计算查询结果总记录数量
     * @throws Exception
     */
    public long count(SQL sql) throws Exception;
}
