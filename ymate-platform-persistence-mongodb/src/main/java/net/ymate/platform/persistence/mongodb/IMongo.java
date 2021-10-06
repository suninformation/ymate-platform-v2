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

import com.mongodb.ClientSessionOptions;
import net.ymate.platform.core.beans.annotation.Ignored;
import net.ymate.platform.core.persistence.AbstractTrade;
import net.ymate.platform.core.persistence.IDataSourceRouter;
import net.ymate.platform.core.persistence.IPersistence;
import net.ymate.platform.core.persistence.ITrade;

/**
 * @author 刘镇 (suninformation@163.com) on 15/11/21 上午9:24
 */
@Ignored
public interface IMongo extends IPersistence<IMongoSession, IMongoConfig, IMongoConnectionHolder> {

    String MODULE_NAME = "persistence.mongodb";

    /**
     * 获取默认数据源适配器
     *
     * @return 返回数据源适配器对象
     * @throws Exception 可能产生的任何异常
     */
    IMongoDataSourceAdapter getDefaultDataSourceAdapter() throws Exception;

    /**
     * 获取指定源数据源适配器
     *
     * @param dataSourceName 数据源名称
     * @return 返回数据源适配器对象
     * @throws Exception 可能产生的任何异常
     */
    IMongoDataSourceAdapter getDataSourceAdapter(String dataSourceName) throws Exception;

    // ------

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param executor 会话执行器
     * @param <T>      执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openSession(IMongoSessionExecutor<T> executor) throws Exception;

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param connectionHolder 连接对象持有者
     * @param executor         会话执行器
     * @param <T>              执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openSession(IMongoConnectionHolder connectionHolder, IMongoSessionExecutor<T> executor) throws Exception;

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param dataSourceName 数据源名称
     * @param executor       会话执行器
     * @param <T>            执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openSession(String dataSourceName, IMongoSessionExecutor<T> executor) throws Exception;

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param dataSourceRouter 数据源路由对象
     * @param executor         会话执行器
     * @param <T>              执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openSession(IDataSourceRouter dataSourceRouter, IMongoSessionExecutor<T> executor) throws Exception;

    // ------

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param executor 会话执行器
     * @param <T>      执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openGridFsSession(IGridFsSessionExecutor<T> executor) throws Exception;

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param bucketName 桶名称
     * @param executor   会话执行器
     * @param <T>        执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openGridFsSession(String bucketName, IGridFsSessionExecutor<T> executor) throws Exception;

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param dataSourceName 数据源名称
     * @param bucketName     桶名称
     * @param executor       会话执行器
     * @param <T>            执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openGridFsSession(String dataSourceName, String bucketName, IGridFsSessionExecutor<T> executor) throws Exception;

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param connectionHolder 连接对象持有者
     * @param executor         会话执行器
     * @param <T>              执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openGridFsSession(IMongoConnectionHolder connectionHolder, IGridFsSessionExecutor<T> executor) throws Exception;

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param dataSourceRouter 数据源路由对象
     * @param executor         会话执行器
     * @param <T>              执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openGridFsSession(IDataSourceRouter dataSourceRouter, IGridFsSessionExecutor<T> executor) throws Exception;

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param connectionHolder 连接对象持有者
     * @param bucketName       桶名称
     * @param executor         会话执行器
     * @param <T>              执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openGridFsSession(IMongoConnectionHolder connectionHolder, String bucketName, IGridFsSessionExecutor<T> executor) throws Exception;

    /**
     * 开启会话并执行会话执行器接口逻辑(执行完毕会话将自动关闭)
     *
     * @param dataSourceRouter 数据源路由对象
     * @param bucketName       桶名称
     * @param executor         会话执行器
     * @param <T>              执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openGridFsSession(IDataSourceRouter dataSourceRouter, String bucketName, IGridFsSessionExecutor<T> executor) throws Exception;

    // ------

    /**
     * 开启数据库文件储存会话(注意一定记得关闭会话)
     *
     * @return 返回会话对象
     * @throws Exception 可能产生的异常
     */
    IGridFsSession openGridFsSession() throws Exception;

    /**
     * 开启数据库文件储存会话(注意一定记得关闭会话)
     *
     * @param bucketName 桶名称
     * @return 返回会话对象
     * @throws Exception 可能产生的异常
     */
    IGridFsSession openGridFsSession(String bucketName) throws Exception;

