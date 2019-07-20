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
package net.ymate.platform.persistence.mongodb;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MapReduceIterable;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IResultSet;
import net.ymate.platform.core.persistence.ISession;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.core.persistence.base.IEntity;
import net.ymate.platform.persistence.mongodb.support.Aggregation;
import net.ymate.platform.persistence.mongodb.support.OrderBy;
import net.ymate.platform.persistence.mongodb.support.Query;
import org.bson.Document;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/21 下午12:07
 */
@Ignored
public interface IMongoSession extends ISession<IMongoConnectionHolder> {

    /**
     * 根据实体执行查询
     *
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @return 返回全部结果数据
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> IResultSet<T> find(Class<T> entity) throws Exception;

    /**
     * 根据实体执行查询
     *
     * @param <T>     指定结果集数据类型
     * @param entity  实体对象
     * @param orderBy 排序条件
     * @return 返回全部结果数据
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> IResultSet<T> find(Class<T> entity, OrderBy orderBy) throws Exception;

    /**
     * 根据实体执行分页查询
     *
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @param page   分页查询对象
     * @return 返回结果数据
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> IResultSet<T> find(Class<T> entity, Page page) throws Exception;

    /**
     * 根据实体执行分页查询
     *
     * @param <T>     指定结果集数据类型
     * @param entity  实体对象
     * @param orderBy 排序条件
     * @param page    分页查询对象
     * @return 返回结果数据
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> IResultSet<T> find(Class<T> entity, OrderBy orderBy, Page page) throws Exception;

    /**
     * 根据指定key条件执行查询
     *
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @param filter 过滤条件
     * @return 返回结果集中第一条数据
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> T findFirst(Class<T> entity, Query filter) throws Exception;

    /**
     * 通过ID查找指定的实体对象
     *
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @param id     记录Id
     * @return 返回实体对象
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> T find(Class<T> entity, Serializable id) throws Exception;

    /**
     * 计算查询结果总记录数量
     *
     * @param <T>    指定实体类型
     * @param entity 实体类对象
     * @return 返回记录数量
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> long count(Class<T> entity) throws Exception;

    /**
     * 计算查询结果总记录数量
     *
     * @param <T>    指定实体类型
     * @param entity 实体类对象
     * @param filter 过滤条件
     * @return 返回总记录数量
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> long count(Class<T> entity, Query filter) throws Exception;

    /**
     * 判断指定ID的实体记录是否存在
     *
     * @param entity 实体类对象
     * @param id     记录Id
     * @param <T>    指定实体类型
     * @return 返回true表示记录存在
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> boolean exists(Class<T> entity, Serializable id) throws Exception;

    /**
     * 判断指定条件的实体记录是否存在
     *
     * @param entity 实体类对象
     * @param filter 条件对象
     * @param <T>    指定实体类型
     * @return 返回true表示记录存在
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> boolean exists(Class<T> entity, Query filter) throws Exception;

    /**
     * 聚合
     *
     * @param entity       实体类对象
     * @param resultClass  结果类对象
     * @param aggregations 聚合条件表达式
     * @param <T>          指定实体类型
     * @param <RESULT>     指定结果类型
     * @return 返回聚合结果集迭代器对象
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity, RESULT> AggregateIterable<RESULT> aggregate(Class<T> entity, Class<RESULT> resultClass, Aggregation... aggregations) throws Exception;

    /**
     * 去重
     *
     * @param entity      实体类对象
     * @param resultClass 结果类对象
     * @param fieldName   属性名称
     * @param <T>         指定实体类型
     * @param <RESULT>    指定结果类型
     * @return 返回去重结果集迭代器对象
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity, RESULT> DistinctIterable<RESULT> distinct(Class<T> entity, Class<RESULT> resultClass, String fieldName) throws Exception;

    /**
     * 去重
     *
     * @param entity      实体类对象
     * @param resultClass 结果类对象
     * @param fieldName   属性名称
     * @param query       过滤条件
     * @param <T>         指定实体类型
     * @param <RESULT>    指定结果类型
     * @return 返回去重结果集迭代器对象
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity, RESULT> DistinctIterable<RESULT> distinct(Class<T> entity, Class<RESULT> resultClass, String fieldName, Query query) throws Exception;

    /**
     * MapReduce
     *
     * @param entity         实体类对象
     * @param resultClass    结果类对象
     * @param mapFunction    关联Map映射的JavaScript脚本函数
     * @param reduceFunction 关联Reduces的JavaScript脚本函数
     * @param <T>            指定实体类型
     * @param <RESULT>       指定结果类型
     * @return 返回MapReduce结果集迭代器对象
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity, RESULT> MapReduceIterable<RESULT> mapReduce(Class<T> entity, Class<RESULT> resultClass, String mapFunction, String reduceFunction) throws Exception;

    /**
     * MapReduce
     *
     * @param entity         实体类对象
     * @param mapFunction    关联Map映射的JavaScript脚本函数
     * @param reduceFunction 关联Reduces的JavaScript脚本函数
     * @param <T>            指定实体类型
     * @return 返回MapReduce结果集迭代器对象
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> MapReduceIterable<Document> mapReduce(Class<T> entity, String mapFunction, String reduceFunction) throws Exception;

    /**
     * 根据实体执行更新
     *
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @return 返回更新之前的实体对象
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> T update(T entity) throws Exception;

    /**
     * 根据实体执行更新
     *
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @param filter 更新字段过滤集合
     * @return 返回更新之前的实体对象
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> T update(T entity, Fields filter) throws Exception;

    /**
     * 根据实体执行批量更新
     *
     * @param <T>      指定结果集数据类型
     * @param entities 实体对象集合
     * @return 返回更新之前的实体对象集合
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> List<T> update(List<T> entities) throws Exception;

    /**
     * 根据实体执行批量更新
     *
     * @param <T>      指定结果集数据类型
     * @param entities 实体对象集合
     * @param filter   更新字段过滤集合
     * @return 返回更新之前的实体对象集合
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> List<T> update(List<T> entities, Fields filter) throws Exception;

    /**
     * 根据实体执行记录插入
     *
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @return 返回插入后的实体对象
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> T insert(T entity) throws Exception;

    /**
     * 根据实体执行记录批量插入
     *
     * @param <T>      指定结果集数据类型
     * @param entities 实体对象集合
     * @return 返回插入后的实体对象集合
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> List<T> insert(List<T> entities) throws Exception;

    /**
     * 根据实体执行记录删除
     *
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @return 返回删除之前的实体对象
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> T delete(T entity) throws Exception;

    /**
     * 删除指定ID的记录
     *
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @param id     记录唯一标识
     * @return 返回删除之前的实体对象
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> T delete(Class<T> entity, Serializable id) throws Exception;

    /**
     * 根据实体执行记录批量删除
     *
     * @param <T>      指定结果集数据类型
     * @param entities 实体对象集合
     * @return 返回删除之前的实体对象集合
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> List<T> delete(List<T> entities) throws Exception;

    /**
     * 批量删除指定ID的记录
     *
     * @param <T>    指定结果集数据类型
     * @param entity 实体对象
     * @param ids    记录唯一标识集合
     * @return 返回删除之前的实体对象集合
     * @throws Exception 可能产生的异常
     */
    <T extends IEntity> long delete(Class<T> entity, Collection<Serializable> ids) throws Exception;
}
