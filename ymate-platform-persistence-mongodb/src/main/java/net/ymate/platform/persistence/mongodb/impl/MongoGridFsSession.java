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

import com.mongodb.client.ClientSession;
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
import net.ymate.platform.persistence.mongodb.support.Operator;
import net.ymate.platform.persistence.mongodb.support.OrderBy;
import net.ymate.platform.persistence.mongodb.support.Query;
import net.ymate.platform.persistence.mongodb.support.QueryBuilder;
import net.ymate.platform.persistence.mongodb.transaction.Transactions;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonObjectId;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/26 上午11:12
 */
public class MongoGridFsSession extends AbstractSession<IMongoConnectionHolder> implements IGridFsSession {

    private final String bucketName;

    private final GridFSBucket fsBucket;

    private final MongoCollection<GridFSFile> dbCollection;

    private final IMongoConnectionHolder connectionHolder;

    public MongoGridFsSession(IMongoConnectionHolder connectionHolder) throws Exception {
        this(connectionHolder, GridFS.DEFAULT_BUCKET);
    }

    public MongoGridFsSession(IMongoConnectionHolder connectionHolder, String bucketName) throws Exception {
        if (connectionHolder == null) {
            throw new NullArgumentException("connectionHolder");
        }
        this.connectionHolder = connectionHolder;
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
        return upload(null, file, fsUploadOption);
    }

