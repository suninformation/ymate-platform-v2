---
name: ymp-mongodb
description: YMP框架MongoDB持久化模块，基于会话机制的NoSQL数据存取封装，支持多数据源、事务、GridFS文件存储、聚合查询
version: 2.1.4-dev
author: YMP Team
category: persistence
tags:
  - java
  - mongodb
  - nosql
  - persistence
  - gridfs
trigger: 当用户需要使用MongoDB数据库、NoSQL持久化、GridFS文件存储、MongoDB聚合查询、MongoDB事务等场景时触发
tools:
  - mongodb
  - nosql-database
  - gridfs
examples:
  - MongoDB配置数据源并插入文档
  - MongoDB实体类@Entity映射查询更新删除
  - MongoDB Query+Operator条件查询分页排序
  - MongoDB Aggregation聚合分组统计
  - MongoDB GridFS文件上传下载
---

# MongoDB 技能包

> AI读取指引：本模块专注MongoDB NoSQL持久化。关系型数据库请跳转persistence-jdbc；缓存层业务缓存请跳转cache；核心容器注解请跳转core。

---

## 0. 快速索引
- Maven artifactId：`ymate-platform-persistence-mongodb`
- 静态入口类全限定名：`net.ymate.platform.persistence.mongodb.MongoDB`
- 必备注解（启动类）：`@EnableAutoScan` + `@EnableBeanProxy`
- 5行最简调用代码：

```java
UserEntity u = new UserEntity();
u.setNickname("test");
u.setAge(20);
u = MongoDB.get().openSession(session -> session.insert(u));
UserEntity found = MongoDB.get().openSession(session -> session.find(UserEntity.class, u.getId()));
```

## 1. 模块摘要
基于会话机制封装MongoDB Java驱动，提供实体映射、查询条件对象化拼装、多数据源切换、事务管理、GridFS文件存储及聚合管道能力。

- 会话`IMongoSession`统一管理连接生命周期，自动释放资源
- `Query`+`Operator`+`QueryBuilder`三种方式构建Bson查询条件
- 支持`Aggregation`聚合管道（match/group/project/sort/unwind等）
- GridFS会话`IGridFsSession`管理大文件上传下载与元数据查询
- 手动事务`openTransaction`支持副本集PRIMARY节点多文档原子性

## 2. 核心注解速查表

| 注解 | 全限定名 | 作用 | 核心参数2-5个 |
|------|----------|------|---------------|
| @MongoConf | `net.ymate.platform.persistence.mongodb.annotation.MongoConf` | 模块注解配置，声明数据源集合 | `dsDefaultName`默认数据源名；`value`=@MongoDataSource[] |
| @MongoDataSource | `net.ymate.platform.persistence.mongodb.annotation.MongoDataSource` | 单个MongoDB数据源定义 | `name`数据源名；`databaseName`数据库；`username`/`password`；`servers`主机数组；`autoConnection`；`connectionUrl`；`collectionPrefix`集合前缀 |
| @Entity | `net.ymate.platform.core.persistence.annotation.Entity` | 声明类为MongoDB文档实体（集合映射） | `value`集合名（默认类名） |
| @Id | `net.ymate.platform.core.persistence.annotation.Id` | 声明字段为主键_id | 无参数（配合@Property(name=IMongo.Opt.ID)） |
| @Property | `net.ymate.platform.core.persistence.annotation.Property` | 文档字段映射 | `name`字段名；`nullable`；`autoincrement`；`useKeyGenerator`键生成器 |
| @Comment | `net.ymate.platform.core.persistence.annotation.Comment` | 实体或字段注释 | `value`注释文本 |

## 3. 核心API速查