    /**
     * 开启数据库文件储存会话(注意一定记得关闭会话)
     *
     * @param dataSourceName 数据源名称
     * @param bucketName     桶名称
     * @return 返回会话对象
     * @throws Exception 可能产生的异常
     */
    IGridFsSession openGridFsSession(String dataSourceName, String bucketName) throws Exception;

    /**
     * 开启数据库文件储存会话(注意一定记得关闭会话)
     *
     * @param connectionHolder 连接对象持有者
     * @param bucketName       桶名称
     * @return 返回会话对象
     * @throws Exception 可能产生的异常
     */
    IGridFsSession openGridFsSession(IMongoConnectionHolder connectionHolder, String bucketName) throws Exception;

    /**
     * 开启数据库文件储存会话(注意一定记得关闭会话)
     *
     * @param connectionHolder 连接对象持有者
     * @return 返回会话对象
     * @throws Exception 可能产生的异常
     */
    IGridFsSession openGridFsSession(IMongoConnectionHolder connectionHolder) throws Exception;

    /**
     * 开启数据库文件储存会话(注意一定记得关闭会话)
     *
     * @param dataSourceRouter 数据源路由对象
     * @param bucketName       桶名称
     * @return 返回会话对象
     * @throws Exception 可能产生的异常
     */
    IGridFsSession openGridFsSession(IDataSourceRouter dataSourceRouter, String bucketName) throws Exception;

    /**
     * 开启数据库文件储存会话(注意一定记得关闭会话)
     *
     * @param dataSourceRouter 数据源路由对象
     * @return 返回会话对象
     * @throws Exception 可能产生的异常
     */
    IGridFsSession openGridFsSession(IDataSourceRouter dataSourceRouter) throws Exception;

    // ------

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param trade 事务业务操作对象
     * @throws Exception 可能产生的任何异常
     */
    void openTransaction(ITrade trade) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param trade                事务业务操作对象
     * @param clientSessionOptions 事务配置对象
     * @throws Exception 可能产生的任何异常
     */
    void openTransaction(ITrade trade, ClientSessionOptions clientSessionOptions) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param connectionHolder 连接对象持有者
     * @param trade            事务业务操作对象
     * @throws Exception 可能产生的任何异常
     */
    void openTransaction(IMongoConnectionHolder connectionHolder, ITrade trade) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param connectionHolder     连接对象持有者
     * @param trade                事务业务操作对象
     * @param clientSessionOptions 事务配置对象
     * @throws Exception 可能产生的任何异常
     */
    void openTransaction(IMongoConnectionHolder connectionHolder, ITrade trade, ClientSessionOptions clientSessionOptions) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param dataSourceRouter 数据源路由对象
     * @param trade            事务业务操作对象
     * @throws Exception 可能产生的任何异常
     */
    void openTransaction(IDataSourceRouter dataSourceRouter, ITrade trade) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param dataSourceRouter     数据源路由对象
     * @param trade                事务业务操作对象
     * @param clientSessionOptions 事务配置对象
     * @throws Exception 可能产生的任何异常
     */
    void openTransaction(IDataSourceRouter dataSourceRouter, ITrade trade, ClientSessionOptions clientSessionOptions) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param dataSourceName 数据源名称
     * @param trade          事务业务操作对象
     * @throws Exception 可能产生的任何异常
     */
    void openTransaction(String dataSourceName, ITrade trade) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param dataSourceName       数据源名称
     * @param trade                事务业务操作对象
     * @param clientSessionOptions 事务配置对象
     * @throws Exception 可能产生的任何异常
     */
    void openTransaction(String dataSourceName, ITrade trade, ClientSessionOptions clientSessionOptions) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param trade 事务业务操作对象
     * @param <T>   执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openTransaction(AbstractTrade<T> trade) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param trade                事务业务操作对象
     * @param clientSessionOptions 事务配置对象
     * @param <T>                  执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openTransaction(AbstractTrade<T> trade, ClientSessionOptions clientSessionOptions) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param connectionHolder     连接对象持有者
     * @param trade                事务业务操作对象
     * @param clientSessionOptions 事务配置对象
     * @param <T>                  执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openTransaction(IMongoConnectionHolder connectionHolder, AbstractTrade<T> trade, ClientSessionOptions clientSessionOptions) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param connectionHolder 连接对象持有者
     * @param trade            事务业务操作对象
     * @param <T>              执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openTransaction(IMongoConnectionHolder connectionHolder, AbstractTrade<T> trade) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param dataSourceRouter 数据源路由对象
     * @param trade            事务业务操作对象
     * @param <T>              执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openTransaction(IDataSourceRouter dataSourceRouter, AbstractTrade<T> trade) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param dataSourceRouter     数据源路由对象
     * @param trade                事务业务操作对象
     * @param clientSessionOptions 事务配置对象
     * @param <T>                  执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openTransaction(IDataSourceRouter dataSourceRouter, AbstractTrade<T> trade, ClientSessionOptions clientSessionOptions) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param dataSourceName 数据源名称
     * @param trade          事务业务操作对象
     * @param <T>            执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openTransaction(String dataSourceName, AbstractTrade<T> trade) throws Exception;