    @Override
    public String upload(String id, File file, GridFSUploadOptions fsUploadOption) throws Exception {
        if (file == null || !file.isAbsolute() || !file.isFile() || !file.exists() || !file.canRead()) {
            throw new IllegalArgumentException("file");
        }
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            return upload(id, file.getName(), inputStream, fsUploadOption);
        }
    }

    @Override
    public String upload(File file) throws Exception {
        return upload(null, file, null);
    }

    @Override
    public String upload(String id, File file) throws Exception {
        return upload(id, file, null);
    }

    @Override
    public String upload(String fileName, InputStream inputStream, GridFSUploadOptions fsUploadOption) throws Exception {
        return upload(null, fileName, inputStream, fsUploadOption);
    }

    @Override
    public String upload(String id, String fileName, InputStream inputStream, GridFSUploadOptions fsUploadOption) throws Exception {
        if (StringUtils.isBlank(fileName)) {
            throw new NullArgumentException("fileName");
        }
        if (inputStream == null) {
            throw new NullArgumentException("inputStream");
        }
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        ObjectId returnValue;
        if (StringUtils.isNotBlank(id)) {
            returnValue = new ObjectId(id);
            if (clientSession == null) {
                fsBucket.uploadFromStream(new BsonObjectId(returnValue), fileName, inputStream, fsUploadOption != null ? fsUploadOption : new GridFSUploadOptions());
            } else {
                fsBucket.uploadFromStream(clientSession, new BsonObjectId(returnValue), fileName, inputStream, fsUploadOption != null ? fsUploadOption : new GridFSUploadOptions());
            }
        } else {
            if (clientSession == null) {
                returnValue = fsBucket.uploadFromStream(fileName, inputStream, fsUploadOption != null ? fsUploadOption : new GridFSUploadOptions());
            } else {
                returnValue = fsBucket.uploadFromStream(clientSession, fileName, inputStream, fsUploadOption != null ? fsUploadOption : new GridFSUploadOptions());
            }
        }
        return returnValue.toString();
    }

    @Override
    public String upload(String fileName, InputStream inputStream) throws Exception {
        return upload(null, fileName, inputStream, null);
    }

    @Override
    public String upload(String id, String fileName, InputStream inputStream) throws Exception {
        return upload(id, fileName, inputStream, null);
    }

    @Override
    public void download(String id, OutputStream outputStream) throws Exception {
        if (StringUtils.isBlank(id)) {
            throw new NullArgumentException("id");
        }
        if (outputStream == null) {
            throw new NullArgumentException("outputStream");
        }
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            fsBucket.downloadToStream(new ObjectId(id), outputStream);
        } else {
            fsBucket.downloadToStream(clientSession, new ObjectId(id), outputStream);
        }
    }

    @Override
    public void download(String id, File distFile) throws Exception {
        if (distFile == null || !distFile.isAbsolute()) {
            throw new IllegalArgumentException("distFile");
        }
        try (OutputStream outputStream = new FileOutputStream(distFile)) {
            download(id, outputStream);
        }
    }

    @Override
    public boolean exists(String id) {
        return find(id) != null;
    }

    @Override
    public GridFSFile match(String fileHash) {
        if (StringUtils.isBlank(fileHash)) {
            throw new NullArgumentException("fileHash");
        }
        return findFirst(Query.create(IMongo.GridFs.MD5, Operator.create().eq(fileHash)));
    }

    @Override
    public GridFSFile findFirst(Query query) {
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            return fsBucket.find(query.toBson()).first();
        }
        return fsBucket.find(clientSession, query.toBson()).first();
    }

    @Override
    public GridFSFile findFirst(QueryBuilder query) {
        return findFirst(query.build());
    }

    @Override
    public GridFSFile find(String id) {
        if (StringUtils.isBlank(id)) {
            throw new NullArgumentException("id");
        }
        return findFirst(Query.create(IMongo.GridFs.ID, Operator.create().eq(new ObjectId(id))));
    }

    @Override
    public IResultSet<GridFSFile> find() {
        return find((Query) null, null, null);
    }

    @Override
    public IResultSet<GridFSFile> find(OrderBy orderBy) {
        return find(orderBy, null);
    }

    @Override
    public IResultSet<GridFSFile> find(OrderBy orderBy, Page page) {
        return find((Query) null, orderBy, page);
    }

    @Override
    public IResultSet<GridFSFile> find(String filename, OrderBy orderBy) {
        return find(filename, orderBy, null);
    }

    @Override
    public IResultSet<GridFSFile> find(String filename, OrderBy orderBy, Page page) {
        if (StringUtils.isBlank(filename)) {
            throw new NullArgumentException(IMongo.GridFs.FILE_NAME);
        }
        Query query = Query.create(IMongo.GridFs.FILE_NAME, Operator.create().eq(filename));
        return find(query, orderBy, page);
    }

    @Override
    public IResultSet<GridFSFile> find(Query query) {
        return find(query, null, null);
    }

    @Override
    public IResultSet<GridFSFile> find(QueryBuilder query) {
        return find(query.build(), null, null);
    }

    @Override
    public IResultSet<GridFSFile> find(Query query, OrderBy orderBy) {
        return find(query, orderBy, null);
    }

    @Override
    public IResultSet<GridFSFile> find(QueryBuilder query, OrderBy orderBy) {
        return find(query.build(), orderBy, null);
    }

    @Override
    public IResultSet<GridFSFile> find(Query query, OrderBy orderBy, Page page) {
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        GridFSFindIterable findIterable;
        if (query != null) {
            findIterable = clientSession == null ? fsBucket.find(query.toBson()) : fsBucket.find(clientSession, query.toBson());
        } else {
            findIterable = clientSession == null ? fsBucket.find() : fsBucket.find(clientSession);
        }
        if (orderBy != null) {
            findIterable.sort(orderBy.toBson());
        }
        long recordCount = 0;
        boolean pageFlag = false;
        if (page != null && page.page() > 0 && page.pageSize() > 0) {
            findIterable.skip((page.page() - 1) * page.pageSize()).limit(page.pageSize());
            if (query != null) {
                recordCount = clientSession == null ? dbCollection.countDocuments(query.toBson()) : dbCollection.countDocuments(clientSession, query.toBson());
            } else {
                recordCount = clientSession == null ? dbCollection.countDocuments() : dbCollection.countDocuments(clientSession);
            }
            pageFlag = true;
        }
        List<GridFSFile> results = new ArrayList<>();
        findIterable.forEach((Consumer<? super GridFSFile>) results::add);
        return pageFlag ? new DefaultResultSet<>(results, page.page(), page.pageSize(), recordCount) : new DefaultResultSet<>(results);
    }

    @Override
    public IResultSet<GridFSFile> find(QueryBuilder query, OrderBy orderBy, Page page) {
        return find(query.build(), orderBy, page);
    }

    @Override
    public IResultSet<GridFSFile> find(Query query, Page page) {
        return find(query, null, page);
    }

    @Override
    public IResultSet<GridFSFile> find(QueryBuilder query, Page page) {
        return find(query.build(), null, page);
    }

    @Override
    public void rename(String id, String newFileName) {
        if (StringUtils.isBlank(id)) {
            throw new NullArgumentException("id");
        }
        if (StringUtils.isBlank(newFileName)) {
            throw new NullArgumentException("newFileName");
        }
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            fsBucket.rename(new ObjectId(id), newFileName);
        } else {
            fsBucket.rename(clientSession, new ObjectId(id), newFileName);
        }
    }

    @Override
    public void remove(String id) {
        if (StringUtils.isBlank(id)) {
            throw new NullArgumentException("id");
        }
        ClientSession clientSession = Transactions.getClientSession(connectionHolder);
        if (clientSession == null) {
            fsBucket.delete(new ObjectId(id));
        } else {
            fsBucket.delete(clientSession, new ObjectId(id));
        }
    }

    @Override
    public void remove(Collection<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            ids.stream().filter(StringUtils::isNotBlank).map(ObjectId::new).forEach(fsBucket::delete);
        }
    }
}