| API签名 | 作用 | 所在类/接口 |
|---------|------|------------|
| `MongoDB.get()` | 获取模块单例 | `MongoDB`静态入口 |
| `<T> T openSession(IMongoSessionExecutor<T>)` | Lambda形式开启默认数据源会话并自动关闭 | `IMongo` |
| `<T> T openSession(String dsName, IMongoSessionExecutor<T>)` | 指定数据源名称开启会话 | `IMongo` |
| `IMongoSession openSession()` | 手动开启会话（需try-with-resources） | `IMongo` |
| `<T extends IEntity> IResultSet<T> find(Class<T>, Query)` | 条件查询实体集合 | `IMongoSession` |
| `<T extends IEntity> T find(Class<T>, String id)` | 按_id查询单条 | `IMongoSession` |
| `<T extends IEntity> T insert(T entity)` / `List<T> insert(List<T>)` | 插入单条/批量（返回含_id） | `IMongoSession` |
| `<T extends IEntity> T update(T, Fields)` / `List<T> update(List, Fields)` | 指定字段更新（Fields可选） | `IMongoSession` |
| `long delete(Class, String/List<String>/Query)` / `T delete(T)` | 删除 | `IMongoSession` |
| `long count(Class, Query)` / `boolean exists(Class, id/Query)` | 统计与存在判断 | `IMongoSession` |
| `List<Document> aggregate(String collectionName, List<Aggregation>)` | 聚合查询 | `IMongoSession` |
| `IGridFsSession openGridFsSession(String bucketName)` | 开启GridFS会话（默认桶fs） | `IMongo` |
| `String gridFs.upload(File/InputStream, GridFSUploadOptions)` | 文件上传返回fileId | `IGridFsSession` |
| `void gridFs.download(String id, OutputStream/File)` | 按_id下载 | `IGridFsSession` |
| `GridFSFile gridFs.find(String id)` / `IResultSet<GridFSFile> gridFs.find(Query)` | 文件元数据查询 | `IGridFsSession` |
| `Query.create().cond(field, Operator)` | 链式构建查询 | `Query` |
| `Operator.create().eq/ne/gt/gte/lt/lte/in/nin/regex/exists/between` | 比较与元素运算 | `Operator` |
| `Aggregation.create().match().group().project().sort().limit().skip().unwind().out()` | 聚合管道阶段 | `Aggregation` |
| `MongoDB.get().openTransaction(ITrade/AbstractTrade<T>)` | 开启事务（副本集要求） | `IMongo` |

## 4. 标准代码模板

### 模板1：ymp-conf.properties一个MongoDB数据源完整配置

```properties
#-------------------------------------
# MongoDB持久化模块
#-------------------------------------
ymp.configs.persistence.mongodb.ds_default_name=default
ymp.configs.persistence.mongodb.ds_name_list=default

ymp.configs.persistence.mongodb.ds.default.username=clientuser
ymp.configs.persistence.mongodb.ds.default.password=12345678
ymp.configs.persistence.mongodb.ds.default.password_encrypted=false
ymp.configs.persistence.mongodb.ds.default.database_name=demo
ymp.configs.persistence.mongodb.ds.default.authentication_database_name=admin
ymp.configs.persistence.mongodb.ds.default.servers=localhost:27017
ymp.configs.persistence.mongodb.ds.default.collection_prefix=ymp_
ymp.configs.persistence.mongodb.ds.default.auto_connection=true
```

### 模板2：@Entity实体类 + CRUD + Query+Operator

```java
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
package com.example.mongodb.entity;

import net.ymate.platform.core.persistence.annotation.Comment;
import net.ymate.platform.core.persistence.annotation.Entity;
import net.ymate.platform.core.persistence.annotation.Property;
import net.ymate.platform.persistence.mongodb.support.BaseEntity;

import java.util.Date;

/**
 * 用户文档实体映射集合 ymp_user
 *
 * @author Example
 * @since 2.1.4-dev
 */
@Entity("user")
@Comment("用户集合")
public class UserEntity extends BaseEntity {

    public interface FIELDS {
        String NICKNAME = "nick_name";
        String AGE = "age";
        String GENDER = "gender";
        String CREATE_TIME = "create_time";
    }

    @Property(name = FIELDS.NICKNAME, nullable = false)
    @Comment("昵称")
    private String nickname;

    @Property(name = FIELDS.AGE)
    @Comment("年龄")
    private Integer age;

    @Property(name = FIELDS.GENDER)
    @Comment("性别：M男 F女")
    private String gender;

    @Property(name = FIELDS.CREATE_TIME, nullable = false)
    @Comment("创建时间")
    private Date createTime;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
```

