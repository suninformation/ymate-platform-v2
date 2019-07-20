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
package net.ymate.platform.persistence.mongodb.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.gridfs.GridFS;
import net.ymate.platform.core.persistence.AbstractSession;
import net.ymate.platform.core.persistence.IResultSet;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.core.persistence.impl.DefaultResultSet;
import net.ymate.platform.persistence.mongodb.IGridFsSession;
import net.ymate.platform.persistence.mongodb.IMongo;
import net.ymate.platform.persistence.mongodb.IMongoConnectionHolder;
import net.ymate.platform.persistence.mongodb.IMongoDataSourceAdapter;
import net.ymate.platform.persistence.mongodb.support.Operator;
import net.ymate.platform.persistence.mongodb.support.OrderBy;
import net.ymate.platform.persistence.mongodb.support.Query;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonArray;
import org.bson.BsonObjectId;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/26 上午11:12
 */
public class MongoGridFsSession extends AbstractSession<IMongoConnectionHolder> implements IGridFsSession {

    private String bucketName;

    private GridFSBucket fsBucket;

    private MongoCollection<GridFSFile> dbCollection;

    private IMongoConnectionHolder connectionHolder;

    public MongoGridFsSession(IMongoDataSourceAdapter dataSourceAdapter) throws Exception {
        this(dataSourceAdapter, GridFS.DEFAULT_BUCKET);
    }

    public MongoGridFsSession(IMongoDataSourceAdapter dataSourceAdapter, String bucketName) throws Exception {
        this.connectionHolder = new DefaultMongoConnectionHolder(dataSourceAdapter);
        this.bucketName = StringUtils.defaultIfBlank(bucketName, GridFS.DEFAULT_BUCKET);
        //
        MongoDatabase mongoDatabase = this.connectionHolder.getConnection();
        fsBucket = GridFSBuckets.create(mongoDatabase, this.bucketName);
        dbCollection = mongoDatabase.getCollection(this.bucketName.concat(".files"), GridFSFile.class);
    }

    @Override
    public GridFSBucket getGridFsBucket() {
        return fsBucket;
    }

    @Override
    public IMongoConnectionHolder getConnectionHolder() {
        return connectionHolder;
    }

    @Override
    public String getBucketName() {
        return bucketName;
    }

    @Override
    public void close() {
    }

    @Override
    public String upload(File file, GridFSUploadOptions fsUploadOption) throws Exception {
        try (InputStream inputStream = new FileInputStream(file)) {
            return upload(file.getName(), inputStream, fsUploadOption);
        }
    }

    @Override
    public String upload(String fileName, InputStream inputStream, GridFSUploadOptions fsUploadOption) throws Exception {
        ObjectId returnValue = fsBucket.uploadFromStream(fileName, inputStream, fsUploadOption != null ? fsUploadOption : new GridFSUploadOptions());
        return returnValue != null ? returnValue.toString() : null;
    }

    @Override
    public boolean exists(String id) {
        return find(id) != null;
    }

    @Override
    public GridFSFile findFirst(Query query) {
        return fsBucket.find(query.toBson()).first();
    }

    @Override
    public GridFSFile find(String id) {
        return findFirst(Query.create(IMongo.Opt.ID, Operator.create().eq(id)));
    }

    @Override
    public IResultSet<GridFSFile> find() {
        return find(null, null, null);
    }

    @Override
    public IResultSet<GridFSFile> find(OrderBy orderBy) {
        return find(orderBy, null);
    }

    @Override
    public IResultSet<GridFSFile> find(OrderBy orderBy, Page page) {
        return find(null, orderBy, page);
    }

    @Override
    public IResultSet<GridFSFile> find(String filename, OrderBy orderBy) {
        if (StringUtils.isBlank(filename)) {
            throw new NullArgumentException(IMongo.GridFs.FILE_NAME);
        }
        Query query = Query.create(IMongo.GridFs.FILE_NAME, Operator.create().eq(filename));
        return find(query, orderBy, null);
//        FindIterable<GridFSFile> findIterable = dbCollection.find(query.toBson());
//        if (orderBy != null) {
//            findIterable.sort(orderBy.toBson());
//        }
//        List<GridFSFile> results = new ArrayList<>();
//        findIterable.forEach((Consumer<? super GridFSFile>) results::add);
//        return new DefaultResultSet<>(results);
    }

    @Override
    public IResultSet<GridFSFile> find(Query query) {
        return find(query, null, null);
    }

    @Override
    public IResultSet<GridFSFile> find(Query query, OrderBy orderBy) {
        return find(query, orderBy, null);
    }

//    private IResultSet<GridFSFile> doFind() {
//        FindIterable<GridFSFile> findIterable = dbCollection.find();
//        if (orderBy != null) {
//            findIterable.sort(orderBy.toBson());
//        }
//        boolean pageFlag = false;
//        if (page != null && page.page() > 0 && page.pageSize() > 0) {
//            findIterable.skip((page.page() - 1) * page.pageSize()).limit(page.pageSize());
//            pageFlag = true;
//        }
//        List<GridFSFile> results = new ArrayList<>();
//        findIterable.forEach((Consumer<? super GridFSFile>) results::add);
//        return pageFlag ? new DefaultResultSet<>(results, page.page(), page.pageSize(), dbCollection.countDocuments()) : new DefaultResultSet<>(results);
//    }

    @Override
    public IResultSet<GridFSFile> find(Query query, OrderBy orderBy, Page page) {
        GridFSFindIterable findIterable;
        if (query != null) {
            findIterable = fsBucket.find(query.toBson());
        } else {
            findIterable = fsBucket.find();
        }
        if (orderBy != null) {
            findIterable.sort(orderBy.toBson());
        }
        long recordCount = 0;
        boolean pageFlag = false;
        if (page != null && page.page() > 0 && page.pageSize() > 0) {
            findIterable.skip((page.page() - 1) * page.pageSize()).limit(page.pageSize());
            //
            if (query != null) {
                recordCount = dbCollection.countDocuments(query.toBson());
            } else {
                recordCount = dbCollection.countDocuments();
            }
            pageFlag = true;
        }
        List<GridFSFile> results = new ArrayList<>();
        findIterable.forEach((Consumer<? super GridFSFile>) results::add);
        return pageFlag ? new DefaultResultSet<>(results, page.page(), page.pageSize(), recordCount) : new DefaultResultSet<>(results);
    }

    @Override
    public IResultSet<GridFSFile> find(Query query, Page page) {
        return find(query, null, page);
    }

    @Override
    public void rename(String id, String newFileName) {
        if (StringUtils.isBlank(id)) {
            throw new NullArgumentException("id");
        }
        if (StringUtils.isBlank(newFileName)) {
            throw new NullArgumentException("newFileName");
        }
        fsBucket.rename(new ObjectId(id), newFileName);
    }

    @Override
    public void remove(String id) {
        fsBucket.delete(new ObjectId(id));
    }

    @Override
    public void remove(Collection<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            BsonArray bsonIds = ids.stream().map(id -> new BsonObjectId(new ObjectId(id))).collect(Collectors.toCollection(BsonArray::new));
            fsBucket.delete(bsonIds);
        }
    }
}
