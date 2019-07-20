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

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.IResultSet;
import net.ymate.platform.core.persistence.ISession;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.persistence.mongodb.support.OrderBy;
import net.ymate.platform.persistence.mongodb.support.Query;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/26 上午10:06
 */
@Ignored
public interface IGridFsSession extends ISession<IMongoConnectionHolder> {

    /**
     * 获取GridFSBucket对象
     *
     * @return 返回GridFSBucket对象
     */
    GridFSBucket getGridFsBucket();

    /**
     * 获取桶名称
     *
     * @return 返回桶名称
     */
    String getBucketName();

    /**
     * 上传文件
     *
     * @param file           待上传的文件对象
     * @param fsUploadOption 文件上传配置对象
     * @return 返回上传的文件唯一标识
     * @throws Exception 可能产生的任何异常
     */
    String upload(File file, GridFSUploadOptions fsUploadOption) throws Exception;

    /**
     * 上传文件
     *
     * @param fileName       文件名称
     * @param inputStream    文件输入流对象
     * @param fsUploadOption 文件上传配置对象
     * @return 返回上传的文件唯一标识
     * @throws Exception 可能产生的任何异常
     */
    String upload(String fileName, InputStream inputStream, GridFSUploadOptions fsUploadOption) throws Exception;

    /**
     * 判断指定id的文件是否存在
     *
     * @param id 文件唯一标识
     * @return 返回true表示存在
     */
    boolean exists(String id);

    /**
     * 根据条件查询符合条件的第一条记录
     *
     * @param query 查询条件对象
     * @return 返回文件记录对象
     */
    GridFSFile findFirst(Query query);

    /**
     * 根据id查询文件记录
     *
     * @param id 文件唯一标识
     * @return 返回文件记录对象
     */
    GridFSFile find(String id);

    /**
     * 查询全部记录
     *
     * @return 返回查询结果集对象
     */
    IResultSet<GridFSFile> find();

    /**
     * 查询全部记录
     *
     * @param orderBy 排序对象
     * @return 返回查询结果集对象
     */
    IResultSet<GridFSFile> find(OrderBy orderBy);

    /**
     * 查询全部记录
     *
     * @param orderBy 排序对象
     * @param page    分页对象
     * @return 返回查询结果集对象
     */
    IResultSet<GridFSFile> find(OrderBy orderBy, Page page);

    /**
     * 查询指定名称的文件记录
     *
     * @param filename 文件名称
     * @param orderBy  排序对象
     * @return 返回查询结果集对象
     */
    IResultSet<GridFSFile> find(String filename, OrderBy orderBy);

    /**
     * 根据条件查询符合条件的文件记录
     *
     * @param query 条件对象
     * @return 返回查询结果集对象
     */
    IResultSet<GridFSFile> find(Query query);

    /**
     * 根据条件查询符合条件的文件记录
     *
     * @param query   条件对象
     * @param orderBy 排序对象
     * @return 返回查询结果集对象
     */
    IResultSet<GridFSFile> find(Query query, OrderBy orderBy);

    /**
     * 根据条件查询符合条件的文件记录
     *
     * @param query   条件对象
     * @param orderBy 排序对象
     * @param page    分页对象
     * @return 返回查询结果集对象
     */
    IResultSet<GridFSFile> find(Query query, OrderBy orderBy, Page page);

    /**
     * 根据条件查询符合条件的文件记录
     *
     * @param query 条件对象
     * @param page  分页对象
     * @return 返回查询结果集对象
     */
    IResultSet<GridFSFile> find(Query query, Page page);

    /**
     * 文件重命名
     *
     * @param id          文件唯一标识
     * @param newFileName 新文件名称
     */
    void rename(String id, String newFileName);

    /**
     * 删除指定id的文件记录
     *
     * @param id 文件唯一标识
     */
    void remove(String id);

    /**
     * 批量删除指定id的文件记录
     *
     * @param ids 文件唯一标识集合
     */
    void remove(Collection<String> ids);
}