```java
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
package com.example.mongodb;

import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.core.persistence.IResultSet;
import net.ymate.platform.core.persistence.Page;
import net.ymate.platform.persistence.mongodb.MongoDB;
import net.ymate.platform.persistence.mongodb.support.OrderBy;
import net.ymate.platform.persistence.mongodb.support.Operator;
import net.ymate.platform.persistence.mongodb.support.Query;
import com.example.mongodb.entity.UserEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * MongoDB CRUD使用示例
 *
 * @author Example
 * @since 2.1.4-dev
 */
public class MongoCrudExample {

    /**
     * 插入一条用户记录
     *
     * @param nickname 昵称
     * @param age      年龄
     * @return 插入后实体（含_id）
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public UserEntity insertUser(String nickname, Integer age) throws Exception {
        return MongoDB.get().openSession(session -> {
            UserEntity u = new UserEntity();
            u.setNickname(nickname);
            u.setAge(age);
            u.setGender("F");
            u.setCreateTime(new Date());
            return session.insert(u);
        });
    }

    /**
     * 条件查询分页用户
     *
     * @param minAge   最小年龄
     * @param gender   性别
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public IResultSet<UserEntity> queryUsers(int minAge, String gender, int pageNum, int pageSize) throws Exception {
        return MongoDB.get().openSession(session -> {
            Query q = Query.create()
                    .cond(UserEntity.FIELDS.AGE, Operator.create().gte(minAge))
                    .cond(UserEntity.FIELDS.GENDER, Operator.create().eq(gender));
            OrderBy orderBy = OrderBy.create().desc(UserEntity.FIELDS.CREATE_TIME);
            return session.find(UserEntity.class, q, orderBy, Page.create(pageNum).pageSize(pageSize));
        });
    }

    /**
     * 按_id更新性别
     *
     * @param id     主键_id
     * @param gender 新性别
     * @return 更新后实体
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public UserEntity updateGender(String id, String gender) throws Exception {
        return MongoDB.get().openSession(session -> {
            UserEntity u = new UserEntity();
            u.setId(id);
            u.setGender(gender);
            return session.update(u, Fields.create(UserEntity.FIELDS.GENDER));
        });
    }

    /**
     * 批量删除
     *
     * @param ids 主键集合
     * @return 删除条数
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public long deleteUsers(List<String> ids) throws Exception {
        return MongoDB.get().openSession(session -> session.delete(UserEntity.class, ids));
    }
}
```

### 模板3：聚合Aggregation示例（分组、投影、match）

```java
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
package com.example.mongodb;

import com.mongodb.client.MongoCollection;
import net.ymate.platform.core.persistence.Fields;
import net.ymate.platform.persistence.mongodb.MongoDB;
import net.ymate.platform.persistence.mongodb.support.Aggregation;
import net.ymate.platform.persistence.mongodb.support.Operator;
import net.ymate.platform.persistence.mongodb.support.OrderBy;
import net.ymate.platform.persistence.mongodb.support.Query;
import com.example.mongodb.entity.UserEntity;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB聚合查询示例
 *
 * @author Example
 * @since 2.1.4-dev
 */
public class MongoAggregationExample {

    /**
     * 按性别分组，统计人数与平均年龄，取前5个分组
     *
     * @return 聚合结果列表（_id=gender, count, avgAge）
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public List<Document> groupByGenderStats() throws Exception {
        return MongoDB.get().openSession(session -> {
            MongoCollection<Document> col = session.getCollection(UserEntity.class);
            List<Aggregation> pipeline = new ArrayList<>();
            // $match 过滤年龄>=18
            pipeline.add(Aggregation.create()
                    .match(Query.create().cond(UserEntity.FIELDS.AGE, Operator.create().gte(18))));
            // $group 分组
            pipeline.add(Aggregation.create().group(
                    Operator.create().field(UserEntity.FIELDS.GENDER),
                    Query.create()
                            .cond("count", Operator.create().sum(1))
                            .cond("avgAge", Operator.create().avg(UserEntity.FIELDS.AGE))
                            .cond("maxAge", Operator.create().max(UserEntity.FIELDS.AGE))
            ));
            // $project 投影输出字段（排除_id或重命名）
            pipeline.add(Aggregation.create().project(Fields.create("count", "avgAge", "maxAge")));
            // $sort 按count倒序
            pipeline.add(Aggregation.create().sort(OrderBy.create().desc("count")));
            // $limit 前5
            pipeline.add(Aggregation.create().limit(5));
            // 转为Bson列表执行
            List<org.bson.conversions.Bson> stages = new ArrayList<>();
            pipeline.forEach(a -> stages.add(a.toBson()));
            return col.aggregate(stages).into(new ArrayList<>());
        });
    }
}
```

### 模板4：GridFS上传/下载