    /**
     * 开启事务(执行完毕事务将自动关闭, 任何异常将导致事务回滚)
     *
     * @param dataSourceName       数据源名称
     * @param trade                事务业务操作对象
     * @param clientSessionOptions 事务配置对象
     * @param <T>                  执行结果对象类型
     * @return 返回执行结果
     * @throws Exception 可能产生的任何异常
     */
    <T> T openTransaction(String dataSourceName, AbstractTrade<T> trade, ClientSessionOptions clientSessionOptions) throws Exception;

    /**
     * GridFS常量
     */
    class GridFs {

        public static final String ID = Opt.ID;

        public static final String FILE_NAME = "filename";

        public static final String CHUNK_SIZE = "chunkSize";

        public static final String UPLOAD_DATE = "uploadDate";

        public static final String LENGTH = "length";

        public static final String MD5 = "md5";
    }

    /**
     * 操作符常量
     */
    class Opt {

        /**
         * 主键
         */
        public static final String ID = "_id";

        // 查询条件

        public static final String CMP = "$cmp";
        public static final String EQ = "$eq";
        public static final String GT = "$gt";
        public static final String GTE = "$gte";
        public static final String LT = "$lt";
        public static final String LTE = "$lte";
        public static final String NE = "$ne";
        public static final String IN = "$in";
        public static final String NIN = "$nin";

        public static final String SLICE = "$slice";

        //

        public static final String AND = "$and";
        public static final String OR = "$or";
        public static final String NOT = "$not";
        public static final String NOR = "$nor";

        public static final String EXISTS = "$exists";
        public static final String TYPE = "$type";
        public static final String MOD = "$mod";
        public static final String REGEX = "$regex";

        public static final String TEXT = "$text";
        public static final String SEARCH = "$search";
        public static final String LANGUAGE = "$language";

        public static final String WHERE = "$where";

        public static final String SUBSTR = "$substr";

        //

        public static final String NEAR = "$near";
        public static final String NEAR_SPHERE = "$nearSphere";

        public static final String GEO_WITHIN = "$geoWithin";
        public static final String GEO_INTERSECTS = "$geoIntersects";

        public static final String BOX = "$box";
        public static final String POLYGON = "$polygon";
        public static final String CENTER = "$center";
        public static final String CENTER_SPHERE = "$centerSphere";

        //

        public static final String ALL = "$all";
        public static final String ELEM_MATCH = "$elemMatch";
        public static final String SIZE = "$size";

        //

        public static final String SET = "$set";
        public static final String UNSET = "$unset";
        public static final String INC = "$inc";
        public static final String MUL = "$mul";
        public static final String RENAME = "$rename";
        public static final String SET_ON_INSERT = "$setOnInsert";
        public static final String PULL = "$pull";
        public static final String PULL_ALL = "$pullAll";
        public static final String EACH = "$each";
        public static final String POSITION = "$position";
        public static final String POP = "$pop";

//        public static final String BIT = "$bit"; // Current Not Support.

        public static final String SUM = "$sum";
        public static final String AVG = "$avg";
        public static final String FIRST = "$first";
        public static final String LAST = "$last";
        public static final String MAX = "$max";
        public static final String MIN = "$min";

//        public static final String CURRENT_DATE = "$currentDate"; // Current Not Support.

        public static final String PUSH = "$push";
        public static final String PUSH_ALL = "$pushAll";
        public static final String ADD_TO_SET = "$addToSet";
        public static final String ISOLATED = "$isolated";

        // 聚合类型

        public static final String PROJECT = "$project";
        public static final String MATCH = "$match";
        public static final String REDACT = "$redact";

        public static final String LIMIT = "$limit";
        public static final String SKIP = "$skip";
        public static final String UNWIND = "$unwind";
        public static final String GROUP = "$group";
        public static final String SORT = "$sort";
        public static final String OUT = "$out";

        public static final String META = "$meta";
    }
}