```java
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
package com.example.mongodb;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import net.ymate.platform.persistence.mongodb.MongoDB;
import org.bson.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * GridFS文件存储示例
 *
 * @author Example
 * @since 2.1.4-dev
 */
public class MongoGridFsExample {

    private static final String BUCKET = "my_files";

    /**
     * 从本地文件上传到GridFS
     *
     * @param localFile 本地文件
     * @return 文件唯一标识fileId
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public String uploadFile(File localFile) throws Exception {
        return MongoDB.get().openGridFsSession(BUCKET, session -> {
            GridFSUploadOptions opts = new GridFSUploadOptions()
                    .metadata(new Document("contentType", "application/octet-stream")
                            .append("uploader", "ymp-example"));
            return session.upload(localFile, opts);
        });
    }

    /**
     * 从输入流上传指定文件名
     *
     * @param fileName 显示文件名
     * @param in       输入流
     * @return 文件唯一标识fileId
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public String uploadStream(String fileName, InputStream in) throws Exception {
        return MongoDB.get().openGridFsSession(BUCKET, session ->
                session.upload(fileName, in, new GridFSUploadOptions()));
    }

    /**
     * 按fileId下载到目标文件
     *
     * @param fileId  文件ID
     * @param destFile 目标本地文件
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public void downloadFile(String fileId, File destFile) throws Exception {
        try (OutputStream out = new FileOutputStream(destFile)) {
            MongoDB.get().openGridFsSession(BUCKET, session -> {
                session.download(fileId, out);
                return null;
            });
        }
    }

    /**
     * 查询文件元信息
     *
     * @param fileId 文件ID
     * @return GridFS文件元信息（含filename、length、uploadDate、metadata）
     * @throws Exception 可能的异常
     * @since 2.1.4-dev
     */
    public GridFSFile findFile(String fileId) throws Exception {
        return MongoDB.get().openGridFsSession(BUCKET, session -> session.find(fileId));
    }
}
```

## 5. 配置速查

### 5.1 配置文件最常改项

| Key | 默认值 | 说明 |
|-----|--------|------|
| `ymp.configs.persistence.mongodb.ds_default_name` | default | 默认数据源名称 |
| `ymp.configs.persistence.mongodb.ds_name_list` | default | 多数据源列表，竖线分隔 |
| `ymp.configs.persistence.mongodb.ds.<ds>.database_name` | - | 必选，MongoDB数据库名 |
| `ymp.configs.persistence.mongodb.ds.<ds>.servers` | - | 主机列表，格式 host:port，多个竖线分隔 |
| `ymp.configs.persistence.mongodb.ds.<ds>.username` | - | 认证用户名 |
| `ymp.configs.persistence.mongodb.ds.<ds>.password` | - | 认证密码 |
| `ymp.configs.persistence.mongodb.ds.<ds>.authentication_database_name` | admin | 认证数据库 |
| `ymp.configs.persistence.mongodb.ds.<ds>.connection_url` | - | 完整MongoDB连接URI（提供则忽略用户名/密码/主机等） |
| `ymp.configs.persistence.mongodb.ds.<ds>.collection_prefix` | - | 集合统一前缀 |
| `ymp.configs.persistence.mongodb.ds.<ds>.auto_connection` | false | 模块初始化即建立连接 |
| `ymp.configs.persistence.mongodb.ds.<ds>.password_encrypted` | false | 密码是否已加密 |
| `ymp.configs.persistence.mongodb.ds.<ds>.password_class` | - | 密码解密处理器`IPasswordProcessor`实现类 |
| `ymp.configs.persistence.mongodb.ds.<ds>.options_handler_class` | - | `IMongoClientOptionsHandler`自定义客户端参数 |

### 5.2 注解配置核心参数

**@MongoDataSource核心参数：**
- `name`：数据源名（必填）
- `databaseName`：数据库名（必填）
- `servers`：`String[]`主机列表
- `username`/`password`/`authenticationDatabaseName`
- `connectionUrl`：二选一，提供则覆盖独立参数
- `autoConnection`/`collectionPrefix`
- `passwordEncrypted`/`passwordClass`
- `optionsHandlerClass`

## 6. 常见坑点排查

| 现象 | 原因 | 解决 |
|------|------|------|
| `Transaction numbers are only allowed on a replica set member` 事务异常 | MongoDB未开启副本集或未连到PRIMARY节点 | 启用副本集；确保写入连接到PRIMARY；测试可使用单节点副本集 `mongod --replSet rs0` |
| `Cannot run 'listIndexes' in a multi-document transaction` GridFS在事务内失败 | MongoDB事务不支持GridFS等管理类命令 | GridFS上传下载移出事务；事务内仅做文档CRUD |
| 实体查询全部返回null字段 | @Property未指定`name`或集合前缀不一致 | 对照MongoDB实际字段名；检查`collection_prefix`与@Entity配合 |
| `Query.cond(Operator.eq(...))` 查不到但手动Bson可以 | `Operator.create()`要紧跟具体操作，空Operator导致$eq对null | 使用 `Operator.create().eq(val)` 链式写法；调试 `Query.toBson().toString()` |
| GridFS首次上传抛异常但文件仍上传成功 | MongoDB懒建桶时listCollections警告 | 忽略首次异常或手动`mongofiles`预先创建桶；后续正常 |
| 多数据源切换`openSession(dsName)`返回仍为默认 | `dsName`字符串与`ds_name_list`注册名不一致 | 检查properties或@MongoDataSource中name大小写；`ds_name_list`必须显式列出 |
